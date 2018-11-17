package progetto_2;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.sasl.AuthenticationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CodificaMAC  extends NewFile{
    private Mac mac;
    public CodificaMAC(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita, byte modi_operativi, byte tipo, byte dimensione_firma, File file, File destinazione, int kShare, int nShare) {
        super(mittente, destinatario, cifrario_m, cifrario_k, padding, integrita, modi_operativi, tipo, dimensione_firma, file, destinazione, kShare, nShare);
    }
    public CodificaMAC(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita, byte modo_operativo, byte tipo, byte dimensione_firma, byte[] salt, byte[] iv) {
        super(mittente, destinatario, cifrario_m, cifrario_k, padding, integrita, modo_operativo, tipo, dimensione_firma,salt,iv);
    }
    @Override
    protected boolean verify(byte[] verifier, byte[] newVerifier) {
    	boolean result = Arrays.equals(verifier, newVerifier); 	
        return result;
    }

    @Override
    protected byte[] retrieveVerifier() {
        return completeVerifier();
    }

    @Override
    protected void updateVerifier(byte[] c) {
        mac.update(c);
    }

    @Override
    protected void getVerifier() throws InvalidKeyException, NoSuchAlgorithmException, AuthenticationException {
        mac = Mac.getInstance(Match.tipo.get(this.tipo));
        SecretKeySpec macKey = new SecretKeySpec(kr.getKey("MAC"), mac.getAlgorithm());
        mac.init(macKey);
    }

    @Override
    protected byte[] readVerifier(FileInputStream fis) throws IOException {
        byte[] mac = null;
        if (Match.tipo.get(this.tipo) == "HmacMD5")
            mac = fis.readNBytes(16);
        if (Match.tipo.get(this.tipo) == "HmacSHA256")
            mac = fis.readNBytes(32);
        if (Match.tipo.get(this.tipo) == "HmacSHA384")
            mac = fis.readNBytes(48);
        return mac;
    }

    @Override
    protected byte[] completeVerifier() {
        return mac.doFinal();
    }

    @Override
    protected void handleBuffer(byte[] buffer, int block, Cipher cipher, FileOutputStream os) throws IOException {
        mac.update(buffer);
        os.write(cipher.update(buffer, 0, block));
    }

    @Override
    protected void createVerifier(FileOutputStream os) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        int lunghezza = 0;
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
            mac = Mac.getInstance(Match.tipo.get(this.tipo));
            mac.init(macKey);


    }
}
