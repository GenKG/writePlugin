import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.HttpResponseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

abstract class RequestHelper {
    protected String url;

    public <T extends GlvrdResponsable> T responseParser(String response, Class<T> tClass) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        T map = mapper.readValue(response, tClass);
        if (!map.getStatus().equals("ok")) {
            throw new Exception(map.getMessage());
        }
        return map;
    }

    public String getData(HttpURLConnection con) throws IOException, HttpResponseException, Exception {
        switch (con.getResponseCode()) {
            case 429:
                throw new HttpResponseException(429, "too many requests");
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