package net.jmatrix.encryption;

import java.io.IOException;

import sun.misc.BASE64Decoder;

/** */
public class Password {
   String password=null;
   
   public Password() {}
   
   public Password(String s) {
      password=s;
   }
   
   public String toString() {
      if (password == null)
         return "null";
      return password.replaceAll(".", "*");
   }
   
   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
	  this.password = password;
   }
   
   public void setPasswordBase64(String s) throws IOException {
      password=new String((new BASE64Decoder()).decodeBuffer(s));
   }
}
