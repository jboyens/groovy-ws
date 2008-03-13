package groovyx.net.ws.testArray;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.service.model.BindingOperationInfo;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;


/**
 * Created by IntelliJ IDEA.
 * User: alleon
 * Date: Dec 4, 2007
 * Time: 12:20:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestArraysWithDynamicClient {
    private Client client = null;

    void execute(String myURL) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        client = DynamicClientFactory.newInstance().createClient(myURL, Thread.currentThread().getContextClassLoader());

        client.getOutInterceptors().add(new LoggingOutInterceptor());
        client.getInInterceptors().add(new LoggingInInterceptor());

        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        HTTPClientPolicy httpClientPolicy = conduit.getClient();
        httpClientPolicy.setAllowChunking(false);
        conduit.setClient(httpClientPolicy);

//        QName qname = new QName(client.getEndpoint().getService().getName().getNamespaceURI(), "getPlaceList");
//        BindingOperationInfo op = client.getEndpoint().getEndpointInfo().getBinding().getOperation(qname);

//        Object obj = Thread.currentThread().getContextClassLoader().loadClass("com.strikeiron.GetTeamInfoByCity").newInstance();
//        Class clazz = obj.getClass();
//        for (Field fi:clazz.getDeclaredFields()) {
//            System.out.println(fi.getName());
//        }
//
//        Field af = null;
//        af = clazz.getDeclaredField("userID");
//        af.setAccessible(true);
//        af.set(obj, "guillaume.alleon@laposte.net");
//
//        af = clazz.getDeclaredField("password");
//        af.setAccessible(true);
//        af.set(obj, "galleon2");
//
//        af = clazz.getDeclaredField("city");
//        af.setAccessible(true);
//        af.set(obj, "New York");

//        Object[] objTab = new Object[]{"mountain view", "ca", "us"};

//        System.out.println(op.isUnwrappedCapable());

        try {
//            NFL WebService
//            Object[] response = client.invoke("GetTeamInfoByCity", "guillaume.alleon@laposte.net", "galleon2", "New York");
            Object[] response = client.invoke("GetPlaceList", "mountain view", 5, true);
//            Object[] response = client.invoke("ConvertPlaceToLonLatPt", objTab);

            // TODO Parse the answer

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TestArraysWithDynamicClient tdc = new TestArraysWithDynamicClient();
        try {
            System.out.println("Entering Main");
            tdc.execute("http://terraservice.net/TerraService.asmx?WSDL");
//          tdc.execute("http://terraservice.net/TerraService.asmx?WSDL");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchFieldException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}