package groovyx.net.ws;

import groovyx.net.ws.cxf.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

/**
 * A Webservice client using the cxf-framework dynamic client factory.
 *
 * @version 06.03.2009
 */
public class WSClient extends AbstractCXFWSClient
{
    /**
     * HTTP protocol
     */
    private static final String HTTPS = "https";

    /**
     * The URL of the WSDL-file.
     */
    protected URL url;

    /**
     * The ClassLoader to use to generate classes from WSDL.
     */
    protected ClassLoader classloader;


    /**
     * Default constructor.
     *
     * @param wsdlLocation The url of the wsdl-file
     * @param classloader  The classoader
     */
    public WSClient(String wsdlLocation, ClassLoader classloader)
    {
        try {
            this.url = new URL(wsdlLocation);
            //this.localWSDL = this.url;
            this.classloader = classloader;
            //this.client = createClient(this.url, classloader);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL is not valid.", e);
        }

        this.sslHelper               = new SSLHelper();
        this.proxyHelper             = new ProxyHelper();
        this.basicAuthHelper         = new BasicAuthenticationHelper();
        this.mtomHelper              = new MtomHelper();
        this.connectionTimeoutHelper = new ConnectionTimeoutHelper();
    }

    /**
     * Configures the configuration for the transport of the client connection.
     *
     * @param conduit The conduit that need to be modified
     */
    private void configureHttpClientPolicy(HTTPConduit conduit)
    {
        HTTPClientPolicy httpClientPolicy = conduit.getClient();
        httpClientPolicy.setAllowChunking(false);

        conduit.setClient(httpClientPolicy);
    }

    /**
     * Initializes the default configurations for ssl, http basic authentication and a proxy.
     * If no properties were set previously, the standard properties of the respective
     * configurations will be used.
     */
    public void initialize() {
        HTTPConduit conduit;
        URL url;

        url = this.url;

        this.proxyHelper.initialize();
        this.basicAuthHelper.initialize();

        final boolean isSSLProtocol = WSClient.HTTPS.equals(this.url.getProtocol());
        if (isSSLProtocol) {
            this.sslHelper.initialize();

            url = this.sslHelper.getLocalWsdlUrl(this.url);
        }

        this.client = createClient(url, this.classloader);

        conduit = (HTTPConduit) this.client.getConduit();

        this.proxyHelper.enable(this.client);
        this.basicAuthHelper.enable(this.client);

        if (isSSLProtocol) {
          this.sslHelper.enable(this.client);
        }

        this.mtomHelper.enable(this.client);

        configureHttpClientPolicy(conduit);
    }

    /**
     * Set the properties of the proxy.
     *
     * @param proxyProperties The map containing the properties.
     *
     * @see ProxyHelper#setProperties(Map)
     */
    public void setProxyProperties(Map<String, String> proxyProperties){
        this.proxyHelper.setProperties(proxyProperties);
    }

    /**
     * Set the properties of the ssl connection.
     *
     * @param sslProperties The map containing the properties.
     *
     * @see SSLHelper#setProperties(Map)
     */
    public void setSSLProperties(Map<String, String> sslProperties){
        this.sslHelper.setProperties(sslProperties);
    }

    /**
     * Set the properties of the basic authentication.
     *
     * @param name     The username
     * @param password The password
     *
     * @see BasicAuthenticationHelper#setBasicAuthentication(String, String)
     */
    public void setBasicAuthentication(String name, String password) {
        this.basicAuthHelper.setBasicAuthentication(name, password);
    }

    /**
     * @param isMtom <code>true</code> if mtom is enabled, otherwise <code>false</code>.
     */
    public void setMtom(boolean isMtom) {
        this.mtomHelper.setMtom(isMtom);
    }

    /**
     * @param timeout The timeout value in milliseconds.
     */
    public void setConnectionTimeout(long timeout) {
        this.connectionTimeoutHelper.setConnectionTimeout(timeout);
    }

}
