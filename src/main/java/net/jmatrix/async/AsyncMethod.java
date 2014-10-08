package net.jmatrix.async;

import java.lang.reflect.Method;
import java.util.Observable;
import java.util.concurrent.Callable;

import net.jmatrix.utils.ClassLogFactory;

import org.slf4j.Logger;

/**
 * <code><AsyncMethod/code> wraps the invocation of a given java method so that it
 * can be passed to one of the AsyncService's submit methods as Callable object.
 * When the invocation of the encapsulated method completes, AsyncDProxy observers
 * of this AsyncMethod are notified so that method invocations waiting on concurrency
 * limit restrictions can be initiated.
 * 
 * @author fhart
 *
 * @param <V> the return type of the given method
 */
public class AsyncMethod<V> extends Observable implements Callable<V>
{
   static final Logger log = ClassLogFactory.getLog();
   
   protected Object target;
   protected Method method;
   protected Object[] args;
   
   /**
    * <code>AsyncMethod</code> 
    *
    * @param target the Object off of which the given method will be invoked
    * @param method the Method to invoke
    * @param args the parameters to pass to the Method
    */
   public AsyncMethod(Object target, Method method, Object[] args)
   {
      this.target = target;
      this.method = method;
      this.args   = args;
   }

   @Override
   @SuppressWarnings({"unchecked" })
   public V call() throws Exception
   {
      try
      {
         log.debug("Invoking method "+target.getClass().getSimpleName()+"."+method.getName());
         return (V)method.invoke(target, args);
      } 
      catch (Exception e)
      {
         log.error("Error invoking method "+target.getClass().getSimpleName()+"."+method.getName(), e);
         throw e;
      }
      catch (Error e)
      {
         log.error("Error invoking method "+target.getClass().getSimpleName()+"."+method.getName(), e);
         throw e;
      }
      finally
      {
         try
         {
            log.debug("Notifying observers that "+this+" completed.");
            setChanged();
            notifyObservers();
         }
         catch (Throwable e)
         {
            log.error("Error notifying observers of AsyncMethod",e);
         }
      }
   }

   /**
    * <code>getTarget</code> returns the Object off of which the encapsulated
    * Method will be dispatched
    * 
    * @return e Object off of which the encapsulated Method will be dispatched
    */
   public Object getTarget()
   {
      return target;
   }

   /**
    * <code>getMethod</code> returns the Method to invoke
    *
    * @return the Method to invoke
    */
   public Method getMethod()
   {
      return method;
   }

   /**
    * <code>getArgs</code> returns the parameters to pass to the ecapsulated
    * method
    * 
    * @return the parameters to pass to the ecapsulated method
    */
   public Object[] getArgs()
   {
      return args;
   }
   
   @Override
   public String toString()
   {
      String s = super.toString();
      return s.substring(s.lastIndexOf('.')+1)+"."+method.getName();
   }
   
   @Override
   public int hashCode()
   {
      int result = target.hashCode() + method.hashCode();
      if (args != null)
      {
         for (Object o : args)
         {
            if (o != null)
            {
               result += o.hashCode();
            }
            else
            {
               result += "null".hashCode();
            }
         }
      }
      return result;
   }
   
   @Override
   public boolean equals(Object o)
   {
      AsyncMethod<?> asyncMethod = (AsyncMethod<?>)o;
      boolean result;
      result = target.equals(asyncMethod.getTarget());
      result &= method.equals(asyncMethod.getMethod());
      int i = 0;
      for (Object arg : args)
      {
         result &= arg.equals(asyncMethod.getArgs()[i++]);
      }
      return result;
   }

}
