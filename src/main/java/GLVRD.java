import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import org.apache.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

class GlvrdResponse implements GlvrdResponsable {
    @JsonProperty("status")
    public String status;

    @JsonProperty("code")
    public String code;

    @JsonProperty("message")
    public String message;

    public String getStatus() {
        return this.status;
    }

    @JsonProperty("score")
    public String score;

    @JsonProperty("fragments")
    public List<Fragment> fragments;
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

    public String getStatus() {
        return this.status;
    }

    @JsonProperty("hints")
    public JsonNode hints;
}

interface GlvrdResponsable {
    String getStatus();
}

public class GLVRD {
    protected String apiKey;
    private static final String apiHost = "https://glvrd.ru/api";
    private static final HashMap<String, GlvrdHints> hashMapHints = new HashMap<>();
    private static final HashMap<String, GlvrdResponse> hashMapText = new HashMap<>();

    public GLVRD(String apiKey) {
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
                throw new Exception(map.getStatus());
            }
            return map;
        }

        public HttpURLConnection createConnection() throws Exception {
            final var url = new URL(apiHost + this.url);
            final var con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("X-GLVRD-KEY", apiKey);
            con.setUseCaches(true);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");

            return con;
        }

        public String request(String urlParameters, HttpURLConnection con) throws Exception {
            final var writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(urlParameters);
            writer.close();

            final int status = con.getResponseCode();
            switch (status) {
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
            throw new Exception("Invalid response " + urlParameters + "; status: " + status);
        }
    }

    public GlvrdHints hints(String ids) throws Exception {
        if (hashMapHints.containsKey(ids)) {
            return hashMapHints.get(ids);
        }

        final var requestBuilder = new RequestBuilder("/v3/hints/");
        final var con = requestBuilder.createConnection();
        final var string = requestBuilder.request("ids=" + ids, con);
        final var result = requestBuilder.responseParser(string, GlvrdHints.class);
        hashMapHints.put(ids, result);

        return result;
    }

    public GlvrdResponse proofRead(String text) throws Exception {
        if (hashMapText.containsKey(text)) {
            return hashMapText.get(text);
        }

        final var requestBuilder = new RequestBuilder("/v3/proofread/");
        final var con = requestBuilder.createConnection();
        final var string = requestBuilder.request("text=" + text, con);
        final var result = requestBuilder.responseParser(string, GlvrdResponse.class);
        hashMapText.put(text, result);

        return result;
    }
}
