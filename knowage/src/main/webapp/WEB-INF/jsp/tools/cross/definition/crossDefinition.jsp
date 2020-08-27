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


<%@ page language="java" pageEncoding="UTF-8" session="true"%>
<%@ page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<% 
	String contextName = KnowageSystemConfiguration.getKnowageContext(); 
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

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/cross/definition/CrossDefinition.js")%>"></script>

</head>

<body class="bodyStyle kn-cross-navigation-definition" ng-app="crossDefinition" id="ng-app">
<%if(includeInfusion){ %> 
            <%@include file="/WEB-INF/jsp/commons/infusion/infusionTemplate.html"%> 
<%} %>
<script type="text/ng-template" id="nodes_renderer1.html">
<!--<i class="fa fa-times-circle" ng-show="par.type==2" ng-click="ctrl.deleteFixedValue(par))"></i>-->
<md-toolbar class="ternaryToolbar tree-node tree-node-content crossnavigation-parameter " ui-tree-handle layout="row" layout-align="start center">
		<i class="fa fa-bars"></i>
	    {{par.name}}
	<span flex class="flex"></span>
        {{ctrl.getTypeLabel(par.type)}}
</md-toolbar>
</script>
<script type="text/ng-template" id="nodes_renderer2.html">
  <div layout="row" layout-align="start center" class="tree-node tree-node-content crossnavigation-parameter {{par.id==ctrl.detail.toPars[ctrl.selectedItem].id?'highlight-selected-parameter':''}}" ng-if="!par.links.length">
	{{par.name}}
	<span flex class="flex"></span>
    {{ctrl.getTypeLabel(par.type)}}
  </div>
  <ol ui-tree-nodes="" ng-model="par.links" ng-class="{hidden: collapsed}" ng-if="!par.links.length" >
      <li ng-repeat="node in par.links" ui-tree-node >
      </li>
  </ol>

  <div class="tree-node tree-node-content crossnavigation-parameter link" ng-if="par.links.length" layout="row" layout-align="start center">
	  {{par.links[0].name}}
	  <i class="fa fa-link"></i>
	  {{par.name}}
    <span flex class="flex"></span>
	<md-button class="md-icon-button">
    	<md-icon md-font-icon="fa fa-times-circle" ng-click="ctrl.removeLink(par.id)"></md-icon>
	</md-button>
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
	 
	<md-content  flex layout>
		<angular-table flex	id="docList" 
			ng-model="listDoc" item-name="DOCUMENT_NAME"
			show-item-tooltip="false" highlights-selected-item="true"
			columns='[{"label":"Label","name":"DOCUMENT_LABEL"}, {"label":"Name","name":"DOCUMENT_NAME"}]'
			show-search-bar="true"
			columns-search='["DOCUMENT_LABEL","DOCUMENT_NAME"]'
			total-item-count = totalCount
			scope-functions = tableFunction 
			page-changed-function="changeDocPage(searchValue, itemsPerPage, currentPageNumber , columnsSearch,columnOrdering, reverseOrdering)"
			search-function="changeDocPage(searchValue, itemsPerPage, 0, columnsSearch, columnOrdering, reverseOrdering)"
			click-function="clickOnSelectedDoc(item,listId,closeDialog)">
		</angular-table>
	</md-content>	 
	
	 

<div ng-show="loading" class="loadingSpinner">
    <i class="fa fa-spinner fa-pulse fa-4x"></i> 
</div>
</md-dialog>
</script>

	<angular-list-detail ng-controller="navigationController as ctrl" full-screen="true"  >
       <list label="translate.load('sbi.crossnavigation.lst')" new-function="ctrl.newNavigation"> <!-- Requires an instruction like $scope.translate = sbiModule_translate on myController -->
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
		
        <detail label="ctrl.detail.simpleNavigation?ctrl.detail.simpleNavigation.name:'New navigation'" save-function="ctrl.saveFunc" cancel-function="ctrl.cancelFunc"> <!-- assuming that $scope.selectedItem stores the selected item on teh controller  -->
			<md-card>
			<md-card-content>
			<form name="tsForm" novalidate >			
				<div layout="row" layout-wrap>
					<div flex="50" layout="column">
						<md-input-container class="md-block">  
							<label>{{translate.load("sbi.crossnavigation.name");}}</label>
							<input md-maxlength="40" type="text" name="name" ng-model="ctrl.detail.simpleNavigation.name" required > 
							<div ng-messages="tsForm.name.$error" ng-show="tsForm.name.$dirty && tsForm.name.$invalid">
						    	<div ng-message="md-maxlength">{{translate.load("sbi.crossnavigation.name.invalid");}}</div>
						    </div> 
						</md-input-container>
						<md-input-container class="md-block"> 
							<label>{{translate.load("sbi.crossnavigation.description");}}</label>
							<input md-maxlength="200" type="text" name="description" ng-model="ctrl.detail.simpleNavigation.description" > 
							<md-icon md-font-icon="fa fa-info-circle"  class="md-knowage-theme" ng-click="ctrl.showHints('Description')"></md-icon>
							<div ng-messages="tsForm.name.$error" ng-show="tsForm.name.$dirty && tsForm.name.$invalid">
						    	<div ng-message="md-maxlength">{{translate.load("sbi.crossnavigation.description.invalid");}}</div>
						    </div>
						</md-input-container>
					</div>
					<div flex="50"  layout="column">
						<div layout="row" layout-align="start center">
							<md-input-container class="md-block" style="height:50px" flex>
								<label>{{translate.load("sbi.crossnavigation.modality.lbl")}}</label>
					        	<md-select ng-model="ctrl.crossmodality" ng-model-options="{trackBy: '$value.value'}">
					          		<md-option ng-value="crossMode" ng-repeat="crossMode in crossModes">{{crossMode.label}}</md-option>
					        	</md-select>			      
					      	</md-input-container>
					      	<md-input-container class="md-block" style="height:50px" ng-if="ctrl.crossmodality.value == 2" flex="25">
								<label>{{translate.load("sbi.crossnavigation.window.width")}}</label>
					        	<input type="number" name="width" ng-model="ctrl.popupOptions.width" min="0"> 		   
					        	<div class="hint">{{translate.load("sbi.crossnavigation.window.width.hint")}}</div>   
					      	</md-input-container>
					      	<md-input-container class="md-block" style="height:50px" ng-if="ctrl.crossmodality.value == 2" flex="25">
								<label>{{translate.load("sbi.crossnavigation.window.height")}}</label>
							    <input type="number" name="height" ng-model="ctrl.popupOptions.height" min="0"> 
							    <div class="hint">{{translate.load("sbi.crossnavigation.window.height.hint")}}</div>
					      	</md-input-container>
						</div>
					      
					      <md-input-container class="md-block"> 
							<label>{{translate.load("sbi.crossnavigation.breadcrumb");}}</label>
							<input md-maxlength="200" type="text" name="breadcrumb" ng-model="ctrl.detail.simpleNavigation.breadcrumb" > 
							<md-icon md-font-icon="fa fa-info-circle" class="md-knowage-theme" ng-click="ctrl.showHints('Breadcrumb')"></md-icon>
							<div ng-messages="tsForm.name.$error" ng-show="tsForm.name.$dirty && tsForm.name.$invalid">
						    	<div ng-message="md-maxlength">{{translate.load("sbi.crossnavigation.breadcrumb.invalid");}}</div>
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
						<div layout="row" ng-if="ctrl.detail.simpleNavigation.fromDoc" >
							<md-input-container flex> <label>{{translate.load("sbi.crossnavigation.fixedValue");}}</label>
								<input maxlength="100" type="text" ng-model="ctrl.tmpfixedValue" > 
							</md-input-container>
							<md-button ng-click="ctrl.addFixedParam()" class="md-fab md-mini" > 
								<md-icon md-font-icon="fa fa-plus" >
								</md-icon> 
							</md-button>
						</div>
					    <div ui-tree="ctrl.treeOptions" id="tree1-root" data-nodrop-enabled="true" data-clone-enabled="true" ng-show="ctrl.detail.fromPars.length>0">
					      <ol ui-tree-nodes="" ng-model="ctrl.detail.fromPars" data-nodrop-enabled="true">
					        <li ng-repeat="par in ctrl.detail.fromPars" ui-tree-node ng-include="'nodes_renderer1.html'" ></li>
					      </ol>
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
			</md-card-content>
			</md-card>
		</detail>
	</angular-list-detail>
</body>
</html>


<%}else{ %>
Access Denied
<%} %>

