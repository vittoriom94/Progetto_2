package progetto_2;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CodificaHash extends NewFile{
    private MessageDigest digest;
    public CodificaHash(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita, byte modi_operativi, byte tipo, byte dimensione_firma, File file, File destinazione, int kShare, int nShare) {
        super(mittente, destinatario, cifrario_m, cifrario_k, padding, integrita, modi_operativi, tipo, dimensione_firma, file, destinazione, kShare, nShare);
    }

    public CodificaHash(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita, byte modo_operativo, byte tipo, byte dimensione_firma, byte[] salt, byte[] iv) {
        super(mittente, destinatario, cifrario_m, cifrario_k, padding, integrita, modo_operativo, tipo, dimensione_firma,salt,iv);
    }

    @Override
    protected boolean verify(byte[] verifier, byte[] newVerifier) {
        return false;
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
    protected byte[] completeVerifier() {
        return digest.digest();
    }

    @Override
    protected void handleBuffer(byte[] buffer, int block, Cipher cipher, FileOutputStream os) throws IOException {
        digest.update(buffer);
        os.write(cipher.update(buffer, 0, block));
    }

    @Override
    protected void createVerifier(FileOutputStream os) throws IOException, NoSuchAlgorithmException {
        //controllare traccia
        int lunghezza=0;
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

        digest = MessageDigest.getInstance(Match.tipo.get(this.tipo));

        digest.update(salt);

    }
}
