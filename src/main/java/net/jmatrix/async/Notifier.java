package net.jmatrix.async;

import java.util.HashSet;
import java.util.Set;

import net.jmatrix.utils.ClassLogFactory;

import org.slf4j.Logger;

/**
 * <code>Notifier</code> is an abstract class that can be inherited by
 * another class to provide notification to a collection of target 
 * objects. FutureWrapper objects extend Notifier to allow Collections
 * of FutureWrapper objects to be notified when one of their members
 * completes. In this case the notificationTargets are the Collection
 * objects.
 * 
 * @author fhart
 *
 */
public abstract class Notifier
{
   static final Logger log = ClassLogFactory.getLog();
   
   protected Set<Object> notificationTargets;
   
   /**
    * @see NotifyingExecutor#notificationLock
    */
   protected NotificationLock notificationLock = NotifyingExecutor.getNotificationLock();

   /**
    * <code>getNotificationTargets</code> returns the Set of Notification
    * targets.
    * 
    * @return the Set of Notification targets.
    */
   protected Set<Object> getNotificationTargets()
   {
      try 
      {
         notificationLock.lock();
         return notificationTargets;
      }
      finally
      {
         notificationLock.unlock();
      }
   }

   /**
    * <code>setNotificationTargets</code> updates the Set of Notification
    * targets.
    */
   protected void setNotificationTargets(Set<Object> notificationTargets)
   {
      try 
      {
         notificationLock.lock();
         this.notificationTargets = notificationTargets;
      }
      finally
      {
         notificationLock.unlock();
      }
   }

   /**
    * <code>addNotificationTargets</code> adds the given object to the Set of Notification
    * targets.
    */
   protected void addNotificationTarget(Object notificationTarget)
   {
      try 
      {
         notificationLock.lock();
         if (notificationTargets == null)
         {
            notificationTargets = new HashSet<Object>();
         }
         notificationTargets.add(notificationTarget);
      }
      finally
      {
         notificationLock.unlock();
      }
   }
   
   /**
    * <code>notifyTargets</code> iterates over the Set of notificationTargets and
    * calls notifiyAll on each on of the Collections in the set.
    *
    */
   public void notifyTargets()
   {
      try 
      {
         // Acquire the notificationLock to prevent this method from notifying a
         // collection that has not fully registered all its Notifiers and
         // subsequently waited on.
         notificationLock.lock();
         if (notificationTargets != null)
         {
            for (Object target: notificationTargets)
            {
               String targetName = getTargetName(target);
               if (log.isTraceEnabled()) log.trace("Synchronizing on: "+targetName);
               // Acquire the target Collection's monitor in order to notify the Collection
               synchronized (target)
               {
                  log.debug("Notifying target: " + targetName + " that " +
                            this + " has completed.");
                  // Notify all waiters of the target collection
                  target.notifyAll();
               }
               if (log.isTraceEnabled()) log.trace("Releasing synchronizing on: "+targetName);
            }
         }
      }
      finally
      {
         notificationLock.unlock();
      }
   }

   /**
    * <code>getTargetName</code> construct a short name for logging the identity
    * of the target.
    * 
    * @param target a Collection of Future objects on which threads wait for the
    *        Notifiers to complete
    * 
    * @return a short name for logging the identity of the target.
    */
   /**
    * <code>getTargetName</code> 
    *
    * @param target
    * @return
    */
   protected String getTargetName(Object target)
   {
      if (target == null) 
      {
         return null;
      }
      return target.getClass().getSimpleName()+"@"+target.hashCode();
   }
}
