package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

import groovyx.net.ws.gdemo.services.AnnotatedService

class AnnotatedServiceTest extends GroovyTestCase {
    private myService = AnnotatedService.getName()
    private myServiceUrl = "http://localhost:9000/"+myService

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

    void testResourceAnnotation() {
        println proxy.getSession()
        assertNull proxy.getSession()
    }

    void tearDown() {
        server.stop()
    }
}
