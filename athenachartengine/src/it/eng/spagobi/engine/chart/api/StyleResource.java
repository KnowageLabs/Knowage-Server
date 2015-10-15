package it.eng.spagobi.engine.chart.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import it.eng.spagobi.engine.chart.ChartEngineConfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

@Path("/style")
public class StyleResource {
	
	private static final String PATH_TO_STYLE = "/style";
	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
    public String getStyles() throws Exception {
		
	String resourcePath= ChartEngineConfig.getEngineResourcePath();

	JSONArray allStyles= new JSONArray();
	
	File folder = new File(resourcePath+PATH_TO_STYLE);
	
	if(!folder.exists()){
		return allStyles.toString();
	}
	
	File[] listOfFiles = folder.listFiles();
	
	
	for (int i=0;i< listOfFiles.length;i++){
		if(listOfFiles[i].isFile()){
			String pathToFile=folder.getPath()+"\\"+listOfFiles[i].getName();
			BufferedReader br= new BufferedReader(new FileReader(pathToFile));
			StringBuilder fileContent= new StringBuilder();
			String line= null;
			
			while((line=br.readLine())!= null){
				fileContent.append(line);
			}
			br.close();
			
			try{
	          JSONObject obj= XML.toJSONObject(fileContent.toString());		
	         
	          //if empty json object is returned it would not be added to styles 
	          if(!obj.toString().equals(new JSONObject().toString())){
	        	  allStyles.put(obj);
	          }
			}catch(Exception e){
				e.printStackTrace();	
			}
			
		}
	}
	     return allStyles.toString();
    }
	
}
