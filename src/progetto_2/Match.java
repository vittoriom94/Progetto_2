package progetto_2;

import java.util.Map;

public class Match {
	public static Map<Byte,String> cifrario_m = Map.of(
			(byte)0x00 , "AES",
			(byte)0x01 , "DES",
			(byte)0x02 , "DESede") ; 
	public static Map<Byte,String> padding= Map.of(
			(byte)0x00 , "PKCS1",
			(byte)0x01 , "OAEP");
	public static Map<Byte,String> modi_operativi= Map.of(
			(byte)0x00 , "ECB",
			(byte)0x01 , "CBC",
			(byte)0x02 , "CFB") ; 
	public static Map<Byte,String> tipo= Map.ofEntries(
			Map.entry((byte)0x00, "SHA-1"),
			Map.entry((byte)0x01, "SHA-224"),
			Map.entry((byte)0x02, "SHA-256"),
			Map.entry((byte)0x03, "SHA-384"),
			Map.entry((byte)0x04, "SHA-512"),
			Map.entry((byte)0x05, "HmacMD5"),
			Map.entry((byte)0x06, "HmacSHA256"),
			Map.entry((byte)0x07, "HmacSHA384"),
			Map.entry((byte)0x08, "SHA1withDSA"),
			Map.entry((byte)0x09, "SHA224withDSA"),
			Map.entry((byte)0x10, "SHA256withDSA"));
	public static Map<Byte,Integer> dimensione= Map.of(
			(byte)0x00 , 1024,
			(byte)0x01 , 2048) ; 
}
