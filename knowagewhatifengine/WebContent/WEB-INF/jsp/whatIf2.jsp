
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
<title>OLAP</title>
<script>
	var JSsbiExecutionID = '<%= sbiExecutionID %>'
	var toolbarVisibleBtns = '<%= whatIfEngineInstance.getModelConfig().getToolbarVisibleButtons() %>'
	var drillType = '<%= whatIfEngineInstance.getModelConfig().getDrillType() %>'
</script>
</head>
<body ng-controller="olapController" >
	<div layout="row">
		
		<div layout="column" flex=80>
			<div  ng-include="mainToolbar">
			</div>
		
			<div layout="row" ng-include="filterPanel" class="top-alignment" ng-drop="true" ng-drop-success="dropFilter($data,$event)">
			</div>

			<div layout="column" ng-include="olapPanel">
			</div>
		</div>

		<div style="width:2px"></div>
		
		<div flex=20 ng-include="rightPanel">						
		</div>
	</div>
</body>
</html>