package org.openhab.binding.isy.internal;

import java.net.URI;
import java.util.concurrent.Future;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.isy.internal.protocol.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.thoughtworks.xstream.XStream;

public class IsyWebSocketSubscription {

    private Logger logger = LoggerFactory.getLogger(IsyWebSocketSubscription.class);
    private ISYModelChangeListener listener;
    private String connectUrl;
    private String authenticationHeader;
    Future<Session> future = null;
    XStream xStream;
    private WebSocketClient client;

    public IsyWebSocketSubscription(String url, String authenticationHeader, ISYModelChangeListener listener,
            XStream xStream) {
        this.listener = listener;
        this.connectUrl = url;
        this.authenticationHeader = authenticationHeader;
        this.xStream = xStream;
    }

    public void disconnect() {
        logger.debug("Disconnecting Isy web socket subscription");
        if (client != null) {
            client.destroy();
            listener = null;
            client = null;

        }

    }

    public void connect() {

        client = new WebSocketClient();
        WebSocketListener webSocketListener = new WebSocketListener() {

            @Override
            public void onWebSocketText(String arg0) {
                parseXml(arg0);

            }

            @Override
            public void onWebSocketError(Throwable arg0) {
                // TODO result needs to feed back to binding
                logger.error("Error with websocket communication", arg0);

            }

            @Override
            public void onWebSocketConnect(Session arg0) {
                // TODO result needs to feed back to binding
                logger.debug("Socket Connected: " + arg0);
                listener.onDeviceOnLine();

            }

            @Override
            public void onWebSocketClose(int arg0, String arg1) {
                listener.onDeviceOffLine();
                logger.debug("Socket Closed: [{}]{} ", arg0, arg1);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    logger.error("Socket Interrupted", e);
                }
                // TODO: should not automatically reconnect, only attempt when bridge starts
                // result of attempt needs to feed back to binding
                logger.info("Reconnecting via Websocket to Isy.");
                if (future != null) {
                    future.cancel(true);
                }
                connect();

            }

            @Override
            public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {
                // TODO result needs to feed back to binding
                logger.warn("Unexpected binary data from websocket {}", arg0);
            }
        };
        try {
            client.start();
            URI echoUri = new URI("ws://" + this.connectUrl + "/rest/subscribe");
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setSubProtocols("ISYSUB");
            request.setHeader("Authorization", this.authenticationHeader);
            request.setHeader("Sec-WebSocket-Version", "13");
            request.setHeader("Origin", "com.universal-devices.websockets.isy");
            Future<Session> future = client.connect(webSocketListener, echoUri, request);

            future.get();
            logger.info("Connecting to :" + echoUri);
        } catch (Exception e) {
            logger.error("Error establishing websocket subscription", e);
        }

    }

    private void parseXml(String message) {
        logger.trace("Parsing message: [{}]", message);

        Object messageObj;
        try {
            messageObj = xStream.fromXML(message);
        } catch (Exception e) {
            logger.error("Error parsing message [{}]", message);
            return;
        }
        if (messageObj instanceof Event) {
            Event event = (Event) messageObj;
            logger.debug("Node '{}' got control message '{}' action '{}'",
                    Strings.isNullOrEmpty(event.getNode()) ? "n/a" : event.getNode(), event.getControl(),
                    event.getAction());

            if (this.listener == null) {
                return;
            }

            // if control doesn't start with '_', it's likely a node value update ('ST', 'DON', 'DFOF', etc)
            if (!event.getControl().startsWith("_")) {
                // value update
                listener.onNodeChanged(event);
            } else if ("_1".equals(event.getControl()) && "6".equals(event.getAction())) {
                listener.onVariableChanged(event.getEventInfo().getVariableEvent());
            } else if ("_3".equals(event.getControl())) {
                if ("ND".equals(event.getAction())) {
                    logger.debug("Device added {}", event.getNode());
                    this.listener.onNodeAdded(event);
                } else if ("NN".equals(event.getAction())) {
                    logger.debug("Device renamed {}", event.getNode());
                    this.listener.onNodeRenamed(event);
                } else if ("NR".equals(event.getAction())) {
                    // if device part of a scene, there is no extra message from the web service,
                    // thus need to remove from linked scenes as well
                    // also, device remove removes all sub devices, but each sub device has it's own remove WS message
                    logger.debug("Device removed {}", event.getNode());
                    this.listener.onNodeRemoved(event);
                } else if ("GD".equals(event.getAction())) {
                    logger.debug("Scene added {}", event.getNode());
                    this.listener.onSceneAdded(event);
                } else if ("GN".equals(event.getAction())) {
                    logger.debug("Scene renamed {}", event.getNode());
                    this.listener.onSceneRenamed(event);
                } else if ("GR".equals(event.getAction())) {
                    // this also needs to initiate removing all links to the scene
                    logger.debug("Scene removed {}", event.getNode());
                    this.listener.onSceneRemoved(event);
                } else if ("MV".equals(event.getAction())) {
                    logger.debug("Scene link added {}", event.getNode());
                    this.listener.onSceneLinkAdded(event);
                } else if ("RG".equals(event.getAction())) {
                    logger.debug("Scene link removed {}", event.getNode());
                    this.listener.onSceneLinkRemoved(event);
                }
            }
        }
    }
}