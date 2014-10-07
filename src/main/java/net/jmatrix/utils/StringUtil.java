package net.jmatrix.utils;

/**
 * 
 */
public final class StringUtil {
   
   
   /**
    * Takes a string, returns a string that contains only digits.
    */
   public static final String digitsOnly(String s) {
      if (s == null)
         return null;
      StringBuilder sb=new StringBuilder();
      char chars[]=s.toCharArray();
      for (char c:chars)
         if (Character.isDigit(c))
            sb.append(c);
      
      return sb.toString();
   }
   
   /**
    * Takes a string, returns a string that contains only alphabetics.
    */
   public static final String alphaOnly(String s) {
      if (s == null)
         return null;
      StringBuilder sb=new StringBuilder();
      char chars[]=s.toCharArray();
      for (char c:chars)
         if (Character.isLetter(c))
            sb.append(c);
      
      return sb.toString();
   }
   
   /** 
    * allows alpha, digits, and [-_]
    */
   public static final String sqlIdOnly(String s) {
      if (s == null)
         return null;
      StringBuilder sb=new StringBuilder();
      char chars[]=s.toCharArray();
      for (char c:chars)
         if (Character.isLetter(c) || Character.isDigit(c) || 
             c == '-' || c == '_')
            sb.append(c);
      
      return sb.toString();
   }

   /**
    * Takes a string, returns a string that contains only alphabetics.
    */
   public static final String alphanumericOnly(String s) {
      if (s == null)
         return null;
      StringBuilder sb=new StringBuilder();
      char chars[]=s.toCharArray();
      for (char c:chars)
         if (Character.isLetter(c) || Character.isDigit(c))
            sb.append(c);
      
      return sb.toString();
   }
   
   /**
    * Takes a string, returns a string that contains only hex digits. Good for
    * filtering input for things like MAC addresses.
    */
   public static final String hexDigitsOnly(String s) {
      if (s == null)
         return null;
      StringBuilder sb=new StringBuilder();
      char chars[]=s.toCharArray();
      for (char c:chars)
         if (Character.isDigit(c) || (c >= 'a' && c <= 'f') ||  (c >= 'A' && c <= 'F'))
            sb.append(c);
      
      return sb.toString();
   }

   public static final boolean empty(String s)
   {
      return s==null || s.length()==0;
   }
   
   public static final boolean isNotBlank(String s)
   {
	   return !(empty(s));
   }
   
   public static final String substringAfter (String s, String delimeter)
   {
	   if (s == null)
		  return null;
	   
	   int index = s.indexOf(delimeter);
	   
	   return s.substring(index+1);
   }
   
   public static final boolean equals(String s1, String s2)
   {
      return s1==s2 || (s1 != null && s1.equals(s2));
   }
   
   public static final String nonNull(String s)
   {
      return (s==null) ? "" : s;
   }
   
   
   /** */
   public static final String pad(String padchar, int size) {
      StringBuffer sb=new StringBuffer();
      for (int i=0; i<size; i++)
         sb.append(padchar);
      return sb.toString();
   }
   
   /** */
   public static final String leftPad(String padchar, int size, 
                                      String text) {
      int pads=size-text.length();
      if (pads <= 0)
         return text;
      
      StringBuffer sb=new StringBuffer();
      for (int i=0; i<pads; i++)
         sb.append(padchar);
      sb.append(text);

      return sb.toString();
   }

   /** */
   public static final String rightPad(String padchar, int size, 
                                       String text) {
      int pads=size-text.length();
      if (pads <= 0)
         return text;
      
      StringBuffer sb=new StringBuffer();
      sb.append(text);
      for (int i=0; i<pads; i++)
         sb.append(padchar);

      return sb.toString();
   }
}
