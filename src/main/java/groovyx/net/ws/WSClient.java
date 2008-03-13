package groovyx.net.ws;

import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import groovy.lang.GroovyObjectSupport;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.*;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.charset.Charset;

public class WSClient extends GroovyObjectSupport {
    private Client client;
    private Map<String, String> credential = new HashMap<String, String>();

    private static final Logger LOG = LogUtils.getL7dLogger(WSClient.class);

    /**
     * List all the operations available on the service
     *
     */
    public void getClientCode(OutputStream os) {
        OutputStreamWriter osw = new OutputStreamWriter(os, Charset.forName("utf-8"));
        ServiceInfo si = client.getEndpoint().getEndpointInfo().getService();
        ServiceGroovyBuilder sgb;
        sgb = new ServiceGroovyBuilder(si);
        sgb.walk();
        String serviceGroovyScript = sgb.getCode();
        try {
            osw.append(serviceGroovyScript);
            osw.flush();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Set up the WS-Security parameters
     *
     * @param map user/password pair using the method
     *
     * @throws Exception
     */
    public void setCredential(Map<String, String> map) throws Exception {

        this.credential = map;
        if (credential.size() != 1) throw new Exception("Cannot have multiple ceredentials");

        Endpoint ep = client.getEndpoint();
        Map outProps = new HashMap();

        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        System.out.println("Set WSHandler for " + credential.keySet().toArray()[0]);
        outProps.put(WSHandlerConstants.USER, map.keySet().toArray()[0]);
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientPasswordHandler.class.getName());

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        ep.getOutInterceptors().add(wssOut);
        ep.getOutInterceptors().add(new SAAJOutInterceptor());
    }

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
        Object[] objs = InvokerHelper.asArray(args);

        try {

            QName qname = new QName(client.getEndpoint().getService().getName().getNamespaceURI(), name);
            BindingOperationInfo op = client.getEndpoint().getEndpointInfo().getBinding().getOperation(qname);

            if (op == null) {
                return null;
            }

            Object[] response = null;

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Invoke, operation info: " + op + ", objs: " + objs);
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
     * @param InputStream the input stream pointing to the WSDL
     * @param cl the classloader
     */
    /*
    public WSClient(InputStream is, ClassLoader cl) {
        File temp = new File.createTempFile("plop",".txt");
        temp.deleteOnExit();

        byte bytes[] = null;
        try {
            bytes = new byte[is.available()];

            OutputStream os = new FileOutputStream(temp);
            os.write(is.read(bytes));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        new WSClient(temp.getAbsolutePath(), cl);
    }
    */
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

            //client.getOutInterceptors().add(new LoggingOutInterceptor());
            //client.getInInterceptors().add(new LoggingInInterceptor());

            //context.put("mtom-enabled", Boolean.TRUE);

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
            Service service = ep.getService();

            EndpointInfo epfo = null;
            for (ServiceInfo svcfo : service.getServiceInfos()) {
                for (EndpointInfo e : svcfo.getEndpoints()) {
                    BindingInfo bfo = e.getBinding();
                    System.out.println("BindingInfo = " + bfo);

                    if (bfo.getBindingId().equals("http://schemas.xmlsoap.org/wsdl/soap/")) {
                        for (Object o : bfo.getExtensors().get()) {
                            System.out.println("o = " + o);
                            if (o instanceof SOAPBindingImpl) {
                                SOAPBindingImpl soapB = (SOAPBindingImpl) o;
                                if (soapB.getTransportURI().equals("http://schemas.xmlsoap.org/soap/http")) {
                                    epfo = e;
                                    break;
                                }
                            }
                        }

                    }
                }
            }

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
/*
            Endpoint ep = client.getEndpoint();
            Map outProps = new HashMap();

            outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
            outProps.put(WSHandlerConstants.USER, "MCFCDEVXML");
            outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);
            outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientPasswordHandler.class.getName());

            WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
            ep.getOutInterceptors().add(wssOut);
            ep.getOutInterceptors().add(new SAAJOutInterceptor());

        } catch (MalformedURLException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
*/
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private class ClientPasswordHandler implements CallbackHandler {

        /**
         * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
         */

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof WSPasswordCallback) {
                    WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                    // set the password given a username
                    System.out.println("in callback " + callbacks[i] + " for " + pc.getIdentifer());
                    String user = pc.getIdentifer();

                    //String pass = userpas.get(pc.getIdentifer());
                    System.out.println("for user " + user + " password [" + credential.get(user) + "] has been found");

                    pc.setPassword(credential.get(user));
                } else {
                    throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
                }
            }
        }

    }
}
