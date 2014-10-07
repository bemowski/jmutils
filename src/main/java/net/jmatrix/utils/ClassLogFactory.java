package net.jmatrix.utils;

import org.apache.commons.logging.Log;

/** */
public class ClassLogFactory {
   /** */
   public static final Log getLog() {
      String callingClassname=DebugUtils.getCallingClassName(1);
      Log log=org.apache.commons.logging.LogFactory.getLog(callingClassname);
      return log;
   }

   public static final Log getLog(int i) {
      String callingClassname=DebugUtils.getCallingClassName(1+i);
      Log log=org.apache.commons.logging.LogFactory.getLog(callingClassname);
      return log;
   }
}
