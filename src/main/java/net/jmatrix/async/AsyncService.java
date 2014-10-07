package net.jmatrix.async;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import net.jmatrix.exception.JMException;

/** 
 * This is a local service, used by other services internally to execute
 * work asynchronously.
 * 
 **/
public interface AsyncService extends ThreadFactory {
   
   /**
    * <code>submit</code> this method schedules a Runnable for execution
    * in a separate worker thread. No handle to the result of the execution
    * is returned from this method
    *
    * @param r a Runnable that represents the work to be done
    * @throws JMException
    */
    public void execute(Runnable r) throws JMException;
   
   /**
    * <code>submit</code> this method schedules a Runnable for execution
    * in a separate worker thread
    *
    * @param r a Runnable that represents the work to be done
    * @return A Future object that wraps the results of the computation
    * @throws JMException
    * @see {@link ExecutorService#submit(Runnable)}
    */
   public Future<Void> submit(Runnable r) throws JMException;

   /**
    * <code>submit</code> this method schedules a Callable for execution
    * in a separate worker thread
    *
    * @param c a Callable that represents the work to be done
    * @return A Future object that wraps the results of the computation
    * @throws JMException
    * @see {@link ExecutorService#submit(Callable)}
    */
   public <V> Future<V> submit(Callable<V> c) throws JMException;
   
   /**
    * <code>waitForAny</code> waits on the given collection of Future objects
    * until at least one of them have completed. Modifying the Collection
    * after calling waitForAny results in undefined behavior.
    *
    * @param c a Collection of Future objects on which to wait
    * @return the number of Future objects whose isDone() method returns true
    * @throws JMException
    */
   public <V> int waitForAny(Collection<Future<V>> c) throws JMException;
   
   /**
    * <code>waitForAll</code> waits on the given collection of Future objects
    * until all of them have completed. Modifying the Collection
    * after calling waitForAny results in undefined behavior.
    *
    * @param c a Collection of Future objects on which to wait
    * @throws JMException
    */
   public <V> void waitForAll(Collection<Future<V>> c) throws JMException;

   /**
    * <code>waitForAny</code> waits on the KeySet of the given Map of Future
    * objects until at least one of them have completed. This method is useful
    * so that some context can be associated with each Future object of a group
    * of Future objects so that when they complete or throw an Exception the
    * program can know something about the given Future object. Modifying the
    * Collection after calling waitForAny results in undefined behavior.
    * 
    * @param c
    *           a Map of Future objects on which to wait
    * @return the number of Future objects whose isDone() method returns true
    * @throws JMException
    */
   public <K,V> int waitForAny(Map<Future<K>,V> m) throws JMException;
   
   /**
    * <code>waitForAll</code> waits on the KeySet of the given Map of Future
    * objects until all of them have completed. This method is useful so that
    * some context can be associated with each Future object of a group of
    * Future objects so that when they complete or throw an Exception the
    * program can know something about the given Future object. Modifying the
    * Collection after calling waitForAny results in undefined behavior. See
    * {@link VMProvisioningServiceImpl#deleteAllCpbxVMBoxes(String)} for an
    * example of how this can be used
    * 
    * @param c
    *           a Map of Future objects on which to wait
    * @throws JMException
    */
   public <K,V> void waitForAll(Map<Future<K>,V> m) throws JMException;
}
