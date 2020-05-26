import javax.crypto.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;

public class Crypto {
    // Handles cryptography stuff for objects

    private byte[] imgBytes;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    private Signature signature;

    public Crypto(File imgFile) {
        try {
            KeyPair keys = KeyPairGenerator.getInstance("EC").generateKeyPair();
            this.publicKey = keys.getPublic();
            this.privateKey = keys.getPrivate();
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        imgBytes = new byte[(int) imgFile.length()];
        try (InputStream fis = new FileInputStream(imgFile)) {
            fis.read(imgBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            signature = Signature.getInstance("SHA256withECDSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public byte[] sign() {
        try {
            signature.initSign(privateKey);
            signature.update(imgBytes);
            return signature.sign();
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean verify(byte[] sign) {
        try {
            signature.initVerify(publicKey);
            signature.update(imgBytes);
            return signature.verify(sign);
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }
}
