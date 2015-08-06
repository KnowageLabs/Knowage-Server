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
	var userId = '<%=userId%>';
	var hostName = '<%=request.getServerName()%>';
	var serverPort = '<%=request.getServerPort()%>';
</script>

<jsp:include
	page="<%=ChartEngineUtil.getLibraryInitializerPath(template)%>" >
	<jsp:param name="template" value="<%=template%>" />
</jsp:include>

<%@include file="commons/includeAthenaChartEngineJS5.jspf"%>

</head>

<%-- == BODY ========================================================== --%>

<body>

	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">
	
 		Ext.onReady(function(){
 			Ext.log({level: 'info'}, 'CHART: IN');

 			//Ext.Loader.setPath('Sbi.chart', '/athenachartengine/js/src/ext5/sbi/chart');

 			var mainPanel = Ext.create('Ext.panel.Panel', {
 				id: 'mainPanel',
 				width: '100%',
 			    height: '100%',
 			    renderTo: Ext.getBody()
 			});
 			initChartLibrary(
 					mainPanel.id, 
 					LN('sbi.chartengine.viewer.drilluptext'), 
 					LN('sbi.chartengine.viewer.decimalpoint'), 
 					LN('sbi.chartengine.viewer.thousandsep'));
 			
 			Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate = '<%=template%>';
 			Sbi.chart.viewer.ChartTemplateContainer.datasetLabel = '<%=datasetLabel%>';
 			
 			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager('http', hostName, serverPort, sbiExecutionId, userId);
 			
 			if(<%=isCockpit%>) {
 				
 	 			Sbi.chart.viewer.ChartTemplateContainer.aggregations = '<%=aggregations %>';
 	 			Sbi.chart.viewer.ChartTemplateContainer.selections = '<%= selections %>';
 	 			Sbi.chart.viewer.ChartTemplateContainer.associations = '<%=associations %>';
 	 			Sbi.chart.viewer.ChartTemplateContainer.widgetId = '<%=widgetId%>';
 				
 				var templateContainer = Ext.create('Ext.mixin.Observable', {
 					listeners: {
 						dataReady: function(jsonData) {
 							var parameters = {
 									jsonTemplate: Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate,
 									driverParams: '<%=driverParams%>',
 									jsonData: jsonData   // PARAMETRO AGGIUNTIVO -> GESTITO NEL SERVIZIO!
 							};
 							chartServiceManager.run('jsonChartTemplate', parameters, [], function (response) {
 								var chartConf = Ext.JSON.decode(response.responseText, true);
 								renderChart(chartConf);
 							});
 						}
 					}
 				});
 				
 			 	var coreServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getCoreWebServiceManager('http', hostName, serverPort, sbiExecutionId, userId);
 				
 				var dataParameters = {
 					aggregations: Sbi.chart.viewer.ChartTemplateContainer.aggregations,
 					selections: Sbi.chart.viewer.ChartTemplateContainer.selections,
 				};
 				
 				var pathParameters = [
 						Sbi.chart.viewer.ChartTemplateContainer.datasetLabel
 				];

	 			coreServiceManager.run('loadData', dataParameters, pathParameters, function (response) {
	 				templateContainer.fireEvent('dataReady', response.responseText);
	 			});	
 				
 			}else { 				
 				
 				var parameters = {
 						jsonTemplate: Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate,
 						driverParams: '<%=driverParams%>'
 					};
 					chartServiceManager.run('jsonChartTemplate', parameters, [], function (response) {
 						var chartConf = Ext.JSON.decode(response.responseText, true);
 						renderChart(chartConf);
 					});
 				
 			} 
 			
	    	Ext.log({level: 'info'}, 'CHART: OUT');

 		  });

	</script>

</body>
</html>