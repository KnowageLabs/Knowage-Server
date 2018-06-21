<%@ page language="java" pageEncoding="utf-8" session="true"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="eventModule">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!-- angular imports -->
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/event/eventLogsApp.js")%>"></script>
<!-- Styles -->
<link rel="stylesheet" type="text/css"
	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
	
<p>{{4+4}}</p>
</head>
<body>

</body>
</html>