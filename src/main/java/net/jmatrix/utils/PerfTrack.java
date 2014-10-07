package net.jmatrix.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * PerfTrack tracks performance through multiple operations within
 * a Thread.  Functionally it works with static method calls, that can 
 * be called from anywhere within the code.  Primary methods are start/stop - 
 * which adds a tracking of the performance (or wall clock time) it takes 
 * to execute the operation in question.  
 * 
 * The PerfTrack builds a tree-like structure showing the amounts of time taken
 * for each sub-operation.  
 * 
 * This will be useful in tracking performance issues in the web application, 
 * and the tree based performance output allows us to focus in rapidly on 
 * problem areas of the code and its interactions with external system.
 * 
 * This code was Modelled on the Log4J MDC (Mapped Diagnostic Context).
 * 
 * @author Paul Bemowski
 */
public final class PerfTrack {
   private static boolean debug = Boolean.valueOf(System.getProperty("debug", "false"));
   static Log log=ClassLogFactory.getLog();
   
   // FIXME: 
   //   - Change to InheritableThreadLocal??
   //   - Ensure thread safety - I think its threadsafe now.
   static ThreadLocal<Item> threadLocalCurrent=new ThreadLocal<Item>();
   
   /** */
   public static void start(Method m) {
      String s=getMethodString(m);
      start(s,s);
   }
   
   /** */
   public static void start(Method m, long threshold) {
      String s=getMethodString(m);
      start(s,s,threshold);
   }
   
   /** */
   public static void start() {
      String caller = getCaller();
      start(caller,caller, null);
   }

   /** */
   public static void start(long threshold) {
      String caller = getCaller();
      start(caller, caller, threshold);
   }

   /** */
   public static void start(String name) {
      start(name, getCaller(), null);
   }

   /** */
   public static void start(String name, long threshold) {
      start(name, getCaller(), threshold);
   }

   /** */
   public static void start(String name, String id) {
      start(name, id, null);
   }

      /** */
   public static void start(String name, String id, long threshold) {
      start(name, id, new Long(threshold));
   }

   /** */
   protected static void start(String name, String id, Long threshold) {
      if (name == null)
         throw new NullPointerException("Null PerfTrack name.  Cannot perftrack null.");
      
      Item current=threadLocalCurrent.get();
            
      current=addChild(name, id, current, threshold);  // new sub-item
   
      current.start();
      current.id = id;
      
      threadLocalCurrent.set(current);

      // fixme: remove - verbose PerfTrack Debug.
      if (log.isTraceEnabled()) 
      {
         log.trace("PerfTrack.start("+name+"), current: "+current);
      }
   }

   /** */
   public static long stop() {
      String name = getCaller();
      return stop(name, name, null);
   }
   
   /** */
   public static long stop(Throwable t) {
      String name = getCaller();
      return stop(name, name, t);
   }
   
   /** */
   public static long stop(Method m) {
      String s=getMethodString(m);
      return stop(s);
   }

   /** */
   public static long stop(Method m, Throwable t) {
      String s=getMethodString(m);
      return stop(s,s,t);
   }

   /** */
   public static long stop(String name) {
      return stop(name, name, null);
   }
   
   /** */
   public static long stop(String name, String id, Throwable t)
   {
      long et = -1;
      if (name == null)
         throw new NullPointerException("Null PerfTrack name.  Cannot perftrack null.");

      Item current = threadLocalCurrent.get();

      if (current == null)
      {
         log.warn("Stopping, but current is null??");
      }
      else
      {
         Item lastChild = null;
         if (current.getName() != null && current.getName().equals(name))
         {
            et = current.stop();

            // fixme: remove - verbose PerfTrack Debug.
            if (log.isTraceEnabled()) 
            {
               log.trace("PerfTrack.stop("+name+"), current: "+current+
                         " complete: "+isCurrentRootAndComplete());
            }

            current.throwable = t;

            Item parent = current.getParent();
            if (parent != null)
            {
               threadLocalCurrent.set(parent);
            }
            else
            {
               // leave current as root...
               // threadLocalCurrent.set(null);
            }
         }
         else if ((lastChild = current.getLastChild()) != null &&
                  name.equals(lastChild.getName()))
         {
            // Don't see how we get into this block of code. Logging it to
            // see if we actually do.
            log.warn("Stopping lastChild: "+name);
            // Update the id of the last child run
            lastChild.id = id;
            lastChild.throwable = t;
         }
         else
         {
            // stopping item that is not current. likely because
            // someone forgot to stop a PerfTrack with try/finally
            // or other programming error/typo.
            Item ancestor = current.findAncestor(name);
            // If we find the target item above us in the stack, stop that
            // item and all intervening items.
            if (ancestor != null)
            {
               while (current != ancestor)
               {
                  log.warn("Stopping '" + name + "' but current is '" +
                           current.getName() + "'");
                  et = current.stop();

                  // fixme: remove - verbose PerfTrack Debug.
                  if (log.isTraceEnabled())
                  {
                     log.trace("PerfTrack.stop("+name+"), current: "+current+
                               " complete: "+isCurrentRootAndComplete());
                  }

                  current = current.parent;
               }
               threadLocalCurrent.set(ancestor);
               // Recursive call, but this time we know we're stopping current
               stop(name,id,t);
            }
            else
            {
               log.warn("Stopping '" +name+ "' but '"+name+"' was not found on "+
                        "PerfTrack Item stack. current is '"+current.getName() + "'");
            }
         }
      }
      return et;
   }
   
   /** */
   public static void clear() {
      //log.debug("PerfTrack.clear()");
      threadLocalCurrent.remove();
   }
   
   /** */
   public static boolean isCurrentRootAndComplete() {
      Item current=threadLocalCurrent.get();
      if (current != null ) {
         if (current.getParent() == null) {
            if (current.isDone()) {
               return true;
            }
         }
      } 
//      else {
//         return true;
//      }
      return false;
   }

   public static String toString(int depth) {
      Item current=threadLocalCurrent.get();
      if (current != null ) {
         Map<String,Collection<Item>> residueMap = new HashMap<String,Collection<Item>>();
         if (current.getParent() == null) {
            // this is the root.
            return current.toString(depth, residueMap);
         } else {
            log.warn("toString() called, but current is not root.  Unfinished tree items.");
            Item root=current.findRoot();
            return root.toString(depth, residueMap);
         }
      }
      else 
         return "PerfTrack: no data?";
   }
   
   protected static String getCaller()
   {
      return getCaller(Thread.currentThread().getStackTrace());
   }
   
   protected static String getCaller(StackTraceElement[] stackTrace)
   {
      for (int i = 1; i < stackTrace.length; i++)
      {
         StackTraceElement e = stackTrace[i];
         String className = e.getClassName();
         if (debug) System.out.println (i+"::"+className);
         if (!className.equals(PerfTrack.class.getName()))
         {
            className=className.substring(className.lastIndexOf(".")+1);
            return createItemName(className,e.getMethodName()); 
         }
      }
      return "<unknown>";
   }

   protected static String createItemName(String className, String methodName)
   {
      return  className + "." + methodName;
   }

   protected static Item addChild(String name, String id, Item current, Long threshold) {
      Item child = null;
      if (current == null) {
         child=new Item(name, null);
      } else {
         child=new Item(name, current);
      }
      if (threshold != null) {
         child.threshold = threshold.longValue();
      } else if (current != null) {
         child.threshold = current.threshold;
      }
      return child;
   }

   /** Returns a string representing a method. */
   protected static final String getMethodString(Method m) {
      String classname=m.getDeclaringClass().getSimpleName();
      return createItemName(classname,m.getName());
   }

   /**  */
   static class Item {
      String id;
      String name;
      Item parent=null;
      long start=0;
      long stop=0;
      long et=0;
      long threshold = 20;
      
      /**
       * throwable stores any Throwable thrown by the method corresponding to this item
       */
      Throwable throwable;

      List<Item> children=new ArrayList<Item>();
      
      /** */
      public Item(String n, Item p) {
         name=n;
         parent=p;
         if (p != null)
         {
            p.children.add(this);
         }
      }
      
      public Item findAncestor(String x)
      {
         if (x == null) return null;
         Item ancestor = this;
         while (ancestor != null && !x.equals(ancestor.getName()))
         {
            ancestor = ancestor.parent;
         }
         return ancestor;
      }
      
      public Item getLastChild()
      {
         if (children != null && children.size() > 0) {
            return children.get(children.size()-1);
         }
         return null;
      }

      public void start() {start=System.currentTimeMillis();}
      public long stop() {stop=System.currentTimeMillis(); return (et=stop-start);}
      public List<Item> getChildren() {return children;}
      public String getName() {return name;}
      public Item getParent() {return parent;}
      public Throwable getThrowable()
      {
         return throwable;
      }

      public void setThrowable(Throwable throwable)
      {
         this.throwable = throwable;
      }      
      public String toString() {
         return "PTItem("+id+", name="+name+", start="+start+", stop="+stop+", parent="+parent+")";
      }
      
      public boolean hasChildren() {
         if (children.size() > 0)
            return true;
         return false;
      }
      
      public boolean isDone() {
         if (stop == 0)
            return false;
         return true;
      }
      
      public Item findRoot() {
         if (parent == null)
            return this;
         else
            return parent.findRoot();
      }
      
      public String toString(final int depth, Map<String,Collection<Item>> parentResidueItems) {
         StringBuilder sb=new StringBuilder();
         String threw = throwable==null?"":"threw ["+throwable.getClass().getSimpleName()+"] ";
         Map<String,Collection<Item>> residualItems = new HashMap<String,Collection<Item>>(); 
         if (et >= threshold || parent == null || throwable != null) {
            pad(depth,sb);
            sb.append(name+" "+threw+et+"ms\n");
            if (children.size() > 0) {
               long unaccounted=et;

               for (Item child: children) {
                  if (child.et < child.threshold && child.throwable == null){
                     Collection<Item> items = residualItems.get(child.id);
                     if (items == null) items = new ArrayList<Item>();
                     items.add(child);
                     residualItems.put(child.id, items);            
                  }                  
               }

               for (Item child: children) {
                  unaccounted=unaccounted-child.et;
                  sb.append(child.toString(depth+1,residualItems));
               }

               for (String tag : residualItems.keySet()) {
                  Collection<Item> callCollection = residualItems.get(tag);
                  if (callCollection.size()==1) continue;
                  long callCollectionDuration = 0;
                  for(Item item : callCollection) {
                     callCollectionDuration += item.et;
                  }
                  pad(depth+1, sb);
                  int size = callCollection.size();
                  sb.append("* "+size+" call"+(size==1?"":"s")+" to "+tag+" "+callCollectionDuration+"ms\n");
               }
               // unaccounted
               pad(depth+1, sb);
               sb.append("Other "+unaccounted+"ms\n");
            }
         } else {
            Collection<Item> residualItem = parentResidueItems.get(id);
            if (residualItem == null || residualItem.size() == 1) {
               pad(depth,sb);
               sb.append(name+" "+threw+et+"ms\n");
            }
         }
         return sb.toString();
      }

      protected String pad(int depth, StringBuilder sb)
      {
         for (int i=0; i<depth; i++) {
            sb.append("  ");
         }
         return sb.toString();
      }
   }
}
