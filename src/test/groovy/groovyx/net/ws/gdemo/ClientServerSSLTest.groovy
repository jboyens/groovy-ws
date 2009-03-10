package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient
import groovyx.net.ws.gdemo.services.MathService

class ClientServerSSLTest extends GroovyTestCase {
    private myService = MathService.getName()
    private myServiceUrl = "https://localhost:9100/"+myService

    private server

    void test1() {
        println "Test 1: Private SSL configuration"

//      Map<String, String> mapServer = [
//                                       "https.keystore":"src/test/resources/certs/cherry.jks",
//                                       "https.keystore.pass":"password",
//                                       "https.truststore":"src/test/resources/certs/truststore.jks",
//                                       "https.truststore.pass":"password"
//                                      ]
        Map<String, String> mapServer = [
                                         "https.keystore":"src/test/resources/certs/myserverstore.jks",
                                         "https.keystore.pass":"basile",
                                         "https.truststore":"",
                                         "https.truststore.pass":""
                                        ]

        println this.getClass().protectionDomain.codeSource.location.path
        println System.getProperty("user.dir")

        server = new WSServer(myServiceUrl)
        server.setSSL(mapServer)
        server.start()
        println "server started"

        // TODO: Change behavior of mssl: empty map by default, and add given key
        Map<String, String> mapClient = [
//                                       "https.keystore":"src/test/resources/certs/cherry.jks",
//                                       "https.keystore.pass":"password",
//                                       "https.truststore":"src/test/resources/certs/truststore.jks",
//                                       "https.truststore.pass":"password"
                                         "https.keystore":"",
                                         "https.keystore.pass":"",
                                         "https.truststore":"src/test/resources/certs/truststore.jks",
                                         "https.truststore.pass":"basile"
                                        ]

        def proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
        proxy.setSSLProperties(mapClient)
        proxy.initialize()

        assert proxy.add(2.0 as double, 5.0 as double) == 7.0
        assert proxy.square(4.0 as double) == 16.0

        //server.stop()
    }

    void test2() {
        println "Test 2: Private SSL configuration with client authentication"

        Map<String, String> mapServer = [
                                         "https.keystore":"src/test/resources/certs/myserverstore.jks",
                                         "https.keystore.pass":"basile",
                                         "https.truststore":"src/test/resources/certs/truststore.jks",
                                         "https.truststore.pass":"basile"
                                        ]

        server = new WSServer(myServiceUrl)
        server.setSSL(mapServer)
        server.setClientAuthentication(true)
        server.start()
        println "server started"

        Map<String, String> mapClient = [
                                         "https.keystore":"src/test/resources/certs/myclientstore.jks",
                                         "https.keystore.pass":"basile",
                                         "https.truststore":"src/test/resources/certs/truststore.jks",
                                         "https.truststore.pass":"basile"
                                        ]

        def proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
        proxy.setSSLProperties(mapClient)
        proxy.initialize()

        assert proxy.add(54.0 as double, 98.0 as double) == 152.0
        assert proxy.square(5.0 as double) == 25.0

        //server.stop()
    }

    void test3() {
        println "Test 3: Private SSL configuration with java system properties"

        System.setProperty("https.keystore", "src/test/resources/certs/myserverstore.jks")
        System.setProperty("https.keystore.pass", "basile")
        System.setProperty("https.truststore", "src/test/resources/certs/truststore.jks")
        System.setProperty("https.truststore.pass", "basile")

        server = new WSServer(myServiceUrl)
        server.start()

        System.setProperty("https.keystore", "src/test/resources/certs/myclientstore.jks")
        System.setProperty("https.keystore.pass", "basile")

        def proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
        proxy.initialize()

        assert proxy.add(1.0 as double, 2.0 as double) == 3.0
        assert proxy.square(3.0 as double) == 9.0

        //server.stop()
    }

    void tearDown(){
        server.stop()
    }

}
