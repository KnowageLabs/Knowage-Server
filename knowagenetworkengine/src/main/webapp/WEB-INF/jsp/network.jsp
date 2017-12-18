<%-- SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.  If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%-- 
author: Alberto Ghedin
--%>



<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="it.eng.spagobi.engines.network.bean.INetwork"%>
<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>	


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spago.configuration.*"%>
<%@page import="it.eng.spago.base.*"%>
<%@page import="it.eng.spagobi.engines.network.NetworkEngineInstance"%>
<%@page import="it.eng.spagobi.engines.network.NetworkEngineConfig"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.engines.network.bean.JSONNetwork"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Locale"%>
<%@page import="it.eng.spagobi.services.common.EnginConf"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	NetworkEngineInstance networkEngineInstance;
	UserProfile profile;
	Locale locale;
	String isFromCross;
	String spagobiServerHost;
	String spagobiContext;
	String spagobiSpagoController;
	String executionId;
	String engineServerHost;
	String enginePort;
	String engineContext;
	
	networkEngineInstance = (NetworkEngineInstance)ResponseContainerAccess.getResponseContainer(request).getServiceResponse().getAttribute("ENGINE_INSTANCE");
	profile = (UserProfile)networkEngineInstance.getEnv().get(EngineConstants.ENV_USER_PROFILE);
	locale = (Locale)networkEngineInstance.getEnv().get(EngineConstants.ENV_LOCALE);
	
	isFromCross = (String)networkEngineInstance.getEnv().get("isFromCross");
	if (isFromCross == null) {
		isFromCross = "false";
	}
	
	NetworkEngineConfig networkEngineConfig = NetworkEngineConfig.getInstance();
    
    spagobiServerHost = request.getParameter(SpagoBIConstants.SBI_HOST);
    spagobiContext = request.getParameter(SpagoBIConstants.SBI_CONTEXT);
    spagobiSpagoController = request.getParameter(SpagoBIConstants.SBI_SPAGO_CONTROLLER);
    INetwork net = networkEngineInstance.getNet();
    

 	engineServerHost = request.getServerName();
 	enginePort = "" + request.getServerPort();
    engineContext = request.getContextPath();
    if( engineContext.startsWith("/") || engineContext.startsWith("\\") ) {
    	engineContext = request.getContextPath().substring(1);
    }
    
    executionId = request.getParameter("SBI_EXECUTION_ID");
    if(executionId != null) {
    	executionId = "'" + request.getParameter("SBI_EXECUTION_ID") + "'";;
    } else {
    	executionId = "null";
    }   
    
    String networkInfo = "null";
    if(net.getNetworkInfo()!=null && !net.getNetworkInfo().equals("{}")){
    	networkInfo = (StringEscapeUtils.unescapeJava(net.getNetworkInfo()));
    }
   
%>


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
	
	<head>
		<%@include file="commons/includeExtJS.jspf" %>
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeCytoscapeJS.jspf" %>
		<%@include file="commons/includeSbiNetworkJS.jspf"%>
		
		<%-- START SCRIPT FOR DOMAIN DEFINITION (MUST BE EQUAL BETWEEN SPAGOBI AND EXTERNAL ENGINES) -->
		<script type="text/javascript">
		document.domain='<%= EnginConf.getInstance().getSpagoBiDomain() %>';
		</script>
		<-- END SCRIPT FOR DOMAIN DEFINITION --%>
	
	

        
        <style>
            /* The Cytoscape Web container must have its dimensions set. */
            html, body { height: 100%; width: 100%; padding: 0; margin: 0; }
            #aaa { width: 100%; height: 100%; }
                                   #tt {position:absolute; display:block}
                        #tttop {display:block; height:5px; margin-left:5px; overflow:hidden}
                        #ttcont {display:block; padding:2px 12px 3px 7px; margin-left:5px; background:#666; color:#FFF}
                        #ttbot {display:block; height:5px; margin-left:5px; overflow:hidden}
        </style>
    </head>
    
    <body>

        <script type="text/javascript">
        
        
        Sbi.config = {};
        var url = {
				  host: '<%= engineServerHost %>'
				, port: '<%= enginePort %>'
				, contextPath: '<%= engineContext %>'
			};
		
			var params = {
				SBI_EXECUTION_ID: <%=executionId %>			
			  , LIGHT_NAVIGATOR_DISABLED: 'TRUE'
			};
			Sbi.config.serviceRegistry = Ext.create('Sbi.service.ServiceRegistry',{ baseUrl: url
  																			  , baseParams: params}); 
			Sbi.config.spagobiServiceRegistry = Ext.create('Sbi.service.ServiceRegistry',{
															baseUrl: {contextPath: '<%= spagobiContext %>'}
														  , baseParams: {LIGHT_NAVIGATOR_DISABLED: 'TRUE'}
												});
			
			
			
        
                var networkEscaped = <%= net.getNetworkType().equals("json")?net.getNetworkAsString():("\""+StringEscapeUtils.escapeJavaScript( net.getNetworkAsString() )+"\"")	%>;
                var networkLink = <%= net.getNetworkCrossNavigation()	%>;
				var networkType = '<%= net.getNetworkType() %>';	
				var networkOptions = <%= StringEscapeUtils.unescapeJava(net.getNetworkOptions()) %>;	
				var networkInfo= <%= networkInfo %>;	
				var config = {};
				config.networkEscaped = networkEscaped;
				config.networkLink = networkLink;
				config.networkType = networkType;
				config.networkOptions = networkOptions;
				config.networkInfo = networkInfo;
				var network =null;
				
				Ext.onReady(function() { 
					Ext.QuickTips.init();
					network = Ext.create('Sbi.network.NetworkContainerPanel',config); //by alias

			
					var networkPanel = Ext.create('Ext.container.Viewport', {
				      layout: 'border',
				      items: [network]
				    });
					


				});
        </script>
    </body>
	


</html>




	

	
	
	
	
	
	
	
	
	
	
	
	
    