package groovyx.net.ws.cxf;


import java.util.HashMap;

import groovyx.net.ws.WSClient;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;

/**
 * Helper class to configure the proxy settings.
 *
 * @see SettingConstants#HTTP_PROXY_HOST
 * @see SettingConstants#HTTP_PROXY_PORT
 * @see SettingConstants#HTTP_PROXY_USER
 * @see SettingConstants#HTTP_PROXY_PASSWORD
 *
 * @version 06.03.2009
 */
public class ProxyHelper extends AbstractSettingHelper
{
    /**
     * Default constructor
     *
     */
    public ProxyHelper(){
    }

    /**
     * Reads the system properties and puts the value of
     * {@link SettingConstants#HTTP_PROXY_HOST}, {@link SettingConstants#HTTP_PROXY_PORT},
     * {@link SettingConstants#HTTP_PROXY_USER} and {@link SettingConstants#HTTP_PROXY_PASSWORD}
     * into the property-map.
     */
    @Override
    protected void setDefaultProperties()
    {
        this.properties = new HashMap<String, String>();

        this.properties.put(SettingConstants.HTTP_PROXY_HOST,
            System.getProperty(SettingConstants.HTTP_PROXY_HOST));
        this.properties.put(SettingConstants.HTTP_PROXY_PORT,
            System.getProperty(SettingConstants.HTTP_PROXY_PORT));
        this.properties.put(SettingConstants.HTTP_PROXY_USER,
            System.getProperty(SettingConstants.HTTP_PROXY_USER));
        this.properties.put(SettingConstants.HTTP_PROXY_PASSWORD,
            System.getProperty(SettingConstants.HTTP_PROXY_PASSWORD));
    }

    /**
     * Sets the user and the password for the proxy authorization.
     */
    @Override
    protected void configureClientParameters(Client client){
        String host = this.properties.get(SettingConstants.HTTP_PROXY_HOST);
        String port = this.properties.get(SettingConstants.HTTP_PROXY_PORT);
        String proxyUsername = this.properties.get(SettingConstants.HTTP_PROXY_USER);
        String proxyPassword = this.properties.get(SettingConstants.HTTP_PROXY_PASSWORD);

        HTTPConduit conduit = (HTTPConduit)client.getConduit();

        if (host != null)
        {
            conduit.getClient().setProxyServer(host);
            if (port != null)
            {
                conduit.getClient().setProxyServerPort(Integer.parseInt(port));
            }
            if ((proxyUsername != null) && (proxyPassword != null))
            {
                conduit.getProxyAuthorization().setUserName(proxyUsername);
                conduit.getProxyAuthorization().setPassword(proxyPassword);
            }
        }
    }
}