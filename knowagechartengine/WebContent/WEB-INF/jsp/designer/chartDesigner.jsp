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
<html>

	<head>
	
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		<%@include file="/WEB-INF/jsp/designer/chartImport.jsp"%>
	
		<link rel="stylesheet" type="text/css" href="<%=GeneralUtilities.getSpagoBiContext()%>/themes/commons/css/customStyle.css">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular/designer/chartDesigner.js"></script>
	
		<script>
		
			var sbiExecutionId = <%=request.getParameter("SBI_EXECUTION_ID")!=null? "'"+request.getParameter("SBI_EXECUTION_ID")+"'" : "null"%>;
			var userId = '<%=userId%>';
			var hostName = '<%=request.getServerName()%>';
			var serverPort = '<%=request.getServerPort()%>';
			var docLabel = '<%= docLabel %>';			
			var jsonTemplate = JSON.parse('<%=template.replaceAll("&#39;","\\\\'")%>');
			var datasetLabel  = '<%=datasetLabel%>'; 
			
			var chartLibNamesConfig = <%=ChartEngineUtil.getChartLibNamesConfig()%>;
 			
 			var isCockpit = <%=isCockpit%>;
 			
			var thisContextName			= '${pageContext.request.contextPath}';  <%-- knowagechartengine --%>
			thisContextName = thisContextName.replace('/','');
			var mainContextName 		= '<%=contextName.replaceAll("/", "")%>';  <%-- knowage --%>
			var exporterContextName 	= 'highcharts-export-web';
			
		
		</script>
	
	</head>

	<body class="bodyStyle" ng-app="chartDesignerManager" layout="column">
	
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2 flex><%=docLabel%></h2>
				<span class="extraButtonContainer"></span>
				<md-button aria-label="Preview" ng-click="previewChart()">PREVIEW</md-button>
				<md-button aria-label="Save" ng-click="saveChartTemplate()">SAVE</md-button>
				<md-button aria-label="Back" ng-click="goBackFromDesigner()">BACK</md-button>
			</div>
		</md-toolbar>
	
		<md-tabs md-selected="selectedTab" flex> 
			<md-tab	label='chart' chart-tab> </md-tab> 
			<md-tab label='structure'> </md-tab> 
			<md-tab	label='configuration' configuration-tab> </md-tab>
			<md-tab label='advanced'> </md-tab> 
		</md-tabs>
	
	</body>
	
</html>