package net.jmatrix.utils;

import java.lang.reflect.*;


public class LightweightCopy {
   public static <T> T copy(T t) throws SecurityException, IllegalArgumentException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      return copy(t, "id");
   }
   
   public static <T> T copy(T t, String field) throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
      
      if (t == null)
         return t;
      
      Class c=t.getClass();
      
      Object copy=c.newInstance();
      
      String camel=field.substring(0, 1).toUpperCase()+field.substring(1);
      
      Method getter=c.getMethod("get"+camel,
            new Class[] {});
      
      Object id=getter.invoke(t, (Object[])null);
      
      if (id == null) {
         // no reason to copy it.
      } else {
         Method setter=c.getMethod("set"+camel, new Class[] {id.getClass()});
         
         setter.invoke(copy, new Object[] {id});
      }

      
      return (T)copy;
   }
}
