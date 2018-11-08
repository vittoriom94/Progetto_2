package progetto_2;


import java.io.*;
import java.security.Key;
import java.util.HashMap;

public class KeyRing implements Serializable {
    private HashMap<String, Byte[]> keys;
    public KeyRing(){
        this.keys = new HashMap<>();
    }

    public static KeyRing loadKeyring(File f){
        KeyRing kr = null;
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);

            kr = (KeyRing) ois.readObject();
            ois.close();
            fis.close();
            return kr;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return kr;
    }

    public byte[] getKey(String id){

       return Utils.fromByteTobyte(keys.get(id));
    }

    public void saveKey(byte[] k, String id){
        keys.put(id, Utils.frombyteToByte(k));
    }

    public void saveKeyring(File f){
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(this);

            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
