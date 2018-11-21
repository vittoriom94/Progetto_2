package progetto_2;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class CodificaHash extends Incapsula {
    private MessageDigest digest;
    private boolean signed;
    private boolean concat;
    public CodificaHash(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita, byte modi_operativi, byte tipo, byte dimensione_firma, File file, File destinazione, int kShare, int nShare) {
        super(mittente, destinatario, cifrario_m, cifrario_k, padding, integrita, modi_operativi, tipo, dimensione_firma, file, destinazione, kShare, nShare);
        signed = false;
        concat = false;
    }

    public CodificaHash(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita, byte modo_operativo, byte tipo, byte dimensione_firma, byte[] salt, byte[] iv) {
        super(mittente, destinatario, cifrario_m, cifrario_k, padding, integrita, modo_operativo, tipo, dimensione_firma,salt,iv);
        signed = true;
        concat = true;
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
        digest.update(c);
    }

    @Override
    protected void getVerifier() throws InvalidKeyException, NoSuchAlgorithmException {
        digest = MessageDigest.getInstance(Match.tipo.get(this.tipo));
        digest.update(salt);

    }

    @Override
    protected byte[] readVerifier(FileInputStream fis) throws IOException {
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
        return hash;
    }

    @Override
    protected void handleMessage(Cipher cipher, FileOutputStream os, int tot) throws IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        createVerifier(os);
        bufferReadEncode(os,cipher,new FileInputStream(file));
        byte[] verifier = completeVerifier();

        FileOutputStream temp = new FileOutputStream(Const.tempHash);
        temp.write(verifier);
        bufferReadEncode(temp,cipher,new FileInputStream(file));
        concat = true;

        Utils.printFile(Const.tempHash);
        temp.close();
        bufferReadEncode(os,cipher,new FileInputStream(Const.tempHash));
        os.write(cipher.doFinal());
        Const.tempHash.delete();


    }

    @Override
    protected byte[] completeVerifier() {
        signed = true;
        return digest.digest();
    }

    @Override
    protected void handleBuffer(byte[] buffer, int block, Cipher cipher, FileOutputStream os) throws IOException {
        if(!signed) {
            digest.update(buffer);
        } else if (!concat) {
            os.write(buffer);

        } else {
            os.write(cipher.update(buffer, 0, block));
        }

    }

    @Override
    protected void createVerifier(FileOutputStream os) throws IOException, NoSuchAlgorithmException {

        digest = MessageDigest.getInstance(Match.tipo.get(this.tipo));

        digest.update(salt);

    }

    @Override
    protected byte[] bufferReadDecode(FileInputStream fis, Cipher cipher) throws SignatureException, IOException, BadPaddingException, IllegalBlockSizeException{
        FileOutputStream fos = new FileOutputStream(destinazione);
        byte[] buffer = new byte[Const.BUFFER];
        int block = Const.BUFFER;



        while (block == Const.BUFFER) {
            block = fis.readNBytes(buffer, 0, block);
            if (block != Const.BUFFER && block != 0) {
                byte[] buffer2 = new byte[block];
                for (int p = 0; p < buffer2.length; p++)
                    buffer2[p] = buffer[p];
                updateVerifier(buffer2);
                fos.write(buffer2);
            } else {
                if(block != 0) {
                    updateVerifier(buffer);
                    fos.write(buffer);
                }
            }
        }




        //fos.write(buffer);
        //updateVerifier(buffer);
        byte[] verifier = retrieveVerifier();
        fos.flush();
        fos.close();
        fis.close();
        return verifier;
    }
}
