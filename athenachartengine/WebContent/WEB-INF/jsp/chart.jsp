<%-- SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.  If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%-- 
author: 
--%>

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
    
%>

<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
	<%-- == HEAD ========================================================== --%>
	<head>
	   <title><%=docName.trim().length() > 0? docName: "SpagoBICockpitEngine"%></title>
       <meta http-equiv="X-UA-Compatible" content="IE=edge" />
       
        <%@include file="commons/includeExtJS5.jspf" %>
		<%@include file="commons/includeHighcharts414.jspf" %>
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeSpagoBICockpitJS4.jspf" %>
		
    </head>
	
	<%-- == BODY ========================================================== --%>
    
    <body>
    
    <p>PAGINA DI TEST</p>
    
	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">
	
		<%-- 
		var params = {
				SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
				, user_id: "<%=userId%>"
		};

	
		Sbi.config.serviceReg = new Sbi.service.ServiceReg();
		
		Sbi.config.serviceReg.addServiceBaseConf('chartServiceConf', {
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
	
		Sbi.config.serviceReg.registerService('jsonChartTemplate', {
			name: 'jsonChartTemplate'
			, description: 'Load the jsonChartTemplate'
			, resourcePath: 'jsonChartTemplate/{jsonTemplate}/'
		}, 'spagobiServiceConf');
 		--%>
		
		Ext.create('Ext.Button', {
		    text: 'Test',
		    height: 200,
		    renderTo: Ext.getBody(),
		    handler: function() {

		    	Ext.Ajax.request({
   				/*	url: Sbi.config.serviceReg.getServiceUrl('jsonChartTemplate'), */
   					url: 'http://<%= request.getServerName()%>:<%= request.getServerPort()%>/AthenaChartEngine/api/1.0/jsonChartTemplate/bar',
   					method: 'GET',
   					timeout: 60000,
   					disableCaching: false,
   					params:
   					{
   						jsonTemplate: '<%=template%>'
   						, SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
   						, user_id: "<%=userId%>"
   					},
   					headers:
   					{
   						'Content-Type': 'application/json'
   					},
   					success: function (response) {
   						var chartConf = JSON.stringify(eval('(' + response.responseText + ')'));
   						
   						Ext.Msg.alert('Content', chartConf);
   						/* 
   						*/
   						
   					},
   					failure: function (response) {
   						Ext.Msg.alert('Status', 'Request Failed: '+response.status);

   					}
   				});			       
		    }
		});
	</script>
	
	</body>
</html>