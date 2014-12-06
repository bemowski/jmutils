package net.jmatrix.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Formatter;

import net.jmatrix.aspects.AbstractLoggingAspect;

/**
 * <code>PerfTracked</code> 
 * @author fhart
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PerfTracked
{
   
   /**
    * <code>paramNames</code> 
    *
    * @deprecated Use the format parameter instead
    */
   String paramNames() default "";
   
   /**
    * <code>format</code> a format string of the for defined in
    * {@link Formatter} extended with 4 additional conversions. defined in the
    * {@link AbstractLoggingAspect#format(String, Object[])}
    * 
    * @return
    */
   String format() default "";

   /**
    * <code>result</code> a format string of the form defined in
    * {@link Formatter} extended with 4 additional conversions defined in
    * {@link AbstractLoggingAspect#format(String, Object[])}
    * 
    * This directive is used to format the return value of a method
    *
    * @return
    */
   String result() default "%s";
   
   /**
    * <code>threshold</code> is the number of milliseconds of execution time that
    * are required for a method to show up in the PerfTrack summary.
    *
    * @return
    */
   long threshold() default 20;
   
   /**
    * <code>verbosÔe</code> indicates whether or not to include method parameters in the 
    * PerfTrack summary printed out at the end of a request. Default is false
    *
    * @return
    */
   boolean verbosÔe() default false;
}
