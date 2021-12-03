import org.apache.http.HttpStatus;
import java.util.List;
import java.net.*;
import java.io.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

class GlvrdResponse implements GlvrdResponsable {
    @JsonProperty("status")
    public String status;

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
    public Object hints;
}

interface GlvrdResponsable {
    String getStatus();
}

public class GLVRD {
    protected String apiKey;
    static final String apiHost = "https://glvrd.ru/api";

    public GLVRD(String apiKey) {
        this.apiKey = apiKey;
    }

    class RequestBuilder {
        private String url;

        RequestBuilder(String url) {
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
            final URL url = new URL(apiHost + this.url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("X-GLVRD-KEY", apiKey);
            con.setUseCaches(true);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");

            return con;
        }

        public String request(String urlParameters, HttpURLConnection con) throws Exception {
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(urlParameters);
            writer.close();

            final int status = con.getResponseCode();
            switch (status) {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_CREATED:
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();

                    return sb.toString();
            }
            throw new Exception("Invalid response");
        }
    }

    public GlvrdHints hints(String ids) throws Exception {
        RequestBuilder requestBuilder = new RequestBuilder("/v3/hints/");
        final HttpURLConnection con = requestBuilder.createConnection();
        final String string = requestBuilder.request("ids=" + ids, con);

        return requestBuilder.responseParser(string, GlvrdHints.class);
    }

    public GlvrdResponse proofRead(String text) throws Exception {
        RequestBuilder requestBuilder = new RequestBuilder("/v3/proofread/");
        final HttpURLConnection con = requestBuilder.createConnection();
        String string = requestBuilder.request("text=" + text, con);

        return requestBuilder.responseParser(string, GlvrdResponse.class);
    }
}
