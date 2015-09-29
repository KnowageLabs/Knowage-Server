package it.eng.spagobi.api;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.StringWriter;

import javax.json.JsonObject;
import javax.naming.InitialContext;
import org.apache.naming.NamingContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.*;
import org.w3c.dom.Document;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;


@Path("/Styles")
public class Styles {
	
	@SuppressWarnings("unchecked")
	@GET
	// TODO: check why it doesn't work with this annotation
	@Produces(MediaType.APPLICATION_JSON)
	public String metoda() throws Exception{
		
		//DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		//DocumentBuilder db = dbf.newDocumentBuilder();
		Object resources = (((NamingContext) new InitialContext().lookup("java:comp/env"))).lookup("spagobi_resource_path");
		System.out.println("usao");
		System.out.println(resources.toString());
		JSONArray retValue=new JSONArray();
		JSONObject empty=new JSONObject();
		
		File folder = new File(resources.toString()+"/chart/style");
		File[] listOfFiles = folder.listFiles();

			System.out.println(listOfFiles.length);
		    System.out.println(empty.toString());
		    for (int i = 0; i < listOfFiles.length; i++) {
		  
		      if (listOfFiles[i].isFile()) {
		        System.out.println("File " + listOfFiles[i].getName());
		        
		        String domstring = folder.getPath()+"\\"+listOfFiles[i].getName();
		        
		        System.out.println(domstring);
		        // read file into string 
		        BufferedReader br= new BufferedReader(new FileReader(domstring)); 
		        String lineContent="";
		        String fileContent="";
		          
		        while(lineContent!= null){
		        	lineContent=br.readLine();
		        	fileContent+=lineContent;
		        }
		        br.close();
		        // convert from string to JSON object
		        try{
		        JSONObject jsonObj= XML.toJSONObject(fileContent);
		        
		        if(!jsonObj.toString().equals(empty.toString())){
		        retValue.put(jsonObj);
		        }
		        
		        System.out.println(jsonObj.toString());
		        }catch(Exception e){
		        	e.printStackTrace();
		        }finally {
		        	//continue;
		        }
		      
		        
		      } else if (listOfFiles[i].isDirectory()) {
		        System.out.println("Directory " + listOfFiles[i].getName());
		      }
		    }
	
		    System.out.println(retValue.toString());
		return  retValue.toString();
	}

}



