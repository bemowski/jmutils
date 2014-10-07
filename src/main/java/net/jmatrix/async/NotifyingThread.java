package net.jmatrix.async;

import org.apache.commons.logging.Log;

import net.jmatrix.utils.ClassLogFactory;

public class NotifyingThread extends Thread
{
   static final Log log=ClassLogFactory.getLog();
   
   protected Notifier notifier;
   
   public NotifyingThread(Runnable r)
   {
      super(r);
   }

   public Notifier getNotifier()
   {
      return notifier;
   }

   public void setNotifier(Notifier notifier)
   {
      this.notifier = notifier;
   }
   
   public void notifyTargets()
   {
      if (notifier != null)
      {
         notifier.notifyTargets();
      }
      else
      {
         log.warn("Notifier is null for NotifyingThread: "+this);
      }
   }
}
