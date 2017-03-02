<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	
	
	
	
	
    
