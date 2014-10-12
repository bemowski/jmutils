package net.jmatrix.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import net.jmatrix.jproperties.JProperties;
import net.jmatrix.syslog.LogContext;
import net.jmatrix.test.annotations.AnnotationTest;
import net.jmatrix.utils.ClassLogFactory;
import net.jmatrix.utils.StreamUtil;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.OutputStreamAppender;

/** */
public class AbstractServiceTest {
   static Logger log=ClassLogFactory.getLog();

   public static JProperties p=new JProperties();
   
   
   static {
      // this works, with junit ant task, if we defind the forkmode="once"
      // vs. the ant default of "perTest"
      
      //Log4JLogConfig.log4jBootstrap();
      
      String testPropsFile=System.getProperty("test.properties");
      System.out.println ("Test Properties:"+testPropsFile);
      try {
         if (!StringUtils.isEmpty(testPropsFile)) {
            p.load(testPropsFile);
         }
      } catch (Exception ex) {
         throw new RuntimeException("Cannot setup test harness.", ex);
      }
      
      LogContext.put(LogContext.TRANSPORT, "JUNIT");
      
      try {
         Thread.currentThread().sleep(3000);
      } catch (Exception ex) {
         
      }
   }

   @BeforeClass
   public static void setup() throws Exception {
//      Log4JLogConfig.log4jBootstrap();
//      
//      EProperties p=new EProperties();
//      
//      String testPropsFile=System.getProperty("test.properties");
//      System.out.println ("Test Properties:"+testPropsFile);
//      
//      p.load(System.getProperty("test.properties"));
//      
//      locator=ServiceLocator.init(p.getProperties("services"));
   }
   
   @AfterClass
   public static void tearDown() {
   }
   
   /**
    * <code>compareToSavedResult</code> compares the given log output to a saved 
    * copy from a prior run found in the file given in the resultFileName parameter.
    * If that file does not exist, it will be written by this run. If a visual
    * inspection of the results shows this run to be successful, check those results
    * in and subsequent regression runs will be compared to it.
    *
    * @param result a String holding the log output of the current test
    * @param resultFileName the file where the priro (successful) log output is stored
    * @throws FileNotFoundException thrown if the prior log output is not found
    * @throws IOException if there is a problem reading the prior run's output file
    */
   protected void compareToSavedResult(String result, String resultFileName) 
      throws FileNotFoundException, IOException
   {
      String dirName = getOutputDir();
      File f = new File(dirName, resultFileName);
      if (f.exists())
      {
         FileInputStream fis = null;
         try
         {
            fis = new FileInputStream(f);
            String s = StreamUtil.readToString(fis);
            assertEquals(result, s);
         }
         finally
         {
            if (fis != null) fis.close();
         }
      }
      else
      {
         FileOutputStream fos = null;
         try
         {
            fos = new FileOutputStream(f);
            fos.write(result.getBytes());
            log.debug("Saved "+result.length()+" bytes to: "+f.getPath());
         }
         finally
         {
            if (fos != null) fos.close();
         }
      }
   }
   
   protected String getOutputDir()
   {
      return System.getProperty("test.output","../../test/output");
   }



   /**
    * <code>addLogCaptureAppender</code> adds a {@link OutputStreamAppender} to the logback
    * root {@link Logger} in order to capture logging results to a StringWriter (and
    * ultimately to a String). The {@link StringWriter} in the {@link WriterAppender} is
    * returned from this method.<p>
    * 
    * The envisioned use case is that at the start of a test method, addLogCaptureAppender
    * is called to begin capturing the loggin output during the test and then at the end
    * of the test method, the contents of the String writer is converted to a String and
    * compared against a saved copy of the log output that has been declared "good" using
    * the {@link #compareToSavedResult(String, String)} method in this class. This presumes
    * that there is no logging content that varies randomly from test to test. To this end,
    * the appender that is attached to the root Logger by this method does not log time
    * stamps or thread names or anything other than the message content passed to the log
    * method. Even if this method contains some random content, the log string can be 
    * post-processed using some regular expression replacement or other strategies to yield
    * run invariant content. See {@link AnnotationTest#annotationTest()} for an example
    * method that uses this strategy.
    *
    * @return the ByteArrayOutputStream contained in the OutputStreamAppender attached to the
    * root {@link Logger}
    */
   protected ByteArrayOutputStream addLogCaptureAppender()
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PatternLayout layout = new PatternLayout();
      layout.setPattern("%m\n");
      OutputStreamAppender osa = new OutputStreamAppender();
      LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).addAppender(osa);
      return baos;
   }
   
   
}
