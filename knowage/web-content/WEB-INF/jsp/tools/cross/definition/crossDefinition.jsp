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


<%@ page language="java" pageEncoding="utf-8" session="true"%>
<%@ page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<% 
	String contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); 
%>

<%
		//TODO check for user profile autorization
		boolean canSee=false,canSeeAdmin=false;
		if(UserUtilities.haveRoleAndAuthorization(userProfile, null, new String[]{SpagoBIConstants.MANAGE_CROSS_NAVIGATION})){
			canSee=true;
		 canSeeAdmin=UserUtilities.haveRoleAndAuthorization(userProfile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[]{SpagoBIConstants.MANAGE_CROSS_NAVIGATION});
		}
%>

<% if(canSee ){ %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html >

<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/generalStyle.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/sbi_default/css/crossnavigation/cross-definition.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/cross/definition/CrossDefinition.js"></script>

</head>

<body class="bodyStyle" ng-app="crossDefinition" id="ng-app">

<script type="text/ng-template" id="nodes_renderer1.html">
<md-toolbar class="ternaryToolbar tree-node tree-node-content crossnavigation-parameter " ui-tree-handle layout="row">
    <div>
		<span class="fa fa-bars"></span>
	    {{par.name}}
    </div>
	<span flex class="flex"></span>
    <div >
        {{ctrl.getTypeLabel(par.type)}}
    </div>
</md-toolbar>
</script>
<script type="text/ng-template" id="nodes_renderer2.html">
  <div layout="row" class="tree-node tree-node-content crossnavigation-parameter {{par.id==ctrl.detail.toPars[ctrl.selectedItem].id?'highlight-selected-parameter':''}}" ng-if="!par.links.length">
    <div >
	  {{par.name}}
    </div>
	<span flex class="flex"></span>
    <div >
        {{ctrl.getTypeLabel(par.type)}}
    </div>
  </div>
  <ol ui-tree-nodes="" ng-model="par.links" ng-class="{hidden: collapsed}" ng-if="!par.links.length" >
      <li ng-repeat="node in par.links" ui-tree-node >
      </li>
  </ol>

  <div class="tree-node tree-node-content crossnavigation-parameter link" ng-if="par.links.length" layout="row">
    <div >
	  {{par.links[0].name}}
	  <span class="fa fa-link"></span>
	  {{par.name}}
    </div>
    <span flex class="flex"></span>
    <i class="fa fa-times-circle" ng-click="ctrl.removeLink(par.id)"></i>
  </div>
	
</script>
<script type="text/ng-template" id="dialog1.tmpl.html">
<md-dialog aria-label="{{translate.load('sbi.crossnavigation.selectDocument')}}" ng-cloak layout="column" style="height: 90%;    width: 40%;">

	<md-toolbar>
		<div class="md-toolbar-tools">
			<h1>{{translate.load('sbi.crossnavigation.selectDocument')}}</h1>
			<span flex></span>
			<md-button ng-click="closeDialog()"> {{translate.load('sbi.general.cancel')}} </md-button>
		</div>
	</md-toolbar>
	 
	<md-content  flex layout style="position: relative;">
		<angular-list id="docList"  style="position: absolute;"
			ng-model="listDoc" item-name="DOCUMENT_NAME"
			show-item-tooltip="false" highlights-selected-item="true"
			show-search-bar="true"
			no-pagination="true" click-function="clickOnSelectedDoc(item,listId,closeDialog)">
		</angular-list>
	</md-content>	 
	
	 

<div ng-show="loading" class="loadingSpinner">
    <i class="fa fa-spinner fa-pulse fa-4x"></i> 
</div>
</md-dialog>
</script>

	<angular-list-detail ng-controller="navigationController as ctrl"   >
       <list label="translate.load('sbi.crossnavigation.lst')" new-function="ctrl.newNavigation" > <!-- Requires an instruction like $scope.translate = sbiModule_translate on myController -->
			<!-- navigations list -->
			<angular-table 
					flex
					id="dataSourceList"
					ng-model="ctrl.list"
					columns="ctrl.navigationList.columns"
					columns-search="ctrl.navigationList.searchColumns"
					show-search-bar=true
					highlights-selected-item=true
					click-function="ctrl.navigationList.loadSelectedNavigation(item)"
					speed-menu-option="ctrl.navigationList.dsSpeedMenu"					
				>						
			</angular-table>
			<div ng-show="ctrl.navigationList.loadingSpinner" class="loadingSpinner">
				<i class="fa fa-spinner fa-pulse fa-4x"></i> 
			</div>
		</list>
		
        <detail label="ctrl.detail.simpleNavigation?ctrl.detail.simpleNavigation.name:''" save-function="ctrl.saveFunc" cancel-function="ctrl.cancelFunc"> <!-- assuming that $scope.selectedItem stores the selected item on teh controller  -->
			<form name="tsForm" novalidate >			
				<div layout="row" layout-wrap>
					<div flex="50">
						<md-input-container > 
							<label>{{translate.load("sbi.crossnavigation.name");}}</label>
							<input maxlength="100" type="text" ng-model="ctrl.detail.simpleNavigation.name" required > 
							<div ng-messages="tsForm.ctrl.detail.simpleNavigation.name.$error" ng-show="tsForm.ctrl.detail.simpleNavigation.name.$dirty">
						    	<div ng-message="required">This is required!</div>
						    </div>
						</md-input-container>
					</div>
				</div>
	
				<div layout="row" layout-wrap>
					<div flex="50" layout="row" >
						<md-input-container flex> 
							<label>{{translate.load("sbi.crossnavigation.doc.a")}}</label> 
							<input maxlength="100" type="text" ng-model="ctrl.detail.simpleNavigation.fromDoc" readonly>
						</md-input-container>
						<md-button ng-click="ctrl.listLeftDocuments()" class="md-raised md-ExtraMini">{{translate.load("sbi.generic.select")}}</md-button>
					</div>
				
					<div flex="50" layout="row">
						<md-input-container flex> <label>{{translate.load("sbi.crossnavigation.doc.b");}}</label> 
							<input maxlength="100" type="text" ng-model="ctrl.detail.simpleNavigation.toDoc" readonly> </md-input-container>
						</md-input-container>
						<md-button ng-click="ctrl.listRightDocuments()" class="md-raised md-ExtraMini">{{translate.load("sbi.generic.select")}}</md-button>
					</div>
				</div>
			
				<div layout="row">
					<div layout="column" flex="50" class="parametersList">
						<md-toolbar class="secondaryToolbar md-knowage-theme" >
							<div class="md-toolbar-tools ">
								<h1>{{translate.load("sbi.crossnavigation.leftParameters");}}</h1>
							</div>
						</md-toolbar>
						<h3 ng-model="ctrl.detail.fromDoc"></h3>
					    <div ui-tree="ctrl.treeOptions" id="tree1-root" data-nodrop-enabled="true" data-clone-enabled="true" ng-show="ctrl.detail.fromPars.length>0">
					      <ol ui-tree-nodes="" ng-model="ctrl.detail.fromPars" data-nodrop-enabled="true">
					        <li ng-repeat="par in ctrl.detail.fromPars" ui-tree-node ng-include="'nodes_renderer1.html'" ></li>
					      </ol>
					    </div>
					    <div layout="row" flex="100" ng-if="ctrl.detail.simpleNavigation.fromDoc" >
							<md-input-container flex> <label>{{translate.load("sbi.crossnavigation.fixedValue");}}</label>
								<input maxlength="100" type="text" ng-model="ctrl.tmpfixedValue" > 
							</md-input-container>
							<md-button ng-click="ctrl.addFixedParam()" class="md-fab md-mini" > 
								<md-icon md-font-icon="fa fa-plus" >
								</md-icon> 
							</md-button>
						</div>
					</div>
					<div layout="column" flex class="parametersList">
						<md-toolbar class="secondaryToolbar md-knowage-theme" >
							<div class="md-toolbar-tools ">
								<h1>{{translate.load("sbi.crossnavigation.rightParameters");}}</h1>
							</div>
						</md-toolbar>
						<h3 ng-model="ctrl.detail.toDoc"></h3>
					    <div ui-tree="ctrl.treeOptions2" id="tree2-root" data-empty-placeholder-enabled="false" ng-show="ctrl.detail.toPars.length>0">
					      <ol ui-tree-nodes="" ng-model="ctrl.detail.toPars" >
					        <li ng-repeat="par in ctrl.detail.toPars" ui-tree-node ng-include="'nodes_renderer2.html'"  data-nodrag></li>
					      </ol>
					    </div>
					</div>
				</div>
			</form>
			<div ng-show="ctrl.detailLoadingSpinner" class="loadingSpinner">
				<i class="fa fa-spinner fa-pulse fa-4x"></i> 
			</div>
		</detail>
	</angular-list-detail>
</body>
</html>


<%}else{ %>
Access Denied
<%} %>

