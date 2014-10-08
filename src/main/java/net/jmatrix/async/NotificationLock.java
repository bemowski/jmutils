package net.jmatrix.async;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import net.jmatrix.utils.ClassLogFactory;

import org.slf4j.Logger;

/**
 * <code>NotificationLock</code> extends {@link ReentrantLock} and adds some
 * logging of lock/unlock methods as well as providing an extra unlock method
 * that releases all recursive locks for the current thread
 * 
 * @author fhart
 * 
 */
@SuppressWarnings("serial")
public class NotificationLock extends ReentrantLock
{
   static final Logger log=ClassLogFactory.getLog();
   
   public NotificationLock()
   {
      super(true);
   }
   
   /* (non-Javadoc)
    * @see java.util.concurrent.locks.ReentrantLock#getOwner()
    */
   public Thread getOwner()
   {
      return super.getOwner();
   }
   
   /* (non-Javadoc)
    * @see java.util.concurrent.locks.ReentrantLock#lock()
    */
   public void lock()
   {
      String priorOwner = getOwnerName();
      try
      {
         while (!super.tryLock(60, TimeUnit.SECONDS))
         {
            log.warn("POSSIBLE DEADLOCK. Unable to obtain NotificationLock: "+this);
         }
         if (log.isTraceEnabled())
         {
            log.trace("Locked [owner="+priorOwner+", holdCount="+getHoldCount()+"]");
         }
      } 
      catch (InterruptedException e)
      {
         log.error("Interrupted trying to obtain NotificationLock: "+this+
                   ", holdCount="+getHoldCount(),e);
      }
   }

   /* (non-Javadoc)
    * @see java.util.concurrent.locks.ReentrantLock#unlock()
    */
   public void unlock()
   {
      if (isHeldByCurrentThread())
      {
         String priorOwner = getOwnerName();
         super.unlock();
         if (log.isTraceEnabled())
         {
            log.trace("Unlocked [owner="+priorOwner+", holdCount="+getHoldCount()+"]");
         }
      }
      else
      {
         log.error("Unexpected owner of notificationLock: "+getOwner());
      }
   }

   /**
    * <code>unlock</code> optionally unlocks all recursive locks by the current thread
    *
    * @param fullUnlock a boolean which if true unlocks all recursive locks by the current thread
    */
   public void unlock(boolean fullUnlock)
   {
      if (isHeldByCurrentThread())
      {
         String priorOwner = getOwnerName();
         if (fullUnlock)
         {
            if (getHoldCount() > 1)
            {
               log.warn("Unexpected holdCound: "+getHoldCount());
            }
            while (getHoldCount() > 0) super.unlock();
         }
         else
         {
            super.unlock();
         }
         if (log.isTraceEnabled())
         {
            log.trace("Unlocked [owner="+priorOwner+", holdCount="+getHoldCount()+"]");
         }
      }
      else
      {
         log.error("Unexpected owner of notificationLock: "+getOwner());
      }
   }

   /**
    * <code>getOwnerName</code> returns the current owner of the lock
    *
    * @return the current owner of the lock  or null if the lock is not currently locked
    */
   protected String getOwnerName()
   {
      Thread owner = getOwner();
      return owner==null ? null : owner.getName();
   }
   
}
