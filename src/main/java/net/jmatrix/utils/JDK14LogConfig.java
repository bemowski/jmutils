package net.jmatrix.utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 */
public class JDK14LogConfig {
   public static boolean debug=true;
   
   static Log log=LogFactory.getLog(JDK14LogConfig.class);
   
   // FIXME: Move this to a config file.
   
   public static void startup() {
      System.out.println ("Log is "+log.getClass().getName());
      
      LogManager logManager=LogManager.getLogManager();
      
      Logger logger=Logger.getLogger("");
      
      //logger.removeHandler(logger.getHandlers()[0]);
      
      ConsoleHandler consoleHandler=new ConsoleHandler();
      consoleHandler.setFormatter(new LogFormatter());
      consoleHandler.setLevel(Level.FINER);
      logger.addHandler(consoleHandler);
      logger.setLevel(Level.FINER);
      
      Logger.getLogger("").addHandler(consoleHandler);
      Logger.getLogger("").setLevel(Level.ALL);
      
      Logger.global.addHandler(consoleHandler);
      Logger.global.setLevel(Level.ALL);
//      String level=logManager.getProperty(".level");
//      System.out.println ("Default level is "+level);
//     

      //try {
      //   logManager.readConfiguration(new StringBufferInputStream(logConfig));
      //} catch (Exception ex) {
      //   ex.printStackTrace();
      //}
//      level=logManager.getProperty(".level");
//      System.out.println ("New level is "+level);
      

      if (debug) {
         Enumeration loggers=logManager.getLoggerNames();
         
         while (loggers.hasMoreElements()) {
            String name=(String)loggers.nextElement();
            Logger l=Logger.getLogger(name);
            System.out.println ("   Logger: "+name);
            System.out.println ("          level: "+l.getLevel());
            System.out.println ("       handlers: "+Arrays.asList(l.getHandlers()));
            System.out.println ("       use parent?: "+l.getUseParentHandlers());
         }
      }
      
      log.debug("debug");
   }
   
   
   public static class LogFormatter extends Formatter {
      
      DateFormat df=new SimpleDateFormat("HH:mm:ss.SSS");
      
      /** */
      @Override
      public String format(LogRecord record) {
         
         StringBuilder sb=new StringBuilder();
         
         synchronized(df) {
            sb.append(df.format(new Date(record.getMillis())) + " ");
         }
         
         sb.append(record.getLevel()+" ");
         sb.append(record.getThreadID()+":"+Thread.currentThread().getName()+" ");
         sb.append(shortLoggerName(record.getLoggerName()+" "));
         sb.append(record.getMessage());
         
         if (record.getThrown() != null) {
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            PrintWriter pw=new PrintWriter(new OutputStreamWriter(baos));
            record.getThrown().printStackTrace(pw);
            pw.flush();
            sb.append("\n"+baos.toString());
         } else {
            sb.append("\n");
         }
         return sb.toString();
      }
      
      private static final String shortLoggerName(String loggerName) {
         return loggerName.substring(loggerName.lastIndexOf(".")+1);
      }
   }
   
   public static void main(String args[]) {
      debug=true;
      startup();
      
      log.debug("debug");
      log.info("info");
      log.warn("warn");
      log.error("error");
   }
}
