package progetto_2;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author carmineannunziata
 */
public class MessageShare implements Serializable {

    byte[] file;  //file in byte
    //HashMap<Integer, byte[]> block_file = new HashMap<>(); // file in blocchi di byte (id_blocco - byte_blocco)
    //Map<BigInteger, BigInteger> shares = new HashMap<>();  // id_share - val_share
    //Map<BigInteger,Map<BigInteger, BigInteger>> files= new HashMap<>(); // id_blocco - ( Map(id_share - val_share) )
    //Map<BigInteger,String> id_servers= new HashMap(); // id_server - nomerandomfile
    //Map<BigInteger,Map<BigInteger,String>> serversMap= new HashMap<>(); // id_blocco - ( Map(id_server - nomerandomfile) )
    //BigInteger s;


    //string chiave: identificativo file   n-blocco
    private HashMap<String, HashMap<Integer, HashMap<Integer, ShareEntry>>> mapFiles = new HashMap<>();

    private int k;
    private int n;
    private transient SecretSharing sc;

    public MessageShare(int k,int n){
        sc = SharesRing.getInstance().getShamir();
        this.k=k;
        this.n=n;

    }

    public void shareFile(String nome, FileInputStream fis) throws IOException {
        byte[] buffer = new byte[Const.BUFFER];
        int block = Const.BUFFER;
        int i = 0;
        HashMap<BigInteger, BigInteger> shares;

        while (block == Const.BUFFER) {
            block = fis.readNBytes(buffer, 0, block);

            if (block != 8) {
                byte[] buffer2 = new byte[block];
                for (int p = 0; p < buffer2.length; p++)
                    buffer2[p] = buffer[p];
                //do something buffer2
                shares = (HashMap<BigInteger, BigInteger>) genBlock(buffer2);

            } else {
                //do something buffer
                shares = (HashMap<BigInteger, BigInteger>) genBlock(buffer);

            }

            distribute(nome, shares,i);
            i++;
            //ho creato lo share, le ho distribuite e ho salvato i dati in map files

        }

        //Salva file su disco
        saveInstance();
    }

    //converte i blocchi in biginteger e li passa a Shamir per generare le share

    public Map<BigInteger, BigInteger> genBlock(byte[] block){
        BigInteger s= Utils.getBigInteger(block);
        return sc.genShares(s,k,n);
    }

    public void distribute(String nome, HashMap<BigInteger, BigInteger> shares, int i) throws IOException {
        File[] servers = Utils.getServers(); //di classe?
        HashMap<Integer, ShareEntry> temp1 = new HashMap<>();
        for (BigInteger bi : shares.keySet()) {
            File tempfile;
            String nomeShare;
            do {

                nomeShare = UUID.randomUUID().toString();
                tempfile = new File(servers[bi.intValue() - 1].getAbsolutePath() + "/" + nomeShare + ".share");
            } while (tempfile.exists());
            tempfile.createNewFile();
            FileOutputStream fos = new FileOutputStream(tempfile);
            //fos.write(Utils.getbyteFromBigInteger(map.get(s).get(bi)));
            fos.write(shares.get(bi).toByteArray());

            fos.flush();
            fos.close();

            //salva nella hashmap da scrivere
            temp1.put(bi.intValue(), new ShareEntry(nomeShare, shares.get(bi).hashCode()));
        }
        HashMap<Integer, HashMap<Integer, ShareEntry>>  val = mapFiles.getOrDefault(nome, new HashMap<>());
        val.put(i,temp1); //unire?
        mapFiles.put(nome,val);
    }
    private void saveInstance() {
        try {
            FileOutputStream fos = new FileOutputStream(Const.MESSAGEFILE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(this);

            oos.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void rebuilfFile(String nome, FileOutputStream fos) throws IOException {
        //a parte, devo caricarmi l'istanza della classe, qui mapFiles è già popolato
        //itera su mapfiles.get(nome)
        //ricostruisci segreto
        //scrivi segreto
        File[] servers = Utils.getServers();
        HashMap<Integer, HashMap<Integer, ShareEntry>> temp  = mapFiles.get(nome);//Dividere nome in nome+k!!!

        int minshares = 3;//!!!!
        for(int i=0;i<temp.size();i++){
            HashMap<Integer, ShareEntry> block = temp.get(i);
            HashMap<BigInteger, BigInteger> shares = new HashMap<>();
            int k = 0;

            for (Map.Entry e : block.entrySet()) {

                byte[] share = SharesRing.getShare(((ShareEntry) e.getValue()).getFile(), servers[(Integer) e.getKey() - 1]);

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
                throw new NotEnoughSharesException(nome, minshares);
            }
            BigInteger secret = sc.getSecret(shares);
            writeBlock(fos, secret);
        }
        fos.close();
    }

    private void writeBlock(FileOutputStream fos, BigInteger secret) {
        try {
            fos.write(Utils.getbyteFromBigInteger(secret));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static SharesRing loadMessageShare() {
        SharesRing sr = null;
        if (!Const.MESSAGEFILE.exists()) {
            return sr;
        }
        try {

            FileInputStream fis = new FileInputStream(Const.MESSAGEFILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            sr = (SharesRing) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            Const.MESSAGEFILE.delete(); //Utils.deleteData();
            throw new RuntimeException("Il file messageRing è corrotto.", e);
        }

        return sr;
    }


}


