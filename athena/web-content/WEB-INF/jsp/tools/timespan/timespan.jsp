<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/tools/glossary/commons/headerInclude.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="timespanManager">

<head>
	
	<meta http-equiv="x-ua-compatible" content="IE=EmulateIE9" >
	
	<link rel="stylesheet" href="/athena/themes/glossary/css/font-awesome-4.3.0/css/font-awesome.min.css">
	
	<!-- angular reference-->
	<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular.js"></script>
	<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular-animate.min.js"></script>
	<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular-aria.min.js"></script>
	
	
	<!-- angular-material-->
	<link rel="stylesheet" href="/athena/js/lib/angular/angular-material_0.10.0/angular-material.min.css">
	<script type="text/javascript" src="/athena/js/lib/angular/angular-material_0.10.0/angular-material.js"></script>
	
	<!-- angular tree -->
	<link rel="stylesheet" 	href="/athena/js/lib/angular/angular-tree/angular-ui-tree.min.css">
	<script type="text/javascript" src="/athena/js/lib/angular/angular-tree/angular-ui-tree.js"></script>
	
	<!-- angular list -->
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/angular-list.css">
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/AngularList.js"></script>
	
	<!-- time picker -->
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js"></script>
	
	<!-- context menu -->
	<script type="text/javascript" src="/athena/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>
	
	<!--pagination-->
	<script type="text/javascript" src="/athena/js/lib/angular/pagination/dirPagination.js"></script>
	
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/generalStyle.css">
	

	<!-- glossary tree -->
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/tree-style.css">
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/glossary/commons/GlossaryTree.js"></script>
	
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/RestService.js"></script>
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
		
		
		
		<div flex layout-fill>

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
								ng-repeat="cat in ctrl.tsCategory" value="{{cat.value}}">
								{{cat.label}} 
							</md-option> 
						</md-select> 
						
						</md-input-container>
					</div>
				</div>
				
				<md-divider style="margin:20px;"></md-divider>
				
				<div layout="row" layout-align="center center" >
					<table style="width: 70%; font-size: 10pt; margin-top: 20px; table-layout: fixed; text-align: center;">
						<tr>
							<th>
								<span style="display: block;">{{translate.load("sbi.timespan.from");}}</span>
								<md-datepicker layout-align="center center" ng-if="ctrl.selectedItem.type=='temporal'" ng-model="ctrl.from" md-placeholder="Enter date" style="display: block;"></md-datepicker>
								<angular-time-picker ng-if="ctrl.selectedItem.type=='time'" ng-model="ctrl.from" style="display: block; padding-left: 85px;"></angular-time-picker>
							</th>
							<th>
								<span style="display: block; ">{{translate.load("sbi.timespan.to");}}</span>
								<md-datepicker ng-if="ctrl.selectedItem.type=='temporal'" ng-model="ctrl.to" md-placeholder="Enter date" style="display: block; "></md-datepicker>
								<angular-time-picker ng-if="ctrl.selectedItem.type=='time'" ng-model="ctrl.to" style="display: block; padding-left: 85px;"></angular-time-picker>
							</th>
							<th>
								<md-button ng-click="ctrl.addInterval(ctrl.from, ctrl.to)" class="md-fab md-MiniList blue" aria-label="add interval"> 
									<md-icon md-font-icon="fa fa-plus" ></md-icon> 
								</md-button>
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
				
			</form>
			</md-content>


		</div>
		
	</div>

</body>
</html>
