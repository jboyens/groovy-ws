package groovyx.net.ws.gdemo.services

import javax.activation.DataHandler
import javax.mail.util.ByteArrayDataSource
import javax.activation.DataSource


public class DataService{
	
	byte[] data
	String title
	
	String loadData(String title){
		
		def f = new File(title)
		try{
			this.title=f.name
			data = null
			data = f.readBytes()
		}catch (Exception e){
			e.printStackTrace()
		}
		this.title
	}
		
	DataHandler getData(){
		ByteArrayDataSource bads = new ByteArrayDataSource(data, "application/octet-stream")
		DataHandler dh = new DataHandler(bads)
	}
	
	void saveData(byte[] bb, String title){
		//this.data = bb.getContent() as byte[]
		this.data = bb
		this.title=title
	}
	
	String storeData(){
		new FileOutputStream(title).write(data)
		title
	}
	
	String opData(){
		storeData()
		def env = System.getenv()
		def order="convert -swirl 250 $title $title"
		def proc = order.execute()
		proc.waitFor()
		loadData(title)
		
	}
	
	DataService (){
		title=null
		data=null
	}
	
	String toString(){
		def str = "${title.name} \n ${data}"
	}
}
