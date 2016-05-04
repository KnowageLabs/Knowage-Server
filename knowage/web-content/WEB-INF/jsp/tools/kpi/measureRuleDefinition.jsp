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
<html ng-app="measureRuleManager">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.css">
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/theme/eclipse.css">  
  <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.js"></script>  
 <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/ui-codemirror.js"></script> 
 <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/sql/sql.js"></script>  

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.css" />
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/sql-hint.js"></script>

<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/kpi/measureRuleCustomStyle.css", currTheme)%>">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/measureRuleDefinition.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/measureRuleSubController/measureRuleQueryController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/measureRuleSubController/measureRuleMetadataController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/measureRuleSubController/measureRulePreviewController.js"></script>

</head>
<body>
	<angular-list-detail ng-controller="measureRuleMasterController"  full-screen="true">
		
		<list label="translate.load('sbi.kpi.measure.list')" ng-controller="measureListController" new-function="newMeasureFunction" layout-column>
	 	<angular-table flex id='measureListTable' ng-model=measureRuleList
				columns='measureRuleColumnsList'
			 	 show-search-bar=true
			 	 speed-menu-option=measureMenuOption
				 click-function="measureClickFunction(item);" > </angular-table>
		</list>
		<extra-button>
			  <md-button class="md-flat" ng-click="showAliasTab=!showAliasTab;" >{{translate.load("sbi.kpi.alias")}}</md-button>
			  <md-button class="md-flat" ng-click="showPlaceholdersTab=!showPlaceholdersTab">{{translate.load("sbi.kpi.placeholder")}}</md-button>
		</extra-button>
		
<!-- 		<detail ng-controller="measureDetailController" save-function="saveMeasureFunction" disable-save-button="!detailProperty.dataSourcesIsSelected || (detailProperty.dataSourcesIsSelected && currentRule.definition=='')" cancel-function="cancelMeasureFunction"  > -->
				
		<detail label="getLabelToBar()" ng-controller="measureDetailController" save-function="saveMeasureFunction" disable-save-button="!detailProperty.dataSourcesIsSelected || (detailProperty.dataSourcesIsSelected && currentRule.definition=='')" cancel-function="cancelMeasureFunction"  >

		<div layout="row" class="absolute" layout-fill>
		 
			<md-tabs flex md-selected='selectedTab.tab' >
			
				<md-tab id="tab1">
       				<md-tab-label>{{translate.load("sbi.ds.query")}}</md-tab-label>
        			<md-tab-body >
        				<%@include	file="./measureRuleTemplate/queryTemplate.jsp"%>
        			</md-tab-body>
				</md-tab>
				
				<md-tab id="tab2"  md-on-select="loadMetadata()" ng-disabled="!detailProperty.dataSourcesIsSelected">
       				<md-tab-label>{{translate.load("sbi.execution.executionpage.toolbar.metadata")}}</md-tab-label>
        			<md-tab-body  >
        			<%@include	file="./measureRuleTemplate/metadataTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
				
				<md-tab id="tab3" md-on-select="loadPreview(true)" ng-disabled="!detailProperty.dataSourcesIsSelected">
       				<md-tab-label>{{translate.load("sbi.ds.test")}}</md-tab-label>
        			<md-tab-body>
        			<%@include	file="./measureRuleTemplate/previewTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
				
			</md-tabs> 
		
		<md-sidenav class="md-sidenav-left md-whiteframe-z2" md-component-id="aliasTab" md-is-locked-open="showAliasTab">
	      <md-toolbar>
	        <h1 class="md-toolbar-tools">{{translate.load("sbi.kpi.alias")}}</h1>
	      </md-toolbar>
	      <md-content layout-margin flex class="relative" >
	        <angular-list layout-fill class="absolute" id="aliasListANGL"
                		ng-model=aliasList
                		item-name='name' 
                		show-search-bar=true 
                		>
                		</angular-list> 
	      </md-content>
	    </md-sidenav>
	    
	    <md-sidenav class="md-sidenav-left md-whiteframe-z2" md-component-id="placeholderTab" md-is-locked-open="showPlaceholdersTab">
	      <md-toolbar>
	        <h1 class="md-toolbar-tools">{{translate.load("sbi.kpi.placeholder")}}</h1>
	      </md-toolbar>
	      <md-content layout-margin flex class="relative"> 
	        <angular-list layout-fill class="absolute" id="placeholderListANGL"
                		ng-model=placeholderList
                		item-name='name' 
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
