package net.jmatrix.async;

import java.util.HashMap;
import java.util.Map;

import net.jmatrix.http.servlet.TLContext;
import net.jmatrix.syslog.LogContext;
import net.jmatrix.utils.ClassLogFactory;
import net.jmatrix.utils.PerfTrack;

import org.slf4j.Logger;

/**
 * Sets the ThreadLocal variables from the constructing thread's 
 * context locally - then, sets those variables on the thread context
 * before running the enclosed Runnable.
 */
@SuppressWarnings("rawtypes")
public class AsyncRunnable implements Runnable {
   static final Logger log=ClassLogFactory.getLog();

   Runnable delegate=null;
   
   Notifier notifier;

   Map<String, Object> logContext = null;
   Map<String, Object> tlContextMap = null;

   
   String name=null;
   
   public AsyncRunnable(Runnable r) {
      // Clone the return value because the map returned by this method will be
      // used in another thread to initialize the logging context and if the
      // thread calling this method terminates before the values in the map
      // are accessed then the map will have been cleared and the second thread 
      // will not be able to access the values.
      logContext = new HashMap<String, Object>(LogContext.getAll());
      // getThreadContextMap() creates a copy of the TLContext map so we
      // do not have to clone it here.
      tlContextMap = TLContext.getThreadContextMap();
      name=r.toString(); 
      delegate=r;
   }
   
   
   /** */
   @Override
   public void run() {
      String perf=name;
      
      try {
         Thread thread = Thread.currentThread();
         if (thread instanceof NotifyingThread)
         {
            // Provide the current thread with a reference to this AsyncRunable's
            // Future object so that the NotifyingExecutor in AsyncExecutorImpl
            // is able to notify the Future object when this AsyncRunable has
            // completed.
            log.debug("Setting notifier "+this+" on thread "+thread);
            ((NotifyingThread)thread).setNotifier(getNotifier());
         }
         LogContext.putAll(logContext);
         TLContext.setThreadContextMap(tlContextMap);
         PerfTrack.start(perf);
         delegate.run();
      }
      catch (InterruptedException e)
      {
         log.error("interrupted: ", e);
         // Can't throw checked exceptions from run method
      }
      catch (RuntimeException e)
      {
         log.error("Async Runtime Exception: ", e);
         throw e;
      }
      catch (Error e)
      {
         log.error("Async Error: ", e);
         throw e;
      } finally {
         PerfTrack.stop(perf);
         
         if (PerfTrack.isCurrentRootAndComplete()) {
            log.debug("PerfTrack:\n"+PerfTrack.toString(0));
//               SystemLogService syslog=getSyslog();
//               if (syslog != null)
//                  syslog.log(evt);
            //PerfTrack.clear();
         } else {
            log.warn("Async runnable ending with asymmetric PerfTrack.");
            log.debug("PerfTrack:\n"+PerfTrack.toString(0));
         }
         PerfTrack.clear();
         
         TLContext.clear();
         LogContext.clear();
      }
   }

   public synchronized Notifier getNotifier() throws InterruptedException
   {
      if (notifier == null)
      {
         // We must wait on the thread that submitted this AsyncRunnable to
         // register the Future (notifier) object associated with this AsyncRunnable 
         // because this getter is called by the async thread running
         // this AsyncRunnable whereas the setNotifier is called by the main thread
         // that submitted this AsyncRunnable and the async thread can begin execution 
         // before the submitting thread has a chance to register the notifier. 
         wait(60000);
      }
      if (notifier == null)
      {
         throw new InterruptedException("AsyncRunnable's notifier is still null after waiting 60 seconds.");
      }
      return notifier;
   }

   public synchronized void setNotifier(Notifier notifier)
   {
      this.notifier = notifier;
      // Notify this object that the Future object (notifier) is available for use
      notify();
   }


   public String toString()
   {
      String s = super.toString();
      return s.substring(s.lastIndexOf('.')+1)+"("+delegate+")";
   }
}
