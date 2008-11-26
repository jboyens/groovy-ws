package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

import groovyx.net.ws.gdemo.services.BookService

println "Test 1: Simple http server"

myService = BookService.getName()
myServiceUrl = "http://localhost:9000/"+myService

server = new WSServer(myServiceUrl)
server.start()
//Get WSDL and print it in a file

fos = new FileWriter(myService+".wsdl")
new URL(myServiceUrl+"?wsdl").eachLine(){ fos.write(it+"\n"); println it}
fos.flush(); fos.close()

server.stop();

println "Test successful"
