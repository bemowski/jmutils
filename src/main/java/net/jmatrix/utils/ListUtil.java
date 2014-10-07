package net.jmatrix.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility methods dealing with java.util.List.
 */
public class ListUtil {
   /** 
    * Converts a List<String> to a comma separated list.
    */
   public static final String listToString(List<String> l) {
      if (l == null)
         throw new NullPointerException("Input List is null in listToString");
      if (l.size() == 0) 
         return "";
      if (l.size() == 1)
         return l.get(0);
      
      StringBuilder sb=new StringBuilder();
      for (String s:l) {
         sb.append(s+",");
      }
      String s=sb.toString();
      return s.substring(0,s.length()-1); // strip the final ','
   }
   
   /** Converts a comma separated string into a List<String> */
   public static final List<String> stringToList(String s) {
      if (s == null) 
         throw new NullPointerException("Input String is null in stringToList");
      s=s.trim();
      List<String> l=new ArrayList<String>();
      if (s.length() == 0) {
         return l;
      }
      StringTokenizer st=new StringTokenizer(s, ",", false);
      while(st.hasMoreTokens()) {
         l.add(st.nextToken());
      }
      return l;
   }
   
   public static final int size(List<?> list)
   {
      return list == null ? 0 : list.size();
   }
   
   public static final boolean isEmpty(List<?> list)
   {
      return size(list) == 0;
   }

   public static <T> Iterable<T> nonNull(Iterable<T> iterable) 
   {
      return iterable == null ? Collections.<T>emptyList() : iterable;
   }
   
   public static <T> List<T> nonNull(List<T> list) 
   {
      return list == null ? Collections.<T>emptyList() : list;
   }
}
