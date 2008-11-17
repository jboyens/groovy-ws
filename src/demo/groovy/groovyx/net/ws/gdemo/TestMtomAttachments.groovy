package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient
import groovyx.net.ws.gdemo.services.DataService

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


//For testing and checking if Mtom is enabled, enable logging with -Dcxf.config.file=build/cxf.xml
myService = DataService.getName()
myServiceUrl = "http://localhost:9000/"+myService


server = new WSServer(myServiceUrl)
server.setMtom(true)
server.start()
println "Server started"

proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
proxy.setMtom(true)
proxy.create()

//Write wsdl on local filesystem
fw = new FileWriter(myService+".wsdl")
new URL(myServiceUrl+"?wsdl").eachLine(){fw.write(it+"\n")}
fw.flush(); fw.close()

println "Mtom test: Server --> Client"
//Server read file, client get it and write it on local filesystem
proxy.loadData(imgSource.absolutePath)
image = (Byte[]) proxy.getData()
new FileOutputStream("dl_"+imgSource.name).write(image)
assert image!=null

println "Mtom test: Client --> Server"
//Client loads a file, and uploads it to the server
byte[] data = imgSource.readBytes()
DataHandler dh = new DataHandler(new ByteArrayDataSource(data, "application/octet-stream"))
bbname = "ul_"+imgSource.name
proxy.saveData(dh, bbname)

//Server operates on the file (use swirl from ImageMagick)
res = proxy.opData()
assert res == bbname

println "Mtom test: get back a modified file Server --> Client"
//Get the image and write it to the local filesystem
image = (byte[]) proxy.getData()
new FileOutputStream("dlul_"+imgSource.name).write(image)
assert image!=null
assert data!=image

println "dlul_"+imgSource.name+" created! With Mtom of course ;)"
//Stop server
server.stop()

println "Test successful!"
