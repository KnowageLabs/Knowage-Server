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
<link rel="stylesheet" type="text/css"	href="/knowage/themes/catalogue/css/catalogue.css">
<!-- Scripts -->
<script type="text/javascript" src=" "></script> 
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/catalogues/modalitiesCheck.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Constraints Management</title>
</head>
<body class="bodyStyle" ng-controller="ModalitiesCheckController as ctrl" >

	<angular_2_col>
		<left-col>
		<div class="leftBox">
	<md-toolbar class="header" >
					<div class="md-toolbar-tools" >
						<div style="font-size: 24px;">{{translate.load("sbi.modalities.check.title.constraints");}}</div>
						<md-button aria-label="create_button"
							class="md-fab md-ExtraMini addButton"
							style="position:absolute; right:11px; top:0px;"
							ng-click="createConstraints()"
							>
							 
							<md-icon
								md-font-icon="fa fa-plus" 
								style=" margin-top: 6px ; color: white;">
							</md-icon> 
						</md-button>
					</div>
				
						</md-toolbar>	
		
	<md-content style="display:inline !important;">
    <md-tabs md-dynamic-height md-selected="selectedTab" md-border-bottom="">
      <md-tab label='{{translate.load("sbi.modalities.check.title.configurable");}}' ng-click="clearRight(selectedTab)">
        <md-content layout-padding
			style="background-color: rgb(236, 236, 236);"
			class="ToolbarBox miniToolbar noBorder leftListbox">
        <angular-table 
						layout-fill
						id="TestItemList_id"
						ng-model="ItemList"
						columns ='[
							{"label":"LABEL","name":"label","size":"50px"},
							{"label":"NAME","name":"name","size":"50px"},
							{"label":"CHECK TYPE","name":"valueTypeCd","size":"85px"}
							 ]'
						show-search-bar=true
						highlights-selected-item=true
						click-function="loadConstraints(item)"
						speed-menu-option="ccSpeedMenu"
						
							>					
						 					
					</angular-table>  
          
        </md-content>
      </md-tab>
      <md-tab label='{{translate.load("sbi.modalities.check.title.predefined");}}' ng-click="clearRight(selectedTab)">
        <md-content layout-padding
			style="background-color: rgb(236, 236, 236);"
			class="ToolbarBox miniToolbar noBorder leftListbox">
         <angular-table 
						layout-fill
						id="predefined_id"
						ng-model="PredefinedList"
						columns ='[
							{"label":"LABEL","name":"label","size":"50px"},
							{"label":"NAME","name":"name","size":"50px"},
							{"label":"CHECK TYPE","name":"valueTypeCd","size":"85px"}
							 ]'
							 
						show-search-bar = false
						highlights-selected-item=true
						click-function="loadPredefined(item)"						
					>	 					
					</angular-table>
         
         
        </md-content>
      </md-tab>
    </md-tabs>
  </md-content>
  </div>
		</left-col>
		<right-col>
		<form name="attributeForm" layout-fill ng-submit="attributeForm.$valid && saveConstraints()"
		class="detailBody md-whiteframe-z1">
		
			<div ng-show="showme">
				<md-toolbar class="header"> 
					<div class="md-toolbar-tools h100">
					<div style="text-align: center; font-size: 24px;">{{translate.load("sbi.modalities.check.title.details");}}</div>
					<div style="position: absolute; right: 0px" class="h100">
						<md-button type="button" tabindex="-1" aria-label="cancel"
							class="md-raised md-ExtraMini rightHeaderButtonBackground" style=" margin-top: 2px;"
							ng-click="cancel()">{{translate.load("sbi.browser.defaultRole.cancel");}}
						</md-button>
						<md-button  type="submit"
							aria-label="save_constraint" class="md-raised md-ExtraMini rightHeaderButtonBackground"
							style=" margin-top: 2px;"
							ng-disabled="!attributeForm.$valid">
						{{translate.load("sbi.browser.defaultRole.save")}}
						</md-button>
					</div>
				</div>
				</md-toolbar>
				
				<md-content flex style="margin-left:20px;" class="ToolbarBox miniToolbar noBorder">
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.label")}}</label>
							<input name="lbl" ng-model="SelectedConstraint.label" ng-required="true"
							ng-maxlength="20" ng-change="setDirty()">
							
							<div  ng-messages="attributeForm.lbl.$error" ng-show="SelectedConstraint.label== null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>
							
							 </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.name")}}</label>
							<input name="name" ng-model="SelectedConstraint.name"  ng-required = "true"
						    ng-maxlength="40" ng-change="setDirty()">
						    
						    <div  ng-messages="attributeForm.name.$error" ng-show="SelectedConstraint.name== null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>
						    
						    
						     </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.description")}}</label>
							<input ng-model="SelectedConstraint.description"
					        ng-maxlength="160" ng-change="setDirty()"> </md-input-container>
						</div>
					</div>
				
				<div layout="row" layout-wrap>
      				<div flex=100>
				       <md-input-container class="small counter" > 
				       <label>{{translate.load("sbi.modalities.check.details.check_type")}}</label>
				       <md-select  aria-label="dropdown" placeholder ="Check Type"
				       	name ="dropdown" 
				        ng-required = "true"
				        ng-model="SelectedConstraint.valueTypeCd"
				        ng-change="changeType(SelectedConstraint.valueTypeCd)"
				        > <md-option 
				        ng-repeat="l in listType track by $index" value="{{l.VALUE_CD}}">{{l.VALUE_NM}} </md-option>
				       </md-select>
				       <div  ng-messages="attributeForm.dropdown.$error" ng-show="SelectedConstraint.valueTypeCd== null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>   
				        </md-input-container>
				   </div>
			</div>
     			<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{label}}</label>
							<input ng-model="SelectedConstraint.firstValue" 
						    ng-maxlength="160" ng-change="setDirty()"> </md-input-container>
						</div>
					</div>
					
				<div layout="row" layout-wrap ng-show ="additionalField">
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.modalities.check.details.rangeMax")}}</label>
							<input ng-model="SelectedConstraint.secondValue" 
						    ng-maxlength="160" ng-change="setDirty()"> </md-input-container>
						</div>
					</div>	

				</md-content>
				</div>
				
				<div ng-show="showpred">
				
				<md-toolbar class="header"> 
					<div class="md-toolbar-tools h100">
					<div style="text-align: center; font-size: 24px;">{{translate.load("sbi.modalities.check.title.details");}}</div>
					
				</div>
				</md-toolbar>
			
			<md-content flex style="margin-left:20px;" class="ToolbarBox miniToolbar noBorder">
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.label")}}</label>
							<input ng-model="PredefinedItem.label" ng-readonly="true">
							 </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.name")}}</label>
							<input ng-model="PredefinedItem.name"  ng-readonly="true">
						     </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.description")}}</label>
							<textarea ng-model="PredefinedItem.description" ng-readonly="true"></textarea>
					         </md-input-container>
						</div>
					</div>
				
				
					
				<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.modalities.check.details.check_type")}}</label>
							<input ng-model="PredefinedItem.valueTypeCd" ng-readonly="true">
						     </md-input-container>
						</div>
					</div>	
				</md-content>
			</div>
				
			</form>

		</right-col>
	</angular_2_col>
</body>
</html>