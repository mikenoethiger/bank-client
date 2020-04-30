package bank.rest.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class HttpHelper {

    public static URI createURIOrFail(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formEncode(String... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("even amount of values expected");
        }
        var map = new HashMap<String, String>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(values[i], values[i+1]);
        }
        return formEncode(map);
    }

    public static String formEncode(Map<String,String> values) {
        String encoded = "";
        for (Map.Entry<String, String> e : values.entrySet()) {
            encoded += e.getKey() + "=" + e.getValue() + "&";
        }
        // remove last &
        encoded = encoded.substring(0, encoded.length()-1);
        return encoded;
    }

    public static HttpResponse<String> clientSendOrFail(HttpRequest r, HttpClient client, int expectedStatus) throws IOException {
        try {
            HttpResponse<String> response = client.send(r, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != expectedStatus) {
                throw new IOException(response.toString());
            }
            return response;
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public static HttpResponse<String> clientSendOrFail(HttpRequest r, HttpClient client) throws IOException {
        try {
            return client.send(r, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}
