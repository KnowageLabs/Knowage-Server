<%-- SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.  If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%-- 
author: 
--%>

<%@page import="it.eng.spagobi.engine.chart.model.conf.ChartConfig"%>
<%@page import="it.eng.spagobi.engine.chart.ChartEngineConfig"%>
<%@page import="it.eng.spagobi.engine.util.ChartEngineUtil"%>
<%@page import="it.eng.spagobi.engine.chart.ChartEngineInstance"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.XML"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	ChartEngineInstance engineInstance;
	IEngUserProfile profile;
	String profileJSONStr;
	Map env;
	String contextName;
	String environment;
	String executionRole;
	Locale locale;
	String template;
	String docLabel;
	String docVersion;
	String docAuthor;
	String docName;
	String docDescription;
	String docIsPublic;
	String docIsVisible;
	String docPreviewFile;
	String[] docCommunities;
	String docCommunity;
	List docFunctionalities;
	String userId;
	String isTechnicalUser;
	List<String> includes;
	String datasetLabel;
	
	//from cockpit
	boolean isCockpit = false;
	String aggregations = "";
	String selections = "";
	String associations = "";
	String widgetId = "";
	String metaData = "";

	engineInstance = (ChartEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
	env = engineInstance.getEnv();
	profile = engineInstance.getUserProfile();
	profileJSONStr = new ObjectMapper().writeValueAsString(profile);
	locale = engineInstance.getLocale();
	
	contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); 
	environment = request.getParameter("SBI_ENVIRONMENT"); 
	executionRole = (String)env.get(EngineConstants.ENV_EXECUTION_ROLE);
	userId = (engineInstance.getDocumentUser()==null)?"":engineInstance.getDocumentUser().toString();
	isTechnicalUser = (engineInstance.isTechnicalUser()==null)?"":engineInstance.isTechnicalUser().toString();
	template = engineInstance.getTemplate().toString(0);
	
	if(env.get("EXECUTE_COCKPIT") != null){
		isCockpit = true;
		datasetLabel = env.get(EngineConstants.ENV_DATASET_LABEL)!=null?(String)env.get(EngineConstants.ENV_DATASET_LABEL):"";
		aggregations = env.get("AGGREGATIONS")!=null?(String)env.get("AGGREGATIONS"):"";
		selections = env.get("SELECTIONS")!=null?(String)env.get("SELECTIONS"):"";
		associations = env.get("ASSOCIATIONS")!=null?(String)env.get("ASSOCIATIONS"):"";
		metaData = env.get("METADATA")!=null?(String)env.get("METADATA"):"";
		widgetId = env.get("WIDGETID")!=null?(String)env.get("WIDGETID"):"";
	}else{
		datasetLabel = engineInstance.getDataSet().getLabel();
	}
	
	docLabel = (engineInstance.getDocumentLabel()==null)?"":engineInstance.getDocumentLabel().toString();
	docVersion = (engineInstance.getDocumentVersion()==null)?"":engineInstance.getDocumentVersion().toString();
	docAuthor = (engineInstance.getDocumentAuthor()==null)?"":engineInstance.getDocumentAuthor().toString();
	docName = (engineInstance.getDocumentName()==null)?"":engineInstance.getDocumentName().toString();
	docDescription = (engineInstance.getDocumentDescription()==null)?"":engineInstance.getDocumentDescription().toString();
	docIsPublic= (engineInstance.getDocumentIsPublic()==null)?"":engineInstance.getDocumentIsPublic().toString();
	docIsVisible= (engineInstance.getDocumentIsVisible()==null)?"":engineInstance.getDocumentIsVisible().toString();
	docPreviewFile= (engineInstance.getDocumentPreviewFile()==null)?"":engineInstance.getDocumentPreviewFile().toString();	
	docCommunities= (engineInstance.getDocumentCommunities()==null)?null:engineInstance.getDocumentCommunities();
	docCommunity = (docCommunities == null || docCommunities.length == 0) ? "": docCommunities[0];
	docFunctionalities= (engineInstance.getDocumentFunctionalities()==null)?new ArrayList():engineInstance.getDocumentFunctionalities();
	
	boolean forceIE8Compatibility = false;
	
	boolean fromMyAnalysis = false;
	if(request.getParameter("MYANALYSIS") != null && request.getParameter("MYANALYSIS").equalsIgnoreCase("TRUE")){
		fromMyAnalysis = true;
	}else{
		if (request.getParameter("SBI_ENVIRONMENT") != null && request.getParameter("SBI_ENVIRONMENT").equalsIgnoreCase("MYANALYSIS")){
	fromMyAnalysis = true;
		}
	}
	
    Map analyticalDrivers  = engineInstance.getAnalyticalDrivers();
    Map driverParamsMap = new HashMap();
	for(Object key : engineInstance.getAnalyticalDrivers().keySet()){
		if(key instanceof String && !key.equals("widgetData")){
			String value = request.getParameter((String)key);
			if(value!=null){
				driverParamsMap.put(key, value);
			}
		}
	}
	String driverParams = new JSONObject(driverParamsMap).toString(0);
	String uuidO=request.getParameter("SBI_EXECUTION_ID")!=null? request.getParameter("SBI_EXECUTION_ID"): "null";
%>



<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
<%-- == HEAD ========================================================== --%>
<head>
<title><%=docName.trim().length() > 0? docName: "SpagoBICockpitEngine"%></title>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />

<%@include file="commons/includeExtJS5.jspf"%>
<%@include file="commons/includeMessageResource.jspf"%>

<script>
	var sbiExecutionId = <%=request.getParameter("SBI_EXECUTION_ID")!=null? "'"+request.getParameter("SBI_EXECUTION_ID")+"'" : "null"%>;
	//var sbiExecutionId = '<%=uuidO%>';
	var userId = '<%=userId%>';
	var hostName = '<%=request.getServerName()%>';
	var serverPort = '<%=request.getServerPort()%>';
	var protocol = '<%=request.getScheme()%>';
</script>

<jsp:include
	page="<%=ChartEngineUtil.getLibraryInitializerPath(template)%>" >
	<jsp:param name="template" value="<%=template%>" />
</jsp:include>

<%@include file="commons/includeAthenaChartEngineJS5.jspf"%>

</head>

<%-- == BODY ========================================================== --%>

<body>
   <%-- div with wait while loading message --%>
   <div id="divLoadingMessage<%=uuidO%>" style="display: none; align=center">
   		<img src='${pageContext.request.contextPath}/img/icon-info15.png' />  Downloading...
   </div>

	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">		
		
		/**
			Providing global context variable that will enable us using the context name
			inside of JS files (e.g. in WebService.js, WebServiceManager.js and 
			WebServiceManagerFactory.js as "context" sub-property of "config" property).
			
			@author: danristo (danilo.ristovski@mht.net)
		*/
		Sbi.context = '${pageContext.request.contextPath}';
		
		var chartConfiguration = null;
		
		var isChartHeightEmpty = null;
		var isChartWidthEmpty= null;
	
		function exportChart(exportType) {		
			document.getElementById('divLoadingMessage<%=uuidO%>').style.display = 'inline';
			
			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager(protocol, hostName, serverPort, sbiExecutionId, userId);
			var chartExportWebServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartExportWebServiceManager(protocol, hostName, serverPort, sbiExecutionId, userId); 			
			
			/* gets the template content */
			var parameters = {
 						jsonTemplate: Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate,
 						driverParams: '<%=driverParams%>'
 			};
			chartServiceManager.run('jsonChartTemplate', parameters, [], 
					function (response) {
						var chartConf = response.responseText;
						Ext.DomHelper.useDom = true; 
						// need to use dom because otherwise an html string is composed as a string concatenation,
				        // but, if a value contains a " character, then the html produced is not correct!!!
				        // See source of DomHelper.append and DomHelper.overwrite methods
				        // Must use DomHelper.append method, since DomHelper.overwrite use HTML fragments in any case.
				         var dh = Ext.DomHelper;
						 var form = document.getElementById('export-chart-form');
				          if (form === undefined || form === null) {
					          var form = dh.append(Ext.getBody(), { 
					        	// creating the hidden input in form
					            id: 'export-chart-form'
					          , tag: 'form'
					          , method: 'post'
					          , cls: 'export-form'					       
					          });
					       // creating the hidden inputs in form:
					          dh.append(form, {					
									tag: 'input'
									, type: 'hidden'
									, name: 'options'
									, value: ''
								});
					          dh.append(form, {					
									tag: 'input'
									, type: 'hidden'
									, name: 'content'
									, value: '' 
								});
					          dh.append(form, {					
									tag: 'input'
									, type: 'hidden'
									, name: 'type'
									, value: '' 
								});
					          dh.append(form, {					
									tag: 'input'
									, type: 'hidden'
									, name: 'width'
									, value: ''
								});
					          dh.append(form, {					
									tag: 'input'
									, type: 'hidden'
									, name: 'constr'
									, value: '' 
								});
					          dh.append(form, {					
									tag: 'input'
									, type: 'hidden'
									, name: 'async'
									, value: ''
								});
				          }              				       	
				         form.elements[0].value = chartConf;
				         form.elements[1].value = 'options';
				         form.elements[2].value = (exportType=='PDF')?'application/pdf':'image/png';
				         form.elements[3].value = '600';
				         form.elements[4].value = 'Chart';
				         form.elements[5].value = 'false';
				         form.action = protocol + '://'+ hostName + ':' + serverPort + '/highcharts-export-web/';
				         form.target = '_blank'; // result into a new browser tab
				         form.submit();
				         document.getElementById('divLoadingMessage<%=uuidO%>').style.display = 'none';
					}					
			);
		}
				
	
 		Ext.onReady(function(){
 			Ext.log({level: 'info'}, 'CHART: IN');

 			var mainPanel = Ext.create('Ext.panel.Panel', {
 				id: 'mainPanel',
 				width: '100%',
 			    height: '100%',
 			   	//autoScroll: true,
			  	//overflowX: 'scroll',
			  	//overflowY: 'scroll',
 			    renderTo: Ext.getBody()
 			});
 			
 			var globalThis = this;
 			
 			
 			/* 
 				Listen for the resizing of the window (panel) in order to re-render
 				the chart.
 				@author: danristo (danilo.ristovski@mht.net)
			*/
 			Ext.on
 			(
 				"resize",
 				
 				function(newWidth, newHeight)
 				{ 	 					
 					/*
 						If there are chart dimension values (height and width) specified 
 						for this chart (chart that relies on the D3 library), variable
 						'chartConfiguration' will stay 'null', since we did not enter
 						the part of code that specify this value (actual JSON file) that
 						we receive from the server. This way, resize will not be applied 
 						this chart and it will despite of resizing stay with the same 
 						size as on the beginning (on the initial render of the chart).
 					*/ 	 					
 					if (chartConfiguration!=null)
					{
 						var chartType = chartConfiguration.chart.type.toUpperCase();
		 					
	 					/* 
	 						Check if the chart (document) that we want to render (run) on the page 
	 						usese D3 as a library for rendering.
						*/
	 					var isD3Chart = (chartType == "SUNBURST" || chartType == "WORDCLOUD" || chartType == "PARALLEL" || chartType == "CHORD");
 						
 						if (isD3Chart)
 						{
 	 						/* 
 	 							Set new values for the height and the width of the chart (the DIV
 	 							that contains the chart), as a consequence of a resizing the window
 	 							(panel). This will eventually affect on those chart elements that 
 	 							depend on these two parameters.
 							*/
 							if (isChartHeightEmpty==true)
							{
 								chartConfiguration.chart.height = newHeight;
							}
 	 						
 	 						if (isChartWidthEmpty==true)
 							{
 	 							chartConfiguration.chart.width = newWidth; 
 							}				
 							
 	 						/* Re-render the chart after resizing the window (panel). */
 	 						renderChart(chartConfiguration);
 						}
					}
 				}
 			);
 			
 			initChartLibrary(
 					mainPanel.id, 
 					LN('sbi.chartengine.viewer.drilluptext'), 
 					LN('sbi.chartengine.viewer.decimalpoint'), 
 					LN('sbi.chartengine.viewer.thousandsep'));
 			
 			Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate = '<%=template%>';
 			Sbi.chart.viewer.ChartTemplateContainer.datasetLabel = '<%=datasetLabel%>';
 			
 			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager(protocol, hostName, serverPort, sbiExecutionId, userId);
 			
 			if(<%=isCockpit%>) {
 				
 	 			Sbi.chart.viewer.ChartTemplateContainer.aggregations = '<%=aggregations %>';
 	 			Sbi.chart.viewer.ChartTemplateContainer.selections = '<%= selections %>';
 	 			Sbi.chart.viewer.ChartTemplateContainer.associations = '<%=associations %>';
 	 			Sbi.chart.viewer.ChartTemplateContainer.widgetId = '<%=widgetId%>';
 	 			Sbi.chart.viewer.ChartTemplateContainer.metaData = '<%=metaData%>';
 				
 							var parameters = {
 									jsonTemplate: Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate,
 									driverParams: '<%=driverParams%>',
							jsonData: Sbi.chart.viewer.ChartTemplateContainer.metaData   // PARAMETRO AGGIUNTIVO -> GESTITO NEL SERVIZIO!
 							};
 							chartServiceManager.run('jsonChartTemplate', parameters, [], function (response) {
 								var chartConf = Ext.JSON.decode(response.responseText, true);
 								renderChart(chartConf);
 							});
 				
 			}else { 				
 				
 				var parameters = {
 						jsonTemplate: Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate,
 						driverParams: '<%=driverParams%>'
 					};
 					chartServiceManager.run('jsonChartTemplate', parameters, [], function (response) {
 						 						 						
 						var chartConf = Ext.JSON.decode(response.responseText, true);			
 						
 						/* 
 							Set the initial size of the chart if the height and width are not 
 							defined by the user (through the Designer). This is mandatory for
 							rendering the chart. If not specified at all - error will appear.
 							@author: danristo (danilo.ristovski@mht.net)
 						*/
 						var heightChart = chartConf.chart.height;
 						var widthChart = chartConf.chart.width;
 						var typeChart = chartConf.chart.type.toUpperCase();
 							
 						(heightChart != undefined && heightChart != "") ? Ext.getCmp('mainPanel').setHeight(Number(heightChart)) : null;
 						(widthChart != undefined && widthChart!="") ? Ext.getCmp('mainPanel').setWidth(Number(widthChart)) : null; 
 						
 						/* 
							If the chart is of the Highcharts library, do not apply current dimensions of the
							window that contains the chart. D3 does not handle this appropriately, so we need
							the starting dimensions for the chart - current dimensions of the window within 
							which the chart (of D3 library) is placed.
							@author: danristo (danilo.ristovski@mht.net)
						*/
						var isD3Chart = (typeChart == "SUNBURST" || typeChart == "WORDCLOUD" || typeChart == "PARALLEL" || typeChart == "CHORD");
	 					
						/*
							If type of the chart is one of those that rely on the D3 library
							and dimensions of the chart that we are going to render for the
							first time are not specified (empty), adapt size of the chart to
							the size of the window (panel) in which it will be rendered. The
							indicator for empty dimensions for the previous code (on.resize)
							will be chartConfiguration=null, since we will not enter this 
							if-statement.
							@author: danristo (danilo.ristovski@mht.net)
						*/
						//if (isD3Chart && ((chartConf.chart.width=="" || chartConf.chart.height=="") || typeChart == "SUNBURST"))
						if (isD3Chart)
						{	
							if (heightChart=="" || widthChart=="" || typeChart == "SUNBURST")
							{
		 						if (heightChart == "")	
		 						{
		 							isChartHeightEmpty = true;
		 							chartConf.chart.height = window.innerHeight;
		 						}
		 						else
	 							{
		 							isChartHeightEmpty = false;
	 							}
		 						
		 						if (widthChart == "" || typeChart == "SUNBURST")	
		 						{
		 							isChartWidthEmpty = true;
		 							chartConf.chart.width = window.innerWidth;
		 						}
		 						else
	 							{
		 							isChartWidthEmpty = false;
	 							}
		 						
		 						chartConfiguration = chartConf;
							} 							
						} 						
 						
						//console.log("CHART.JSP: chartConf");
						//console.log(chartConf);
						
 						renderChart(chartConf);
 					});
 				
 			} 
 			
	    	Ext.log({level: 'info'}, 'CHART: OUT');

 		  });

	</script>

</body>
</html>