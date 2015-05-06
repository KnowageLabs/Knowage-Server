<%-- SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.  If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%-- 
author: 
--%>

<%@page import="it.eng.spagobi.engine.chart.model.conf.ChartConfig"%>
<%@page import="it.eng.spagobi.engine.chart.ChartEngineConfig"%>
<%@page import="it.eng.spagobi.engine.util.ChartEngineUtil"%>
<%@page import="it.eng.spagobi.engine.chart.ChartEngineInstance"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
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


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
<%-- == HEAD ========================================================== --%>
<head>
<title>Word cloud</title>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />

<%@include file="/WEB-INF/jsp/commons/includeExtJS5.jspf"%>
<script type="text/javascript" src ="/AthenaChartEngine/test/word.js"></script>


</head>

<%-- == BODY ========================================================== --%>

<body>


	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">
	
 		Ext.onReady(function(){
 
	    	Ext.Ajax.request({
				//url: 'http://localhost:8080/athena/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE',
				url: "http://localhost:8080/athena/restful-services/1.0/datasets/rebase/data",
				method: 'GET',
				timeout: 60000,
				disableCaching: true,
				
				headers:
				{
					'Content-Type': 'application/x-www-form-urlencoded'
				},
				success: function (response) {
					
					config = {
							
						fontFamily: "Impact",
						padding: 3
						
					};
					
					var jsono = Ext.decode(response.responseText);
					
					renderWordCloud(jsono, config);
					
				},
				failure: function (response) {
					console.log("Didn't manage");
				}
			});

 		  });
		
	</script>

</body>
</html>