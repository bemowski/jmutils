package net.jmatrix.exception;

public class JMException extends Exception
{

    public JMException(ErrorCode ec, String msg)
    {
        this(ec, msg, null);
    }
    
    public JMException(ErrorCode ec, String msg, Throwable t)
    {
        super(msg, t);
        setErrorCode(ec);
    }

    protected ErrorCode errorCode;

    public ErrorCode getErrorCode()
    {
        return errorCode;
    }

    protected void setErrorCode(ErrorCode errorCode)
    {
        this.errorCode = errorCode;
    }
    
    public enum ErrorCode {
        INVALID_FUTURE, 
        INTERRUPTED_ERROR, 
        NOT_INITIIALIZED
    }
}
