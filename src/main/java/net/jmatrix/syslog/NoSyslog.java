package net.jmatrix.syslog;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention(RUNTIME)
@Target({METHOD,TYPE})
public @interface NoSyslog {
   
   // Add a parameter - max ET.  So if the method takes longer than maxET, then
   // log it, if less than maxET - then skip it.
   
   // or perhaps only log when an exception is thrown... 
}
