package groovyx.net.ws.issues

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

class ServerSimpleTest  extends GroovyTestCase {
    void testMail() {
        println "Test 1: Simple http server"
        assertFalse javax.mail.internet.InternetHeaders.class.protectionDomain.codeSource.location.toString().contains("groovyws")

        new AntBuilder().mail(mailhost:'smtp.gmail.com', mailport:'465', ssl:'true', user:'guillaume.alleon', password:'VaPuJek&3', subject:"Hello World") {
	    from(address:"krakosaure@gmail.com")
	    to(address:"guillaume.alleon@gmail.com")
	    message("foo")
        }
    }
}
