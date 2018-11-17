package progetto_2;

import javax.security.sasl.AuthenticationException;
import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class KeyRing implements Serializable {
    private String name;
    private HashMap<String, Byte[]> keys;
    public KeyRing(String nomefile){
        //this.keys = SharesRing.getInstance().checkGenericKeys();
        this.keys = new HashMap<>();
        this.name = nomefile;
    }


    public byte[] getKey(String id) throws AuthenticationException {
        try {
            return Utils.fromByteTobyte(keys.get(id));
        } catch (NullPointerException npe){

            throw new AuthenticationException("La chiave " + id + " non è presente oppure non si dispone delle autorizzazioni necessarie");

        }
    }

    public void saveKey(byte[] k, String id){
        keys.put(id, Utils.frombyteToByte(k));
    }

    public void saveShamir(String mittente, String destinatario, int k, int n){
        SharesRing sr = SharesRing.getInstance();
        SecretSharing ss = sr.getShamir();
        HashMap<String, HashMap<BigInteger,BigInteger>>   sharesAndIds = new HashMap<>();

        //aggiungi chiavi allo sharesring
        for( Map.Entry<String,Byte[]> e : keys.entrySet()){
            BigInteger secret = Utils.getBigInteger(Utils.fromByteTobyte(e.getValue()));
            String id = e.getKey();

            HashMap<BigInteger, BigInteger> shares = (HashMap<BigInteger, BigInteger>) ss.genShares(secret,k,n);

            sharesAndIds.put( id, shares);
        }

        sr.saveSharesRing(mittente,destinatario,name,k, sharesAndIds);
    }
}
