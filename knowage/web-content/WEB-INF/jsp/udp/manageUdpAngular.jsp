<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!doctype html>
<html ng-app="udpManagementApp">

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/udp/manageUdp.js"></script>

	<link rel="stylesheet" type="text/css" href="/knowage/themes/commons/css/generalStyle.css">
		
</head>

<body>
	<div ng-controller="Controller as ctrl">
		<div layout="row" layout-margin layout-fill layout-padding>
		  <div flex = "55">
		  	<div layout="column" class="left-part">
			  	<div class = "headContainer">
				  	<md-toolbar class="md-blue minihead">
					    <h1 class="md-toolbar-tools">
							{{translate.load("sbi.udp.udpManagement")}}
					    </h1>
					</md-toolbar>
				</div>
				<div>
					<div >
					    <md-button 	class="md-raised md-ExtraMini" ng-click="deleteRow()"> {{translate.load("sbi.generic.delete")}}</md-button>
					 </div>  
				    <md-content flex  layout-margin class="gridContainer">
					    <angular-table 
							id="table" ng-model="data" 
							columns='["label","name","type","family"]'
							columns-search='["label","name","type","family"]'
							highlights-selected-item = "true"
							show-search-bar="true"
							no-pagination="true"
							selected-item="itemSelected"
							click-function = "copyRowInForm(item,cell,listId)"
						></angular-table>
					</md-content>
				</div>
			 </div>
		  </div>
		  
		  <div flex = "5"></div>
		  
		  <div flex = "40">
		     <div layout="column">
			  	<md-toolbar class="md-blue minihead">
				    <h1 class="md-toolbar-tools">
				      Details {{translate.load("sbi.udp.udpManagement")}}
				    </h1>
				</md-toolbar>
			    <md-content flex layout-padding>
			    <form name="propertyForm" data-ng-submit="saveProperty()">
				    <div>
						<md-button type="submit" class="md-raised md-ExtraMini " ng-disabled="propertyForm.$invalid"> {{translate.load("sbi.generic.update2")}} </md-button>
						<md-button type="reset" class="md-raised md-ExtraMini" ng-click ="resetForm()"> {{translate.load("sbi.generic.search.clear")}} </md-button>
					</div>
					<div>
					<md-input-container>
	  					<label>{{translate.load("sbi.udp.label")}}</label>
	  					<input ng-model="property.label" required  type="text">
	  				</md-input-container>
	  				<md-input-container>	
	  					<label>{{translate.load("sbi.udp.name")}}</label>
	  					<input ng-model="property.name" required  type="text">
					</md-input-container>
					<md-input-container>	
	  					<label>{{translate.load("sbi.udp.description")}}</label>
	  					<textarea ng-model="property.description" md-maxlength="2500"></textarea>
					</md-input-container>
					<md-input-container>
				       <md-checkbox ng-model="property.multivalue" aria-label="Checkbox 1">
				         {{translate.load("sbi.udp.multivalue")}}
				      </md-checkbox>
				     </md-input-container>
				     <md-input-container>
				        <md-select placeholder="{{translate.load('sbi.udp.type')}}" ng-model="property.type" required>
						   <md-option value="Boolean">Boolean</md-option>
						   <md-option value="Text">Text</md-option>
						   <md-option value="Integer">Integer</md-option>
						</md-select>
	  				 </md-input-container>
 			     	 <md-input-container>
				        <md-select placeholder="{{translate.load('sbi.udp.family')}}" ng-model="property.family" required>
						   <md-option value="Model">Model</md-option>
						   <md-option value="Kpi">Kpi</md-option>
	  					</md-select>
  					 </md-input-container>
			     </form>
				</md-content>
			 </div>
		  </div>
		</div>
	</div>
</body>
</html>