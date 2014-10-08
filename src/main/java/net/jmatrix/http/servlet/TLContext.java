package net.jmatrix.http.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jmatrix.utils.ClassLogFactory;

import org.slf4j.Logger;


/**
 * The TLContext is a family of threadlocal data that is used in the 
 * context of executing an HTTP servlet.
 * 
 * From this class, an executing service call can get the following data
 * via static methods (that are current to the executing thread context):
 *   > The HttpServletRequest and HttpServletResponse of the executing method.
 *   
 * Calling methods should deal with any possible null returns from these public 
 * method calls.  
 * 
 * Individual transport layers - like REST or BlazeDS - may have analogous
 * transport layer contexts - but each is specific to the transport layer, and
 * the services need to be transport agnostic.  So rather than going to 
 * any transport specific context like FlexContext or JAX-RS's @Context annotations 
 * services can rely on this Context and related Filters that set these
 * values in any transport execution context.
 * 
 * As of Feb 9, this context system replaces the formed  SessionUser
 * threadlocal. 
 * 
 * @author Paul Bemowski
 */
public final class TLContext {
   static Logger log=ClassLogFactory.getLog();

   // Implementation uses a single threadloacal Map<String, Object> with 
   // private, internally defined keys.  
   
   //private static final String LOG_CONTEXT="log.context";
   private static final String HTTP_REQUEST="http.request";
   private static final String HTTP_RESPONSE="http.response";
   
   public static final String CONTEXT_DATA="context.data";
   private static class ThreadLocalContext extends ThreadLocal<Map<String, Object>> {
      public Map<String, Object> initialValue() {
         return new HashMap<String, Object>();
      }
   }
   
   private static ThreadLocalContext threadContext=new ThreadLocalContext();
   
   //////////////////////  PUBLIC STATIC METHODS  ///////////////////////////
   
   // General methods
   
   /**
    * <code>getThreadContextMap</code> used to get a copy of the current thread's
    * TLContext's map of data. Its primary use should be in the copying of one
    * thread's TLContext to that of a child thread. 
    * 
    * If you're not sure what you're doing, don't call this method.
    *
    * @return a HashMap<String, Object> that contains this thread's thread local data.
    * The Map returned is a copy of the thread local instance of the Map with the same
    * contents <em>at the time the copy was made</em>. This is necessary since the
    * parent thread may terminate and get reused (getting new TLContext data) before
    * the child thread can use the data.
    */
   public static Map<String, Object> getThreadContextMap()
   {
      return new HashMap<String, Object>(threadContext.get());
   }

   /**
    * <code>setThreadContextMap</code> used to initialize the thread local data for
    * the current thread from an external source. It is primarily used to initialize
    * the TLContext data for a new child thread from its parent. 
    * 
    * If you're not sure what you're doing, don't call this method.
    *
    * @return a HashMap<String, Object> that contains this thread's thread local data
    */
   public static void setThreadContextMap(Map<String, Object> map)
   {
      threadContext.set(map);
   }
   
   /** Clears all threadlocal context.  Should only be called from the 
    * finally block at a boundary layer ServletFilter. */
   public static void clear() {
      Map<String, Object> context=threadContext.get();
      context.clear();
   }
   
   public static boolean empty() {
      return threadContext.get().size()==0;
   }
   
   public static void put(String key, Object value) {
      threadContext.get().put(key, value);
   }

   public static Object get(String key) {
      return threadContext.get().get(key);
   }

   
   
   // Http Stuff /////////////////////////////////////////////////////////////
   /** */
   public static HttpServletRequest getServletRequest() {
      return (HttpServletRequest)threadContext.get().get(HTTP_REQUEST);
   }
   
   /** */
   public static void setServletRequest(HttpServletRequest req) {
      threadContext.get().put(HTTP_REQUEST, req);
   }
   
   /** */
   public static HttpServletResponse getServletResponse() {
      return (HttpServletResponse) threadContext.get().get(HTTP_RESPONSE);
   }
   
   /** */
   public static void setServletResponse(HttpServletResponse res) {
      threadContext.get().put(HTTP_RESPONSE, res);
   }
   
   // could add UserAgent and or URI - may be interesting for logging.
   // both are available from the servlet request so they would just be
   // contextual convenience methods.
   ////////////////////////////////////////////////////////////////////
   
   /**
    * ContextData is a map that is persisted during a user's logged
    * in state.  it will survive as long as UserPrinciapl - in a 
    * transport agnostic manner.  Though the data itself will likely 
    * be stored in Http based session.
    * 
    * Service layer consumers of getContextData essentially allow for
    * statefull method-to-method storage of data.
    * 
    * This method can return null, if the context data was not 
    * earlier initialized.  (user didnt login?)
    */
   public static Map<String, Object> getContextData() {
      return (Map)threadContext.get().get(CONTEXT_DATA);
   }
   
   /**
    * This method is used by a transport specifc filter to inject
    * stored map into this context.
    */
   public static void setContextData(Map<String, Object> data) {
      threadContext.get().put(CONTEXT_DATA, data);
   }
}
