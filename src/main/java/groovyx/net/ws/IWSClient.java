package groovyx.net.ws;

/**
 * Interface for a WSClient. The generic type C refers to a client implementation.
 *
 * @author <a href="mailto:groovy@courson.de">Dennis Bayer</a>
 * @version 14.11.2008
 */
public interface IWSClient<C>
{
    /**
     * Invokes the webservice-method.
     *
     * @param name The name of the method.
     * @param args The arguments of the method.
     *
     * @return The result of the call.
     */
    public Object invokeMethod(String name, Object args);

    /**
     * Factory method to create the webservices client.
     *
     * @param args The arguments needed for the initialization of the client.
     *
     * @return An instance of C.
     */
    public C createClient(Object... args);

    /**
     * Initializes the client.
     */
    public void initialize();
}
