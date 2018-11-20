package progetto_2;

import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.sasl.AuthenticationException;


public abstract class NewFile {
    private String mittente;
    private String destinatario;
    private byte cifrario_m;//00 AES ; 01 DES ; 02 TRIPLEDES
    private byte cifrario_k;//00 RSA-1024 ; 01 RSA-2048
    private byte padding;// 00 PKCS1 ; 01 OAEP
    private byte integrita;//00 Firma ; 01 MAC ; 02 Hash
    protected byte[] salt;//8 byte
    private byte modo_operativo;//00 ECB ; 01 CBC ; 02 CFB
    private byte[] iv;//8 byte des/triple des;16 aes
    protected byte tipo;
    //00 SHA1 ; 01 SHA224 ; 02 ; SHA256 ; 03 SHA384 ; 04 SHA512
    //05 MD5 ; 06 SHA256 ; 07 SHA384
    //08 SHA1; 09 SHA224; 10 SHA256
    protected byte dimensione_firma;//00 1024 ; 01 2048;
    private byte[] messaggio;

    protected KeyRing kr;
    protected File file;
    protected File destinazione;
    private int kShare;
    private int nShare;
    private String timestamp;

    public NewFile(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita,
                   byte modi_operativi, byte tipo, byte dimensione_firma, File file, File destinazione, int kShare, int nShare) {
        this.mittente = mittente;
        this.destinatario = destinatario;
        this.cifrario_m = cifrario_m;
        this.cifrario_k = cifrario_k;
        this.padding = padding;
        this.integrita = integrita;
        this.modo_operativo = modi_operativi;
        this.tipo = tipo;
        this.dimensione_firma = dimensione_firma;
        this.file = file;
        this.destinazione = destinazione;
        this.kShare = kShare;
        this.nShare = nShare;
        this.timestamp = System.currentTimeMillis()+"";
        codifica();
    }

    public NewFile(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita, byte modo_operativo, byte tipo, byte dimensione_firma, byte[] salt, byte[] iv) {
        this.mittente = mittente;
        this.destinatario = destinatario;
        this.cifrario_m = cifrario_m;
        this.cifrario_k = cifrario_k;
        this.padding = padding;
        this.integrita = integrita;
        this.modo_operativo = modo_operativo;
        this.tipo = tipo;
        this.dimensione_firma = dimensione_firma;
        this.salt = salt;
        this.iv = iv;

    }

    public static boolean decodifica(String nome, File file, File destinazione){
        try {

            Utils.printFile(file);
            FileInputStream fis = new FileInputStream(file);
            NewFile nf =  readHeader(fis);

            boolean result = nf.completeDecode(fis,nome,destinazione);
            return result;

        } catch(IOException | SignatureException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e){

            throw new RuntimeException(e);
        } finally{
            file.delete();
        }

    }

    private boolean completeDecode(FileInputStream fis,String nome, File destinazione) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, IOException, SignatureException {
        this.destinazione = destinazione;
        SharesRing sr = SharesRing.getInstance();
        kr = sr.rebuild(mittente,destinatario,nome);
        SecretKey secretKey = rebuildSecretKey(fis);
        Cipher cipher = getCipher(secretKey);


        if(this instanceof CodificaHash){
            FileOutputStream temp = new FileOutputStream(Const.tempHashD);
            bufferReadEncode(temp,cipher,fis);


            temp.write(cipher.doFinal());
            temp.flush();
            temp.close();
            Utils.printFile(Const.tempHashD);
            fis = new FileInputStream(Const.tempHashD);
        }
        byte[] verifier = readVerifier(fis);
        getVerifier();
        byte[] newVerifier = bufferReadDecode(fis, cipher);
        
        boolean v = verify(verifier, newVerifier);
        Const.tempHashD.delete();
        return v;
    }

    protected abstract boolean verify(byte[] verifier, byte[] newVerifier) throws SignatureException;

    private Cipher getCipher(SecretKey secretKey) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(Match.cifrario_m.get(this.cifrario_m) + "/" + Match.modi_operativi.get(this.modo_operativo) + "/PKCS5Padding");
        if (modo_operativo == (byte) 0x00) {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        }
        return cipher;
    }

    protected byte[] bufferReadDecode(FileInputStream fis, Cipher cipher) throws SignatureException, IOException, BadPaddingException, IllegalBlockSizeException {
        FileOutputStream fos = new FileOutputStream(destinazione);
        byte[] buffer = new byte[Const.BUFFER];
        int block = Const.BUFFER;
        /*block = fis.read(buffer);
        while (block == 8) {
            byte[] c = cipher.update(buffer);
            updateVerifier(c);
            fos.write(c);
            block = fis.readNBytes(buffer, 0, block);
        }*/


        while (block == Const.BUFFER) {
            block = fis.readNBytes(buffer, 0, block);
            if (block != Const.BUFFER && block != 0) {
                byte[] buffer2 = new byte[block];
                for (int p = 0; p < buffer2.length; p++)
                    buffer2[p] = buffer[p];
                byte[] c = cipher.update(buffer2);
                updateVerifier(c);
                fos.write(c);
            } else {
                if(block != 0) {
                    byte[] c = cipher.update(buffer);
                    updateVerifier(c);
                    fos.write(c);
                }
            }
        }


        byte[] o = cipher.doFinal();
        fos.write(o);
        updateVerifier(o);
        byte[] verifier = retrieveVerifier();
        fos.flush();
        fos.close();
        fis.close();
        return verifier;
    }

    protected abstract byte[] retrieveVerifier();

    protected abstract void updateVerifier(byte[] c) throws SignatureException;

    protected abstract void getVerifier() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, AuthenticationException;

    protected abstract byte[] readVerifier(FileInputStream fis) throws IOException;

    private SecretKey rebuildSecretKey(FileInputStream fis) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
        //byte[] secretKey = kr.getKey("Secret");



        //decodifica chiave tramite RSA e privateRSAKey;

        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(kr.getKey("RSAPrivate"));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(ks);

        //Cipher cipherkey = Cipher.getInstance("RSA/" + Match.modi_operativi.get(this.modo_operativo) + "/" + Match.padding.get(this.padding) + "Padding");
        Cipher cipherkey = Cipher.getInstance("RSA/ECB/" + Match.padding.get(this.padding) + "Padding");
        cipherkey.init(Cipher.DECRYPT_MODE, privateKey);


        RSAPrivateKey rpk = (RSAPrivateKey) privateKey;
        int byteLength = rpk.getModulus().bitLength()/8;
        byte[] secretKey = new byte[byteLength];
        fis.readNBytes(secretKey,0,byteLength);
        System.out.println("lunghezza chiave des aes " + secretKey.length + "\n" + secretKey[0]);

        System.out.println(" ");
        byte[] secretKeyDecodedByte = cipherkey.doFinal(secretKey);

        return new SecretKeySpec(secretKeyDecodedByte, 0, secretKeyDecodedByte.length, Match.cifrario_m.get(this.cifrario_m));
    }

    private static NewFile readHeader(FileInputStream fis) throws IOException {
        byte sl = (byte) Const.MESSAGESEPARATOR.charAt(0);
        ArrayList<byte[]> params = new ArrayList<>();
        int num = 2;
        int i = 0;
        ArrayList<Byte> ba = new ArrayList<>();
        while (i < num) {
            byte b = (byte) fis.read();

            if (b == sl) {
                params.add(Utils.toByteArray(ba));
                i++;
                ba.clear();
            } else {
                ba.add(b);
            }
        }

        //this.mittente = Utils.byteArrayToString(params.get(0));
        //this.destinatario = Utils.byteArrayToString(params.get(1));
        String mittente = new String(params.get(0), "UTF-8");
        String destinatario = new String(params.get(1), "UTF-8");
        byte cifrario_m = (byte) fis.read();
        byte cifrario_k = (byte) fis.read();
        byte  padding = (byte) fis.read();
        byte integrita = (byte) fis.read();
        byte modo_operativo = (byte) fis.read();
        byte tipo = (byte) fis.read();
        byte dimensione_firma = (byte) fis.read();

        byte[] salt = fis.readNBytes(8);
        byte[] iv;
        if (cifrario_m == (byte) 0x00) {
            iv = fis.readNBytes(16);
        } else {
            iv = fis.readNBytes(8);
        }
        switch(integrita){
            case 0: return new CodificaSign(mittente,destinatario,cifrario_m,cifrario_k,padding,integrita,modo_operativo,tipo,dimensione_firma,salt, iv);
            case 1: return new CodificaMAC(mittente,destinatario,cifrario_m,cifrario_k,padding,integrita,modo_operativo,tipo,dimensione_firma,salt, iv);
            default: return new CodificaHash(mittente,destinatario,cifrario_m,cifrario_k,padding,integrita,modo_operativo,tipo,dimensione_firma,salt, iv);
        }
    }



    public void codifica() {
        try {
            kr = SharesRing.getInstance().rebuild(mittente,destinatario,timestamp+"-"+file.getName());
            FileOutputStream os = new FileOutputStream(destinazione);
            //Genera chiave segreta per il messaggio
            SecretKey secretKey = generateSecretKey();
            //cifrario messaggio, AES o DES
            Cipher cipher = Cipher.getInstance(Match.cifrario_m.get(this.cifrario_m) + "/" + Match.modi_operativi.get(this.modo_operativo) + "/PKCS5Padding");
            //header messaggio cifrato, generazione iv e salt
            ArrayList<Byte> bytes = generateHeader();
            os.write(Utils.toByteArray(bytes));

            //cifra la chiave segreta
            byte[] secretKeyBytes = encryptSecretKey(secretKey);
            os.write(secretKeyBytes);
            int tot = bytes.size()+secretKeyBytes.length;
            //inizializza cifrario messaggio

            if (modo_operativo == (byte) 0x00) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            }


            //integrità
            handleMessage(cipher, os, tot);

            os.flush();
            os.close();

            // eliminare
            Utils.printFile(destinazione);


            //salva keyring
            kr.saveShamir(mittente,destinatario,kShare, nShare);
            MessageShare.getInstance().shareFile(mittente,destinatario,timestamp+"-"+file.getName(),destinazione,kShare,nShare);
        }catch(IOException | SignatureException | InvalidKeySpecException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException e){
            throw new RuntimeException(e);
        } finally {
            destinazione.delete();
        }

    }

    protected abstract void handleMessage(Cipher cipher, FileOutputStream os, int tot) throws IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, InvalidKeyException, NoSuchAlgorithmException, SignatureException;

    protected void writeVerifier(int tot,byte[] verifier) throws IOException {

        System.out.print("\ncodifica\n");
        RandomAccessFile file1 = new RandomAccessFile(destinazione, "rw");
        for (int p = 0; p < tot; p++) {
            byte c = file1.readByte();
        }
        file1.write(verifier);
        file1.close();
    }

    protected abstract byte[] completeVerifier() throws SignatureException;

    protected void bufferReadEncode(FileOutputStream os, Cipher cipher, FileInputStream f) throws IOException, SignatureException {
        //per firma, override bufferReadEncode update firma e richiama super
        byte[] buffer = new byte[Const.BUFFER];
        int block = Const.BUFFER;

        while (block == Const.BUFFER) {
            block = f.readNBytes(buffer, 0, block);
            if (block != Const.BUFFER && block != 0) {
                byte[] buffer2 = new byte[block];
                for (int p = 0; p < buffer2.length; p++)
                    buffer2[p] = buffer[p];
                handleBuffer(buffer2, block, cipher, os);
            } else {
                if(block != 0) {
                    handleBuffer(buffer, block, cipher, os);
                }


            }
        }
        f.close();

    }

    protected abstract void handleBuffer(byte[] buffer, int block, Cipher cipher, FileOutputStream os) throws IOException, SignatureException;

    protected abstract void createVerifier(FileOutputStream os) throws IOException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException;


    private byte[] encryptSecretKey(SecretKey secretKey) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, AuthenticationException {
        //cifrario RSA
        Cipher cipherkey = Cipher.getInstance("RSA/ECB/" + Match.padding.get(this.padding) + "Padding");

        //cifra la chiave
        X509EncodedKeySpec ks = new X509EncodedKeySpec(kr.getKey("RSAPublic"));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publickey = kf.generatePublic(ks);

        cipherkey.init(Cipher.ENCRYPT_MODE, publickey);
        byte byteKey[] = secretKey.getEncoded();
        byte[] cypherkey = cipherkey.doFinal(byteKey);
        System.out.println("lunghezza chiave des aes " + cypherkey.length + "\n" + cypherkey[0]);

        System.out.println(" ");


        return cypherkey;
    }

    private ArrayList<Byte> generateHeader() throws UnsupportedEncodingException {
        SecureRandom random = new SecureRandom();
        ArrayList<Byte> bytes = new ArrayList<>();
        byte sl = (byte) Const.MESSAGESEPARATOR.charAt(0);
        bytes.addAll(Utils.toByteArrayNonprimitive(mittente.getBytes("UTF-8")));
        bytes.add(sl);
        bytes.addAll(Utils.toByteArrayNonprimitive(destinatario.getBytes("UTF-8")));
        bytes.add(sl);
        bytes.add(cifrario_m);
        bytes.add(cifrario_k);
        bytes.add(padding);
        bytes.add(integrita);
        bytes.add(modo_operativo);
        bytes.add(tipo);
        bytes.add(dimensione_firma);
        salt = new byte[8];
        random.nextBytes(salt);
        bytes.addAll(Utils.toByteArrayNonprimitive(salt));
        int n = this.cifrario_m == (byte) 0x00 ? 16 : 8;
        iv = new byte[n];
        if (modo_operativo != (byte) 0x00) {
            random.nextBytes(iv);

        } else {
            Arrays.fill(iv, (byte) 0x00);
        }
        bytes.addAll(Utils.toByteArrayNonprimitive(iv));
        return bytes;
    }

    private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = null;

        keyGenerator = KeyGenerator.getInstance(Match.cifrario_m.get(this.cifrario_m));

        //aes 128, des 56, triple des 112
        int keySize;
        if (cifrario_m == 0) {
            keySize = 128;
        } else if (cifrario_m == 1) {
            keySize = 56;
        } else {
            keySize = 112;
        }
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();
    }


    public static KeyRing createKeyPair(byte size, String type, KeyRing kr) {
        try {
            if (kr == null) {
                kr = new KeyRing(Const.ANYMESSAGEID);//???
            }
            KeyPairGenerator gen = KeyPairGenerator.getInstance(type);

            gen.initialize(Match.dimensione.get(size));
            KeyPair k = gen.generateKeyPair();
            Key publickey = k.getPublic();
            Key privatekey = k.getPrivate();
            kr.saveKey(publickey.getEncoded(), type + "Public");
            kr.saveKey(privatekey.getEncoded(), type + "Private");

            return kr;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo: " + type, e);
        }

    }
}
