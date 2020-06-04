import org.json.simple.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class DeviceClient {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        File imgFile = new File("resources\\cat.jpg");
        byte[] imgBytes = Utilities.fileToBytes(imgFile);
        Crypto deviceCrypto = new Crypto();
        byte[] imgSign = deviceCrypto.sign(imgBytes);

        System.out.println(imgSign.length);

        // open HTTP connection to server
        URL url = new URL("http://127.0.0.1:5000/featureVector");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // build request
        JSONObject request = new JSONObject();
        request.put("image", Utilities.encodedString(imgBytes));
        request.put("imageSign", Utilities.encodedString(imgSign));
        String reqStr = request.toString();

        // connection options
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setFixedLengthStreamingMode(reqStr.length());
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        // send request
        try (OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream())){
            osw.write(reqStr);
        }
    }
}