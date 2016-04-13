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
	
		<!-- 
			Directive that will render the left main menu of the Workspace web page. 
			@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		-->
		<left-col flex=15>	
			<left-main-menu-workspace></left-main-menu-workspace>
		</left-col>
		
		<right-col>
				<customize-view-workspace>													</customize-view-workspace>						
				<recent-view-workspace 		ng-show="currentOptionMainMenu=='recent'">		</recent-view-workspace>	
				<favorites-view-workspace 	ng-show="currentOptionMainMenu=='favorites'">	</favorites-view-workspace>	
				<documents-view-workspace 	ng-show="currentOptionMainMenu=='documents'">	</documents-view-workspace>			
				<datasets-view-workspace 	ng-show="currentOptionMainMenu=='datasets'">	</dataset-view-workspace>
				<analysis-view-workspace 	ng-show="currentOptionMainMenu=='analysis'">	</analysis-view-workspace>
		</right-col>
		
	</angular_2_col>
	
</body>
</html>