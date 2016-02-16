<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="kpiDefinitionManager">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>KPI definition</title>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/kpi/kpiDefinitionController.js"></script>

</head>
<body>
	<angular-list-detail ng-controller="kpiDefinitionMasterController">
		<list label="translate.load('sbi.kpi.list')">lista</list>
		<detail>dettaglio</detail>
	</angular-list-detail>
</body>
</html>