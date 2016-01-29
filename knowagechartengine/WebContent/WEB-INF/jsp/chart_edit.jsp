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
<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>	


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>

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
	boolean isCockpit = false;
	JSONArray styles;
	
	engineInstance = (ChartEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
	env = engineInstance.getEnv();
	profile = engineInstance.getUserProfile();
	profileJSONStr = new ObjectMapper().writeValueAsString(profile);
	locale = engineInstance.getLocale();
	
	if(env.get("EDIT_COCKPIT") != null){
		datasetLabel = (String)env.get(EngineConstants.ENV_DATASET_LABEL);
		isCockpit = true;
	}else{
		
		
		/*
		// Danilo (for handling missing dataset for the document)
		if (engineInstance.getDataSet() == null)
		{
			datasetLabel = null;
		}
		else
		{

			datasetLabel = engineInstance.getDataSet().getLabel();
		}
		*/
		
		datasetLabel = engineInstance.getDataSet().getLabel();
	}
	
	contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); 
	environment = request.getParameter("SBI_ENVIRONMENT"); 
	executionRole = (String)env.get(EngineConstants.ENV_EXECUTION_ROLE);
	userId = (engineInstance.getDocumentUser()==null)?"":engineInstance.getDocumentUser().toString();
	isTechnicalUser = (engineInstance.isTechnicalUser()==null)?"":engineInstance.isTechnicalUser().toString();
	template = engineInstance.getTemplate().toString(0);
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
    
    styles=(JSONArray)request.getSession().getAttribute(EngineConstants.DEFAULT_CHART_STYLES);
    
%>

<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
	<%-- == HEAD ========================================================== --%>
	<head>
	
	
	   <title><%=docName.trim().length() > 0? docName: "ChartEngine"%></title>
       <meta http-equiv="X-UA-Compatible" content="IE=edge" />
       
        <%@include file="commons/includeExtJS5.jspf" %>        
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeAthenaChartEngineJS5.jspf" %>
		
		<!-- 
			The 'treemap.js' file is needed for the Designer page, so it can call particular
			functions when exporting (rendering) the chart preview on this page. Exporting is 
			performed by clicking on the 'refresh' button inside the header of the Preview panel.
			
			@commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 -->
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/treemap/treemap.js"></script>		
				
				<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/treemap/treemap.js"></script>
				
    </head>
	
	<%-- == BODY ========================================================== --%>
    
    <body>
	
	    
	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">
	
		/* 
			Mask the body when data necessary for Designer page is loading.
			
			@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		*/
		Ext.getBody().mask(LN('sbi.chartengine.viewer.chart.loading'), 'x-mask-loading');
		Sbi.chart.designer.Styles= '<%=styles%>';	
 		
	    Ext.onReady(function(){
 			Ext.log({level: 'info'}, 'CHART: IN');
 			
 			/* 
				Unmask the body when data necessary for Designer page is loaded.
				
				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			*/
 			Ext.getBody().unmask();
 			
 			/**
 				Getting back from the end of the current URL path for 3 steps in order
 				to get the context (the part of the URL that gives the information about 
				the name of the root project).
				
 				@author: danristo (danilo.ristovski@mht.net)
 			*/
 			Ext.Loader.setPath('Sbi.chart', '../../../js/src/ext5/sbi/chart'); 		
 			
 			<%-- 
 			
  			initChartLibrary(mainPanel.id);
  			
  			--%>  			
  			
  			var sbiExecutionId = <%=request.getParameter("SBI_EXECUTION_ID")!=null? "'"+request.getParameter("SBI_EXECUTION_ID")+"'" : "null"%>;
 			var userId = '<%=userId%>';
 			var hostName = '<%=request.getServerName()%>';
 			var serverPort = '<%=request.getServerPort()%>';
 			var docLabel = '<%= docLabel %>';
 			var jsonTemplate = Ext.JSON.decode('<%=template%>');
 			var datasetLabel  = '<%=datasetLabel%>';
 			
 			/*
 			// Danilo (for handling missing dataset for the document)
 			if (datasetLabel == 'null')
 			{
 				Sbi.exception.ExceptionHandler.showInfoMessage
				(					
					"You need to specify dataset in order to specify document",
					"Dataset missing"
				);
 			}*/
 			
 			var chartLibNamesConfig = <%=ChartEngineUtil.getChartLibNamesConfig()%>;
 			
 			var isCockpit = <%=isCockpit%>;
 			
			var thisContextName			= '${pageContext.request.contextPath}';  <%-- knowagechartengine --%>
			thisContextName = thisContextName.replace('/','');
			var mainContextName 		= '<%=contextName.replaceAll("/", "")%>';  <%-- knowage --%>
			var exporterContextName 	= 'highcharts-export-web';
 			
			
 			Sbi.chart.designer.Designer.initialize(
 					sbiExecutionId, 
 					userId, 
 					hostName, 
 					serverPort, 
 					docLabel, 
 					jsonTemplate, 
 					datasetLabel,
 					chartLibNamesConfig,
 					isCockpit,
 					thisContextName,
 					mainContextName,
 					exporterContextName
 			);
 			
		});
 		
 		</script>
 		
 		<script language="javascript" type="text/javascript">
			function saveFromCockpit(){
				Sbi.trace("[Chart.saveFromCockpit]: IN");
				var result = false;
				var errorMessages = Sbi.chart.designer.Designer.validateTemplate();
				
				var chartEngineWidgetDesigner = window.parent.cockpitPanel.widgetContainer.widgetEditorWizard.editorMainPanel.widgetEditorPage.widgetEditorPanel.mainPanel.customConfPanel.designer;
				
				if (errorMessages == false) {
					var exportedAsOriginalJson = Sbi.chart.designer.Designer.exportAsJson(true);
					
					chartEngineWidgetDesigner.chartTemplate = exportedAsOriginalJson;
					chartEngineWidgetDesigner.setAggregationsOnChartEngine();
					chartEngineWidgetDesigner.setErrorMessage(null);
					
					result = true;					
					
				}else{
					chartEngineWidgetDesigner.setErrorMessage(errorMessages);
// 	  				Ext.Msg.show({
// 	  					title : LN('sbi.chartengine.validation.errormessage'),
// 	  					message : errorMessages,
// 	  					icon : Ext.Msg.WARNING,
// 	  					closable : false,
// 	  					buttons : Ext.Msg.OK
// 	  				});
	  				result = false;
				}
				
				Sbi.trace("[Chart.saveFromCockpit]: OUT");
				return result;
			}
    </script>
	
	</body>
</html>