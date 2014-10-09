package net.jmatrix.async;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import net.jmatrix.context.LogContext;
import net.jmatrix.http.servlet.TLContext;
import net.jmatrix.utils.ClassLogFactory;
import net.jmatrix.utils.PerfTrack;

import org.slf4j.Logger;

//Casting will be done elsewhere by consumer
public class AsyncCallable<V> implements Callable<V> {
	static final Logger log = ClassLogFactory.getLog();

	Callable<V> delegate = null;
	
	Notifier notifier;
	
	Map<String, Object> logContext = null;
	Map<String, Object> tlContextMap = null;

	String name = null;

	public AsyncCallable(Callable<V> c) {
	   delegate = c;
	   // Clone the return value because the map returned by this method will be
	   // used in another thread to initialize the logging context and if the
	   // thread calling this method terminates before the values in the map
	   // are accessed then the map will have been cleared and the second thread 
	   // will not be able to access the values.
	   logContext = new HashMap<String, Object>(LogContext.getAll());
	   // getThreadContextMap() creates a copy of the TLContext map so we
	   // do not have to clone it here.
	   tlContextMap = TLContext.getThreadContextMap();
	   name = c.toString();
	}

   @Override
   public V call() throws Exception
   {
      String perf = name;
      V returnValue = null;

      try
      {
         Thread thread = Thread.currentThread();
         if (thread instanceof NotifyingThread)
         {
            // If the current thread is an instance of NotifyingThread, 
            // provide the current thread with a reference to this AsyncCallable's
            // Future object so that the NotifyingExecutor in AsyncExecutorImpl
            // is able to notify the Future object when this AsyncCallable has
            // completed.
            log.debug("Setting notifier "+getNotifier()+" on thread "+thread);
            ((NotifyingThread)thread).setNotifier(getNotifier());
         }
         LogContext.putAll(logContext);
         TLContext.setThreadContextMap(tlContextMap);
         PerfTrack.start(perf);
         log.debug("Calling: "+name);
         returnValue = delegate.call();
      } 
      catch (InterruptedException e)
      {
         log.error("",e);
         throw e;
      }
      catch (ExecutionException e)
      {
         log.error("",e);
         throw e;
      }
      catch (RuntimeException e)
      {
         log.error("",e);
         throw e;
      }
      catch (Error e)
      {
         log.error("",e);
         throw e;
      }
      finally
      {         
         PerfTrack.stop(perf);

         if (PerfTrack.isCurrentRootAndComplete())
         {
            log.debug("PerfTrack:\n" + PerfTrack.toString(0));
            // SystemLogService syslog=getSyslog();
            // if (syslog != null)
            // syslog.log(evt);
            // PerfTrack.clear();
         }
         else
         {
            log.warn("Async runnable ending with asymmetric PerfTrack.");
            log.debug("PerfTrack:\n" + PerfTrack.toString(0));
         }
         PerfTrack.clear();

         TLContext.clear();
         LogContext.clear();

      }
      log.debug("returning " + returnValue);
      return returnValue;
   }
   
   public synchronized Notifier getNotifier() throws InterruptedException
   {
      if (notifier == null)
      {
         // We must wait on the thread that submitted this AsyncCallable to
         // register the Future (notifier) object associated with this AsyncCallable 
         // because this getter is called by the async thread running
         // this AsyncCallable whereas the setNotifier is called by the main thread
         // that submitted this AsyncCallable and the async thread can begin execution 
         // before the submitting thread has a chance to register the notifier. 
         wait(60000);
      }
      if (notifier == null)
      {
         throw new InterruptedException("AsyncCallable's notifier is still null after waiting 60 seconds.");
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
