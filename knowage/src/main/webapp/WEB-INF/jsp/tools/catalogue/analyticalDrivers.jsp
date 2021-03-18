<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

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


<%@ page language="java" pageEncoding="UTF-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="AnalyticalDriversModule">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- Styles -->
<link rel="stylesheet" type="text/css"    href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/catalogues/analyticalDrivers.js")%>"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Analytical Drivers Management</title>
</head>
<body class="bodyStyle kn-analyticalDriver analyticalDriver" ng-controller="AnalyticalDriversController as ctrl" >
	<angular-list-detail show-detail="showme">
 		<list label='translate.load("sbi.analytical.drivers.title")' new-function="createDrivers"> 
			<angular-table flex id="adList_id" ng-model="adList" 
				columns='[
				  {"label":"Label","name":"label"},
				  {"label":"Name","name":"name"},
				  {"label":"Type","name":"type"}
				]'
				columns-search='["label","name"]' show-search-bar=true highlights-selected-item=true speed-menu-option="adSpeedMenu"click-function="loadDrivers(item)">
			</angular-table>
		</list>
		<extra-button>
			<md-button class="md-flat" ng-click="createUseModes()" ng-disabled="selectedTab == 0"ng-show="showme">New UseMode</md-button>
		</extra-button>
		
		<detail label='selectedDriver.label==undefined? "" : selectedDriver.label' save-function="save" cancel-function="cancel" 
			disable-save-button="!attributeForm.$valid" show-save-button="showme" show-cancel-button="showme">
			<form name="attributeForm" ng-submit="attributeForm.$valid && save()">
				<md-tabs md-dynamic-height md-selected="selectedTab" md-border-bottom="">
					<md-tab label='{{translate.load("sbi.analytical.drivers.details")}}'>
						<md-card>
							<md-content layout-padding>
								<div layout="row" layout-wrap>
									<div flex=100>
										<md-input-container class="md-block">
											<label>{{translate.load("sbi.ds.label")}}</label>
											<input name="lbl" ng-model="selectedDriver.label" ng-required="true" ng-maxlength="20" ng-change="setDirty()" ng-pattern="regex.extendedAlphanumeric">
											<div ng-messages="attributeForm.lbl.$error" role="alert" ng-messages-multiple>
												<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
												<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
												<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 20</div>
					  						</div>
										</md-input-container>
									</div>
								</div>
					
								<div layout="row" layout-wrap>
									<div flex=100>
										<md-input-container class="md-block">
											<label>{{translate.load("sbi.ds.name")}}</label>
											<input name="name" ng-model="selectedDriver.name"  ng-required = "true" ng-maxlength="40" ng-change="setDirty()" ng-pattern="regex.extendedAlphanumeric">
											<div ng-messages="attributeForm.name.$error" role="alert" ng-messages-multiple>
												<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
												<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.extendedAlphanumericRegex")}}</div>
												<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 40</div>
					  						</div>
									    </md-input-container>
									</div>
								</div>
					
								<div layout="row" layout-wrap>
									<div flex=100>
										<md-input-container class="md-block">
											<label>{{translate.load("sbi.ds.description")}}</label>
											<input name="description" ng-model="selectedDriver.description" ng-maxlength="160" ng-change="setDirty()" ng-pattern="regex.xss">
								        		<div ng-messages="attributeForm.description.$error" role="alert" ng-messages-multiple>
												<div ng-message="pattern">{{translate.load("sbi.config.manage.fields.validation.xssRegex")}}</div>
												<div ng-message="maxlength">{{translate.load("sbi.config.manage.fields.validation.maximumCharacters")}} 160</div>
					  						</div>
					  					</md-input-container>
									</div>
								</div>
					
								<label>{{translate.load("sbi.generic.type")}}</label>
								<div layout="row" layout-wrap>
									<div flex=100>
										<md-input-container class="small counter">
										    <md-radio-group ng-model="selectedDriver.type" layout="row">
										      <md-radio-button ng-repeat="l in listType  | filter: {VALUE_CD:'!DATE_RANGE'}" value="{{l.VALUE_CD}}" > {{l.VALUE_NM}} </md-radio-button>
											</md-radio-group>
									    </md-input-container>
									</div>
								</div>
							 	<div>
								   	<md-input-container class="small counter"> 
						        			<md-checkbox ng-change="setDirty()"  ng-model="selectedDriver.functional" aria-label="driver"></md-checkbox> 
							       	   	</md-input-container>
									<label>{{translate.load("sbi.analytical.drivers.functional")}}</label>
							   	</div>
			   					<div>
							    		<md-input-container class="small counter"> 
								        	<md-checkbox ng-change="setDirty()"  ng-model="selectedDriver.temporal" aria-label="driver"></md-checkbox> 
									</md-input-container>
								    	<label>{{translate.load("sbi.analytical.drivers.temporal")}}</label>
								</div>
							</md-content>
						</md-card>
			 		</md-tab> 
					<md-tab label='{{translate.load("sbi.analytical.drivers.usemode.details");}}' ng-if="showadMode"> 
						<md-card>
						 <md-content class="md-padding" layout="column">
						 	<p ng-show="useModeList.length == 0">There is no Use Modes defined for this Analytical Driver</p>
						 		<div layout="row" layout-align="start" ng-show="useModeList.length > 0">
						 			<md-button class="md-icon-button" hide show-gt-sm>
								  		<md-icon md-font-icon="fa fa-search"></md-icon>
									</md-button>
								    <md-input-container style="width:90% !important;" class="searchInput" >
										<input class="workspaceSearchInput" type="text" id="searchInput" ng-model="searchInput" 
											title="{{translate.load('sbi.workspace.mainmenu.search.tooltip')}}">
									</md-input-container>
							
									<md-button class="md-icon-button" title="{{translate.load('sbi.generic.search.clear')}}" aria-label="Clear" 
											ng-show=true ng-click="searchInput = ''" hide show-gt-sm>
										   <md-icon md-font-icon="fa fa-times"></md-icon>
									</md-button>
						 		</div>
						 		<div layout="column">
									<md-card class="functionsCard"  ng-repeat="useMode in useModeListTemp | filter:searchInput" ng-click="openUseModeDetails(useMode)">
										<md-card-content layout="row" layout-align="start center" class="noPadding">
											<div flex layout="row" layout-align="start center">
												<span flex="20" class="md-headline" layout-padding>{{useMode.name}}</span>
												<div layout="row">
													<span class="md-caption" style="color: rgb(76,76,76)" ng-repeat="role in useMode.associatedRoles | orderBy : 'name'"  ng-show="$index<3">&nbsp;{{role.name}}<span ng-if="!$last">,</span></span>
												</div>
											</div>
											<md-button style="float:right;"class="md-icon-button md-secondary" ng-click="confirmDelete(useMode,$event);$event.stopPropagation();" aria-label="delete">
												<md-icon md-font-icon="fa fa-trash-o"></md-icon>
											</md-button>
										</md-card-content>
					     				</md-card>
								</div>
							</md-content>		    
						</md-card> 
			 		</md-tab>
			 	</md-tabs> 
			</form>
		</detail>
	</angular-list-detail>
</body>
</html>
