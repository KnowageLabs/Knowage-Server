<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="ModalitiesCheckModule">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- Styles -->
<link rel="stylesheet" type="text/css"	href="/knowage/themes/glossary/css/generalStyle.css">
<!-- Styles -->
<script type="text/javascript" src=" "></script>
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/catalogues/modalitiesCheck.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body class="bodyStyle" ng-controller="ModalitiesCheckController as ctrl" >

	<angular_2_col>
		<left-col>
			<div class="leftBox">
			<div style="height:40%;" >
				<md-toolbar class="md-blue minihead">
					<div class="md-toolbar-tools">
						<div>{{translate.load("sbi.modalities.check.title.configurable");}}</div>
						<md-button aria-label="create_button"
							class="md-fab md-ExtraMini addButton"
							style="position:absolute; right:11px; top:0px;"
							ng-click="createConstraints()" > 
							<md-icon
								md-font-icon="fa fa-plus" 
								style=" margin-top: 6px ; color: white;">
							</md-icon> 
						</md-button>
					</div>
				
						</md-toolbar>
						<md-content layout-padding style="background-color: rgb(236, 236, 236);" class="ToolbarBox miniToolbar noBorder leftListbox">
					<angular-table 
						layout-fill
						id="TestItemList_id"
						ng-model="ItemList"
						columns ='[
							{"label":"LABEL","name":"label","size":"30px"},
							{"label":"NAME","name":"name","size":"30px"},
							{"label":"DESCRIPTION","name":"description","size":"50px"},
							{"label":"CHECK TYPE","name":"CHECK_TYPE","size":"30px"}
							 ]'
						show-search-bar=true
						highlights-selected-item=true
						click-function="loadConstraints(item)"
						multi-select=true
						
							>					
						 					
					</angular-table>
				</md-content>
				</div>
				
				<div style="height:60%;">
				<md-toolbar class="md-blue minihead">
					<div class="md-toolbar-tools">
						<div>{{translate.load("sbi.modalities.check.title.predefined");}}</div>
						
					</div>
				
						</md-toolbar>
						<md-content layout-padding style="background-color: rgb(236, 236, 236);" class="ToolbarBox miniToolbar noBorder leftListbox">
					<angular-table 
						layout-fill
						id="predefined_id"
						ng-model="predefined"
						columns ='[
							{"label":"LABEL","name":"label","size":"30px"},
							{"label":"NAME","name":"name","size":"30px"},
							{"label":"DESCRIPTION","name":"description","size":"50px"},
							{"label":"CHECK TYPE","name":"CHECK_TYPE","size":"30px"}
							 ]'
							 
						show-search-bar = false
												
					>	 					
					</angular-table>
				</md-content>
				</div>
					</div>
					
				
					
		</left-col>
		<right-col>
		
		<form layout-fill ng-submit="saveConstraints()"
		class="detailBody md-whiteframe-z1">
		
			<div ng-show="showme">
				<md-toolbar class="md-blue minihead"> 
					<div class="md-toolbar-tools h100">
					<div style="text-align: center; font-size: 24px;">{{translate.load("sbi.modalities.check.title.details");}}</div>
					<div style="position: absolute; right: 0px" class="h100">
						<md-button type="button" tabindex="-1" aria-label="cancel"
							class="md-raised md-ExtraMini " style=" margin-top: 2px;"
							ng-click="cancel()">{{translate.load("sbi.browser.defaultRole.cancel");}}
						</md-button>
						<md-button  type="submit"
							aria-label="save_constraint" class="md-raised md-ExtraMini "
							style=" margin-top: 2px;">
							
						{{translate.load("sbi.browser.defaultRole.save")}} </md-button>
<!-- 						{{button_flag ? translate.load("sbi.browser.defaultRole.save") : translate.load("sbi.generic.update2")}} update button-->
					</div>
				</div>
				</md-toolbar>
				
				<md-content flex style="margin-left:20px;" class="ToolbarBox miniToolbar noBorder">
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.label")}}</label>
							<input ng-model="SelectedConstraint.LABEL" required 
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.name")}}</label>
							<input ng-model="SelectedConstraint.NAME" required 
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.description")}}</label>
							<input ng-model="SelectedConstraint.DESCRIPTION"  
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
					</div>
				
				<div layout="row" layout-wrap>
      				<div flex=100>
				       <md-input-container class="small counter"> 
				       <label>{{translate.load("sbi.modalities.check.details.check_type")}}</label>
				       <md-select  aria-label="aria-label" 
				        ng-model="SelectedConstraint.CHECK_TYPE"> <md-option 
				        ng-repeat="l in listType track by $index" ng-click="FieldsCheck(l)" >{{l.label}} </md-option>
				       </md-select> </md-input-container>
				   </div>
			</div>
     
     			<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{label}}</label>
							<input ng-model="selectedDataSource.Type" 
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
					</div>
					
				<div layout="row" layout-wrap ng-show ="additionalField">
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.modalities.check.details.rangeMax")}}</label>
							<input ng-model="selectedDataSource.RangeMax" 
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
					</div>	
					
				</md-content>
			</form>
		</right-col>
	</angular_2_col>
</body>
</html>