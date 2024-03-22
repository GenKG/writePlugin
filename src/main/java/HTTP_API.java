import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HTTP_API extends GLVRD_API {
    protected String apiKey;
    protected static final HashMap<String, GlvrdHints> hashMapHints = new HashMap<>();
    protected static final HashMap<String, GlvrdHTTPResponse> hashMapText = new HashMap<>();

    public HTTP_API(String apiKey) {
        this.apiKey = apiKey;
    }

    class RequestBuilder extends RequestHelper {
        RequestBuilder(final String url) {
            this.url = url;
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

        public String request(String urlParameters, HttpURLConnection con) throws Exception  {
            if (con.getDoOutput()) {
                final var writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(urlParameters);
                writer.close();
            }

            return this.getData(con);
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

        return requestBuilder.responseParser(string, GlvrdStatus.class);
    }
}
