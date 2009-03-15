package groovyx.net.ws.cxf;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;

/**
 * Created by IntelliJ IDEA.
 * User: alleon
 * Date: Mar 10, 2009
 * Time: 9:59:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionTimeoutHelper extends AbstractSettingHelper {
    private long timeout;

    /**
     * Default constructor
     *
     */
    public ConnectionTimeoutHelper() {
    }

    /**
     * Sets the default timeout value to <code>0</code> which means forever
     */
    @Override
    protected void setDefaultProperties() {
        this.timeout = 0;
    }

    /**
     * Sets the value of the connectionTimeout property.
     *
     * @param timeout value of the timeout in milliseconds
     *
     */
    public void setConnectionTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    /**
     * Sets the user and the password for the proxy authorization.
     *
     * @param client .
     */
    @Override
    protected void configureClientParameters(Client client) {
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        conduit.getClient().setConnectionTimeout(this.timeout);
    }
}
