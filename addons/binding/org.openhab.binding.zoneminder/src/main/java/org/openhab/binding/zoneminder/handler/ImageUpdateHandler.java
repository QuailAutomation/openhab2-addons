package org.openhab.binding.zoneminder.handler;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.zoneminder.ZoneMinderProperties;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderBridgeServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageUpdateHandler {

    private Logger logger = LoggerFactory.getLogger(ImageUpdateHandler.class);
    private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);
    private ZoneMinderThingMonitorHandler thingHandler;

    @SuppressWarnings("unused")
    private Runnable updater;
    @SuppressWarnings("unused")
    private ScheduledFuture<?> task = null;
    private String cachedVideoURL = null;

    ImageUpdateHandler(ZoneMinderServerBridgeHandler bridge, ZoneMinderThingMonitorHandler handler) {
        this.thingHandler = handler;
        this.updater = new Runnable() {

            @Override
            public void run() {
                ImageUpdateHandler.this.refreshImage();
            }
        };
    }

    private ZoneMinderBridgeServerConfig getBridgeConfig() {
        Bridge bridge = this.thingHandler.getBridge();
        if (bridge == null) {
            return null;
        }
        return bridge.getConfiguration().as(ZoneMinderBridgeServerConfig.class);
    }

    private Integer getImageScale() {
        Integer targetSize = this.thingHandler.getMonitorConfig().getMaxImageSize();
        Map<String, String> props = this.thingHandler.getThing().getProperties();
        String sorgX = props.get(ZoneMinderProperties.PROPERTY_MONITOR_IMAGE_WIDTH);
        String sorgY = props.get(ZoneMinderProperties.PROPERTY_MONITOR_IMAGE_HEIGHT);
        if (sorgX == null || sorgY == null) {
            return 100;
        }
        Integer orgX = Integer.decode(sorgX);
        Integer orgY = Integer.decode(sorgY);
        Integer orgSize = Math.max(orgX, orgY);
        return Math.round((targetSize * 100) / orgSize);
    }

    private String getMonitorID() {
        return this.thingHandler.getMonitorConfig().getId();
    }

    private Thing getThing() {
        return this.thingHandler.getThing();
    }

    private URL buildImageURL() throws MalformedURLException {

        // this is actually pretty expensive (since it uses reflection to create the config object),
        // so only do this once for all options
        ZoneMinderBridgeServerConfig config = this.getBridgeConfig();
        if (config == null) {
            throw new IllegalStateException("unable to get bridge config, bridge down?");
        }
        String proto = config.getProtocol();
        String host = config.getHostName();
        Integer port = config.getHttpPort();
        String basepath = config.getServerBasePath();
        String user = config.getUserName();
        String pass = config.getPassword();
        Integer scale = this.getImageScale();

        String url = proto + "://" + host + ":" + port.toString() + basepath + "/cgi-bin/nph-zms?mode=single&";
        url += "scale=" + scale.toString() + "&monitor=" + this.getMonitorID();
        if (user != null && user.length() > 0) {
            url += "&user=" + user + "&pass=" + pass;
        }
        return new URL(url);
    }

    private String buildVideoURL() {

        // this is actually pretty expensive (since it uses reflection to create the config object),
        // so only do this once for all options
        ZoneMinderBridgeServerConfig bridgeConfig = this.getBridgeConfig();
        if (bridgeConfig == null) {
            throw new IllegalStateException("unable to get bridge config (bridge == null), bridge down?");
        }
        String proto = bridgeConfig.getProtocol();
        String host = bridgeConfig.getHostName();
        Integer port = bridgeConfig.getHttpPort();
        String basepath = bridgeConfig.getServerBasePath();
        String user = bridgeConfig.getUserName();
        String pass = bridgeConfig.getPassword();
        Integer scale = this.getImageScale();
        Integer framerate = this.thingHandler.getMonitorConfig().getVideoFramerate();
        String encoding = this.thingHandler.getMonitorConfig().getVideoEncoding();
        String mode = "mpeg";
        String format = null;
        if (encoding.equalsIgnoreCase("mjpeg")) {
            mode = "jpeg";
            encoding = null;
        }
        if (encoding != null && encoding.length() > 0) {
            format = encoding.toLowerCase();
        }

        String url = proto + "://" + host + ":" + port.toString() + basepath + "/cgi-bin/nph-zms?";
        url += "scale=" + scale.toString() + "&monitor=" + this.getMonitorID() + "&mode=" + mode + "&buffer=1000";

        if (format != null) {
            url += "&format=" + format;
        }
        if ("jpeg".equalsIgnoreCase(mode)) {
            url += "&maxfps=30&rate=" + framerate;
        }
        if (user != null && user.length() > 0) {
            url += "&user=" + user;
            if (pass != null) {
                url += "&pass=" + pass;
            }
        }
        return url;
    }

    public void start() {
        // if (this.task != null) {
        // logger.error("{}: image updater already running for {}", this.getThing().getUID().getId(),
        // this.getThing().getLabel());
        // return;
        // }
        //
        // Integer interval = this.getImageUpdateRate();
        // if (interval == 0) {
        // return;
        // }
        //
        // this.task = this.bridge.startTask(this.updater, 100, interval, TimeUnit.SECONDS);
    }

    public void stop() {
        // if (this.task == null) {
        // return;
        // }
        //
        // this.bridge.stopTask(this.task);
    }

    RawType getImage() throws Exception {
        URL url = this.buildImageURL();
        return new RawType(this.readImage(url).toByteArray(), "image/jpeg");
    }

    StringType getVideoURL() {
        String url = this.buildVideoURL();
        if (url.equals(this.cachedVideoURL)) {
            return null;
        }
        this.cachedVideoURL = url;
        return new StringType(url);
    }

    @SuppressWarnings("unused")
    private void refreshImage() {
        if (refreshInProgress.compareAndSet(false, true)) {
            try {
                for (Channel cx : this.getThing().getChannels()) {
                    if ("Image".equals(cx.getAcceptedItemType())) {
                        try {
                            final URL url = this.buildImageURL();
                            this.thingHandler.updateState(cx.getUID(),
                                    new RawType(readImage(url).toByteArray(), "image/jpeg"));
                        } catch (Exception e) {
                            logger.warn("could not update value: {}", getThing(), e);
                        }
                    }
                }
            } finally {
                refreshInProgress.set(false);
            }
        }
    }

    private ByteArrayOutputStream readImage(URL url) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        URI uri = url.toURI();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        HttpGet httpget = new HttpGet(uri);
        CloseableHttpResponse response = httpclient.execute(httpget, context);
        try {
            HttpEntity entity = response.getEntity();
            IOUtils.copy(entity.getContent(), baos);
            entity.getContent();
        } finally {
            response.close();
        }
        return baos;
    }
}
