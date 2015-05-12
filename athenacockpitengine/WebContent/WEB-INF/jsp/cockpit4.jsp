<%-- SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.  If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%-- 
author: Andrea Gioia (andrea.gioia@eng.it)
--%>

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
<%@page import="it.eng.spagobi.engine.cockpit.CockpitEngineInstance"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<% 
	CockpitEngineInstance engineInstance;
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
	
	String documentMode;

	engineInstance = (CockpitEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
	env = engineInstance.getEnv();
	profile = engineInstance.getUserProfile();
	profileJSONStr = new ObjectMapper().writeValueAsString(profile);
	locale = engineInstance.getLocale();
	
	contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); 
	environment = request.getParameter("SBI_ENVIRONMENT"); 
	executionRole = (String)env.get(EngineConstants.ENV_EXECUTION_ROLE);
	userId = (engineInstance.getDocumentUser()==null)?"":engineInstance.getDocumentUser().toString();
	isTechnicalUser = (engineInstance.isTechnicalUser()==null)?"":engineInstance.isTechnicalUser().toString();
	template = engineInstance.getTemplate().toString();
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
	
	documentMode = (request.getParameter("documentMode")==null)?"VIEW":request.getParameter("documentMode");
	
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
%>

<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
	<%-- == HEAD ========================================================== --%>
	<head>
	   <title><%=docName.trim().length() > 0? docName: "SpagoBICockpitEngine"%></title>
       <meta http-equiv="X-UA-Compatible" content="IE=edge" />
       
        <%@include file="commons/includeExtJS4.jspf" %>
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeSpagoBICockpitJS4.jspf" %>
    </head>
	
	<%-- == BODY ========================================================== --%>
    
    <body>
    
	
	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">

		var template = <%= template %>;

		Sbi.user = <%= profileJSONStr %>;
		
		Sbi.config = {};
		var url = {
			protocol: '<%= request.getScheme()%>'   
		    , host: '<%= request.getServerName()%>'
		    , port: '<%= request.getServerPort()%>'
		    , contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>'
		    , controllerPath: null // no cotroller just servlets   
		};
		
		var executionContext = {};
        <% 
        Iterator it = analyticalDrivers.keySet().iterator();
		while(it.hasNext()) {
			String parameterName = (String)it.next();
			String parameterValue = (String)analyticalDrivers.get(parameterName);		
			String quote = (parameterValue.startsWith("'"))? "" : "'";
			if ( parameterValue.indexOf(",") >= 0){					
		 %>
				executionContext ['<%=parameterName%>'] = [<%=quote%><%=parameterValue%><%=quote%>];
		<%	}else{
		%>
				executionContext ['<%=parameterName%>'] = <%=quote%><%=parameterValue%><%=quote%>;
		 <%
		 	}							
		 }
        %>
        Sbi.config.executionContext = executionContext;
	
		var params = {
				SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
				, user_id: "<%=userId%>"
		};
	
		Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
		  	baseUrl: url
		  , baseParams: params
		});
		
				
		// test
		Sbi.config.serviceReg = new Sbi.service.ServiceReg();
		
		Sbi.config.serviceReg.addServiceBaseConf('cockpitServiceConf', {
			method: "GET"
			
			, baseUrlConf: {
				protocol: '<%= request.getScheme()%>'     
				, host: '<%= request.getServerName()%>'
				, port: '<%= request.getServerPort()%>'
				, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>'
			}
			, controllerConf: {
				controllerPath: 'api'   
				, serviceVersion: '1.0'
				, serviceVersionParamType: 'path' 
			}
		
			, basePathParams:{}
			, baseQueryParams: params
			, baseFormParams: {}
	
			//, absolute: false
		});
		
		Sbi.config.serviceReg.addServiceBaseConf('spagobiServiceConf', {
			method: "GET"
			
			, baseUrlConf: {
				protocol: '<%= request.getScheme()%>'     
				, host: '<%= request.getServerName()%>'
				, port: '<%= request.getServerPort()%>'
				, contextPath: 'SpagoBI'
			}
			, controllerConf: {
				controllerPath: 'restful-services'   
				, serviceVersion: '1.0'
				, serviceVersionParamType: 'path' 
			}
		
			, basePathParams:{}
			, baseQueryParams: params
			, baseFormParams: {}
	
			//, absolute: false
		});
	
		Sbi.config.serviceReg.registerService('loadDataSetStore', {
			name: 'loadDataSetStore'
			, description: 'Load the store of the specified dataset'
			, resourcePath: 'datasets/{datasetLabel}/data'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadChartDataSetStore', {
			name: 'loadChartDataSetStore'
			, description: 'Load the store of the specified dataset for a chart widget'
			, resourcePath: 'datasets/{datasetLabel}/chartData'
			, method: 'POST'
		}, 'spagobiServiceConf');
		
	    Sbi.config.serviceReg.registerService('cleanCache', {
	            name: 'cleanCache'
	            , description: 'clean the cache of used datasets'
	            , resourcePath: 'cache/{datasetLabels}/cleanCache'
	            , method: 'DELETE'
	        }, 'spagobiServiceConf');
	    
	   Sbi.config.serviceReg.registerService('checkAssociation', {
               name: 'checkAssociation'
               , description: 'check if association is valid'
               , resourcePath: 'datasets/{association}/checkAssociation'
               , method: 'POST'
           }, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadJoinedDataSetStore', {
			name: 'loadJoinedDataSetStore'
			, description: 'Load the store of the specified joined dataset'
			, resourcePath: 'datasets/joined/data'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadDataSetField', {
			name: 'loadDataSetField'
			, description: 'Load all the fields of the specified dataset'
			, resourcePath: 'datasets/{datasetLabel}/fields'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadDataSetParams', {
			name: 'loadDataSetParams'
			, description: 'Load all the params of the specified dataset'
			, resourcePath: 'datasets/{datasetLabel}/parameters'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadEnterpriseDataSets', {
			name: 'loadEnterpriseDatasets'
			, description: 'Load all enterprise datasets'
			, resourcePath: 'datasets/enterprise'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadOwnedDataSets', {
			name: 'loadOwnedDataSets'
			, description: 'Load all datasets owned by the user'
			, resourcePath: 'datasets/owned'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadSharedDataSets', {
			name: 'loadSharedDataSets'
			, description: 'Load all datasets shared by other users (eneterprise and owned datsets are not included)'
			, resourcePath: 'datasets/shared'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadMyDataDataSets', {
			name: 'loadMyDataDataSets'
			, description: 'Load all datasets visible to the user in MyData panel (i.e. owned + shared + enterprise)'
			, resourcePath: 'datasets/mydata'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadDocumentParams', {
			name: 'loadDocumentParams'
			, description: 'Load all the params of the specified document'
			, resourcePath: 'documents/{documentLabel}/parameters'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('setAssociations', {
			name: 'setAssociations'
			, description: 'Set the associations'
			, resourcePath: 'associations'
			, method: 'POST'
		}, 'cockpitServiceConf');
		
		Sbi.config.serviceReg.registerService('getCrosstab', {
			name: 'getCrosstab'
			, description: 'Get the crosstab in HTML'
			, resourcePath: 'crosstab'
			, method: 'GET'
		}, 'cockpitServiceConf');
		
		Sbi.config.serviceReg.registerService('updateCrosstab', {
			name: 'updateCrosstab'
			, description: 'Update the crosstab in HTML'
			, resourcePath: 'crosstab/update'
			, method: 'GET'
		}, 'cockpitServiceConf');
		
		Sbi.config.serviceReg.registerService('setCrosstabSort', {
			name: 'setCrosstabSort'
			, description: 'Get the crosstab in HTML'
			, resourcePath: 'crosstab/sort'
			, method: 'POST'
		}, 'cockpitServiceConf');
	
		/*
		var testUrl = service.getServiceUrl({pathParams: {datasetLabel: 'ds__405004519'}, queryParams: {frutto: "mela"}});
		alert(testUrl);
		service.on('request', function(service, response, options){alert('Called service [' + service.name + ']');}, this);
		service.doRequest({pathParams: {datasetLabel: 'ds__405004519'}, queryParams: {frutto: "mela"}});
		Sbi.config.serviceReg.callService('loadDataSetField', {
			pathParams: {datasetLabel: 'ds__405004519'}
			, queryParams: {frutto: "mela"}
			, success: function(response) {alert(response.responseText);}
			, failure: function(response) {alert(response.responseText);}
			, scope: this
		});
		*/
		
		// test
		
		
		
		
		
		Sbi.storeManager = new Sbi.data.StoreManager({storesConf: template.storesConf, template: template});
		//Sbi.storeManager.setConfiguration(template.storesConf);
		
		Sbi.config.docLabel ="<%=docLabel%>";
		Sbi.config.docVersion = "<%=docVersion%>";
		Sbi.config.userId = "<%=userId%>";
		Sbi.config.isTechnicalUser = "<%=isTechnicalUser%>";
		Sbi.config.docAuthor = "<%=docAuthor%>";
		Sbi.config.docName = "<%=docName.replace('\n', ' ')%>";
		Sbi.config.docDescription = "<%=docDescription.replace('\n', ' ')%>";
		Sbi.config.docIsPublic= "<%=docIsPublic%>";
		Sbi.config.docIsVisible= "<%=docIsVisible%>";
		Sbi.config.docPreviewFile= "<%=docPreviewFile%>";
		Sbi.config.docCommunities= "<%=docCommunity%>";
		Sbi.config.docFunctionalities= <%=docFunctionalities%>;		
		Sbi.config.SBI_EXECUTION_ID= <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>;
		Sbi.config.fromMyAnalysis = <%=fromMyAnalysis%>;
		Sbi.config.environment = "<%=environment%>";
		Sbi.config.contextName =  '<%= contextName %>';
		Sbi.config.documentMode = "<%=documentMode%>";
		
		var cockpitPanel = null;
		    
		Ext.onReady(function(){
					
			Ext.QuickTips.init();   
				
			cockpitPanel = new Sbi.cockpit.MainPanel({analysisState: template});	
				
			var viewport = new Ext.Viewport({
				id:    'view',
		   		layout: 'fit',
		        items: [cockpitPanel]
			});
		});
	
	</script>
	
	</body>

</html>

