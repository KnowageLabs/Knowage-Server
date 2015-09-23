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
	
	
<!-- 	queste sono per la angular list -->
<!-- angular tree -->
	<link rel="stylesheet" 	href="/athena/js/lib/angular/angular-tree/angular-ui-tree.min.css">
	<script type="text/javascript" src="/athena/js/lib/angular/angular-tree/angular-ui-tree.js"></script>
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/tree-style.css">
	
	<!-- context menu -->
	<script type="text/javascript" src="/athena/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>
	
	<!--pagination-->
	<script type="text/javascript" src="/athena/js/lib/angular/pagination/dirPagination.js"></script>
	
	<link rel="stylesheet" href="/athena/themes/glossary/css/font-awesome-4.3.0/css/font-awesome.min.css">

	<!-- angular list -->
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/angular-list.css">
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/AngularList.js"></script>

<!-- fine -->
		
	<link rel="stylesheet" type="text/css" href="/athena/themes/scheduler/css/scheduler_style.css">
	
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

<body ng-app="EventDefinitionApp">
	<div ng-controller="LoadJobDataController as jobDataCtrl" layout-fill 
		ng-init="jobDataCtrl.initJobsValues('<%=jobName%>', '<%=jobGroup%>', '<%=jobDescription%>');	">
		
		<div layout-padding layout="row" flex layout-fill class="h100"  ng-controller="ActivityEventController as activityEvent" >



			<%-- Event detail panel --%>
			<div  layout="column" flex class="md-whiteframe-z1">
				<md-tabs  ng-init="activityEvent.selectFirstEvent(jobDataCtrl.events)" flex>
					
					<md-tab id="eventTabDetail" layout-padding flex>
						<md-tab-label>{{translate.load("sbi.generic.details")}}</md-tab-label>
						<md-tab-body class="md-padding " flex>
							<form name="contactForm" ng-submit="contactForm.$valid && activityEvent.saveEvent(contactForm.$valid)" layout="column" class="detailBody" novalidate>
								<md-content class="bottomButtonsBox">
									<md-input-container>
				                        <label>{{translate.load("sbi.scheduler.activity.events.event.name")}}:</label>
				                        <input ng-model="activityEvent.event.name" required maxlength="100" ng-maxlength="100" md-maxlength="100">
		                        	</md-input-container>
		                        	
									<md-input-container>
				                        <label>{{translate.load("sbi.scheduler.activity.events.event.description")}}:</label>
				                       <textarea ng-model="activityEvent.event.description" columns="1" maxlength="500" ng-maxlength="500" md-maxlength="500"></textarea>
		                        	</md-input-container>
		                        	
		                        	<md-input-container>
			                        	<label>{{translate.load("sbi.scheduler.activity.events.event.type")}}:</label> 
				                        <md-select ng-model="activityEvent.event.event_type" required>
									    	<md-option ng-repeat="eventType in jobDataCtrl.typeEvents" value="{{eventType.value}}">
									    		{{eventType.label}}
									    	</md-option>
									  	</md-select>
								  	</md-input-container>
									  	
									<div layout="row" class="checkboxRow">
				                        <label>{{translate.load("sbi.scheduler.activity.events.event.suspended")}}:</label>
				                        <md-checkbox ng-model="activityEvent.event.is_suspended">
		                        	</div>
		                        	
		                        	<div ng-if="activityEvent.event.event_type=='dataset'">
										<md-toolbar class="minihead">
											<div class="md-toolbar-tools">
												<h2 class="md-flex">{{translate.load("sbi.kpis.dataset")}}</h2>
											</div>
										</md-toolbar>
										
										<md-content layout-padding class="borderBox"> 
											<md-input-container>
												<label>{{translate.load("sbi.scheduler.activity.events.event.type.dataset")}}</label>
												<md-select ng-model="activityEvent.event.dataset" required>
													<md-option ng-repeat="item in jobDataCtrl.datasets "
														value="{{item.id.dsId}}">{{item.label}}</md-option> 
												</md-select> 
											</md-input-container> 
											<md-input-container>
												<label>{{translate.load("sbi.scheduler.activity.events.event.frequency")}}:</label>
												<input type="number"
													ng-model="activityEvent.event.frequency"> 
											</md-input-container> 
										</md-content>
									</div>
	
	                        	</md-content>
	
								<div layout="row" layout-align="end center" class=" bottomButtonsBox">
									<md-button type="button" class="md-raised"
										ng-click="activityEvent.resetForm()">Cancella</md-button>
									<div style="z-index: 1;">
										<md-button type="submit" class="md-raised md-primary"
											ng-disabled="!contactForm.$valid"> Salva </md-button>
										<md-tooltip md-direction="top" ng-if="!contactForm.$valid">
										completare i campi correttamente </md-tooltip>
									</div>
								</div>

							</form>
						</md-tab-body>
					</md-tab> 
					
					<md-tab id="eventTabDocuments"> 
						<md-tab-label>{{translate.load("sbi.scheduler.activity.events.documentsmanagement")}}</md-tab-label>
						<md-tab-body layout="column" >
							<div ng-include="'/athena/js/src/angular_1.4/scheduler/template/documentMenagementDetail.jsp'"></div>
						</md-tab-body> 
					</md-tab>
					
					
					<md-tab id="json">
					<md-tab-label>json</md-tab-label>
						<md-tab-body layout="column" >
						<pre>{{activityEvent.event | json}}</pre>
						</md-tab-body> 
					</md-tab>
				</md-tabs>
				
		</div>

		</div>

	</div>

</body>
</html>