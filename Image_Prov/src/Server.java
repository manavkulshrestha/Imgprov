import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;

public class Server {

    public static void main(String[]args) throws IOException, ImageReadException, ClassNotFoundException {
        ServerSocket ss = new ServerSocket(3000);
        Socket s = ss.accept();

        DataInputStream dIn = new DataInputStream(s.getInputStream());
        DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
        ObjectInputStream oIn = new ObjectInputStream(s.getInputStream());

        // read image
        int size = dIn.readInt();
        byte[] imgBytes = new byte[size];
        dIn.readFully(imgBytes);

        // send arbitrary feature vector. This will be returned by the DNN
        float[] featureVector = new float[] {1.0f,1.01f,1.002f,1.0003f,1.00004f};
        byte[] featureVectorBytes = Utilities.floatArrayToByteArray(featureVector);
        dOut.writeInt(featureVectorBytes.length);
        dOut.write(featureVectorBytes);

        // receive public key. won't be sent like this in final version
        PublicKey publicKey = (PublicKey) oIn.readObject();

        // read image with metadata
        byte[] img2Bytes = new byte[dIn.readInt()];
        dIn.readFully(img2Bytes);

        // parse XML
        String xml = Imaging.getXmpXml(img2Bytes);
        byte[] featureVector2Bytes = Base64.getDecoder().decode(Utilities.badXmlStrParser(xml, "featureVector").getBytes(StandardCharsets.UTF_8));
        byte[] cat2Sign = Base64.getDecoder().decode(Utilities.badXmlStrParser(xml, "featureVectorSignature").getBytes(StandardCharsets.UTF_8));

        // verify with public key and send result
        Crypto cat2Crypto = new Crypto(featureVector2Bytes, publicKey);
        dOut.writeBoolean(cat2Crypto.verify(cat2Sign));

        // send result from feature vector deviation check
        float[] featureVector2 = Utilities.byteArrToFloatArr(featureVector2Bytes);
        dOut.writeBoolean(verifyFeatureVector(featureVector, featureVector2, .01f));
    }

    public static boolean verifyFeatureVector(float[] u, float[] v, float epsilon) {
        if (u.length != v.length)
            return false; //should this throw instead?

        float distance = 0;

        for (int i=0; i<u.length; i++) {
            float diff = u[i] - v[i];
            distance += diff*diff;
        }

        return distance < epsilon;
    }
}
