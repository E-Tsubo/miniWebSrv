package minwebsrv.servlet;

public class ServletException extends Exception {
	public ServletException(String message) {
        super(message);
    }

    public ServletException(String message, Throwable rootCause)  {
        super(message, rootCause);
    }

    public ServletException(java.lang.Throwable rootCause) {
        super(rootCause);
    }
}
