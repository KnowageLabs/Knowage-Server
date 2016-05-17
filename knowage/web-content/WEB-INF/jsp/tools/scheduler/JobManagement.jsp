<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="jobManager">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/scheduler/scheduler.css", currTheme)%>">

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/scheduler/jobManagement.js")%>"></script>

</head>
<body class="bodyStyle">
	<angular-list-detail ng-controller="Controller as ctrl" show-detail="ctrl.showDetail" id="angularListDetail">
		<list label='Scheduler' new-function="ctrl.addJob">
			<angular-table flex
				id='joblist' ng-model=ctrl.jobList
				columns='[{"label":translate.load("sbi.generic.name"),"name":"jobName"}]'
				columnsSearch='["jobName"]' show-search-bar=true
				selected-item="ctrl.selectedJob"
				highlights-selected-item=true
				click-function="ctrl.loadJob(item);"
				speed-menu-option=ctrl.menuJob>
			</angular-table>
		</list>
		<detail label='ctrl.getDetailTitle()'
			save-function="ctrl.saveJob"
			cancel-function="ctrl.closeDetail"
			disable-save-button="!jobDetailForm.$valid"
			show-save-button="ctrl.showDetail"
			show-cancel-button="ctrl.showDetail">
			
			<md-tabs class="mozScroll hideTabs h100" md-border-bottom flex>
				<md-tab label="{{translate.load('sbi.scheduler.overview')}}" ng-if="!ctrl.selectedJob.NEWJOB" md-on-select="ctrl.selectOverviewTab()" md-active="ctrl.isOverviewTabActive">
					<div layout-fill class="containerDiv">
						<div layout="column" layout-wrap layout-fill>
							<md-card>
								<md-toolbar class="md-blue minihead">
									<div class="md-toolbar-tools" layout="row" layout-wrap>
										<h2>{{translate.load("sbi.scheduler.documents");}}</h2>
									</div>
								</md-toolbar>
					 			<md-content layout="row" layout-wrap flex>
									<angular-table flex
										id='documentlist' ng-model=ctrl.selectedJob.documents
										columns='[{"label":translate.load("sbi.generic.name"),"name":"name","size":"200px"},
											{"label":translate.load("sbi.scheduler.parameters"),"name":"condensedParameters"}]'
										show-search-bar=false
										highlights-selected-item=false>
									</angular-table>
								</md-content>
								
								<md-toolbar class="md-blue minihead">
									<div class="md-toolbar-tools layout-wrap layout-row">
										<h2>{{translate.load("sbi.scheduler.schedulations");}}</h2>
										<md-button ng-click="ctrl.addTrigger()" class="md-fab" aria-label="Add trigger" style="position:absolute; right:11px; top:0px;">
											<md-icon md-font-icon="fa fa-plus" style="margin-top: 6px; color: white;"></md-icon> 
										</md-button>
									</div>
								</md-toolbar>
								
								<md-content layout="column" layout-wrap flex>
									<angular-table flex
										id='schedulelist' ng-model=ctrl.selectedJob.triggers
										columns='[{"label":translate.load("sbi.generic.name"),"name":"triggerName","size":"200px"},
											{"label":translate.load("sbi.scheduler.type"),"name":"triggerChronType"},
											{"label":translate.load("sbi.scheduler.startdate"),"name":"triggerStartDateTime"},
											{"label":translate.load("sbi.scheduler.enddate"),"name":"triggerEndDateTime"}]'
										show-search-bar=false
										highlights-selected-item=false
										speed-menu-option=ctrl.menuTrigger>
									</angular-table>
								</md-content>
							</md-card>
						</div>
					</div>
				</md-tab>
				<md-tab label="{{translate.load('sbi.generic.details')}}" md-on-select="ctrl.selectDetailTab()" md-active="!ctrl.isOverviewTabActive">
					<md-card>
						<md-card-content layout="column" style="padding: 0px;">
							<form name="jobDetailForm" class="wordForm" novalidate>
								<div>
									<div flex="100">
										<md-input-container class="small counter" ng-show="ctrl.selectedJob.NEWJOB">
											<label>{{translate.load("sbi.generic.name")}}</label>
											<input class="input_class" name="name" ng-model="ctrl.selectedJob.jobName" required maxlength="80" ng-maxlength="80" md-maxlength="80">
											<div ng-messages="jobDetailForm.name.$error">
												<div ng-message="required">{{translate.load("sbi.federationdefinition.required")}}</div>
									        </div>
										</md-input-container>
										<md-input-container class="small counter">
											<label>{{translate.load("sbi.generic.descr")}}</label>
											<input class="input_class" ng-model="ctrl.selectedJob.jobDescription" maxlength="120" ng-maxlength="120" md-maxlength="120">
										</md-input-container>
									</div>
								</div>
								<md-toolbar class="md-blue minihead" layout="row">
									<div class="md-toolbar-tools layout-wrap layout-row">
										<h2>{{translate.load("sbi.scheduler.documents");}}</h2>
										<div flex></div>
										<md-button ng-click="ctrl.addDocument()" class="md-fab" aria-label="Add document" style="top:0px;">
											<md-icon style="height: auto;" md-font-icon="fa fa-plus" style="margin-top: 6px; color: white;"></md-icon> 
										</md-button>
										<md-button ng-click="ctrl.deleteDocument()" class="md-icon-button actionButton" aria-label="Delete document" ng-show='ctrl.selectedDocumentIndex >= 0'>
											<md-icon style="height: auto;" md-font-icon="fa fa-trash" style="margin-top: 6px; color: white;"></md-icon> 
										</md-button>
										<md-button ng-click="ctrl.cloneDocument()" class="md-icon-button actionButton" aria-label="Clone document" ng-show='ctrl.selectedDocumentIndex >= 0'>
											<md-icon style="height: auto;" md-font-icon="fa fa-clone" style="margin-top: 6px; color: white;"></md-icon> 
										</md-button>
									</div>
								</md-toolbar>
								<md-tabs class="mozScroll hideTabs h100" md-selected="ctrl.selectedDocumentIndex" md-border-bottom md-dynamic-height flex>
									<md-tab ng-repeat="document in ctrl.selectedJob.documents track by $index" label="{{document.name}}">
										<md-list>
											<md-list-item ng-repeat="parameter in document.parameters | orderBy:'parameter.name'" layout="row" layout-align="start">
												<md-subheader flex="40">{{parameter.name}}</md-subheader>
												<md-content layout="column" flex>
													<!-- <span>{{parameter.value}}</span> -->
													<md-input-container class="small counter" ng-show="parameter.temporal">
														<label>{{translate.load("scheduler.parameterValuesStrategyQuestion","component_scheduler_messages")}}</label>
														<md-select aria-label="aria-label" ng-model="parameter.type" ng-change="ctrl.setDefaultValue(parameter)">
															<md-option ng-repeat="strategy in ctrl.triggerStrategies" value="{{strategy.value}}">{{strategy.label}}</md-option>
														</md-select>
													</md-input-container>
													
													<md-input-container class="small counter" ng-hide="parameter.temporal">
														<label>{{translate.load("scheduler.parameterValuesStrategyQuestion","component_scheduler_messages")}}</label>
														<md-select aria-label="aria-label" ng-model="parameter.type" ng-change="ctrl.setDefaultValue(parameter)">
															<md-option ng-repeat="strategy in ctrl.triggerStrategiesNoFormula" value="{{strategy.value}}">{{strategy.label}}</md-option>
														</md-select>
													</md-input-container>
													
													<md-input-container ng-show="parameter.type == 'fixed'">
														<label>{{translate.load("sbi.execution.roleselection.fieldlabel")}}</label>
														<md-select aria-label="aria-label" ng-model="parameter.role"> 
															<md-option ng-repeat="role in ctrl.selectedDocumentRoles" value="{{role.role}}">{{role.role}}</md-option>
														</md-select>
													</md-input-container>
													
													<md-input-container class="small counter" ng-show="parameter.type == 'fixed' && parameter.values.length > 0 && !parameter.manualInput">
														<label>{{translate.load("sbi.thresholds.values")}}</label>
														<md-select ng-model="parameter.selectedValues" ng-disabled="!parameter.role || parameter.role == ''" md-on-close="ctrl.saveParameterValues(parameter)" multiple>
															<md-option ng-value="value" ng-repeat="value in parameter.values track by $index">{{value}}</md-option>
														</md-select>
													</md-input-container>
													
													<md-input-container class="small counter" ng-show="parameter.type == 'fixed' && (parameter.values.length == 0 || parameter.manualInput)">
														<label>{{translate.load("sbi.thresholds.values")}}</label>
														<input class="input_class" ng-model="parameter.value">
													</md-input-container>
													
													<md-input-container ng-show="parameter.type == 'loadAtRuntime'">
														<label>{{translate.load("sbi.execution.roleselection.fieldlabel")}}</label>
														<md-select aria-label="aria-label" ng-model="parameter.value"> 
															<md-option ng-repeat="role in ctrl.selectedDocumentRoles" value="{{role.userAndRole}}">{{role.role}}</md-option>
														</md-select>
													</md-input-container>
													
													<md-input-container ng-show="parameter.type == 'formula'">
														<label>{{translate.load("scheduler.formulaName","component_scheduler_messages")}}</label>
														<md-select aria-label="aria-label" ng-model="parameter.value"> 
															<md-option ng-repeat="formula in ctrl.formulas" value="{{formula.name}}">{{formula.description}}</md-option>
														</md-select>
													</md-input-container>
													
													<md-input-container class="small counter">
														<md-select aria-label="aria-label" ng-model="parameter.iterative">
															<md-option ng-repeat="iteration in ctrl.triggerIterations" value="{{iteration.value}}">{{iteration.label}}</md-option>
														</md-select>
													</md-input-container>
												</md-content>
												<md-divider ng-if="!$last"></md-divider>
											</md-list-item>
										</md-list>
									</md-tab>
								</md-tabs>
							</form>
						</md-card-content>
					</md-card>
				</md-tab>
			<</md-tabs>
		</detail>
	</angular-list-detail>
</body>
</html>
