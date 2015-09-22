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
		ng-init="jobDataCtrl.initJobsValues('<%=jobName%>', '<%=jobGroup%>', '<%=jobDescription%>')">
		
		<div layout-padding layout="row" flex layout-fill class="h100"  ng-controller="ActivityEventController as activityEvent">

			<%-- List of activity events --%>
			<div layout-padding layout-fill  layout="column" flex="20"	class="md-whiteframe-z1">
				<md-button class="md-raised" layout-align="center center" ng-click="activityEvent.createNewEvent()">
				{{translate.load("sbi.scheduler.activity.events.newevent")}}</md-button>
				
				<md-content class="h100"> 
					<angular-list layout-fill style="position: absolute; height: 100%;"
							id='eventList' 
	                		ng-model=jobDataCtrl.events
	                		item-name='name'
	                		click-function="activityEvent.setEvent(item)"
	                		highlights-selected-item=true
	                		show-search-bar=true
	                		selected-item=activityEvent.selectedEvent
	                		speed-menu-option=activityEvent.eventItemOpt
	                		>
					</angular-list>
				</md-content>
			</div>

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
				                        <input ng-model="activityEvent.editedEvent.name" required maxlength="100" ng-maxlength="100" md-maxlength="100">
		                        	</md-input-container>
		                        	
									<md-input-container>
				                        <label>{{translate.load("sbi.scheduler.activity.events.event.description")}}:</label>
				                       <textarea ng-model="activityEvent.editedEvent.description" columns="1" maxlength="500" ng-maxlength="500" md-maxlength="500"></textarea>
		                        	</md-input-container>
		                        	
		                        	<md-input-container>
			                        	<label>{{translate.load("sbi.scheduler.activity.events.event.type")}}:</label> 
				                        <md-select ng-model="activityEvent.editedEvent.event_type" required>
									    	<md-option ng-repeat="eventType in jobDataCtrl.typeEvents" value="{{eventType.value}}">
									    		{{eventType.label}}
									    	</md-option>
									  	</md-select>
								  	</md-input-container>
									  	
									<div layout="row" class="checkboxRow">
				                        <label>{{translate.load("sbi.scheduler.activity.events.event.suspended")}}:</label>
				                        <md-checkbox ng-model="activityEvent.editedEvent.is_suspended">
		                        	</div>
		                        	
		                        	<div ng-if="activityEvent.editedEvent.event_type=='dataset'">
										<md-toolbar class="minihead">
											<div class="md-toolbar-tools">
												<h2 class="md-flex">{{translate.load("sbi.kpis.dataset")}}</h2>
											</div>
										</md-toolbar>
										
										<md-content layout-padding class="borderBox"> 
											<md-input-container>
												<label>{{translate.load("sbi.scheduler.activity.events.event.type.dataset")}}</label>
												<md-select ng-model="activityEvent.editedEvent.dataset" required>
													<md-option ng-repeat="item in jobDataCtrl.datasets "
														value="{{item.id.dsId}}">{{item.label}}</md-option> 
												</md-select> 
											</md-input-container> 
											<md-input-container>
												<label>{{translate.load("sbi.scheduler.activity.events.event.frequency")}}:</label>
												<input type="number"
													ng-model="activityEvent.editedEvent.frequency"> 
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
						<md-tab-body layout="column">
<!-- 							<angular-list layout-fill style="position: absolute; height: 100%;" -->
<!-- 								id='documentList'  -->
<!-- 		                		ng-model='jobDataCtrl.documents' -->
<!-- 		                		item-name='label' -->
<!-- 		                		show-search-bar=true /> --> 


							
							<md-chips ng-model="jobDataCtrl.documents" readonly="true">
						      <md-chip-template>
						        <strong>{{$chip.label}}</strong>
						      </md-chip-template>
						    </md-chips>
						    
						   
							<md-toolbar class="minihead unselectedItem" ng-class="activityEvent.SaveAsSnapshot? 'selectedItem' : 'unselectedItem'">
								<div class="md-toolbar-tools" layout="row" >
								    <label>{{translate.load("SaveAsSnapshot")}}:</label>
				                    <md-checkbox ng-model="activityEvent.SaveAsSnapshot">
		                        </div>
							</md-toolbar>
							<div ng-if="activityEvent.SaveAsSnapshot">
								<md-content layout-padding class="borderBox"> 
										<md-input-container>
					                        <label>{{translate.load("sbi.scheduler.activity.events.event.name")}}:</label>
					                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
		                        		</md-input-container>
		                        		
		                        		<md-input-container>
					                        <label>{{translate.load("sbi.scheduler.activity.events.event.description")}}:</label>
					                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
		                        		</md-input-container>
		                        		
		                        		<md-input-container>
					                        <label>History Length:</label>
					                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
		                        		</md-input-container>
								</md-content>
							</div>
							
							<md-toolbar class="minihead unselectedItem" ng-class="activityEvent.SaveAsFile? 'selectedItem' : 'unselectedItem'">
								<div class="md-toolbar-tools" layout="row" >
								    <label>{{translate.load("SaveAsFile")}}:</label>
				                    <md-checkbox ng-model="activityEvent.SaveAsFile">
		                        </div>
							</md-toolbar>
							<div ng-if="activityEvent.SaveAsFile">
								<md-content layout-padding class="borderBox"> 
									<md-input-container>
				                        <label>{{translate.load("File Name")}}:</label>
				                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
	                        		</md-input-container>
	                        		
	                        		<md-input-container>
				                        <label>{{translate.load("Folder Name")}}:</label>
				                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
	                        		</md-input-container>	
	                        		
	                        		 <div  layout="row" class="checkboxRow" >
									    <label>{{translate.load("Saved zipped file")}}:</label>
					                    <md-checkbox ng-model="activityEvent.Savedzippedfile">
		                       		 </div>	
		                       		
		                       		<md-input-container ng-if="activityEvent.Savedzippedfile==true">
				                        <label>{{translate.load("Folder Name")}}:</label>
				                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
	                        		</md-input-container>								
			

								</md-content>
							</div>
							
							<md-toolbar class="minihead unselectedItem" ng-class="activityEvent.SaveAsDocument? 'selectedItem' : 'unselectedItem'">
								<div class="md-toolbar-tools" layout="row" >
								    <label>{{translate.load("SaveAsDocument")}}:</label>
				                    <md-checkbox ng-model="activityEvent.SaveAsDocument">
		                        </div>
							</md-toolbar>
							<div ng-if="activityEvent.SaveAsDocument">
								<md-content layout-padding class="borderBox"> 
									<md-input-container>
				                        <label>{{translate.load("sbi.scheduler.activity.events.event.name")}}:</label>
				                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
	                        		</md-input-container>
	                        		
	                        		<md-input-container>
				                        <label>{{translate.load("sbi.scheduler.activity.events.event.description")}}:</label>
				                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
	                        		</md-input-container>
	                        		
	                        		 <div  layout="row" class="checkboxRow" >
									    <label>{{translate.load("fixed folder")}}:</label>
					                    <md-checkbox ng-model="activityEvent.fixedFolder">
		                       		 </div>	
		                       		 
		                       		 alberello
		                       		 
		                       		<div  layout="row" class="checkboxRow" >
									    <label>{{translate.load("folder from dataset")}}:</label>
					                    <md-checkbox ng-model="activityEvent.datasetFolder">
		                       		 </div>	
		                       		
		                       		<md-input-container  class="subCheckboxRowElement"  ng-if="activityEvent.datasetFolder==true">
										<label>{{translate.load("sbi.scheduler.activity.events.event.type.dataset")}}</label>
										<md-select ng-model="" >
											<md-option ng-repeat="item in jobDataCtrl.datasets "
												value="{{item.id.dsId}}">{{item.label}}</md-option> 
										</md-select> 
									</md-input-container> 
									
									<md-input-container  class="subCheckboxRowElement"  ng-if="activityEvent.datasetFolder==true">
										<label>{{translate.load("Driver")}}</label>
										<md-select ng-model="" >
											<md-option value="driver1">Driver1</md-option> 
											<md-option value="driver2">Driver2</md-option> 
										</md-select> 
									</md-input-container> 
		                       		 
								</md-content>
							</div>
							
							<md-toolbar class="minihead unselectedItem" ng-class="activityEvent.SendToJavaClass? 'selectedItem' : 'unselectedItem'">
								<div class="md-toolbar-tools" layout="row" >
								    <label>{{translate.load("SendToJavaClass")}}:</label>
				                    <md-checkbox ng-model="activityEvent.SendToJavaClass">
		                        </div>
							</md-toolbar>
							<div ng-if="activityEvent.SendToJavaClass">
								<md-content layout-padding class="borderBox"> 
									<md-input-container>
					                        <label>{{translate.load("class path")}}:</label>
					                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
		                        		</md-input-container>
								</md-content>
							</div>
							
							<md-toolbar class="minihead unselectedItem" ng-class="activityEvent.SendEmail? 'selectedItem' : 'unselectedItem'">
								<div class="md-toolbar-tools" layout="row" >
								    <label>{{translate.load("SendEmail")}}:</label>
				                    <md-checkbox ng-model="activityEvent.SendEmail">
		                        </div>
							</md-toolbar>
							<div ng-if="activityEvent.SendEmail">
								<md-content layout-padding class="borderBox"> 
								
									
									 <div  layout="row" class="checkboxRow" >
									    <label>{{translate.load("send unique mailfor all scheduler")}}:</label>
					                    <md-checkbox ng-model="activityEvent.uniqueMail">
		                       		 </div>	
		                       		 
		                       		  <div  layout="row" class="checkboxRow" >
									    <label>{{translate.load("send zipped file")}}:</label>
					                    <md-checkbox ng-model="activityEvent.Sendzippedfile">
		                       		 </div>	
		                       		
		                       		<md-input-container   class="subCheckboxRowElement"  ng-if="activityEvent.Sendzippedfile==true">
				                        <label>{{translate.load("zipped file name")}}:</label>
				                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
	                        		</md-input-container>		
		                       		
		                       		 <div  layout="row" class="checkboxRow" >
									    <label>{{translate.load("fixed list of recipients")}}:</label>
					                    <md-checkbox ng-model="activityEvent.fixedListOfRecipients">
		                       		 </div>	
		                       		
		                       		<md-input-container  class="subCheckboxRowElement"  ng-if="activityEvent.fixedListOfRecipients==true">
				                        <label>{{translate.load("Mail to")}}:</label>
				                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
	                        		</md-input-container>		
		                       			
		                       		 <div  layout="row" class="checkboxRow" >
									    <label>{{translate.load("Use a Datasetas recipient's list")}}:</label>
					                    <md-checkbox ng-model="activityEvent.datasetRecipients">
		                       		 </div>	
		                       		
		                       		<md-input-container  class="subCheckboxRowElement"  ng-if="activityEvent.datasetRecipients==true">
										<label>{{translate.load("sbi.scheduler.activity.events.event.type.dataset")}}</label>
										<md-select ng-model="" >
											<md-option ng-repeat="item in jobDataCtrl.datasets "
												value="{{item.id.dsId}}">{{item.label}}</md-option> 
										</md-select> 
									</md-input-container> 
									
									<md-input-container  class="subCheckboxRowElement"  ng-if="activityEvent.datasetRecipients==true">
										<label>{{translate.load("Parameter")}}</label>
										<md-select ng-model="" >
											<md-option value="param1">param1</md-option> 
											<md-option value="param2">param2</md-option> 
										</md-select> 
									</md-input-container> 
											
									 <div  layout="row" class="checkboxRow" >
									    <label>{{translate.load("Use an expression ")}}:</label>
					                    <md-checkbox ng-model="activityEvent.useExpression">
		                       		 </div>	
		                       		
		                       		<md-input-container class="subCheckboxRowElement" ng-if="activityEvent.useExpression==true">
				                        <label>{{translate.load("Expression")}}:</label>
				                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
	                        		</md-input-container>			
								
									<div  layout="row" class="checkboxRow" >
									    <label>{{translate.load("Include report name ")}}:</label>
					                    <md-checkbox ng-model="activityEvent.includeReportName">
		                       		 </div>	
								
									<md-input-container >
				                        <label>{{translate.load("Mail subject")}}:</label>
				                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
	                        		</md-input-container>	
	                        		
	                        		<md-input-container >
				                        <label>{{translate.load("File name")}}:</label>
				                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
	                        		</md-input-container>	
	                        		
	                        		<md-input-container >
				                        <label>{{translate.load("Mail text")}}:</label>
				                        <input ng-model=""  maxlength="100" ng-maxlength="100" md-maxlength="100">
	                        		</md-input-container>	
								
								
								</md-content>
							</div>
							
							<md-toolbar class="minihead unselectedItem" ng-class="activityEvent.SendToDistributionList? 'selectedItem' : 'unselectedItem'">
								<div class="md-toolbar-tools" layout="row" >
								    <label>{{translate.load("SendToDistributionList")}}:</label>
				                    <md-checkbox ng-model="activityEvent.SendToDistributionList">
		                        </div>
							</md-toolbar>

							
    
    
    
    						
							
							
						</md-tab-body> 
					</md-tab>
					
				</md-tabs>
		</div>

		</div>

	</div>

</body>
</html>