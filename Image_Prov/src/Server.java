// INCOMPLETE
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int SEND_FEATURE_VECTOR = 0;
    private static final int SEND_VERIFICATION_RESULT = 1;

    public static void main(String[]args) throws IOException {
        ServerSocket ss = new ServerSocket(3000);
        Socket s = ss.accept();

        DataInputStream dIn = new DataInputStream(s.getInputStream());

        // see how to handle input
        int requestType = dIn.readInt();

        // read image
        byte[] imgBytes = new byte[dIn.readInt()];
        dIn.readFully(imgBytes);

        DataOutputStream dOut = new DataOutputStream(s.getOutputStream());

        switch (requestType) {
            case SEND_FEATURE_VECTOR:
                // send arbitrary feature vector for now. Output from DNN will be sent.
                byte[] featureVectorBytes = new byte[] {1,2,3,4};
                dOut.writeInt(featureVectorBytes.length);
                dOut.write(featureVectorBytes);
                break;
            case SEND_VERIFICATION_RESULT:
                // TODO create crypto object to do verification with public key (also, read public key)
                dOut.writeBoolean(true);
                break;
        }

        dIn.close();
//        dOut.close();
    }



}
