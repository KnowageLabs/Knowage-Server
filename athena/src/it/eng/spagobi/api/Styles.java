package it.eng.spagobi.api;


import java.io.File;

import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;


@Path("/Styles")
public class Styles {
	
	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public void metoda() throws Exception{
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		System.out.println("usao");
		
		File folder = new File("C:/Users/lkostic/athena-runtimes/apache-tomcat-7.0.61/resources/chart/style");
		File[] listOfFiles = folder.listFiles();

			System.out.println(listOfFiles.length);
		
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        System.out.println("File " + listOfFiles[i].getName());
		        
		        String domstring = folder+"\\"+listOfFiles[i].getName();
		        
		        System.out.println(domstring);
		        
		        Document dom = db.parse(domstring);
		        
		        System.out.println(dom);
		        
		        
		      } else if (listOfFiles[i].isDirectory()) {
		        System.out.println("Directory " + listOfFiles[i].getName());
		      }
		    }
		
	}

}
