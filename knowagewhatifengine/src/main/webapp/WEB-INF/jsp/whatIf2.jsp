
<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS               --%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="olapManager">
	<head>
		<meta charset="UTF-8">
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		<%@include file="/WEB-INF/jsp/commons/olap/olapImport.jsp"%>
		<title>OLAP</title>
		<script>
			var JSsbiExecutionID = '<%= sbiExecutionID %>'
			var toolbarVisibleBtns = '<%= whatIfEngineInstance.getModelConfig().getToolbarVisibleButtons() %>'
			var toolbarClickedBtns = '<%= whatIfEngineInstance.getModelConfig().getToolbarClickedButtons() %>'
			var drillType = '<%= whatIfEngineInstance.getModelConfig().getDrillType() %>'
			var locker = '<%= whatIfEngineInstance.getModelConfig().getLocker() %>'
			var status = '<%= whatIfEngineInstance.getModelConfig().getStatus()%>'
			var mode = '<%= mode %>'
			var engineName = '<%= engine %>'
			var schemaID = '<%= schemaID %>'
			var schemaName = '<%= schemaName %>'
			var cubeName = '<%= cubeName %>'
			var currentContentId = '<%= currentContentId %>'
			var editModeCurrentContentId = '<%= editModeCurrentContentId %>'
			var jsonTemplate = '<%= jsonTemplate %>'
		</script>
	</head>
	<body layout="row" ng-controller="olapController" class="kn-olap">
		
	 	<div ng-if="showLoadingMask" layout-fill style='position:fixed;z-index: 500;background:rgba(0,0,0, 0.3);'>
            <md-progress-circular  md-mode='indeterminate' style='top:50%;left:50%' ></md-progress-circular>
        </div>
	 
		<div layout="column" flex>	
			<main-toolbar ng-hide="true" loading></main-toolbar>
			<filter-panel loading></filter-panel>
			<olap-panel loading></olap-panel>
		</div>
		<div style="width:2px"></div>
		<sbi-side-nav></sbi-side-nav>
		
	</body>
</html>