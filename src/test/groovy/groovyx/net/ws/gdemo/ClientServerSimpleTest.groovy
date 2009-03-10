package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

import groovyx.net.ws.gdemo.services.MathService

class ClientServerSimpleTest extends GroovyTestCase {
    private myService = MathService.getName()
    private myServiceUrl = "http://localhost:9005/"+myService

    private server
    private proxy

    void setUp() {
        server = new WSServer(myServiceUrl)
        server.start()
        println "Http server started on port 9000"

        proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
        proxy.initialize()
        println "Http client started"
    }

    void testAdd() {
        assert proxy.add(1.0 as double, 2.0 as double) == 3.0
    }

    void testSquare() {
        assert proxy.square(3.0 as double) == 9.0
    }

    void tearDown() {
        server.stop()
        server = null
        proxy  = null
    }
}
