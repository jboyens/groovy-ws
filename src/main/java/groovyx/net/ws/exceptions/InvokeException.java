/*
 * $Id$
 *
 * copyright CURSOR Software AG, 2009
 */
package groovyx.net.ws.exceptions;

/**
 * An exception which can occur during the invoke of a webservice call.
 *
 * @author <a href="mailto:Dennis.Bayer@cursor.de">Dennis Bayer</a>
 * @version 08.06.2009
 */
public class InvokeException extends RuntimeException
{
    private static final long serialVersionUID = -7444028836301431091L;

    /**
     *
     */
    public InvokeException()
    {
        super();
    }

    /**
     *
     */
    public InvokeException(String message)
    {
        super(message);
    }

    /**
     *
     */
    public InvokeException(String message, Throwable t)
    {
        super(message, t);
    }

    /**
     *
     */
    public InvokeException(Throwable t)
    {
        super(t);
    }

}
