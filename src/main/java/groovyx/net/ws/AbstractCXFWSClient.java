package groovyx.net.ws;

import groovy.lang.GroovyObjectSupport;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.codehaus.groovy.runtime.InvokerHelper;

import groovyx.net.ws.cxf.*;

/**
 * @author <a href="mailto:guillaume.alleon@gmail.com">Tog</a>
 * @author <a href="mailto:groovy@courson.de">Dennis Bayer</a>
 * @author <a href="mailto:basile.clout@gmail.com">Basile Clout</a>
 * @version 0.5
 */
public abstract class AbstractCXFWSClient extends GroovyObjectSupport implements IWSClient<Client> {
    /**
     * The webservice-client
     */
    protected Client client;

    /**
     * A helper to set the ssl configuration.
     */
    protected SSLHelper sslHelper;

    /**
     * A helper to set the proxy configuration.
     */
    protected ProxyHelper proxyHelper;

    /**
     * A helper to set the basic auth configuration.
     */
    protected BasicAuthenticationHelper basicAuthHelper;

    /**
     * A helper to set the connection timeout.
     */
    protected ConnectionTimeoutHelper connectionTimeoutHelper;

    /**
     * A helper to set the MTOM configuration.
     */
    protected MtomHelper mtomHelper;

    /**
     * A helper to set the SOAP configuration.
     */
    protected SoapHelper soapHelper;

    /**
     * @return The logger for the class
     */
    protected Logger getLogger() {
        return LogUtils.getL7dLogger(getClass());
    }

    /* (non-Javadoc)
     * @see groovyx.net.ws.IWSClient#invokeMethod(java.lang.String, java.lang.Object)
     */
    @Override
    public Object invokeMethod(String name, Object args) {
        Object[] objs = InvokerHelper.asArray(args);

        try {
            QName qname = new QName(getServiceNamespaceURI(), name);
            BindingOperationInfo op = getBindingOperationInfo(qname);

            if (op == null) {
                return null;
            }

            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().warning("Invoke, operation info: " + op + ", objs: " + objs.toString());
            }

            Object[] response;

            if (op.isUnwrappedCapable()) {
                op = op.getUnwrappedOperation();
                response = this.client.invoke(op, objs);
            } else {
                response = this.client.invoke(name, objs);
            }

            if (response != null) {
                return parseResponse(response);
            }

            return null;
        }
        catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not invoke method.", e);
            return null;
        }

    }

    /**
     * Parses the response.
     *
     * @param response The object-array which contains the result
     * @return The modified response.
     */
    protected Object parseResponse(Object[] response) {
        return response[0];
    }

    /**
     * Returns the BindingOperationInfo for the given QName.
     *
     * @param qname The {@link QName} of the operation.
     * @return A {@link BindingOperationInfo}.
     * @see AbstractCXFWSClient#invokeMethod(String, Object)
     */
    protected final BindingOperationInfo getBindingOperationInfo(QName qname) {
        getLogger().warning(" Using SOAP version: " + this.soapHelper.getBinding().getSoapVersion().getVersion());
        return this.soapHelper.getBinding().getOperation(qname);
    }

    /**
     * Returns the ServiceNamespaceURI of the currently set service.
     *
     * @return A string containing the ServiceNamespaceURI.
     * @see AbstractCXFWSClient#invokeMethod(String, Object)
     */
    protected final String getServiceNamespaceURI() {
        return this.client.getEndpoint().getService().getName().getNamespaceURI();
    }

    /**
     * Creates an object for the given classname using the classloader of the
     * current thread.
     *
     * @param classname The classname of the object which should be created.
     * @return An instance of the class.
     */
    public Object create(String classname) {
        Object obj = null;

        try {
            obj = Thread.currentThread().getContextClassLoader().loadClass(classname).newInstance();
        }
        catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return obj;
    }

    /**
     * The cxf-implementation of {@link IWSClient}.
     *
     * @param args : url (The url of the wsdl-file), cl (The classloader)
     * @see groovyx.net.ws.IWSClient#createClient(java.lang.Object[])
     */
    public Client createClient(Object... args) {

//        if (classLoader == null) {
//            classLoader = Thread.currentThread().getContextClassLoader();
//        }

        if (args[0] instanceof URL) {
            URL url = (URL) args[0];

            if (args.length == 2) {
                return DynamicClientFactory.newInstance().createClient(url.toExternalForm(), (ClassLoader) args[1]);
            }

            throw new IllegalArgumentException("Parameters are not set properly.");
        }
        return null;
    }

}
