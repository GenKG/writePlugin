import org.apache.commons.httpclient.HttpException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class JS_API extends GLVRD_API {
    protected static final HashMap<String, GlvrdJSResponse> hashMapText = new HashMap<>();

    class RequestBuilder extends RequestHelper {
        RequestBuilder(final String url) {
            this.url = url;
        }

        public HttpURLConnection createConnectionPost() throws Exception {
            var url = new URL(apiHost + this.url);
            var con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setUseCaches(true);
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Accept", "*/*");
            con.setRequestProperty("Origin", "https://glvrd.ru");
            con.setRequestProperty("Host", "glvrd.ru");
            con.setRequestProperty("Referer", "https://glvrd.ru/");

            return con;
        }

        public String request(String data, HttpURLConnection con) throws Exception {
            byte[] out = data.getBytes(StandardCharsets.UTF_8);
            con.setRequestProperty("Content-Length", Integer.toString(out.length));
            OutputStream stream;
            try {
                stream = con.getOutputStream();
                stream.write(out);
            } catch (Exception e) {
                throw new HttpException("Сеть недоступна");
            }

            return this.getData(con);
        }
    }

    public GlvrdJSResponse proofread(String text) throws Exception {
        if (hashMapText.containsKey(text)) {
            return hashMapText.get(text);
        }

        final var requestBuilder = new RequestBuilder("/v0/@proofread/");
        final var con = requestBuilder.createConnectionPost();

        var chunks = URLEncoder.encode(text, StandardCharsets.UTF_8);
        final var string = requestBuilder.request("chunks=" + chunks, con);
        final var result = requestBuilder.responseParser(string, GlvrdJSResponse.class);
        hashMapText.put(text, result);

        return result;
    }
}
