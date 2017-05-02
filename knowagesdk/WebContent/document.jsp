<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

 <%@page import="java.io.*"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Random"%>
<%@page import="it.eng.spagobi.sdk.proxy.DocumentsServiceProxy"%>
<%@page import="it.eng.spagobi.sdk.documents.bo.SDKTemplate"%>
<%@page import="it.eng.spagobi.sdk.documents.bo.SDKSchema"%>
<%@page import="it.eng.spagobi.sdk.importexport.bo.SDKFile"%>
<%@page import="javax.activation.DataHandler"%>
<%@page import="org.apache.axis.attachments.ManagedMemoryDataSource"%>
<%@page import="it.eng.spagobi.sdk.exceptions.SDKException" %>
<%
/**
This page invokes a SpagoBI web services in order to execute the document's methods.
It's a JSP for ONLY case tests.
To call it the url must be: http://localhost:8080/knowagesdk/document.jsp?doUpload=true&folderUpload=<nome_folder>
By default a foodmart/datamart.jar is ever downloaded.
The parameter 'doUpload' force an upload operation after the previous download operation.
The parameter 'folderUpload' gives the name of the folder to upload the file. If 'doUpload' is true and 'folderUpload' is null
a random folder name is created.
*/
%>
<%@ page  session="true"  language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
String user = "biadmin";
String password = "biadmin";
String message = "Il documento è stato ";
String doUpload = "false";
String folderUpload = null;
String action = null;
String bodyCode = "";
String idCatalogue = "";

if (user != null && password != null) {
	try { 
		DocumentsServiceProxy proxy = new DocumentsServiceProxy(user, password);
		proxy.setEndpoint("http://localhost:8080/knowage/sdk/DocumentsService");		
		//gets request variables
		doUpload = (String)request.getParameter("doUpload");
		folderUpload = (String)request.getParameter("folderUpload");
		action = (request.getParameter("action")!=null)?(String)request.getParameter("action"):"";
		
		SDKTemplate template = new SDKTemplate();
		
		if (action.equalsIgnoreCase("getAllDatamartModels")){
			HashMap <String,String> mapModels = proxy.getAllDatamartModels();
			if (mapModels != null){
				message = "<H2>Sul server sono presenti i seguenti modelli: </H2><br>";
				
				for (Iterator iterator = mapModels.keySet().iterator(); iterator.hasNext();) {
					String folderName = (String) iterator.next();
					String fileName = mapModels.get(folderName);
					message += " <b>Folder:</b> " + folderName + "  - <b>Model:</b> " + fileName + "<br>";
				}				
				bodyCode += "<body>  " + message + " </body></html>";
			}
		}else if (action.equalsIgnoreCase("uploadMondrianSchema")){
			try{					
				Random randomGenerator = new Random();				
				SDKSchema schema = new SDKSchema();
				schema.setSchemaName("Sisba3"+ randomGenerator.nextInt(100));
				schema.setSchemaDescription("Schema di test");
				schema.setSchemaDataSourceLbl("SISBA3");
				
				//recupera il file con lo schema di test
				SDKFile schemaFile = new SDKFile();
				schemaFile.setFileName("schema");			
				
				// retrieves template
				String path = "D:\\Progetti\\Sisba3\\starschema.xml";
				//String path = "D:\\Progetti\\Sisba3\\sisba3.xml";
				//check file content
				FileInputStream isFile = new FileInputStream(path);
				//defines a content to return
				java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
				java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(baos);	
				int c = 0;
				byte[] b = new byte[1024];
				while ((c = isFile.read(b)) != -1) {
					if (c == 1024)
						bos.write(b);
					else
						bos.write(b, 0, c);
				}
				bos.flush();
				byte[] templateContent = baos.toByteArray();
				bos.close();
				ManagedMemoryDataSource mods =  new ManagedMemoryDataSource(new java.io.ByteArrayInputStream(templateContent), Integer.MAX_VALUE - 2,
						null, true);
				DataHandler dhSource = new DataHandler(mods);
				schemaFile.setContent(dhSource);
				schema.setSchemaFile(schemaFile);
				isFile.close();
				try{
					proxy.uploadMondrianSchema(schema);		
					message = "<H2>Upload dello schema Mondrian terminato con successo!</H2><br>";
					bodyCode += "<body>  " + message + " </body></html>";
				}catch(SDKException e){
					e.printStackTrace();
					message = "<H2>Upload dello schema Mondrian terminato con errore!</H2><br> " + e.getCode() + " - " + e.getDescription() ;					
					bodyCode += "<body>  " + message + " </body></html>";
				}catch(Exception e){
					e.printStackTrace();
					message = "<H2>Upload dello schema Mondrian terminato con errore!</H2><br> " + e.getMessage();					
					bodyCode += "<body>  " + message + " </body></html>";
				}
				
			}  catch (Exception e) {
				e.printStackTrace();				
			}
		}
		else{
			//test download datamart.jar
			template = proxy.downloadDatamartModelFiles("foodmart", "datamart.jar", "foodmart.sbimodel");
			//template = proxy.downloadDatamartFile("foodmart", "datamart.jar");
			if (template != null){ 
				if (doUpload == null || doUpload.equalsIgnoreCase("false")) {
			
					DataHandler dh = template.getContent();
					InputStream is = dh.getInputStream();
					String fileName = template.getFileName();
					
					java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
					java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(baos);
		
					int c = 0;
					byte[] b = new byte[1024];
					while ((c = is.read(b)) != -1) {
						if (c == 1024)
							bos.write(b);
						else
							bos.write(b, 0, c);
					}
					bos.flush();
					byte[] content = baos.toByteArray();
					bos.close();
					baos.close();
					
					response.setHeader("Content-Disposition","attachment; filename=\"" + fileName + "\";");
					response.setContentLength(content.length);
					response.setContentType("application/x-zip-compressed");
					//response.setContentType("application/xml");
					response.getOutputStream().write(content);
	
					message += "scaricato con successo!";
				}
			}else{
				message += "scaricato con errori! Verifica i logs.";
				bodyCode += "<body> <h2> " + message + " </h2> " + new java.util.Date() +" </body></html>";
			}
			//test modifica datamart esistente			
			if(doUpload != null && doUpload.equalsIgnoreCase("true")){
				//test upload datamart.jar
				if (folderUpload == null){
					Random randomGenerator = new Random();
					folderUpload = "foodmartSDK_"+ randomGenerator.nextInt(100);
				}
				template.setFileName("datamart.jar");
				template.setFolderName(folderUpload);
				template.setContent(template.getContent());
				proxy.uploadDatamartTemplate(template,template,null);
				//proxy.uploadDatamartModel(template);
				message += "aggiornato con successo!";
				bodyCode += "<body> <h2> " + message + " </h2> " + new java.util.Date() +" </body></html>";
			}
		}
	}  catch (Exception e) {
		e.printStackTrace();
			
	}
} else {
	response.sendRedirect("login.jsp");
}

%>
<%=bodyCode%>
