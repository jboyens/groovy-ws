package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

import groovyx.net.ws.gdemo.services.MathService
import groovyx.net.ws.gdemo.services.MultiplyService

class ClientMultipleServersTest extends GroovyTestCase {
    private myService = MathService.getName()
    private myServiceUrl = "http://localhost:9000/"+myService
    private myOtherService = MultiplyService.getName()
    private myOtherServiceUrl = "http://localhost:9001/"+myOtherService

    private server
    private otherServer
    private proxy
    private otherProxy

    void test1() {
//  void setUp() {
        server = new WSServer(myServiceUrl)
        server.start()
        println "Http server started on port 9000"

        otherServer = new WSServer(myOtherServiceUrl)
        otherServer.start()
        println "Http server started on port 9001"

        proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
        proxy.initialize()
        println "Http client started"

        otherProxy = new WSClient(myOtherServiceUrl+"?wsdl", this.class.classLoader)
        otherProxy.initialize()
        println "Http client started"
//  }

//  void testTwoServices() {
        assert proxy.add(1.0 as double, 2.0 as double) == 3.0
        assert proxy.square(3.0 as double) == 9.0
        assert otherProxy.multiply(9.0 as double, 2.0 as double) == 18.0
//  }

//  void tearDown() {
        server.stop()
        otherServer.stop()
    }
}
