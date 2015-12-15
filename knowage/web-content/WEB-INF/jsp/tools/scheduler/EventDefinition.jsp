<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

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
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/scheduler/EventDefinitionApp.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/scheduler/css/scheduler_style.css">

</head>

<body ng-app="EventDefinitionApp">
	<div  flex layout-fill class="h100 " 
		ng-controller="ActivityEventController as activityEventCtrl"
		ng-init="activityEventCtrl.initJobsValues('<%=jobName%>', '<%=jobGroup%>', '<%=triggerName%>', '<%=triggerGroup%>');">

		<form name="contactForm"  layout-fill ng-submit="contactForm.$valid && activityEventCtrl.saveEvent(contactForm.$valid)" class="detailBody md-whiteframe-z1" novalidate>
			<md-tabs flex class="h100 mini-tabs">
				<md-tab id="eventTabDetail" layout-padding flex>
					<md-tab-label>{{translate.load("sbi.generic.details")}}</md-tab-label>
					<md-tab-body class="md-padding " flex>
						
						<md-content class="h100" > 
							<md-input-container>
								<label>{{translate.load("scheduler.schedname","component_scheduler_messages")}}:</label>
								<input ng-model="activityEventCtrl.event.triggerName" name={{translate.load("scheduler.schedname","component_scheduler_messages")}} required maxlength="100" ng-maxlength="100" md-maxlength="100" ng-disabled="activityEventCtrl.disableName"> 
							</md-input-container>
							<md-input-container>
								<label>{{translate.load("scheduler.scheddescription","component_scheduler_messages")}}:</label>
								<textarea ng-model="activityEventCtrl.event.triggerDescription" 
										columns="1" maxlength="500" ng-maxlength="500" md-maxlength="500"></textarea>
							</md-input-container>

							<div layout="row" class="checkboxRow">
								<label>{{translate.load("scheduler.startdate","component_scheduler_messages")}}:</label>
								<md-datepicker ng-model="activityEventCtrl.event.startDate" name={{translate.load("scheduler.startdate","component_scheduler_messages")}} required md-placeholder={{translate.load("scheduler.startdate","component_scheduler_messages")}}></md-datepicker>
								<label style="margin: 0 20px;">{{translate.load("scheduler.starttime","component_scheduler_messages")}}:</label>
								<angular-time-picker id="myTimePicker1" required ng-model="activityEventCtrl.event.startTime"></angular-time-picker>
							</div>

							<div layout="row" class="checkboxRow">
								<label style="margin-right: 5px;">{{translate.load("scheduler.enddate","component_scheduler_messages")}}:</label>
								<md-datepicker ng-model="activityEventCtrl.event.endDate" md-placeholder={{translate.load("scheduler.enddate","component_scheduler_messages")}}></md-datepicker>
								<label style="margin: 0 20px; margin-right: 26px;">{{translate.load("scheduler.endtime","component_scheduler_messages")}}: </label>
								<angular-time-picker id="myTimePicker2" ng-model="activityEventCtrl.event.endTime"></angular-time-picker>
							</div>

							<md-toolbar class="unselectedItem" 
									ng-class="activityEventCtrl.typeOperation != 'single'? 'selectedItem' : 'unselectedItem'"
									style="height: 50px;  min-height: 30px;">
								<div class="md-toolbar-tools" layout="row" style="padding-left: 0px;">
									<md-input-container> 
										<label>{{translate.load("sbi.generic.type")}}</label>
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
									<label>{{translate.load("scheduler.eventType","component_scheduler_messages")}}:</label>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.event_type"
											ng-change="activityEventCtrl.changeTypeFrequency()" required name={{translate.load("scheduler.repeatinterval","component_scheduler_messages")}}>
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
									<span class="textspan">{{translate.load("scheduler.repeatinterval","component_scheduler_messages")}}</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.repetitionKind"
											style="margin:0px" ng-init="activityEventCtrl.getActivityRepetitionKindForScheduler()"
											ng-change="activityEventCtrl.changeTypeFrequency();"> 
										<md-option ng-repeat="interval in activityEventCtrl.EVENT_INTERVALS " value="{{interval.value}}">{{interval.label}}</md-option> 
									</md-select>
								</div>

								<div ng-if="activityEventCtrl.eventSched.repetitionKind == 'minute'" layout="row"
										ng-init="activityEventCtrl.eventSched.minute_repetition_n =activityEventCtrl.eventSched.minute_repetition_n || 1;">
									<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.minute_repetition_n"
											ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect"> 
										<md-option ng-repeat="item in activityEventCtrl.getNitem(60) " value="{{item}}">{{item}}</md-option>
									</md-select>
									<span class="textspan">{{translate.load("sbi.kpis.mins")}}</span>
								</div>
								
								<div ng-if="activityEventCtrl.eventSched.repetitionKind == 'hour'" layout="row" 
										ng-init="activityEventCtrl.eventSched.hour_repetition_n = activityEventCtrl.eventSched.hour_repetition_n || 1;">
									<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.hour_repetition_n"
											ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect"> 
										<md-option ng-repeat="item in activityEventCtrl.getNitem(24) " value="{{item}}">{{item}}</md-option>
									</md-select>
									<span class="textspan">{{translate.load("sbi.kpis.hours")}}</span>
								</div>

								<div ng-if="activityEventCtrl.eventSched.repetitionKind == 'day'" layout="row" 
										ng-init="activityEventCtrl.eventSched.day_repetition_n = activityEventCtrl.eventSched.day_repetition_n || 1;">
									<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.day_repetition_n"
											ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect">
										<md-option ng-repeat="item in activityEventCtrl.getNitem(31) " value="{{item}}">{{item}}</md-option>
									</md-select>
									<span class="textspan">{{translate.load("sbi.kpis.days")}}</span>
								</div>

								<div ng-if="activityEventCtrl.eventSched.repetitionKind == 'week'" layout="row" class="alignedCheckbox">
									<div layout="row" ng-repeat="week in activityEventCtrl.WEEKS">
										<label>{{week.label}}:</label>
										<md-checkbox aria-label="aria-label" ng-click="activityEventCtrl.toggleWeek(week.value)"
												ng-checked="activityEventCtrl.isChecked(week.value, activityEventCtrl.event.chrono.parameter.days, (activityEventCtrl.event.chrono.type == 'week'))">
										</md-checkbox>
									</div>
								</div>

								<div ng-if="activityEventCtrl.eventSched.repetitionKind == 'month'" layout="row" flex>
									<div layout="column" layout-wrap layout-align="center center">
										<div layout="row"  style="margin: 0 15px;">
											<span>{{translate.load("sbi.generic.advanced")}}</span>
											<md-switch style="margin: 0px 10px 17px 10px;" ng-change="activityEventCtrl.toggleMonthScheduler()"
													class="greenSwitch" aria-label="Switch " ng-model="activityEventCtrl.typeMonth"
													ng-init="activityEventCtrl.typeMonth=activityEventCtrl.typeMonth!=undefined? activityEventCtrl.typeMonth : true ;">
											</md-switch>
											<span>{{translate.load("sbi.behavioural.lov.type.simple")}}</span>
										</div>
										<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonth==true"
												ng-init="activityEventCtrl.monthrep_n =activityEventCtrl.monthrep_n || 1;">
											<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
											<md-select aria-label="aria-label"
													ng-model="activityEventCtrl.monthrep_n" class="numberSelect"
													ng-change="activityEventCtrl.toggleMonthScheduler()">
												<md-option ng-repeat="item in activityEventCtrl.getNitem(12) " value="{{item}}">{{item}}</md-option> 
											</md-select>
											<span class="textspan">{{translate.load("sbi.kpis.months")}}</span>
										</div>

										<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonth!=true">
											<span class="textspan">{{translate.load("scheduler.generic.inMonth","component_scheduler_messages")}}</span>
											<md-select aria-label="aria-label"
													ng-model="activityEventCtrl.month_repetition" style="margin:0px;" multiple='true'
													ng-change="activityEventCtrl.toggleMonthScheduler()">
												<md-option ng-repeat="month in activityEventCtrl.MONTHS " value="{{month.value}}">{{month.label}}</md-option> 
											</md-select>
										</div>
									</div>

									<div layout="column" layout-wrap layout-align="center center">
										<div layout="row"  style="margin: 0 15px;">
											<span>{{translate.load("sbi.generic.advanced")}}</span>
											<md-switch style=" margin: 0px 10px 17px 10px;"
													ng-change="activityEventCtrl.toggleMonthScheduler()" class="greenSwitch"
													aria-label="Switch " ng-model="activityEventCtrl.typeMonthWeek"
													ng-init="activityEventCtrl.typeMonthWeek = activityEventCtrl.typeMonthWeek!=undefined? activityEventCtrl.typeMonthWeek : true">
											</md-switch>
											<span>{{translate.load("sbi.behavioural.lov.type.simple")}}</span>
										</div>

										<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonthWeek==true"
												ng-init="activityEventCtrl.dayinmonthrep_week = activityEventCtrl.dayinmonthrep_week || 1;">
											<span class="textspan">{{translate.load("scheduler.generic.theDay","component_scheduler_messages")}}</span>
											<md-select aria-label="aria-label" ng-model="activityEventCtrl.dayinmonthrep_week"
													class="numberSelect" ng-change="activityEventCtrl.toggleMonthScheduler()">
												<md-option ng-repeat="item in activityEventCtrl.getNitem(31) " value="{{item}}">{{item}}</md-option> 
											</md-select>
										</div>

										<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonthWeek != true"
												ng-init="activityEventCtrl.month_week_number_repetition = activityEventCtrl.month_week_number_repetition|| '1';">
											<span class="textspan">{{translate.load("scheduler.generic.theWeek","component_scheduler_messages")}}</span>
											<md-select aria-label="aria-label" ng-model="activityEventCtrl.month_week_number_repetition"
													style="margin:0px;" ng-change="activityEventCtrl.toggleMonthScheduler()">
												<md-option ng-repeat="order in activityEventCtrl.WEEKS_ORDER" value="{{order.value}}">{{order.label}}</md-option> 
											</md-select>
											
											<span class="textspan">{{translate.load("scheduler.generic.inDay","component_scheduler_messages")}}</span>
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
						<div class="h100" ng-include="'${pageContext.request.contextPath}/js/src/angular_1.4/scheduler/template/documentManagementDetail.jsp'"></div>
					</md-tab-body> 
				</md-tab>
				
				
			</md-tabs>

			
			<div layout="row"  class=" bottomButtonsBox">
				 <div ng-if="!contactForm.$valid">
				        <md-icon md-font-icon="fa fa-info-circle" style="    color:  #104D71;    line-height: 20px;"></md-icon>
				        <md-tooltip>
				        <ul style="padding: 0px;" >
						  <li style="display: block;" ng-repeat="(key, errors) in contactForm.$error track by $index"> 
						    <ul style="padding: 0px;">
						      <li style="display: block;" ng-repeat="e in errors">{{ e.$name }} <i class="fa fa-arrow-right"></i> <span style="color: red;    font-size: 12px;    font-weight: 900;">{{ key }}</span>.</li>
						    </ul>
						  </li>
						</ul>
					</md-tooltip>
				</div>

				<md-button type="submit" class="md-raised md-primary submButton"
					ng-disabled="!contactForm.$valid">{{translate.load("scheduler.save", "component_scheduler_messages")}}</md-button>
			</div>
		</form>
	</div>
</body>
</html>