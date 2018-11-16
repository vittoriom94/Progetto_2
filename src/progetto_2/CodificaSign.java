package progetto_2;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class CodificaSign extends NewFile{
    private Signature signature;
    private boolean signed ;
    public CodificaSign(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita, byte modi_operativi, byte tipo, byte dimensione_firma, File file, File destinazione, int kShare, int nShare) {
        super(mittente, destinatario, cifrario_m, cifrario_k, padding, integrita, modi_operativi, tipo, dimensione_firma, file, destinazione, kShare, nShare);
        signed = false;
    }

    public CodificaSign(String mittente, String destinatario, byte cifrario_m, byte cifrario_k, byte padding, byte integrita, byte modo_operativo, byte tipo, byte dimensione_firma, byte[] salt, byte[] iv) {
        super(mittente, destinatario, cifrario_m, cifrario_k, padding, integrita, modo_operativo, tipo, dimensione_firma,salt,iv);
    }

    @Override
    protected boolean verify(byte[] verifier, byte[] newVerifier) throws SignatureException {
    	boolean result = signature.verify(verifier);   	
        return result;
    }

    @Override
    protected byte[] retrieveVerifier() {
        return null;
    }

    @Override
    protected void updateVerifier(byte[] c) throws SignatureException {
        signature.update(c);
    }

    @Override
    protected void getVerifier() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException {
        X509EncodedKeySpec ks2 = new X509EncodedKeySpec(kr.getKey("DSAPublic"));
        KeyFactory kf2 = KeyFactory.getInstance("DSA");
        PublicKey publicKey = kf2.generatePublic(ks2);
        signature = Signature.getInstance(Match.tipo.get(this.tipo));
        signature.initVerify(publicKey);
    }

    @Override
    protected byte[] readVerifier(FileInputStream fis) throws IOException {


        byte[] temp = fis.readNBytes(2);
        byte[] digitalSignature = new byte[(int) temp[1] + 2];

        for (int p = 0; p < digitalSignature.length; p++) {
            if (p < 2)
                digitalSignature[p] = temp[p];
            else
                digitalSignature[p] = (byte) fis.read(); 	
        }
        return digitalSignature;
    }

    @Override
    protected byte[] completeVerifier() throws SignatureException {
        signed = true;
        return signature.sign();
    }

    @Override
    protected void handleBuffer(byte[] buffer, int block, Cipher cipher, FileOutputStream os) throws IOException, SignatureException {
        if(signed)
            os.write(cipher.update(buffer, 0, block));
        else {
            updateVerifier(buffer);

        }

    }

    @Override
    protected void createVerifier(FileOutputStream os) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException {

            signature = Signature.getInstance(Match.tipo.get(tipo));


            PKCS8EncodedKeySpec ks2 = new PKCS8EncodedKeySpec(kr.getKey("DSAPrivate"));
            KeyFactory kf2 = KeyFactory.getInstance("DSA");
            PrivateKey privateKey = kf2.generatePrivate(ks2);

            signature.initSign(privateKey);

    }


    @Override
    protected void handleMessage(Cipher cipher, FileOutputStream os, int tot) throws IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        createVerifier(os);
        bufferReadEncode(os, cipher);
        byte[] verifier = completeVerifier();
        os.write(verifier);
        bufferReadEncode(os, cipher);
        os.write(cipher.doFinal());

    }
}
