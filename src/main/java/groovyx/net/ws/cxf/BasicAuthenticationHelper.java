package groovyx.net.ws.cxf;

import java.util.HashMap;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;

/**
 * Helper class to configure the http basic authentication
 *
 * @see SettingConstants#HTTP_USER
 * @see SettingConstants#HTTP_PASSWORD
 *
 * @author <a href="mailto:groovy@courson.de">Dennis Bayer</a>
 * @author <a href="mailto:guillaume.alleon@gmail.com">Tog</a>
 *
 * @since 0.5
 */
public class BasicAuthenticationHelper extends AbstractSettingHelper {

    /**
     * Sets the basic authentication properties defined in {@link SettingConstants}
     *
     * @param user     The username to use to connect to the proxy
     * @param password The password.
     *
     * @see SettingConstants#HTTP_USER
     * @see SettingConstants#HTTP_PASSWORD
     */
    public void setBasicAuthentication(String user, String password) {
        this.properties = new HashMap<String, String>();
        this.properties.put(SettingConstants.HTTP_USER, user);
        this.properties.put(SettingConstants.HTTP_PASSWORD, password);
    }

    /**
     * Reads the system properties and puts the value of
     * {@link SettingConstants#HTTP_USER} and {@link SettingConstants#HTTP_PASSWORD}
     * into the property-map.
     */
    @Override
    protected void setDefaultProperties() {
        this.properties = new HashMap<String, String>();

        this.properties.put(SettingConstants.HTTP_USER, System
                .getProperty(SettingConstants.HTTP_USER));
        this.properties.put(SettingConstants.HTTP_PASSWORD, System
                .getProperty(SettingConstants.HTTP_PASSWORD));
    }

    /**
     * Sets the user and password for the http conduit.
     */
    @Override
    protected void configureClientParameters(Client client) {
        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        String username = this.properties.get(SettingConstants.HTTP_USER);
        String password = this.properties.get(SettingConstants.HTTP_PASSWORD);

        if ((username != null) && (password != null)) {
            AuthorizationPolicy authPolicy = conduit.getAuthorization();
            authPolicy.setUserName(username);
            authPolicy.setPassword(password);
        }
    }

}
