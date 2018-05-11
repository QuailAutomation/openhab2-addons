package org.openhab.binding.enphaseenvoy.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.enphaseenvoy.protocol.InverterProduction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class InverterParser {

    private static final Logger logger = LoggerFactory.getLogger(InverterParser.class);
    @SuppressWarnings("unused")
    private EnphaseEnvoyBridgeHandler bridgeHandler;

    final Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();

    public InverterParser(EnphaseEnvoyBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    public List<InverterProduction> getInverterData(EnphaseEnvoyBridgeConfiguration config) throws IOException {
        String json = this.getInverters(config);
        json = json.trim();
        JsonReader parser = new JsonReader(new StringReader(json));
        parser.setLenient(true);
        Gson gson = new GsonBuilder().create();
        List<InverterProduction> result = new ArrayList<InverterProduction>();
        parser.beginArray();
        boolean ended = false;
        while (!ended) {
            if (parser.peek() != JsonToken.END_ARRAY) {
                InverterProduction production = gson.fromJson(parser, InverterProduction.class);
                result.add(production);
            } else {
                ended = true;
            }
        }
        parser.close();
        return result;
    }

    private String getInverters(EnphaseEnvoyBridgeConfiguration config) {
        String passwd = config.password;

        DigestAuthenticator authenticator = new DigestAuthenticator(new Credentials("envoy", passwd));
        OkHttpClient client = new OkHttpClient.Builder()
                .authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                .addInterceptor(new AuthenticationCacheInterceptor(authCache)).build();

        String url = "http://" + config.hostname + "/api/v1/production/inverters";
        Request request = new Request.Builder().url(url)
                .addHeader("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("Cache-Control", "no-cache").addHeader("Connection", "keep-alive").get().build();
        try {
            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();
            String result = body.string();
            result = result.replace("<html><body><pre>", "").replace("</pre></body></html>", "");
            return result;
        } catch (IOException e) {
            logger.warn("getting inverter data failed", e);
        }
        return null;
    }
}
