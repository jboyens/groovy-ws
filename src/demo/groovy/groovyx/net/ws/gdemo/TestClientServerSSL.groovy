package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient
import groovyx.net.ws.gdemo.services.MathService

myServiceUrl = "https://localhost:9000/"+MathService.getName()

//******************************************************************
println "Test 1: Private SSL configuration"

mapServer = [
             "https.keystore":"certs/myserverstore.jks",
             "https.keystore.pass":"basile",
             "https.truststore":"",
             "https.truststore.pass":"",
             ]

server = new WSServer(myServiceUrl)
server.setSSL(mapServer)
server.start()
println "server started"

//TODO: Change behavior of mssl: empty map by default, and add given key
mapClient = [
             "https.keystore":"",
             "https.keystore.pass":"",
             "https.truststore":"certs/truststore.jks",
             "https.truststore.pass":"basile",
             ]
proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
proxy.setSSL(mapClient)
proxy.initialize()

addition = proxy.add(2.0 as double, 5.0 as double)
square = proxy.square(4.0 as double)
assert addition == 7.0
assert square == 16.0
println "$addition, $square"

server.stop()

println "Test 1 successful "
println ""



//****************************************************************
println "Test 2: Private SSL configuration with client authentication"

mapServer = [
             "https.keystore":"certs/myserverstore.jks",
             "https.keystore.pass":"basile",
             "https.truststore":"certs/truststore.jks",
             "https.truststore.pass":"basile",
             ]

server = new WSServer(myServiceUrl)
server.setSSL(mapServer)
server.setClientAuthentication(true)
server.start()

mapClient = [
             "https.keystore":"certs/myclientstore.jks",
             "https.keystore.pass":"basile",
             "https.truststore":"certs/truststore.jks",
             "https.truststore.pass":"basile",
             ]

proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
proxy.setSSL(mapClient)
proxy.initialize()

addition = proxy.add(54.0 as double, 98.0 as double)
square = proxy.square(5.0 as double)
assert addition == 152.0
assert square == 25.0
println "$addition, $square"

server.stop()

println "Test 2 successful "
println ""



//****************************************************************
println "Test 3: Private SSL configuration with java system properties"


System.setProperty("https.keystore", "certs/myserverstore.jks")
System.setProperty("https.keystore.pass", "basile")
System.setProperty("https.truststore", "certs/truststore.jks")
System.setProperty("https.truststore.pass", "basile")

myServiceUrl = "https://localhost:9000/"+MathService.getName()
	
server = new WSServer(myServiceUrl)
server.start();

System.setProperty("https.keystore", "certs/myclientstore.jks")
System.setProperty("https.keystore.pass", "basile")

proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
proxy.initialize()

addition = proxy.add(1.0 as double, 2.0 as double)
assert addition == 3.0

square = proxy.square(3.0 as double)
assert square == 9.0
server.stop()
println "$addition, $square"
println "Test 3 successfull"

