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

    public Crypto(byte[] imgBytes, PublicKey publicKey) {

        this.publicKey = publicKey;
        privateKey = null;

        this.imgBytes = imgBytes;

        try {
            signature = Signature.getInstance("SHA256withECDSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public Crypto(byte[] imgBytes) {
        try {
            KeyPair keys = KeyPairGenerator.getInstance("EC").generateKeyPair();
            publicKey = keys.getPublic();
            privateKey = keys.getPrivate();
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        this.imgBytes = imgBytes;

        try {
            signature = Signature.getInstance("SHA256withECDSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] fileToBytes(File file) {
        byte[] fileBytes = new byte[(int) file.length()];
        try (InputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileBytes;
    }

    public Crypto(File imgFile) {
        this(fileToBytes(imgFile));
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] sign() {
        if (privateKey == null) {
            // only a verifier was built
            return null;
        }

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
