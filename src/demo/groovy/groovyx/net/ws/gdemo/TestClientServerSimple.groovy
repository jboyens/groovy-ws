package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

import groovyx.net.ws.gdemo.services.MathService

myService = MathService.getName()
myServiceUrl = "http://localhost:9000/"+myService

server = new WSServer(myServiceUrl)
server.start()
println "Http server started on port 9000"

proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
proxy.create();
println "Http client started"

addition = proxy.add(1.0 as double, 2.0 as double)
assert addition == 3.0
square = proxy.square(3.0 as double)
assert square == 9.0
println addition
println square

server.stop()
println "Test successful!"
