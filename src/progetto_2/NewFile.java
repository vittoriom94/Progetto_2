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
        this.tipo=tipo;
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

        kr.saveKey(publickey.getEncoded(), type+"Public");
        kr.saveKey(privatekey.getEncoded(),type+"Private");
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
    public boolean codifica(File file, File destinazione) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        try {

            kr = new KeyRing();

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
            if(cifrario_m==0){
                keySize=128;
            } else if(cifrario_m==1){
                keySize=56;
            } else {
                keySize  =112;
            }
            keyGenerator.init(keySize);
            SecretKey secretKey = keyGenerator.generateKey();

            //cifrario messaggio, AES o DES
            Cipher cipher = Cipher.getInstance(Match.cifrario_m.get(this.cifrario_m) + "/" + Match.modi_operativi.get(this.modo_operativo) + "/PKCS5Padding");

            //iv e salt
            SecureRandom random = new SecureRandom();
            salt = new byte[8];
            random.nextBytes(salt);
            byte sl = (byte)'.';
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
            int n = this.cifrario_m==(byte)0x00?16:8;
            iv = new byte[n];

            if(modo_operativo!=(byte)0x00) {
                random.nextBytes(iv);

            } else {
                Arrays.fill(iv, (byte)0x00);
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
            saveKeyPair( cifrario_k,"RSA");
            

            //cifra la chiave
            X509EncodedKeySpec ks = new X509EncodedKeySpec(kr.getKey("RSAPublic"));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publickey = kf.generatePublic(ks);

            cipherkey.init(Cipher.ENCRYPT_MODE, publickey);
            byte byteKey[] = secretKey.getEncoded();
            byte[] cypherkey = cipherkey.doFinal(byteKey);
            System.out.println("lunghezza chiave des aes " + cypherkey.length + "\n" + cypherkey[0]);
            /*while (i < cypherkey.length) {
                os.write(cypherkey[i]);
                i++;
            }*/
            kr.saveKey(cypherkey,"Secret");
            int lunghezza;
            if(Match.tipo.get(this.tipo)=="HmacMD5") 
            	lunghezza = 16;
            if(Match.tipo.get(this.tipo)=="HmacSHA256") 
            	lunghezza = 32;	
            if(Match.tipo.get(this.tipo)=="HmacSHA384") 
            	lunghezza = 48;
            byte b[] = new byte[lunghezza];
            bytes.addAll(Utils.toByteArrayNonprimitive(b));
            //Cipher cipherkey = Cipher.getInstance("RSA/"+Match.modi_operativi.get(this.modo_operativo)+"/"+Match.padding.get(this.padding));
            //mac+messaggio
            if(integrita == 0){
            	KeyGenerator keygen = KeyGenerator.getInstance(Match.tipo.get(this.tipo));
            	Key macKey = keygen.generateKey();
            	kr.saveKey(macKey.getEncoded(), "MAC");
            	Mac mac = Mac.getInstance(Match.tipo.get(this.tipo));
            	mac.init(macKey);
            	if(modo_operativo==(byte)0x00){
            		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            	} else {
            		cipher.init(Cipher.ENCRYPT_MODE, secretKey,new IvParameterSpec(iv));
            	}
            	int content1 = 8;
            	FileInputStream f = new FileInputStream(file);
            	while (content1 == 8) {
            		messaggio = f.readNBytes(content1);
            		content1 = messaggio.length;
            		mac.update(messaggio);	
            		cipher.update(messaggio);
            	}
            	byte[] macBytes2 = mac.doFinal();
            	byte[] cyphertext = cipher.doFinal();
            	i=0;
            	os.flush();
            	os.close();
            	
            	while (i < cyphertext.length) {
            		os.write(cyphertext[i]);
            		i++;
            	}
            	os.flush();
            	os.close();

            /*
            FileInputStream fisdebug = new FileInputStream(destinazione);
            byte[] readall = fisdebug.readAllBytes();
            System.out.println("Stampa codifica");
            for(byte b: readall){
                System.out.println(b);
            }
            fisdebug.close();


            kr.saveKeyring(new File(destinazione.getPath().substring(0, destinazione.getPath().length()-4) + "Keyring.txt"));
            return true;
*/}
        } catch (IOException e) {
            System.out.println("Errore: " + e);
            System.exit(1);
            return false;
        }
		return false;
  }

    public boolean decodifica(File file, File destinazione) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        kr = KeyRing.loadKeyring(new File(file.getPath().substring(0, file.getPath().length()-4) + "Keyring.txt"));

        FileInputStream fis = new FileInputStream(file);



        byte sl = (byte)'.';
        ArrayList<byte[]> params = new ArrayList<byte[]>();
        int num = 9;
        int i = 0;
        ArrayList<Byte> ba = new ArrayList<Byte>();
        while(i < num){
            byte b = (byte)fis.read();
            ba.add(b);
            if(b == sl){
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
        if(this.cifrario_m==(byte)0x00){
            System.out.println("here");
            this.iv=fis.readNBytes(16);
        } else {
            System.out.println("there");
            this.iv=fis.readNBytes(8);
        }
        //trova lunghezza rsa
        //FileInputStream fisKey = new FileInputStream(keyFile);

        //int rsaKeySize = (fisKey.read() +1)*128;
        //System.out.println("key size: " + rsaKeySize);
        //byte[] secretKey = fis.readNBytes(rsaKeySize);
        byte[] secretKey = kr.getKey("Secret");
        byte[] messaggio = fis.readAllBytes();
        fis.close();

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
        byte[] secretKeyDecodedByte = cipherkey.doFinal(secretKey);

        SecretKey secretKeyDecoded = new SecretKeySpec(secretKeyDecodedByte, 0, secretKeyDecodedByte.length, Match.cifrario_m.get(this.cifrario_m));

        //decodifica messaggio tramite chiave decodificata;
        Cipher cipher = Cipher.getInstance(Match.cifrario_m.get(this.cifrario_m) + "/" + Match.modi_operativi.get(this.modo_operativo) + "/PKCS5Padding");
        if(modo_operativo == (byte)0x00) {
            cipher.init(Cipher.DECRYPT_MODE, secretKeyDecoded);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKeyDecoded, new IvParameterSpec(iv));
        }
        this.messaggio = cipher.doFinal(messaggio);

        FileOutputStream fos = new FileOutputStream(destinazione);
        fos.write(this.messaggio);
        fos.flush();
        fos.close();



        return true;
    }
}

