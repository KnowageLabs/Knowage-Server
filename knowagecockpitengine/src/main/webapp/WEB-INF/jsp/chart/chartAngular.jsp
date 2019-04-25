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

<%-- 
author: Danilo Ristovski (danristo, danilo.ristovski@mht.net)
--%>

<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html ng-app="chartExecutionManager">

	<%-- == HEAD ========================================================== --%>
	<head>
		
		<%-- ---------------------------------------------------------------------- --%>
		<%-- IMPORT [START]															--%>
		<%-- ---------------------------------------------------------------------- --%>
		
		<!-- Getting all the data resource that is needed for execution of the chart (Java code). -->
		<%@include file="commons/angular/angularResource.jspf"%>
		
		<!-- 
			Imports all Angular resources that are needed for the Angular application (e.g. libraries), 
			including the sbiModule. 
		-->
		<%@include file="commons/angular/angularImport.jsp"%>
		
		<!-- Imports all JS files needed by our Angular application (main application, controller, configurations etc.). -->
		<%@include file="commons/angular/chartExecutionImport.jsp"%>
		
		<%@include file="/WEB-INF/jsp/chart/execution/chartRenderImport.jsp"%>
		
		<!-- TODO: provide comment!!! -->
		<script src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/chart/ChartExecutionWebServiceManagerFactory.js")%>"></script>
		
		<!-- 
			Import the D3 library, needed for the rendering of charts that are implemented in this language 
			(SUNBURST, WORDCLOUD, PARALLEL, CHORD).
		 -->
		<script type="text/javascript" src='${pageContext.request.contextPath}/js/lib/d3/d3.js'/></script>
		
		<%-- ---------------------------------------------------------------------- --%>
		<%-- IMPORT [END]															--%>
		<%-- ---------------------------------------------------------------------- --%>
		
		<title>
			<%=docName.trim().length() > 0 ? docName: "ChartEngine"%>
		</title>
		
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
						
		<script>
		
			/* Parameters needed for the WebServiceManagerFactory. */
			var sbiExecutionId = <%=request.getParameter("SBI_EXECUTION_ID")!=null? "'"+request.getParameter("SBI_EXECUTION_ID")+"'" : "null"%>;
			var userId = '<%=userId%>'; <%-- e.g. biadmin --%>
			var hostName = '<%=request.getServerName()%>'; <%-- e.g. localhost --%>
			var serverPort = '<%=request.getServerPort()%>'; <%-- e.g. 8080 --%>
			var protocol = window.location.protocol; <%-- e.g. http: --%>
			var driverParams = '<%=driverParams%>'; <%-- e.g. object that hold values for IS_TECHNICAL_USER, MODALITY, SBI_ENVIRONMENT, user_id, etc. --%>

			var currentDocumentName = '<%=docName%>'; <%-- name of the document --%>
			var currentDocumentLabel = '<%=docLabelComositeDoc%>'; <%-- label of the document --%>
			
			// Global variable that serves as an indicator if the chart type of the rendered document is ChartJS. (danristo)			
			var isLibChartJs = '<%=isLibChartJS%>';
			//global variable that indicates that table accessable for screen reader should be visible or not
			var includeChartTable= false; 
		</script>
		
		<% if (template != null && !template.equals("") && !template.matches("^\\{\\s*\\}$")) { %>
			
			<%-- 
				The 'page' attribute of the "jsp:include" XML tag provides an inclusion of the JSP page that is responsible for 
				initialization of the chart rendering for different chart types (libraries): Highcharts and D3. Namely, depending
				on the type of the chart, i.e. the library that is used for rendering of the file (template) of that chart type,
				we will include these JSP files: the "chartlib/d3js244Initializer.jsp" (for the SUNBURST, CHORD, WORDCLOUD and 
				PARALLEL chart types) and the "chartlib/highcharts414Initializer.jsp" (for all other chart types). They contain
				all initialization functions, functions that are used for interactivity of the chart (drill-down, cross-navigation),
				as well as for rending the chart.
			 --%>
<%-- 			<jsp:include page="<%=ChartEngineUtil.getLibraryInitializerPath(template,docLabel,profile)%>"> --%>
<%-- 				<jsp:param name="template" value="<%=template%>"/> --%>
<%-- 			</jsp:include> --%>
			
		<% } %>
		
		<!-- TODO: Move this to .css file -->
		<style type="text/css">
			.highcharts-container {
				margin: auto;
			}
		</style>
	
	</head>

	<%-- == BODY ========================================================== --%>
	<body ng-controller="chartExecutionController" ng-style="bodyStyleConfig" layout="column">
	    
		<!-- Show the information in the DIV above the rendered chart that the chart is exporting - downloading. (danristo) -->
	  	<div style="background-color: #a9c3db;">
		  	<div style="align:center; padding: 5px;" ng-show="showDownloadProgress">		
				<md-icon class="fa fa-download" style="display: inline;"></md-icon>
				<span style="color: #3b678c">Downloading... </span>	
		  	</div>
	  	</div>
	   	
	   	<!-- Show the circular loading animation after the chart JSON is received and before it is rendered completely. (danristo) -->
	   	<div ng-show="loadingChart">
			<md-progress-circular md-mode="indeterminate" class="md-accent" style="position:fixed; top:calc(50% - 37.5px); left:calc(50% - 37.5px); z-index:100;"  md-diameter="75">
			</md-progress-circular>			
	   	</div>
   		   	
		<%-- 
			If the executed document has a template (the chart that is going to be rendered), continue
			with the chart execution (the chart rendering).
		--%>
		<% if (template!=null && !template.equals("") && !template.matches("^\\{\\s*\\}$")) { %>
			
			<%-- == JAVASCRIPT  ===================================================== --%>
			<script language="javascript" type="text/javascript">		
				
				// Initialization of variable that are going to be used by the controller.
				var thisContextName, locale, driverParams, jsonTemplate, datasetLabel, isCockpit = null,
				aggregations, selections, associations, widgetId, metaData;
				
				/*
					The locale (language) is used when rendering D3 charts, for formatting localization of the series values
					that are displayed on charts in various ways (values in tables, values in tooltip etc.).
					@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				*/
				locale = '<%=locale%>';	
				thisContextName = '${pageContext.request.contextPath}';  //'knowagechartengine'
		 		driverParams = '<%=driverParams%>';
	 			jsonTemplate = '<%=template%>';
	 			datasetLabel = '<%=datasetLabel%>';
	 			isCockpit = <%=isCockpit%>;
	 			
				aggregations = '<%=aggregations%>';
	 			selections = '<%=selections%>';
	 			associations = '<%=associations%>';
	 			widgetId = '<%=widgetId%>';
	 			metaData = '<%=metaData%>';
	 			 			
	 			/* 
	 				Set the exportChart variable to NULL. This variable will be predefined into a function that will serve
	 				for exporting of a chart to the JPG or PDF format file. This way, by declaring it in the JSP, the web
	 				page's (window's) iframe with the ID of "documentFrame" will see it (that finction) when attempting to
	 				export the chart.
	 			*/
	 			var exportChart = null;
	 			
	 			/*
	 				The Settings.js structure that will be assigned to this global variable and used whenever the customization
	 				from this file is needed in the JS code.
	 				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 			*/
	 			var chartEngineSharedSettings = null;
	 			
	 			var chartExecutionWebServiceManager = null;
	 			
	 			//dragan
	 			var chartLibNamesConfig = <%=ChartEngineUtil.getChartLibNamesConfig()%>;
	 				
			</script>
			
		
		

			
			<chart-renderer  flex
					chart-lib-names-config='chartLibNamesConfig'
					chart-template='chartTemplate'
					dataset-label='datasetLabel'
					>

			</chart-renderer>
			
			<chart-table ng-if="isLibChartJs && includeChartTable" ng-model="chartTableData.data.datasets" categories="chartTableData.data.labels"></chart-table>
			
			<form id="export-chart-form" class="export-form">
				<input type="hidden" name="options"/>
				<input type="hidden" name="content"/>
				<input type="hidden" name="type"/>
				<input type="hidden" name="width"/>
				<input type="hidden" name="constr"/>
				<input type="hidden" name="async"/>
				<input type="hidden" name="chartHeight"/>
				<input type="hidden" name="chartWidth"/>
			</form>		
	
		<%-- 
			If the executed document does not have a template (the chart that is going to be rendered), display a 
			proper message. 
		--%>	
		<% } else {%>		
			
			<div>
				<p style="padding: 10px 0px 0px 10px;">{{translate.load("sbi.generic.error.notemplate")}}</p>
			</div>
			
		<% }%>
	
	</body>
	
</html>