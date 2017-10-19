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
<%@include file="/WEB-INF/jsp/chart/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="chartDesignerManager">

	<head>
	
		<%@include file="/WEB-INF/jsp/chart/commons/angular/angularImport.jsp"%>
		<%@include file="/WEB-INF/jsp/chart/designer/chartImport.jsp"%>
		<%@include file="/WEB-INF/jsp/chart/execution/chartRenderImport.jsp"%> 
	
		<link rel="stylesheet" type="text/css" href="<%=GeneralUtilities.getSpagoBiContext()%>/themes/commons/css/customStyle.css">
		
	
		<script>
		
			var sbiExecutionId = <%=request.getParameter("SBI_EXECUTION_ID")!=null? "'"+request.getParameter("SBI_EXECUTION_ID")+"'" : "null"%>;
			var userId = '<%=userId%>';
			var hostName = '<%=request.getServerName()%>';
			var serverPort = '<%=request.getServerPort()%>';
			var sbiHost = '<%=request.getParameter(SpagoBIConstants.SBI_HOST)%>';
			var docLabel = '<%= docLabel %>';
			var docId = '<%= docId %>';
			
			var template = '<%=template.replaceAll("&#39;","\\\\'")%>';
			var datasetLabel  = '<%=datasetLabel%>'; 
			
			var chartLibNamesConfig = <%=ChartEngineUtil.getChartLibNamesConfig()%>;
 			
 			var isCockpit = <%=isCockpit%>;
 			
			var thisContextName			= '${pageContext.request.contextPath}';  <%-- knowagechartengine --%>
			thisContextName = thisContextName.replace('/','');
			var mainContextName 		= '<%=contextName.replaceAll("/", "")%>';  <%-- knowage --%>
			var exporterContextName 	= 'highcharts-export-web';
		
		</script>
	
	</head>

	<body class="bodyStyle kn-chartdesigner" ng-controller="ChartDesignerController">
		<form name="userForm" layout="column" layout-fill ng-submit="saveChartTemplate()">
		<div layout-fill style='position:fixed;z-index: 500;background:rgba(0,0,0, 0.3);' ng-if="!chartTypes.length>0">
			<md-progress-circular md-mode="indeterminate" style='top:50%;left:50%' ng-disabled="chartTypes.length>0"></md-progress-circular>
		</div>
		
		<md-toolbar ng-if=!isCockpitEng>
			<div class="md-toolbar-tools">
				<h2 flex><%=docLabel%></h2>
				<span class="extraButtonContainer"></span>
<!-- 				<md-button aria-label="Preview" ng-click="testChart()">TEST</md-button> -->
				<!--commented perview button -->

				<md-button aria-label="Preview" ng-if="previewChartEnable" ng-disabled="userForm.$invalid" ng-click="previewChart()">{{translate.load("sbi.generic.preview")}}</md-button> 
				<md-button aria-label="Save" type="submit" ng-disabled="userForm.$invalid">{{translate.load("sbi.generic.save")}}</md-button>
				<md-button aria-label="Back" ng-click="goBackFromDesigner()">{{translate.load("sbi.generic.back")}}</md-button>
			</div>
		</md-toolbar>
		
		<md-tabs md-selected="selectedTab" flex> 
			<md-tab label='{{translate.load("sbi.chartengine.designer.tab.chart");}}'><chart-tab></chart-tab> </md-tab> 
			<md-tab>
				<md-tab-label>
                <div ng-class="structureForm.$invalid ? 'kn-danger' : ''">
                    {{translate.load("sbi.chartengine.designer.tab.structure");}}
                    <span >
                        <span/>
                    </span>
                </div>
            </md-tab-label>
            
            <md-tab-body>
                <div flex><chartstructure-tab></chartstructure-tab></div>
            </md-tab-body>
			
			</md-tab>
			
			<md-tab>
				<md-tab-label>
                <div ng-class="configurationForm.$invalid ? 'kn-danger' : ''">
                    {{translate.load("sbi.chartengine.designer.tab.configuration");}}
                    <span >
                        <span/>
                    </span>
                </div>
            </md-tab-label>
            
            <md-tab-body>
                <div flex><configuration-tab></configuration-tab></div>
            </md-tab-body>
			
			</md-tab> 

			<md-tab label='{{translate.load("sbi.chartengine.designer.tab.advanced");}}' md-on-select="refreshJsonTree()"><advanced-tab></advanced-tab> </md-tab> 
		</md-tabs>
		</form>

	</body>
	
</html>