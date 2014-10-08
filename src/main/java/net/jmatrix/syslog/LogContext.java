package net.jmatrix.syslog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.jmatrix.utils.ClassLogFactory;

import org.slf4j.Logger;
import org.slf4j.MDC;

/** 
 * The LogContext stores threadlocal contextual information, very similar to
 * the SLF4J's MDC - mapped diagnostic context.  
 * 
 * In fact, many variables here will be pushed to the MDC.
 * 
 * However, we are trying to avoid direct, pervasive compile-time dependence
 * on MDC. This abstraction facilitates that move.
 * 
 * Looking at this class... I'm keeping MDC - a HashMap in lock-step with
 * a local HashMap as long as the values are Strings. This is a restriction 
 * on SLF4J's MDC.  Why not just use MDC as the datastore?  A fair question.
 * I guess for some variables may not be relevant to logging... I don't know.
 * Could probably change it in the future.  Its fully encapsulated here - so 
 * a change would not matter.  Not much harm in storing it twice at the moment.
 * 
 * */
public class LogContext {
   static Logger log=ClassLogFactory.getLog();
   
   public static final String TRANSPORT="TRANSPORT";
   public static final String CLIENT_IP="CLIENT_IP";
   
   public static final String SESSION_ID="SESSION_ID";
   public static final String REQUEST_ID="REQUEST_ID";
   
   public static final String LOGIN_ID="LOGIN_ID";
   public static final String ACCOUNT_NUMBER="ACCOUNT_NUMBER";
   
   private static class ThreadLocalContext extends ThreadLocal<Map<String, Object>> {
      public Map<String, Object> initialValue() {
         return new HashMap<String, Object>();
      }
   }
   
   private static ThreadLocalContext context=new ThreadLocalContext();
   
   /** 
    * Puts a value into the LogContext. (very often called by ServletFilters.
    * 
    * Calls to put() with a null value cause an existing value to be removed.
    */
   public static void put(String name, String value) {
      if (value == null) {
         remove(name);
      } else {
         Map<String, Object> cmap=context.get();
         cmap.put(name, value);
         MDC.put(name, value);
      }
   }
   
   /**
    * Retrieves a value from the log context.
    */
   public static Object get(String name) {
      Map<String, Object> cmap=context.get();
      return cmap.get(name);
   }
   
   /** 
    * Removes a value from the LogContext.
    */
   public static Object remove(String name) {
      Map<String, Object> cmap=context.get();
      
      MDC.remove(name);
      return cmap.remove(name);
   }
   
   public static boolean empty() {
      if (context.get().size() == 0)
         return true;
      return false;
   }
   
   /** 
    * Returns all values in the current context.  Helpful for 
    * thread-context-cloning methods.
    */
   public static Map<String, Object> getAll() {
      return context.get();
   }
   
   /**
    * Sets all values on the current context.  Helpful for 
    * thread-context-cloning methods.
    */
   public static void putAll(Map<String, Object> newcontext) {
      Set<String> keyset=newcontext.keySet();
      for (String key:keyset) {
         Object value = newcontext.get(key);
         context.get().put(key, value);
         if (value instanceof String) {
            MDC.put(key, (String)value);
         }
      }
   }
   
   /** 
    * Removes all values from the context.
    */
   public static void clear() {
      Map<String, Object> cmap=context.get();
      
      // have to copy the keys to another object to prevent 
      // ConcurrentModificationException
      Set<String> keys=new HashSet<String>();
      keys.addAll(cmap.keySet());
      
      for (String key:keys) {
         cmap.remove(key);
         MDC.remove(key);
      }
   }
   
   // this was a bad idea - i think.  I'm removing it - but leaving the code
   // in case I was smarter when I wrote it than I am now when I'm removing 
   // it.  I think this is successfully replaced by the NoLog annotation.
   // 4/28/2011, bemo.
   //public static final String SYSLOG_IGNORE="SYSLOG_IGNORE";
   
//   public static boolean ignore() {
//      Boolean ignore=(Boolean)context.get().get(SYSLOG_IGNORE);
//      if (ignore != null && ignore)
//         return true;
//      return false;
//   }
//   
//   public static void setIgnore(boolean b) {
//      context.get().put(SYSLOG_IGNORE, new Boolean(b));
//   }
}
