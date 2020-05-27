import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.xmp.JpegRewriter;
import org.apache.commons.imaging.formats.jpeg.xmp.JpegXmpRewriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Arrays;

public class ImageProvenance {

    public static void main(String[] args) throws SignatureException, IOException, ImageReadException, ImageWriteException {
        // rudimentary test
        File catFile = new File("resources\\cat.jpg");
//        Crypto catCrypto = new Crypto(catFile);
//        Crypto duckCrypto = new Crypto(new File("resources\\duck.jpg"));
//
//        byte[] catSign = catCrypto.sign();
//        byte[] duckSign = duckCrypto.sign();
//
//        System.out.println(catCrypto.verify(catSign)); // true
//        System.out.println(duckCrypto.verify(duckSign)); // true
//
//        System.out.println(catCrypto.verify(duckSign)); // false
//        System.out.println(duckCrypto.verify(catSign)); // false

//        float[] test = new float[] {1.01f, 1.002f, 1.0003f};
//        System.out.println(Arrays.toString(test));
//        byte[] bytes = Server.floatArrayToByteArray(test);
//        System.out.println(Arrays.toString(bytes));
//        float[] floats = Client.byteArrToFloatArr(bytes);
//        System.out.println(Arrays.toString(floats));

//        new JpegXmpRewriter().updateXmpXml(catFile, new FileOutputStream(new File("resources\\cat_new.jpg")), "<featureVector>test</featureVector>");
    }
}