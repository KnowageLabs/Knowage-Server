<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@include file="/WEB-INF/jsp/tools/glossary/commons/headerInclude.jspf"%>
<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

	<link rel="stylesheet" href="/athena/themes/glossary/css/font-awesome-4.3.0/css/font-awesome.min.css">
	
	<!-- angular reference-->
	<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular.js"></script>
	<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular-animate.min.js"></script>
	<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular-aria.min.js"></script>

	<!-- angular-material-->
	<link rel="stylesheet" href="/athena/js/lib/angular/angular-material_0.10.0/angular-material.min.css">
	<script type="text/javascript" src="/athena/js/lib/angular/angular-material_0.10.0/angular-material.js"></script>
	
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/RestService.js"></script>
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/federateddataset/federatedDataset.js"></script>
	<link rel="stylesheet" type="text/css" href="/athena/themes/federateddataset/css/federateddatasetStyle.css">
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/generalStyle.css">
	
	<!-- angular list -->
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/angular-list.css">
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/AngularList.js"></script>
	
	<!-- context menu -->
 	<script type="text/javascript" src="/athena/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>


 
 <!-- angular tree -->
 <link rel="stylesheet"  href="/athena/js/lib/angular/angular-tree/angular-ui-tree.min.css">
 <script type="text/javascript" src="/athena/js/lib/angular/angular-tree/angular-ui-tree.js"></script>
 <link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/tree-style.css">
 
 <!-- context menu -->
 <script type="text/javascript" src="/athena/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>
 
 <!--pagination-->
 <script type="text/javascript" src="/athena/js/lib/angular/pagination/dirPagination.js"></script>

	

</head>
<body class="bodyStyle" ng-app="MYAPPNIKOLA">

	<div ng-controller="MyCRTL" layout="column"
		style="width: 100%; height: 100%;"
		class="contentdemoBasicUsage">
		<md-toolbar class="miniheadfederation">
		<div class="md-toolbar-tools">
			<i class="fa fa-bar-chart fa-2x"></i>
			<h2 class="md-flex" style="padding-left: 14px">DATASET FEDERATION</h2>
		</div>
		</md-toolbar>

		<md-content layout-padding=""
			style="height: 90%; padding: 20px;"">
		<div ng-show="state" layout="row" layout-wrap>
			
			<div flex="49" margin-right: 20px">
				<md-toolbar class="miniheadfedsmall" style="border-bottom: 2px solid grey;" >
					<div class="md-toolbar-tools">
						<i class="fa fa-list-alt fa-2x"></i>
						<h2 class="md-flex" style="padding-left: 14px">AVALIABLE DATASETS</h2>
						<span flex=""></span>					
					</div>
				</md-toolbar>
			
				<div style="height:80%">
					<angular-list layout-fill="" 
					id="selectedDatasets" 
					ng-model="list" 
					item-name="label"
					show-search-bar=true
					speed-menu-option=glossSpeedMenuOptAD
					click-function="moveToListNew(item)"
					
					>					
					</angular-list>
				</div>
				
			</div>
			
			<div flex="49" style="margin-left: 20px;">
				<md-toolbar class="miniheadfedsmall" style="border-bottom: 2px solid grey;" >
					<div class="md-toolbar-tools">
						<i class="fa fa-list-alt fa-2x"></i>
						<h2 class="md-flex" style="padding-left: 14px">SELECTED DATASETS</h2>
						<span flex=""></span>					
					</div>
				</md-toolbar>
			
				<md-content style="height:80%">
					<angular-list layout-fill="" 
					id="availableDatasets" 
					ng-model="listaNew" 
					item-name="label" 
						
					speed-menu-option=glossSpeedMenuOpt
					
					>					
					</angular-list>
					
				</md-content>
				
			</div>
			
		</div>

		<div ng-hide="state">
			
			<md-toolbar class="miniheadfedsmall"
				style="">
			<div class="md-toolbar-tools">
				<h2 class="md-flex" style="padding-left: 14px">ASSOCIATIONS EDITOR</h2>
			</div>
			</md-toolbar>
			<md-content
				style=" padding: 5px;  height:41%">
			<div ng-repeat="dataset in listaNew">
				<div style="width: 250px; float: left; padding: 5px;">
					<md-toolbar class="miniheadfedsmall"
						style="">
					<div class="md-toolbar-tools">
						<h2 class="md-flex">{{dataset.label | uppercase}}</h2>
						
					</div>
					</md-toolbar>
					<div style=" height:85%;">
					<div ng-show="true">
						<angular-list
							layout-fill="" 
							id='{{dataset.label}}'
							ng-model="dataset.metadata.fieldsMeta" 
							item-name="name"
							highlights-selected-item=true
							selected-item="myselectedvariable[dataset.label]"
							click-function="selektuj(item, listId)"
							
						>
						</angular-list>
						
					</div>
					</div>
				</div>
			</div>
			</md-content>
		</div>

		<div ng-hide="state" style="padding-top: 5px">
			<md-toolbar class="miniheadfedsmall"
				style="">
			<div class="md-toolbar-tools">
				<h2 class="md-flex" style="padding-left: 14px">ASSOCIATIONS LIST</h2>
				<span flex=""></span><md-button class="md-fab md-ExtraMini createRelationButton"><md-icon class="fa fa-plus" style="position:absolute; left:0px; top:5px; right:5px; color:white"
					ng-click="napuniNiz();"></md-icon></md-button> 	
			</div>


			</md-toolbar>
			<md-content style=" height:40%">

			<div>
				
				<md-content >
				<div>
					<md-list>
						<div ng-repeat="k in multiArray">
							
							<md-list-item style="min-height:35px">
							
							<div ng-repeat="bla in k track by $index">
							
							<span ng-if="$index==0">
								{{bla.sourceTable.name | uppercase }}.{{bla.sourceColumns[0]}}</span>={{bla.destinationTable.name | uppercase }}.{{bla.destinationColumns[0]}}
							</div>
							<span flex=""></span> 
							<md-button aria-label="trash" class="md-fab md-ExtraMini trashcan-background">
										 	<i class="fa fa-trash" ng-click="deleteFromMultiArray(k)"></i>
										</md-button>
						</md-list-item>
						
						</div>
						
					</md-list>
				</div>
				
				</md-content>
			</md-content>
		</div>
	</div>

	<div ng-show="state">
		<md-button class="md-raised buttonR" aria-label="btn_next_step"
			style=" margin-top: 20px; float:right;" ng-click="toggle();">NEXT
		STEP</md-button>
	</div>
	<div ng-hide="state">
		<md-button class="md-raised buttonL" aria-label="btn_back_to_first_page"
			 ng-click="toggle()">BACK</md-button>
		<md-button class="md-raised buttonR" aria-label="btn_save_federation"
			
			ng-click="showAdvanced($event)">SAVE federation</md-button>

	</div>
	</md-content>
	</div>

</body>
</html>
