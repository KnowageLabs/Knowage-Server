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
<%@ page import="it.eng.knowage.engine.cockpit.api.export.pdf.PdfExporter"%>

<%@ page trimDirectiveWhitespaces="true" %>
<%@ page contentType="applicaton/octet-stream" %>
<%
int documentId = Integer.valueOf(request.getParameter("document"));
String userId = request.getParameter("user_id");
String requestURL = (String) request.getAttribute("requestURL");
RenderOptions renderOptions = (RenderOptions) request.getAttribute("renderOptions");
String pdfPageOrientation = request.getParameter("pdfPageOrientation");
boolean pdfFrontPage = Boolean.valueOf(request.getParameter("pdfFrontPage"));
boolean pdfBackPage = Boolean.valueOf(request.getParameter("pdfBackPage"));

PdfExporter pdfExporter = new PdfExporter(documentId, userId, requestURL, renderOptions, pdfPageOrientation, pdfFrontPage, pdfBackPage);
byte[] data = pdfExporter.getBinaryData();

response.setHeader("Content-length", Integer.toString(data.length));
response.setHeader("Content-Type", "application/pdf");
response.setHeader("Content-Disposition", "attachment; fileName=" + request.getParameter("DOCUMENT_LABEL") + ".pdf");

response.getOutputStream().write(data, 0, data.length);
response.getOutputStream().flush();
response.getOutputStream().close();

return;
%>