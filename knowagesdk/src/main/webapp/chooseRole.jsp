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

<%
/**
This page retrieves the document choosen by the user on documentsList.jsp and retrieves the valid roles for 
document execution, according to user's roles.
If the user has only one valid role, he is automatically redirected to documentParameters.jsp, otherwise a form for role selection is displayed.
*/
%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="it.eng.spagobi.sdk.proxy.DocumentsServiceProxy"%>
<%@page import="it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Choose role</title>
	<style>
	body, p { font-family:Tahoma; font-size:10pt; padding-left:30; }
	pre { font-size:8pt; }
	</style>
</head>
<body>

<%
String user = (String) session.getAttribute("spagobi_user");
String password = (String) session.getAttribute("spagobi_pwd");
if (user != null && password != null) {
	String documentIdStr = request.getParameter("documentId");
	Integer documentId = new Integer(documentIdStr);
	session.setAttribute("spagobi_documentId", documentId);
	// request for valid roles for the execution of a document 
	String[] validRoles = null;
	try {
		DocumentsServiceProxy proxy = new DocumentsServiceProxy(user, password);
		proxy.setEndpoint("http://localhost:8080/knowage/sdk/DocumentsService");
		validRoles =  proxy.getCorrectRolesForExecution(documentId);
		if (validRoles.length == 0) {
			%>
			User cannot execute document
			<%
		} else if (validRoles.length == 1) {
			response.sendRedirect("documentParameters.jsp?role=" + validRoles[0]);
		} else {
			%>
			<span><b>Choose the role</b></span>
			<form action="documentParameters.jsp" method="post">
				Choose role: 
				<select name="role">
				<%
				for (int i = 0; i < validRoles.length; i++) {
					%>
					<option value="<%= validRoles[i] %>"><%= validRoles[i] %></option>
					<%
				}
				%>
				</select>
				<input type="submit" value="Go on" />
			</form>
			<%
		}
	} catch (NonExecutableDocumentException e) {
		%>
		User cannot execute document
		<%
	}

} else {
	response.sendRedirect("login.jsp");
}
%>
</body>
</html>