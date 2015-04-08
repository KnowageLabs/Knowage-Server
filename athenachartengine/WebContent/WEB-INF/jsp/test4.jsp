<%-- 
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
--%>

<%-- 
author: Andrea Gioia (andrea.gioia@eng.it)
--%>

<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>	
	


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	WhatIfEngineInstance whatIfEngineInstance;
	Locale locale;
	whatIfEngineInstance = (WhatIfEngineInstance)es.getAttributeFromSession(EngineConstants.ENGINE_INSTANCE );
	locale = whatIfEngineInstance.getLocale();
	
	/**
	WhatIfEngineInstance whatIfEngineInstance;
	UserProfile profile;
	String isFromCross;
	String spagobiServerHost;
	String spagobiContext;
	String spagobiSpagoController;
	
	ExecutionSession es = new ExecutionSession(request, request.getSession());
	
	
	whatIfEngineInstance = (WhatIfEngineInstance)es.getAttributeFromSession(EngineConstants.ENGINE_INSTANCE );

	
	isFromCross = (String)whatIfEngineInstance.getEnv().get("isFromCross");
	if (isFromCross == null) {
		isFromCross = "false";
	}
	
	WhatIfEngineConfig whatIfEngineConfig = WhatIfEngineConfig.getInstance();
	**/
%>


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
	
	<head>
		<%@include file="commons/includeExtJS4.jspf" %>
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeSpagoBICockpitJS4.jspf" %>

		<%-- START SCRIPT FOR DOMAIN DEFINITION (MUST BE EQUAL BETWEEN SPAGOBI AND EXTERNAL ENGINES) -->
		<script type="text/javascript">
		document.domain='<%= EnginConf.getInstance().getSpagoBiDomain() %>';
		</script>
		<-- END SCRIPT FOR DOMAIN DEFINITION --%>
	
	</head>
	
	<body>
	
    	<script type="text/javascript">  
    	
        Ext.onReady(function(){
    		var helloPanel = Ext.create('Sbi.test.HelloWorldPanel',{}); //by alias
    		var helloPanelViewport = Ext.create('Ext.container.Viewport', {
    			layout:'fit',
    	     	items: [helloPanel]
    	    });
        });
        
        </script>
	
	</body>

</html>
	
	
	
	
	
    