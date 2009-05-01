package groovyx.net.ws.cxf;

import java.util.Map;

import org.apache.cxf.endpoint.Client;

/**
 * Abstract helper class to set configuration
 * properties of a {@link Client}
 *
 * @author <a href="mailto:groovy@courson.de">Dennis Bayer</a>
 * 
 * @since 0.5
 */
public abstract class AbstractSettingHelper {

    /**
     * Stores the properties.
     */
    protected Map<String, String> properties;

    /**
     * Initializes the setting. If no properties were set externally,
     * the default properties are used.
     */
    public void initialize() {
        if (!isConfigured()) {
            setDefaultProperties();
        }
    }

    /**
     * @return <code>true</code> if the properties are already set,
     *         otherwise <code>false</code>.
     */
    protected boolean isConfigured() {
        return this.properties != null && !this.properties.isEmpty();
    }

    /**
     * Sets the properties.
     *
     * @param properties The map containing the properties.
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Sets the default properties.
     */
    protected abstract void setDefaultProperties();

    /**
     * Sets the default properties.
     *
     * @param client The CXF client to configure
     */
    protected abstract void configureClientParameters(Client client);

    /**
     * Enables the settings according to the values in the propertymap.
     *
     * @param client The CXF client on which to enable the properties
     */
    public void enable(Client client) {
        configureClientParameters(client);
    }

}
