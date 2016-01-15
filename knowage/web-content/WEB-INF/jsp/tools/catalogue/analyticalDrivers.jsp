<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="AnalyticalDriversModule">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- Styles -->
<link rel="stylesheet" type="text/css"	href="/knowage/themes/glossary/css/generalStyle.css">
<link rel="stylesheet" type="text/css"	href="/knowage/themes/catalogue/css/catalogue.css">
<!-- Styles -->
<script type="text/javascript" src=" "></script>
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/catalogues/analyticalDrivers.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Analytical Drivers Management</title>
</head>
<body class="bodyStyle" ng-controller="AnalyticalDriversController as ctrl" >

	<angular_2_col>
		<left-col>
		<div style="height:100%;">
	<div class="leftBox" style="height:50%;">
		<md-toolbar class="header">
		<div class="md-toolbar-tools">
			<div style="font-size: 24px;">{{translate.load("sbi.analytical.drivers.title");}}</div>

			<md-button class="md-fab md-ExtraMini addButton" aria-label="create"
				style="position:absolute; right:11px; top:0px;"
				ng-click="createDrivers()"> <md-icon
				md-font-icon="fa fa-plus" style=" margin-top: 6px ; color: white;">
			</md-icon> </md-button>
		</div>
		</md-toolbar>
		<md-content layout-padding
			style="background-color: rgb(236, 236, 236);"
			class="ToolbarBox miniToolbar noBorder leftListbox"> <angular-table
			layout-fill id="adList_id" ng-model="adList"
			columns='[
						         {"label":"Label","name":"label"},
						         {"label":"Name","name":"name"},
						         {"label":"Type","name":"type"}
						         ]'
			show-search-bar=true highlights-selected-item=true
			speed-menu-option="adSpeedMenu" click-function="loadDrivers(item)">
		</angular-table>
		 </md-content>
	</div>
	
	<div class="leftBox" style="height:50%;">
		<md-toolbar class="header">
		<div class="md-toolbar-tools">
			<div style="font-size: 24px;">{{translate.load("sbi.analytical.drivers.usemode.title");}}</div>

			<md-button class="md-fab md-ExtraMini addButton" aria-label="create"
				style="position:absolute; right:11px; top:0px;"
				ng-click="createUseModes()"> <md-icon
				md-font-icon="fa fa-plus" style=" margin-top: 6px ; color: white;">
			</md-icon> </md-button>
		</div>
		</md-toolbar>
		<md-content layout-padding
			style="background-color: rgb(236, 236, 236);"
			class="ToolbarBox miniToolbar noBorder leftListbox">
			 <angular-table
								layout-fill id="useModeList_id" ng-model="useModeList"
								columns='[
											{"label":"Name","name":"name"}
										 ]'
								highlights-selected-item=true
								speed-menu-option="dumSpeedMenu" click-function="loadUseMode(item)">
			</angular-table>
		 </md-content>
	</div>
	</div>
		</left-col>
		<right-col>
		<form name="attributeForm" layout-fill ng-submit="attributeForm.$valid && saveDrivers()"
		class="detailBody md-whiteframe-z1">
		<div ng-show="showme">
				<md-toolbar class="header"> 
					<div class="md-toolbar-tools h100">
					<div style="text-align: center; font-size: 24px;">{{translate.load("sbi.analytical.drivers.details");}}</div>
					<div style="position: absolute; right: 0px" class="h100">
						<md-button type="button" tabindex="-1" aria-label="cancel"
							class="md-raised md-ExtraMini rightHeaderButtonBackground" style=" margin-top: 2px;"
							ng-click="cancel()">{{translate.load("sbi.browser.defaultRole.cancel");}}
						</md-button>
						<md-button  type="submit"
							aria-label="save_constraint" class="md-raised md-ExtraMini rightHeaderButtonBackground"
							style=" margin-top: 2px;"
							ng-disabled="!attributeForm.$valid"
							>
						{{translate.load("sbi.browser.defaultRole.save")}}
						</md-button>
					</div>
				</div>
				</md-toolbar>
				<md-content flex style="margin-left:20px;"
				class="ToolbarBox miniToolbar noBorder"> 
			<md-tabs md-dynamic-height md-selected="selectedTab" md-border-bottom="">
			<md-tab label='{{translate.load("sbi.analytical.drivers.details");}}'>
			<md-content flex style="margin-left:20px; overflow:hidden"
				class="md-padding ToolbarBox noBorder">

			<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.label")}}</label>
							<input name="lbl" ng-model="selectedDriver.label" ng-required="true"
							ng-maxlength="20" ng-change="setDirty()">
							
							<div  ng-messages="attributeForm.lbl.$error" ng-show="selectedDriver.label== null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>
							
							 </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.name")}}</label>
							<input name="name" ng-model="selectedDriver.name"  ng-required = "true"
						    ng-maxlength="40" ng-change="setDirty()">
						    
						    <div  ng-messages="attributeForm.name.$error" ng-show="selectedDriver.name== null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>
						    
						    
						     </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.description")}}</label>
							<input ng-model="selectedDriver.description"
					        ng-maxlength="160" ng-change="setDirty()"> </md-input-container>
						</div>
					</div>
					
					<label>{{translate.load("sbi.generic.type")}}</label>
					<div layout="row" layout-wrap>
						<div flex=100>
						
							<md-input-container class="small counter">
							
							    <md-radio-group ng-model="selectedDriver.type" layout="row">
							    
							      <md-radio-button ng-repeat="l in listType track by $index" value="{{l.VALUE_CD}}"> {{l.VALUE_NM}} </md-radio-button>

    							</md-radio-group>
						    
						     </md-input-container>
						</div>
					</div>
				
				 <div layout="row" layout-wrap>
			   <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedDriver.functional" aria-label="driver">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.analytical.drivers.functional")}}</label>
			       </div>
			   </div>
			   <div layout="row" layout-wrap>
			    <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedDriver.temporal" aria-label="driver">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.analytical.drivers.temporal")}}</label>
			       </div>
			   </div>
			
			</md-content> </md-tab> 
			<md-tab label='{{translate.load("sbi.analytical.drivers.usemode.details");}}' ng-if="showadMode"> <md-content
				flex style="margin-left:20px;"
				class="md-padding ToolbarBox noBorder"> 
				
					<div layout="row" layout-wrap>
						<div flex=100>
							 <md-input-container class="small counter">
								<label>{{translate.load("sbi.ds.label")}}</label>
								<input name="lbl" ng-model="selectedParUse.label" ng-required="selectedTab == 1"
								ng-maxlength="20" ng-change="setDirty()">
							
									<div  ng-messages="attributeForm.lbl.$error" ng-show="selectedDriver.label== null">
										<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
									</div>
							
							 </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.name")}}</label>
							<input name="name" ng-model="selectedParUse.name"  ng-required = "selectedTab == 1"
						    ng-maxlength="40" ng-change="setDirty()">
						    
						    <div  ng-messages="attributeForm.name.$error" ng-show="selectedDriver.name== null">
				        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
				      </div>
						    
						    
						     </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.description")}}</label>
							<input ng-model="selectedParUse.description"
					        ng-maxlength="160" ng-change="setDirty()"> </md-input-container>
						</div>
					</div>
				
					
					<md-radio-group ng-model="selectedParUse.valueSelection" ng-init="selectedDriver.valueSelection = man_in"  layout="row">
				      <md-radio-button value="lov" class="md-primary">
				      
				      {{translate.load("sbi.analytical.drivers.usemode.lovdate")}}
					
				      </md-radio-button>
				      <md-radio-button value="map_in">
				      
				      {{translate.load("sbi.analytical.drivers.usemode.mapinput")}}
				      
				      </md-radio-button>
				      <md-radio-button value="man_in">
				      
				      {{translate.load("sbi.analytical.drivers.usemode.manualinput")}}
				      
				      </md-radio-button>
    				</md-radio-group>
    				
    				<div ng-show= "selectedParUse.valueSelection == 'lov'">
						<div layout="row" layout-wrap>
	      				<div flex=100>
					       <md-input-container class="small counter" > 
					       <label>{{translate.load("sbi.analytical.drivers.usemode.lovdate")}}</label>
					       <md-select  aria-label="dropdown"
					       	name ="dropdown" 
					        ng-required = "selectedParUse.valueSelection == 'lov'"
					        ng-model="selectedParUse.idLov"> <md-option 
					        ng-repeat="l in listDate track by $index" ng-click="FieldsCheck(l)" value="{{l.id}}">{{l.name}} </md-option>
					       </md-select>
					       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedParUse.idLov== null">
					        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					      </div>   
					        </md-input-container>
					    </div>
					    </div>
					    <div layout="row" layout-wrap>
	      				<div flex=100>
					       <md-input-container class="small counter" > 
					       <label>{{translate.load("sbi.generic.select")}}</label>
					       <md-select  aria-label="dropdown"
					       	name ="dropdown" 
					        ng-required = "selectedParUse.valueSelection == 'lov'"
					        ng-model="selectedParUse.selectionType"> <md-option 
					        ng-repeat="l in listSelType track by $index" ng-click="FieldsCheck(l)" value="{{l.VALUE_CD}}">{{l.VALUE_NM}}</md-option>
					       </md-select>
					       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedParUse.selectionType== null">
					        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					      </div>   
					        </md-input-container>
					    </div>
					    </div>												
					</div>
					<div ng-show= "selectedParUse.valueSelection == 'map_in'">
						<div layout="row" layout-wrap>
	      				<div flex=100>
					       <md-input-container class="small counter" > 
					       <label>{{translate.load("sbi.analytical.drivers.usemode.mapinput.layer")}}</label>
					       <md-select  aria-label="dropdown"
					       	name ="dropdown" 
					        ng-required = "selectedParUse.valueSelection == 'map_in'"
					        ng-model="selectedParUse.selectedLayer"> <md-option 
					        ng-repeat="l in layersList track by $index" ng-click="FieldsCheck(l)" value="{{l.name}}">{{l.name}} </md-option>
					       </md-select>
					       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedParUse.selectedLayer== null">
					        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					      </div>   
					        </md-input-container>
					    </div>
					    </div>
					   	<div layout="row" layout-wrap>
						<div flex=100>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.analytical.drivers.usemode.mapinput.layer.prop")}}</label>
							<input ng-model="selectedParUse.selectedLayerProp"
					        ng-maxlength="160" ng-change="setDirty()"> </md-input-container>
						</div>
					</div>					
					</div>
					
    				
    				<div layout="row" layout-wrap  ng-if = "selectedParUse.valueSelection != 'map_in'">
			    <md-input-container class="small counter"> 
			        <md-checkbox
			         ng-change="setDirty()"  ng-model="selectedParUse.maximizerEnabled" aria-label="driver">
			        </md-checkbox> 
			       </md-input-container>
			       <div flex=3 style="line-height: 40px">
			        <label>{{translate.load("sbi.analytical.drivers.usemode.expandable")}}</label>
			       </div>
			   		</div>
			   		
    				<label>{{translate.load("sbi.generic.defaultValue")}}</label>
    				<md-radio-group ng-model="selectedParUse.defaultrg" layout="row">
    				
				      <md-radio-button value="none" class="md-primary">
				      {{translate.load("sbi.analytical.drivers.usemode.none")}}
				      
				      </md-radio-button>
				      <md-radio-button value="lov" >
				      {{translate.load("sbi.analytical.drivers.usemode.uselov")}}
				      
				      </md-radio-button>
				      <md-radio-button value="pickup" ng-disabled="selectedParUse.valueSelection == 'map_in'">
				      {{translate.load("sbi.analytical.drivers.usemode.pickup")}}
				      
				      </md-radio-button>
    				</md-radio-group>
					<div ng-show= "defaultrg == 'lov'">
						<div layout="row" layout-wrap>
	      				<div flex=100>
					       <md-input-container class="small counter" > 
					       <label>{{translate.load("sbi.analytical.drivers.usemode.lovdate")}}</label>
					       <md-select  aria-label="dropdown"
					       	name ="dropdown" 
					        ng-required = "defaultrg == 'lov'"
					        ng-model="selectedParUse.idLovForDefault"> <md-option 
					        ng-repeat="l in listDate track by $index" ng-click="FieldsCheck(l)" value="{{l.id}}">{{l.name}} </md-option>
					       </md-select>
					       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedParUse.idLovForDefault== null">
					        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					      </div>   
					        </md-input-container>
					    </div>
					    </div>
				    </div>
					
					<div ng-show= "defaultrg == 'pickup'">
						<div layout="row" layout-wrap ng-if="selectedParUse.valueSelection != 'map_in'">
	      				<div flex=100>
					       <md-input-container class="small counter" > 
					       <label>{{translate.load("sbi.generic.select")}}</label>
					       <md-select  aria-label="dropdown"
					       	name ="dropdown" 
					        ng-required = "defaultrg == 'pickup'"
					        ng-model="selectedParUse.defaultFormula"> <md-option 
					        ng-repeat="f in defaultFormula track by $index" ng-click="FieldsCheck(l)" value="{{f.f_value}}">{{f.name}} </md-option>
					       </md-select>
					       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedParUse.defaultFormula == null">
					        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					      </div>   
					        </md-input-container>
					    </div>
					    </div>
				    </div>
				    <md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.users.roles");}}
					</md-toolbar>
					
				    <div layout="row" layout-wrap flex>
					<div flex="25" ng-repeat="rl in rolesList">
						<md-checkbox ng-checked="setRoles(rl, role)" ng-click="check(rl, role)"> 
						{{ rl.name }} 
						</md-checkbox>
					</div>
				    </div>
					
				     <md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.modalities.check.title.predefined");}}
					</md-toolbar>
					
					<div layout="row" layout-wrap flex>
					<div flex="25" ng-repeat="c in checksList">
						<md-checkbox > {{ c.name }} </md-checkbox>

					</div>
				    </div>	
				 </md-content>
				    </div>
				   
				 </md-content> </md-tab></md-tabs> </md-content>
		</div>	
		</form>
		</right-col>
	</angular_2_col>
</body>
</html>