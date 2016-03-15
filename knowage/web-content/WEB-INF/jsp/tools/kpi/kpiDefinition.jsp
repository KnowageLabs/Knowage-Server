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

<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/kpi/kpiCustomStyle.css", currTheme)%>">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/kpiDefinitionController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/kpiDefinitionSubController/cardinalityController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/kpiDefinitionSubController/filtersController.js"></script>
<script type="text/ng-template" id="templatesaveKPI.html">
<md-dialog aria-label="Select Function"  ng-cloak>
  <form>
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h1>Save new KPI</h1>
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
         >
		<md-item-template>
          <span md-highlight-text="searchText">{{item.valueCd}}</span>
        </md-item-template> 
      </md-autocomplete>    
     </div>
    
	<div class="footer">
	<md-button class="dialogButton" ng-click="apply()" ng-disable="kpi.name.length==0" md-autofocus>Apply <md-icon md-font-icon="fa fa-check buttonIcon" aria-label="apply"></md-icon></md-button>
	</div>
   	 </md-dialog-content>
  </form>
</md-dialog>
</script>


<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.css">
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/theme/eclipse.css">  
  <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.js"></script>  
 <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/ui-codemirror.js"></script> 
 <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/mathematica/mathematica.js"></script>  

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.css" />
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/sql-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/clike/clike.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/selection/mark-selection.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/kpiDefinitionSubController/formulaController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/kpiDefinitionSubController/listController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/kpiDefinitionSubController/thresholdController.js"></script>



</head>
<body>
	<angular-list-detail ng-controller="kpiDefinitionMasterController" full-screen=true >
		<list label="translate.load('sbi.kpi.list')" ng-controller="listController" new-function="addKpi" >
		<angular-table 
		id='kpiListTable' ng-model=kpiList
		columns='[{"label":"Name","name":"name"},{"label":"DateCreation","name":"datacreation"},{"label":"Category","name":"valueCd"},{"label":"Author","name":"author"}]'
		columnsSearch='["name"]' show-search-bar=true
		speed-menu-option=measureMenuOption 
		scope-functions=tableFunction 
		click-function="loadKPI(item);"> </angular-table>
		</list>
		<detail save-function="parseFormula" cancel-function="cancel">
 	
		<md-tabs layout-fill class="absolute" md-selected='selectedTab.tab'>
				<md-tab id="tab1" >
       				<md-tab-label>{{translate.load("sbi.kpi.formula")}}<span ng-show="formulaModified.value">*</span></md-tab-label>
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
				
				<md-tab id="tab3"  md-on-select="setFilters()">
       				<md-tab-label>{{translate.load("sbi.kpi.filters")}}</md-tab-label>
        			<md-tab-body>
        			<%@include	file="./kpiTemplate/filtersTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
				
				<md-tab id="tab4" md-on-select="loadThreshold()" >
       				<md-tab-label>{{translate.load("sbi.kpis.threshold")}}</md-tab-label>
        			<md-tab-body>
        			<%@include	file="./kpiTemplate/thresholdTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
			</md-tabs>
		
	


		</detail>
	</angular-list-detail>
</body>
</html>
