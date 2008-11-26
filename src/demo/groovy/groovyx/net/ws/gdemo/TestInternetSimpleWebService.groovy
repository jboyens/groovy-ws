package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

//TerraServer

proxy = new WSClient("http://terraservice.net/TerraService.asmx?WSDL", this.class.classLoader)
proxy.initialize()

place = proxy.create("com.terraserver_usa.terraserver.Place")
place.city="mountain view"
place.state="ca"
place.country="us"
result=proxy.ConvertPlaceToLonLatPt(place)
assert result.lon==-122.08000183105469
assert result.lat==37.400001525878906
println "longitude=${result.lon}, latitude=${result.lat}"
println "Test successful"
