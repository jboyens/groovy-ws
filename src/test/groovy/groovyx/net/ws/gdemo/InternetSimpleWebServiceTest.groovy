package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

class InternetSimpleWebServiceTest extends GroovyTestCase {

    void testTerraService() {
        def proxy = new WSClient("http://terraservice.net/TerraService.asmx?WSDL", this.class.classLoader)
        proxy.initialize()

        def place = proxy.create("com.terraserver_usa.terraserver.Place")
        place.city="mountain view"
        place.state="ca"
        place.country="us"
        def result = proxy.ConvertPlaceToLonLatPt(place)
        assert result.lon == -122.08000183105469
        assert result.lat ==   37.400001525878906
    }
}
