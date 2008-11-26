package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient
import groovyx.net.ws.gdemo.services.ImageService

import java.io.FileWriter
import javax.activation.DataHandler
import javax.mail.util.ByteArrayDataSource

println "Test: Mtom attachments server and client"

exfile = (args && args[0]) ? args[0] : "lenna_small.tiff"

//	Example file on local file system
imgSource = new File(exfile)
if(!imgSource.exists()){
	println "Example file ${imgSource.absolutePath} not found"
	println "Exiting prematurely"
	System.exit(0)
}
println "Using file ${imgSource.absolutePath}"


// for testing and checking if Mtom is enabled, enable logging with -Dcxf.config.file=build/cxf.xml
myService = ImageService.getName()
myServiceUrl = "http://localhost:9000/"+myService

server = new WSServer(myServiceUrl)
server.setMtom(true)
server.start()
println "Server started"

proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
proxy.setMtom(true)
proxy.initialize()

//Write wsdl on local filesystem
fw = new FileWriter(myService+".wsdl")
new URL(myServiceUrl+"?wsdl").eachLine(){fw.write(it+"\n")}
fw.flush()
fw.close()

println "Read file locally and send it to the server"
// Server read file, client get it and write it on local filesystem
def bb = new File(imgSource.absolutePath).readBytes()
def dh = new DataHandler(new ByteArrayDataSource(bb, "application/octet-stream"))

proxy.putData(dh, "lenna.jpg")
proxy.storeData()

println "Modify image"
proxy.findEdges()

proxy.loadData("lenna.jpg")
image = (Byte[]) proxy.getData()
new FileOutputStream("dl_"+imgSource.name).write(image)
assert image!=null

//Stop server
server.stop()

println "Test successful!"
