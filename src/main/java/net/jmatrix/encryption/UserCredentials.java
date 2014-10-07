package net.jmatrix.encryption;

import java.util.Map;

/**
 * This a JSON serializable username/password pair. 
 * 
 * This class (optionally) uses custom serialization to suppored obfuscation
 * of passwords via Base64.  Below are examples of both:
 * 
 * 
 *    "username":"someuser",
 *    "password":{
 *       "password":"somepass"
 *    }
 * 
 * Or: 
 * 
 *    "username":"someuser",
 *    "password":{
 *       "passwordBase64":"43lajvaewa32akdajlo=="
 *    }
 *     
 * Additionally, this class supports an ad-hoc key/value String/String map, 
 * which may provide additional context and/or comments.
 */
public class UserCredentials {
   String username=null;
   Password password=null;
   
   Map<String, String> context=null;
   
   public UserCredentials() {}

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public Password getPassword() {
      return password;
   }

   public void setPassword(Password password) {
      this.password = password;
   }

   public Map<String, String> getContext() {
      return context;
   }

   public void setContext(Map<String, String> context) {
      this.context = context;
   }
}
