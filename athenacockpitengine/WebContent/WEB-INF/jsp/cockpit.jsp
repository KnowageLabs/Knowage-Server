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

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<% 
	CockpitEngineInstance engineInstance;
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
	List<String> includes;
	
	String documentMode;

	engineInstance = (CockpitEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
	env = engineInstance.getEnv();
	locale = engineInstance.getLocale();
	
	contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); 
	environment = request.getParameter("SBI_ENVIRONMENT"); 
	executionRole = (String)env.get(EngineConstants.ENV_EXECUTION_ROLE);
	userId = (engineInstance.getDocumentUser()==null)?"":engineInstance.getDocumentUser().toString();
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
%>

<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
	<%-- == HEAD ========================================================== --%>
	<head>
		<title><%=docName.trim().length() > 0? docName: "SpagoBICockpitEngine"%></title>
       
        <%@include file="commons/includeExtJS4.jspf" %>
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeSpagoBICockpitJS4.jspf" %>
    </head>
	
	<%-- == BODY ========================================================== --%>
    
    <body>
    
	
	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">

		var template = <%= template %>;

		Sbi.config = {};
		var url = {
			protocol: '<%= request.getScheme()%>'   
		    , host: '<%= request.getServerName()%>'
		    , port: '<%= request.getServerPort()%>'
		    , contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>'
		    , controllerPath: null // no cotroller just servlets   
		};
	
		var params = {
				SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
		};
	
		Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
		  	baseUrl: url
		    , baseParams: params
		});
		
		Sbi.storeManager = new Sbi.data.StoreManager();

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

