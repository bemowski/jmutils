package net.jmatrix.aspects;

import net.jmatrix.annotations.Logged;
import net.jmatrix.utils.ClassLogFactory;
import net.jmatrix.utils.ExceptionUtils;
import net.jmatrix.utils.StringUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;

@Aspect
public class LoggedAspect extends AbstractLoggingAspect
{
   @Pointcut("execution(@net.jmatrix.annotations.Logged * *(..))")
   void loggedCall() {}

   @Around("loggedCall() && @annotation(logged)")
   public Object logged (ProceedingJoinPoint thisJoinPoint, Logged logged) throws Throwable
   {
      Logger log = ClassLogFactory.getLog(1);

      String methodSignature = "<unknown>";
      String methodName = "<unknown>";
      try
      {
         if (StringUtil.empty(logged.format()))
         {
            // Still support old-style paramNames
            methodSignature = constructMethodSignature(thisJoinPoint, logged.paramNames());
         }
         else
         {
            methodSignature = formatMethodSignature(thisJoinPoint, logged.format());
         }
         methodName = methodSignature.substring(0,methodSignature.indexOf('('));
               
         log.debug("Entering "+methodSignature);
         Object result = thisJoinPoint.proceed();
         boolean hasPerTrackedAnnotation = hasPerfTrackedAnnotation(thisJoinPoint);
         if (!hasPerTrackedAnnotation) log.debug("Exiting  "+methodName+"="+format(logged.result(), new Object[] {result}));
         return result;
      }
      catch (Throwable e)
      {
         NullPointerException npe = ExceptionUtils.findExceptionInStack(e, NullPointerException.class);
         if (npe == null)
         {
            log.warn("Failed   "+methodName+" Exception="+e);
         }
         else
         {
            log.warn("Failed   "+methodName,e);
         }
         throw e;
      }
   }
}
