package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

import groovyx.net.ws.gdemo.services.BookService

println "Test: Simple dynamic test"

myService = BookService.getName()

// namespace lower case
// package must be defined in the Aegis binding. 
// apparenty, cannot be the same as the service classes
bookClass = "myservice.Book" 
aosClass="org.apache.cxf.arrays.ArrayOfString"

myServiceUrl = "http://localhost:9000/"+myService

server = new WSServer(myServiceUrl)
server.start()

def fos = new FileWriter(myService+".wsdl")
hop=new URL(myServiceUrl+"?wsdl")
hop.eachLine(){ fos.write(it+"\n")}
fos.flush(); fos.close()

proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
proxy.create()

//Adding books ...
mybooks = proxy.getMyBooks()


book = proxy.create(bookClass)
aos = proxy.create(aosClass)

book.title="Groovy in Action"
book.isbn="1-932394-84-2"
aos.string=["Dierk"]
book.authors = aos
book.year=2007
proxy.addBook(book)

book = proxy.create(bookClass)
book.title="Java Web Services"
aos.string = ["David A. Chappell", "Tyler Jewell"]
book.authors=aos
book.isbn="0-596-00269-6"
book.year=2002
proxy.addBook(book)

aob = proxy.getMyBooks()
assert aob.book[0].title == "Groovy in Action"
assert aob.book[1].authors.string == ["David A. Chappell", "Tyler Jewell"]
assert aob.book[0].year == 2007
assert proxy.findBook("1-932394-84-2").title == "Groovy in Action"
println aob.book[0].title

println "Test successful"
server.stop()
