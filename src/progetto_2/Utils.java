package progetto_2;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Key;
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
        if(!Const.SERVERPATH.exists()){
            Const.SERVERPATH.mkdir();
        }
        for(int i = 0;i<n;i++){
            File f = new File(Const.SERVERPATH.getName()+"/0"+(i+1)+ " " +names.remove(0) + "/");
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
    public static void testMAC() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            SecretKey secretKey = keyGenerator.generateKey();
            KeyGenerator keygen = KeyGenerator.getInstance("HmacMD5");
            Key macKey = keygen.generateKey();
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(macKey);
            //cifrario messaggio, AES o DES
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            FileInputStream fis = new FileInputStream("prova.txt");
            FileOutputStream os = new FileOutputStream("prova2.txt");

            byte[] t = cipher.doFinal(fis.readAllBytes());
            os.write(t);
            os.close();
            fis.close();
            byte[] macBytes2 = mac.doFinal(t);
            for (int p = 0; p < macBytes2.length; p++)
                System.out.print(macBytes2[p]);


            Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher2.init(Cipher.DECRYPT_MODE, secretKey);
            File file = new File("prova2.txt");
            FileInputStream fis2 = new FileInputStream(file);

            FileOutputStream w = new FileOutputStream("chiaro.txt");

            System.out.println();
            byte[] c0 = fis2.readAllBytes();
            byte[] c = cipher2.doFinal(c0);
            w.write(c);
            byte[] macBytes3 = mac.doFinal(c0);
            for (int p = 0; p < macBytes3.length; p++)
                System.out.print(macBytes3[p]);
            System.out.println("Ok");
        } catch (Exception e){
            e.printStackTrace();
        }
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

    public static File[] getServers() {
        File[] servers = new File[Const.SERVERPATH.listFiles().length];

        for(File dir : Const.SERVERPATH.listFiles()){
            String s = dir.getName().substring(0,2);
            int num = Integer.valueOf(s)-1;
            servers[num] = dir;
        }
        return servers;
    }

    public static void deleteData() {
        Path directory = Paths.get("server/");
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
