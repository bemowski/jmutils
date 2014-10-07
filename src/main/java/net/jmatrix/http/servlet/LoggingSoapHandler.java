package net.jmatrix.http.servlet;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.logging.Log;

import net.jmatrix.utils.ClassLogFactory;

   /**
    * <code>LoggingHandler</code> is a SOAPHandler that logs raw SOAP requests and
    * responses.
    * @author fhart
    *
    */
   public class LoggingSoapHandler implements SOAPHandler<SOAPMessageContext>
   {
      private static Log log = ClassLogFactory.getLog();
      
      @Override
      public boolean handleMessage(SOAPMessageContext smc) 
      {
         logMessage(smc);
         
         return true;
      }

      protected void logMessage(SOAPMessageContext smc)
      {
         try
         {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter out = new PrintWriter(baos, true);
            Boolean outboundProperty = (Boolean)smc.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);

            Map<String,List<String>> httpHeaders;
            if (outboundProperty.booleanValue()) {
               out.println("\nOutbound message:");
               httpHeaders = (Map<String, List<String>>) smc.get(MessageContext.HTTP_REQUEST_HEADERS);
            }
            else 
            {
               out.println("\nInbound message:");
               httpHeaders = (Map<String, List<String>>) smc.get(MessageContext.HTTP_RESPONSE_HEADERS);
            }

            out.println("\nHTTP Headers:");
            if (httpHeaders != null)
            {
               for(String headerName : httpHeaders.keySet())
               {
                  out.print(headerName);
                  out.print("=");
                  out.println(httpHeaders.get(headerName));
               }
            }
            
            // smc (SoapMessageContext) assumes the body of the message is 
            // a SOAP XML document.  But if there is an error on the server, 
            // like a 400 or something - then smc.getMessag() can throw NPE.
            // I'm adding this becasue ther eis still much useful message in the 
            // header information - so we'll still log that even if this throws 
            // and npe.
            // bemo, 3/3/14.
            try {
               SOAPMessage message = smc.getMessage();
               message.writeTo(baos);
            } catch (Exception ex) {
               out.println("Exception in SOAP Message: "+ex.toString());
            }

            out.println("");   
            
            log.debug("SOAPMessage:"+baos.toString());
         }
         catch (Exception e)
         {
            log.error("Error logging SOAPMessageContext",e);
         }
      }

      @Override
      public void close(MessageContext messageContext)
      {
         log.debug("close("+messageContext+")");
      }

      @Override
      public boolean handleFault(SOAPMessageContext messageContext)
      {
         logMessage(messageContext);
         return false;
      }

      @Override
      public Set<QName> getHeaders()
      {
         return null;
      }
   }
