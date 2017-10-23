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

 <%@page import="java.io.InputStream"%>
<%@page import="java.util.Random"%>
<%@page import="it.eng.spagobi.sdk.proxy.DataSetsSDKServiceProxy"%>
<%@page import="it.eng.spagobi.sdk.datasets.bo.SDKDataSet"%>

<%
/**
This page invokes a Knowage web services in order to execute the dataset's methods.
It's a JSP for ONLY case tests.
To call it the url must be: http://localhost:8080/knowagesdk/dataset.jsp?ds_id=1
The parameter 'ds_id' defines an existing dataset (modify test), if it isn't setted the insert method is called.
*/
%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
/*
	String user = "biadmin";
	String password = "biadmin";
	try {
	 DataSetsSDKServiceProxy proxy = new DataSetsSDKServiceProxy(user, password);
	 proxy.setEndpoint("http://localhost:8080/knowage/sdk/DataSetsSDKService");
	 SDKDataSet[] datasets = proxy.getDataSets();
	 System.out.println("*** dataset: " + datasets.length);
	}  catch (Exception e) {
	 e.printStackTrace();
}
*/
String user = "biadmin";
String password = "biadmin";
String message = "Il dataset è stato ";
String dsId = null;
if (user != null && password != null) {
	InputStream is = null;
	try { 
		DataSetsSDKServiceProxy proxy = new DataSetsSDKServiceProxy(user, password);
		proxy.setEndpoint("http://localhost:8080/knowage/sdk/DataSetsSDKService");		
		
		//test modifica dataset esistente
		dsId = (String)request.getParameter("ds_id");
		SDKDataSet dataset = null;
		if (dsId != null){
			dataset = proxy.getDataSet(Integer.valueOf(dsId));
			System.out.println("*** dataset: " + dataset);
		}else{
			//test inserimento nuovo dataset (tipo qbe)		
			 Random randomGenerator = new Random();
			 dataset = new SDKDataSet();		
			 dataset.setLabel("testDataset_" + randomGenerator.nextInt(100));
			 dataset.setName("test WS del Dataset ");
			 //dataset.setDescription("test WS del Dataset tipo Qbe ");
			 dataset.setType("SbiQbeDataSet");
			 String configuration = "{\'qbeDatamarts\':\'\',\'qbeDataSource\':\'knowage\',\'qbeSQLQuery\':\'select * from sbi_engines\',\'qbeJSONQuery\':\'select * from sbi_engines\'}";
			 
			// dataset.setType("SbiQueryDataSet");		
			// String configuration = "{\"Query\":\"select * from sbi_checks \",\"queryScript\":\"\",\"queryScriptLanguage\":\"\",\"dataSource\":\"knowage\"}";
			 
			 //dataset.setType("SbiFileDataSet");		
			 //String configuration = "{\"Query":\"select * from sbi_checks \",\"queryScript\":\"\",\"queryScriptLanguage\":\"\",\"dataSource\":\"knowage\"}";
			 
			 dataset.setConfiguration(configuration);			 					 
	 		 //dataset.setJdbcDataSourceId(1);	
		}
		Integer result = proxy.saveDataset(dataset);
		
		if (dataset.getId() != null && result == null) {
			message += "modificato con successo!";
		}else if (dataset.getId() == null && result == null) {			
			message += "inserimento terminato con errori! Verifica il log.";
		}else{
			message += "inserito con successo! (new id = "+ result +")";
		}
		System.out.println("*** result saving...: " + result);

	}  catch (Exception e) {
		e.printStackTrace();
			
	}
} else {
	response.sendRedirect("login.jsp");
}
%>
<body>
<h2><%= message%></h2>
<%= new java.util.Date() %>
</body></html> 
