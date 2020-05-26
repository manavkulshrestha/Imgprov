import java.io.File;
import java.security.*;

public class ImageProvenance {

    public static void main(String[] args) throws SignatureException {
        // rudimentary test
        Crypto catCrypto = new Crypto(new File("resources\\cat.jpg"));
        Crypto duckCrypto = new Crypto(new File("resources\\duck.jpg"));

        byte[] catSign = catCrypto.sign();
        byte[] duckSign = duckCrypto.sign();

        System.out.println(catCrypto.verify(catSign)); // true
        System.out.println(duckCrypto.verify(duckSign)); // true

        System.out.println(catCrypto.verify(duckSign)); // false
        System.out.println(duckCrypto.verify(catSign)); // false
    }
}