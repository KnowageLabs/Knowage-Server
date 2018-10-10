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
<%@ page import="it.eng.knowage.slimerjs.wrapper.beans.RenderOptions" %>
<%@ page import="it.eng.knowage.engine.cockpit.api.export.jpg.JpgExporter"%>



<%@ page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@ page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>

<%@ page trimDirectiveWhitespaces="true" %>
<%@ page contentType="applicaton/octet-stream" %>
<%
int documentId = Integer.valueOf(request.getParameter("document"));
String userId = request.getParameter("user_id");
String requestURL = (String) request.getAttribute("requestURL");
RenderOptions renderOptions = (RenderOptions) request.getAttribute("renderOptions");

BIObject document = DAOFactory.getBIObjectDAO().loadBIObjectById(documentId);

JpgExporter jpgExporter = new JpgExporter(documentId, userId, requestURL, renderOptions);
byte[] data = jpgExporter.getBinaryData();

int sheetCount = jpgExporter.getSheetCount(document);

response.setHeader("Content-length", Integer.toString(data.length));
if(sheetCount==1){
	response.setHeader("Content-Type", "image/jpg");
	response.setHeader("Content-Disposition", "attachment; fileName=" + request.getParameter("DOCUMENT_LABEL") + ".jpg");
} else {
	response.setHeader("Content-Type", "application/zip");
	response.setHeader("Content-Disposition", "attachment; fileName=" + request.getParameter("DOCUMENT_LABEL") + ".zip");
}

response.getOutputStream().write(data, 0, data.length);
response.getOutputStream().flush();
response.getOutputStream().close();

return;
%>