package progetto_2;

import javax.security.sasl.AuthenticationException;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

public class SharesRing implements Serializable {
    private BigInteger p;
    //string chiave: nomefile   tipochiave|k???
    //lista identificativi?

    //tipo chiave -> a chi è associata -> quote
    private HashMap<String, HashMap<Identifier, HashMap<Integer, ShareEntry>>> mapFiles = new HashMap<>();
    private transient static SharesRing instance = null;
    private transient File[] servers;
    private transient SecretSharing sh;
    private static final long serialVersionUID = 4L;

    protected SharesRing() {
        System.out.println("Generazione primo per le chiav... Attendere.");
        this.p = SecretSharing.generatePrime(Const.BITLENGHT);
        mapFiles.put("RSAPublic",new HashMap<>());
        mapFiles.put("DSAPublic",new HashMap<>());
        mapFiles.put("MAC",new HashMap<>());
        mapFiles.put("RSAPrivate",new HashMap<>());
        mapFiles.put("DSAPrivate",new HashMap<>());


    }

    public static SharesRing getInstance() {

        if (instance == null) {
            SharesRing sr = SharesRing.loadSharesRing();
            instance = Objects.requireNonNullElseGet(sr, SharesRing::new);
            instance.saveInstance();
        }
        instance.sh = new SecretSharing(instance.p);
        instance.servers = Utils.getServers();
        return instance;
    }


    public SecretSharing getShamir() {
        return new SecretSharing(this.p);
    }

    private static SharesRing loadSharesRing() {
        SharesRing sr = null;
        if (!Const.SHARESFILE.exists()) {
            return sr;
        }
        try {

            FileInputStream fis = new FileInputStream(Const.SHARESFILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            sr = (SharesRing) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            Const.SHARESFILE.delete(); //Utils.deleteData();
            throw new RuntimeException("Il file sharesRing è corrotto.", e);
        }

        return sr;
    }

    public void saveSharesRing(String mittente, String destinatario, String nome,int k, HashMap<String, HashMap<BigInteger, BigInteger>> map) {


        try {

            //salva la share

            for (String s : map.keySet()) {
                HashMap<Integer, ShareEntry> shares = new HashMap<>();
                for (BigInteger bi : map.get(s).keySet()) {
                    File tempfile;
                    String nomeShare;
                    do {

                        nomeShare = UUID.randomUUID().toString();
                        tempfile = new File(servers[bi.intValue() - 1].getAbsolutePath() + "/" + nomeShare + ".share");
                    } while (tempfile.exists());
                    tempfile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(tempfile);
                    //fos.write(Utils.getbyteFromBigInteger(map.get(s).get(bi)));
                    fos.write(map.get(s).get(bi).toByteArray());

                    fos.flush();
                    fos.close();

                    //salva nella hashmap da scrivere
                    shares.put(bi.intValue(), new ShareEntry(nomeShare, map.get(s).get(bi).hashCode()));
                }
                HashMap<Identifier, HashMap<Integer, ShareEntry>> temp = mapFiles.get(s);
                Identifier id = new Identifier(mittente,destinatario,nome,k,s);
                temp.put(id, shares);
                mapFiles.put(s, temp);
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        saveInstance();





        /*
        try {

            //salva la share
            HashMap<String, HashMap<Integer, ShareEntry>> temp0 = new HashMap<>();
            for (String s : map.keySet()) {
                HashMap<Integer, ShareEntry> temp1 = new HashMap<>();
                for (BigInteger bi : map.get(s).keySet()) {
                    File tempfile;
                    String nomeShare;
                    do {

                        nomeShare = UUID.randomUUID().toString();
                        tempfile = new File(servers[bi.intValue() - 1].getAbsolutePath() + "/" + nomeShare + ".share");
                    } while (tempfile.exists());
                    tempfile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(tempfile);
                    //fos.write(Utils.getbyteFromBigInteger(map.get(s).get(bi)));
                    fos.write(map.get(s).get(bi).toByteArray());

                    fos.flush();
                    fos.close();

                    //salva nella hashmap da scrivere
                    temp1.put(bi.intValue(), new ShareEntry(nomeShare, map.get(s).get(bi).hashCode()));
                }
                temp0.put(s, temp1);
            }
            mapFiles.put(name, temp0);


        } catch (IOException e) {
            e.printStackTrace();
        }
        saveInstance();
*/

    }

    private void saveInstance() {
        try {
            FileOutputStream fos = new FileOutputStream(Const.SHARESFILE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(this);

            oos.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public KeyRing rebuild(String mittente, String destinatario, String nome) {



        KeyRing kr = new KeyRing(nome);
        for(String s: mapFiles.keySet()){
            Identifier id = new Identifier(mittente,destinatario,nome,0,s);
            if(mapFiles.get(s).containsKey(id)) {
                kr.saveKey(searchShares(id, s), s);
            }
        }
        return kr;
        /*

        for (String s : mapFiles.get(nomefile).keySet()) {
            String key = s.split(Pattern.quote(Const.SEPARATORKR))[0];
            int minshares = Integer.valueOf(s.split(Pattern.quote(Const.SEPARATORKR))[1]);
            kr.saveKey(searchShares(nomefile, key,minshares), key);

        }
        for (String s : mapFiles.get(Const.ANYMESSAGEID).keySet()) {
            String key = s.split(Pattern.quote(Const.SEPARATORKR))[0];
            int minshares = Integer.valueOf(s.split(Pattern.quote(Const.SEPARATORKR))[1]);
            kr.saveKey(searchShares(Const.ANYMESSAGEID, key,minshares), key);

        }

        return kr;*/
    }

    //nome file, nome chiave
    private byte[] searchShares(Identifier id, String key) {

        int k = 0;
        HashMap<Integer, ShareEntry> temp = mapFiles.get(key).get(id);
        int minshares =0;
        for(Identifier id2 : mapFiles.get(key).keySet()){
            if(id.equals(id2)) {
                minshares = id2.getK();
                break;
            }
        }
        HashMap<BigInteger, BigInteger> shares = new HashMap<>();


        for (Map.Entry e : temp.entrySet()) {

            byte[] share = getShare(((ShareEntry) e.getValue()).getFile(), servers[(Integer) e.getKey() - 1]);

            if (share != null) {
                BigInteger shareValue = new BigInteger(share);
                boolean checkHash = shareValue.hashCode() == ((ShareEntry) e.getValue()).getHash();
                if (checkHash) {
                    shares.put(BigInteger.valueOf((int) e.getKey()), shareValue);
                    k++;
                }
            }
        }
        if(k<minshares){
            throw new NotEnoughSharesException(key, minshares);
        }
        BigInteger secret = sh.getSecret(shares);
        return Utils.getbyteFromBigInteger(secret);
    }

    public static byte[] getShare(String name, File server) {
        File f = new File(server.getAbsolutePath() + "/" + name + ".share");
        if (f.exists()) {
            try {
                FileInputStream fis = new FileInputStream(f);
                byte[] b = fis.readAllBytes();
                fis.close();
                return b;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;

    }

    /*public HashMap<String, Byte[]> checkGenericKeys() {
        HashMap<String, Byte[]> keys = new HashMap<>();
        HashMap<String, HashMap<Integer, ShareEntry>> genericKeys = mapFiles.get(Const.ANYMESSAGEID);
        if(genericKeys == null){
            return new HashMap<String, Byte[]>();
        }
        for (String s : genericKeys.keySet()) {
            String key = s.split(Pattern.quote(Const.SEPARATORKR))[0];
            int minshares = Integer.valueOf(s.split(Pattern.quote(Const.SEPARATORKR))[1]);
            keys.put(key ,Utils.frombyteToByte(searchShares(Const.ANYMESSAGEID, key,minshares)));

        }
        return keys;
    }*/

    public void clear() {
        this.mapFiles = new HashMap<>();
        mapFiles.put("RSAPublic",new HashMap<>());
        mapFiles.put("DSAPublic",new HashMap<>());
        mapFiles.put("MAC",new HashMap<>());
        mapFiles.put("RSAPrivate",new HashMap<>());
        mapFiles.put("DSAPrivate",new HashMap<>());
        saveInstance();
    }

}
