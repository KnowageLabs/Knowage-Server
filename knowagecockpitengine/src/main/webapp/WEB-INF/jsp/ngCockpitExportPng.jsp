<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

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
<%@page import="it.eng.knowage.engine.cockpit.api.export.png.PngExporter"%>
<%@ page import="it.eng.knowage.slimerjs.wrapper.beans.RenderOptions" %>
<%@ page import="java.util.zip.ZipInputStream" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="javax.servlet.ServletContext" %>

<%@ page contentType="applicaton/octet-stream" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%
	ServletContext servletContext = request.getServletContext();
	int documentId = Integer.valueOf(request.getParameter("document"));
	String userId = request.getParameter("user_id");
	String requestURL = (String) request.getAttribute("requestURL");
	RenderOptions renderOptions = (RenderOptions) request.getAttribute("renderOptions");

	PngExporter pngExporter = new PngExporter(documentId, userId, requestURL, renderOptions);
	byte[] data = pngExporter.getBinaryData();

	boolean isZipped = new ZipInputStream(new ByteArrayInputStream(data)).getNextEntry() != null;

	response.setHeader("Content-length", Integer.toString(data.length));
	if (!isZipped) {
		response.setHeader("Content-Type", "image/png");
		response.setHeader("Content-Disposition", "attachment; fileName=" + request.getParameter("DOCUMENT_LABEL") + ".png");
	} else {
		response.setHeader("Content-Type", "application/zip");
		response.setHeader("Content-Disposition", "attachment; fileName=" + request.getParameter("DOCUMENT_LABEL") + ".zip");
	}

	response.getOutputStream().write(data, 0, data.length);
	response.getOutputStream().flush();
	response.getOutputStream().close();

	return;
%>