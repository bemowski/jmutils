package net.jmatrix.http.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.jmatrix.annotations.PerfTracked;
import net.jmatrix.context.LogContext;
import net.jmatrix.http.servlet.TLContext;
import net.jmatrix.utils.ClassLogFactory;
import net.jmatrix.utils.PerfTrack;

import org.slf4j.Logger;

/**
 * 
 */
public class JMFilter implements Filter {
   static Logger log=ClassLogFactory.getLog();
  
   
   @Override
   public void init(FilterConfig fc) throws ServletException {
      log.debug("Initializing JMFilter Filter");
   }
   
   @Override
   public void destroy() {
   }
   
   @Override
   @PerfTracked(format="request=%s", threshold=100)
   public void doFilter(ServletRequest request, ServletResponse response,
                        FilterChain chain) throws IOException, ServletException {

      HttpServletRequest  hrequest  = (HttpServletRequest)request;
      HttpServletResponse hresponse = (HttpServletResponse)response;
      HttpSession         session   = hrequest.getSession();
      
      // Get a fresh set of AsyncService instances in this request.
      // See the javadoc for ServiceLocator.clearAsyncServices() for more details
      
      try {
         TLContext.clear();
         LogContext.clear();
         PerfTrack.start("URL: "+hrequest.getRequestURI());
         
         LogContext.put(LogContext.TRANSPORT, "SOAP");
         log.debug("Processing Request: "+hrequest.getRequestURI());
         
         chain.doFilter(request, response);

      } catch (Throwable ex) {
         log.error("Error processing.", ex);
      } finally {
         PerfTrack.stop();
         TLContext.clear();
         LogContext.clear();
         log.debug("Done: "+hrequest.getRequestURI());
      }
   }
}
