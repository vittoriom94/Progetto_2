package progetto_2;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class KeyRing implements Serializable {
    private String name = "";
    private HashMap<String, Byte[]> keys;
    public KeyRing(String nomefile){
        this.keys = new HashMap<>();
        this.name = nomefile;
    }

    public static KeyRing loadKeyring(File f){
        KeyRing kr = null;
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);

            kr = (KeyRing) ois.readObject();
            ois.close();
            fis.close();

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


    public void saveShamir(int k, int n){
        SharesRing sr = SharesRing.loadSharesRing(new File("sharesRing.txt"));
        if(sr==null){
            sr = new SharesRing();
        }
        SecretSharing ss = new SecretSharing(1500);
        for( Map.Entry<String,Byte[]> e : keys.entrySet()){
            BigInteger secret = Utils.getBigInteger(Utils.fromByteTobyte(e.getValue()));
            String id = name+"-"+e.getKey()+"-"+ss.getP().toString();
            // le chiavi vanno da 1 a n
            HashMap<BigInteger, BigInteger> shares = (HashMap<BigInteger, BigInteger>) ss.genShares(secret,k,n);

            sr.add( id, shares);
        }
        sr.saveSharesRing(new File("sharesRing.txt"));


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
