<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="timespanManager">
<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<link rel="stylesheet" type="text/css" href="/athena/themes/timespan/css/timespanStyle.css">
	
	<!-- time picker -->
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js"></script>

	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/timespan/timespan.js"></script>
	
</head>


<body class="bodyStyle">

	<div ng-controller="Controller as ctrl" layout="row" layout-wrap layout-fill>
		
		<div flex="20" layout-fill class="leftBox">
			
			<md-toolbar class="md-blue minihead ">
					<div class="md-toolbar-tools">
						<div>{{translate.load("sbi.timespan");}}</div>
						<md-button ng-click="ctrl.newTs()" aria-label="new ts"
							class="md-fab md-ExtraMini addButton" 
							style="position:absolute; right:11px; top:0px;"> 
							<md-icon md-font-icon="fa fa-plus"
								style=" margin-top: 6px ; color: white;">
							</md-icon> 
						</md-button>
					</div>
					</md-toolbar>
					
					<md-content layout-padding class="ToolbarBox miniToolbar noBorder leftListbox">
						<angular-list layout-fill 
							id='ts' 
	                		ng-model=ctrl.tsList
	                		item-name='name'
	                		show-search-bar=true
	                		highlights-selected-item=true
	                		click-function="ctrl.loadTimespan(item)"
	                		menu-option=ctrl.menuTs
                		>
                		</angular-list>
					</md-content>
		
		</div>
		
		
		<md-tabs flex md-dynamic-height class="hideTabs h100" md-border-bottom > 
		
			<md-tab label="empty" style="margin:10px;" md-on-select="ctrl.activeTab='empty'" md-active="ctrl.activeTab=='empty'">
				<p style="padding: 10px;">{{translate.load("sbi.timespan.empty.message");}}</p>
			</md-tab>
			
			<md-tab label="details" style="margin:10px;" md-on-select="ctrl.activeTab='details'" md-active="ctrl.activeTab=='details'">
				
				<div layout="column" style=" padding: 10px; height: calc(100% - 20px); ">
				<!--  div flex layout-fill-->
		
					<md-toolbar class="md-blue minihead">
						<div class="md-toolbar-tools h100">
							<div style="text-align: center; font-size: 30px;"></div>
							<div style="position: absolute; right: 0px" class="h100">
								<md-button type="button" tabindex="-1" aria-label="cancel ts"
									class="md-raised md-ExtraMini " style=" margin-top: 2px;"
									ng-click="ctrl.cancel()">
									{{translate.load("sbi.browser.defaultRole.cancel");}} 
								</md-button>
								<md-button type="button" ng-click="ctrl.saveTimespan()" aria-label="save ts"
									class="md-raised md-ExtraMini " style=" margin-top: 2px;"
									ng-disabled="ctrl.selectedItem.name.length === 0 || ctrl.selectedItem.type.length === 0">
									{{translate.load("sbi.browser.defaultRole.save");}}
								</md-button>
							</div>
						</div>
					</md-toolbar>
		
					<md-content flex style="margin-left:20px;">
		
					<form name="tsForm" novalidate >
		
						<div layout="row" layout-wrap>
							<div flex="50">
								<md-input-container > <label>{{translate.load("sbi.generic.name");}}</label>
								<input maxlength="100" type="text" ng-model="ctrl.selectedItem.name"> </md-input-container>
							</div>
		
							<div flex="50">
								<md-input-container > <label>{{translate.load("sbi.generic.type");}}</label> 
								<md-select ng-model="ctrl.selectedItem.type" ng-change="ctrl.changeType()"> 
									<md-option ng-repeat="type in ctrl.tsType" value="{{type.value}}" >
										{{type.label}} 
									</md-option> 
								</md-select> 
								
								</md-input-container>
							</div>
						</div>
						
						<div layout="row" >
							<div flex="50">
								<md-input-container > <label>{{translate.load("sbi.generic.category");}}</label> 
								<md-select ng-model="ctrl.selectedItem.category"> 
									<md-option
										ng-repeat="cat in ctrl.tsCategory" value="{{cat.VALUE_ID}}">
										{{cat.VALUE_NM}} 
									</md-option> 
								</md-select> 
								
								</md-input-container>
							</div>
						</div>
						
						<md-divider ></md-divider>
						
						<div ng-if="ctrl.selectedItem.type" layout="row" layout-align="center center" >
							<table style="width: 70%; font-size: 10pt; table-layout: fixed; text-align: center;">
								<tr>
									<th>
										<label >{{translate.load("sbi.timespan.from");}}</label>
										<md-datepicker ng-if="ctrl.selectedItem.type=='temporal'" ng-model="ctrl.from" md-placeholder="Enter date" style="display: block;"></md-datepicker>
										<div ng-if="ctrl.selectedItem.type=='time'" style="width: 65px; margin: auto;">
											<angular-time-picker ng-model="ctrl.from" />
										</div>
									</th>
									<th>
										<label >{{translate.load("sbi.timespan.to");}}</label>
										<md-datepicker ng-if="ctrl.selectedItem.type=='temporal'" ng-model="ctrl.to" md-placeholder="Enter date" style="display: block;"></md-datepicker>
										<div ng-if="ctrl.selectedItem.type=='time'" style="width: 65px; margin: auto;">
											<angular-time-picker  ng-model="ctrl.to" />
										</div>
									</th>
									<th style="width:15%;">
										<md-button ng-click="ctrl.addInterval(ctrl.from,ctrl.to)" class="md-fab md-MiniList blue" aria-label="add interval"> 
											<md-icon md-font-icon="fa fa-plus" ></md-icon> 
										</md-button>
									</th>
									<th style="width:20%">
										<md-input-container ng-if="ctrl.selectedItem.type=='temporal'" style="padding-bottom:0px;">
											<label>{{translate.load("sbi.timespan.delay");}}</label>
											<input type="number" ng-model="ctrl.delay" >
										</md-input-container>
									</th>
								</tr>
								<tr ng-repeat="span in ctrl.selectedItem.definition">
									<td>{{span.from}}</td>
									<td>{{span.to}}</td>
									<td>
										<md-button ng-click="ctrl.removeInterval(span)" class="md-fab md-MiniList" aria-label="remove interval">
											<md-icon md-font-icon="fa fa-times"></md-icon>
										</md-button>
									</td>
								</tr>
							</table>
							
						</div>
						<div ng-if="ctrl.selectedItem.definition.length==0" class="divNoData">
							<label>{{translate.load("sbi.timespan.nointerval.message");}}</label>
						</div>
						<md-divider ></md-divider>
					</form>
					</md-content>
		
		
				</div>
			</md-tab>
		</md-tabs>
		
	</div>

</body>
</html>
