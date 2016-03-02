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


<%@page import="it.eng.spagobi.services.common.SsoServiceFactory"%>
<%@page import="it.eng.spagobi.services.common.SsoServiceInterface"%>
<%@page import="java.io.ByteArrayInputStream"%>
<%@ page language="java"
         extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPagePortlet"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         session="true" 
%>

<%@page import="java.io.InputStream"%>
<%@page import="java.io.IOException"%>
<%@page import="it.eng.spagobi.sdk.documents.bo.SDKExecutedDocumentContent"%>
<%@page import="it.eng.spagobi.sdk.proxy.DocumentsServiceProxy"%>
<%@page import="it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter"%>
<%@page import="it.eng.spagobi.sdk.documents.bo.SDKDocument"%>
<%@page import="it.eng.spagobi.services.execute.service.ServiceKpiValueXml"%>

<%@page import="javax.activation.DataHandler"%>

<% 		
//Richiedere il token
		
		
		String token = (String)request.getParameter("SECURITY_TOKEN");
		String userId = (String)request.getParameter("USERID");

		SsoServiceInterface proxyService = SsoServiceFactory.createProxyService();
		proxyService.validateTicket(token, userId);

		String kId = (String)request.getParameter("KPI_VALUE_ID");
		Integer kpiValueId = null;
		if(kId!=null && !kId.equals("")){
			kpiValueId = new Integer(kId);
		}
		ServiceKpiValueXml x = new ServiceKpiValueXml();
		String xml = x.getKpiValueXML(token,userId,kpiValueId);
		ServletOutputStream os = response.getOutputStream();
		        try{
		        	byte[] xmlB = xml.getBytes();
		    		response.setContentType("text/xml");
		    		response.setHeader("content-disposition", "attachment; filename=xml");
		    		
		    		os.write(xmlB);
		    		os.flush();
		    	} finally {
		    		if (os != null)
		    			try {
		    				os.close();
		    			} catch (IOException e) {
		    				e.printStackTrace();
		    			} 
		    	}
		        
%>
