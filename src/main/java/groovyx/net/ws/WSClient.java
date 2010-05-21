package groovyx.net.ws;

import groovyx.net.ws.cxf.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

/**
 * A Webservice client using the cxf-framework dynamic client factory.
 *
 * @author <a href="mailto:groovy@courson.de">Dennis Bayer</a>
 * @author <a href="mailto:guillaume.alleon@gmail.com">Tog</a>
 * 
 * @since 0.1
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
        this.soapHelper              = new SoapHelper();
    }

    /**
     * Default constructor.
     *
     * @param wsdlLocation The url of the wsdl-file
     * @param classloader  The classoader
     * @param soapVersion  The preferred SOAP version
     */
    public WSClient(String wsdlLocation, ClassLoader classloader, SoapVersion soapVersion) {
        this(wsdlLocation, classloader);
        this.soapHelper.setPreferredSoapVersion(soapVersion);
    }


    /**
     * Configures the configuration for the transport of the client connection.
     *
     * @param conduit The conduit that need to be modified
     */
    private void configureHttpClientPolicy(HTTPConduit conduit) {
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

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        this.client = createClient(url, this.classloader);
        this.classloader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(oldLoader);

        this.soapHelper.enable(this.client);
        this.proxyHelper.enable(this.client);
        this.basicAuthHelper.enable(this.client);

        if (isSSLProtocol) {
          this.sslHelper.enable(this.client);
        }

        this.mtomHelper.enable(this.client);

        conduit = (HTTPConduit) this.client.getConduit();
        configureHttpClientPolicy(conduit);
    }
    
    public Object create(String classname) throws IllegalAccessException {
    	
    	if (classname == null) {
            throw new IllegalArgumentException("Must provide the class name");
        }
    	
    	Class clazz = null;
    	try {
    		clazz = classloader.loadClass(classname);
    	} catch (Exception e) {
    		e.printStackTrace();    		
    	}
    	
    	assert clazz != null;
        if (clazz.isEnum()){
            return clazz;
        }

        Object obj = null;

        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return obj;
    }

    /**
     * Set the properties of the proxy.
     *
     * @param proxyProperties The map containing the properties.
     *
     * @see ProxyHelper#setProperties(Map)
     */
    public void setProxyProperties(Map<String, String> proxyProperties) {
        this.proxyHelper.setProperties(proxyProperties);
    }

    /**
     * Set the properties of the ssl connection.
     *
     * @param sslProperties The map containing the properties.
     *
     * @see SSLHelper#setProperties(Map)
     */
    public void setSSLProperties(Map<String, String> sslProperties) {
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

    /**
     * @param soapVersion The SOAP version as define in {@link SoapVersion}.
     */
    public void setPreferredSoapVersion(SoapVersion soapVersion) {
        this.soapHelper.setPreferredSoapVersion(soapVersion);
    }
    
}
