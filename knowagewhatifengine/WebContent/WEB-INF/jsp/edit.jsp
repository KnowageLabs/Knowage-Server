
<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS               --%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="olapManager">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/commons/olap/olapImport.jsp"%>
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/css/olap.css">
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/css/whatIf.css">
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/css/customStyle.css">
<title>OLAP Designer</title>
<script>
	var JSsbiExecutionID = '<%= sbiExecutionID %>'
	var mode = '<%= mode %>'
	var engineName = '<%= engine %>'
	
</script>
</head>
<body ng-controller="olapController" >

	<rest-loading></rest-loading>
	<olap-designer></olap-designer>
 
	
</body>
</html>