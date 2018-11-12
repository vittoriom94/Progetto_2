package progetto_2;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class KeyRing implements Serializable {
    private String name;
    private HashMap<String, Byte[]> keys;
    public KeyRing(String nomefile){
        this.keys = SharesRing.getInstance().checkGenericKeys();
        this.name = nomefile;

    }


    public byte[] getKey(String id){

       return Utils.fromByteTobyte(keys.get(id));
    }

    public void saveKey(byte[] k, String id){
        keys.put(id, Utils.frombyteToByte(k));
    }

    public void saveShamir(int k, int n){
        SharesRing sr = SharesRing.getInstance();
        SecretSharing ss = sr.getShamir();
        HashMap<String, HashMap<BigInteger,BigInteger>>   sharesAndIds = new HashMap<>();

        //aggiungi chiavi allo sharesring
        for( Map.Entry<String,Byte[]> e : keys.entrySet()){
            BigInteger secret = Utils.getBigInteger(Utils.fromByteTobyte(e.getValue()));
            String id = e.getKey()+ Const.SEPARATORKR+k;//k???

            HashMap<BigInteger, BigInteger> shares = (HashMap<BigInteger, BigInteger>) ss.genShares(secret,k,n);

            sharesAndIds.put( id, shares);
        }
        sr.saveSharesRing(name, sharesAndIds);
    }


    @Deprecated
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

    @Deprecated
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
