package progetto_2;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.*;



public class MessageShare implements Serializable {
    //string chiave: identificativo file   n-blocco
    private HashMap<Identifier, HashMap<Integer, HashMap<Integer, ShareEntry>>> mapFiles = new HashMap<>();
    private transient static MessageShare instance = null;
    private transient File[] servers;

    private transient SecretSharing sh;

    protected MessageShare(){
    }

    public static MessageShare getInstance(){
        if (instance == null) {
            MessageShare ms = MessageShare.loadMessageShare();
            instance = Objects.requireNonNullElseGet(ms,MessageShare::new);
            instance.saveInstance();
        }
        instance.servers = Utils.getServers();
        return instance;
    }

    public void shareFile(String mittente, String destinatario, String nome, File file,int k,int n) throws IOException {
        if(sh == null){
            sh = SharesRing.getInstance().getShamir();
        }
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[Const.BUFFERMESSAGE];
        int block = Const.BUFFERMESSAGE;
        int i = 0;
        HashMap<BigInteger, BigInteger> shares;

        while (block == Const.BUFFERMESSAGE) {
            block = fis.readNBytes(buffer, 0, block);

            if (block != 8) {
                byte[] buffer2 = new byte[block];
                for (int p = 0; p < buffer2.length; p++)
                    buffer2[p] = buffer[p];
                //do something buffer2
                shares = (HashMap<BigInteger, BigInteger>) genBlock(buffer2,  k,  n);

            } else {
                //do something buffer
                shares = (HashMap<BigInteger, BigInteger>) genBlock(buffer, k, n);

            }
            Identifier id = new Identifier(mittente,destinatario,nome,k,"");
            distribute(id, shares,i);
            i++;
            //ho creato lo share, le ho distribuite e ho salvato i dati in map files

        }
        fis.close();

        //Salva file su disco
        saveInstance();
    }

    //converte i blocchi in biginteger e li passa a Shamir per generare le share

    private Map<BigInteger, BigInteger> genBlock(byte[] block,int k, int n){
        BigInteger s= Utils.getBigInteger(block);
        return sh.genShares(s,k,n);
    }

    private void distribute(Identifier id, HashMap<BigInteger, BigInteger> shares, int i) throws IOException {
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
        HashMap<Integer, HashMap<Integer, ShareEntry>>  val = mapFiles.getOrDefault(id, new HashMap<>());
        val.put(i,temp1); //unire?
        mapFiles.put(id,val);
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

    public void rebuildFile(Identifier id, File file)  {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            if (sh == null) {
                sh = SharesRing.getInstance().getShamir();
            }
            //a parte, devo caricarmi l'istanza della classe, qui mapFiles è già popolato
            //itera su mapfiles.get(nome)
            //ricostruisci segreto
            //scrivi segreto
            HashMap<Integer, HashMap<Integer, ShareEntry>> temp = mapFiles.get(id);//Dividere nome in nome+k!!!

            int minshares = 0;
            for (Identifier id2 : mapFiles.keySet()) {
                if (id.equals(id2)) {
                    minshares = id2.getK();
                    break;
                }
            }
            for (int i = 0; i < temp.size(); i++) {
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
                if (k < minshares) {
                    throw new NotEnoughSharesException(id.getNome(), minshares);
                }
                BigInteger secret = sh.getSecret(shares);
                writeBlock(fos, secret);
            }
            fos.close();
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private void writeBlock(FileOutputStream fos, BigInteger secret) {
        try {
            fos.write(Utils.getbyteFromBigInteger(secret));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static MessageShare loadMessageShare() {
        MessageShare ms = null;
        if (!Const.MESSAGEFILE.exists()) {
            return ms;
        }
        try {

            FileInputStream fis = new FileInputStream(Const.MESSAGEFILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ms = (MessageShare) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            Const.MESSAGEFILE.delete(); //Utils.deleteData();
            throw new RuntimeException("Il file messageRing è corrotto.", e);
        }

        return ms;
    }

    public void clear() {
        this.mapFiles = new HashMap<>();
        saveInstance();
    }

    public Set<Identifier> getIdentifiers(){
        return mapFiles.keySet();
    }

}


