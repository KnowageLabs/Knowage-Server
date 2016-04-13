<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

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
	
	<md-toolbar class="md-toolbar-tools md-knowage-theme"> MY WORKSPACE
		<md-button ng-click="" class="md-fab md-fab-top-right md-button ng-scope md-knowage-theme md-ink-ripple"> 
			<md-icon md-font-icon="fa fa-folder-o "></md-icon> 
		</md-button>
	</md-toolbar>
	
	<angular_2_col>
		
		<!-- 
			Directive that will render the left main menu of the Workspace 
			web page. 
		-->
		<left-col flex=15>	
			<left-main-menu-workspace></left-main-menu-workspace>
		</left-col>

		<right-col>
		
				<md-content class="md-toolbar-tools">
					
					<div  class="md-toolbar-tools" layout="row" layout-align="center center">
						
						<!--  Search button -->
						<md-button class="md
						-icon-button" ng-click="toggleSearchView()">
							  <md-icon md-font-icon="fa fa-search"></md-icon>
						</md-button>
						<!-- Search input -->
					    <md-input-container class="searchInput">
							<label></label>
							<input   type="text">
						</md-input-container>
						<!-- Document view buttons -->
						<md-button class="md-icon-button" ng-click="toggleDocumentView()">
							  <md-icon md-font-icon="fa fa-th"></md-icon>
						</md-button>
						<md-button class="md-icon-button" ng-click="toggleDocumentView()">
							  <md-icon md-font-icon="fa fa-list"></md-icon>
						</md-button>
						
					</div>
					
				</md-content>
				
				<recent-view-workspace></recent-view-workspace>	
				<favorites-view-workspace></favorites-view-workspace>	
				<documents-view-workspace></documents-view-workspace>			
				<dataset-view-workspace></dataset-view-workspace>
				<analysis-view-workspace></analysis-view-workspace>
				
		</right-col>
		
	</angular_2_col>
	
</body>
</html>