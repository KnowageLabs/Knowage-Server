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
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%	
	Locale locale;
	String spagobiServerHost;
	String spagobiContext;
	String spagobiSpagoController;
	String formDocumentId;
	String language, country;
	
	language = request.getParameter(SpagoBIConstants.SBI_LANGUAGE); 
	country = request.getParameter(SpagoBIConstants.SBI_COUNTRY);
	try {
		locale = new Locale(language, country);
	} catch (Exception e) {
		locale = Locale.UK;
	}
	
	formDocumentId = request.getParameter("document");
	spagobiServerHost = request.getParameter(SpagoBIConstants.SBI_HOST);
    spagobiContext = request.getParameter(SpagoBIConstants.SBI_CONTEXT);
    spagobiSpagoController = "/servlet/AdapterHTTP"; //request.getParameter(SpagoBIConstants.SBI_SPAGO_CONTROLLER);
%>
<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>

<html>
	<head>
		<%@include file="commons/includeExtJS.jspf" %>
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeSbiQbeJS.jspf"%>
		
		<title>Template Builder</title>
	</head>
	
	<body>
	<script type="text/javascript">  
		Sbi.config = {};
	
		var url = {
	    	host: '<%= request.getServerName()%>'
	    	, port: '<%= request.getServerPort()%>'
	    	, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
	    	   				  request.getContextPath().substring(1):
	    	   				  request.getContextPath()%>'
	    	    
	    };

	    var params = {
	    	SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
	    	, "<%= SpagoBIConstants.SBI_LANGUAGE %>" : "<%= locale.getLanguage() %>"
	    	, "<%= SpagoBIConstants.SBI_COUNTRY %>" : "<%= locale.getCountry() %>"
	    };

	    Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
	    	baseUrl: url
	        , baseParams: params
	    });

	    var remoteUrl = {
			completeUrl: '<%= spagobiServerHost + spagobiContext + spagobiSpagoController %>'
		};

	    var remoteServiceRegistryParams = {
		    	SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
		    	, LIGHT_NAVIGATOR_DISABLED: 'TRUE'
		    	, "NEW_SESSION" : "TRUE"
		}; 
		Sbi.config.remoteServiceRegistry = new Sbi.service.ServiceRegistry({
			baseUrl: remoteUrl
		    , baseParams: remoteServiceRegistryParams
		    , defaultAbsolute: true
		});

	

		var documentTemplateBuilderPanel = new Sbi.formtemplate.DocumentTemplateBuilder({
			formDocumentId: <%= formDocumentId %>
		});
		var viewport = new Ext.Viewport({border: false, layout: 'border', items: [{border: false, region: 'center', items: [documentTemplateBuilderPanel]}]});  
	 </script>
	</body>
</html>