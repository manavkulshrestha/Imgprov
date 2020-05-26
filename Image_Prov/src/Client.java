// INCOMPLETE
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class Client {

    private static final int GET_FEATURE_VECTOR = 0;
    private static final int GET_VERIFICATION_RESULT = 1;

    public static void main(String[] args) throws IOException {
        File catFile = new File("resources\\cat.jpg");
        File duckFile = new File("resources\\duck.jpg");

        byte[] catBytes = Crypto.fileToBytes(catFile);
        byte[] duckBytes = Crypto.fileToBytes(duckFile);

        Crypto catCrypto = new Crypto(catBytes);
        Crypto duckCrypto = new Crypto(duckBytes);

        byte[] catSign = catCrypto.sign();
        byte[] duckSign = duckCrypto.sign();

        Socket s = new Socket("localhost", 3000);

        DataOutputStream dOut = new DataOutputStream(s.getOutputStream());

        // TODO
        // two cases:
        // 1. send just image
        // 2. send image (with metadata) and public key

        // send cat image
        dOut.writeInt(GET_FEATURE_VECTOR);
        dOut.writeInt(catBytes.length);
        dOut.write(catBytes);


    }
}
