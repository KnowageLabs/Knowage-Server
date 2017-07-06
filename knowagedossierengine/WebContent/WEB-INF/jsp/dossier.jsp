<%@ page language="java"  contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>     
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<html ng-app="dossierModule">
<head>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/src/dossierController.js"></script>
<link rel="stylesheet" type="text/css"	href="<%=request.getContextPath()%>/css/customStyle.css">
<script>
var jsonTemplate = '<%= request.getAttribute("jsonTemplate") %>';
var documentId = '<%= request.getAttribute("documentId") %>';
</script>
</head>
<body ng-controller="dossierCTRL as ctrl">


<md-content class="ToolbarBox miniToolbar noBorder mozTable layout-fill">

	<md-card>	
		
		<md-toolbar class="secondaryToolbar" layout-padding>				
			<div class="md-toolbar-tools">
				<h2>
				  {{translate.load("sbi.generic.detail");}}
				</h2>
			</div>				
		</md-toolbar>
		<md-card-content>						         		
			<div layout="row" layout-align="start center">
				<md-input-container flex="80" class="md-block">
			    	<label>{{translate.load("sbi.dossier.activity.name");}}</label>
					<input ng-model="dossierActivity.activity" ng-required="true">
					<div  ng-messages="datasetForm.lbl.$error" ng-show="!selectedDataSet.name">
   						 	<div ng-message="required">{{translate.load("sbi.generic.reqired");}}</div>
 					</div>
				</md-input-container>
				<md-button style="height:40px" flex class="md-flat" ng-click="createNewActivity()" ng-disabled="!dossierActivity.activity">
				{{translate.load("sbi.dossier.activity.launch");}}
				</md-button>
			</div>	
		</md-card-content>											
	</md-card>
	
	<md-card layout="column">					
		<md-toolbar class="secondaryToolbar" layout-padding>				
			<div class="md-toolbar-tools">
				<h2>
				  <span>{{translate.load("sbi.dossier.activity.launched");}}</span>
				</h2>	
			</div>			
		</md-toolbar>						         			
			<angular-table
					id="dossierActivityTable"
					flex
					ng-model=activitiesForDocument
					columns='activitiesForDocumentColumns'
					show-search-bar=false
					speed-menu-option='activitySpeedMenu'
					no-pagination=false>
			</angular-table>							
	</md-card>
	
	
</md-content>

</body>
</html>
