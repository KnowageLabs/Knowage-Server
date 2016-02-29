<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!doctype html>
<html ng-app="udpManagementApp">

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/udp/manageUdp.js"></script>

	<link rel="stylesheet" type="text/css"   href="<%=urlBuilder.getResourceLinkByTheme(request, "/themes/commons/css/customStyle.css", currTheme)%>">
		
</head>

<body>
	<div ng-controller="Controller as ctrl">
		<md-content layout="row" layout-fill>
		  <md-content flex = "45" layout-padding>
		  	<md-content layout="column">
			  	<md-toolbar class="md-knowage-theme">
				    <h1 class="md-toolbar-tools">
						{{translate.load("sbi.udp.udpManagement")}}
				    </h1>
				</md-toolbar>
				<md-content layout-padding>
				    <md-button 	class="md-raised md-ExtraMini" ng-click="deleteRow()"> {{translate.load("sbi.generic.delete")}}</md-button>
				 </md-content>  
			    <md-content flex="80">
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
			 </md-content>
		  </md-content>
		  
		  <md-content flex = "5" layout-padding></md-content>
		  
		  <md-content flex = "45" layout-padding>
		     <md-content layout="column">
			  	<md-toolbar class="md-knowage-theme">
				    <h1 class="md-toolbar-tools">
				      Details {{translate.load("sbi.udp.udpManagement")}}
				    </h1>
				</md-toolbar>
			    <md-content>
				    <form name="propertyForm" data-ng-submit="saveProperty()">
					    <md-content>
							<md-button type="submit" class="md-raised md-ExtraMini " ng-disabled="propertyForm.$invalid"> {{translate.load("sbi.generic.update2")}} </md-button>
							<md-button type="reset" class="md-raised md-ExtraMini" ng-click ="resetForm()"> {{translate.load("sbi.generic.search.clear")}} </md-button>
						</md-content>
						<md-content>
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
	  					 </md-content>
				     </form>
				</md-content>
			 </md-content>
		  </md-content>
		</md-content>
	</div>
</body>
</html>