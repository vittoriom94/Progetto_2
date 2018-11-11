package progetto_2;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class SharesRing implements Serializable{
    //String = nomefile|tipochiave
    //hashMap <server, share>
    //private transient HashMap<String, HashMap<BigInteger,BigInteger>>   map = new HashMap<>();
    private BigInteger p;

    private HashMap<String, HashMap<Integer,String>>  mapFiles = new HashMap<>();
    private transient static SharesRing instance = null;

    protected SharesRing(){
        this.p = SecretSharing.generatePrime(Const.BITLENGHT);
    }
    public static SharesRing getInstance() {

        if(instance == null) {
            SharesRing sr = SharesRing.loadSharesRing(Const.SHARESFILE);
            if(sr == null) {
                instance = new SharesRing();
            } else {
                instance = sr;
            }
        }
        return instance;
    }


    public SecretSharing getShamir(){
        return new SecretSharing(this.p);
    }

    /*public void add(String id, HashMap<BigInteger,BigInteger> shares){
        map.put(id, shares);
    }

    private void initialize(){
        map = new HashMap<>();
    }
*/
    public BigInteger getP(){
        return p;
    }
    public void setMapFiles(HashMap<String, HashMap<Integer,String>>  mapFiles){
        this.mapFiles = mapFiles;
    }

    public static  SharesRing loadSharesRing(File f){
        SharesRing sr = null;
        if(!f.exists()){
            return sr;
        }
        try {

            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            sr = (SharesRing) ois.readObject();
            //sr.initialize();
            ois.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return sr;
    }

    public void saveSharesRing(File f, HashMap<String, HashMap<BigInteger,BigInteger>> map){

        File[] servers = Utils.getServers();

        try {

            //salva la share
            for(String s : map.keySet()){
                HashMap<Integer, String> temp = new HashMap<>();
                for(BigInteger bi : map.get(s).keySet()){
                    File tempfile;
                    String nomeShare;
                    do {

                        nomeShare = UUID.randomUUID().toString();
                        tempfile = new File(servers[bi.intValue()-1].getAbsolutePath() + "/" + nomeShare + ".share");
                    } while(f.exists());
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public KeyRing rebuild(String nomefile){


        File[] servers = Utils.getServers();
        KeyRing kr = new KeyRing(nomefile);
        for(String s : mapFiles.keySet()){
            String name = s.split(Pattern.quote(Const.SEPARATORKR))[0];
            String key = s.split(Pattern.quote(Const.SEPARATORKR))[1];

            SecretSharing sh = new SecretSharing(p);
            int i=0;
            if(name.equalsIgnoreCase(nomefile)){
                HashMap<Integer, String> temp = mapFiles.get(s);
                HashMap<BigInteger, BigInteger> shares = new HashMap<>();

                for(Map.Entry e :temp.entrySet()){
                    byte[] share = getShare((Integer)e.getKey(), (String)e.getValue());
                    if(share!=null){
                        i++;
                        shares.put(BigInteger.valueOf((int)e.getKey()), new BigInteger(share));

                    }
                }
                BigInteger secret = sh.getSecret(shares);
                kr.saveKey(Utils.getbyteFromBigInteger(secret), key);

            }
        }
        return kr;
    }
    public byte[] getShare(Integer i, String name){
        File server = new File("server/");
        File[] servers = new File[server.listFiles().length];

        for(File dir : server.listFiles()){
            String s = dir.getName().substring(0,2);
            int num = Integer.valueOf(s)-1;
            servers[num] = dir;
        }

        File f = new File(servers[i-1].getAbsolutePath() + "/" + name + ".share");
        if(f.exists()){
            try {
                FileInputStream fis = new FileInputStream(f);
                byte[] b =  fis.readAllBytes();
                fis.close();
                return b;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;

    }
    /*
    public void distribute() {

        File[] servers = Utils.getServers();

            try {

                //salva la share
                for(String s : map.keySet()){
                    HashMap<Integer, String> temp = new HashMap<>();
                    for(BigInteger bi : map.get(s).keySet()){
                        File f;
                        String nomeShare;
                        do {

                            nomeShare = UUID.randomUUID().toString();
                            f = new File(servers[bi.intValue()-1].getAbsolutePath() + "/" + nomeShare + ".share");
                        } while(f.exists());
                        f.createNewFile();
                        FileOutputStream fos = new FileOutputStream(f);
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
    }*/
}
