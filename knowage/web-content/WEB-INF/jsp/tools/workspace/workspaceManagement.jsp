<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS                                                           --%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="workspaceManager">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/commons/workspace/workspaceImport.jsp"%>

<!-- STYLE -->
<link rel="stylesheet" type="text/css"	href="/knowage/themes/commons/css/customStyle.css">
</head>
<body ng-controller="workspaceController">
	
	<main-toolbar-workspace></main-toolbar-workspace>
	<angular_2_col>
		<left-col flex=15>	
			<left-main-menu-workspace></left-main-menu-workspace>
		</left-col>
		<right-col>
				<customize-view-workspace></customize-view-workspace>				
				<recent-view-workspace></recent-view-workspace>	
				<favorites-view-workspace></favorites-view-workspace>	
				<documents-view-workspace></documents-view-workspace>			
				<dataset-view-workspace></dataset-view-workspace>
				<analysis-view-workspace></analysis-view-workspace>
		</right-col>
	</angular_2_col>
	
</body>
</html>