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
    private File destinazione;
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

    }

    public static boolean decodifica(String nome, File file, File destinazione){
        try {

            FileInputStream fis = new FileInputStream(file);
            NewFile nf =  readHeader(fis);
            boolean result = nf.completeDecode(fis,nome,destinazione);
            return result;

        } catch(IOException | SignatureException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e){
            throw new RuntimeException(e);
        }

    }

    private boolean completeDecode(FileInputStream fis,String nome, File destinazione) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, IOException, SignatureException {
        this.destinazione = destinazione;
        SharesRing sr = SharesRing.getInstance();
        kr = sr.rebuild(mittente,destinatario,nome);
        SecretKey secretKey = rebuildSecretKey(fis);
        Cipher cipher = getCipher(secretKey);
        byte[] verifier = readVerifier(fis);
        getVerifier();
        byte[] newVerifier = bufferReadDecode(fis, cipher);
        
        boolean v = verify(verifier, newVerifier);
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

    private byte[] bufferReadDecode(FileInputStream fis, Cipher cipher) throws SignatureException, IOException, BadPaddingException, IllegalBlockSizeException {
        FileOutputStream fos = new FileOutputStream(destinazione);
        byte[] buffer = new byte[8];
        int block = 8;
        block = fis.read(buffer);
        while (block == 8) {
            byte[] c = cipher.update(buffer);
            updateVerifier(c);
            fos.write(c);
            block = fis.readNBytes(buffer, 0, block);
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
            System.out.println("here");
            iv = fis.readNBytes(16);
        } else {
            System.out.println("there");
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
            kr = SharesRing.getInstance().rebuild(mittente,destinatario,file.getName()+timestamp);
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

            FileInputStream fisdebug = new FileInputStream(destinazione);
            byte[] readall = fisdebug.readAllBytes();
            System.out.println("Stampa codifica\n");
            for(byte b: readall){
                System.out.print(b+ " ");
            }
            System.out.println();
            fisdebug.close();


            //salva keyring
            kr.saveShamir(mittente,destinatario,kShare, nShare);
            MessageShare.getInstance().shareFile(mittente,destinatario,file.getName()+timestamp,destinazione,kShare,nShare);
        }catch(IOException | SignatureException | InvalidKeySpecException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException e){
            throw new RuntimeException(e);
        }

    }

    protected void handleMessage(Cipher cipher, FileOutputStream os, int tot) throws IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        createVerifier(os);
        bufferReadEncode(os, cipher);
        os.write(cipher.doFinal());
        byte[] verifier = completeVerifier();
        writeVerifier(tot,verifier);
    }

    private void writeVerifier(int tot,byte[] verifier) throws IOException {

        System.out.print("\ncodifica\n");
        RandomAccessFile file1 = new RandomAccessFile(destinazione, "rw");
        for (int p = 0; p < tot; p++) {
            byte c = file1.readByte();
        }
        file1.write(verifier);
        file1.close();
    }

    protected abstract byte[] completeVerifier() throws SignatureException;

    protected void bufferReadEncode(FileOutputStream os, Cipher cipher) throws IOException, SignatureException {
        //per firma, override bufferReadEncode update firma e richiama super
        byte[] buffer = new byte[8];
        int block = 8;
        FileInputStream f = new FileInputStream(file);

        while (block == 8) {
            block = f.readNBytes(buffer, 0, block);
            if (block != 8) {
                byte[] buffer2 = new byte[block];
                for (int p = 0; p < buffer2.length; p++)
                    buffer2[p] = buffer[p];
                handleBuffer(buffer2, block, cipher, os);
            } else {
                handleBuffer(buffer, block, cipher, os);

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
   /*
    public boolean codifica(File file, File destinazione, int kShare, int nShare) throws
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, SignatureException {
        try {

            //genera Keyring
            kr = new KeyRing(destinazione.getName());
            FileOutputStream os = new FileOutputStream(destinazione);

            //Genera chiave segreta per il messaggio
            KeyGenerator keyGenerator = KeyGenerator.getInstance(Match.cifrario_m.get(this.cifrario_m));
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
            SecretKey secretKey = keyGenerator.generateKey();

            //cifrario messaggio, AES o DES
            Cipher cipher = Cipher.getInstance(Match.cifrario_m.get(this.cifrario_m) + "/" + Match.modi_operativi.get(this.modo_operativo) + "/PKCS5Padding");

            //header messaggio cifrato, generazione iv e salt
            SecureRandom random = new SecureRandom();

            byte sl = (byte) Const.MESSAGESEPARATOR.charAt(0);
            ArrayList<Byte> bytes = new ArrayList<Byte>();
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

            os.write(Utils.toByteArray(bytes));
            int tot = bytes.size();

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

            kr.saveKey(cypherkey, "Secret");

            //Cipher cipherkey = Cipher.getInstance("RSA/"+Match.modi_operativi.get(this.modo_operativo)+"/"+Match.padding.get(this.padding));
            //mac+messaggio
            byte[] buffer = new byte[8];
            if (modo_operativo == (byte) 0x00) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            }
            int lunghezza = 0;
            int block = 8;
            if (integrita == 0) {
                Signature signature = Signature.getInstance(Match.tipo.get(this.tipo));
                NewFile.createKeyPair(dimensione_firma, "DSA", this.kr);


                PKCS8EncodedKeySpec ks2 = new PKCS8EncodedKeySpec(kr.getKey("DSAPrivate"));
                KeyFactory kf2 = KeyFactory.getInstance("DSA");
                PrivateKey privateKey = kf2.generatePrivate(ks2);

                signature.initSign(privateKey);

                FileInputStream f = new FileInputStream(file);
                while (block == 8) {
                    block = f.readNBytes(buffer, 0, block);
                    if (block != 8) {
                        byte[] buffer2 = new byte[block];
                        for (int p = 0; p < buffer2.length; p++)
                            buffer2[p] = buffer[p];
                        signature.update(buffer2);
                        for (int p = 0; p < buffer2.length; p++)
                            System.out.print(buffer2[p]);
                    } else {
                        for (int p = 0; p < buffer.length; p++)
                            System.out.print(buffer[p]);
                        signature.update(buffer);
                    }
                }

                byte[] digitalSignature = signature.sign();
                System.out.println("Firma+lunghezza: " + digitalSignature.length);
                for (int p = 0; p < digitalSignature.length; p++)
                    System.out.print(digitalSignature[p]);
                os.write(digitalSignature);
                tot += digitalSignature.length;//<----------

                os.flush();
                os.close();
                f.close();
                f = new FileInputStream(file);
                System.out.println();
                int c;
                RandomAccessFile file1 = new RandomAccessFile(destinazione, "rw");
                for (int p = 0; p < tot; p++)
                    c = file1.read();


                System.out.println("Messaggio con la cifratura");
                block = 8;
                while (block == 8) {
                    block = f.readNBytes(buffer, 0, block);
                    if (block != 8) {
                        byte[] buffer2 = new byte[block];
                        for (int p1 = 0; p1 < buffer2.length; p1++)
                            buffer2[p1] = buffer[p1];
                        for (int p1 = 0; p1 < buffer2.length; p1++)
                            System.out.print(buffer2[p1]);
                        file1.write(cipher.update(buffer2, 0, block));
                    } else {
                        for (int p1 = 0; p1 < buffer.length; p1++)
                            System.out.print(buffer[p1]);
                        file1.write(cipher.update(buffer, 0, block));
                    }

                }
                file1.write(cipher.doFinal());
                file1.close();
                f.close();
                //System.out.println((char)c);


            }
            if (integrita == 1) {

                if (Match.tipo.get(this.tipo) == "HmacMD5")
                    lunghezza = 16;
                if (Match.tipo.get(this.tipo) == "HmacSHA256")
                    lunghezza = 32;
                if (Match.tipo.get(this.tipo) == "HmacSHA384")
                    lunghezza = 48;
                byte[] b = new byte[lunghezza];
                os.write(b);

                KeyGenerator keygen = KeyGenerator.getInstance(Match.tipo.get(this.tipo));
                Key macKey = keygen.generateKey();
                kr.saveKey(macKey.getEncoded(), "MAC");
                Mac mac = Mac.getInstance(Match.tipo.get(this.tipo));
                mac.init(macKey);


                FileInputStream f = new FileInputStream(file);
                while (block == 8) {
                    block = f.readNBytes(buffer, 0, block);
                    if (block != 8) {
                        byte[] buffer2 = new byte[block];
                        for (int p = 0; p < buffer2.length; p++)
                            buffer2[p] = buffer[p];
                        for (int p = 0; p < buffer2.length; p++)
                            System.out.print(buffer2[p]);
                        os.write(cipher.update(buffer2, 0, block));
                        mac.update(buffer2);
                    } else {
                        for (int p = 0; p < buffer.length; p++)
                            System.out.print(buffer[p]);
                        os.write(cipher.update(buffer, 0, block));
                        mac.update(buffer);
                    }

                }
                byte[] macBytes2 = mac.doFinal();
                os.write(cipher.doFinal());
                os.flush();
                os.close();
                System.out.print("\ncodifica\n");
                for (int p = 0; p < macBytes2.length; p++)
                    System.out.print(macBytes2[p]);
                RandomAccessFile file1 = new RandomAccessFile("codificato.txt", "rw");
                for (int p = 0; p < tot; p++) {
                    byte c = file1.readByte();
                    //System.out.println((char)c);
                }
                file1.write(macBytes2);
                file1.close();
            }
            if (integrita == 2) {
                //controllare traccia
                if (Match.tipo.get(this.tipo) == "SHA-1")
                    lunghezza = 20;
                if (Match.tipo.get(this.tipo) == "SHA-224")
                    lunghezza = 28;
                if (Match.tipo.get(this.tipo) == "SHA-256")
                    lunghezza = 32;
                if (Match.tipo.get(this.tipo) == "SHA-384")
                    lunghezza = 48;
                if (Match.tipo.get(this.tipo) == "SHA-512")
                    lunghezza = 64;
                byte[] b = new byte[lunghezza];
                os.write(b);
                MessageDigest digest = MessageDigest.getInstance(Match.tipo.get(this.tipo));
                digest.update(salt);
                FileInputStream f = new FileInputStream(file);
                while (block == 8) {
                    block = f.readNBytes(buffer, 0, block);
                    if (block != 8) {
                        byte[] buffer2 = new byte[block];
                        for (int p = 0; p < buffer2.length; p++)
                            buffer2[p] = buffer[p];
                        digest.update(buffer2);
                        os.write(cipher.update(buffer2, 0, block));
                        for (int p = 0; p < buffer2.length; p++)
                            System.out.print(buffer2[p]);
                    } else {
                        for (int p = 0; p < buffer.length; p++)
                            System.out.print(buffer[p]);
                        digest.update(buffer);
                        os.write(cipher.update(buffer, 0, block));
                    }
                }
                byte[] hashValue = digest.digest();
                os.write(cipher.doFinal());
                os.flush();
                os.close();
                System.out.print("\ncodifica\n");
                for (int p = 0; p < hashValue.length; p++)
                    System.out.print(hashValue[p]);
                RandomAccessFile file1 = new RandomAccessFile("codificato.txt", "rw");
                for (int p = 0; p < tot; p++) {
                    byte c = file1.readByte();
                    //System.out.println((char)c);
                }

                file1.write(hashValue);
                file1.close();

                //System.out.println((char)c);


            }
            kr.saveShamir(kShare, nShare);
            return true;
        } catch (IOException e) {
            System.out.println("Errore: " + e);
            System.exit(1);
            return false;
        }
    }
    */
    /*
    public boolean decodifica(File file, File destinazione) throws
            IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, SignatureException {

        SharesRing sr = SharesRing.getInstance();
        kr = sr.rebuild(file.getName());
        FileInputStream fis = new FileInputStream(file);


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
        this.mittente = new String(params.get(0), "UTF-8");
        this.destinatario = new String(params.get(1), "UTF-8");
        this.cifrario_m = (byte) fis.read();
        this.cifrario_k = (byte) fis.read();
        this.padding = (byte) fis.read();
        this.integrita = (byte) fis.read();
        this.modo_operativo = (byte) fis.read();
        this.tipo = (byte) fis.read();
        this.dimensione_firma = (byte) fis.read();

        this.salt = fis.readNBytes(8);
        if (this.cifrario_m == (byte) 0x00) {
            System.out.println("here");
            this.iv = fis.readNBytes(16);
        } else {
            System.out.println("there");
            this.iv = fis.readNBytes(8);
        }
        byte[] secretKey = kr.getKey("Secret");


        //decodifica chiave tramite RSA e privateRSAKey;

        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(kr.getKey("RSAPrivate"));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(ks);

        //Cipher cipherkey = Cipher.getInstance("RSA/" + Match.modi_operativi.get(this.modo_operativo) + "/" + Match.padding.get(this.padding) + "Padding");
        Cipher cipherkey = Cipher.getInstance("RSA/ECB/" + Match.padding.get(this.padding) + "Padding");
        cipherkey.init(Cipher.DECRYPT_MODE, privateKey);

        System.out.println("lunghezza chiave des aes " + secretKey.length + "\n" + secretKey[0]);

        System.out.println(" ");
        byte[] secretKeyDecodedByte = cipherkey.doFinal(secretKey);

        SecretKey secretKeyDecoded = new SecretKeySpec(secretKeyDecodedByte, 0, secretKeyDecodedByte.length, Match.cifrario_m.get(this.cifrario_m));

        //decodifica messaggio tramite chiave decodificata;
        Cipher cipher = Cipher.getInstance(Match.cifrario_m.get(this.cifrario_m) + "/" + Match.modi_operativi.get(this.modo_operativo) + "/PKCS5Padding");
        if (modo_operativo == (byte) 0x00) {
            cipher.init(Cipher.DECRYPT_MODE, secretKeyDecoded);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKeyDecoded, new IvParameterSpec(iv));
        }
        byte[] buffer = new byte[8];
        if (integrita == 0) {
            X509EncodedKeySpec ks2 = new X509EncodedKeySpec(kr.getKey("DSAPublic"));
            KeyFactory kf2 = KeyFactory.getInstance("DSA");
            PublicKey publicKey = kf2.generatePublic(ks2);

            byte[] temp = fis.readNBytes(2);
            byte[] digitalSignature = new byte[(int) temp[1] + 2];

            for (int p = 0; p < digitalSignature.length; p++) {
                if (p < 2)
                    digitalSignature[p] = temp[p];
                else
                    digitalSignature[p] = (byte) fis.read();
            }
            for (int p = 0; p < digitalSignature.length; p++)
                System.out.print(digitalSignature[p]);
            System.out.println();
            Signature signature = Signature.getInstance(Match.tipo.get(this.tipo));
            //Signature signature = Signature.getInstance("SHA224WithDSA");

            signature.initVerify(publicKey);

            FileOutputStream fos = new FileOutputStream(destinazione);

            //inserire parte inserimentoo chiave dal keyring
            int block = 8;
            block = fis.read(buffer);
            while (block == 8) {
                byte[] c = cipher.update(buffer);
                for (int p = 0; p < c.length; p++)
                    System.out.print(c[p]);
                signature.update(c);
                fos.write(c);
                block = fis.readNBytes(buffer, 0, block);
            }
            byte[] o = cipher.doFinal();
            fos.write(o);
            System.out.println();
            for (int p = 0; p < o.length; p++)
                System.out.print(o[p]);
            signature.update(o);
            boolean verified = signature.verify(digitalSignature);
            System.out.println(verified);
            fos.flush();
            fos.close();
            fis.close();
        }
        if (integrita == 1) {
            byte[] mac = null;
            if (Match.tipo.get(this.tipo) == "HmacMD5")
                mac = fis.readNBytes(16);
            if (Match.tipo.get(this.tipo) == "HmacSHA256")
                mac = fis.readNBytes(32);
            if (Match.tipo.get(this.tipo) == "HmacSHA384")
                mac = fis.readNBytes(48);
            System.out.println();
            for (int ll = 0; ll < mac.length; ll++)
                System.out.print(mac[ll]);
            FileOutputStream fos = new FileOutputStream(destinazione);

            //inserire parte inserimentoo chiave dal keyring
            Mac mac2 = Mac.getInstance(Match.tipo.get(this.tipo));
            SecretKeySpec macKey = new SecretKeySpec(kr.getKey("MAC"), mac2.getAlgorithm());
            mac2.init(macKey);

            int block = 8;
            block = fis.read(buffer);

            while (block == 8) {
                byte[] c = cipher.update(buffer);
                for (int p = 0; p < c.length; p++)
                    System.out.print(c[p]);
                fos.write(c);
                mac2.update(c);
                block = fis.readNBytes(buffer, 0, block);
            }
            byte[] o = cipher.doFinal();
            fos.write(o);
            System.out.println();
            for (int p = 0; p < o.length; p++)
                System.out.println(o[p]);
            mac2.update(o);
            byte[] macBytes2 = mac2.doFinal();
            System.out.print("\ndecodifica\n");
            for (int p = 0; p < macBytes2.length; p++)
                System.out.print(macBytes2[p]);
            fos.flush();
            fos.close();
            fis.close();
        }
        if (integrita == 2) {

            byte[] hash = null;
            if (Match.tipo.get(this.tipo) == "SHA-1")
                hash = fis.readNBytes(20);
            if (Match.tipo.get(this.tipo) == "SHA-224")
                hash = fis.readNBytes(28);
            if (Match.tipo.get(this.tipo) == "SHA-256")
                hash = fis.readNBytes(32);
            if (Match.tipo.get(this.tipo) == "SHA-384")
                hash = fis.readNBytes(48);
            if (Match.tipo.get(this.tipo) == "SHA-512")
                hash = fis.readNBytes(64);
            System.out.println("Hash");
            for (int ll = 0; ll < hash.length; ll++)
                System.out.print(hash[ll]);
            FileOutputStream fos = new FileOutputStream(destinazione);

            MessageDigest digest = MessageDigest.getInstance(Match.tipo.get(this.tipo));
            digest.update(salt);
            int block = 8;
            block = fis.read(buffer);

            while (block == 8) {
                byte[] c = cipher.update(buffer);
                for (int p = 0; p < c.length; p++)
                    System.out.print(c[p]);
                fos.write(c);
                digest.update(c);
                block = fis.readNBytes(buffer, 0, block);
            }
            byte[] o = cipher.doFinal();
            fos.write(o);
            System.out.println();
            for (int p = 0; p < o.length; p++)
                System.out.println(o[p]);
            digest.update(o);
            byte[] hashBytes = digest.digest();
            System.out.print("\nhashDec\n");
            for (int p = 0; p < hashBytes.length; p++)
                System.out.print(hashBytes[p]);
            fos.flush();
            fos.close();
            fis.close();
        }
        return true;

    }

    */
}
