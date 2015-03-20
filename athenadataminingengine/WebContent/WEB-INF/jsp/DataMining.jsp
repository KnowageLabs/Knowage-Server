<%-- 
SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
--%>

<%-- 
author:...
--%>

<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>	


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spago.configuration.*"%>
<%@page import="it.eng.spago.base.*"%>
<%@page import="it.eng.spagobi.engines.datamining.DataMiningEngineConfig"%>
<%@page import="it.eng.spagobi.engines.datamining.DataMiningEngineInstance"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Locale"%>
<%@page import="it.eng.spagobi.services.common.EnginConf"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="it.eng.spagobi.utilities.engines.rest.ExecutionSession"%>
<%@page import="it.eng.spagobi.engines.datamining.model.DataMiningDataset"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	DataMiningEngineInstance dataMiningEngineInstance;
	UserProfile profile;
	Locale locale;
	String isFromCross;
	String spagobiServerHost;
	String spagobiContext;
	String spagobiSpagoController;
	String executionOutput;
	Boolean doUploadDatasets= false;
	
	ExecutionSession es = new ExecutionSession(request, request.getSession());
	
	dataMiningEngineInstance = (DataMiningEngineInstance)es.getAttributeFromSession(EngineConstants.ENGINE_INSTANCE );
	

	
	profile = (UserProfile)dataMiningEngineInstance.getEnv().get(EngineConstants.ENV_USER_PROFILE);
	locale = (Locale)dataMiningEngineInstance.getEnv().get(EngineConstants.ENV_LOCALE);
	
	isFromCross = (String)dataMiningEngineInstance.getEnv().get("isFromCross");
	if (isFromCross == null) {
		isFromCross = "false";
	}
    
    spagobiServerHost = request.getParameter(SpagoBIConstants.SBI_HOST);
    spagobiContext = request.getParameter(SpagoBIConstants.SBI_CONTEXT);
    spagobiSpagoController = request.getParameter(SpagoBIConstants.SBI_SPAGO_CONTROLLER);
%>


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
	
	<head>
		<%@include file="commons/includeExtJS.jspf" %>
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeSbiDataMiningJS.jspf"%>
		
		<%-- START SCRIPT FOR DOMAIN DEFINITION (MUST BE EQUAL BETWEEN SPAGOBI AND EXTERNAL ENGINES) -->
		<script type="text/javascript">
		document.domain='<%= EnginConf.getInstance().getSpagoBiDomain() %>';
		</script>
		<-- END SCRIPT FOR DOMAIN DEFINITION --%>
	
	</head>
	
	<body>
	
    	<script type="text/javascript">  
	    	Sbi.config = {};
	    	var config= {};
	

			var urlSettings = {
			    sbihost:  '<%=spagobiServerHost%>'    
			    , protocol: '<%= request.getScheme()%>'
		    	, host: '<%= request.getServerName()%>'
		    	, port: '<%= request.getServerPort()%>'
		    	, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
		    		  spagobiContext.substring(1) :
		    			spagobiContext%>'	    	   				  
		    };
		
	        var externalUrl =  urlSettings.sbihost+"/"+ urlSettings.contextPath+"/restful-services/";
			
	        Sbi.config.urlSettings = urlSettings;
	        Sbi.config.externalUrl = externalUrl;
	         
	        var params = {
		    	SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
		    };
	    	
	
		    Sbi.config.ajaxBaseParams = params;

	        Ext.onReady(function(){

 	        	var dmPanel = Ext.create('Sbi.datamining.DataMiningPanel',{}); 

	    		var dataMiningPanelViewport = Ext.create('Ext.container.Viewport', {
	    			layout:'fit',
	    			items: [dmPanel]
	    	    });
	        });
	    </script>
	
	</body>

</html>




	

	
	
	
	
	
	
	
	
	
	
	
	
    