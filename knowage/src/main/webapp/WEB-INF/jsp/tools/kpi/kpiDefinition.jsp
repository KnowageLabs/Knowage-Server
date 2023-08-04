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


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="kpiDefinitionManager">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>KPI definition</title>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<link rel="stylesheet" type="text/css"	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>"> 
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/kpi/kpiCustomStyle.css", currTheme)%>">
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/kpi/kpiDefinitionController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/kpi/kpiDefinitionSubController/cardinalityController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/kpi/kpiDefinitionSubController/filtersController.js")%>"></script>
<script type="text/ng-template" id="templatesaveKPI.html">
<md-dialog aria-label="Select Function"  ng-cloak>
  <form>
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h1>{{translate.load('sbi.kpi.addkpiassociation')}}</h1>
        <span flex></span>
        <md-button class="md-icon-button" ng-click="close()">
          <md-icon md-font-icon="fa fa-times closeIcon" aria-label="Close dialog"></md-icon>
        </md-button>
      </div>
	
    </md-toolbar>
    <md-dialog-content >
     <div class="md-dialog-content">
		<md-input-container class="small counter" class="small counter">
			<label>Name</label>
			<input class="input_class" ng-model="kpi.name" required maxlength="100" ng-maxlength="100" md-maxlength="100">
		 </md-input-container>
	<md-autocomplete 
          ng-disabled="false" 
          md-selected-item="kpi.category" 
          md-search-text="searchText" 
          md-items="item in querySearchCategory(searchText)"
          md-item-text="item.valueCd" 
          md-floating-label="Categoria"
          md-autoselect	="true"
		  md-search-text-change="searchTextChange(kpi,searchText)"
         >
		<md-item-template>
          <span md-highlight-text="searchText">{{item.valueCd}}</span>
        </md-item-template> 
      </md-autocomplete>  

	<md-checkbox ng-model="kpi.enableVersioning">{{translate.load('sbi.kpi.edit.enable.versioning')}}</md-checkbox>  
     </div>
</md-dialog-content>
    <md-dialog-actions layout="row">
	<md-button class="dialogButton" ng-click="apply()" ng-disable="kpi.name.length==0" md-autofocus>Save <md-icon md-font-icon="fa fa-check buttonIcon" aria-label="apply"></md-icon></md-button>
   	 </md-dialog-actions>
  </form>
</md-dialog>
</script>


<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.css")%>">
  <link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/theme/eclipse.css")%>">  
  <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.js")%>"></script>  
 <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/ui-codemirror.js")%>"></script> 
 <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/mathematicaModified.js")%>"></script>  

<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.css")%>" />
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/hint/sql-hint.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/mode/clike/clike.js")%>"></script>
<script src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/codemirror/CodeMirror-master/addon/selection/mark-selection.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/kpi/kpiDefinitionSubController/formulaController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/kpi/kpiDefinitionSubController/listController.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/kpi/kpiDefinitionSubController/thresholdController.js")%>"></script>



</head>
<body class="kn-kpi-definition" ng-cloak>
	<angular-list-detail ng-controller="kpiDefinitionMasterController" full-screen=true>
		<list label="translate.load('sbi.kpi.list')" ng-controller="listController" new-function="addKpi" >
			<div layout-padding>
				<div layout="row" class="noPadding" layout-align="start end">
					<md-icon md-font-icon="fa fa-search" aria-hidden="true"></md-icon> 
					<md-input-container class="md-icon-float md-block" flex>
						<input ng-model="searchVal" type="text" placeholder="search" aria-label="search text" aria-hidden="false" aria-invalid="false">
					</md-input-container>
				</div>
				<table class="kn-table kn-table-clickable-rows kn-table-fixed">
					<thead>
						<tr>
							<th ng-repeat='col in columns' ng-click="toggleOrder(col)">{{col.label}} <i ng-if="col.name == listOrder" ng-class="{'rotate-180':listDirection && col.name == listOrder}" class="rotate-transition fa fa-arrow-down"></i></th>
							<th class="multiTableAction"></th>
						</tr>
					</thead>
					<tbody>
						<tr ng-repeat="row in kpiList | orderBy : listOrder : listDirection | filter:searchVal" ng-click="loadKPI(row)">
							<td ng-repeat='col in columns'>{{row[col.name]}}</td>
							<td class="multiTableAction">
								<md-button class="md-icon-button" ng-click="cloneKpi(row,$event)">
									<md-tooltip md-delay="500">{{translate.load('sbi.generic.clone')}}</md-tooltip>
									<md-icon md-font-icon="fa fa-copy"></md-icon>
								</md-button>
								<md-button class="md-icon-button" ng-click="deleteMeasure(row,$event)">
									<md-tooltip md-delay="500">{{translate.load('sbi.generic.delete')}}</md-tooltip>
									<md-icon md-font-icon="fa fa-trash"></md-icon>
								</md-button>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<!--  angular-table flex 
			id='kpiListTable' ng-model=kpiList
			columns='[{"label":"Name","name":"name"},{"label":"DateCreation","name":"datacreation"},{"label":"Category","name":"valueCd"},{"label":"Author","name":"author"}]'
			columns-search='["name","valueCd","author"]' show-search-bar=true
			speed-menu-option=measureMenuOption 
			scope-functions=tableFunction 
			click-function="loadKPI(item);"> </angular-table -->
		</list>
		<extra-button>
			  <md-button class="md-flat" ng-click="showAliasTab=!showAliasTab;" >{{translate.load("sbi.kpi.alias")}}</md-button>
		</extra-button>
		<detail save-function="parseFormula" cancel-function="cancel" label="getKpiName()">
 		<div layout="row" class="absolute" layout-fill>
		<md-tabs flex md-selected='selectedTab.tab' class="hidden-overflow-orizontal">
				<md-tab id="tab1" >
       				<md-tab-label>{{translate.load("sbi.kpi.formula")}}<span ng-if="formulaModified.value">*</span></md-tab-label>
        			<md-tab-body>
        			<%@include	file="./kpiTemplate/formulaTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
				<md-tab id="tab2" md-on-select="setCardinality()">
       				<md-tab-label >{{translate.load("sbi.kpi.cardinality")}}</md-tab-label>
        			<md-tab-body >
        			<%@include	file="./kpiTemplate/cardinalityTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
				<md-tab id="tab4" md-on-select="loadThreshold()" >
       				<md-tab-label>{{translate.load("sbi.kpis.threshold")}}</md-tab-label>
        			<md-tab-body>
        			<%@include	file="./kpiTemplate/thresholdTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
			</md-tabs>
		
			
		<md-sidenav class="md-sidenav-right md-whiteframe-z2" md-component-id="aliasTab" md-is-locked-open="showAliasTab">
	      <md-toolbar class="secondaryToolbar">
	        <h1 class="md-toolbar-tools">{{translate.load("sbi.kpi.alias")}}</h1>
	      </md-toolbar>
	     	<md-content layout-margin flex class="relative">
	        <angular-list layout-fill class="absolute" id="aliasListANGL"
                		ng-model=measures
                		item-name='alias' 
                		show-search-bar=true 
                		>
                		</angular-list> 
	      </md-content>
	    </md-sidenav>
	</div>

		</detail>
	</angular-list-detail>
</body>
</html>
