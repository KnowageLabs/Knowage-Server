<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="templateManagement">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- non c'entra	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/commons/LayerTree.js"></script> -->
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/tree-style.css">
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/generalStyle.css">

<!-- controller -->
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/servermanager/templateManagement.js"></script>




</head>
<body class="bodyStyle">

	<div ng-controller="Controller " layout="row" layout-wrap layout-fill>
		
	</div>

</body>
</html>

