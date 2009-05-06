package groovyx.net.ws;

import groovy.lang.GroovyObjectSupport;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Field;

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
 * @since 0.5
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
            QName qname = null;
            BindingOperationInfo op = null;

            getLogger().warning(" Using SOAP version: " + this.soapHelper.getBinding().getSoapVersion().getVersion());

            for (BindingOperationInfo boi:this.soapHelper.getBinding().getOperations()){
                qname = boi.getName();
                System.out.println("-> " + qname);
                if (qname.getLocalPart().equals(name)){
                    op = boi;
                }
            }

            if (op == null) {
                throw new NoSuchMethodException(name);
            }

            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().warning("Invoke, operation info: " + op + ", objs: " + objs.toString());
            }

            Object[] response;

            if (op.isUnwrappedCapable()) {
                op = op.getUnwrappedOperation();
                response = this.client.invoke(op, objs);
            } else {
                response = this.client.invoke(qname, objs);
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
     * Creates an object for the given classname using the classloader of the
     * current thread.
     *
     * @param classname The classname of the object which should be created.
     * @return An instance of the class.
     * @throws IllegalAccessException
     */
    public Object create(String classname) throws IllegalAccessException {

        if (classname == null) {
            throw new IllegalArgumentException("Must provide the class name");
        }

        Class clazz = null;
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(classname);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        assert clazz != null;
        if (clazz.isEnum()){
            for (Field f:clazz.getFields()) {
                System.out.println("field: "+f.getName());
            }
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
     * The cxf-implementation of {@link IWSClient}.
     *
     * @param args : url (The url of the wsdl-file), cl (The classloader)
     * @see groovyx.net.ws.IWSClient#createClient(java.lang.Object[])
     */
    public Client createClient(Object... args) {

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
