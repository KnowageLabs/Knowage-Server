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


<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>


<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

 <%
		 	
			String datasetID = "";
 			String datasetLabel = "";
			if(request.getParameter("id")!=null){
				datasetID = request.getParameter("id");
			}
			if(request.getParameter("label")!=null){
				datasetLabel = request.getParameter("label");
			}
		%>
		
		<script>
			var datasetId = '<%= datasetID %>';
			var datasetLabel = '<%= datasetLabel  %>';
		</script> 



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

	<head>
	
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>{{translate.load("Dataset Link");}}</title>
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/federateddataset/linkDataset.js"></script>
		<link rel="stylesheet" type="text/css"	href="/knowage/themes/commons/css/customStyle.css">
	
		</head>

	<body class="federatedDataset" ng-app="linkDatasetModule" id="ng-app">
		
		<!-- Binding the Angular controller FederationDefinitionCTRL from the module FEDERATIONDEFINITION to the div -->
		<div ng-controller="linkDatasetCTRL" layout-fill class="contentdemoBasicUsage">		
		  <div class ="md-container" >
			<md-toolbar class="miniheadfederation" >
				<div class="md-toolbar-tools">
				
				<h2 class="md-flex" >Dataset Link for <%= datasetLabel  %>  with id: <%= datasetID %> </h2>
				
				</div>

			</md-toolbar>
	
						
	
			<md-content layout-padding class="mainContainer" >
			
			<md-select placeholder="Select Source" ng-model="source">
      		<md-option ng-value="source" ng-repeat="source in sourceList" ng-click="getTablesBySourceID(source.sourceId)">{{source.name}}</md-option>
    		</md-select>
			
				<div ng-show="showme == true" layout="row" >
					
					<!-- Avaliable tables -->
					<div flex class="datasetBox">
					  <md-card>	
						<md-toolbar class="miniheadfedsmall"  >
							<div class="md-toolbar-tools">
								<i class="fa fa-bars "></i>
								<h2 class="md-flex" >Avaibile tables</h2>
								<span flex=""></span>					
							</div>
						</md-toolbar>
						

					
						<md-content  layout-padding>
							<angular-list
							layout-fill 
							id="availableTables_id" 
							ng-model="tablesList" 
							item-name="name"
							show-search-bar=true
							click-function="moveToSelected(item)"
							style="overflow:hidden"	
							>					
							</angular-list>
						</md-content>
					 </md-card>	
					</div>
				
					<!-- Selected tables -->
					<div  flex class="datasetBox">
					 <md-card>
						<md-toolbar class="miniheadfedsmall"  >
							<div class="md-toolbar-tools">
								<i class="fa fa-bars"></i>
								<h2 class="md-flex" >Selected Tables</h2>
								<span flex=""></span>					
							</div>
						</md-toolbar>
					
						<md-content layout-padding >
							<angular-list layout-fill 
							id="selectedTables_id" 
							ng-model="selectedTables" 
							item-name="name" 
							speed-menu-option=removeFromSelected
							>					
							</angular-list>
						</md-content>
					</md-card>
				</div>
				
				</div>
			
				<md-button class="md-raised buttonR" aria-label="btn_save"
						ng-click="saveRelations()">Save
				</md-button>

			</md-content>
		 <!-- end div container -->
		 </div>	
		</div>
	
	</body>

</html>
