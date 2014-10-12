package net.jmatrix.test.annotations;


import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.jmatrix.annotations.Logged;
import net.jmatrix.annotations.PerfTracked;
import net.jmatrix.test.AbstractServiceTest;
import net.jmatrix.utils.ClassLogFactory;
import net.jmatrix.utils.PerfTrack;

import org.junit.Test;
import org.slf4j.Logger;


public class AnnotationTest extends AbstractServiceTest
{
   static final Logger log = ClassLogFactory.getLog();
   
   @Test
   public void annotationTest() throws Exception
   {
      log.debug("Calling annotationTest()");
      
      // Add a new appender to capture the log output of this test in a StringWriter.
      StringWriter sw = addLogCaptureAppender();
      
      // Run test
      String arg = "This is a test of the annotation system. This is only a test.";
      level1("level1: "+arg);
      log.debug("PerfTrack:\n"+PerfTrack.toString(0));
      // Should not print anything since threshold should not be met
      level4("level4: "+arg, new TestDomainClass1(), new TestDomainClass1());
      log.debug("PerfTrack:\n"+PerfTrack.toString(0));
      loggedMethod("value1");
      format1("format1: "+arg, new TestDomainClass1(), new TestDomainClass1());
      log.debug("PerfTrack:\n"+PerfTrack.toString(0));
      format2("format2: "+arg, new TestDomainClass1(), new TestDomainClass1());
      errorFormat1("Erroneous Format 1");
      errorFormat2("Erroneous Format 2");
      
      // Get the log output
      String result = sw.toString();
    
      // Remove timing values which can vary and cause the comparison to the prior run to fail
      result = result.replaceAll("[0-9]+ms", "# ms");
      
      // Remove Object array addreses which can vary and cause the comparison to the prior run to fail
      result = result.replaceAll("\\[Ljava.lang.Object;@[0-9a-f]+", "[Ljava.lang.Object;@00000000");

      // Compare results of the current run against the results of a saved run
      compareToSavedResult(result, this.getClass().getName()+".annotationTest");
   }

   @PerfTracked(paramNames="s1")
   public void level1(String s1) throws InterruptedException
   {
      Thread.sleep(20);
      level2(s1);
   }
   
   @PerfTracked // Parameter should not print since no paramNames specified
   public void level2(String s2) throws InterruptedException
   {
      Thread.sleep(40);
      level3(s2);
   }
   
   @PerfTracked(paramNames="s3")
   public void level3(String s3) throws InterruptedException
   {
      Thread.sleep(10);
      for (int i = 0; i < 5; i++)
      {
         level4(s3, null, null);
      }
      // Should be printed normally in the PerfTrack since it is only invoked once even though it is
      // below the execution threshold.
      level4b(s3,null,null);
   }
   
   @PerfTracked(paramNames="s4,o1:,o3:5:false", threshold=50)
   public void level4(String s4, Object o1, Object o2) throws InterruptedException
   {
      Thread.sleep(30);
      log.debug(s4);
   }
   
   @PerfTracked(paramNames="s4,o1:,o3:5:false", threshold=50)
   public void level4b(String s4, Object o1, Object o2) throws InterruptedException
   {
      Thread.sleep(5);
      log.debug(s4);
   }
   
   @PerfTracked(threshold=50)
   @Logged(format="s4=%.7s, o1=%J, o2=%3$d")
   public void format1(String s4, Object o1, Object o2) throws InterruptedException
   {
      Thread.sleep(60);
      log.debug(s4);
   }

   @Logged(format="s4=%1$s, _s4=%<s, o2=%3$D, o2.2=%<j")
   public void format2(String s4, Object o1, Object o2) throws InterruptedException
   {
      Thread.sleep(30);
      log.debug(s4);
   }
   
   // Erroneous log formats - bad conversion spec
   @Logged(format="s4=%1$q")
   public void errorFormat1(String s) throws InterruptedException
   {
      Thread.sleep(30);
      log.debug(s);
   }

   // Erroneous log formats - nonexistant arg
   @Logged(format="s4=%s, nonExistantArg=%s")
   public void errorFormat2(String s) throws InterruptedException
   {
      Thread.sleep(30);
      log.debug(s);
   }
   
   @Logged(format="param=\"%s\"%n")
   public void loggedMethod(String param) throws InterruptedException
   {
      Thread.sleep(10);
   }
   
   public static class TestDomainClass1
   {
      protected String name;

      protected List<String> list = new ArrayList<String>();
      protected Object o;

      public TestDomainClass1()
      {
         this.name = this.getClass().getName();
         list.add("String1");
         list.add("String2");
         list.add("String3");
         list.add("String4");
         list.add("String5");
         list.add("String6");
         o = new TestDomainClass2();
      }

      public String getName()
      {
         return name;
      }

      public List<String> getList()
      {
         return list;
      }

      public Object getO()
      {
         return o;
      }
   }
   
   public static class TestDomainClass2
   {
      protected String name;
      protected List<String> list = new ArrayList<String>();
      protected Object o;

      public TestDomainClass2()
      {
         this.name = this.getClass().getName();
         list.add("String1");
         list.add("String2");
         list.add("String3");
         o = new TestDomainClass3();
      }

      public String getName()
      {
         return name;
      }

      public List<String> getList()
      {
         return list;
      }

      public Object getO()
      {
         return o;
      }
   }

   public static class TestDomainClass3
   {
      protected String name;
      protected List<String> list = new ArrayList<String>();

      public TestDomainClass3()
      {
         this.name = this.getClass().getName();
         list.add("String1");
         list.add("String2");
         list.add("String3");
      }

      public String getName()
      {
         return name;
      }

      public List<String> getList()
      {
         return list;
      }

   }
}