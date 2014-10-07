package net.jmatrix.encryption;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A JSON serializable encryption key.  
 * 
 * This class also includes meta data about the key itself.
 * 
 * The key can be stored as a file as follows: 
 *    name.version.json
 *  or
 *    name.json (if version is null).
 *    
 * For some algorithms - this may not be enough information. So this class
 * can be subclassed to support more data.  Or - additional nullable 
 * fields could be added.
 */
public class EncryptionKey {
   static final DateFormat df=new SimpleDateFormat("dd.MM.yyyy HH:mm");
   
   /** Date that the key started to be used.  Can be estimated. */
   String dateCommissioned=null;
   
   /** date the key ceased to be used.  */
   String dateDecommissioned=null;
   
   /** The key name is simple name for a key. */
   String name=null;
   
   /** Verison is optional, but can be helpful in rekeying. */
   int version=-1;
   
   /**
    * The encryption algorithm, suitable for looking up a Cipher
    * with Cipher.getInstance(algorithm)
    */
   String algorithm=null;
   
   /** This is a base 64 encoded version of the key.  The key may well
    * be a string itself - but it is still base64 encoded from its byte
    * array implementation.  
    * 
    * The key is stored in base64 encoded format.  But can be read as a 
    * decoded string or byte array via utility methods.
    */
   String key64=null;
   
   String keyHex=null;
   
   /**
    * A free form text field with notes about this key, potentially 
    * what it is used for, etc.
    */
   String notes=null;
   
   enum Format {HEX, BASE64}
   
   Format keyFormat=Format.BASE64;
   
   public EncryptionKey() {}
   
   @JsonIgnore
   public void setKeyString(String s) {
      setKey64(new BASE64Encoder().encode(s.getBytes()));
   }
   
   @JsonIgnore
   public String getKeyString() throws IOException {
      return new String(getKeyBytes());
   }
   
   @JsonIgnore
   public byte[] getKeyBytes() throws IOException {
      if (keyFormat == Format.BASE64)
         return new BASE64Decoder().decodeBuffer(key64);
      else if (keyFormat == Format.HEX)
         return hexStringToByteArray(keyHex);
      else
         throw new RuntimeException("Unknown key format '"+keyFormat+"'");
   }
   
   public String toString() {
      
      String keySize=null;
      try {
         byte key[]=getKeyBytes();
         
         keySize=key.length+"bytes, "+(key.length*8)+" bits";
      } catch (Exception ex) {
         keySize="err: "+ex;
      }
      return "EncryptionKey("+name+"-"+version+", algo="+algorithm+
            ", format="+keyFormat+", keySize="+keySize+
            ", commissioned "+dateCommissioned+")";
   }
   
   
   @JsonIgnore
   public String getFilename() {
      return name+(version>=0?"-"+version:"")+".json";
   }
   
   /** This is only here to */
   public static final EncryptionKey load(String filepath) throws Exception {
      ObjectMapper om=new ObjectMapper();
      EncryptionKey key=om.readValue(new File(filepath), EncryptionKey.class);
      
      return key;
   }
   
   static byte[] hexStringToByteArray(String s) {
      int len = s.length();
      byte[] data = new byte[len / 2];
      for (int i = 0; i < len; i += 2) {
          data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                               + Character.digit(s.charAt(i+1), 16));
      }
      return data;
   }
   
   @JsonIgnore
   public String getFullName() {
      return name+"-"+version;
   }
   
   @JsonIgnore
   public String getFileName() {
      return getFullName()+".json";
   }
   
   ////////////////////////////////////////////////////////////////////////
   public String getDateCommissioned() {
      return dateCommissioned;
   }

   public void setDateCommissioned(String dateCommissioned) {
      this.dateCommissioned = dateCommissioned;
   }

   public String getDateDecommissioned() {
      return dateDecommissioned;
   }

   public void setDateDecommissioned(String dateDecommissioned) {
      this.dateDecommissioned = dateDecommissioned;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getVersion() {
      return version;
   }

   public void setVersion(int version) {
      this.version = version;
   }

   public String getAlgorithm() {
      return algorithm;
   }

   public void setAlgorithm(String algorithm) {
      this.algorithm = algorithm;
   }

   public String getKey64() {
      return key64;
   }

   public void setKey64(String key64) {
      this.key64 = key64;
   }

   public String getNotes() {
      return notes;
   }

   public void setNotes(String notes) {
      this.notes = notes;
   }

   public String getKeyHex() {
      return keyHex;
   }

   public void setKeyHex(String keyHex) {
      this.keyHex = keyHex;
   }

   public Format getKeyFormat() {
      return keyFormat;
   }

   public void setKeyFormat(Format keyFormat) {
      this.keyFormat = keyFormat;
   }
}
