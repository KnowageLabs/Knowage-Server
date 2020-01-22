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

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jsp"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="metaManager">

<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%
String datasourceId= request.getParameter("datasourceId");
String bmName= request.getParameter("bmName");
String bmId= request.getParameter("bmId");
%>

<script> 
var datasourceId='<%= datasourceId%>';
var bmId='<%= bmId%>';
var bmName='<%= StringEscapeUtils.escapeJavaScript(bmName) %>';
var translatedModel=<%= translatedModel%>;
</script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>meta Definition</title>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/associatordirective/associatordirective.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/metaDefinitionController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/metaModelCreationController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/metaModelDefinitionController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/dialogController/addBusinessClassDialogController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/dialogController/addBusinessViewDialogController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/dialogController/addInboundRelationship.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/dialogController/addOutboundRelationship.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/dialogController/addCalculatedFieldController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/dialogController/refreshPhysicalModelController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/dialogController/bvPhisicalTablesController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/dialogController/editTemporalHierarchyController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/services/businessViewFilterService.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/controllers/businessModelSqlFilterController.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/interceptors/businessViewFilterInterceptor.js"></script>
	
	<!-- DIRECTIVES -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/meta/directives/reorder/reorder.js"></script>
	
	<base href="/" /> <!-- mandatory for HTML5 and $location usage -->
	
</head>
<body ng-controller="metaDefinitionController" layout="column" class="kn-metaWeb" ng-switch on="steps.current">

<rest-loading></rest-loading>
	<angular-list-detail full-screen=true>
		<list layout="column" label="bmName" ng-controller="metaModelDefinitionController"> 
			<angular-table flex id='datasourceStructureListTable' ng-model=dataSourceStructure
				columns='datasourceStructureColumnsList'
				columns-search=['columnName']
				scope-functions='datasourceStructureScopeFunctions'
			 	show-search-bar=true no-pagination="true"
			 	sortable-column="['columnName']"
			 	class="alternatedRows" >
			 	
			 	<queue-table>
				 	<md-divider ></md-divider>
			 		<div layout="row" layout-align="space-around center" >
				 		<md-checkbox aria-label='check all business' ng-checked='scopeFunctions.allPhysicalModelAreChecked()' ng-click='scopeFunctions.toggleAllPhysicalModel()'> {{scopeFunctions.translate.load("sbi.meta.model.physical.selectAll")}}</md-checkbox>
				 		<md-checkbox aria-label='check all business' ng-checked='scopeFunctions.allBusinessModelAreChecked()' ng-click='scopeFunctions.toggleAllBusinessModel()'>  {{scopeFunctions.translate.load("sbi.meta.model.business.selectAll")}}</md-checkbox>
			 		</div>
			 	</queue-table>
	 		 </angular-table>
		</list>
		<extra-list-button>
		 <md-button ng-click="closeMetaDefinition()">{{translate.load("sbi.general.close")}}</md-button>
		 <md-button ng-click="continueToMeta()" >{{translate.load("sbi.general.continue")}}</md-button>
		</extra-list-button>
		<extra-button>
		 <md-button ng-click="closeMetaDefinition()">{{translate.load("sbi.general.close")}}</md-button>
		</extra-button>
		<detail id="metaWebView" label="bmName" ng-controller="metaModelCreationController" save-function="saveModel" >
			<md-tabs flex class="metaTabs">
				<md-tab id="businessTab">
					<md-tab-label>{{translate.load("sbi.meta.model.business")}}</md-tab-label>
					<md-tab-body >
						<%@include	file="./metaWebTemplates/businessModelTab.jsp"%>
					</md-tab-body>
				</md-tab>
				
				<md-tab id="physicalTab">
					<md-tab-label>{{translate.load("sbi.meta.model.physical")}}</md-tab-label>
					<md-tab-body>
						<%@include	file="./metaWebTemplates/physicalModelTab.jsp"%>
					</md-tab-body>
				</md-tab>
			</md-tabs>
		</detail>
	</angular-list-detail>	
	

</body>
</html>

<style>
angular-table#datasourceStructureListTable .centerCheckbox{
	margin-left: 50%;
}
angular-table#datasourceStructureListTable .centerHeadText{
    text-align: center;
}

.goldKey{
color: gold !important;
}

component-tree#bcmTree>div ,component-tree#bvmTree>div , component-tree#pmTree>div{
margin:20px;
}

md-input-container.md-knowage-theme .md-input[disabled], md-input-container.md-knowage-theme .md-input [disabled]{
	color: black;
}

md-tabs.md-knowage-theme .md-tab.md-active{
    font-weight: 500;
}

expander-box{
background: white;
}

#metaWebView.kn-detail>md-content.kn-detail-content,#metaWebView.kn-detail>md-content.kn-detail-content>md-tabs{
margin: 0!important
}

.kn-detail .md-button.md-knowage-theme.md-fab {
    top: 19px;
}

.fullScreenPanel {
	    position: absolute;
   		width: 90%;
   		height: 90%;
   		margin: 2% 5%;
	}
	
.flexCard{
     display: flex !important;
   }
.md-scroll-mask {
    background-color: rgba(0, 0, 0, 0.5) !important;
    }
    
@media all and (-ms-high-contrast: none), (-ms-high-contrast: active) {

	md-tabs.metaTabs>md-tabs-content-wrapper>md-tab-content>div{
	height: 100%;
	}

}
</style>