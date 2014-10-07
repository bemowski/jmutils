package net.jmatrix.encryption;

/**
 * Supports symmetric private key encryption of strings. This class is a simple
 * interface to allow the service to return encryption implementations based on
 * simple key names.
 **/
public interface Cryptor {
   public String encryptAndEncode(String clearText) throws EncryptionException;

   public String decodeAndDecrypt(String base64CipherText) throws EncryptionException;
   
   public EncryptionKey getKey();
}
