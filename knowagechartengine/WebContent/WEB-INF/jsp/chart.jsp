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
<%@page import="it.eng.spagobi.utilities.engines.rest.ExecutionSession"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="org.json.JSONObject"%>

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
	
	ExecutionSession es = new ExecutionSession(request, request.getSession());
	engineInstance = (ChartEngineInstance)es.getAttributeFromSession(EngineConstants.ENGINE_INSTANCE );
	
	/*The use of the above commented snippet had led to https://production.eng.it/jira/browse/KNOWAGE-678 and 
	* https://production.eng.it/jira/browse/KNOWAGE-552. The chart engine is stateful, thus the http session
	* is not the place to store and retrive the engine instance, otherwise concurrency issues are raised.
	* @author: Alessandro Portosa (alessandro.portosa@eng.it)
	*/
	//engineInstance = (ChartEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
	
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
	
	/*
		WORKAROUND: Replace the single quote character wherever in the chart template with the ASCII code for a single quote character, so we can render the chart 
		inside the Cockpit or Chart Engine even when the JSON template contains	this character (e.g. "L'Italia"). Later, because of rendering the chart, this code
		will be replaced with the "escaped" single quote character combination (in order not to have "L&#39;Italia").
		@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	*/
	template = template.replaceAll("'","&#39;");
	
	if(env.get("EXECUTE_COCKPIT") != null){
		isCockpit = true;
		datasetLabel = env.get(EngineConstants.ENV_DATASET_LABEL)!=null?(String)env.get(EngineConstants.ENV_DATASET_LABEL):"";
		aggregations = env.get("AGGREGATIONS")!=null?(String)env.get("AGGREGATIONS"):"";
		selections = env.get("SELECTIONS")!=null?(String)env.get("SELECTIONS"):"";
		associations = env.get("ASSOCIATIONS")!=null?(String)env.get("ASSOCIATIONS"):"";
		metaData = env.get("METADATA")!=null?(String)env.get("METADATA"):"";
		widgetId = env.get("WIDGETID")!=null?(String)env.get("WIDGETID"):"";
	} else {
		datasetLabel = (engineInstance.getDataSet() != null )?
				engineInstance.getDataSet().getLabel() : "" ;
	}
	
	/*
		WORKAROUND: Replace the single quote character in the meta data (information about the data inside the dataset that is picked for the chart/cockpit) 
		with the "escaped" single quote combination, so we can render the chart inside the Cockpit when the data in the table (dataset) that is picked contains
		this character (e.g. "L'Italia").
		@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	*/
	metaData = metaData.replaceAll("'","\\\\'");
	
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
	String driverParams = new JSONObject(driverParamsMap).toString(0).replaceAll("'", "\\\\'");
	String uuidO=request.getParameter("SBI_EXECUTION_ID")!=null? request.getParameter("SBI_EXECUTION_ID"): "null";
%>


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
<%-- == HEAD ========================================================== --%>
<head>
<title><%=docName.trim().length() > 0? docName: "ChartEngine"%></title>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />

<%@include file="commons/includeExtJS5.jspf"%>
<%@include file="commons/includeMessageResource.jspf"%>

<script>
	var sbiExecutionId = <%=request.getParameter("SBI_EXECUTION_ID")!=null? "'"+request.getParameter("SBI_EXECUTION_ID")+"'" : "null"%>;
	//var sbiExecutionId = '<%=uuidO%>';
	var userId = '<%=userId%>';
	var hostName = '<%=request.getServerName()%>';
	var serverPort = '<%=request.getServerPort()%>';
	var protocol = window.location.protocol;
	var driverParams = '<%=driverParams%>';
</script>

<% if (template != null && !template.equals("") && !template.matches("^\\{\\s*\\}$")) {%>
	
	<jsp:include
		page="<%=ChartEngineUtil.getLibraryInitializerPath(template,docLabel, profile)%>" >
		<jsp:param name="template" value="<%=template%>" />
	</jsp:include>
	
	<%@include file="commons/includeKnowageChartEngineJS5.jspf"%>
	
<% } %>

</head>

<%-- == BODY ========================================================== --%>
<%-- <body style="display: flex;
    width: 60%;
     height: 100%;
    padding: 10px; 
    vertical-align: bottom;">
--%>

<!-- <body style="width:50%; height:50%;"> -->
<!-- <body style="height:50%; margin:0 0 0 25%;  overflow-x:hidden;  overflow-y:hidden"> -->
<body>

<% if (template != null && !template.equals("") && !template.matches("^\\{\\s*\\}$")) {%>
	<%-- div with wait while loading message --%>
   	<div id="divLoadingMessage<%=uuidO%>" style="display: none; align=center">
		<img src='${pageContext.request.contextPath}/img/icon-info15.png' />  Downloading...
   	</div>

	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">		
		
		var chartConfiguration = null;
		
		var isChartHeightEmpty = null;
		var isChartWidthEmpty= null;
				
		function exportChart(exportType) {
		
			var chartType = chartConfiguration.chart.type.toUpperCase();
		
			var isD3Chart = (chartType == "SUNBURST" || chartType == "WORDCLOUD" || chartType == "PARALLEL" || chartType == "CHORD");
			
			if (!isD3Chart) {
				document.getElementById('divLoadingMessage<%=uuidO%>').style.display = 'inline';
			}			
			
			var thisContextName			= '${pageContext.request.contextPath}';  //'knowagechartengine';
			thisContextName.replace('/','');
			var exporterContextName 	= 'highcharts-export-web';
			
			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager(protocol, hostName, serverPort, thisContextName, sbiExecutionId, userId);
			var chartExportWebServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartExportWebServiceManager(protocol, hostName, exporterContextName, serverPort, sbiExecutionId, userId); 			
			
			/* gets the template content */
			/*
				Since we are calling the rendering part and VM (before the rendering) from the Highcharts
				Export web application (we clicked on the Export option in File dropdown on the page where
				chart renders), we need to forward this information towards the belonging VM so it can adapt
				its code (skipping of some parts of the initial (standard) implementation). This additional
				property (data) is "exportWebApp".
				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			*/			 
			
			/*
				When we need to export charts when they are rendered, we need to send a proper information
				about the dimensions of that chart, i.e. the dimensions and dimension types that user has
				set for the chart. If the type of the dimension is "percentage", we need to convert numeric
				values that are set for this/these dimension(s), because they represent a percentage, not 
				the absolute value (in pixels).
				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			*/					
			var jsonObj = JSON.parse(Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate);
			
			var jsonObjHeight = jsonObj.CHART.height;
			var jsonObjWidth = jsonObj.CHART.width;
			
			var jsonObjHeightEmpty = false;
			var jsonObjWidthEmpty = false;
			
			/*
				CONVERSION OF DIMENSION(S) EXPRESSED IN PERCENTAGES INTO THE ABSOLUTE ONE,
				EXPRESSED IN PIXELS (SO WE CAN GIVE ACCURATE DATA TO THE PHANTOM JS).
			*/
			
			if (jsonObjHeight!=undefined && jsonObjHeight!="")
			{
				jsonObjHeight = Number(jsonObjHeight);					
			}
			else
			{
				jsonObjHeight = window.innerHeight
				jsonObjHeightEmpty = true;
			}
			
			if (jsonObjWidth!=undefined && jsonObjWidth!="")
			{
				jsonObjWidth = Number(jsonObjWidth);				
			}
			else
			{
				jsonObjWidth = window.innerWidth;
				jsonObjWidthEmpty = true;
			}
			
			if (jsonObj.CHART.heightDimType=="percentage" && !jsonObjHeightEmpty)
			{
				jsonObjHeight = jsonObjHeight*window.innerHeight/100;
			}
			
			if (jsonObj.CHART.widthDimType=="percentage" && !jsonObjWidthEmpty)
			{
				jsonObjWidth = jsonObjWidth*window.innerWidth/100;
			}
			
			jsonObj.CHART.height = jsonObjHeight;
			jsonObj.CHART.width = jsonObjWidth;
			
			var parameters = {
				//jsonTemplate: Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate,
				jsonTemplate: JSON.stringify(jsonObj),
				exportWebApp: true,
				driverParams: '<%=driverParams%>'

 			};				

			if (isD3Chart) {
				/*
					Forward the D3 chart's height and width towards the part of the code that calls the 
					Highcharts service for exporting chart.
					@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				*/
				exportD3Chart(protocol, hostName,serverPort,exportType, jsonObj.CHART.height, jsonObj.CHART.width);
			}else{
				chartServiceManager.run('jsonChartTemplate', parameters, [], 
						function (response) {
					
							/*
								WORKAROUND: Replacing in other way - from the ASCII code for the single quote character to the "escaped" single quote combination in order 
								to enable a proper (adequate) the exporting of the chart. This way we will decode the former single quote in the chart template that was
								exchanged for this code (JSON cannot handle single quote inside it) and have a single quote on its place in the exported chart. 
								@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							*/
							response.responseText = response.responseText.replace(new RegExp("&#39;",'g'),"\\'");
					
							var chartConf = response.responseText;
							
							/*
								If the chart is of the type that relies on the Highcharts library, check
								if the type is of HEATMAP or TREEMAP, since they need preparation of the
								data provided for such a chart. This preparation is done locally, inside
								this ('KnowageChartEngine') project and we do not have access to it from 
								the	one that uses the Phantom JS ('highcharts-export' project).
								@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							*/
							if (chartType == 'TREEMAP' || chartType == 'HEATMAP')
							{
								var jsonChartConf = Ext.JSON.decode(chartConf);
								
								if(chartType == 'TREEMAP' && typeof(prepareChartConfForTreemap) == "function") {
									chartConf = Ext.JSON.encode(prepareChartConfForTreemap(jsonChartConf));
								}
								else if(chartType == 'HEATMAP' && typeof(prepareChartConfForHeatmap) == "function") {
									chartConf = Ext.JSON.encode(prepareChartConfForHeatmap(jsonChartConf));
								}
							}	
							
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
							form.action = protocol + '//'+ hostName + ':' + serverPort + '/highcharts-export-web/';
				         	form.target = '_blank'; // result into a new browser tab
				         	form.submit();
				         	document.getElementById('divLoadingMessage<%=uuidO%>').style.display = 'none';
						}					
					);
			}		
		};
		
		/*  
			Needed for the PARALLEL chart.
			@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		*/		
		function removePixelsFromFontSize(fontSize) {
				
			var indexOfPx = fontSize.indexOf('px');
			
			if (indexOfPx > 0) {
				return fontSize.substring(0,indexOfPx);
			} else {
				return fontSize;
			}
			
		};	
		
		function exportD3Chart(protocol, hostName,serverPort,exportType, chartHeight, chartWidth){
			var body = Ext.select("body");
			var html = body.elements[0].innerHTML;
			var encoded = btoa(html);
			
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
	        	dh.append(form, {
					tag: 'input'
					, type: 'hidden'
					, name: 'chartHeight'
					, value: '12'
				});
	        	dh.append(form, {
					tag: 'input'
					, type: 'hidden'
					, name: 'chartWidth'
					, value: '55'
				});
          	}
         	form.elements[0].value = encoded;
         	form.elements[1].value = 'html';
         	form.elements[2].value = (exportType=='PDF')?'application/pdf':'image/png';
         	form.elements[3].value = '600';
         	form.elements[4].value = 'Chart';
         	form.elements[5].value = 'false';
         	form.elements[6].value = chartHeight;
         	form.elements[7].value = chartWidth;
			form.action = protocol + '//'+ hostName + ':' + serverPort + '/highcharts-export-web/';
         	form.target = '_blank'; // result into a new browser tab
         	form.submit();
		};
	
 		Ext.onReady(function(){
 			Ext.log({level: 'info'}, 'CHART: IN');
 			Ext.getBody().mask(LN('sbi.chartengine.viewer.chart.loading'), 'x-mask-loading'); 			
 			
 			var mainPanel = Ext.create('Ext.panel.Panel', {
 				id: 'mainPanel',
 				width: '100%',
 			    height: '100%',
				bodyStyle : 'background:transparent;',
				style: "margin: auto !important;",	// TODO: Danilo (make some comment): KNOWAGE-623
 			   	//style: "overflow: auto",
 			    renderTo: Ext.getBody()
 			});
 			
 			var globalThis = this;
 			 			
 			/* 
 				Listen for the resizing of the window (panel) in order to re-render
 				the chart.
 				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			*/
 			Ext.on("resize", function(newWidth, newHeight) { 	  					
				/*
					If there are chart dimension values (height and width) specified 
					for this chart (chart that relies on the D3 library), variable
					'chartConfiguration' will stay 'null', since we did not enter
					the part of code that specify this value (actual JSON file) that
					we receive from the server. This way, resize will not be applied 
					this chart and it will despite of resizing stay with the same 
					size as on the beginning (on the initial render of the chart).
				*/ 	 					
				if (chartConfiguration!=null){
					var chartType = chartConfiguration.chart.type.toUpperCase();
	 				
 					/* 
 						Check if the chart (document) that we want to render (run) on the page 
 						usese D3 as a library for rendering.
					*/
					
 					var isD3Chart = (chartType == "SUNBURST" || chartType == "WORDCLOUD" || chartType == "PARALLEL" || chartType == "CHORD");
							 					
					if (isD3Chart) {
 						/* 
 							Set new values for the height and the width of the chart (the DIV
 							that contains the chart), as a consequence of a resizing the window
 							(panel). This will eventually affect on those chart elements that 
 							depend on these two parameters.
						*/
						
						if (isChartHeightEmpty==true) { 	
							chartConfiguration.chart.height = window.innerHeight-2; // sometimes is newHeight != window.innerHeight 								 								
						}
 						
 						if (isChartWidthEmpty==true) {
							chartConfiguration.chart.width = window.innerWidth; // sometimes is newWidth != window.innerWidth	 						 
						}				
 						
 						/* Re-render the chart after resizing the window (panel). */
 						renderChart(chartConfiguration);
 						Ext.getBody().unmask();
					}
				}
 			});
 				
 			initChartLibrary(
 					mainPanel.id, 
 					LN('sbi.chartengine.viewer.drilluptext'), 
 					LN('sbi.chartengine.viewer.decimalpoint'), 
 					LN('sbi.chartengine.viewer.thousandsep'));
 			
 			Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate = '<%=template%>';
 			Sbi.chart.viewer.ChartTemplateContainer.datasetLabel = '<%=datasetLabel%>';
 			
 			var thisContextName	= '${pageContext.request.contextPath}';  //'knowagechartengine';
 			var thisContextNameParam = thisContextName.replace('/', '');
			
 			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager(
 					protocol, hostName, serverPort, thisContextNameParam, sbiExecutionId, userId);
 			
 			var parameters={};
 			
 			if(<%=isCockpit%>) {
 				
 	 			Sbi.chart.viewer.ChartTemplateContainer.aggregations = '<%=aggregations %>';
 	 			Sbi.chart.viewer.ChartTemplateContainer.selections = '<%= selections %>';
 	 			Sbi.chart.viewer.ChartTemplateContainer.associations = '<%=associations %>';
 	 			Sbi.chart.viewer.ChartTemplateContainer.widgetId = '<%=widgetId%>';
 	 			Sbi.chart.viewer.ChartTemplateContainer.metaData = '<%=metaData%>';
 				
 	 			//console.log("CHART.JS line 499");
 	 			//console.log("jsonTemplate: ");
 	 			//console.log(Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate);
 	 			
				parameters = {
					jsonTemplate: Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate,
					driverParams: '<%=driverParams%>',
					jsonData: Sbi.chart.viewer.ChartTemplateContainer.metaData   // PARAMETRO AGGIUNTIVO -> GESTITO NEL SERVIZIO!
				};
				//console.log(Sbi.chart.viewer.ChartTemplateContainer.metaData);
				//console.log(<%=driverParams%>);
				
				/*chartServiceManager.run('jsonChartTemplate', parameters, [], function (response) {
					var chartConf = Ext.JSON.decode(response.responseText, true);
					renderChart(chartConf);
					Ext.getBody().unmask();
				});*/
 				

 			}else {
 				parameters = {
					jsonTemplate: Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate,
					driverParams: '<%=driverParams%>'
				};
 			}
 			
 			chartServiceManager.run('jsonChartTemplate', parameters, [], function (response) {

 				/*
					WORKAROUND: Replacing in other way - from the ASCII code for the single quote character to the "escaped" single quote combination in order 
					to enable a proper (adequate) rendering of the chart. This way we will decode the former single quote in the chart template that was
					exchanged for this code (JSON cannot handle single quote inside it) and have a single quote on its place in the rendered chart. 
					@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				*/
				response.responseText = response.responseText.replace(new RegExp("&#39;",'g'),"\\'");
 				
 				var chartConf = Ext.JSON.decode(response.responseText, true);	
 				
				var typeChart = chartConf.chart.type.toUpperCase();		 				
				var isD3Chart = (typeChart == "SUNBURST" || typeChart == "WORDCLOUD" || typeChart == "PARALLEL" || typeChart == "CHORD");
				
				/* 
					Set the initial size of the chart if the height and width are not 
					defined by the user (through the Designer). This is mandatory for
					rendering the chart. If not specified at all - error will appear.
					
					@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				*/
				var heightChart = chartConf.chart.height;
				var heightDimType = chartConf.chart.heightDimType;
				
				var widthChart = chartConf.chart.width;
				var widthDimType = chartConf.chart.widthDimType;				
				
				var mainPanelTemp = Ext.getCmp("mainPanel");
				
				mainPanelTemp.setStyle("overflow","auto");	
				
				if (!heightChart)
				{
					mainPanelTemp.setStyle("overflow-y","hidden");
				}
				else if (heightDimType == "percentage")
				{
					mainPanelTemp.setStyle("height",heightChart+"%");					
					mainPanelTemp.setStyle("overflow-y","hidden");
					
					if (!isD3Chart)
					{
						chartConf.chart.height = undefined;
					}
				}		
				
				if (!widthChart)
				{
					mainPanelTemp.setStyle("overflow-x","hidden");
				}
				else if (widthDimType == "percentage")
				{
					mainPanelTemp.setStyle("width",widthChart+"%");					
					mainPanelTemp.setStyle("overflow-x","hidden");
					
					if (!isD3Chart)
					{
						chartConf.chart.width = undefined;
					}
				} 										
				
				/*
					If type of the chart is one of those that rely on the D3 library
					and dimensions of the chart that we are going to render for the
					first time are not specified (empty), adapt size of the chart to
					the size of the window (panel) in which it will be rendered. The
					indicator for empty dimensions for the previous code (on.resize)
					will be chartConfiguration=null, since we will not enter this 
					if-statement.
					
					@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				*/
				if (isD3Chart) {	
					
					isChartHeightEmpty = false;
					isChartWidthEmpty = false;
					
					if ((heightDimType=="pixels" && (heightChart==undefined || heightChart=="")) ||
							(widthDimType=="pixels" && (widthChart==undefined || widthChart == ""))) 
					{
						if (heightDimType=="pixels" && (heightChart==undefined || heightChart=="")) 
						{
							chartConf.chart.height = window.innerHeight-2;		 							
							isChartHeightEmpty = true;		 							
						} 
						
						if (widthDimType=="pixels" && (widthChart==undefined || widthChart == "")) 
						{
							chartConf.chart.width = window.innerWidth;		 							
							isChartWidthEmpty = true;
						}
					}
						
					chartConfiguration = chartConf;	
					renderChart(chartConf);

				} 
				else {
					
					chartConfiguration = chartConf;	
					renderChart(chartConf);
					
				}			
				
				Ext.getBody().unmask();
			});
 			
	    	Ext.log({level: 'info'}, 'CHART: OUT');
	  	});

	</script>
<% } else {%>
	<div>
	<script language="javascript" type="text/javascript">
	Ext.onReady(function(){
		var mainPanel = Ext.create('Ext.panel.Panel', {
			id: 'mainPanel',
			width: '100%',
		    height: '100%',
		    renderTo: Ext.getBody(),
		    html: '<p>' + LN('sbi.generic.error.notemplate') + '</p>'
		});
	});
	</script>
	</div>
<% }%>
</body>
</html>
