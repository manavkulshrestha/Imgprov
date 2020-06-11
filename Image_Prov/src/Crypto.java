import java.io.*;
import java.security.*;
import java.util.Base64;

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

    public String keyToPem(Key key) {
        String keyType;
        if (key instanceof PublicKey)
            keyType = "PUBLIC";
        else if (key instanceof  PrivateKey)
            keyType = "PRIVATE";
        else
            // key type not recognized
            return null;

        String keyFormatted = "-----BEGIN EC "+keyType+" KEY-----";

        String keyContent = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        for (String piece: keyContent.split("(?<=\\G.{64})")) {
            keyFormatted += "\n"+piece;
        }

        return keyFormatted+"\n-----END EC "+keyType+" KEY-----";
    }

    public void saveKeyToPem(String filename, Key key) {
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(keyToPem(key));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePublicKeyToPem(String filename) {
        saveKeyToPem(filename, publicKey);
    }

    public void savePrivateKeyToPem(String filename) {
        saveKeyToPem(filename, privateKey);
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
