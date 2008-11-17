package groovyx.net.ws.gdemo.services

public class BookService {
	
	private books = []
	
	Book findBook(String isbn){
		books.find { b -> b.isbn == isbn }
	}
	
	void addBook(Book b){
		books+=b
	}
	
	Book[] getMyBooks(){
		return books
	}
	
}
