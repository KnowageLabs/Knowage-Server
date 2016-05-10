<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS                                                           --%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="workspaceManager">
<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/commons/workspace/workspaceImport.jsp"%>

<!-- Styles -->
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 

</head>
<body ng-controller="workspaceController" class="workspace" id="workspaceWebPageBody"> 
	
	<main-toolbar-workspace></main-toolbar-workspace>
	
	<md-content layout="row">
	
		<!-- 
			Directive that will render the left main menu of the Workspace web page. 
			@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		-->
		<md-content flex=15 class="leftRightPanelHeight">
			<left-main-menu-workspace></left-main-menu-workspace>
		</md-content>
		
		<md-content flex class="leftRightPanelHeight">
				<customize-view-workspace>													</customize-view-workspace>						
				<recent-view-workspace 		ng-show="currentOptionMainMenu.length==0 
				|| currentOptionMainMenu=='recent'">										</recent-view-workspace>	
				<favorites-view-workspace 	ng-show="currentOptionMainMenu=='favorites'">	</favorites-view-workspace>	
				<documents-view-workspace 	ng-show="currentOptionMainMenu=='documents'">	</documents-view-workspace>			
				<datasets-view-workspace 	ng-show="currentOptionMainMenu=='datasets'">	</datasets-view-workspace>
				<models-view-workspace		ng-show="currentOptionMainMenu=='models'">	        </models-view-workspace>
				<analysis-view-workspace 	ng-show="currentOptionMainMenu=='analysis'">	</analysis-view-workspace>				
		</md-content>
		
	</md-content>
	
</body>
</html>