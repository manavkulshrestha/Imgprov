import java.io.File;
import java.security.*;

public class Crypto {

    private byte[] imgBytes;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    private Signature signature;

    /**
     * Constructor (verifier)
     *
     * @param imgBytes  The image as an array of bytes.
     * @param publicKey If public key is specified, keypair won't be generated. This Crypto object can only be used
     *                  for verification, not signing.
     */
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

    /**
     * Constructor (Signer and verifier)
     *
     * @param imgBytes The image as an array of bytes.
     */
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

    /**
     * Constructor (Signer and verifier)
     *
     * @param imgFile The image as a File object.
     */
    public Crypto(File imgFile) {
        this(Utilities.fileToBytes(imgFile));
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
