package groovyx.net.ws.cxf;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;

/**
 * Helper class to set the connection timeout for a request.
 *  
 * @author <a href="mailto:guillaume.alleon@gmail.com">Tog</a>
 * 
 * @since 0.5
 */
public class ConnectionTimeoutHelper extends AbstractSettingHelper {

    /**
     *  The duration of the timeout in milliseconds. 
     */
    private long timeout = 0;

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
     */
    public void setConnectionTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the user and the password for the proxy authorization.
     *
     * @param client The client to configure
     */
    @Override
    protected void configureClientParameters(Client client) {
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        conduit.getClient().setConnectionTimeout(this.timeout);
    }
}
