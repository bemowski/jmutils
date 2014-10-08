package net.jmatrix.utils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

/**
 * Used to manipulate query strings.
 */
public final class QueryUtil
{
   static Logger log=ClassLogFactory.getLog();
   
   List<Pair> pairs=new ArrayList<Pair>();
   /** */
   public QueryUtil(String queryString) {
      if (queryString == null ||
          queryString.length() ==0)
         return;

      if (queryString.startsWith("?")) {
         queryString=queryString.substring(1);
      }

      StringTokenizer st=new StringTokenizer(queryString, "&", false);
      while (st.hasMoreTokens()) {
         String ps=st.nextToken();
         int indexOfEquals=ps.indexOf("=");
         if (indexOfEquals == -1) {
            // invalid key value pair in query string.
            System.out.println ("Invalid query String: pair '"+ps+"'");
         } else {
            Pair p=new Pair(ps.substring(0, indexOfEquals),
                            ps.substring(indexOfEquals+1));
            pairs.add(p);
         }
      }
   }
   
   public List<Pair> getParameters() {
      return pairs;
   }
   
   public Map<String, String> getParametersMap() {
      Map<String, String> map=new HashMap<String, String>();
      
      for (Pair p:pairs)
         map.put(p.getKey(), p.getVal());
      return map;
   }

   /** */
   public String strip(String key) {
      StringBuffer query=new StringBuffer();
      boolean first=false;
      for (int i=0; i<pairs.size(); i++) {
         Pair p=pairs.get(i);
         if (!p.getKey().equals(key)) {
            if (first) {
               query.append(p.toString());
               first=false;
            }
            else
               query.append("&"+p.toString());
         }
      }
      log.debug("Returning query stripped of '"+key+"': "+query);
      return query.toString();
   }

   /** */
   public String get(String key) {
      if (key == null)
         return null;

      for (int i=0; i<pairs.size(); i++) {
         Pair p=pairs.get(i);
         if (p.getKey().equals(key))
            return p.getVal();
      }
      return null;
   }

   /** */
   private class Pair
   {
      String key=null;
      String val=null;

      public Pair(String key, String val) {
         this.key=key;
         this.val=val;
      }
      
      public String getKey() {return key;}
      public String getVal() {return val;}
      public String toString() {return key+"="+val;}
   }

   public static String requestToHiddenInputs(HttpServletRequest request) {
      Enumeration names=request.getParameterNames();
      StringBuffer sb=new StringBuffer();
      while (names.hasMoreElements()) {
         String key=(String)names.nextElement();
         String value=request.getParameter(key);
         
         sb.append("<input type=\"hidden\" name=\""+key+"\" value=\""+
                   URLEncoder.encode(value)+"\">\n");
      }
      return sb.toString();
   }
}


