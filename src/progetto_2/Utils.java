package progetto_2;

import java.util.ArrayList;
import java.util.List;

public class Utils {

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
}
