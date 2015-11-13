<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="geoManager">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/commons/angular/geoImport.jsp"%>


<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoController.js"></script>

<title>GeoReport</title>

</head>
<body class="mapBodyStyle">

<div ng-controller="mapCtrl" ng-cloak>

	<geo-map map-id='myMap'></geo-map>

</div>

</body>

</html>