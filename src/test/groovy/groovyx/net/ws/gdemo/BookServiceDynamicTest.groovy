package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

import groovyx.net.ws.gdemo.services.BookService

class BookServiceDynamicTest extends GroovyTestCase {
    private myService = BookService.getName()
    private myServiceUrl = "http://localhost:9000/"+myService
    private bookClass = "myservice.Book" 
    private aosClass="org.apache.cxf.arrays.ArrayOfString"
//    private aosClass="groovyx.net.ws.gdemo.services.ArrayOfString"

    private server
    private proxy
    private book
    private aob
    private aos

    void setUp() {
        server = new WSServer(myServiceUrl)
        server.start()
        println "Http server started on port 9000"

        proxy = new WSClient(myServiceUrl+"?wsdl", this.class.classLoader)
        proxy.initialize();
        println "Http client started"

        book = proxy.create(bookClass)
        aos  = proxy.create(aosClass)

        println book.class.name
        book.class.fields.each{println it.name}
        book.class.methods.each{
            println it.name
            println it.returnType.name
        }
        println "---------------"
        aos.class.methods.each{
            println it.name
            println it.returnType.name
        }

        book.title="Groovy in Action"
        book.isbn="1-932394-84-2"
        aos.string=["Dierk"]
        book.authors = aos
        book.year=2007
        proxy.addBook(book)

        book.title="Java Web Services"
        aos.string = ["David A. Chappell", "Tyler Jewell"]
        book.authors=aos
        book.isbn="0-596-00269-6"
        book.year=2002
        proxy.addBook(book)
    }

    void testAddBook() {
        aob = proxy.getMyBooks()
        assert aob.book[0].title == "Groovy in Action"
        aob.book[1].authors.class.fields.each{println it.name}
        println ">"+aob.book[1].title+"<"
        println ">"+aob.book[1].authors.string+"<"
        println ">"+aob.book[1].authors.string[0]+"<"
        println aob.book[1].authors.string.size()
        assert aob.book[1].authors.string.size() == 2
        //assertContains("David A. Chappell",  aob.book[1].authors.string)
        println ">"+aob.book[1].authors.string[0]+"<"
        assertEquals(aob.book[1].authors.string[0], "David A. Chappell")
        println ">"+aob.book[1].authors.string[1]+"<"
        assert aob.book[1].authors.string[1] == "Tyler Jewell"
        //assert aob.book[1].authors.string == ["David A. Chappell", "Tyler Jewell"]
        assert aob.book[0].year == 2007
        assert proxy.findBook("1-932394-84-2").title == "Groovy in Action"
    }

    void tearDown() {
        server.stop()
    }
}
