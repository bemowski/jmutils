package net.jmatrix.utils;

import java.lang.reflect.Method;


/**
 * A set of utilities for dealing with .class objects and files.
 */
public class ClassUtil
{

   /** Returns the short name of a class - it's name without 
    * the package prefix. Often useful for logging. */
   public static final String shortClassName(Class c) {
      return c.getSimpleName();
   }

   /** Returns the short name of a class - it's name without 
    * the package prefix. Often useful for logging. */
   public static final String shortClassName(Object o) {
      return o.getClass().getSimpleName();
   }
   
   public static Method getter(Class t, String field) throws SecurityException, NoSuchMethodException {
      String name="get"+field.substring(0,1).toUpperCase()+field.substring(1);
      return t.getMethod(name, (Class[])null);
   }
}
