package groovyx.net.ws;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import groovy.lang.GroovyObjectSupport;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.codehaus.groovy.runtime.InvokerHelper;



public class WSClient extends GroovyObjectSupport {
    private Client client;
    private Map<String,Object> context = new HashMap<String, Object>();

    /**
     * Invoke a method on a gsoap component using the xfire
     * dynamic client </p>
     * <p>Example of Groovy code:</p>
     * <code>
     * client = new SoapClient("http://www.webservicex.net/WeatherForecast.asmx?WSDL")
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
           
            System.out.println("-> " + qname.toString() + "  -  " + op.toString());
            
            Object[] response = client.invoke(op, (Object[])objs, context);
/*
            Object[] response = client.invoke(name, objs);
*/            
            // TODO Parse the answer
            if (response == null){
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
     */
    public WSClient(String URLLocation, ClassLoader cl){
        try {
            client = DynamicClientFactory.newInstance().createClient(URLLocation, cl);

            client.getOutInterceptors().add(new LoggingOutInterceptor());
            client.getInInterceptors().add(new LoggingInInterceptor());

            //context.put("mtom-enabled", Boolean.TRUE);      

            String host     = System.getProperty("http.proxyHost");
            String port     = System.getProperty("http.proxyPort");
            String username = System.getProperty("http.proxy.user");
            String password = System.getProperty("http.proxy.password");

            HTTPConduit conduit = (HTTPConduit) client.getConduit();

            if (host != null) {
                conduit.getClient().setProxyServer(host);
                if (port != null) {
                    conduit.getClient().setProxyServerPort(Integer.parseInt(port));
                }
                if ((username != null) && (password != null)) {
                    conduit.getProxyAuthorization().setUserName(username);
                    conduit.getProxyAuthorization().setPassword(password);
                }
            }

            HTTPClientPolicy httpClientPolicy = conduit.getClient();
            httpClientPolicy.setAllowChunking(false);
            conduit.setClient(httpClientPolicy);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
