package progetto_2;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SharesRing implements Serializable{
    //String = nomefile|tipochiave|p
    //hashMap <server, share>
    private transient HashMap<String, HashMap<BigInteger,BigInteger>>   map = new HashMap<>();
    private BigInteger p;

    private HashMap<String, HashMap<Integer,String>>  mapFiles = new HashMap<>();

    public void add(String id, HashMap<BigInteger,BigInteger> shares){
        //Devo distribuirle qui


        map.put(id, shares);
    }

    private void initialize(){
        map = new HashMap<>();
    }

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
            sr.initialize();
            ois.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return sr;
    }

    public void saveSharesRing(File f, BigInteger p){
        this.p = p;
        distribute();
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




        File server = new File("server/");
        File[] servers = new File[server.listFiles().length];
        for(File dir : server.listFiles()){
            String s = dir.getName().substring(0,2);
            int num = Integer.valueOf(s)-1;
            servers[num] = dir;
        }
        KeyRing kr = new KeyRing(nomefile);
        for(String s : mapFiles.keySet()){
            String name = s.split("-")[0];
            String key = s.split("-")[1];

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

    public void distribute() {
        File server = new File("server/");
        File[] servers = new File[server.listFiles().length];

        for(File dir : server.listFiles()){
            String s = dir.getName().substring(0,2);
            int num = Integer.valueOf(s)-1;
            servers[num] = dir;
        }


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




    }
}
