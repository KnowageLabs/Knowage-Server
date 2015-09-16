<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<!--  uncomment this for Old GUI Spago based -->
<%--  
<%@page import="it.eng.spagobi.commons.bo.UserProfile" %>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter"%>
<%@page import="it.eng.spago.base.SessionContainer"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO"%>
<%@page import="it.eng.spagobi.commons.bo.Role"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.tools.scheduler.to.JobInfo"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterValuesRetriever"%>
<%@page import="it.eng.spagobi.tools.scheduler.RuntimeLoadingParameterValuesRetriever"%>
<%@page import="it.eng.spagobi.tools.scheduler.FormulaParameterValuesRetriever"%>
<%@page import="it.eng.spagobi.tools.scheduler.Formula"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter"%>
--%>

<%@include file="/WEB-INF/jsp/tools/glossary/commons/headerInclude.jspf"%>
<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf"%>

<html>
<head>

<meta http-equiv="x-ua-compatible" content="IE=EmulateIE9">
<meta name="viewport" content="width=device-width">
<!-- JavaScript -->
<!--[if IE 8]> 
	<script src="http://code.jquery.com/jquery-1.11.1.min.js"></script> 
	<script src="http://cdnjs.cloudflare.com/ajax/libs/es5-shim/3.4.0/es5-shim.min.js"></script> 
	<![endif]-->

<!-- angular reference-->
<script type="text/javascript"
	src="/athena/js/lib/angular/angular_1.4/angular.js"></script>
<script type="text/javascript"
	src="/athena/js/lib/angular/angular_1.4/angular-animate.min.js"></script>
<script type="text/javascript"
	src="/athena/js/lib/angular/angular_1.4/angular-aria.min.js"></script>

<!-- angular-material-->
<script type="text/javascript"
	src="/athena/js/lib/angular/angular-material_0.10.0/angular-material.js"></script>
<link rel="stylesheet"
	href="/athena/js/lib/angular/angular-material_0.10.0/angular-material.min.css">

<script type="text/javascript"
	src="/athena/js/src/angular_1.4/scheduler/EventDefinitionApp.js"></script>
	
		<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/RestService.js"></script>

<%
	String jobName = "default jobName value", jobGroup = "default jobGroup value", jobDescription = "default jobDescription value";

	if (request.getParameter("JOB_NAME") != null && !request.getParameter("JOB_NAME").equals("")) {
		jobName = request.getParameter("JOB_NAME");
	}
	if (request.getParameter("JOB_GROUP") != null && !request.getParameter("JOB_GROUP").equals("")) {
		jobGroup = request.getParameter("JOB_GROUP");
	}
	if (request.getParameter("JOB_DESCRIPTION") != null && !request.getParameter("JOB_DESCRIPTION").equals("")) {
		jobDescription = request.getParameter("JOB_DESCRIPTION");
	}
%>
</head>

<!-- <body ng-app="EventDefinitionApp" ng-controller="LoadJobData as load"> -->
<body ng-app="EventDefinitionApp">

	<div ng-controller="LoadJobDataController as jobData"
		ng-init="jobData.initJobsValues('<%=jobName%>', '<%=jobGroup%>', '<%=jobDescription%>')">
		<!-- 
	<h2 class="md-title">Job Name: {{loadJobData.jobName}}</h2>
	<h2 class="md-title">Job Group: {{loadJobData.jobGroup}}</h2>
	<h2 class="md-title">Job Description: {{loadJobData.jobDescription}}</h2>
	-->

		<div layout-padding layout="row" flex
			ng-controller="ActivityEventController as activityEvent">

			<%-- List of activity events --%>
			<div layout-padding layout="column" flex="20"
				class="md-whiteframe-z1">
				<md-button class="md-raised" layout-align="center center">
					{{translate.load("sbi.scheduler.activity.events.newevent")}}</md-button>
				<md-content> 
				<md-list> 
					<md-list-item ng-repeat="event in jobData.events" ng-click="activityEvent.setEvent(event)">
						{{event.name}}
					</md-list-item> 
				</md-list> 
				</md-content>
			</div>

			<%-- Event detail panel --%>
			<div layout-padding layout="column" flex class="md-whiteframe-z1">
				<md-tabs md-dynamic-height ng-init="activityEvent.selectFirstEvent(jobData.events)">
					
					<md-tab id="eventTabDetail" layout-padding>
						<md-tab-label>{{translate.load("sbi.generic.details")}}</md-tab-label>
						<md-tab-body class="md-padding">
						--{{activityEvent.dataset}}--
						--{{activityEvent.frequency}}--
							<form name="contactForm" data-ng-submit="" layout="column">
								<md-input-container >
			                        <label>{{translate.load("sbi.scheduler.activity.events.event.name")}}:</label>
			                        <input ng-model="activityEvent.name" required>
	                        	</md-input-container>
								
								<md-input-container >
			                        <label>{{translate.load("sbi.scheduler.activity.events.event.description")}}:</label>
			                        <input ng-model="activityEvent.description">
	                        	</md-input-container>
								
						
			                        <label>{{translate.load("sbi.scheduler.activity.events.event.type")}}:</label> 
			                        <%-- 
			                        <p>
			                        	{{translate.load("sbi.scheduler.activity.events.event.type")}}:
			                        </p> 
			                        --%>
			                        
			                        <md-select ng-model="activityEvent.type" required>
								    	<md-option value="rest">
								    		{{translate.load("sbi.scheduler.activity.events.event.type.rest")}}</md-option>
								    	<md-option value="jms">
								    		{{translate.load("sbi.scheduler.activity.events.event.type.jms")}}</md-option>
								    	<md-option value="contextbroker">
								    		{{translate.load("sbi.scheduler.activity.events.event.type.contextbroker")}}</md-option>
								    	<md-option value="dataset">
								    		{{translate.load("sbi.scheduler.activity.events.event.type.dataset")}}</md-option>
								  	</md-select>
	                        	
								
								<md-input-container >
			                        <label>{{translate.load("sbi.scheduler.activity.events.event.suspended")}}:</label>
			                        <md-checkbox  ng-model="activityEvent.isSuspended"></md-checkbox>
	                        	</md-input-container>
	                        	
	                        	
	                        	<div ng-if="activityEvent.type=='dataset'">
	                        		<md-input-container>
    								    <label>Dataset</label>
	                        			<md-select ng-model="activityEvent.dataset"   required>
								 		  <md-option ng-repeat="item in jobData.dataset " value="{{item.id}}">{{item.label}}</md-option>
										</md-select>
								  	</md-input-container>
	                        	
	                        	</div>
	                        	
	                        	
								
	                       	</form>
						</md-tab-body>
					</md-tab> 
					<md-tab id="eventTabDocuments"> 
						<md-tab-label>{{translate.load("sbi.scheduler.activity.events.documentsmanagement")}}</md-tab-label>
						<md-tab-body>
							
						</md-tab-body> 
					</md-tab>
					
				</md-tabs>
		</div>

		</div>

	</div>

</body>
</html>