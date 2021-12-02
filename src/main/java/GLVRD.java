import java.net.*;
import java.io.*;

public class GLVRD {
    public String glvrdProofRead(String text, String key) throws Exception {
        final URL url = new URL("https://glvrd.ru/api/v3/proofread/");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("X-GLVRD-KEY", key);
        con.setUseCaches(true);
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("POST");

        String urlParameters = "text=" + text;
        OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
        writer.write(urlParameters);
        writer.close();

        final int status = con.getResponseCode();
        switch (status) {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                return sb.toString();
        }
        throw new Exception("Invalid response");
    }
}
