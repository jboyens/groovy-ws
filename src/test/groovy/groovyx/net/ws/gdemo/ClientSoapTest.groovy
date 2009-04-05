package groovyx.net.ws.gdemo
/**
 * Created by IntelliJ IDEA.
 * User: alleon
 * Date: Mar 30, 2009
 * Time: 8:47:21 AM
 * To change this template use File | Settings | File Templates.
 */
import groovyx.net.ws.WSClient
import groovyx.net.ws.cxf.SoapVersion



public class ClientSoapTest extends GroovyTestCase {

    String wsdl = "http://www.w3schools.com/webservices/tempconvert.asmx?WSDL"

    void testTempConvert() {
        def proxy = new WSClient(wsdl, getClass().getClassLoader())

        proxy.initialize();

        assertEquals(proxy.CelsiusToFahrenheit(0) as double, 32.0)
    }

    void testTempConvert12() {
        def proxy = new WSClient(wsdl, getClass().getClassLoader())

        proxy.setPreferredSoapVersion(SoapVersion.SOAP_1_2)
        proxy.initialize();

        assertEquals(proxy.CelsiusToFahrenheit(0) as double, 32.0)
    }
}    