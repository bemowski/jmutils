package net.jmatrix.test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import net.jmatrix.test.annotations.AnnotationTest;
import net.jmatrix.utils.ClassLogFactory;
import net.jmatrix.utils.StreamUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/** */
public class AbstractServiceTest {
   static Logger log=(Logger)ClassLogFactory.getLog();



   @BeforeClass
   public static void setup() throws Exception {
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
         catch (IOException e)
         {
            handleFailure(result, resultFileName);
            throw e;
         }
         catch (RuntimeException e)
         {
            handleFailure(result, resultFileName);
            throw e;
         }
         catch (Error e)
         {
            handleFailure(result, resultFileName);
            throw e;
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

   private void handleFailure(String result, String resultFileName) throws FileNotFoundException, IOException
   {
      // On failure write the results of the test run to the /tmp dir to make
      // it easier to compare with the saved result.
      File errorOutputFile = new File("/tmp",resultFileName);
      FileOutputStream fos = new FileOutputStream(errorOutputFile);
      fos.write(result.getBytes());
      fos.close();
      log.debug("Saved "+result.length()+" bytes to: "+errorOutputFile.getPath());
   }
   
   protected String getOutputDir()
   {
      return System.getProperty("test.output",System.getProperty("project.basedir",".")+"/test/output");
   }



   /**
    * <code>addLogCaptureAppender</code> adds a {@link OutputStreamAppender} to the logback
    * root {@link Logger} in order to capture logging results to a StringWriter (and
    * ultimately to a String). The {@link StringWriter} in the {@link WriterAppender} is
    * returned from this method.<p>
    * 
    * The envisioned use case is that at the start of a test method, addLogCaptureAppender
    * is called to begin capturing the logging output during the test and then at the end
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
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      PatternLayoutEncoder ple = new PatternLayoutEncoder();
      ple.setPattern("%msg%n");
      ple.setContext(lc);
      ple.start();
      OutputStreamAppender<ILoggingEvent> osa = new OutputStreamAppender<>();
      osa.setEncoder(ple);
      osa.setContext(lc);
      osa.setOutputStream(baos);
      osa.start();
      Logger logger = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
      logger.addAppender(osa);
      return baos;
   }
   
   
}
