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
author: 
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@page import="java.util.Locale"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.kpi.config.bo.KpiValue"%>
<%@page import="it.eng.knowage.engine.kpi.KpiEngineInstance"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="org.json.JSONObject"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	KpiEngineInstance engineInstance;
	IEngUserProfile profile;
	String profileJSONStr;
	Map env;
	String contextName;
	String environment;
	String executionRole;
	Locale locale;
	String template;
	String docId;
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
	String isTechnicalUser;
	List<String> includes;
	String datasetLabel;
	//String kpiValue = "";
	//from cockpit
	boolean isCockpit = false;
	String aggregations = "";
	String selections = "";
	String associations = "";
	String widgetId = "";
	String metaData = "";

	engineInstance = (KpiEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
	env = engineInstance.getEnv();
	profile = engineInstance.getUserProfile();
	profile = (IEngUserProfile) request.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	profileJSONStr = new ObjectMapper().writeValueAsString(profile);
	// locale = engineInstance.getLocale();
	
	contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); 
	environment = request.getParameter("SBI_ENVIRONMENT"); 
	executionRole = (String)env.get(EngineConstants.ENV_EXECUTION_ROLE);
	userId = (engineInstance.getDocumentUser()==null)?"":engineInstance.getDocumentUser().toString();
	isTechnicalUser = (engineInstance.isTechnicalUser()==null)?"":engineInstance.isTechnicalUser().toString();
	template = engineInstance.getTemplate().toString(0);
	
	JSONObject templateObj = engineInstance.getTemplate();
	JSONObject chartObj = templateObj.getJSONObject("chart");
	//JSONObject optionsObj = chartObj.getJSONObject("options");
	String type = (String)chartObj.get("type");
	// String model = (String)chartObj.get("model");
	
	// Boolean showlineargauge = new Boolean((String)optionsObj.get("showlineargauge"));
	// if(env.get("KPI_VALUE")!=null){
	// 	kpiValue = env.get("KPI_VALUE").toString();
	// }
	
	if(env.get("EXECUTE_COCKPIT") != null){
		isCockpit = true;
		datasetLabel = env.get(EngineConstants.ENV_DATASET_LABEL)!=null?(String)env.get(EngineConstants.ENV_DATASET_LABEL):"";
		aggregations = env.get("AGGREGATIONS")!=null?(String)env.get("AGGREGATIONS"):"";
		selections = env.get("SELECTIONS")!=null?(String)env.get("SELECTIONS"):"";
		associations = env.get("ASSOCIATIONS")!=null?(String)env.get("ASSOCIATIONS"):"";
		metaData = env.get("METADATA")!=null?(String)env.get("METADATA"):"";
		widgetId = env.get("WIDGETID")!=null?(String)env.get("WIDGETID"):"";
	} else {
		datasetLabel = (engineInstance.getDataSet() != null )?
				engineInstance.getDataSet().getLabel() : "" ;
	}
	
	/*
	*/	
	docId = (env.get("DOCUMENT_ID") != null? (String)env.get("DOCUMENT_ID") : "");
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
	
	boolean fromMyAnalysis = false;
	if(request.getParameter("MYANALYSIS") != null && request.getParameter("MYANALYSIS").equalsIgnoreCase("TRUE")){
		fromMyAnalysis = true;
	}else{
		if (request.getParameter("SBI_ENVIRONMENT") != null && request.getParameter("SBI_ENVIRONMENT").equalsIgnoreCase("MYANALYSIS")){
			fromMyAnalysis = true;
		}
	}
	
	/*
	*/
    Map analyticalDrivers  = engineInstance.getAnalyticalDrivers();
    Map driverParamsMap = new HashMap();
	for(Object key : engineInstance.getAnalyticalDrivers().keySet()){
		if(key instanceof String && !key.equals("widgetData")){
			String value = request.getParameter((String)key);
			if(value!=null){
				driverParamsMap.put(key, value);
			}
		}
	}
	String driverParams = new JSONObject(driverParamsMap).toString(0).replaceAll("'", "\\\\'");
	String uuidO=request.getParameter("SBI_EXECUTION_ID")!=null? request.getParameter("SBI_EXECUTION_ID"): "null";
%>



<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>

<html ng-app="kpiViewerModule">
<%-- == HEAD ========================================================== --%>
<head>
<!--
<title>KpiEngine</title>
-->
<meta http-equiv="X-UA-Compatible" content="IE=edge" />

<%--
<%@include file="commons/angular/angularResource.jspf"%>
--%>
<%@include file="commons/angular/angularImport.jsp"%>

<script>
	var sbiExecutionId = <%=request.getParameter("SBI_EXECUTION_ID")!=null? "'"+request.getParameter("SBI_EXECUTION_ID")+"'" : "null"%>;
	<%-- var userId = '<%=userId%>'; --%>
	var userId = '<%=request.getParameter("user_id")%>';
	var hostName = '<%=request.getServerName()%>';
	var serverPort = '<%=request.getServerPort()%>';
	var protocol = '<%=request.getScheme()%>';
</script>
<!-- Styles -->

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/nvd3/1.8.2-dev/nv.d3.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/css/commons/css/customStyle.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/angular_1.x/kpi-widget/css/kpiWidgetStyle.css">
<!-- Scripts -->
<%--
<script type="text/javascript" 
		src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/adapters/standalone-framework.js"></script>
<script type="text/javascript" 
		src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/highcharts.src.js"></script>
<script type="text/javascript" 
		src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/highcharts-more.js"></script>
<script type="text/javascript" 
		src="${pageContext.request.contextPath}/js/lib/gaugeJs/gauge.js"></script>
--%>
<script type="text/javascript" 
		src="${pageContext.request.contextPath}/js/lib/d3/3.5.5/d3.js"></script>
<%--
--%>
<script type="text/javascript" 
		src="${pageContext.request.contextPath}/js/lib/nvd3/1.8.2-dev/nv.d3.js"></script>
<script type="text/javascript" 
		src="${pageContext.request.contextPath}/js/lib/angular-nvd3/1.0.6/dist/angular-nvd3.js"></script>
<script type="text/javascript" 
		src="${pageContext.request.contextPath}/js/angular_1.x/gaugeNgDirective/gaugeNgDirectiveApp.js"></script>
		
</head>

<body ng-controller="kpiViewerController" ng-init="init()" layout="column" layout-align="center center" class="kn-schedulerKpi">
	
	<%--
	<h2>KpiEngine</h2>
	<div style="padding:2em; font-size: 0.7em">targetValue: {{documentData.targetValue | json}}</div>
	
	<div layout-align="center center" layout="row" flex >
		<div id="kpiViewer_<%=docId%>"></div>
	</div>
	<div style="padding:2em; font-size: 1.2em">kpiOptions.showlineargauge: {{kpiOptions.showlineargauge | json}}</div>
	
	<div style="padding:2em; font-size: 0.7em">template: {{documentData.template | json}}</div>
	<div style="padding:2em; font-size: 0.7em">kpiValue: {{documentData.kpiValue | json}}</div>
	<div style="padding:2em; font-size: 0.7em">kpiListValue: {{documentData.kpiListValue | json}}</div>
	--%>
<%
if(type.equalsIgnoreCase("kpi")) {
	String model = (String)chartObj.get("model");
	
	if(model.equalsIgnoreCase("widget")) {
		JSONObject optionsObj = chartObj.getJSONObject("options");
		String vieweas = (String)optionsObj.get("vieweas");
		
		if(vieweas.equalsIgnoreCase("speedometer")) {
			Boolean showlineargauge = new Boolean((String)optionsObj.get("showlineargauge"));
			
			if(showlineargauge) {
%>
 <kpi-widget flex
 	ng-model="documentData"
	gauge-size=gaugeSize 
	gauge-min-value=gaugeMinValue 
	gauge-max-value=gaugeMaxValue 
	gauge-value = gaugeValue 
	gauge-target-value = gaugeTargetValue
	threshold-stops =thresholdStops 
	percentage=percentage
></kpi-widget>

<!--
<kpi-linear-gauge flex layout="column" layout-align="center"
	gauge-id="documentData.docId"
	label="documentData.docLabel"
	size="linearGaugeSize"
	min-value="gaugeMinValue"
	max-value="gaugeMaxValue"
	value="gaugeValue"
	target-value="gaugeTargetValue"
	threshold-stops="thresholdStops"
	show-value="documentData.template.chart.options.showvalue"
	show-thresholds="documentData.template.chart.options.showthreshold"
	value-precision="documentData.template.chart.options.precision"
	font-conf="documentData.template.chart.style.font"
></kpi-linear-gauge>
-->

<%
			} else { 
%>
 <kpi-widget ng-model="documentData"
gauge-size=gaugeSize gauge-min-value=gaugeMinValue gauge-value = gaugeValue gauge-target-value = gaugeTargetValue
threshold-stops =thresholdStops percentage=percentage
></kpi-widget> 
<!--<kpi-gauge flex layout="column" layout-align="center"
		gauge-id="documentData.docId"
		label="documentData.docLabel"
		size="gaugeSize"
		min-value="gaugeMinValue"
		max-value="gaugeMaxValue"
		value="gaugeValue"
		target-value="gaugeTargetValue"
		threshold-stops="thresholdStops"
		show-value="documentData.template.chart.options.showvalue"
		show-target="documentData.template.chart.options.showtarget"
		show-thresholds="documentData.template.chart.options.showthreshold"
		value-precision="documentData.template.chart.options.precision"
		font-conf="documentData.template.chart.style.font"
	></kpi-gauge> 	-->

<%
			} 
		} 
	}
} else if(type.equalsIgnoreCase("scorecard")) {
%>

<!-- SCORECARD -->

<%
}
%>


<!-- 	
	<nvd3 options="getSpeedoLinearConf()" data="getSpeedoLinearData()" config="getSpeedoLinearConfig()"></nvd3>
-->
		
	<%-- kpi document angular imports --%>
	<script type="text/javascript">
	(function() {
		var kpiViewerModule = angular.module('kpiViewerModule', 
				['sbiModule', 'ngSanitize', 'ngAnimate'
				 , 'gaugeNgDirectiveApp'
				 , 'nvd3','kpi-widget'
				 ]);
		kpiViewerModule.config(['$mdThemingProvider', function($mdThemingProvider) {
			$mdThemingProvider.theme('knowage')
			$mdThemingProvider.setDefaultTheme('knowage');
		}]);

		kpiViewerModule.factory('documentData', function() {
			var documentTemplate = JSON.parse('<%=template%>');
			<%-- var kpiValue = JSON.parse('<%=kpiValue%>'); --%>
			
			var obj = {
				template : documentTemplate,
				docLabel : '<%=docLabel %>',
				docId : '<%=docId%>',
				kpiValue : {threshold:{thresholdValues:[]}},
				kpiListValue : [],
				targetValue : []
			};
			return obj;
		});
	})();
	</script>
	<script type="text/javascript" 
			src="${pageContext.request.contextPath}/js/angular_1.x/kpi-widget/kpiWidgetController.js"></script>
	<script type="text/javascript" 
			src="${pageContext.request.contextPath}/js/angular_1.x/kpiviewer/utils/kpiViewerFactory.js"></script>
	<script type="text/javascript" 
			src="${pageContext.request.contextPath}/js/angular_1.x/kpiviewer/utils/kpiViewerServices.js"></script>
	<script type="text/javascript" 
			src="${pageContext.request.contextPath}/js/angular_1.x/kpiviewer/kpiViewerController.js"></script>

			
			
</body>
</html>
