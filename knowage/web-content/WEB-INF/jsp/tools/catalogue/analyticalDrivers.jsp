<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


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
<link rel="stylesheet" type="text/css"    href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css">

<!-- Styles -->
<script type="text/javascript" src=" "></script>
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/catalogues/analyticalDrivers.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Analytical Drivers Management</title>
</head>
<body class="bodyStyle kn-layerCatalogue" ng-controller="AnalyticalDriversController as ctrl" >
	<angular-list-detail show-detail="showme">
 		<list label='translate.load("sbi.analytical.drivers.title")' new-function="createDrivers"> 

			 <angular-table
			flex
			id="adList_id" 
			ng-model="adList"
			columns='[
					  {"label":"Label","name":"label"},
					  {"label":"Name","name":"name"},
					  {"label":"Type","name":"type"}
					]'
			show-search-bar=true
			highlights-selected-item=true
			speed-menu-option="adSpeedMenu"
			click-function="loadDrivers(item)">
		     </angular-table>
		
		<angular-table
		flex
		id="useModeList_id"
		ng-model="useModeList"
		columns='[
				  {"label":"Name","name":"name"}
				]'
		highlights-selected-item=true
		speed-menu-option="dumSpeedMenu" click-function="loadUseMode(item)">
	</angular-table>

		</list>
		
		<extra-button>
			  <md-button class="md-flat" ng-click="createUseModes()" ng-show="showme">New UseMode</md-button>
		</extra-button>
		
		<detail label=' selectedDriver.label==undefined? "" : selectedDriver.label'  save-function="save"
		cancel-function="cancel"
		disable-save-button="!attributeForm.$valid"
		show-save-button="showme" show-cancel-button="showme">
		<form name="attributeForm" ng-submit="attributeForm.$valid && save()"
		
		<md-card>
	        <md-card-content>
		      
		     <md-tabs md-dynamic-height md-selected="selectedTab" md-border-bottom="">
			<md-tab label='{{translate.load("sbi.analytical.drivers.details");}}'>
			<md-content flex 
			class="ToolbarBox miniToolbar noBorder mozTable">
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
			<md-tab label='{{translate.load("sbi.analytical.drivers.usemode.details");}}' ng-if="showadMode"> 
			<md-content flex 
			class="ToolbarBox miniToolbar noBorder mozTable">
				
					<div layout="row" layout-wrap>
						<div flex=100>
							 <md-input-container class="small counter">
								<label>{{translate.load("sbi.ds.label")}}</label>
								<input name="lbl" ng-model="selectedParUse.label" ng-required="selectedTab == 1"
								ng-maxlength="20" ng-change="setDirty()">
							
									<div  ng-messages="attributeForm.lbl.$error" ng-show="selectedParUse.label== null">
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
						    
						    <div  ng-messages="attributeForm.name.$error" ng-show="selectedParUse.name== null">
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
				
					
					<md-radio-group ng-model="selectedParUse.valueSelection" ng-init="selectedParUse.valueSelection = man_in"  layout="row">
				      <md-radio-button value="lov">
				      
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
					        ng-model="selectedParUse.idLov">
					        <md-option 
					        ng-repeat="l in listDate track by $index" value="{{l.id}}">{{l.name}}
					        </md-option>
					       </md-select>
					       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedParUse.idLov== -1">
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
					        ng-repeat="l in listSelType track by $index" value="{{l.VALUE_CD}}">{{l.VALUE_NM}}</md-option>
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
					        ng-repeat="l in layersList track by $index" value="{{l.name}}">{{l.name}} </md-option>
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
    				
				      <md-radio-button value="none">
				      {{translate.load("sbi.analytical.drivers.usemode.none")}}
				      
				      </md-radio-button>
				      <md-radio-button value="lov" >
				      {{translate.load("sbi.analytical.drivers.usemode.uselov")}}
				      
				      </md-radio-button>
				      <md-radio-button value="pickup" ng-disabled="selectedParUse.valueSelection == 'map_in'">
				      {{translate.load("sbi.analytical.drivers.usemode.pickup")}}
				      
				      </md-radio-button>
    				</md-radio-group>
					<div ng-show= "selectedParUse.defaultrg == 'lov'">
						<div layout="row" layout-wrap>
	      				<div flex=100>
					       <md-input-container class="small counter" > 
					       <label>{{translate.load("sbi.analytical.drivers.usemode.lovdate")}}</label>
					       <md-select  aria-label="dropdown"
					       	name ="dropdown" 
					        ng-required = "defaultrg == 'lov'"
					        ng-model="selectedParUse.idLovForDefault"> <md-option 
					        ng-repeat="l in listDate track by $index" value="{{l.id}}">{{l.name}} </md-option>
					       </md-select>
					       <div  ng-messages="attributeForm.dropdown.$error" ng-show="selectedParUse.idLovForDefault== -1">
					        <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired");}}</div>
					      </div>   
					        </md-input-container>
					    </div>
					    </div>
				    </div>
					
					<div ng-show= "selectedParUse.defaultrg == 'pickup'">
						<div layout="row" layout-wrap ng-if="selectedParUse.valueSelection != 'map_in'">
	      				<div flex=100>
					       <md-input-container class="small counter" > 
					       <label>{{translate.load("sbi.generic.select")}}</label>
					       <md-select  aria-label="dropdown"
					       	name ="dropdown" 
					        ng-required = "defaultrg == 'pickup'"
					        ng-model="selectedParUse.defaultFormula"> <md-option 
					        ng-repeat="f in defaultFormula track by $index" value="{{f.f_value}}">{{f.name}} </md-option>
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
					<div flex="25" ng-repeat="role in rolesList">
					
						<md-checkbox 
						ng-required = "associatedRoles.length== 0"
						ng-checked="getCheckboxes(role , associatedRoles)"
						ng-click="checkCheckboxes(role , associatedRoles)">
						{{ role.name }}
						</md-checkbox>
					</div>
				    </div>
				    <md-input-container class="small counter" >
				    <div  ng-messages="attributeForm.dropdown.$error" ng-show="associatedRoles.length== 0">
					<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired.role");}}</div>
					</div>  
					</md-input-container>
				     <md-toolbar class="md-blue minihead md-toolbar-tools" 
												style="margin-top:15px" >
										{{translate.load("sbi.modalities.check.title.predefined");}}
					</md-toolbar>
					
					<div layout="row" layout-wrap flex>
					<div flex="25" ng-repeat="check in checksList">
						<md-checkbox 
						ng-checked="getCheckboxes(check , associatedChecks)" 
						ng-click="checkCheckboxes(check , associatedChecks)"> 
						{{ check.name }} 
						</md-checkbox>
					</div>
				    </div>	
				 </md-content>
				    </div>
				   
				 </md-content> </md-tab></md-tabs> </md-content> 
		      
				
			</md-card-content>
	      </md-card>
		
		
		
		</form>
		</detail>
	</angular-list-detail>
</body>
</html>
