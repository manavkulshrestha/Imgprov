import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Utilities {
    public static byte[] fileToBytes(File file) {
        byte[] fileBytes = new byte[(int) file.length()];
        try (InputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileBytes;
    }

    public static byte[] floatArrayToByteArray(float[] values){
        ByteBuffer buffer = ByteBuffer.allocate(4*values.length);

        for (float value: values) {
            buffer.putFloat(value);
        }

        return buffer.array();
    }

    public static float[] byteArrToFloatArr(byte[] arr) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(arr);
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

        float[] floatArray = new float[floatBuffer.limit()];
        floatBuffer.get(floatArray);

        return floatArray;
    }

    public static String badXmlStrParser(String xml, String tag) {
        int valueStart = xml.indexOf(tag)+tag.length()+1;
        return xml.substring(valueStart, xml.indexOf("</"+tag+">", valueStart));
    }
}
