import java.io.File;
import java.security.*;

public class Crypto {

    private PublicKey publicKey;
    private PrivateKey privateKey;

    private Signature signature;

    /**
     * Constructor (verifier)
     *
     * @param publicKey If public key is specified, keypair won't be generated. This Crypto object can only be used
     *                  for verification, not signing.
     */
    public Crypto(PublicKey publicKey) {

        this.publicKey = publicKey;
        privateKey = null;

        try {
            signature = Signature.getInstance("SHA256withECDSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor (Signer and verifier)
     */
    public Crypto() {
        try {
            KeyPair keys = KeyPairGenerator.getInstance("EC").generateKeyPair();
            publicKey = keys.getPublic();
            privateKey = keys.getPrivate();
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        try {
            signature = Signature.getInstance("SHA256withECDSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] sign(byte[] data) {
        if (privateKey == null) {
            // only a verifier was built
            return null;
        }

        try {
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean verify(byte[] data, byte[] sign) {
        try {
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(sign);
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }
}
