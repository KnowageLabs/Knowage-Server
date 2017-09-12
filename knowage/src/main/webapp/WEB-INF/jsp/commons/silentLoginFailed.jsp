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


<%@ page isErrorPage="true" 
		 language="java" 
		 contentType="text/html; charset=UTF-8"
    	 pageEncoding="UTF-8" 
    	 session="false"%>
<%@page import="org.apache.log4j.Logger"%>
<%
// The exception variable may be null since this jsp is used as general error page but also as Spago service exception publisher
// In case of Spago trapped exception, this should be logged elsewhere...
if (exception != null) {
	exception.printStackTrace();
	Logger logger = Logger.getLogger("it.eng.spagobi");
	logger.error(exception);
}
response.setStatus(500);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
<title>Error</title>
</head>
<body>

Silent login failed.<br/>
If the problems persists, contact the system administrator.

</body>
</html>
