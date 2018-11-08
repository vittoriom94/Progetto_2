package progetto_2;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;

public class SharesRing {
    //String = nomefile-tipochiave
    //hashMap <server, share>
    HashMap<String, HashMap<BigInteger,BigInteger>>  map = new HashMap<>();

    public void add(String id, HashMap<BigInteger,BigInteger> shares){
        map.put(id, shares);
    }


    public static SharesRing loadSharesRing(File f){
        SharesRing sr = null;


        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);

            sr = (SharesRing) ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return sr;
    }

    public void saveSharesRing(File f){
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

    public static void distribute() {
        File server = new File("server/");
        File[] servers = new File[server.listFiles().length];

        for(File dir : server.listFiles()){
            String s = dir.getName().substring(0,2);
            int num = Integer.valueOf(s)-1;
            servers[num] = dir;
        }

        for (int i = 0;i<servers.length;i++){
            String nomeShare = "nome";
            File f = new File(servers[i].getAbsolutePath()+"/"+ nomeShare+".share");
            if(!f.exists()){
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
