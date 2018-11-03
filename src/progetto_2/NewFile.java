package progetto_2;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.*;
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
    private byte hash;//00 SHA1 ; 01 SHA224 ; 02 ; SHA256 ; 03 SHA384 ; 04 SHA512
    private byte mac;//00 MD5 ; 01 SHA256 ; 03 SHA384
    private byte firma;//00 SHA1; 02 SHA224; 04 SHA256
    private byte dimensione_firma;//00 1024 ; 01 2048;
    private byte[] messaggio;

    public NewFile(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita,
                   byte[] salt, byte modi_operativi, byte[] iv, byte hash, byte mac, byte firma, byte dimensione_firma, byte[] messaggio) {
        super();
        this.mittente = mittente;
        this.destinatario = destinatario;
        this.cifrario_m = cifrario_m;
        this.cifrario_k = cifrario_k;
        this.padding = padding;
        this.integrita = integrita;
        this.modo_operativo = modi_operativi;
        this.hash = hash;
        this.mac = mac;
        this.firma = firma;
        this.dimensione_firma = dimensione_firma;
        this.messaggio = messaggio;
    }

    public NewFile() {
    }

    public void saveKeyPair(File file, byte size) throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");

        gen.initialize(Match.dimensione.get(size));
        KeyPair k = gen.generateKeyPair();
        Key publickey = k.getPublic();
        Key privatekey = k.getPrivate();
        //System.out.println(publickey.getFormat() + " " + privatekey.getFormat() + " " + publickey.getEncoded().length + " " + privatekey.getEncoded().length + " " + Match.dimensione.get(size));
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(publickey.getEncoded());

        fos.flush();
        fos.close();
        //System.out.println(file.getName() + " " + file.getParent() + " " + file.getPath());
        fos = new FileOutputStream(file.getPath().substring(0, file.getPath().length()-4) + "Private.txt");
        fos.write(privatekey.getEncoded());
        fos.flush();
        fos.close();
    }

    public void creazione() {
        try {
            FileOutputStream file = new FileOutputStream("file.txt");
            PrintStream Output = new PrintStream(file);
            Output.print(mittente);
            Output.print(destinatario);
            Output.print(cifrario_m);
            Output.print(cifrario_k);
            Output.print(padding);
            Output.print(integrita);
            Output.print(salt[1]);
            Output.print(modo_operativo);
            Output.print(iv[1]);
            Output.print(hash);
            Output.print(mac);
            Output.print(firma);
            Output.print(dimensione_firma);
            Output.println(messaggio[1]);
        } catch (IOException e) {
            System.out.println("Errore: " + e);
            System.exit(1);
        }
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
    public boolean codifica(File keyFile, File file, File destinazione) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        try {
            //chiave per il cifrario messaggio
            KeyGenerator keyGenerator = KeyGenerator.getInstance(Match.cifrario_m.get(this.cifrario_m));
            SecretKey secretKey = keyGenerator.generateKey();
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
            /*
            PrintStream Output = new PrintStream(file);
            Output.print(mittente + "_");
            Output.print(destinatario + "_");
            Output.print(cifrario_m + "_");
            Output.print(cifrario_k + "_");
            Output.print(padding + "_");
            Output.print(integrita + "_");
            Output.print(salt[1] + "_");
            Output.print(modo_operativo + "_");
            Output.print(iv[1] + "_");
            Output.print(hash + "_");
            Output.print(mac + "_");
            Output.print(firma + "_");
            Output.println(dimensione_firma + "_");
            */

            //chiave messaggio
            keyGenerator = KeyGenerator.getInstance(Match.cifrario_m.get(this.cifrario_m));
            keyGenerator.init(128);
            secretKey = keyGenerator.generateKey();

            //cifrario messaggio, AES o DES
            Cipher cipher = Cipher.getInstance(Match.cifrario_m.get(this.cifrario_m) + "/" + Match.modi_operativi.get(this.modo_operativo) + "/PKCS5Padding");

            //iv e salt
            iv = cipher.getIV();
            SecureRandom random = new SecureRandom();
            salt = new byte[8];
            random.nextBytes(salt);
            byte sl = (byte)'.';
            ArrayList<Byte> bytes = new ArrayList<Byte>();
            bytes.addAll(Utils.toByteArrayNonprimitive(mittente.getBytes()));
            bytes.add(sl);
            bytes.addAll(Utils.toByteArrayNonprimitive(destinatario.getBytes()));
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
            bytes.add(hash);
            bytes.add(sl);
            bytes.add(mac);
            bytes.add(sl);
            bytes.add(firma);
            bytes.add(sl);
            bytes.add(dimensione_firma);
            bytes.add(sl);
            bytes.addAll(Utils.toByteArrayNonprimitive(salt));
            if(iv!=null) {
                bytes.addAll(Utils.toByteArrayNonprimitive(iv));
            } else {
                int n = this.modo_operativo==(byte)0x00?16:8;
                for(int i = 0;i<n;i++) {
                    bytes.add((byte) 0x00);
                }
            }

            os.write(Utils.toByteArray(bytes));
            int i = 0;

            //cifrario RSA
            Cipher cipherkey = Cipher.getInstance("RSA/" + Match.modi_operativi.get(this.modo_operativo) + "/" + Match.padding.get(this.padding) + "Padding");

            //leggi chiave pubblica
            FileInputStream fis = new FileInputStream(keyFile);
            byte[] publickeyBytes = fis.readAllBytes();
            fis.close();
            //da bytes a PublicKey, la public key viene codificata con x509, la privata dovrebbe essere PKCS8 CONTROLLARE
            X509EncodedKeySpec ks = new X509EncodedKeySpec(publickeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publickey = kf.generatePublic(ks);

            //cifra la chiave
            cipherkey.init(Cipher.ENCRYPT_MODE, publickey);
            byte byteKey[] = secretKey.getEncoded();
            byte[] cypherkey = cipherkey.doFinal(byteKey);
            System.out.println("lunghezza chiave des aes " + cypherkey.length + "\n" + cypherkey[0]);
            while (i < cypherkey.length) {
                os.write(cypherkey[i]);
                i++;
            }

            //Cipher cipherkey = Cipher.getInstance("RSA/"+Match.modi_operativi.get(this.modo_operativo)+"/"+Match.padding.get(this.padding));
            //cifro il messaggio
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            messaggio = (new FileInputStream(file)).readAllBytes();
            byte[] cyphertext = cipher.doFinal(this.messaggio);
            i=0;
            while (i < cyphertext.length) {
                os.write(cyphertext[i]);
                i++;
            }
            os.flush();
            os.close();
            return true;

        } catch (IOException e) {
            System.out.println("Errore: " + e);
            System.exit(1);
            return false;
        }
    }

    public boolean decodifica(File file, File destinazione, File keyFile) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        FileInputStream fis = new FileInputStream(file);

        byte sl = (byte)'.';
        ArrayList<byte[]> params = new ArrayList<byte[]>();
        int num = 11;
        int i = 0;
        ArrayList<Byte> ba = new ArrayList<Byte>();
        while(i < num){
            byte b = (byte)fis.read();
            ba.add(b);
            if(b == '.'){
                params.add(Utils.toByteArray(ba));
                i++;
                ba.clear();
            }
        }
        this.mittente = Utils.byteArrayToString(params.get(0));
        this.destinatario = Utils.byteArrayToString(params.get(1));
        this.cifrario_m = params.get(2)[0];
        this.cifrario_k = params.get(3)[0];
        this.padding = params.get(4)[0];
        this.integrita = params.get(5)[0];
        this.modo_operativo = params.get(6)[0];
        this.hash = params.get(7)[0];
        this.mac = params.get(8)[0];
        this.firma = params.get(9)[0];
        this.dimensione_firma = params.get(10)[0];

        this.salt = fis.readNBytes(8);
        if(this.modo_operativo==(byte)0x00){
            System.out.println("here");
            this.iv=fis.readNBytes(16);
        } else {
            System.out.println("there");
            this.iv=fis.readNBytes(8);
        }

        byte[] secretKey = fis.readNBytes(128);
        byte[] messaggio = fis.readAllBytes();
        fis.close();



        //decodifica chiave tramite RSA e privateRSAKey;
        fis = new FileInputStream(keyFile);
        byte[] privateKeyBytes = fis.readAllBytes();
        fis.close();

        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(ks);

        Cipher cipherkey = Cipher.getInstance("RSA/" + Match.modi_operativi.get(this.modo_operativo) + "/" + Match.padding.get(this.padding) + "Padding");
        cipherkey.init(Cipher.DECRYPT_MODE, privateKey);
        System.out.println("RSA/" + Match.modi_operativi.get(this.modo_operativo) + "/" + Match.padding.get(this.padding) + "Padding");

        System.out.println("lunghezza chiave des aes " + secretKey.length + "\n" + secretKey[0]);
        byte[] secretKeyDecodedByte = cipherkey.doFinal(secretKey);

        SecretKey secretKeyDecoded = new SecretKeySpec(secretKeyDecodedByte, 0, secretKeyDecodedByte.length, Match.cifrario_m.get(this.cifrario_m));

        //decodifica messaggio tramite chiave decodificata;
        Cipher cipher = Cipher.getInstance(Match.cifrario_m.get(this.cifrario_m) + "/" + Match.modi_operativi.get(this.modo_operativo) + "/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeyDecoded);

        this.messaggio = cipher.doFinal(messaggio);

        FileOutputStream fos = new FileOutputStream(destinazione);
        fos.write(this.messaggio);
        fos.flush();
        fos.close();



        return true;
    }
}

