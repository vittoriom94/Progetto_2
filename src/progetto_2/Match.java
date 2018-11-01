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
	public static Map<Byte,String> hash= Map.of(
			(byte)0x00 , "SHA-1",
			(byte)0x01 , "SHA-224",
			(byte)0x02 , "SHA-256",
			(byte)0x03 , "SHA-384",
			(byte)0x04 , "SHA-512") ; 
	public static Map<Byte,String> mac= Map.of(
			(byte)0x00 , "HmacMD5",
			(byte)0x01 , "HmacSHA256",
			(byte)0x02 , "HmacSHA384") ; 
	public static Map<Byte,String> firma= Map.of(
			(byte)0x00 , "SHA1withDSA",
			(byte)0x01 , "SHA256withDSA",
			(byte)0x02 , "SHA256withDSA");
	public static Map<Byte,Integer> dimensione= Map.of(
			(byte)0x00 , 1024,
			(byte)0x01 , 2048) ; 
}
