package progetto_2;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Utils {

    public static void createServers(){
        List<String> names = new LinkedList<>();
        names.add("Dropbox");
        names.add("GoogleDrive");
        names.add("OneDrive");
        names.add("Amazon");
        names.add("Aruba");
        int n = names.size();
        for(int i = 0;i<n;i++){
            File f = new File("server/0"+(i+1)+ " " +names.remove(0) + "/");
            if(!f.exists()){
                f.mkdir();
            }
        }
    }


    public static BigInteger getBigInteger(byte[] b){
        byte[] b2 = new byte[b.length+1];
        b2[0]=0;
        for(int i =0;i<b.length;i++){
            b2[i+1]=b[i];
        }
        return new BigInteger(b2);
    }
    public static byte[] getbyteFromBigInteger(BigInteger bi){
        return bi.toByteArray();
        /*byte[] b = bi.toByteArray();
        byte[] b2 = new byte[b.length-1];
        for(int i = 1;i<b.length;i++){
            b2[i-1]= b[i];
        }
        return b2;*/
    }

    public static Byte[] frombyteToByte(byte[] bytes) {

        Byte[] byteObjects = new Byte[bytes.length];

        int i = 0;
        for (byte b : bytes)
            byteObjects[i++] = b;  // Autoboxing.
        return byteObjects;
    }
    public static byte[] fromByteTobyte(Byte[] byteObjects) {
        byte[] bytes = new byte[byteObjects.length];
        int j = 0;
        for (Byte b : byteObjects)
            bytes[j++] = b.byteValue();
        return bytes;
    }


    public static ArrayList<Byte> toByteArrayNonprimitive(byte[] bytes){
        System.out.println(bytes);
        ArrayList<Byte> bytesNP = new ArrayList<Byte>();
        for(byte b : bytes){
            bytesNP.add(b);
        }
        return bytesNP;
    }

    public static byte[] toByteArray(List ba){
        byte[] bytesArr = new byte[ba.size()];
        for(int i = 0; i < ba.size(); i++) {
            bytesArr[i] = (byte)ba.get(i);
        }
        return bytesArr;
    }
    public static String byteArrayToString(byte[] bytes){
        String s = "";
        for(byte b : bytes){
            s=s+(char)b;
        }
        return s;
    }

    public static String randomName(){
        String s;
        byte[] arr = new byte[16];
        new Random().nextBytes(arr);
        s = new String(arr, Charset.forName("UTF-8"));
        return s;
    }

    public static void TestBiginteger() throws IOException {
        BigInteger bi = BigInteger.probablePrime(10, new Random());
        FileOutputStream fos = new FileOutputStream(new File(("prova.txt")));
        fos.write(bi.toByteArray());
        fos.flush();
        fos.close();
        FileInputStream fis = new FileInputStream(new File(("prova.txt")));
        byte[] b = fis.readAllBytes();
        BigInteger bi2 = new BigInteger(b);
        fis.close();
        System.out.println("ok");
    }
}
