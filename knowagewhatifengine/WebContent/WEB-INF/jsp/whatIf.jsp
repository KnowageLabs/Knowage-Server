<%-- 
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
--%>

<%-- 
author:...
--%>

<%@page import="it.eng.spagobi.engines.whatif.common.WhatIfConstants"%>
<%@page import="java.util.Enumeration"%>
<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>	


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spago.configuration.*"%>
<%@page import="it.eng.spago.base.*"%>
<%@page import="it.eng.spagobi.engines.whatif.WhatIfEngineConfig"%>
<%@page import="it.eng.spagobi.engines.whatif.WhatIfEngineInstance"%>
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

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	WhatIfEngineInstance whatIfEngineInstance;
	UserProfile profile;
	Locale locale;
	String isFromCross;
	String spagobiServerHost = null;
	String spagobiContext = null;
	String documentLabel = null;
	
	ExecutionSession es = new ExecutionSession(request, request.getSession());
	
	
	whatIfEngineInstance = (WhatIfEngineInstance)es.getAttributeFromSession(EngineConstants.ENGINE_INSTANCE );
//	profile = (UserProfile)whatIfEngineInstance.getEnv().get(EngineConstants.ENV_USER_PROFILE);
	locale = (Locale)whatIfEngineInstance.getEnv().get(EngineConstants.ENV_LOCALE);
    
	if(whatIfEngineInstance.getEnv().get("DOCUMENT_LABEL") != null){
		   documentLabel = (String)whatIfEngineInstance.getEnv().get("DOCUMENT_LABEL");
    }
	
	isFromCross = (String)whatIfEngineInstance.getEnv().get("isFromCross");
	if (isFromCross == null) {
		isFromCross = "false";
	}
	
	WhatIfEngineConfig whatIfEngineConfig = WhatIfEngineConfig.getInstance();
    	
   
   // if server Host anc context are null means we are in standalone version
   if(!whatIfEngineInstance.isStandalone())  {
	   spagobiServerHost = request.getParameter(SpagoBIConstants.SBI_HOST);
	   spagobiContext = request.getParameter(SpagoBIConstants.SBI_CONTEXT);
          }  
   
   // spagobiSpagoController = request.getParameter(SpagoBIConstants.SBI_SPAGO_CONTROLLER);
%>


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
	
	<head>
		<%@include file="commons/includeExtJS.jspf" %>
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeSbiWhatIfJS.jspf"%>

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
   	        
   	    	var isStandalone = <%=whatIfEngineInstance.isStandalone()%>;

   	    	var documentLabel= '<%=documentLabel != null ? documentLabel : "" %>';
   	    
   	    	<% if(!whatIfEngineInstance.isStandalone()){ %>
   	    		
   	    	
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
   	       
   	    	<%} %>

   	    	 Sbi.config.isStandalone = isStandalone;
   	         Sbi.config.documentLabel = documentLabel;
   	         
   	        var params = {
   		    	SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
   		    };
   	    	

   		    Sbi.config.ajaxBaseParams = params;
   	    	
   		    Sbi.olap.eventManager={};
   		    
   		 	var whatIfPanel;
   		    
   	        Ext.onReady(function(){
   	    		whatIfPanel = Ext.create('Sbi.olap.OlapPanel',{}); //by alias
   	    		var whatIfPanelViewport = Ext.create('Ext.container.Viewport', {
   	    			layout:'fit',
   	    	     	items: [whatIfPanel]
   	    	    });
   	        });
        
    	</script>
	
	</body>

</html>




	

	
	
	
	
	
	
	
	
	
	
	
	
    