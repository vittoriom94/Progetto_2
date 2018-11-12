package progetto_2;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class SharesRing implements Serializable {
    private BigInteger p;
    //string chiave: nomefile|tipochiave|k???
    //lista identificativi?
    private HashMap<String, HashMap<Integer, String>> mapFiles = new HashMap<>();
    private transient static SharesRing instance = null;

    protected SharesRing() {
        this.p = SecretSharing.generatePrime(Const.BITLENGHT);
    }

    public static SharesRing getInstance() {

        if (instance == null) {
            SharesRing sr = SharesRing.loadSharesRing();
            instance = Objects.requireNonNullElseGet(sr, SharesRing::new);
        }
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

    public void saveSharesRing(File f, HashMap<String, HashMap<BigInteger, BigInteger>> map) {

        File[] servers = Utils.getServers();

        try {

            //salva la share
            for (String s : map.keySet()) {
                HashMap<Integer, String> temp = new HashMap<>();
                for (BigInteger bi : map.get(s).keySet()) {
                    File tempfile;
                    String nomeShare;
                    do {

                        nomeShare = UUID.randomUUID().toString();
                        tempfile = new File(servers[bi.intValue() - 1].getAbsolutePath() + "/" + nomeShare + ".share");
                    } while (f.exists());
                    tempfile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(tempfile);
                    //fos.write(Utils.getbyteFromBigInteger(map.get(s).get(bi)));
                    fos.write(map.get(s).get(bi).toByteArray());
                    fos.flush();
                    fos.close();

                    //salva nella hashmap da scrivere
                    temp.put(bi.intValue(), nomeShare);
                }
                mapFiles.put(s, temp);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(this);

            oos.close();
            fos.close();
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public KeyRing rebuild(String nomefile) {

        File[] servers = Utils.getServers();
        KeyRing kr = new KeyRing(nomefile);
        for (String s : mapFiles.keySet()) {
            String name = s.split(Pattern.quote(Const.SEPARATORKR))[0];
            String key = s.split(Pattern.quote(Const.SEPARATORKR))[1];

            SecretSharing sh = new SecretSharing(p);

            if (name.equalsIgnoreCase(nomefile)) {
                HashMap<Integer, String> temp = mapFiles.get(s);
                HashMap<BigInteger, BigInteger> shares = new HashMap<>();

                for (Map.Entry e : temp.entrySet()) {

                    byte[] share = getShare((String) e.getValue(), servers[(Integer) e.getKey() - 1]);
                    if (share != null) {
                        shares.put(BigInteger.valueOf((int) e.getKey()), new BigInteger(share));
                    }
                }
                BigInteger secret = sh.getSecret(shares);
                kr.saveKey(Utils.getbyteFromBigInteger(secret), key);
            }
        }
        return kr;
    }

    private byte[] getShare(String name, File server) {
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
}
