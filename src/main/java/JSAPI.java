import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpStatus;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

class GlvrdJSResponse implements GlvrdResponsable {
    @JsonProperty("status")
    public String status;

    @JsonProperty("code")
    public String code;

    @JsonProperty("name")
    public String name;

    @JsonProperty("message")
    public String message;

    @JsonProperty("score")
    public String score;

    @JsonProperty("fragments")
    public List<List<FragmentInJS>> fragments;

    public String getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

    @JsonProperty("hints")
    public JsonNode hints;

    @JsonProperty("tabs")
    public List<Tab> tabs;

    @JsonProperty("stylesCss")
    public String stylesCss;
}

class Tab {
    @JsonProperty("code")
    public String code;

    @JsonProperty("name")
    public String name;

    @JsonProperty("textColor")
    public String textColor;

    @JsonProperty("lineColor")
    public String lineColor;
}

class FragmentInJS {
    @JsonProperty("start")
    public int start;

    @JsonProperty("end")
    public int end;

    @JsonProperty("hint")
    public String hint;
}

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
                    // todo ???????? ?????????? ?????????????????? ???????????? ???????????? ???????????????????????? ??????????
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
