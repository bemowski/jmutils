package net.jmatrix.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.jmatrix.utils.ClassLogFactory;

import org.slf4j.Logger;

/**
 * <code>NotifyingExecutor</code> extends the ThreadPoolExecutor and implements
 * the {@link ThreadPoolExecutor#afterExecute(Runnable, Throwable)} and 
 * {@link Thread.UncaughtExceptionHandler#uncaughtException(Thread, Throwable) methods in
 * order trigger the notification of threads waiting on Collections of Future
 * objects
 * 
 * @author fhart
 */
public class NotifyingExecutor extends ThreadPoolExecutor implements Thread.UncaughtExceptionHandler
{
   static final Logger log=ClassLogFactory.getLog();
   
   /**
    * The notificationLock ensures that code that the code in
    * {@link AsyncExecutorImpl} that adds notification targets (Collections of
    * Future objects passed to the waitFor* methods) and the code in
    * {@link Notifier} that notifies those targets do not access those targets
    * in an unsafe manner. In particular the lock ensures that no notifications
    * are sent until the target Collection has been registered with all
    * Future(Notifier) objects in the Collection.
    */
   protected static final NotificationLock notificationLock = new NotificationLock();

   public NotifyingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, 
         TimeUnit unit, BlockingQueue<Runnable> workQueue) 
   {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
   }

   public NotifyingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, 
         TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) 
   {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
   }

   public NotifyingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, 
         TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) 
   {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
   }

   public NotifyingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, 
         TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, 
         RejectedExecutionHandler handler) 
   {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread,
    * java.lang.Runnable)
    */
   @Override
   protected void beforeExecute(Thread t, Runnable r)
   {
      super.beforeExecute(t, r);
      log.debug("beforeExecute called for thread: " + t + ", runnable=" + r);
      log.debug("active threads="+getActiveCount()+", pool size="+getPoolSize());
   }
   
   /**
    * <code>afterExecute</code> is called when a thread completes execution of
    * an asynchronous task. At this point, the isDone method of the Future
    * object associated with this task will return true. This method notifies
    * those Future objects (that were registered with the NotifyingThread in
    * AsyncCallable.call() or AsyncRunnable.run() ) that their task is complete
    * and the result is available.
    * 
    * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable,
    *      java.lang.Throwable)
    * @see AsyncCallable#call()
    */
   @Override
   protected void afterExecute(Runnable r, Throwable t) 
   {
      super.afterExecute(r, t);
      Thread thread = Thread.currentThread();
      if (thread instanceof NotifyingThread)
      {
         notifyTargets((NotifyingThread)thread, t);
      }
      log.debug("afterExecute called for runnable: "+r+", throwable="+t);
      log.debug("active threads="+getActiveCount()+", pool size="+getPoolSize());
   }

   /**
    * <code>notifyTargets</code> notifies those Future objects (that were
    * registered with the NotifyingThread in AsyncCallable.call() or
    * AsyncRunnable.run() ) that their task is complete and the result is
    * available.
    * 
    * @param thread The Thread that completed executing the task and which has
    *        a reference to the associated Future object
    * @param throwable
    */
   private void notifyTargets(NotifyingThread thread, Throwable throwable)
   {
      // TODO Auto-generated method stub
      log.debug("notifyTargets called for thread: "+thread+", throwable="+throwable);
      thread.notifyTargets();
   }

   /**
    * <code>uncaughtException</code> catches any uncaught Throwables from a
    * thread. It is overridden here so that the notifyTargets method gets called
    * when a java.lang.Error is raised by a NotifyingThread. the afterExecute 
    * method in this class will trigger notification for all other thread termination
    * outcomes, but the documentation for afterExecute specifically states that
    * Errors will not trigger afterExecute. notifyTargets must be called in all
    * cases when a NotifyingThread terminates in order to not have callers of
    * {@link AsyncService}.waitFor* methods wait indefinitely.
    * 
    * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread,
    *      java.lang.Throwable)
    */
   @Override
   public void uncaughtException(Thread thread, Throwable throwable)
   {
      if (throwable instanceof Error)
      {
         // If the async thread throws an Error, the afterExecute method is
         // not called and uncaughtException is called. We call notifyTargets 
         // here to insure that notifyTargets gets called even when the task
         // throws an Error.
         if (thread instanceof NotifyingThread)
         {
            notifyTargets((NotifyingThread)thread, throwable);
         }
      }
   }

   public static NotificationLock getNotificationLock()
   {
      return notificationLock;
   }

}
