package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

import groovyx.net.ws.gdemo.services.BookService

class ServerSimpleTest  extends GroovyTestCase {
    void testServer() {
        println "Test 1: Simple http server"

        def myService = BookService.getName()
        def myServiceUrl = "http://localhost:9000/"+myService

        def server = new WSServer(myServiceUrl)
        server.start()
        //Get WSDL and print it in a file

        assertNotNull new URL(myServiceUrl+"?wsdl")

        server.stop();
    }
}
