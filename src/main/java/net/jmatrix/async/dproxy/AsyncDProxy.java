package net.jmatrix.async.dproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.jmatrix.async.AsyncMethod;
import net.jmatrix.async.AsyncService;
import net.jmatrix.async.AsyncServiceImpl;
import net.jmatrix.async.ConcurrencyManager;
import net.jmatrix.exception.JMException;
import net.jmatrix.utils.ClassLogFactory;

import org.apache.commons.logging.Log;

/**
 * <code>AsyncDProxy</code> is a dynamic proxy that wraps the methods of a class
 * and dispatches these methods using the {@link AsyncService}. To use the
 * AsyncDProxy to wrap the methods of a class, an interface must be created
 * so that the normal return types for methods to be invoked asynchronously are replaced
 * by <code>Future</code> objects whose generic parameter is the return type of
 * the corresponding method in the implementing class. For example, a method in a class
 * with a return type of String would now return Future&lt;String> in the asynchronous
 * interface. Methods that return void would return Future&lt;Void> and methods
 * returning primitive types would return a Future whose generic parameter is
 * the java object type corresponding to the primitive type. e.g. methods
 * returning <code>int</code> return Future&lt;Integer>. 
 * <p>
 * There is a perl script <code>genasync</code> in the bin directory that takes
 * an existing standard interface as input and generates the
 * corresponding async interface. The script takes the filename of the 
 * interface and generates the corresponding async interface to standard out.
 * <p>
 * Method calls on proxies returned by {@link #newInstance(Object, Class)}
 * return <code>Future</code> objects that can be used to access the actual
 * return value of the service via the {@link Future#get()} method. This method
 * will return the value returned by the underlying implementation if the
 * method has completed or it will block until the method returns. If
 * the underlying method throws an exception, the Future's <code>get</code>  
 * method will throw an {@link ExecutionException} whose getCause method will return
 * the underlying exception.
 * 
 * @see VMProvisioningServiceImpl#deleteAllCpbxVMBoxes(String)
 * @see AsyncTestService
 * @see Future
 * 
 */
public class AsyncDProxy implements InvocationHandler, DelegateProxy, Cloneable, Observer, ConcurrencyManager
{
   private static Log log = ClassLogFactory.getLog();

   /**
    * DEFAULT_CONCURRENCY_LIMIT is the default maximum number of threads
    * that will be dispatched concurrently from a single AsyncDProxy
    * instance. Additional calls to this async service will block until
    * an outstanding one completes.
    */
   public static int DEFAULT_CONCURRENCY_LIMIT = 20;

   // The property file key used to specify the java interface that
   // the the AsyncDProxy implements.
   public static final String ASYNC_SERVICE_PROPERTY = "asyncService";
   
   // Map of Future objects returned by methods of this service. The invoke
   // method automatically registers any returned Future under the method
   // name that was invoked. This leads to the restriction that there
   // can only be one automatically registered future object for a given
   // method at any time.
   protected Map<Object,Future<?>> futureMap = new HashMap<Object, Future<?>>();
   
   // Flag to alow enabling / disabling method result cache at runtime.
   protected boolean cacheEnabled = false;
   
   // The list of interfaces to be implemented by this dynamic proxy.
   protected Class<?>[] interfaces;

   protected Object delegate = null;
   
   // The actual concurrency limit of this service instance. This limit
   // can be changed by calling setConcurrencyLimit. This is the reason
   // this class is Cloneable since we may want different instances to
   // have different values of this limit. Non async services are singletons
   // since they have not state like this that needs to vary between instances.
   protected int concurrencyLimit = DEFAULT_CONCURRENCY_LIMIT;
   
   // The reference to the AsyncService for invoking service methods
   // asynchronously
   protected AsyncService asyncService;

   
   // NOTE: only manipulate this variable via synchronized methods
   private int activeTasks = 0;

   // Private constructor used to create an AsyncDProxy that takes a
   // reference to a LocalService and a list of interfaces to implement.
   // Use the static newInstance method to create new instances of 
   // AsyncDProxys
   private AsyncDProxy(Object delegate, Class<?>[] interfaces) throws JMException
   {
      this.delegate = delegate;
      this.interfaces = interfaces;
      asyncService = AsyncServiceImpl.getInstance();
   }

   // Private copy constructor used to create separate instances of
   // an AsyncDProxy that has been registered with the ServiceLocator
   // so that changes to the concurrencyLimit are not shared between
   // instances of the AsyncDProxy returned from the ServiceLocator
   private AsyncDProxy(AsyncDProxy proxy)
   {
      interfaces       = proxy.interfaces;
      delegate         = proxy.delegate;
      asyncService     = proxy.asyncService;
      concurrencyLimit = proxy.concurrencyLimit;
   }

   /* (non-Javadoc)
    * @see net.jmatrix.dproxy.DelegateProxy#getDelegate()
    */
   public Object getDelegate()
   {
      return delegate;
   }
   
   /**
    * <code>newInstance</code> creates a new instance of an AsyncDProxy
    * that proxies all methods to the given delegate. This method assumes
    * nothing about the class hierarchy of the implementing delegate. 
    * 
    * @param <T> an interface of type T that defines the methods asynchronously
    *            invokable via the proxy. All methods on the interface should
    *            return a type of Future&lt;X&gt; where X is the natural return
    *            type of the synchronous method implemented in the delegate class 
    * @param delegate an object that implements the methods of the given interface.
    * @return an AsyncDProxy instance whose methods invoke the methods of
    *         the delegate object in separate threads, returning {@link Future}
    *         objects. The resulting dynamic Proxy also implements the 
    *         {@link ConcurrencyManager} and {@link AsyncService} interfaces.
    * @throws ClassNotFoundException, ClassCastException
    * @throws JMException 
    */
   public static <T> T newInstance(Object delegate, Class<T> asyncInterface)
         throws ClassNotFoundException, ClassCastException, JMException
   {
      Class<?>[] interfaces = new Class<?>[] { asyncInterface, ConcurrencyManager.class, AsyncService.class };
      ClassLoader classLoader = delegate.getClass().getClassLoader();
      AsyncDProxy asyncDProxy = new AsyncDProxy(delegate, interfaces);

      log.trace("AsyncDProxy(" + delegate.getClass().getName() + ", " + interfaces + ")");

      return (T) Proxy.newProxyInstance(classLoader, interfaces, asyncDProxy);
   }
   
   /**
    * <code>invoke</code> provides the implementation of the
    * {@link InvocationHandler#invoke} method. It implements the mechanism to
    * limit the concurrency for this service instance by blocking submissions
    * past the concurrency limit until an outstanding submission completes.
    * 
    * It is notified of completing submissions by implementing
    * {@link Observable} so that it is notified each time a new thread completes
    * handling a method of this service.
    * 
    * @see AsyncMethod
    * @see InvocationHandler#invoke(Object, Method, Object[])
    */
   @Override
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable
   {      
      if (method.getDeclaringClass().equals(getAsyncInterface()))
      {
         Method delegateMethod = getMethod(delegate.getClass(), method);
         log.debug("Calling "+method.getDeclaringClass().getSimpleName()+"."+method.getName()+
                   " from "+this.getClass().getSimpleName()+
                   " on "+ delegate +
                   " with args: "+Arrays.toString(args));

         // Create an instance of AsyncMethod which implements Callable<V> 
         // to encapsulate the delegate method and allow it to be run
         // asynchronously by the AsyncService
         AsyncMethod<?> asyncMethod = new AsyncMethod(delegate, delegateMethod, args);
         
         // See if there is a cached Future that has already completed for this AsyncMethod.
         // AsyncMethod's hashcode takes into account the method name and arguments used to
         // invoke the method.
         Future<?> future = futureMap.get(asyncMethod);
         
         // AsyncMethods are observed by the AsyncDProxys that invoke them. When
         // an AsyncMethod completes, it notifies its observing AsyncDProxy so
         // that it can reduce its number of activeThreads. The active thread
         // count is tracked to implement the concurrency limits managed via the
         // ConcurrencyManager interface of the AsyncDProxy.
         asyncMethod.addObserver(this);
         
         if (future == null || !cacheEnabled)
         {
            // This call will wait on this AsyncDProxy object until a completing
            // thread triggers a call to this class's update method
            blockOnConcurrencyLimit();

            // Schedule this method for execution by a separate thread
            future = asyncService.submit(asyncMethod);
            incrementActiveTasks();

            // Cache the Future object in the futureMap so that it can be
            // retrieved later from this AsyncDProxy instance
            futureMap.put(asyncMethod, future);
         }
         else
         {
            log.debug("Using cached Future: "+future);
            // CFH: The line below looks like a bug so it is commented out. AsyncMethods are observed 
            //      by the AsyncDProxys that invoke them. When an AsyncMethod completes, it notifies 
            //      its observing AsyncDProxy so that it can reduce its number of activeThreads. 
            //      The active thread count is tracked to implement the concurrency limits managed 
            //      via the ConcurrencyManager interface of the AsyncDProxy. In this code path, 
            //      however, we never incremented the number of active threads since we are using 
            //      a cached Future and not initiating a new thread. Hence there is no need to reduce 
            //      the active thread count.
            //asyncMethod.notifyObservers();
         }

         // Return the Future object returned from the AsyncService
         return future;
      }
      else if (method.getDeclaringClass().equals(AsyncService.class))
      {
         log.debug("Calling "+method.getDeclaringClass().getSimpleName()+"."+method.getName()+
                   " from "+this.getClass().getSimpleName()+
                   " on "+ asyncService +
                   " with args: "+Arrays.toString(args));
         
         // This second category of methods are member methods of the AsyncService. 
         // We're primarily interested in handling the waitForAll and waitForAny
         // methods here.
         return method.invoke(asyncService, args);
      }
      else
      {
         // This final category of methods handled by invoke are methods local to
         // this AsyncDproxy instance or one of its ancestors, most notably 
         // AsyncLocalService. We cannot use the java.reflect.Method object
         // passed to 'invoke' to directly invoke these methods because
         // the Method object passed to 'invoke' is based off of the async
         // interface specified in the service properties. AsyncDproxy does
         // not statically implement that interface (although it dynamically does)
         // so we look up the method name specified by the input Method object
         // based off of this AsyncDProxy instance.
         Method localMethod = getMethod(AsyncDProxy.class, method);
         log.debug("Calling "+method.getDeclaringClass().getSimpleName()+"."+method.getName()+
                   " from "+this.getClass().getSimpleName()+
                   " on "+ this +
                   " with args: "+Arrays.toString(args));
         
         return localMethod.invoke(this, args);
      }
   }
   
   public void clearFutures()
   {
      futureMap.clear();
   }
   
   /* (non-Javadoc)
    * Called by an AsyncMethod object when that method completes to notify
    * this AsyncDProxy that there is one fewer active threads for this
    * service
    * 
    * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
    */
   @Override
   public synchronized void update(Observable arg0, Object o)
   {
      activeTasks--;
      notify();
   }

   /** */
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return "AsyncDProxy(" + delegate.toString() + ")";
   }

   /* (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   @Override
   public AsyncDProxy clone()
   {
      AsyncDProxy clone = new AsyncDProxy(this);
      return clone;
   }

   /**
    * <code>incrementActiveTasks</code> provides synchronized updating of
    * the number of activeTasks dispatched off of this AsyncDProxy
    *
    */
   protected synchronized void incrementActiveTasks()
   {
      activeTasks++;
   }

   /**
    * <code>getAsyncInterface</code> returns the async interface that this
    * AsyncDProxy implements. This interface is specified in the service
    * properties for the associated service
    *
    * @return the async interface that this AsyncDProxy implements 
    */
   protected Class<?> getAsyncInterface()
   {
      return interfaces[0];
   }

   /**
    * <code>getMethod</code> looks up a {@link Method} with the given method's
    * signature based off of the given target class.
    *
    * @param targetClass The Class or interface containing a method (or whose ancestor
    *        contains a method) whose signature matches the given method's signature
    * @param method a Method. Typically one of the service methods of the async 
    *        interface
    * @return a Method whose signature matches the given method's signature that is 
    *         defined in the given targetClass or one of its ancestors
    * @throws NoSuchMethodException
    */
   protected Method getMethod(Class<?> targetClass, Method method) throws NoSuchMethodException
   {
      Method localMethod = null;
      try
      {
         localMethod = targetClass.getMethod(method.getName(),method.getParameterTypes());
      }
      catch (NoSuchMethodException e)
      {
         localMethod = targetClass.getDeclaredMethod(method.getName(),method.getParameterTypes());
      }
      return localMethod;
   }

   /**
    * <code>blockOnConcurrencyLimit</code> waits on this instance of AsyncDProxy
    * until it is notified that a thread processing another method invocation of
    * this service has completed.
    * 
    * @throws InterruptedException
    */
   protected synchronized void blockOnConcurrencyLimit() throws InterruptedException
   {
      if (activeTasks >= concurrencyLimit)
      {
         log.debug("Concurrency limit of "+concurrencyLimit+" reached");
         wait();
         log.debug("Waking from concurrency wait active="+activeTasks+
                   " limit="+concurrencyLimit);
      }
      else
      {
         log.debug("activeTasks="+activeTasks+" concurrencyLimit="+concurrencyLimit);
      }
   }
   
   /**
    * <code>getConcurrencyLimit</code> returns the current concurrency limit for this 
    * instance of AsyncDProxy
    *
    * @return the current concurrency limit for this instance of AsyncDProxy
    */
   public int getConcurrencyLimit()
   {
      return concurrencyLimit;
   }

   /**
    * <code>setConcurrencyLimit</code> sets the current concurrency limit for this 
    * instance of AsyncDProxy.
    * 
    * @param concurrencyLimit an int that specifies the maximum number of simultaneous
    *        method invications allowed on this AsyncDProxy instance.
    */
   public void setConcurrencyLimit(int concurrencyLimit)
   {
      this.concurrencyLimit = concurrencyLimit;
   }

   /**
    * <code>isCacheEnabled</code> returns the caching behavior of this AsyncDProxy instance.
    * The default is to not cache results of async methods, primarily to avoid unexpected
    * behavior. If caching is desired it should be explicitly requested.
    *
    * @return a boolean indicating the caching behavior of this AsyncDProxy instance
    */
   public boolean isCacheEnabled()
   {
      return cacheEnabled;
   }

   /**
    * <code>setCacheEnabled</code> sets the caching behavior of this AsyncDProxy instance.
    * The default is to not cache results of async methods, primarily to avoid unexpected
    * behavior. If caching is desired it should be explicitly requested.
    *
    * @param cacheEnabled a boolean indicating the caching behavior for this AsyncDProxy instance
    */
   public void setCacheEnabled(boolean cacheEnabled)
   {
      this.cacheEnabled = cacheEnabled;
   }
}
