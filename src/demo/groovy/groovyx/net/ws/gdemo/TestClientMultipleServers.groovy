package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

import groovyx.net.ws.gdemo.services.MathService
import groovyx.net.ws.gdemo.services.MultiplyService

myService = MathService.getName()
myServiceUrl = "http://localhost:9000/"+myService

myOtherService = MultiplyService.getName()
myOtherServiceUrl = "http://localhost:9001/"+myOtherService

server = new WSServer(myServiceUrl)
server.start()
println "Http server started on port 9000"

otherServer = new WSServer(myOtherServiceUrl)
otherServer.start()
println "Http server started on port 9001"

proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
proxy.initialize();
println "Http client started"

otherProxy = new WSClient(myOtherServiceUrl+"?wsdl", this.class.classLoader)
otherProxy.initialize();
println "Http client started"

addition = proxy.add(1.0 as double, 2.0 as double)
assert addition == 3.0
square = proxy.square(3.0 as double)
assert square == 9.0
multiply = otherProxy.multiply(square as double, 2.0 as double)
assert multiply == 18.0

server.stop()
otherServer.stop()
println "Test successful!"
