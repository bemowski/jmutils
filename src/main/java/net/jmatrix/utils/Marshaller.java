package net.jmatrix.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Marshaller {

   static Map<Class, JAXBContext> contextMap = new HashMap<Class, JAXBContext>();

   /**
    * JAXBContext is thread safe, and not using a 'static' copy of the context
    * causes a class loading explosion.  In addition, this code is much much 
    * faster - average read/write times go from 60-80ms to 1 ms. 
    * 
    * For reference:
    * https://jaxb.dev.java.net/guide/Performance_and_thread_safety.html
    * 
    * @param clazz
    * @return
    * @throws JAXBException
    */
   private static final JAXBContext getContext(Class clazz) 
      throws JAXBException {
      JAXBContext context=contextMap.get(clazz);
      if (context == null) {
         synchronized(contextMap) {
            context=contextMap.get(clazz);
            if (context == null) {
               context=JAXBContext.newInstance(clazz);
               contextMap.put(clazz, context);
            }
         }
      }
      return context;
   }
   
   /** Converts a JAXB object graph to its string representation. */
   public static String toString(Object obj) throws JAXBException {
      StringWriter sw=new StringWriter();
      marshal(obj, sw);
      return sw.toString();
   }
   
   
   public static void marshal(java.lang.Object obj, java.io.Writer writer)
      throws JAXBException
   {
      JAXBContext context=null;
      if (obj instanceof JAXBElement) {
         context=getContext(((JAXBElement)obj).getDeclaredType());
      } else {
         context=getContext(obj.getClass());
      }

      javax.xml.bind.Marshaller marshaller=context.createMarshaller();
      marshaller.setProperty("jaxb.formatted.output", new Boolean(true));
      marshaller.marshal(obj, writer);
   }

   public static void write(Object obj, OutputStream os) 
      throws IOException, JAXBException
   {
      marshal(obj, new OutputStreamWriter(os));
   }

   /** */
   public static void write(Object obj, File f) 
      throws IOException, JAXBException
   {
      write(obj, new FileOutputStream(f));
   }

   /** */
   public static Object read(Class type, InputStream is) 
      throws IOException, JAXBException
   {
      long start=System.currentTimeMillis();
      JAXBContext context=getContext(type);

      Unmarshaller unmarshaller=context.createUnmarshaller();

      Object o=unmarshaller.unmarshal(is);
      long end=System.currentTimeMillis();

      return o;
   }
   
   /** */
   public static Object read(Class type, Reader r) 
      throws IOException, JAXBException
   {
      long start=System.currentTimeMillis();
      JAXBContext context=getContext(type);

      Unmarshaller unmarshaller=context.createUnmarshaller();

      Object o=unmarshaller.unmarshal(r);
      long end=System.currentTimeMillis();

      return o;
   }
}
