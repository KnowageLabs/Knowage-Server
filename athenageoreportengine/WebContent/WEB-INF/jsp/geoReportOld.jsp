<%-- SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.  If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%-- 
author: Andrea Gioia (andrea.gioia@eng.it)
--%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
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
<%@page import="it.eng.spagobi.engines.georeport.GeoReportEngineInstance"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="java.util.Iterator"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<% 
	GeoReportEngineInstance engineInstance;
	Map env;
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
	String docDatasetLabel;
	String userId;
	List<String> includes;
	boolean visibleDataSet;

	engineInstance = (GeoReportEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
	env = engineInstance.getEnv();
	locale = engineInstance.getLocale();

	executionRole = (String)env.get(EngineConstants.ENV_EXECUTION_ROLE);
	userId = (engineInstance.getDocumentUser()==null)?"":engineInstance.getDocumentUser().toString();
	template = engineInstance.getGuiSettings().toString();
	docLabel = (engineInstance.getDocumentLabel()==null)?"":engineInstance.getDocumentLabel().toString();
	docVersion = (engineInstance.getDocumentVersion()==null)?"":engineInstance.getDocumentVersion().toString();
	docAuthor = (engineInstance.getDocumentAuthor()==null)?"":engineInstance.getDocumentAuthor().toString();
	docName = (engineInstance.getDocumentName()==null)?"":engineInstance.getDocumentName().toString();
	docDescription = (engineInstance.getDocumentDescription()==null)?"":engineInstance.getDocumentDescription().toString();
	docIsPublic= (engineInstance.getDocumentIsPublic()==null)?"":engineInstance.getDocumentIsPublic().toString();
	docIsVisible= (engineInstance.getDocumentIsVisible()==null)?"":engineInstance.getDocumentIsVisible().toString();
	docPreviewFile= (engineInstance.getDocumentPreviewFile()==null)?"":engineInstance.getDocumentPreviewFile().toString();	
	docDatasetLabel = (engineInstance.getDataSet()==null)?"":engineInstance.getDataSet().getLabel();
	String docDatasetName = (engineInstance.getDataSet()==null)?"":engineInstance.getDataSet().getName();
	docCommunities= (engineInstance.getDocumentCommunities()==null)?null:engineInstance.getDocumentCommunities();
	docCommunity = (docCommunities == null || docCommunities.length == 0) ? "": docCommunities[0];
	docFunctionalities= (engineInstance.getDocumentFunctionalities()==null)?new ArrayList():engineInstance.getDocumentFunctionalities();
	visibleDataSet = (engineInstance.isVisibleDataSet());

	includes = engineInstance.getIncludes();
	
    // gets analytical driver
    Map analyticalDrivers  = engineInstance.getAnalyticalDrivers();

	boolean forceIE8Compatibility = false;
%>

<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
	<%-- == HEAD ========================================================== --%>
	<head>
		<title><%=docName.trim().length() > 0? docName: "SpagoBIGeoReportEngine"%></title>
		
		<% if (forceIE8Compatibility == true){ %> 
			<meta http-equiv="X-UA-Compatible" content="IE=8" />
		<%} %>
		
        <!--[if IE]>
            <script src="https://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
        <![endif]-->
        
        <!-- Stylesheets -->
       
        <link href="css/standard.css" rel="stylesheet" media="screen,projection,print" type="text/css" />
        
        
        <!--[if IE]>
            <link href="css/ie9.css" rel="stylesheet" media="screen,projection,print" type="text/css" />
        <![endif]-->
        <!--[if lte IE 8]>
            <link href="css/ie8.css" rel="stylesheet" media="screen,projection,print" type="text/css" />
        <![endif]-->
        <!--[if lte IE 7]>
            <link href="css/ie7.css" rel="stylesheet" media="screen,projection,print" type="text/css" />
        <![endif]-->
        
		<%@include file="commons/includeMessageResource.jspf" %>
        <%@include file="commons/includeGeoExt.jspf" %>
		<%@include file="commons/includeExtensionsJS.jspf" %>
		<%@include file="commons/includeSpagoBIGeoReportJS.jspf" %>
    </head>
	
	<%-- == BODY ========================================================== --%>
    
    <!--[if IE 8]>
    <body class="lte-8 ie-8 map-body">
    <![endif]-->
    
    <!--[if lte IE 7]>
    <body class="lte-8 lte-7 map-body">
    <![endif]-->
    
    <!--[if gt IE 8]>
    <body class="map-body ie-9">
    <![endif]-->
    
    <!--[if !IE]><!-->
    <!--   <body class="map-body"> -->
    <body>
    <script>  
		if (/*@cc_on!@*/false) {  
			document.documentElement.className+=' ie10';  
		}  
	</script>
    <!--<![endif]-->
	
	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">

		Sbi.template = <%= template %>;

		if(Sbi.template.role) {
			Sbi.template.role = Sbi.template.role.charAt(0) == '/'? 
								Sbi.template.role.charAt(0): 
								'/' + Sbi.template.role.charAt(0);
		}
		var executionRole = '<%= executionRole%>';
		Sbi.template.role = executionRole || Sbi.template.role;
		
		var executionContext = {};
        <% 
        Iterator it = analyticalDrivers.keySet().iterator();
		while(it.hasNext()) {
			String parameterName = (String)it.next();
			String parameterValue = (String)analyticalDrivers.get(parameterName);		
			//System.out.println("parameterName: " + parameterName + " - parameterValue: " + parameterValue);
			//if (parameterValue != null && !parameterValue.equals("")){ //NECESSARIO il test sulla stringa vuota??
			String quote = (parameterValue.startsWith("'"))? "" : "'";
			if ( parameterValue.indexOf(",") >= 0){					
		 %>
				executionContext ['<%=parameterName%>'] = [<%=quote%><%=parameterValue%><%=quote%>];
		<%	}else{
		%>
				executionContext ['<%=parameterName%>'] = <%=quote%><%=parameterValue%><%=quote%>;
		 <%
		 	}							
		    //}
		 } //while
        %>
        Sbi.template.executionContext = executionContext;
		
		execDoc = function(docLab, role, params, dispToolbar, dispSlide,frameId, height) {
			
			var h = height || '100%';
			
			var html = Sbi.sdk.api.getDocumentHtml({
					documentLabel: docLab
					, executionRole: role // "/" + role
					, parameters: params 
			      	, displayToolbar: dispToolbar
					, displaySliders: dispSlide
					, useExtUI: false
					, iframe: {
			        	id: frameId
			          	, height: h
				    	, width: '100%'
						, style: 'border: 0px;'
					}
				});
				
				//var html = '<h1>Prova provata ' + docLab + ' </h1>'
			    return html;
		};		
	</script>
	
		<script language="javascript" type="text/javascript">

		Sbi.config = {};
		var url = {
			protocol: '<%= request.getScheme()%>'   
		    , host: '<%= request.getServerName()%>'
		    , port: '<%= request.getServerPort()%>'
		    , contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>'
		    , controllerPath: null // no cotroller just servlets   
		};

		Sbi.sdk.services.setBaseUrl({
			protocol: '<%= request.getScheme()%>'     
		    , host: url.host
		    , port: url.port
		    //, contextPath: 'SpagoBI'
		    //, controllerPath: 'servlet/AdapterHTTP'  
		});
	
		var params = { };
	
		Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
		  	baseUrl: url
		    , baseParams: params
		});

		Sbi.config.docLabel ="<%=docLabel%>";
		Sbi.config.docVersion = "<%=docVersion%>";
		Sbi.config.userId = "<%=userId%>";
		Sbi.config.docAuthor = "<%=docAuthor%>";
		Sbi.config.docName = "<%=docName.replace('\n', ' ')%>";
		Sbi.config.docDescription = "<%=docDescription.replace('\n', ' ')%>";
		Sbi.config.docIsPublic= "<%=docIsPublic%>";
		Sbi.config.docIsVisible= "<%=docIsVisible%>";
		Sbi.config.docPreviewFile= "<%=docPreviewFile%>";
		Sbi.config.docCommunities= "<%=docCommunity%>";
		Sbi.config.docFunctionalities= <%=docFunctionalities%>;
		Sbi.config.docDatasetLabel= "<%=docDatasetLabel%>";
		Sbi.config.docDatasetName= "<%=docDatasetName%>";
		    
		Sbi.config.visibleDataSet=<%=visibleDataSet%>;
		    
		var geoReportPanel = null;
		    
		Ext.onReady(function(){
			
			if(Sbi.config.visibleDataSet){
				
				Ext.QuickTips.init();   
				
				geoReportPanel = new Sbi.geo.MainPanel(Sbi.template);	
				
		   		var viewport = new Ext.Viewport({
		   			id:    'view',
		      		layout: 'fit',
		            items: [geoReportPanel]
		        });
		   	
			}else{
				Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.dataset.no.visible', [Sbi.config.docDatasetName, Sbi.config.docName]));
			}
		});
	
	</script>
		

		<!-- commenting the following block only the legend control will be displayed on the map. why?  -->
		<div id="map"></div>
	
	</body>

</html>

