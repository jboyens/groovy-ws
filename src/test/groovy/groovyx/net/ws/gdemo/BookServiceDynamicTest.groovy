package groovyx.net.ws.gdemo

import groovyx.net.ws.WSServer
import groovyx.net.ws.WSClient

import groovyx.net.ws.gdemo.services.BookService
import org.apache.cxf.bus.CXFBusFactory
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory

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
        assertNotNull book

        book.title="Groovy in Action"
        book.isbn="1-932394-84-2"
        book.author = "Dierk"
        book.year=2007
        proxy.addBook(book)

        book.title="Java Web Services"
        book.author = "David A. Chappell"
        book.isbn="0-596-00269-6"
        book.year=2002
        proxy.addBook(book)
      
        println "Set-up finished"
    }

    void testAddBook() {
        println "Entering testAddBook ..."

//        def bus = new CXFBusFactory().createBus()
//        def dynamicClientFactory = JaxWsDynamicClientFactory.newInstance(bus)
//        def client = dynamicClientFactory.createClient(myServiceUrl+"?wsdl")
//        Object[] result = client.invoke("getMyBooks", new Object[0])
//        assert result.length == 2
//        assertTrue resutlt[0] instanceof List

        aob = proxy.getMyBooks()
        println aob
        //println aob.class.name
        //println aob.book[0].title
        assert aob.book[0].title == "Groovy in Action"
        println ">"+aob.book[1].title+"<"
        println ">"+aob.book[1].author+"<"

        println ">"+aob.book[1].author+"<"
        assertEquals(aob.book[1].author, "David A. Chappell")
        assert aob.book[0].year == 2007
        def b = proxy.findBook("1-932394-84-2")
        println b
        println b.class.name
        println b.title
        assert proxy.findBook("1-932394-84-2").title == "Groovy in Action"
    }

    void tearDown() {
        server.stop()
    }
}
