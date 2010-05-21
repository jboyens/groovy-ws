package groovyx.net.ws;

import groovy.lang.GroovyObjectSupport;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.codehaus.groovy.runtime.InvokerHelper;

import groovyx.net.ws.cxf.*;
import groovyx.net.ws.exceptions.InvokeException;

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

    /**
     * {@inheritDoc}
     *  
     * @throws RuntimeException if any error occurs it will be wrapped into a RuntimeException
     *  
     * @see groovyx.net.ws.IWSClient#invokeMethod(java.lang.String, java.lang.Object)
     */
    @Override
    public Object invokeMethod(String methodName, Object args) {
        Object[] params = InvokerHelper.asArray(args);

        try {
            BindingOperationInfo operationToBeInvoked = null;

            getLogger().finest("Using SOAP version: " + this.soapHelper.getBinding().getSoapVersion().getVersion());

            for (BindingOperationInfo operation : this.soapHelper.getBinding().getOperations()) {
                QName qname = operation.getName();
                getLogger().finest("available method: " + qname);
                if (methodName.equals(qname.getLocalPart())) {
                    operationToBeInvoked = operation;
                }
            }

            if (operationToBeInvoked == null) {
                throw new NoSuchMethodException(methodName);
            }

            getLogger().finest("Invoke, operation info: " + operationToBeInvoked + ", objs: " + params.toString());

            BindingMessageInfo inputMessageInfo;

            if (!operationToBeInvoked.isUnwrapped()){
                //Operation uses document literal wrapped style.
                inputMessageInfo = operationToBeInvoked.getWrappedOperation().getInput();
            } else {
                inputMessageInfo = operationToBeInvoked.getUnwrappedOperation().getInput();
            }

            List<MessagePartInfo> parts = inputMessageInfo.getMessageParts();
            if (parts.isEmpty()){
                getLogger().finest("parts is empty. No message !");
            } else {
                MessagePartInfo partInfo = parts.get(0); // Input class is Order
                // Get the input class Order
                Class<?> param1Class = partInfo.getTypeClass();

                getLogger().finest("param1 is of Type: "+param1Class.getCanonicalName());
            }
            getLogger().finest("There are "+params.length+" parameters to the call");
            if (params.length > 0) getLogger().finest("First parameter is of type: "+params[0].getClass().getCanonicalName());

            Object[] response;

            if (operationToBeInvoked.isUnwrappedCapable()) {
                operationToBeInvoked = operationToBeInvoked.getUnwrappedOperation();
                getLogger().finest("The operation <"+operationToBeInvoked+"> is Unwrap capable");
                response = this.client.invoke(operationToBeInvoked, params);
            } else {
                operationToBeInvoked = operationToBeInvoked.getWrappedOperation();
                getLogger().finest("The operation <"+operationToBeInvoked+"> is NOT Unwrap capable");
                response = this.client.invoke(operationToBeInvoked, params);
            }

            if (response != null) {
                getLogger().finest("Response: "+response.toString()+" ["+response.getClass().getName()+"]");
                return parseResponse(response);
            }

            return null;
        }
        catch (Exception e)
        {
            getLogger().finest("Could not invoke method." + e);
            throw new InvokeException(e);
        }
    }

    /**
     * Parses the response.
     *
     * @param response The object-array which contains the result
     * @return The modified response.
     */
    protected Object parseResponse(Object[] response) {
        Object toReturn;

        if (response.length == 0) {
            toReturn = response;
        } else if (response.length == 1) {
            toReturn = response[0];
        } else {
            toReturn = new ArrayList<Object>(Arrays.asList(response));
        }

        return toReturn;
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
//            for (Field f:clazz.getFields()) {
//                System.out.println("field: "+f.getName());
//            }
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

    public void changeEndpointAddress(URL newUrl){
        client.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, newUrl.toExternalForm());
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

//                Bus bus = new CXFBusFactory().createBus();
//                JaxWsDynamicClientFactory dynamicClientFactory = JaxWsDynamicClientFactory.newInstance(bus);
//                return dynamicClientFactory.createClient(url.toExternalForm(), (ClassLoader) args[1]);
            }

            throw new IllegalArgumentException("Parameters are not set properly.");
        }

        return null;
    }

}
