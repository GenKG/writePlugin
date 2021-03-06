import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

class GlvrdStatus implements GlvrdResponsable {
    @JsonProperty("status")
    public String status;

    @JsonProperty("code")
    public String code;

    @JsonProperty("name")
    public String name;

    @JsonProperty("message")
    public String message;

    @JsonProperty("max_text_length")
    public int max_text_length;

    @JsonProperty("max_hints_count")
    public int max_hints_count;

    @JsonProperty("period_underlimit")
    public boolean period_underlimit;

    @JsonProperty("frequency_underlimit")
    public boolean frequency_underlimit;

    public String getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}

class GlvrdHTTPResponse implements GlvrdResponsable {
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
    public List<Fragment> fragments;

    public String getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}

class Fragment {
    @JsonProperty("start")
    public int start;

    @JsonProperty("end")
    public int end;

    @JsonProperty("hint_id")
    public String hint_id;
}

class GlvrdHints implements GlvrdResponsable {
    @JsonProperty("status")
    public String status;

    @JsonProperty("message")
    public String message;

    @JsonProperty("hints")
    public JsonNode hints;

    @Override
    public String getStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}

interface GlvrdResponsable {
    String getStatus();

    String getMessage();
}

public class HTTPAPI extends GLVRD_API {
    protected String apiKey;
    protected static final HashMap<String, GlvrdHints> hashMapHints = new HashMap<>();
    protected static final HashMap<String, GlvrdHTTPResponse> hashMapText = new HashMap<>();

    public HTTPAPI(String apiKey) {
        this.apiKey = apiKey;
    }

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
            final var url = new URL(apiHost + this.url);
            final var con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("X-GLVRD-KEY", apiKey);
            con.setUseCaches(true);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");

            return con;
        }

        public HttpURLConnection createConnectionGet() throws Exception {
            final var url = new URL(apiHost + this.url);
            final var con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("X-GLVRD-KEY", apiKey);
            con.setUseCaches(true);
            con.setRequestMethod("GET");

            return con;
        }

        public String request(String urlParameters, HttpURLConnection con) throws Exception {
            if (con.getDoOutput()) {
                final var writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(urlParameters);
                writer.close();
            }

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

    public GlvrdHints hints(String ids) throws Exception {
        if (hashMapHints.containsKey(ids)) {
            return hashMapHints.get(ids);
        }

        final var requestBuilder = new RequestBuilder("/v3/hints/");
        final var con = requestBuilder.createConnectionPost();
        final var string = requestBuilder.request("ids=" + URLEncoder.encode(ids, StandardCharsets.UTF_8), con);
        final var result = requestBuilder.responseParser(string, GlvrdHints.class);
        hashMapHints.put(ids, result);

        return result;
    }

    public GlvrdHTTPResponse proofread(String text) throws Exception {
        if (hashMapText.containsKey(text)) {
            return hashMapText.get(text);
        }

        final var requestBuilder = new RequestBuilder("/v3/proofread/");
        final var con = requestBuilder.createConnectionPost();
        final var string = requestBuilder.request("text=" + URLEncoder.encode(text, StandardCharsets.UTF_8), con);
        final var result = requestBuilder.responseParser(string, GlvrdHTTPResponse.class);
        hashMapText.put(text, result);

        return result;
    }

    public GlvrdStatus status() throws Exception {
        final var requestBuilder = new RequestBuilder("/v3/status");
        final var con = requestBuilder.createConnectionGet();
        final var string = requestBuilder.request("", con);
        final var result = requestBuilder.responseParser(string, GlvrdStatus.class);

        return result;
    }
}
