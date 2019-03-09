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
                logger.error("Error with websocket communication", arg0);

            }

            @Override
            public void onWebSocketConnect(Session arg0) {
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
                logger.info("Reconnecting via Websocket to Isy.");
                if (future != null) {
                    future.cancel(true);
                }
                connect();

            }

            @Override
            public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {
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
            if (!event.getControl().startsWith("_")) {
                if (listener != null) {
                    listener.onModelChanged(event);
                }
            } else if ("_1".equals(event.getControl()) && "6".equals(event.getAction())) {
                listener.onVariableChanged(event.getEventInfo().getVariableEvent());
            } else {
                listener.stillAlive();
            }
        }

    }

}