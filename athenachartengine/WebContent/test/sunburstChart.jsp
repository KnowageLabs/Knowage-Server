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
<title>Sunburst chart</title>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />

<%@include file="/WEB-INF/jsp/commons/includeExtJS5.jspf"%>

<script type="text/javascript" src='/AthenaChartEngine/test/sunburstChart.js'/></script>

<link rel="stylesheet" type="text/css" href="https://fonts.googleapis.com/css?family=Open+Sans:400,600"> 
<link rel="stylesheet" type="text/css" href="sequences.css"/>

</head>

<%-- == BODY ========================================================== --%>

<body>

	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">
	
 		Ext.onReady(function(){
 			
 			Ext.log({level: 'info'}, 'CHART: IN');

 			//initChartLibrary(mainPanel.id);			
 			

	    	Ext.Ajax.request({
				//url: 'http://localhost:8080/athena/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE',
				url: "http://localhost:8080/athena/restful-services/1.0/datasets/vendita/data",
				method: 'GET',
				timeout: 60000,
				disableCaching: false,
				
				success: function (response) {
					
					var jsonObject = Ext.decode(response.responseText);
					
					renderSunburst(jsonObject.rows);
				},
				failure: function (response) {
					console.log("NOOOOOOO");
				}
			});
 			
	    	

 			Ext.log({level: 'info'}, 'CHART: STILL IN');
 			Ext.log({level: 'info'}, 'CHART: OUT');

 		  });
		
	</script>

</body>
</html>