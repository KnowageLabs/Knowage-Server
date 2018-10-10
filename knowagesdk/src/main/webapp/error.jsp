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


<%@ page isErrorPage="true" language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="org.apache.axis.AxisFault"%>
<html>
<head>
	<title>Error!</title>
	<style>
	body, p { font-family:Tahoma; font-size:10pt; padding-left:30; }
	pre { font-size:8pt; }
	</style>
</head>
<body>

<%-- Exception Handler --%>
<font color="red">
<%
exception.printStackTrace();
if (exception instanceof AxisFault) {
	AxisFault axisFault = (AxisFault) exception;
	if (axisFault.getFaultString().startsWith("WSDoAllReceiver")) {
		%>
		Authenticated failed!!
		<%
	} else {
		%>
		Error while connecting to server!!
		<%
	}
} else {
	%>
	Error while connecting to server!!
	<%
}
%><br>
</font>
<a href="login.jsp">Click to retry</a>
</body>
</html>