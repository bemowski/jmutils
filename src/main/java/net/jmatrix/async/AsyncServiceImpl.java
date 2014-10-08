package net.jmatrix.async;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.jmatrix.exception.JMException;
import net.jmatrix.exception.JMException.ErrorCode;
import net.jmatrix.jproperties.JProperties;
import net.jmatrix.utils.ClassLogFactory;

import org.slf4j.Logger;


/**
 * <code>AsyncExecutorImpl</code> Implements the AsyncService interface that
 * schedules tasks (Runnables or Callables) for asynchronous execution and
 * provides methods that allow callers to wait on completion of scheduled tasks.
 * AsyncExecutorImpl delegates the running of tasks in separate threads to a
 * {@link NotifyingExecutor} which implements the {@link ExecutorService}
 * interface.
 * 
 */
public class AsyncServiceImpl implements AsyncService, ThreadFactory 
{
   private static final Logger log=ClassLogFactory.getLog();
   
   private static JProperties props;

   private static AsyncService instance;

   BlockingQueue<Runnable> queue=null;
   
   // Subclass of ThreadPoolExecutor that allocates tasks to threads 
   NotifyingExecutor executor=null;
   
   // Variable used to name new threads
   static volatile int threadid=0;
  
   public static synchronized AsyncService init(JProperties props) {
      if (instance == null) {
          instance = new AsyncServiceImpl(props);
      }
      return instance;
   }
   
   public static AsyncService getInstance() throws JMException {
      if (instance == null) {
          if (props == null) {
             String m = "The init method must be called to initialize this class with a JProperties object";
             throw new JMException(ErrorCode.NOT_INITIIALIZED, m);
          }
          instance = new AsyncServiceImpl(props);
      }
      return instance;
   }
   
   protected AsyncServiceImpl(JProperties props) {
      log.debug("Creating AsyncExecutorImpl");
      
      // Get configuration properties for the NotifyingExecutor
      int corePoolSize    = props.getInt("corePoolSize",10);
      int maximumPoolSize = props.getInt("maximumPoolSize",64);
      long keepAliveTime  = props.getInt("keepAliveTime",60);
      int workQueueSize   = props.getInt("workQueueSize",96);

      if (workQueueSize == 0) {
         queue=new SynchronousQueue<Runnable>();
      } else {
         queue=new ArrayBlockingQueue<Runnable>(workQueueSize);
      }
      executor=new NotifyingExecutor(corePoolSize, maximumPoolSize, keepAliveTime, 
                                      TimeUnit.SECONDS, queue, this, 
                                      new ThreadPoolExecutor.CallerRunsPolicy() );
      executor.prestartAllCoreThreads();
      log.debug("init() done.");
   }
   
   /**
    * Here we define ourselves as being the ThreadPoolExecutor's ThreadFactory.
    * This allows us to customize the threads used to execute tasks. The threads
    * created by this method are instances of {@link NotifyingThread} that are
    * part of the notification chain that notifies waiting threads (callers of
    * {@link AsyncService} waitFor* methods) of the completion of asynchronous
    * tasks run by the {@link NotifyingExecutor}.
    * 
    * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
    * @see NotifyingThread
    */
   @Override
   public Thread newThread(Runnable r) {
      log.debug("Constructing new Thread for Runnable: "+r);
      Thread t=new NotifyingThread(r);
      t.setName("AsyncExec-"+threadid++);
      return t;
   }
   
   /* (non-Javadoc)
    * @see AsyncService#execute(java.lang.Runnable)
    */
   @Override
   public void execute(Runnable r) throws JMException {
      submit(r);
   }

   /**
    * <code>submit</code> implements {@link AsyncService#submit(Runnable)} and
    * wraps the Future returned by the {@link NotifyingExecutor#submit(Runnable)}
    * in a {@link FutureWrapper}. The FutureWrapper implements {@link Notifier}
    * which handles notifying any threads waiting on the task submitted in
    * this method.
    * 
    * @see AsyncService#submit(java.lang.Runnable)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Future<Void> submit(Runnable r) throws JMException {
      log.debug("Runnable: "+r+" submitted.");
      AsyncRunnable asyncRunable = new AsyncRunnable(r);

      FutureWrapper<Void> future;
      future = new FutureWrapper<Void>((Future<Void>)executor.submit(asyncRunable));
      asyncRunable.setNotifier(future);
      return future;
   }

   /**
    * <code>submit</code> implements {@link AsyncService#submit(Callable)} and
    * wraps the Future returned by the {@link NotifyingExecutor#submit(Callable)}
    * in a {@link FutureWrapper}. The FutureWrapper implements {@link Notifier}
    * which handles notifying any threads waiting on the task submitted in
    * this method.
    * 
    * @see AsyncService#submit(Callable)
    */
    @Override
   public <V> Future<V> submit(Callable<V> c)  throws JMException
   {
      AsyncCallable<V> asyncCallable = new AsyncCallable<V>(c);

      FutureWrapper<V> future;
      future = new FutureWrapper<V>(executor.submit(asyncCallable));
      log.debug("Callable: "+c+" submitted. Returned Future: "+future);
      asyncCallable.setNotifier(future);
      return future;
   }
   
   /* (non-Javadoc)
    * @see AsyncService#waitForAny(java.util.Map)
    */
   @Override
   public <K, V> int waitForAny(Map<Future<K>,V> m) throws JMException
   {
      return waitForAny(m.keySet());
   }
   
   /* (non-Javadoc)
    * @see AsyncService#waitForAny(java.util.Collection)
    */
   @Override
   public <V> int waitForAny(Collection<Future<V>> c) throws JMException
   {
      try
      {
         log.debug("Waiting for any completions in collection "+toString(c)+"(size="+c.size()+"): ");

         int runningTasks = 0;
         int collectionSize = c.size();
         NotificationLock notificationLock = NotifyingExecutor.getNotificationLock();
         try
         {
            notificationLock.lock();
            for (Future<?> f : c)
            {
               if (!(f instanceof FutureWrapper))
               {
                  String m = "Future objects passed to waitForAny/waitForAll must have been returned from AsyncExecutorImpl";
                  throw new JMException(ErrorCode.INVALID_FUTURE,m);
               }
               FutureWrapper<?> fw = (FutureWrapper<?>)f;
               Notifier notifier = fw;
               if (notifier != null)
               {
                  notifier.addNotificationTarget(c);
               }
               if (!fw.isDone())
               {
                  log.debug(fw+" still running");
                  runningTasks++;
               }
            }


            if (runningTasks >= collectionSize)
            {
               log.debug("Waiting on any "+runningTasks+" running tasks in collection: "+toString(c));

               wait(c, notificationLock);

               runningTasks = 0;
               for (Future<?> f : c)
               {
                  if (!f.isDone())
                  {
                     log.debug(f+" still running");
                     runningTasks++;
                  }
               }
            }
            log.debug("Returning. "+(collectionSize-runningTasks)+" tasks in collection "+
                  toString(c)+" have completed");
         }
         finally
         {
            notificationLock.unlock(true);
         }
         return runningTasks;
      }
      catch (InterruptedException e)
      {  
         String m = "InterruptedException in waitForAny";
         throw new JMException(ErrorCode.INTERRUPTED_ERROR,m,e);
      }  
   }

   /**
    * <code>wait</code> wait for a notification of the given collection when one of
    * the Futures contained in the collection isDone. 
    * 
    * @param c the Collection on which to wait for notification
    * @param notificationLock the {@link NotificationLock} that insures consistent
    *        access to the data structures that manage notification of collections
    *        of Future objects when one of the contained Futures isDone.
    * @throws InterruptedException
    */
   protected <V> void wait(Collection<Future<V>> c, NotificationLock notificationLock) 
      throws InterruptedException
   {
      // The sequencing of the locking in this method is critical to correct operation
      // of the notification mechanism and to the asynchronous execution of tasks in general.
      //
      // This method is called by the 'waitFor*' methods in this class and as a
      // result the calling thread owns the global notificationLock that
      // synchronizes access to all the objects involved in notifying
      // Collections of Future objects that one of their contained members
      // isDone. 
      //
      // This method first acquires the monitor for the given collection in preparation
      // for waiting on the collection object. Prior to waiting, this method then
      // releases the global notificationLock allowing notifiers to proceed. This
      // method then waits on the collection to be notified. Once notified, this 
      // method releases the monitor no the collection and then re-acquires the 
      // global lock to allow processing of the Futures in the collection
      synchronized (c) 
      {
         notificationLock.unlock(true);
         log.debug("Unlocked. waiting...");
         c.wait();
      }
      notificationLock.lock();
      log.debug("Locked. Proceeding.");
   }

   /**
    * <code>toString</code> provides a compact printable representation of the
    * given collection for debug logging purposes
    *
    * @param c the Collection whose identity needs to be logged
    * @return
    */
   protected static <V> String toString(Collection<Future<V>> c)
   {
      return c.getClass().getSimpleName()+"@"+c.hashCode();
   }

   /* (non-Javadoc)
    * @see AsyncService#waitForAll(java.util.Map)
    */
   @Override
   public <K, V> void waitForAll(Map<Future<K>,V> m) throws JMException
   {
      waitForAll(m.keySet());
   }

   /* (non-Javadoc)
    * @see AsyncService#waitForAll(java.util.Collection)
    */
   @Override
   public <V> void waitForAll(Collection<Future<V>> c) throws JMException
   {
      try
      {
         log.debug("Waiting for all completions in collection "+toString(c)+"(size="+c.size()+"): ");

         int runningTasks;
         NotificationLock notificationLock = NotifyingExecutor.getNotificationLock();
         try
         {
            notificationLock.lock();
            do 
            {
               runningTasks = 0;
               for (Future<?> f : c)
               {
                  if (!(f instanceof FutureWrapper))
                  {
                     String m = "Future objects passed to waitForAny/waitForAll must have been returned from AsyncExecutorImpl";
                     throw new JMException(ErrorCode.INVALID_FUTURE,m);
                  }
                  FutureWrapper<?> fw = (FutureWrapper<?>)f;
                  Notifier notifier = fw;
                  if (notifier != null)
                  {
                     notifier.addNotificationTarget(c);
                  }
                  if (!fw.isDone())
                  {
                     log.debug(fw+" still running");
                     runningTasks++;
                  }
               }
               if (runningTasks > 0)
               {
                  log.debug("Waiting on all "+runningTasks+" running tasks in collection: "+toString(c));
                  wait(c, notificationLock);
               }
            } while (runningTasks > 0);
         }
         finally
         {
            notificationLock.unlock(true);
         }

         log.debug("Returning. All "+c.size()+" tasks in collection "+toString(c)+" have completed");

      }
      catch (InterruptedException e)
      {  
         String m = "InterruptedException in waitForAll";
         throw new JMException(ErrorCode.INTERRUPTED_ERROR,m,e);
      } 
   }
}
