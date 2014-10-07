package net.jmatrix.async;

public interface ConcurrencyManager
{
   /**
    * <code>getConcurrencyLimit</code> returns the maximum number of concurrent
    * threads that can be executing for this service. Each service instance
    * returned from the {@link ServiceLocator#getService(Class)} can set
    * independent values for its concurrency limit. This includes separate
    * instances of the same underlying service retrieved in two separate calls
    * to <code>getService()</code>
    * 
    * @return the maximum number of concurrent threads that can be executing for
    *         this service
    */
   public int getConcurrencyLimit();

   /**
    * <code>setConcurrencyLimit</code> sets the maximum number of concurrent
    * threads that can be executing for this service. Each service instance
    * returned from the {@link ServiceLocator#getService(Class)} can set
    * independent values for its concurrency limit. This includes separate
    * instances of the same underlying service retrieved in two separate calls
    * to <code>getService()</code>
    * 
    * @return the maximum number of concurrent threads that can be executing for
    *         this service
    */
   public void setConcurrencyLimit(int limit);
}
