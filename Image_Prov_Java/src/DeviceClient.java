import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import sun.net.www.protocol.https.Handler;

public class DeviceClient {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        File imgFile = new File("resources\\cat.jpg");
        byte[] imgBytes = Utilities.fileToBytes(imgFile);
        Crypto deviceCrypto = new Crypto();

        // configure the SSLContext with a TrustManager
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
        SSLContext.setDefault(ctx);

        // open HTTPS connection to server
        String url = "https://127.0.0.1:5000/featureVector/";
        HttpsURLConnection con = (HttpsURLConnection) new URL(null, url, new Handler()).openConnection();

        con.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        });

        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestMethod("POST");

        // make POST request to get feature vector
        String vectorRequest = "{data:"+Utilities.encodedString(imgBytes)+",sign:"+Utilities.encodedString(deviceCrypto.sign(imgBytes))+"}";
        try (OutputStream os = con.getOutputStream()) {
            os.write(vectorRequest.getBytes(StandardCharsets.UTF_8));
        }
    }
}

class DefaultTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
