package net.jmatrix.syslog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlTransient;

import net.jmatrix.utils.DebugUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An event of significance to the system.  Any or all fields may or may
 * not be used.  If being used directly, the application programmer should
 * attempt to fill in as many fields as possible with _useful_ debugging
 * information.
 */
public class SysLogEvent {
   // likely some sort of uuid, probably provided by the service
   protected String eventId=null;
   
   /** Could be the result of a thrown exception, or any other ID an
   * application programmer would like to attach to this event. */
   protected String errorId=null;
   
   // the time this error occured. This is based on the local time setting
   // on the server on which the event was constructed, and may be subject
   // to some time skew.
   protected Date timestamp=null;  // obvious.
   protected String hostName=null; // added by server, indicates the server host name.
   protected String systemId=null; // added by server, should indicate the system type.
   protected String version=null;  // the software version.  11.0a
   
   protected String clientIp=null; // 
   
   protected String threadName=null; // added by server, may have some context info...
   
   protected String sessionId=null;  // typically your http session id. 
   protected String transport=null;  // BLAZE / REST / JAX-WS / LOCAL /??
   protected String loginId=null;
   protected String accountNumber=null;  // The account number currently associated with the session.
   
   /** 
    * free form.  Indicates something about the event.  Values might be like
    * SESSION or EVENT or VOICEMAIL etc.    
    */
   protected String type=null;
   
   /** 
    * Again free form.  Can be null.  Indicates something more about the 
    * event type.  if type=SESSION subtype may be LOGIN or CHANGE_ACOCUNT etc.
    */
   protected String subtype=null;
   
   /** 
    * This is a free-form message containing detail about the event.
    */
   protected String message=null;
   
   
   /** This is typically for service method calls, but could be for any call
    * it should indicate the amount of time the method call took.
    */
   protected long execTime=0l;
   
   // These are optional method parameters, used when logging 
   // service events.
   protected List<String> methodArgs=null;
   
   /**
    * this may well be set by the local JVM calling methods - but should be
    * ignored after the populateFromContext method - where the exception 
    * object will be used to populate:
    *   exceptionStack  required
    *   errorId         optional
    *   errorMessage    optional
    */

   // needed because exception stacktrace can't be marshalled
   /**
    * Jax-B doesn't respect transient property
    *  so for each serialization will have to add ignores and transient for
    *  each serialization mechanism
    * */
   @XmlTransient
   @JsonIgnore
   protected Throwable exception=null;

   
   protected String exceptionStack=null;
   
   protected String result=null;
   
   public SysLogEvent() {
      timestamp=new Date();
      eventId=UUID.randomUUID().toString();
   }
   
   public String debugString() {
      return DebugUtils.debugString(this);
   }
   
   public String shortString() {
      StringBuilder sb=new StringBuilder();
      sb.append("SysLogEvt[");
      if (timestamp != null)
         sb.append(timestamp.toString()+" / ");
      
      sb.append("T:"+transport+" / ");
      
      sb.append("sid:"+sessionId+" / ");
      sb.append("cip:"+clientIp+" / ");
      
      if (errorId != null) 
         sb.append("eid:"+errorId+" / ");
      
      sb.append("type/subtype:"+type+" / "+subtype+" / ");
      
      sb.append("message:"+message);
      
      if (exception != null) 
         sb.append(" / "+exception.getMessage());
      sb.append("]");
      return sb.toString();
   }

   public String getEventId() {
      return eventId;
   }

   public void setEventId(String eventId) {
      this.eventId = eventId;
   }

   public String getErrorId() {
      return errorId;
   }

   public void setErrorId(String errorId) {
      this.errorId = errorId;
   }

   public Date getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(Date timestamp) {
      this.timestamp = timestamp;
   }

   public String getHostName() {
      return hostName;
   }

   public void setHostName(String hostName) {
      this.hostName = hostName;
   }

   public String getSystemId() {
      return systemId;
   }

   public void setSystemId(String systemId) {
      this.systemId = systemId;
   }

   public String getClientIp() {
      return clientIp;
   }

   public void setClientIp(String clientIp) {
      this.clientIp = clientIp;
   }

   public String getSessionId() {
      return sessionId;
   }

   public void setSessionId(String sessionId) {
      this.sessionId = sessionId;
   }

   public String getTransport() {
      return transport;
   }

   public void setTransport(String transport) {
      this.transport = transport;
   }

   public String getLoginId() {
      return loginId;
   }

   public void setLoginId(String loginId) {
      this.loginId = loginId;
   }

   public String getAccountNumber() {
      return accountNumber;
   }

   public void setAccountNumber(String accountNumber) {
      this.accountNumber = accountNumber;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getSubtype() {
      return subtype;
   }

   public void setSubtype(String subtype) {
      this.subtype = subtype;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public long getExecTime() {
      return execTime;
   }

   public void setExecTime(long execTime) {
      this.execTime = execTime;
   }

   public Throwable getException() {
      return exception;
   }
   
   public void setException(Throwable exception) {
      this.exception = exception;
   }

   public String getThreadName() {
      return threadName;
   }

   public void setThreadName(String threadName) {
      this.threadName = threadName;
   }

   public List<String> getMethodArgs() {
      return methodArgs;
   }

   public void setMethodArgs(List<String> methodArgs) {
      this.methodArgs = methodArgs;
   }
   
   public void setMethodArgs(String s) {
      List<String> l=new ArrayList<String>();
      l.add(s);
      setMethodArgs(l);
   }

   public String getResult() {
      return result;
   }

   public void setResult(String result) {
      this.result = result;
   }

   public String getVersion() {
      return version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public String getExceptionStack() {
      return exceptionStack;
   }

   public void setExceptionStack(String exceptionStack) {
      this.exceptionStack = exceptionStack;
   }
}
