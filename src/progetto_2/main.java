import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class main {

	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		
	byte c = 0x00;
	byte []v = new byte[5];
	v[1]= 0x01;
	KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(1024);
    KeyPair k = gen.generateKeyPair();
    Key publickey = k.getPublic();
	NewFile newfile = new NewFile("mittente","destinatario", c, c, c,c,
				v, c, v, c, c,c, v);
	//newfile.codifica(publickey);
	newfile.decodifica();

	}
}

