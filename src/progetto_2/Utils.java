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
        byte[] b2 = new byte[b.length+2];
        b2[0]=0;
        b2[1]=-1;
        for(int i =0;i<b.length;i++){
            b2[i+2]=b[i];
        }
        return new BigInteger(b2);
        //System.out.println();
        //BigInteger bi = new BigInteger(1, b);
        //return bi;
    }
    public static byte[] getbyteFromBigInteger(BigInteger bi){
        byte[] b1 = bi.toByteArray();
        byte[] b2 = new byte[b1.length-2];

        for(int i =2;i<b1.length;i++){
            b2[i-2]=b1[i];
        }
        return b2;
        //byte[] b = bi.toByteArray();
        //return b;
    }

    public static Byte[] frombyteToByte(byte[] bytes) {

        Byte[] byteObjects = new Byte[bytes.length];

        int i = 0;
        for (byte b : bytes)
            byteObjects[i++] = b;
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
        MessageShare.getInstance().clear();
        SharesRing.getInstance().clear();



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

    public static void testBiginteger(){
        byte[] b1 = {0,-1,0,1,2,3,4,5};
        byte[] b2 = {0,-1,0,-1,2,3,4,5};
        byte[] b3 = {0,-1,0,0,2,3,4,5};
        byte[] b4 = {0,-1,0,0,-2,3,4,5};

        BigInteger bi1 = new BigInteger(1,b1);
        BigInteger bi2 = new BigInteger(1,b2);
        BigInteger bi3 = new BigInteger(1,b3);
        BigInteger bi4 = new BigInteger(1,b4);

        byte[] br1 = bi1.toByteArray();
        byte[] br2 = bi2.toByteArray();
        byte[] br3 = bi3.toByteArray();
        byte[] br4 = bi4.toByteArray();


        System.out.println();

    }

    public static void testMessageShare() throws IOException {
        FileInputStream fis = new FileInputStream(new File("prova.txt"));
        FileOutputStream fos = new FileOutputStream(new File("provaOUT.txt"));

        MessageShare ms = new MessageShare();
        //ms.shareFile("prova.txt", fis);
        //ms.rebuilfFile("prova.txt", fos);

    }
}

