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
        sc = SharesRing.getInstance().getShamir();
        this.k=k;
        this.n=n;

    }

    public void shareFile(FileInputStream fis) throws IOException {
        byte[] buffer = new byte[Const.BUFFER];
        int block = Const.BUFFER;
        int i = 0;
        Map<BigInteger, BigInteger> shares;

        while (block == Const.BUFFER) {
            block = fis.readNBytes(buffer, 0, block);
            i++;
            if (block != 8) {
                byte[] buffer2 = new byte[block];
                for (int p = 0; p < buffer2.length; p++)
                    buffer2[p] = buffer[p];
                //do something buffer2
                shares = genBlock(buffer2);

            } else {
                //do something buffer
                shares = genBlock(buffer);

            }
            //leggi bufferizzato (while)
            //      blocco i: distribuisci, ottieni le share, salva le share

        }
    }




    //suddivide il file in blocchi di grandezza buffer

    public HashMap<Integer,byte[]> divideFile(FileInputStream file) {

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
    //non è bufferizzato, dare in input il blocco?
    public Map<BigInteger, BigInteger> genBlock(byte[] block){
            BigInteger s= Utils.getBigInteger(block);
            return sc.genShares(s,k,n);
    }











    //distribuisce i blocchi
    //stessa cosa di sopra
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

    //non è bufferizzata
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