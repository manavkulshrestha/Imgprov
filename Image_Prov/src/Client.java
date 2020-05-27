import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.jpeg.xmp.JpegXmpRewriter;

public class Client {

    public static void main(String[] args) throws IOException, ImageReadException, ImageWriteException {
        File imgFile = new File("resources\\cat.jpg");
        byte[] imgBytes = Utilities.fileToBytes(imgFile);

        Socket s = new Socket("localhost", 3000);

        DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
        DataInputStream dIn = new DataInputStream(s.getInputStream());

        // object output stream to send public key and nothing else.
        // practically, this will not be needed in the final version as we will have a better way to do keys
        ObjectOutputStream oOut = new ObjectOutputStream(s.getOutputStream());

        // send cat image
        dOut.writeInt(imgBytes.length);
        dOut.write(imgBytes);

        // get feature vector
        byte[] featureVectorBytes = new byte[dIn.readInt()];
        dIn.readFully(featureVectorBytes);

        System.out.println("feature vector received: "+Arrays.toString(Utilities.byteArrToFloatArr(featureVectorBytes)));

        // get sign for feature vector
        Crypto catCrypto = new Crypto(featureVectorBytes);
        PublicKey publicKey = catCrypto.getPublicKey();

        // send over public key. again, there will be a better way to do this in the final version
        oOut.writeObject(publicKey);

        byte[] catSign = catCrypto.sign();

        // assign metadata
        // all this hassle with dummy files because JpegXmpRewriter rewrites destructively, yikes
        String featureVectorStr = Base64.getEncoder().encodeToString(featureVectorBytes);
        String signStr = Base64.getEncoder().encodeToString(catSign);
        String xmpXmlStr = "<featureVector>"+featureVectorStr+"</featureVector>\n<featureVectorSignature>"+signStr+"</featureVectorSignature>";
        Path dummyPath = Paths.get(imgFile.getParent(), "dummy.jpg");
        Files.copy(imgFile.toPath(), dummyPath, StandardCopyOption.REPLACE_EXISTING);
        imgFile.delete();
        File dummyFile = dummyPath.toFile();
        new JpegXmpRewriter().updateXmpXml(dummyFile, new FileOutputStream(imgFile), xmpXmlStr);
        dummyFile.delete();

        // send cat image with metadata after rereading
        imgFile = new File("resources\\cat.jpg");
        imgBytes = Utilities.fileToBytes(imgFile);

        dOut.writeInt(imgBytes.length);
        dOut.write(imgBytes);

        System.out.println("Verification result (public key check): "+ dIn.readBoolean());
        System.out.println("Feature vector within epsilon? (deviation check): "+dIn.readBoolean());
    }
}
