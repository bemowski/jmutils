package net.jmatrix.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <code>FutureWrapper</code> provides a wrapper around Future objects returned
 * from the {@link NotifyingExecutor#submit} methods. This wrapper delegates all
 * calls to the {@link Future} interface to those underlying Future objects and
 * also extends the Notifier abstract class to notify instances of this class
 * when their associated task is complete.
 * 
 * @author fhart
 * 
 * @param <V>
 */
public class FutureWrapper<V> extends Notifier implements Future<V>
{
   protected Future<V> delegate;
   
   public FutureWrapper(Future<V> delegate)
   {
      this.delegate = delegate;
   }
   
   /* (non-Javadoc)
    * @see java.util.concurrent.Future#cancel(boolean)
    */
   public boolean cancel(boolean mayInterruptIfRunning)
   {
      return delegate.cancel(mayInterruptIfRunning);
   }

   /* (non-Javadoc)
    * @see java.util.concurrent.Future#get()
    */
   public V get() throws InterruptedException, ExecutionException
   {
      return delegate.get();
   }

   /* (non-Javadoc)
    * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
    */
   public V get(long timeout, TimeUnit unit) 
      throws InterruptedException, ExecutionException, TimeoutException
   {
      return delegate.get(timeout, unit);
   }

   /* (non-Javadoc)
    * @see java.util.concurrent.Future#isCancelled()
    */
   public boolean isCancelled()
   {
      return delegate.isCancelled();
   }

   /* (non-Javadoc)
    * @see java.util.concurrent.Future#isDone()
    */
   public boolean isDone()
   {
      return delegate.isDone();
   }
   
   /**
    * <code>toString</code> provides a short printable version of this class for logging
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      String s = super.toString();
      return s.substring(s.lastIndexOf('.')+1);
   }

}
