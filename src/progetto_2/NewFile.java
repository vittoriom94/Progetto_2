import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.security.*;
import javax.crypto.*;


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
			byte[] salt, byte modi_operativi, byte[] iv, byte hash, byte mac, byte firma, byte[] messaggio) {
		super();
		this.mittente = mittente;
		this.destinatario = destinatario;
		this.cifrario_m = cifrario_m;
		this.cifrario_k = cifrario_k;
		this.padding = padding;
		this.integrita = integrita;
		this.salt = salt;
		this.modo_operativo = modi_operativi;
		this.iv = iv;
		this.hash = hash;
		this.mac = mac;
		this.firma = firma;
		this.messaggio = messaggio;
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
		      }
		     catch (IOException e) {
		      System.out.println("Errore: " + e);
		      System.exit(1);
		    }
		  } 
	
	//aggiungere key publickey nel metodo
	public boolean codifica(Key publickey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		  try {
		      FileOutputStream file = new FileOutputStream("file2.txt");
		      PrintStream Output = new PrintStream(file);
		      Output.print(mittente+"_");
		      Output.print(destinatario+"_");
		      Output.print(cifrario_m+"_");
		      Output.print(cifrario_k+"_");
		      Output.print(padding+"_");
		      Output.print(integrita+"_");
		      Output.print(salt[1]+"_");
		      Output.print(modo_operativo+"_");
		      Output.print(iv[1]+"_");
		      Output.print(hash+"_");
		      Output.print(mac+"_");
		      Output.print(firma+"_");
		      Output.println(dimensione_firma+"_");      
		//messaggio
		KeyGenerator keyGenerator = KeyGenerator.getInstance(Match.cifrario_m.get(this.cifrario_m));   
		SecretKey secretKey = keyGenerator.generateKey();
		Cipher cipher = Cipher.getInstance(Match.cifrario_m.get(this.cifrario_m)+"/"+Match.modi_operativi.get(this.modo_operativo)+"/PKCS5Padding");
		//Cipher cipherkey = Cipher.getInstance("RSA/"+Match.modi_operativi.get(this.modo_operativo)+"/"+Match.padding.get(this.padding));
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[ ] cyphertext = cipher.doFinal(this.messaggio);
		int i=0;
		while(i<cyphertext.length) {
			Output.print(cyphertext[i]);
			i++;
		}
		Output.println();
		//chiave
		Cipher cipherkey = Cipher.getInstance("RSA/"+Match.modi_operativi.get(this.modo_operativo)+"/"+Match.padding.get(this.padding)+"Padding");
		cipherkey.init(Cipher.ENCRYPT_MODE, publickey);
		byte byteKey[] = secretKey.getEncoded();
		byte[ ] cypherkey = cipherkey.doFinal(byteKey);
		while(i<cypherkey.length) {
			Output.print(cypherkey[i]);
			i++;
		}
			}	  
		     catch (IOException e) {
		      System.out.println("Errore: " + e);
		      System.exit(1);
		return true;
	}
		return false;
}
	public boolean decodifica() throws IOException {
		 FileInputStream sorgente = new FileInputStream("file2.txt");
		 byte singloByte;
		 byte[] v = new byte[1000] ;
		 int i=0;
	        while ((singloByte = (byte) sorgente.read()) != -1) {
	        	v[i]=singloByte;
	        	i++;
	        }
	        String n = new String(v);
            System.out.println(n);
	        sorgente.close();
		return false;
		/*
		FileReader f;
	    f=new FileReader("file2.txt");
	    BufferedReader b;
	    b=new BufferedReader(f);
	    String s;
	    s=b.readLine();
	    String[] ss = s.split("\\_");
	    this.mittente = ss[0];
		this.destinatario = ss[1];
		this.cifrario_m =(byte)Integer.parseInt(ss[2]);
		this.cifrario_k =(byte)Integer.parseInt(ss[3]) ;
		this.padding = (byte)Integer.parseInt(ss[4]);
		this.integrita = (byte)Integer.parseInt(ss[5]);
		this.salt = ss[5].getBytes();
		this.modo_operativo = (byte)Integer.parseInt(ss[6]);
		while()
		this.iv = ss[7].getBytes();
		this.hash = (byte)Integer.parseInt(ss[8]);
		this.mac = (byte)Integer.parseInt(ss[9]);
		this.firma = (byte)Integer.parseInt(ss[10]);
		return false;
		*/
	}
}

