
<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS               --%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="olapManager">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/css/xxx.css">

<title>XXX</title>
<script>
	var JSsbiExecutionID = '<%= sbiExecutionID %>'
</script>
</head>
<body ng-controller="xxxController" >
	<div layout="row">
		
		<div layout="column" flex=95>
		
			<!-- ANGULAR COMMANDS -->
			
		</div>

		<div style="width:2px"></div>
		
		<sbi-side-nav></sbi-side-nav>
	</div>
</body>
</html>