import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpStatus;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class JSAPI extends GLVRD_API {
    protected static final HashMap<String, GlvrdJSResponse> hashMapText = new HashMap<>();

    class RequestBuilder {
        private String url;

        RequestBuilder(final String url) {
            this.url = url;
        }

        public <T extends GlvrdResponsable> T responseParser(String response, Class<T> tClass) throws Exception {
            final ObjectMapper mapper = new ObjectMapper();
            T map = mapper.readValue(response, tClass);
            if (!map.getStatus().equals("ok")) {
                throw new Exception(map.getMessage());
            }
            return map;
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
            OutputStream stream = con.getOutputStream();
            stream.write(out);

            switch (con.getResponseCode()) {
                case 429:
                    // todo надо слать повторный запрос спустя определенное время
                    break;

                case HttpStatus.SC_OK:
                case HttpStatus.SC_CREATED:
                    var br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    var sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();

                    return sb.toString();
            }
            throw new Exception(con.getResponseCode() + " " + con.getResponseMessage());
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
