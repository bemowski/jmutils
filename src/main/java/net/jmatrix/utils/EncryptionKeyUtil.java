package net.jmatrix.utils;

import net.jmatrix.encryption.EncryptionKey;
import sun.misc.BASE64Encoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


/**
 * Small command line utility for creating and manipulating encryption 
 * key files.
 */
public class EncryptionKeyUtil {
   
   static final String usage=
         "EncryptionUtil command [options]\n"+
         "   template\n"+
         "   encode [string]\n";
   
   
   /** */
   public static void main(String[] args) throws Exception {
      
      if (args.length < 1) {
         System.out.println (usage);
         System.exit(1);
      }
      
      String command=args[0];
      
      
      
      ObjectMapper om=new ObjectMapper();
      om.configure(SerializationFeature.INDENT_OUTPUT, true);
      
      if (command.equals("template")) {
         // Creates an empty key template
         
         EncryptionKey key=new EncryptionKey();
         
         String s=om.writeValueAsString(key);
         
         System.out.println(s);
         
      } else if (command.equals("encode")) {
         System.out.println(new BASE64Encoder().encode(args[1].getBytes()));
      } else {
         System.out.println ("unknown command '"+command+"'");
         System.out.println (usage);
      }

   }

}
