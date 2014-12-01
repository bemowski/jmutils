package net.jmatrix.aspects;

import net.jmatrix.utils.ClassLogFactory;
import net.jmatrix.utils.ExceptionUtils;
import net.jmatrix.utils.PerfTrack;
import net.jmatrix.utils.StringUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;

@Aspect
public class PerfTrackAspect extends AbstractLoggingAspect
{
   static final String[] emptyArray = new String[] {};
   private long threshold = 0;

   @Pointcut("execution(@net.jmatrix.annotations.PerfTracked * *(..))")
   void perfTrackedCall() {}

   @Around("perfTrackedCall() && @annotation(perfTrack)")
   public Object perfTrack (ProceedingJoinPoint thisJoinPoint, net.jmatrix.annotations.PerfTracked perfTrack) throws Throwable
   {
      // Acquire the Log on each invocation rather than using a static member to 
      // hold the logger since we want the logger of our caller which will differ
      // from invocation to invocation. This will mean that the logger name that
      // appears in the log entry will be the class name annotated with
      // @PerfTracked and not PerfTracked itself.
      Logger log = ClassLogFactory.getLog(this.getClass().getName());
      String methodSignature = "<unknown>";
      String methodName = "<unknown>";
      long start=System.currentTimeMillis();
      threshold = perfTrack.threshold();
      try
      {
         if (StringUtil.empty(perfTrack.format()))
         {
            // Still support old-style paramNames
            methodSignature = constructMethodSignature(thisJoinPoint, perfTrack.paramNames());
         }
         else
         {
            methodSignature = formatMethodSignature(thisJoinPoint, perfTrack.format());
         }
         methodName = methodSignature.substring(0,methodSignature.indexOf('('));
               
         if (threshold >= 0) PerfTrack.start(methodSignature,methodName,threshold);
         boolean hasLogAnnotation = hasLogAnnotation(thisJoinPoint);
         if (!hasLogAnnotation) log.debug("Entering "+methodSignature);
         Object result = thisJoinPoint.proceed();
         long et = -1;
         if (threshold >= 0) et = PerfTrack.stop(methodSignature);
         log.debug("Exiting["+(threshold>0?et:"")+"ms] "+methodName+"="+format(perfTrack.result(), new Object[] {result}));
         return result;
      }
      catch (Throwable e)
      {
         NullPointerException npe = ExceptionUtils.findExceptionInStack(e, NullPointerException.class);
         long et = -1;
         if (threshold >= 0) et = PerfTrack.stop(methodSignature,methodSignature,e);
         if (npe == null)
         {
            log.debug("Failed["+(threshold>0?et:"")+"ms] "+methodName+" Exception="+e);
         }
         else
         {
            log.debug("Failed["+(threshold>0?et:"")+"ms] "+methodName,e);
         }
         throw e;
      }
      finally
      {
         if (threshold >= 0) 
         {
            long end=System.currentTimeMillis();
            long et=end-start;
            if (PerfTrack.isCurrentRootAndComplete()) 
            {
               if (et > threshold )
               {
                  log.debug("PerfTrack:\n"+PerfTrack.toString(0));
               }
               PerfTrack.clear();
            }
         }
      }
   }
}
