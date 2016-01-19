<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="templateManagement">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- non c'entra	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/commons/LayerTree.js"></script> -->
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/tree-style.css">
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/generalStyle.css">


<link rel="stylesheet" type="text/css"	href="/knowage/themes/catalogue/css/catalogue.css">
<!-- controller -->
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/servermanager/templateManagement.js"></script>




</head>
<body class="bodyStyle">

	<div ng-controller="Controller " layout="column" layout-wrap layout-fill>

			<md-toolbar style="font:Roboto; padding:14px;background:#3b678c;font-size:18px;text:#ffffff" >
				
			<div class="md-toolbar-tools">
				<h2>{{translate.load("sbi.templatemanagemenent");}}</h2>
				
			</div>
			</md-toolbar>
			<md-content layout="column" layout-wrap>
			<md-input-container class="small counter"> 
				<p>
				<h4>{{translate.load("sbi.templatemanagemenent.firstmessage");}}</h4>

				</p>
			</md-input-container>
			<div layout="row" layout-wrap >
				<div flex= 10>
				<md-input-container class="small counter"> 
					<h4>{{translate.load("sbi.templatemanagemenent.choosedate");}}:</h4>
				</md-input-container>
				</div>
				<div flex= 20>
				  
					<md-datepicker   ng-model="dateSelected.data" name="Select Data" ng-change="parseDate()" md-placeholder={{translate.load("sbi.templatemanagemenent.selectdata");}} ></md-datepicker>
					
				</div>
				<div flex= 5 class="dialog-demo-content">
					<md-input-container class="small counter"> 
						<md-button ng-click="loadDocuments($event)" aria-label="load Documents"
							class="md-fab md-ExtraMini" style="margin-left: -66px; margin-top:14px;background:#153E7E" > <md-icon
							md-font-icon="fa fa-search" style=" margin-top: 6px ; color: white;" >
						</md-icon> </md-button>
					</md-input-container>
				</div>
					
			</div>
			<div layout="row" layout-wrap>
			<div flex=60 ng-show="documents.length!=0"><h4>{{translate.load("sbi.templatemanagemenent.secondmessage");}}</h4></div>
			<div flex= 5 class="dialog-demo-content" style="margin-top:-10px">
					<md-input-container class="small counter"> 
						<md-button ng-click="deleteTemplate($event)" aria-label="delete Templates"
							class="md-fab md-ExtraMini" style="margin-left: -66px; margin-top:14px;background:#153E7E" > <md-icon
							md-font-icon="fa fa-trash" style=" margin-top: 6px ; color: white;" >
						</md-icon> </md-button>
					</md-input-container>
				</div>
			</div>
			<div id="lista" style="background:#eceff1">
			<div layout="row" layout-wrap>
				<div ng-show="flagSelect">
				<md-checkbox  ng-show="flagSelect" ng-checked="flagCheck" ng-click="selectAll()">Select All</md-checkbox>
				</div>
			</div>
			<div layout="row" layout-wrap ng-repeat="doc in documents">
								
				<md-checkbox ng-checked="exists(doc, docChecked)"
					ng-click="toggle(doc, docChecked)"> {{ doc.name }} </md-checkbox>
		
			</div>
			</div>

			
	</md-content>
	
	</div>

</body>
</html>

