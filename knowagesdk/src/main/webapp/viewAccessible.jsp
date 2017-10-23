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
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.io.IOException"%>
<%@page import="it.eng.spagobi.sdk.documents.bo.SDKExecutedDocumentContent"%>
<%@page import="it.eng.spagobi.sdk.proxy.DocumentsServiceProxy"%>
<%@page import="it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter"%>
<%@page import="it.eng.spagobi.sdk.documents.bo.SDKDocument"%>
<%
/**
This page invokes a Knowage web services in order to execute the current document and retrive the result (a PDF file).
This functionality is available only for REPORT and KPI documents.
*/
%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
String user = (String) session.getAttribute("spagobi_user");
String password = (String) session.getAttribute("spagobi_pwd");
String html = "";
if (user != null && password != null) {
	InputStream is = null;
	try {
		SDKDocument document = (SDKDocument) session.getAttribute("spagobi_current_document");
		String role = (String) session.getAttribute("spagobi_role");
		SDKDocumentParameter[] parameters = (SDKDocumentParameter[]) session.getAttribute("spagobi_document_parameters"); 
		DocumentsServiceProxy proxy = new DocumentsServiceProxy(user, password);
		proxy.setEndpoint("http://localhost:8080/knowage/sdk/DocumentsService");
		SDKExecutedDocumentContent export = proxy.executeDocument(document, parameters, role,"text/html");
		is = export.getContent().getInputStream();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		//response.setContentType("text/html");
		//response.setHeader("content-disposition", "attachment; filename=" + export.getFileName());
		//ServletOutputStream os = response.getOutputStream();
		int c = 0;
		byte[] b = new byte[1024];
		while ((c = is.read(b)) != -1) {
			if (c == 1024)
				os.write(b);
			else
				os.write(b, 0, c);
		}
		html = os.toString();
		os.flush();
		os.close();
	} finally {
		if (is != null){
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}

	}
} else {
	response.sendRedirect("login.jsp");
}
%>
<%= html %>