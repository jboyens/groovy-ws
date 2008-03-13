package groovyx.net.ws;

import groovy.lang.GroovyObjectSupport;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.xml.namespace.QName;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WSClient extends GroovyObjectSupport {
    private Client client;

    private static final Logger LOG = LogUtils.getL7dLogger(WSClient.class);


    /**
     * Invoke a method on a GroovyWS component using the CXF
     * dynamic client </p>
     * <p>Example of Groovy code:</p>
     * <code>
     * client = new WSClient("http://www.webservicex.net/WeatherForecast.asmx?WSDL", this.classloader)
     * def answer = client.GetWeatherByPlaceName("Seattle")
     * </code>
     *
     * @param name name of the method to call
     * @param args parameters of the method call
     * @return the value returned by the method call
     */
    public Object invokeMethod(String name, Object args) {
        Object[] objs = InvokerHelper.getInstance().asArray(args);

        try {

            QName qname = new QName(client.getEndpoint().getService().getName().getNamespaceURI(), name);
            BindingOperationInfo op = client.getEndpoint().getEndpointInfo().getBinding().getOperation(qname);

            if (op == null) {
                return null;
            }

            Object[] response;

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Invoke, operation info: " + op + ", objs: " + objs.toString());
            }

            if (op.isUnwrappedCapable()) {
                op = op.getUnwrappedOperation();
                response = client.invoke(op, objs);
            } else {
                response = client.invoke(name, objs);
            }

            // TODO Parse the answer
            if (response == null) {
                return null;
            } else {
                return response[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public Object create(String name) {
        Object obj = null;
        try {
            obj = Thread.currentThread().getContextClassLoader().loadClass(name).newInstance();
            //obj = this.getClass().getClassLoader().loadClass(name).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * Create a SoapClient using a URL
     * <p>Example of Groovy code:</p>
     * <code>
     * client = new SoapClient("http://www.webservicex.net/WeatherForecast.asmx?WSDL")
     * </code>
     *
     * @param URLLocation the URL pointing to the WSDL
     * @param cl the classloader
     */
    public WSClient(String URLLocation, ClassLoader cl) {
        try {
            client = DynamicClientFactory.newInstance().createClient(URLLocation, cl);

//            client.getOutInterceptors().add(new LoggingOutInterceptor());
//            client.getInInterceptors().add(new LoggingInInterceptor());

//            context.put("mtom-enabled", Boolean.TRUE);      

            String host = System.getProperty("http.proxyHost");
            String port = System.getProperty("http.proxyPort");
            String proxyUsername = System.getProperty("http.proxy.user");
            String proxyPassword = System.getProperty("http.proxy.password");

            HTTPConduit conduit = (HTTPConduit) client.getConduit();

            if (host != null) {
                conduit.getClient().setProxyServer(host);
                if (port != null) {
                    conduit.getClient().setProxyServerPort(Integer.parseInt(port));
                }
                if ((proxyUsername != null) && (proxyPassword != null)) {
                    conduit.getProxyAuthorization().setUserName(proxyUsername);
                    conduit.getProxyAuthorization().setPassword(proxyPassword);
                }
            }

            String username = System.getProperty("http.user");
            String password = System.getProperty("http.password");

            if ((username != null) && (password != null)) {
                LOG.fine("Set Auth header with: " + username + "/" + password);
                AuthorizationPolicy auth = conduit.getAuthorization();
                auth.setUserName(username);
                auth.setPassword(password);
            }

            //HttpBasicAuthSupplier hbs = new HttpBasicAuthSupplier("me", "password");
            //HttpAuthorization ha =

            HTTPClientPolicy httpClientPolicy = conduit.getClient();
            httpClientPolicy.setAllowChunking(false);
            conduit.setClient(httpClientPolicy);

            Endpoint ep = client.getEndpoint();

            // Settings of the TLSClientParameters for using https
            //
            if (ep.getEndpointInfo().getAddress().startsWith("https")){
                LOG.fine("Setting TLSClientParameters");
                TLSClientParameters tlsParams = new TLSClientParameters();
                tlsParams.setSecureSocketProtocol("SSL");

                FiltersType filters = new FiltersType();
                filters.getInclude().add(".*_EXPORT_.*");
                filters.getInclude().add(".*_EXPORT1024_.*");
                filters.getInclude().add(".*_WITH_DES_.*");
                filters.getInclude().add(".*_WITH_NULL_.*");
                filters.getInclude().add(".*_DH_anon_.*");
                filters.getInclude().add("SSL_RSA_WITH_RC4_128_MD5");
                filters.getInclude().add("SSL_RSA_WITH_RC4_128_SHA");

                tlsParams.setCipherSuitesFilter(filters);

                conduit.setTlsClientParameters(tlsParams);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
