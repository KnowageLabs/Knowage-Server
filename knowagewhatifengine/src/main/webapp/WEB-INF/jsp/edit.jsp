
<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS               --%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="olapManager">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/commons/olap/olapImport.jsp"%>
<base href="/" />
<title>OLAP Designer</title>
<script>
	var JSsbiExecutionID = '<%= sbiExecutionID %>'
	var mode = '<%= mode %>'
	var engineName = '<%= engine %>'
	
</script>
</head>
<body ng-controller="olapController" class="kn-olap">

	<rest-loading></rest-loading>
	<olap-designer></olap-designer>
 
	
</body>
</html>