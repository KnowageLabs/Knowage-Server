<%@ page language="java" pageEncoding="utf-8" session="true"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="eventModule">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!-- angular imports -->
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%@include file="/WEB-INF/jsp/commons/angular/eventLogsImport.jsp" %>



<!-- Styles -->
<link rel="stylesheet" type="text/css"
	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
</head>

<body ng-controller="eventController">
{{4+4}}
<input type="text" ng-model="test">
<p>{{test}}</p>

<div ng-repeat="(key,value) in events">{{key}} - {{value}}</div>
<div style="border: 1px solid red;" ng-repeat="i in events">ID: {{i.id}} <br> USER: {{i.user}} <br> DATE: {{i.date}} <br> TYPE: {{i.type}} <br> DESC: {{i.desc}}</div>





</body>
</html>