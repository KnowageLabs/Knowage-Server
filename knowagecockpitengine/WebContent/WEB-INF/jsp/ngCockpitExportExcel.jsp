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

<%-- ------------------------------------------------------------------------%>
<%-- JAVA IMPORTS                                                          --%>
<%-- ------------------------------------------------------------------------%>
<%@ page import="java.util.Map"%>
<%@ page import="it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter"%>

<%@ page trimDirectiveWhitespaces="true" %>
<%@ page contentType="applicaton/octet-stream" %>
<%
String outputType = request.getParameter("outputType");
String userId = request.getParameter("user_id");
Map<String,String[]> parameterMap = request.getParameterMap();

Integer documentId = Integer.valueOf(request.getParameter("document"));
String documentLabel = request.getParameter("DOCUMENT_LABEL");
String template = (String) request.getAttribute("template");

ExcelExporter excelExporter = new ExcelExporter(outputType, userId, parameterMap);
String mimeType = excelExporter.getMimeType();
if(mimeType != null){
	byte[] data = excelExporter.getBinaryData(documentId, documentLabel, template);
	
	response.setHeader("Content-length", Integer.toString(data.length));
	response.setHeader("Content-Type", mimeType);
	response.setHeader("Content-Disposition", "attachment; fileName=" + documentLabel + "."+outputType);
	
	response.getOutputStream().write(data, 0, data.length);
	response.getOutputStream().flush();
	response.getOutputStream().close();
}
return;
%>