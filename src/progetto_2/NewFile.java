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


    // chiamato dalla soluzione 2
    private static void doCopy(InputStream is, OutputStream os)
            throws IOException {
        try (is; os) {
            byte[] bytes = new byte[4096];
            int numBytes;
            while ((numBytes = is.read(bytes)) != -1) {
                os.write(bytes, 0, numBytes);
            }
        }
    }


    //aggiungere key publickey nel metodo
    public boolean codifica(File file, File destinazione) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, SignatureException {
        try {
            int tot = 0;
            kr = new KeyRing(destinazione.getName());

            /*
		  	//Soluzione 1
		  	  int BUFFER = 8;
			  FileInputStream fis = new FileInputStream(file);
			  byte[] buffer = new byte[BUFFER]; //array contentente i byte letti a ogni iterazione
			  int read = 0; //ignorare
			  while ((read = fis.read(buffer)) > 0) {
			  	  //effettua update
				  String s = "";
				  for (byte b : buffer) {
					  s = s + (char) b;
				  }
				  System.out.println(s);
			  }
			  //qui dovrebbe esserci il dofinal??
			  fis.close();
				//-----------------------


			  //Soluzione 2
			  FileOutputStream os = new FileOutputStream("file2.txt");
			  KeyGenerator keyGenerator = KeyGenerator.getInstance(Match.cifrario_m.get(this.cifrario_m));
			  SecretKey secretKey = keyGenerator.generateKey();
			  Cipher cipher2 = Cipher.getInstance(Match.cifrario_m.get(this.cifrario_m)+"/"+Match.modi_operativi.get(this.modo_operativo)+"/PKCS5Padding");
			  cipher2.init(Cipher.ENCRYPT_MODE, secretKey);
			  CipherInputStream cis = new CipherInputStream(fis, cipher2);
			  doCopy(cis, os);
			  //----------------
*/


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
            tot = mittente.getBytes("UTF-8").length + 1;
            bytes.addAll(Utils.toByteArrayNonprimitive(destinatario.getBytes("UTF-8")));
            bytes.add(sl);
            tot = destinatario.getBytes("UTF-8").length + 1;
            bytes.add(cifrario_m);
            bytes.add(sl);
            tot += 2;
            bytes.add(cifrario_k);
            bytes.add(sl);
            tot += 2;
            bytes.add(padding);
            bytes.add(sl);
            tot += 2;
            bytes.add(integrita);
            bytes.add(sl);
            tot += 2;
            bytes.add(modo_operativo);
            bytes.add(sl);
            tot += 2;
            bytes.add(tipo);
            bytes.add(sl);
            tot += 2;
            bytes.add(dimensione_firma);
            bytes.add(sl);
            tot += 2;
            bytes.addAll(Utils.toByteArrayNonprimitive(salt));
            tot += salt.length;
            int n = this.cifrario_m == (byte) 0x00 ? 16 : 8;
            iv = new byte[n];
            tot += n;
            if (modo_operativo != (byte) 0x00) {
                random.nextBytes(iv);

            } else {
                Arrays.fill(iv, (byte) 0x00);
            }
            bytes.addAll(Utils.toByteArrayNonprimitive(iv));

            os.write(Utils.toByteArray(bytes));

            int i = 0;

            //cifrario RSA
            //Cipher cipherkey = Cipher.getInstance("RSA/" + Match.modi_operativi.get(this.modo_operativo) + "/" + Match.padding.get(this.padding) + "Padding");
            Cipher cipherkey = Cipher.getInstance("RSA/ECB/" + Match.padding.get(this.padding) + "Padding");
            /*
            //leggi chiave pubblica
            FileInputStream fis = new FileInputStream(keyFile);
            byte[] publickeyBytes = fis.readAllBytes();
            fis.close();
            //da bytes a PublicKey, la public key viene codificata con x509, la privata dovrebbe essere PKCS8 CONTROLLARE
            X509EncodedKeySpec ks = new X509EncodedKeySpec(publickeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publickey = kf.generatePublic(ks);
            */

            //genera chiave pubblica e privata
            //saveKeyPair(cifrario_k, "RSA");


            //cifra la chiave
            X509EncodedKeySpec ks = new X509EncodedKeySpec(kr.getKey("RSAPublic"));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publickey = kf.generatePublic(ks);

            cipherkey.init(Cipher.ENCRYPT_MODE, publickey);
            byte byteKey[] = secretKey.getEncoded();
            byte[] cypherkey = cipherkey.doFinal(byteKey);
            System.out.println("lunghezza chiave des aes " + cypherkey.length + "\n" + cypherkey[0]);
            i=0;
            while (i < cypherkey.length) {
                System.out.print(cypherkey[i] + " ");
                i++;
            }
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
                //Signature signature = Signature.getInstance (Match.tipo.get(this.tipo));
                Signature signature = Signature.getInstance("SHA224withDSA");

                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                kr.saveKey(keyPair.getPublic().getEncoded(), "DSAPub");
                kr.saveKey(keyPair.getPrivate().getEncoded(), "DSAPri");

                signature.initSign(keyPair.getPrivate());

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
                os.write(digitalSignature);
                os.flush();
                os.close();
                f.close();
                tot += digitalSignature.length;
                f = new FileInputStream(file);
                System.out.println();
                RandomAccessFile file1 = new RandomAccessFile(destinazione, "rw");
                for (int p = 0; p < tot; p++) {
                    byte c = file1.readByte();
                }
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
                //Signature signature = Signature.getInstance (Match.tipo.get(this.tipo));
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

    public boolean decodifica(File file, File destinazione) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

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
        i=0;
        while (i < secretKey.length) {
            System.out.print(secretKey[i] + " ");
            i++;
        }
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
        //messaggio + mac
        byte[] mac;
        if (Match.tipo.get(this.tipo) == "HmacMD5")
            mac = fis.readNBytes(16);
        if (Match.tipo.get(this.tipo) == "HmacSHA256")
            mac = fis.readNBytes(32);
        if (Match.tipo.get(this.tipo) == "HmacSHA384")
            mac = fis.readNBytes(48);

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

        return true;
    }
}

