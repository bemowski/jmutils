package net.jmatrix.aspects;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jmatrix.annotations.Logged;
import net.jmatrix.annotations.PerfTracked;
import net.jmatrix.utils.ClassLogFactory;
import net.jmatrix.utils.DebugUtils;
import net.jmatrix.utils.StringUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class AbstractLoggingAspect
{   
   private static final Logger log = ClassLogFactory.getLog();
   static final String[] emptyArray = new String[] {};
   protected static Pattern pattern = Pattern.compile("%(<|([0-9]+)\\$)?([-#+ 0,(]*[0-9]*(?:\\.[0-9]+)?([bhscdoxefgat%jJdD]))");
   protected static ObjectMapper compactObjectMapper   = createObjectMapper(false);
   protected static ObjectMapper indentingObjectMapper = createObjectMapper(true);

   protected boolean hasLogAnnotation(ProceedingJoinPoint thisJoinPoint)
   {
      Signature sig = thisJoinPoint.getStaticPart().getSignature();
      if (sig instanceof MethodSignature) {
         // this must be a call or execution join point
         Method method = ((MethodSignature)sig).getMethod();
         Logged loggedAnnotation = method.getAnnotation(Logged.class);
         if (loggedAnnotation != null) return true;
      }
      return false;
   }

   protected String[] getParamNames(ProceedingJoinPoint thisJoinPoint)
   {
      Signature sig = thisJoinPoint.getStaticPart().getSignature();
      if (sig instanceof MethodSignature) {
         // this must be a call or execution join point
         return ((MethodSignature)sig).getParameterNames();
      }
      return emptyArray;
   }
   
   /**
    * <code>constructMethodSignature</code> 
    *
    * @deprecated Use {@link #formatMethodSignature(ProceedingJoinPoint, String)} instead
    * @param thisJoinPoint
    * @param paramNames
    * @return
    */
   protected String constructMethodSignature(ProceedingJoinPoint thisJoinPoint, String paramNames)
   {
      String methodName;
      String paramString = "";
      methodName = thisJoinPoint.getSignature().toShortString();
      methodName = methodName.substring(0,methodName.indexOf('(')+1);
      String[] params = (paramNames==null?"":paramNames).split(", *",-1);
      String seperator = "";
      int i = 0;
      for (Object arg : thisJoinPoint.getArgs())
      {
         String paramSpec;
         if (i < params.length && !StringUtil.empty(paramSpec = params[i].trim()))
         {
            String[] paramOptions = Arrays.copyOf(paramSpec.split(" *: *",3),3);
            String paramName = paramOptions[0];
            int maxLength = 256;
            String lengthOption = paramOptions[1];
            if (!StringUtil.empty(lengthOption)) {
               try {
                  maxLength = Integer.parseInt(lengthOption);
               } catch (Exception ex) {
                  System.out.println ("Error parsing max length from token '"+lengthOption+"'"+ex.toString());
               }
            }
            String compactOption = paramOptions[2];
            boolean compact = true;
            if (!StringUtil.empty(compactOption))
            {
               compact = Boolean.parseBoolean(compactOption);
            }
            
            arg = DebugUtils.debugString(arg, 0, maxLength, compact);

            paramString = paramString + seperator + paramName +"="+arg;
         }
         seperator = ", ";
         i++;
      }
      methodName = methodName + paramString + ")";
      //System.out.println(methodName);
      return methodName;
   }
   
   /**
    * <code>formatMethodSignature</code> 
    *
    * @param thisJoinPoint an AspectJ {@link ProceedingJoinPoint}
    * @param format a String of the form used by {@link Formatter} with 
    *        extensions described in {@link Logged} and {@link PerfTracked}
    * @return a String containing a formatted method signature for logging.
    */
   protected String formatMethodSignature(ProceedingJoinPoint thisJoinPoint, String format)
   {
      String methodName;
      String paramString = "";
      methodName = thisJoinPoint.getSignature().toShortString();
      methodName = methodName.substring(0,methodName.indexOf('(')+1);
      paramString = format(format, thisJoinPoint.getArgs());
      methodName = methodName + paramString + ")";
      //System.out.println(methodName);
      return methodName;
   }

   public static String format(String format, Object arg)
   {
      return format(format, new Object[] {arg});
   }
   
   /**
    * <code>format</code> implements the extensions to the formatting described in 
    * {@link Formatter} listed below
    * <ul>
    * <li>J - print arg in JSON notation with indentation</li>
    * <li>j - print arg in JSON notation without indentation</li>
    * <li>D - print arg in {@link DebugUtils#debugString(Object)} format with indentation</li>
    * <li>d - print arg in {@link DebugUtils#debugString(Object)} format without indentation
    *         NOTE that this conversion directive conflicts with the standard Formatter's 'd'
    *         directive for formatting numbers. If the arg in question descends from {@link Number}
    *         then it will be formatted using the standard conversion. Any other arg type
    *         is formatted using {@link DebugUtils#debugString(Object)}</li>
    * </ul>
    * @param format a String containing a printf-like formatting directive described above
    * @param args an Array of Objects to be formatted using the given format string
    * @return
    */
   public static String format(String format, Object[] args)
   {
      try
      {
         if (StringUtil.empty(format))
         {
            return "";
         }
         // List of arguments that may or may not be the same size as the input parameter 'args'
         // This list will contain one argument for each conversion spec matched by the RE below
         List<Object> formattedArgs = new ArrayList<Object>();
         // newFormatString holds a re-written format string whose parameter references all have
         // explicit positional references (e.g. %1$s vs %s) and whose non-standard
         // conversion specs (J,j,D, d) have been replaced by the string conversion spec (s).
         StringBuilder newFormatString = new StringBuilder();
         // RE to match the parameter specifications in format strings accepted by java.util.Formatter.
         // The conversion spec 'n' (newline) has been omitted from the RE since there is no
         // corresponding argument to format.
         Matcher matcher = pattern.matcher(format);
         int previousIndex = -1;
         int unspecifiedIndex = 0;
         int j = 0;
         for (int i = 0; matcher.find(); i++)
         {
            newFormatString.append(format.substring(j,matcher.start()));
            j = matcher.start();
            int index = i;
            String indexSpecifier = matcher.group(1);

            if (indexSpecifier == null)
            {
               index = unspecifiedIndex;
               unspecifiedIndex++;
            }
            else if (indexSpecifier.equals("<"))
            {
               index = previousIndex;
            }
            else
            {
               indexSpecifier = matcher.group(2);
               index = Integer.parseInt(indexSpecifier)-1;
            }
            String formatSpecifier = matcher.group(4);
            if (index < args.length)
            {
               if (StringUtil.empty(formatSpecifier))
               {
                  log.error("Missing format specifier in: '"+matcher.group());
                  formattedArgs.add(args[index]);
                  newFormatString.append("%"+(i+1)+"$"+matcher.group(3));
               }
               else if (formatSpecifier.equals("J"))
               {
                  formattedArgs.add(jsonDebug(args[index],true));
                  String group3 = matcher.group(3);
                  group3 = group3.substring(0,group3.length()-1)+"s";
                  newFormatString.append("%"+(i+1)+"$"+group3);
               }
               else if (formatSpecifier.equals("j"))
               {
                  formattedArgs.add(jsonDebug(args[index],false));
                  String group3 = matcher.group(3);
                  group3 = group3.substring(0,group3.length()-1)+"s";
                  newFormatString.append("%"+(i+1)+"$"+group3);
               }
               else if (formatSpecifier.equals("D"))
               {
                  formattedArgs.add(DebugUtils.debugString(args[index]));
                  String group3 = matcher.group(3);
                  group3 = group3.substring(0,group3.length()-1)+"s";
                  newFormatString.append("%"+(i+1)+"$"+group3);
               }
               else if (formatSpecifier.equals("d") && !(args[index] instanceof Number))
               {
                  formattedArgs.add(DebugUtils.debugString(args[index],0,DebugUtils.MAX_LENGTH,true));
                  String group3 = matcher.group(3);
                  group3 = group3.substring(0,group3.length()-1)+"s";
                  newFormatString.append("%"+(i+1)+"$"+group3);
               }
               else
               {
                  formattedArgs.add(args[index]);
                  newFormatString.append("%"+(i+1)+"$"+matcher.group(3));
               }
            }
            else
            {
               log.warn("Index: "+index+" is beyond the end of the argument array of length "+args.length+
                         " at:\n"+DebugUtils.stackString(new Exception()));
               formattedArgs.add("<out-of-bounds>");
            }
            j = matcher.end();
            previousIndex = index;
         }
         newFormatString.append(format.substring(j));
         Object[] formattedArgsArray = formattedArgs.toArray();
         return String.format(newFormatString.toString(), formattedArgsArray);
      }
      catch (Throwable e)
      {
         log.error("Error formatting args with format String: "+format,e);
         try
         {
            return format+": "+args;
         }
         catch (Throwable e1)
         {
            return format;
         }
      }
   }
   
   public static String formatArgList(String format, String separator, Object[] args)
   {
      try
      {
         if (StringUtil.empty(format))
         {
            return "";
         }
         // newFormatString holds a re-written format string whose parameter references all have
         // explicit positional references (e.g. %1$s vs %s) and whose non-standard
         // conversion specs (J,j,D, d) have been replaced by the string conversion spec (s).
         StringBuilder newFormatString = new StringBuilder();
         newFormatString.append("(");
         for (int i = 0; i < args.length; i++)
         {
            newFormatString.append(format);
            if (i+1 < args.length) newFormatString.append(separator);
         }
         newFormatString.append(")");
         return format(newFormatString.toString(), args);
      }
      catch (Throwable e)
      {
         log.error("Error formatting args with format String: "+format,e);
         try
         {
            return format+": "+args;
         }
         catch (Throwable e1)
         {
            return format;
         }
      }
   }

   protected static ObjectMapper createObjectMapper(boolean indent)
   {
      ObjectMapper om=new ObjectMapper();
      om.configure(SerializationFeature.INDENT_OUTPUT, indent);
      om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      return om;
   }
   
   protected static final String jsonDebug(Object o, boolean indent) {
      ObjectMapper om;
      if (indent)
      {
         om = indentingObjectMapper;
      }
      else
      {
         om = compactObjectMapper;
      }
      try {
         if (o == null)
            return "null";
         return om.writeValueAsString(o);
      } catch (Exception ex) {
         throw new RuntimeException("Error in debug serialization.", ex);
      }
   }

//   public static void main(String[] args)
//   {
//      Object[] arr = new Object[5];
//      arr[0]="String1";
//      arr[1]=new String[] {"one", "two", "three"};
//      arr[2]=new VMBox();
//      String format = "String=%s, VMBox=%3$d, Array=%J, VMBox2=%3$J";
//      if (args.length>0) format = args[0];
//      System.out.println(format(format, arr));
//   }
}