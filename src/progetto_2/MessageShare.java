package progetto_2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author carmineannunziata
 */
public class MessageShare {

    byte[] file;  //file in byte
    HashMap<Integer, byte[]> block_file = new HashMap<>(); // file in blocchi di byte (id_blocco - byte_blocco)
    Map<BigInteger, BigInteger> shares = new HashMap<>();  // id_share - val_share
    Map<BigInteger,Map<BigInteger, BigInteger>> files= new HashMap<>(); // id_blocco - ( Map(id_share - val_share) )
    Map<BigInteger,String> id_servers= new HashMap(); // id_server - nomerandomfile
    Map<BigInteger,Map<BigInteger,String>> serversMap= new HashMap<>(); // id_blocco - ( Map(id_server - nomerandomfile) )
    BigInteger s;

    int buffer;
    int k;
    int n;
    int bitlength;
    SecretSharing sc;

    public MessageShare(int k,int n){

        this.k=k;
        this.n=n;

    }


    //suddivide il file in blocchi di grandezza buffer

    public HashMap<Integer,byte[]> divideFile(byte[] file, int buffer) {

        int dim_row=(int)Math.ceil(file.length / (int)buffer)+1;
        int dim_col=buffer;
        int start = 0;

        for(int i = 0; i < dim_row; i++) {
            block_file.put(i, Arrays.copyOfRange(file,start, start + buffer));
            start += buffer ;

        }

        return block_file;
    }

    //converte i blocchi in biginteger e li passa a Shamir per generare le share

    public void genBlock(){
        for (Integer key : block_file.keySet()) {
            s=new BigInteger(1,block_file.get(key));
            int lenP = s.bitLength()+1;
            SecretSharing sh= new SecretSharing(lenP);
            shares = sh.genShares(s,k,n);
            BigInteger b=new BigInteger("1");
            files.put(BigInteger.valueOf(key), shares);


        }

        for(BigInteger key : files.keySet())
        {for(BigInteger key2 : files.get(key).keySet())
        {
            System.out.println("chiave1: " + key + "  chiave2: "+ key2+ "  valore: " + files.get(key).get(key2));
        }
            System.out.println("\n");}
        // distribute();


    }











    //distribuisce i blocchi
    public void distribute() {


        String fileName="/Users/carmineannunziata/Desktop/local.txt";
        File server=new File("/server");
        File[] servers=new File[server.listFiles().length];


        File local=new File(fileName);



        try {
            local.createNewFile();
            FileOutputStream los = new FileOutputStream(local);
            ObjectOutputStream olos = new ObjectOutputStream(los);

            //salva la share
            for(BigInteger b : files.keySet()){
                for(BigInteger bi : files.get(b).keySet()){
                    File f;
                    String nomeShare;
                    do {
                        nomeShare = UUID.randomUUID().toString();
                        System.out.print("nomeshare: "+nomeShare);

                        f = new File(servers[bi.intValue()-1].getAbsolutePath() + "/" + nomeShare + ".share");
                    } while(f.exists());
                    f.createNewFile();
                    FileOutputStream fos = new FileOutputStream(f);
                    ObjectOutputStream ofos = new ObjectOutputStream(fos);
                    ofos.writeObject(shares.get(bi));
                    ofos.flush();
                    ofos.close();
                    //salva nella hashmap da scrivere
                    id_servers.put(bi.add(BigInteger.valueOf(bi.hashCode())), nomeShare + ".share");
                }
                //salva nella map del scrivere
                serversMap.put(b, id_servers);

            }
            olos.writeObject(serversMap);
            olos.flush();
            olos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void restructure(String fileName){

        int dim=n*buffer;
        BigInteger bi=new BigInteger("dim");
        FileInputStream fis;
        try {
            fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            //popola serversMap
            serversMap=(Map<BigInteger, Map<BigInteger, String>>) ois.readObject();
            ois.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MessageShare.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MessageShare.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MessageShare.class.getName()).log(Level.SEVERE, null, ex);
        }


        //popola id_servers
        for (BigInteger key : serversMap.keySet())
            id_servers=serversMap.get(key);

        for (BigInteger key : serversMap.keySet()) // id_blocco - ( Map(id_server - nomerandomfile) )
        { BigInteger i=new BigInteger("00");
            int j=0;
            for (BigInteger key2 : id_servers.keySet()) // id_server - nomerandomfile
            {
                String server_path=new String(i.toString()+ "/" +id_servers.get(key2));
                int hash= key2.subtract(i).intValue();

                try {
                    fis = new FileInputStream(server_path);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    if(hash==ois.readObject().hashCode() && j<=k)
                    {
                        shares.put(key2,(BigInteger)ois.readObject());
                        j++;}
                    else
                        System.out.print("Salta share");

                } catch (IOException ex) {
                    Logger.getLogger(MessageShare.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(MessageShare.class.getName()).log(Level.SEVERE, null, ex);
                }

                i.add(BigInteger.valueOf(1));
            }

            bi.add(sc.getSecret(shares));

        }



    }
}