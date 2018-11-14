package progetto_2;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class NewFile {
    private String mittente;
    private String destinatario;
    private byte cifrario_m;//00 AES ; 01 DES ; 02 TRIPLEDES
    private byte cifrario_k;//00 RSA-1024 ; 01 RSA-2048
    private byte padding;// 00 PKCS1 ; 01 OAEP
    private byte integrita;//00 Firma ; 01 MAC ; 02 Hash
    private byte[] salt;//8 byte
    private byte modo_operativo;//00 ECB ; 01 CBC ; 02 CFB
    private byte[] iv;//8 byte des/triple des;16 aes
    private byte tipo;//00 SHA1 ; 01 SHA224 ; 02 ; SHA256 ; 03 SHA384 ; 04 SHA512
    //05 MD5 ; 06 SHA256 ; 07 SHA384
    //08 SHA1; 09 SHA224; 10 SHA256
    private byte dimensione_firma;//00 1024 ; 01 2048;
    private byte[] messaggio;

    private KeyRing kr;

    public NewFile(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita,
                   byte[] salt, byte modi_operativi, byte[] iv, byte tipo, byte dimensione_firma, byte[] messaggio) {
        super();
        this.mittente = mittente;
        this.destinatario = destinatario;
        this.cifrario_m = cifrario_m;
        this.cifrario_k = cifrario_k;
        this.padding = padding;
        this.integrita = integrita;
        this.modo_operativo = modi_operativi;
        this.tipo = tipo;
        this.dimensione_firma = dimensione_firma;
        this.messaggio = messaggio;
    }

    public NewFile() {
    }

    public void saveKeyPair(byte size, String type) throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance(type);

        gen.initialize(Match.dimensione.get(size));
        KeyPair k = gen.generateKeyPair();
        Key publickey = k.getPublic();
        Key privatekey = k.getPrivate();

        kr.saveKey(publickey.getEncoded(), type + "Public");
        kr.saveKey(privatekey.getEncoded(), type + "Private");
        //System.out.println(publickey.getFormat() + " " + privatekey.getFormat() + " " + publickey.getEncoded().length + " " + privatekey.getEncoded().length + " " + Match.dimensione.get(size));
        /*FileOutputStream fos = new FileOutputStream(file);

        fos.write(publickey.getEncoded());

        fos.flush();
        fos.close();
        //System.out.println(file.getName() + " " + file.getParent() + " " + file.getPath());
        fos = new FileOutputStream(file.getPath().substring(0, file.getPath().length()-4) + "Private.txt");
        fos.write(size);
        fos.write(privatekey.getEncoded());
        fos.flush();
        fos.close();
        */
    }



    //aggiungere key publickey nel metodo
    public boolean codifica(File file, File destinazione) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, SignatureException {
        try {
            int tot = 0;
            kr = new KeyRing(destinazione.getName());
            FileOutputStream os = new FileOutputStream(destinazione);

            //chiave messaggio
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

            //iv e salt
            SecureRandom random = new SecureRandom();
            salt = new byte[8];
            random.nextBytes(salt);
            byte sl = (byte) '.';
            ArrayList<Byte> bytes = new ArrayList<Byte>();
            bytes.addAll(Utils.toByteArrayNonprimitive(mittente.getBytes("UTF-8")));
            bytes.add(sl);
            bytes.addAll(Utils.toByteArrayNonprimitive(destinatario.getBytes("UTF-8")));
            bytes.add(sl);
            bytes.add(cifrario_m);
            bytes.add(sl);
            bytes.add(cifrario_k);
            bytes.add(sl);
            bytes.add(padding);
            bytes.add(sl);
            bytes.add(integrita);
            bytes.add(sl);
            bytes.add(modo_operativo);
            bytes.add(sl);
            bytes.add(tipo);
            bytes.add(sl);
            bytes.add(dimensione_firma);
            bytes.add(sl);
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
            tot = bytes.size();
            int i = 0;

            //cifrario RSA
            //Cipher cipherkey = Cipher.getInstance("RSA/" + Match.modi_operativi.get(this.modo_operativo) + "/" + Match.padding.get(this.padding) + "Padding");
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
            if(integrita == 0) {
                Signature signature = Signature.getInstance (Match.tipo.get(this.tipo));
                //Signature signature = Signature.getInstance ("SHA224withDSA");
                saveKeyPair( dimensione_firma,"DSA");


                PKCS8EncodedKeySpec ks2 = new PKCS8EncodedKeySpec(kr.getKey("DSAPrivate"));
                KeyFactory kf2 = KeyFactory.getInstance("DSA");
                PrivateKey privateKey = kf2.generatePrivate(ks2);

                signature.initSign (privateKey);

                FileInputStream f = new FileInputStream(file);
                while(block == 8) {
                    block=f.readNBytes(buffer,0,block);
                    if(block!=8) {
                        byte[] buffer2 = new byte[block];
                        for(int p=0;p<buffer2.length;p++)
                            buffer2[p]=buffer[p];
                        signature.update(buffer2);
                        for(int p=0;p<buffer2.length;p++)
                            System.out.print(buffer2[p]);
                    }
                    else {
                        for(int p=0;p<buffer.length;p++)
                            System.out.print(buffer[p]);
                        signature.update(buffer);
                    }
                }

                byte [] digitalSignature = signature.sign ();
                System.out.println("Firma+lunghezza: "+ digitalSignature.length);
                for(int p=0;p<digitalSignature.length;p++)
                    System.out.print(digitalSignature[p]);
                os.write(digitalSignature);
                tot += digitalSignature.length;

                os.flush();
                os.close();
                f.close();
                f = new FileInputStream(file);
                System.out.println();
                int c;
                RandomAccessFile file1 = new RandomAccessFile (destinazione,"rw");
                for(int p=0;p<tot;p++)
                    c = file1.read();


                System.out.println("Messaggio con la cifratura");
                block=8;
                while(block == 8) {
                    block=f.readNBytes(buffer,0,block);
                    if(block!=8) {
                        byte[] buffer2 = new byte[block];
                        for(int p1=0;p1<buffer2.length;p1++)
                            buffer2[p1]=buffer[p1];
                        for(int p1=0;p1<buffer2.length;p1++)
                            System.out.print(buffer2[p1]);
                        file1.write(cipher.update(buffer2,0,block));
                    }
                    else {
                        for(int p1=0;p1<buffer.length;p1++)
                            System.out.print(buffer[p1]);
                        file1.write(cipher.update(buffer,0,block));
                    }

                }
                file1.write(cipher.doFinal());
                file1.close();
                f.close();
                //System.out.println((char)c);


            }
            if(integrita == 1) {

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
                if (Match.tipo.get(this.tipo) == "Sha-1")
                    lunghezza = 20;
                if (Match.tipo.get(this.tipo) == "Sha-224")
                    lunghezza = 28;
                if (Match.tipo.get(this.tipo) == "Sha-256")
                    lunghezza = 32;
                if (Match.tipo.get(this.tipo) == "Sha-384")
                    lunghezza = 48;
                if (Match.tipo.get(this.tipo) == "Sha-512/256")
                    lunghezza = 32;
                byte[] b = new byte[lunghezza];
                os.write(b);
                MessageDigest digest = MessageDigest.getInstance("SHA-256");

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
            kr.saveShamir(3, 5);
            return true;
        } catch (IOException e) {
            System.out.println("Errore: " + e);
            System.exit(1);
            return false;
        }
    }



    public boolean decodifica(File file, File destinazione) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, SignatureException {

        SharesRing sr = SharesRing.getInstance();
        kr = sr.rebuild(file.getName());
        FileInputStream fis = new FileInputStream(file);


        byte sl = (byte) '.';
        ArrayList<byte[]> params = new ArrayList<byte[]>();
        int num = 9;
        int i = 0;
        ArrayList<Byte> ba = new ArrayList<Byte>();
        while (i < num) {
            byte b = (byte) fis.read();
            ba.add(b);
            if (b == sl) {
                params.add(Utils.toByteArray(ba));
                i++;
                ba.clear();
            }
        }
        //this.mittente = Utils.byteArrayToString(params.get(0));
        //this.destinatario = Utils.byteArrayToString(params.get(1));
        this.mittente = new String(params.get(0), "UTF-8");
        this.destinatario = new String(params.get(1), "UTF-8");
        this.cifrario_m = params.get(2)[0];
        this.cifrario_k = params.get(3)[0];
        this.padding = params.get(4)[0];
        this.integrita = params.get(5)[0];
        this.modo_operativo = params.get(6)[0];
        this.tipo = params.get(7)[0];
        this.dimensione_firma = params.get(8)[0];

        this.salt = fis.readNBytes(8);
        if (this.cifrario_m == (byte) 0x00) {
            System.out.println("here");
            this.iv = fis.readNBytes(16);
        } else {
            System.out.println("there");
            this.iv = fis.readNBytes(8);
        }
        //trova lunghezza rsa
        //FileInputStream fisKey = new FileInputStream(keyFile);

        //int rsaKeySize = (fisKey.read() +1)*128;
        //System.out.println("key size: " + rsaKeySize);
        //byte[] secretKey = fis.readNBytes(rsaKeySize);
        byte[] secretKey = kr.getKey("Secret");


        //decodifica chiave tramite RSA e privateRSAKey;

        //byte[] privateKeyBytes = fisKey.readAllBytes();
        //fisKey.close();

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
        if(integrita==0) {
            X509EncodedKeySpec ks2 = new X509EncodedKeySpec(kr.getKey("DSAPublic"));
            KeyFactory kf2 = KeyFactory.getInstance("DSA");
            PublicKey publicKey = kf2.generatePublic(ks2);

            byte [] temp = fis.readNBytes(2);
            byte[] digitalSignature=new byte[(int)temp[1]+2];

            for(int p=0;p<digitalSignature.length;p++) {
                if(p<2)
                    digitalSignature[p]=temp[p];
                else
                    digitalSignature[p]=(byte)fis.read();
            }
            for(int p=0;p<digitalSignature.length;p++)
                System.out.print(digitalSignature[p]);
            System.out.println();
            Signature signature = Signature.getInstance (Match.tipo.get(this.tipo));
            //Signature signature = Signature.getInstance("SHA224WithDSA");

            signature.initVerify(publicKey);

            FileOutputStream fos = new FileOutputStream(destinazione);

            //inserire parte inserimentoo chiave dal keyring
            int block = 8;
            block=fis.read(buffer);
            while(block == 8) {
                byte[] c = cipher.update(buffer);
                for(int p=0;p<c.length;p++)
                    System.out.print(c[p]);
                signature.update(c);
                fos.write(c);
                block=fis.readNBytes(buffer,0,block);
            }
            byte[] o = cipher.doFinal();
            fos.write(o);
            System.out.println();
            for(int p=0;p<o.length;p++)
                System.out.print(o[p]);
            signature.update(o);
            boolean verified = signature.verify(digitalSignature);
            System.out.println(verified);
            fos.flush();
            fos.close();
            fis.close();
        }
        if(integrita==1) {
            byte[] mac=null;
            if(Match.tipo.get(this.tipo)=="HmacMD5")
                mac = fis.readNBytes(16);
            if(Match.tipo.get(this.tipo)=="HmacSHA256")
                mac = fis.readNBytes(32);
            if(Match.tipo.get(this.tipo)=="HmacSHA384")
                mac = fis.readNBytes(48);
            System.out.println();
            for(int ll=0;ll<mac.length;ll++)
                System.out.print(mac[ll]);
            FileOutputStream fos = new FileOutputStream(destinazione);

            //inserire parte inserimentoo chiave dal keyring
            Mac mac2 = Mac.getInstance(Match.tipo.get(this.tipo));
            SecretKeySpec macKey = new SecretKeySpec(kr.getKey("MAC"), mac2.getAlgorithm());
            mac2.init(macKey);

            int block = 8;
            block=fis.read(buffer);

            while(block == 8) {
                byte[] c = cipher.update(buffer);
                for(int p=0;p<c.length;p++)
                    System.out.print(c[p]);
                fos.write(c);
                mac2.update(c);
                block=fis.readNBytes(buffer,0,block);
            }
            byte[] o = cipher.doFinal();
            fos.write(o);
            System.out.println();
            for(int p=0;p<o.length;p++)
                System.out.println(o[p]);
            mac2.update(o);
            byte[] macBytes2 = mac2.doFinal();
            System.out.print("\ndecodifica\n");
            for(int p=0;p<macBytes2.length;p++)
                System.out.print(macBytes2[p]);
            fos.flush();
            fos.close();
            fis.close();
        }
        return true;

    }

    public static KeyRing createKeyPair(byte size, String type, KeyRing kr){
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
        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException("Algoritmo: " + type,e);
        }

    }

}

