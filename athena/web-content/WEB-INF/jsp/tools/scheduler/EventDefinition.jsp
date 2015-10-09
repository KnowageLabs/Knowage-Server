<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%@include file="/WEB-INF/jsp/tools/glossary/commons/headerInclude.jspf"%>
<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf"%>

<%
	String jobName = "default jobName value", jobGroup = "default jobGroup value", triggerName="", triggerGroup="";

	if (request.getParameter("JOB_NAME") != null && !request.getParameter("JOB_NAME").equals("")) {
		jobName = request.getParameter("JOB_NAME");
	}
	if (request.getParameter("JOB_GROUP") != null && !request.getParameter("JOB_GROUP").equals("")) {
		jobGroup = request.getParameter("JOB_GROUP");
	}
	if (request.getParameter("TRIGGER_NAME") != null && !request.getParameter("TRIGGER_NAME").equals("")) {
		triggerName = request.getParameter("TRIGGER_NAME");
	}
	if (request.getParameter("TRIGGER_GROUP") != null && !request.getParameter("TRIGGER_GROUP").equals("")) {
		triggerGroup = request.getParameter("TRIGGER_GROUP");
	}
%>

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
<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular.js"></script>
<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular-animate.min.js"></script>
<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular-aria.min.js"></script>

<!-- angular-material-->
<script type="text/javascript" src="/athena/js/lib/angular/angular-material_0.10.0/angular-material.js"></script>
<link rel="stylesheet" href="/athena/js/lib/angular/angular-material_0.10.0/angular-material.min.css">

<script type="text/javascript" src="/athena/js/src/angular_1.4/scheduler/EventDefinitionApp.js"></script>


<!-- 	queste sono per la angular list -->
<!-- angular tree -->
<link rel="stylesheet" href="/athena/js/lib/angular/angular-tree/angular-ui-tree.min.css">
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


<!-- 	angular time picker -->
<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js"></script>

<!-- fine -->

<link rel="stylesheet" type="text/css" href="/athena/themes/scheduler/css/scheduler_style.css">

<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/RestService.js"></script>
</head>

<body ng-app="EventDefinitionApp">
	<div layout-padding flex layout-fill class="h100 " 
		ng-controller="ActivityEventController as activityEventCtrl"
		ng-init="activityEventCtrl.initJobsValues('<%=jobName%>', '<%=jobGroup%>', '<%=triggerName%>', '<%=triggerGroup%>');">

		<form name="contactForm"  layout-fill ng-submit="contactForm.$valid && activityEventCtrl.saveEvent(contactForm.$valid)" class="detailBody md-whiteframe-z1" novalidate>
			<md-tabs flex class="h100 mini-tabs">
				<md-tab id="eventTabDetail" layout-padding flex>
					<md-tab-label>{{translate.load("sbi.generic.details")}}</md-tab-label>
					<md-tab-body class="md-padding " flex>
						
							<md-content class="h100" > 
								<md-input-container>
									<label>{{translate.load("sbi.scheduler.schedulation.events.event.name")}}:</label>
									<input ng-model="activityEventCtrl.event.triggerName" name={{translate.load("sbi.scheduler.schedulation.events.event.name")}} required maxlength="100" ng-maxlength="100" md-maxlength="100" ng-disabled="activityEventCtrl.disableName"> 
								</md-input-container>
								<md-input-container>
									<label>{{translate.load("sbi.scheduler.schedulation.events.event.description")}}:</label>
									<textarea ng-model="activityEventCtrl.event.triggerDescription" 
											columns="1" maxlength="500" ng-maxlength="500" md-maxlength="500"></textarea>
								</md-input-container>

								<div layout="row" class="checkboxRow">
									<label>{{translate.load("Start date")}}:</label>
									<md-datepicker ng-model="activityEventCtrl.event.startDate" name="Start date" required md-placeholder="StartDate"></md-datepicker>
									<label style="margin: 0 20px;">{{translate.load("Start time")}}:</label>
									<angular-time-picker id="myTimePicker" required ng-model="activityEventCtrl.event.startTime"></angular-time-picker>
								</div>

								<div layout="row" class="checkboxRow">
									<label style="margin-right: 5px;">{{translate.load("End date")}}:</label>
									<md-datepicker ng-model="activityEventCtrl.event.endDate" md-placeholder="EndDate"></md-datepicker>
									<label style="margin: 0 20px; margin-right: 26px;">{{translate.load("End time")}}: </label>
									<angular-time-picker id="myTimePicker" ng-model="activityEventCtrl.event.endTime"></angular-time-picker>
								</div>

								<md-toolbar class="unselectedItem" 
										ng-class="activityEventCtrl.typeOperation != 'single'? 'selectedItem' : 'unselectedItem'"
										style="height: 50px;  min-height: 30px;">
									<div class="md-toolbar-tools" layout="row" style="padding-left: 0px;">
										<md-input-container> 
											<label>{{translate.load("Tipo evento")}}</label>
											<md-select aria-label="aria-label" ng-model="activityEventCtrl.typeOperation"
													ng-change="activityEventCtrl.changeTypeOperation();"> 
												<md-option ng-repeat="type in activityEventCtrl.SCHEDULER_TYPES" 
														value="{{type.value}}">{{type.label}}</md-option> 
											</md-select> 
										</md-input-container>
									</div>
								</md-toolbar>
								
								<div ng-if="activityEventCtrl.eventSched.repetitionKind == 'event'"	layout-padding class="borderBox">
									<md-input-container> 
										<label>{{translate.load("sbi.scheduler.schedulation.events.event.type")}}:</label>
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.event_type"
												ng-change="activityEventCtrl.changeTypeFrequency()" required name={{translate.load("sbi.scheduler.schedulation.events.event.type")}}>
											<md-option ng-repeat="eventType in activityEventCtrl.EVENT_TYPES" 
													value="{{eventType.value}}"> {{eventType.label}} </md-option> 
										</md-select> 
									</md-input-container>

									<div ng-if="activityEventCtrl.eventSched.event_type=='dataset'">
										<md-toolbar class="minihead">
											<div class="md-toolbar-tools">
												<h2 class="md-flex">{{translate.load("sbi.kpis.dataset")}}</h2>
											</div>
										</md-toolbar>

										<md-content layout-padding class="borderBox"> 
											<md-input-container>
												<label>{{translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}}</label>
												<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.dataset"
														ng-change="activityEventCtrl.changeTypeFrequency()" required name={{translate.load("sbi.scheduler.schedulation.events.event.dataset")}}>
													<md-option ng-repeat="item in activityEventCtrl.datasets " 
															value="{{item.id.dsId}}">{{item.label}}</md-option> 
												</md-select> 
											</md-input-container> 
											<md-input-container>
												<label>{{translate.load("sbi.scheduler.schedulation.events.event.frequency")}}:</label>
												<input type="number" ng-change="activityEventCtrl.changeTypeFrequency()"
														ng-model="activityEventCtrl.eventSched.frequency"> 
											</md-input-container> 
										</md-content>
									</div>
								</div>

								<div ng-if="activityEventCtrl.shedulerType" layout-padding class="borderBox">
									<div layout="row" style="margin-bottom: 15px;">
										<span class="textspan">Frequenza</span>
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.repetitionKind"
												style="margin:0px" ng-init="activityEventCtrl.getActivityRepetitionKindForScheduler()"
												ng-change="activityEventCtrl.changeTypeFrequency();"> 
											<md-option ng-repeat="interval in activityEventCtrl.EVENT_INTERVALS " value="{{interval.value}}">{{interval.label}}</md-option> 
										</md-select>
									</div>

									<div ng-if="activityEventCtrl.eventSched.repetitionKind=='minute'" layout="row"
											ng-init="activityEventCtrl.eventSched.minute_repetition_n =activityEventCtrl.eventSched.minute_repetition_n || 1;">
										<span class="textspan">Every</span>
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.minute_repetition_n"
												ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect"> 
											<md-option ng-repeat="item in activityEventCtrl.getNitem(60) " value="{{item}}">{{item}}</md-option>
										</md-select>
										<span class="textspan">minutes</span>
									</div>
									
									<div ng-if="activityEventCtrl.eventSched.repetitionKind=='hour'" layout="row" 
											ng-init="activityEventCtrl.eventSched.hour_repetition_n = activityEventCtrl.eventSched.hour_repetition_n || 1;">
										<span class="textspan">Every</span>
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.hour_repetition_n"
												ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect"> 
											<md-option ng-repeat="item in activityEventCtrl.getNitem(24) " value="{{item}}">{{item}}</md-option>
										</md-select>
										<span class="textspan">hours</span>
									</div>

									<div ng-if="activityEventCtrl.eventSched.repetitionKind=='day'" layout="row" 
											ng-init="activityEventCtrl.eventSched.day_repetition_n = activityEventCtrl.eventSched.day_repetition_n || 1;">
										<span class="textspan">Every</span>
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.day_repetition_n"
												ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect">
											<md-option ng-repeat="item in activityEventCtrl.getNitem(31) " value="{{item}}">{{item}}</md-option>
										</md-select>
										<span class="textspan">days</span>
									</div>

									<div ng-if="activityEventCtrl.eventSched.repetitionKind == 'week'" layout="row" class="alignedCheckbox">
										<div layout="row" ng-repeat="week in activityEventCtrl.WEEKS">
											<label>{{week.label}}:</label>
											<md-checkbox aria-label="aria-label" ng-click="activityEventCtrl.toggleWeek(week.value)"
													ng-checked="activityEventCtrl.isChecked(week.value, activityEventCtrl.event.chrono.parameter.days, (activityEventCtrl.event.chrono.type == 'week'))">
											</md-checkbox>
										</div>
									</div>

									<div ng-if="activityEventCtrl.eventSched.repetitionKind=='month'" layout="row" flex>
										<div layout="column" layout-align="center center">
											<div layout="row" flex style="margin: 0 15px;">
												<span>complex</span>
												<md-switch style="margin: 0px 10px 17px 10px;" ng-change="activityEventCtrl.toggleMonthScheduler()"
														class="greenSwitch" aria-label="Switch " ng-model="activityEventCtrl.typeMonth"
														ng-init="activityEventCtrl.typeMonth=activityEventCtrl.typeMonth!=undefined? activityEventCtrl.typeMonth : true ;">
												</md-switch>
												<span>simple</span>
											</div>
											<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonth==true"
													ng-init="activityEventCtrl.monthrep_n =activityEventCtrl.monthrep_n || 1;">
												<span class="textspan">Every</span>
												<md-select aria-label="aria-label"
														ng-model="activityEventCtrl.monthrep_n" class="numberSelect"
														ng-change="activityEventCtrl.toggleMonthScheduler()">
													<md-option ng-repeat="item in activityEventCtrl.getNitem(12) " value="{{item}}">{{item}}</md-option> 
												</md-select>
												<span class="textspan">month</span>
											</div>

											<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonth!=true">
												<span class="textspan">In Month</span>
												<md-select aria-label="aria-label"
														ng-model="activityEventCtrl.month_repetition" style="margin:0px;" multiple='true'
														ng-change="activityEventCtrl.toggleMonthScheduler()">
													<md-option ng-repeat="month in activityEventCtrl.MONTHS " value="{{month.value}}">{{month.label}}</md-option> 
												</md-select>
											</div>
										</div>

										<div layout="column" layout-align="center center">

											<div layout="row" flex style="margin: 0 15px;">
												<span>complex</span>
												<md-switch style=" margin: 0px 10px 17px 10px;"
													ng-change="activityEventCtrl.toggleMonthScheduler()" class="greenSwitch"
														aria-label="Switch " ng-model="activityEventCtrl.typeMonthWeek"
														ng-init="activityEventCtrl.typeMonthWeek = activityEventCtrl.typeMonthWeek!=undefined? activityEventCtrl.typeMonthWeek : true">
												</md-switch>
												<span>simple</span>
											</div>

											<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonthWeek==true"
													ng-init="activityEventCtrl.dayinmonthrep_week = activityEventCtrl.dayinmonthrep_week || 1;">
												<span class="textspan">The day</span>
												<md-select aria-label="aria-label" ng-model="activityEventCtrl.dayinmonthrep_week"
														class="numberSelect" ng-change="activityEventCtrl.toggleMonthScheduler()">
													<md-option ng-repeat="item in activityEventCtrl.getNitem(31) " value="{{item}}">{{item}}</md-option> 
												</md-select>
											</div>

											<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonthWeek != true"
													ng-init="activityEventCtrl.month_week_number_repetition = activityEventCtrl.month_week_number_repetition|| '1';">
												<span class="textspan">The week</span>
												<md-select aria-label="aria-label" ng-model="activityEventCtrl.month_week_number_repetition"
														style="margin:0px;" ng-change="activityEventCtrl.toggleMonthScheduler()">
													<md-option ng-repeat="order in activityEventCtrl.WEEKS_ORDER" value="{{order.value}}">{{order.label}}</md-option> 
												</md-select>
												
												<span class="textspan">In day</span>
												<md-select aria-label="aria-label" ng-model="activityEventCtrl.month_week_repetition"
														style="margin:0px;" multiple='true'
														ng-change="activityEventCtrl.toggleMonthScheduler()">
													<md-option ng-repeat="week in activityEventCtrl.WEEKS " value="{{week.value}}">{{week.label}}</md-option>
												</md-select>
											</div>
										</div>
									</div>
								</div>
							</md-content>

							

					</md-tab-body>
				</md-tab> 
				<md-tab id="eventTabDocuments">
					<md-tab-label>{{translate.load("sbi.scheduler.schedulation.events.documentsmanagement")}}</md-tab-label>
					<md-tab-body layout="column" >
						<div class="h100" ng-include="'/athena/js/src/angular_1.4/scheduler/template/documentManagementDetail.jsp'"></div>
					</md-tab-body> 
				</md-tab>
				
				<md-tab id="json"> 
					<md-tab-label>json</md-tab-label> 
					<md-tab-body layout="column"> 
<!-- 						<pre>activityEventCtrl.event: {{activityEventCtrl.event | json}}</pre> -->
<pre>{{contactForm.$error | json }}</pre>
					</md-tab-body> 
				</md-tab>
			</md-tabs>
			
			<div layout="row"  class=" bottomButtonsBox">
				
			
<!-- 				<md-button type="button" class="md-raised" ng-click="activityEventCtrl.resetForm()">Cancella</md-button> -->
				
					 <div ng-if="!contactForm.$valid">
				        <md-icon md-font-icon="fa fa-plus"</md-icon>
				        <md-tooltip>
				        <ul>
						  <li ng-repeat="(key, errors) in contactForm.$error track by $index"> <strong>{{ key }}</strong> errors
						    <ul>
						      <li ng-repeat="e in errors">{{ e.$name }} has an error: <strong>{{ key }}</strong>.</li>
						    </ul>
						  </li>
						</ul>
					 </md-tooltip>
				      </div>
      
					<md-button type="submit" class="md-raised md-primary submButton"  ng-disabled="!contactForm.$valid">Salva</md-button>
					
					
				
			</div>
							
							
			</form>
	</div>
</body>
</html>