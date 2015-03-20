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
<%@page import="java.util.Map"%>
<%@page import="it.eng.spagobi.engines.georeport.GeoReportEngineInstance"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<% 

GeoReportEngineInstance engineInstance;
engineInstance = (GeoReportEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);

Locale locale;
locale = engineInstance.getLocale();
	
%>

<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>

<html>

	<head>
		<title>Welcome to the sandbox</title>
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeGeoExt.jspf" %>
	</head>
	
	<body>
	
		<!-- Include template here  -->
		<!--   %@include file="tests/capoluoghiPropRemoteTemplate.jsp" % --> 
		<!--  %@include file="tests/usastateChorLocalTemplate.jsp" % -->
		
		
		<script language="javascript" type="text/javascript">

		 	Ext.onReady(function() {
		        var map = new OpenLayers.Map();
		        var layer = new OpenLayers.Layer.WMS(
		            "Global Imagery",
		            "http://maps.opengeo.org/geowebcache/service/wms",
		            {layers: "bluemarble"}
		        );
		        map.addLayer(layer);

		        new GeoExt.MapPanel({
		            renderTo: 'gxmap',
		            height: 400,
		            width: 600,
		            map: map,
		            title: 'A Simple GeoExt Map'
		        });
		    });

		</script>
		
		<H1>Welcome to the sandbox</H1>
		
		<div id="gxmap"></div>
	 
	</body>

</html>

