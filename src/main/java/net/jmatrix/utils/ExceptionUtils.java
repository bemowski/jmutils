package net.jmatrix.utils;

import java.util.List;

import org.apache.commons.logging.Log;

import net.jmatrix.utils.ClassLogFactory;
import net.jmatrix.utils.ListUtil;
import net.jmatrix.utils.StringUtil;

public class ExceptionUtils
{
   private static Log log=ClassLogFactory.getLog();

   public static String getUserMessage(Throwable t) {
      return getUserMessage(t, null);
   }

   public static String getUserMessage(Throwable t, String defaultMsg) {
      String msg = defaultMsg;
      try
      {
         if (StringUtil.empty(msg))
         {
            msg = t.toString().replaceFirst(ExceptionUtils.EXCEPTION_PACKAGE_RE, "$1");
         }
      }
      catch (Throwable e)
      {
         log.error("Unexpected error getting user message: ",e);
      }
      return msg;
   }

   public static String getRepMessage(Throwable t) {
      return getRepMessage(t, null);
   }

   public static String getRepMessage(Throwable t, String defaultMsg) {
      String msg = "";
      try
      {
            msg = getUserMessage(t, defaultMsg);
            msg += getCauseString(t);
      }
      catch (Throwable e)
      {
         log.error("Unexpected error getting rep message: ",e);
      }
      return msg;
   }

   private static String formatContext(List<String> context)
   {
      if (ListUtil.isEmpty(context)) return "";
      StringBuilder sb = new StringBuilder("Error ");
      for(String contextMsg : context)
      {
         sb.append(contextMsg);
         sb.append(" while ");
      }
      sb.append(": ");
      return sb.toString();
   }

   public static String getCauseString(Throwable t)
   {
      String msg = "";
      Throwable cause = t.getCause();
      if (cause != null)
      {
         for (; cause.getCause() != null; cause = cause.getCause());
         // Remove the package name from the exception class name at the start
         // of the toString() representation of the exception
         msg = " Caused by: "+ cause.toString().replaceFirst(ExceptionUtils.EXCEPTION_PACKAGE_RE, "$1");
      }
      return msg;
   }
   
   /** */
   public static <T> T findExceptionInStack(Throwable e, Class <T> c) {
      Throwable t=e;
      while (t != null) {
         if (c.isAssignableFrom(t.getClass()))
            return (T)t;
         t=t.getCause();
      }
      return null;
   }

   protected static final String EXCEPTION_PACKAGE_RE = "^([-A-Z._]+: *)?([a-z0-9_]+\\.)+";

}
