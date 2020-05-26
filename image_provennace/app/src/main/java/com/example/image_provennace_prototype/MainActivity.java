package com.example.image_provennace_prototype;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import androidx.exifinterface.media.ExifInterface;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Arrays;



public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Bitmap bitmap;
    Bitmap thumbnail;
    KeyPair keyPair;
    PublicKey publicKey;
    PrivateKey privateKey;
    String alias = "kpg";
    File file = new File("jpg");
    byte[] byteArray = new byte[(int) file.length()];
    byte[] signature;
    String signatureString;
    String outFile="/";
    FileInputStream fis;
    int intArray[];
    int argb [];
    byte[] jpeghash;
    boolean hashed = false;
    String argbS;
    String hashtext;
    String allZeros = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCap = (Button) findViewById(R.id.capture_button);
        Button sss = (Button)findViewById(R.id.signSend);
        imageView = (ImageView) findViewById(R.id.imageview);
        try {
            if (findKey()==false){
                generateKey();
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }



        btnCap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
                hashed = false;
            }
        });

        sss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hashed == false) {
                    try {
                        //addZeroExif();
                        getRGB ();
                        hashpic();
//                        sign();
//                        verify();
                        addExif();

                        Log.e("finish up", "////////////////////////////////////////////////////////////////////////////////////////");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
////                    catch (SignatureException e) {
////                        e.printStackTrace();
////                    } catch (InvalidKeyException e) {
////                        e.printStackTrace();
//                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    hashed = true;
                }
                else{
                    Log.e("alert","Already signed !!!!!!!!");
                    try {
                        //addZeroExif();
                        sign();
                        verify();
                        hashpic();
                        addExif();

                        Log.e("finish up", "////////////////////////////////////////////////////////////////////////////////////////");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (SignatureException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bitmap = (Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitmap);
        thumbnail= ThumbnailUtils.extractThumbnail(bitmap, 5, 10);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }
        //imageView.setImageBitmap(bitmap);

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/test");
        myDir.mkdirs();
        long sec=System.currentTimeMillis();
        String fname = "Image-" + sec + ".jpg";
        file = new File(myDir, fname);

        Log.i("saved", "" + file);

        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        FileInputStream fis;
        byteArray = new byte[(int) file.length()];

        try{
            fis = new FileInputStream(file);
            fis.read(byteArray);
            fis.close();

        }catch(IOException ioExp){
            ioExp.printStackTrace();
        }
        //String s =  Base64.getEncoder().encodeToString(byteArray);
        String s = byteArray.toString();
        Log.e("Raw_bytes", s);

    }


    private void generateKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
        kpg.initialize(new KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                .setDigests(KeyProperties.DIGEST_SHA256,
                        KeyProperties.DIGEST_SHA512)
                .build());
        keyPair = kpg.generateKeyPair();
    }

    private Boolean findKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableEntryException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        KeyStore.Entry entry = keyStore.getEntry(alias, null);
        if (entry==null){
            return false;
        }
        else{
            privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
            publicKey = keyStore.getCertificate(alias).getPublicKey();

            return true;
        }
    }
    private void sign() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        Signature sign = Signature.getInstance("SHA256withECDSA");
        sign.initSign(privateKey);
        sign.update(byteArray);
        signature = sign.sign();
        signatureString = Base64.getEncoder().encodeToString(signature);
        //Log.e("PrivateKey", privateKey.toString());
        //Log.e("PublicKey",publicKey.toString());
        Log.e("Signed_bytes", signatureString);
        Log.e("Signed_bytes", Arrays.toString(signature));
        Log.e("Signed_bytes", Integer.toString(signature.length));
    }

    public Boolean verify() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign;
        sign = Signature.getInstance("sha256withECDSA");
        sign.initVerify((PublicKey) publicKey);
        sign.update(byteArray);
        boolean result = sign.verify(signature);
        String s = String.valueOf(result);
        Log.e("Verification",s);
        return result;
    }

    private void addExif() throws IOException {
        Log.e("path",file.getAbsolutePath());
        ExifInterface exif = new ExifInterface(file.getPath());
        //String imageDescription =signatureString;
        //String imageDescription= Base64.getEncoder().encodeToString(signature);
        String imageDescription= hashtext;
        exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, imageDescription);
        exif.saveAttributes();
        //Log.e("Exif","EXIF saved");

        ExifInterface exif2 = new ExifInterface(file.getAbsolutePath());
        String s = exif2.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        //Log.e("Public", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        //Log.e("Sign", signatureString);
        Log.e("Exif", s);
    }

    private void addZeroExif() throws IOException {
        Log.e("path",file.getAbsolutePath());
        ExifInterface exif = new ExifInterface(file.getPath());
        //String imageDescription =signatureString;
        String imageDescription= allZeros;
        exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, imageDescription);
        exif.saveAttributes();
        //Log.e("Exif","EXIF saved");

//        ExifInterface exif2 = new ExifInterface(file.getAbsolutePath());
//        String s = exif2.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
//        Log.e("Public", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
//        //Log.e("Sign", signatureString);
//        Log.e("Exif", s);

    }

    private byte[] convertIntArrayToByteArray(int[] data) {
        if (data == null) return null;
        byte[] byts = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++)
            System.arraycopy(intToBytes(data[i]), 0, byts, i * 4, 4);
        return byts;
    }

    private byte[] intToBytes( final int i ) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }




    /////////////////////HASH////////////////////////////
    private void hashpic () throws NoSuchAlgorithmException, UnsupportedEncodingException, FileNotFoundException {


//        try{
//            fis = new FileInputStream(file);
//            fis.read(byteArray);
//            fis.close();
//
//        }catch(IOException ioExp){
//            ioExp.printStackTrace();
//        }
        //String s =  Base64.getEncoder().encodeToString(byteArray);
//        String s = byteArray.toString();
//        Log.e("Raw_bytes", s);



        MessageDigest md = MessageDigest.getInstance("MD5");
        //byteArray=argbS.getBytes("UTF-8");
        //byteArray="123".getBytes();


        md.update(byteArray);
        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        hashtext = bigInt.toString(16);
        Log.e("Hash",hashtext);
    }

    private void getRGB () throws FileNotFoundException {
        fis = new FileInputStream(file);
        Bitmap bi = BitmapFactory.decodeStream(fis);

        bi=bi.copy(Bitmap.Config.ARGB_8888,false);
        intArray = new int[bi.getWidth()*bi.getHeight()];
        argb = new int[bi.getWidth()*bi.getHeight()*4];
        bi.getPixels(intArray, 0, bi.getWidth(), 0, 0, bi.getWidth(), bi.getHeight());

        Log.e("bitmapbytes",Arrays.toString(intArray));
        Log.e("width",Integer.toString(bi.getWidth()));
        Log.e("height",Integer.toString(bi.getHeight()));
        Log.e("pixels-----argb------length1",String.valueOf(argb.length));


        for(int i =0; i< intArray.length;i++){
            argb[i*4]=(intArray[i]& 0xff000000)>>24;
            if(argb[i*4]==-1){
                argb[i*4]=255;
            }
            argb[i*4+1]=(intArray[i]& 0x00ff0000)>>16;
            argb[i*4+2]=(intArray[i]& 0x0000ff00)>>8;
            argb[i*4+3]=(intArray[i]& 0x000000ff);
        }
//        byteArray=convertIntArrayToByteArray(argb);
//        byteArray=convertIntArrayToByteArray(argb);

        argbS = Arrays.toString(argb);

        //String newStr  = "";
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i< argb.length; i++){
            sb.append(argb[i]);
        }



        Log.e("pixels_without_space",sb.toString());
        Log.e("pixels_without_space",sb.toString().substring(sb.toString().length()-20));

        byteArray=sb.toString().getBytes();

        Log.e("pixels-----argb",argbS);

//        Log.e("pixels-----argb------length2",String.valueOf(argb.length));
//        Log.e("pixels-----intarray------length",String.valueOf(intArray.length));
    }


}
