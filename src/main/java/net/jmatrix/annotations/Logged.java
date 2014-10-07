package net.jmatrix.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jmatrix.aspects.AbstractLoggingAspect;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Logged
{
   /**
    * <code>paramNames</code> 
    *
    * @deprecated Use the format parameter instead
    */
   String paramNames() default "";
   
   /**
    * <code>format</code> a format string of the form defined in
    * {@link Formatter} extended with 4 additional conversions defined in
    * {@link AbstractLoggingAspect#format(String, Object[])}
    * 
    * This directive is used to format the arguments to a method
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
}
