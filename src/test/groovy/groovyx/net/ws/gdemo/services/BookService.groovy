package groovyx.net.ws.gdemo.services

public class BookService {
	
	private books = []
	
	Book findBook(String isbn){
      //println isbn
      def b = books.find { b -> b.isbn == isbn }
      //println b.title
      return b
	}
	
	void addBook(Book b){
      books+=b
      books.each{
        println "${it.title} by ${it.author}"
      }
	}
	
	Book[] getMyBooks(){
		return books
	}
	
}
