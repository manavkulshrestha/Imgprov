import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeviceClient {
    public static void main(String[] args) throws IOException {
        File imgFile = new File("resources\\cat.jpg");
        byte[] imgBytes = Utilities.fileToBytes(imgFile);
        Crypto deviceCrypto = new Crypto();
        byte[] imgSign = deviceCrypto.sign(imgBytes);

        deviceCrypto.savePublicKeyToPem("resources\\device_public_key.pem");

        // open HTTP connection to server
        URL url = new URL("http://127.0.0.1:5000/featureVector");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // build request
        JSONObject request = new JSONObject();
        request.put("image", Utilities.encodedString(imgBytes));
        request.put("imageSign", Utilities.encodedString(imgSign));
        String reqStr = request.toString();

        System.out.println(reqStr);

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

        // read response
        JSONObject data = null;
        try (InputStream is = conn.getInputStream()) {
            data = (JSONObject) new JSONParser().parse(new String(is.readAllBytes()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        boolean verified = (boolean) data.get("verified");
        System.out.println("Device verification result: "+verified);
        if (!verified) {
            return;
        }


    }

    public static void modifyExif(File src, File dest) {
        
    }
}