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
<html ng-app="datasetModule">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/catalogues/datasetManagement.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Dataset Catalogue</title>
</head>
<body ng-controller="datasetController" class="bodyStyle kn-rolesManagement">

	<angular-list-detail>
       
       <list label="translate.load('sbi.roles.datasets')"  new-function="createNewDataSet">
       
       	<angular-table
		     flex
			 id="datasetList_id" ng-model="datasetsList"
			columns=dataSetListColumns
			show-search-bar=true highlights-selected-item=true
			click-function="loadDataSet(item)">
		</angular-table> 
        
       </list>
       
       <extra-button>
			<md-fab-speed-dial md-open="false" md-direction="left"
                         ng-class="'md-scale'">
	        <md-fab-trigger>
	          <md-button aria-label="menu" class="md-fab md-raised md-mini md-warn">
	            <md-icon md-font-icon="fa fa-bars" class="fa fa-2x"></md-icon>
	          </md-button>
	        </md-fab-trigger>
	
	        <md-fab-actions>
	            <md-icon md-font-icon="fa fa-eye" ng-click="previewDataset()"></md-icon>
	            
	        </md-fab-actions>
	      </md-fab-speed-dial>
		</extra-button>
       
       <detail save-function="saveDataSet" cancel-function="cancelDataSet">
       
       		<form>
       		
       			 <md-tabs md-dynamic-height md-selected="selectedTab" md-border-bottom="">
					            			
					 <md-tab label='{{translate.load("sbi.generic.details");}}'>
					 	<md-content flex class="ToolbarBox miniToolbar noBorder mozTable">
							<md-card layout-padding>
								<div flex=100>
									<md-input-container class="md-block">
								    	<label>{{translate.load("sbi.ds.label")}}</label>
										<input ng-model="selectedDataSet.label">
									</md-input-container>
								</div>
								<div flex=100>
									<md-input-container class="md-block">
								    	<label>{{translate.load("sbi.ds.name")}}</label>
										<input ng-model="selectedDataSet.name">
									</md-input-container>
								</div>
								<div flex=100>
									<md-input-container class="md-block">
								    	<label>{{translate.load("sbi.ds.description")}}</label>
										<textarea ng-model="selectedDataSet.description" md-maxlength="150" rows="3" md-select-on-focus></textarea>
									</md-input-container>
								</div>
								<div flex=100>
							       <md-input-container class="md-block" > 
							       <label>{{translate.load("sbi.ds.scope")}}</label>
							       <md-select placeholder ="{{translate.load('sbi.ds.scope')}}"
							        ng-required = "true"
							        ng-model="selectedDataSet.scopeId">   
							        <md-option 
							        ng-repeat="l in scopeList" value="{{l.VALUE_ID}}">{{l.VALUE_CD}}
							        </md-option>
							       </md-select>  
							        </md-input-container>
							   </div>
							   <div flex=100>
							       <md-input-container class="md-block" > 
							       <label>{{translate.load("sbi.generic.category")}}</label>
							       <md-select placeholder ="{{translate.load('sbi.generic.category')}}"
							        ng-model="selectedDataSet.catTypeId">   
							        <md-option 
							        ng-repeat="l in categoryList" value="{{l.VALUE_ID}}">{{l.VALUE_CD}}
							        </md-option>
							       </md-select>  
							        </md-input-container>
							   </div>
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable">
							<md-card layout-padding>
								<angular-table
								 flex
			 					 id="datasetVersionList_id" 
			 					 ng-model="selectedDataset.dsVersions"
								 columns='[
								         {"label":"Creation User","name":"userIn"},
								         {"label":"Type","name":"type"},
								         {"label":"Creation Date", "name":"dateIn"}
								         ]'
								show-search-bar=false
								highlights-selected-item=true>
								></angular-table>
							</md-card>
						</md-content>
						
				     </md-tab>
				     
				     <md-tab label='{{translate.load("sbi.generic.type");}}'>
				     
				     	<md-content flex class="ToolbarBox miniToolbar noBorder mozTable">
							<md-card layout-padding>
								<div flex=100>
							       <md-input-container class="md-block" > 
							       <label>{{translate.load("sbi.ds.dsTypeCd")}}</label>
							       <md-select placeholder ="{{translate.load('sbi.ds.dsTypeCd')}}"
							        ng-required = "true"
							        ng-model="selectedDataSet.dsTypeCd">   
							        <md-option 
							        ng-repeat="l in datasetTypeList" value="{{l.VALUE_CD}}">{{l.VALUE_CD}}
							        </md-option>
							       </md-select>  
							        </md-input-container>
							   </div>
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='File'">
							<md-card layout-padding>
								FILE
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Query'">
							<md-card layout-padding>
								<div flex=100>
							       <md-input-container class="md-block" > 
							       <label>{{translate.load("sbi.ds.dataSource")}}</label>
							       <md-select placeholder ="{{translate.load('sbi.ds.dataSource')}}"
							        ng-required = "selectedDataSet.dsTypeCd=='Query'"
							        ng-model="selectedDataSet.dataSource">   
							        <md-option 
							        ng-repeat="l in dataSourceList" value="{{l.label}}">{{l.label}}
							        </md-option>
							       </md-select>  
							        </md-input-container>
							   </div>
							   <md-input-container class="md-block">
								    	<label>{{translate.load("sbi.ds.query")}}</label>
										<textarea ng-model="selectedDataSet.query" rows="8" md-select-on-focus></textarea>
									</md-input-container>
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Java Class'">
							<md-card layout-padding>
								JAVA CLASS
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Script'">
							<md-card layout-padding>
								SCRIPT
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Qbe'">
							<md-card layout-padding>
								QBE
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Custom'">
							<md-card layout-padding>
								CUSTOM
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Flat'">
							<md-card layout-padding>
								FLAT
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Ckan'">
							<md-card layout-padding>
								CKAN
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='Federated'">
							<md-card layout-padding>
								FEDERATED
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable" ng-if="selectedDataSet.dsTypeCd=='REST'">
							<md-card layout-padding>
								REST
							</md-card>
						</md-content>
												
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable">
							<md-card layout-padding>
								<div>
					  				{{translate.load('sbi.scheduler.parameters')}}
								<i class="fa fa-plus-square" ng-click="addParameters()" aria-hidden="true" ></i>
								<div ng-repeat="p in selectedDataSet.parameters" layout-gt-sm="row" layout-align="start center">
									<div layout="row"> 								
											<md-input-container class="md-block" flex-gt-sm>
			      							<label>{{translate.load('sbi.generic.name')}}</label>
			  								<input ng-model="p.name">
											</md-input-container>
											
											<md-input-container class="md-block" flex-gt-sm>
			      							<label>{{translate.load('sbi.generic.type')}}</label>
			  								<input ng-model="p.type">
											</md-input-container>
											
											<md-input-container class="md-block" flex-gt-sm>
			      							<label>{{translate.load('sbi.generic.defaultValue')}}</label>
			  								<input ng-model="p.defaultValue">
											</md-input-container>
									</div> 								
			 						<div>
										<i class="fa fa-minus-square" ng-click="removeParameter(i)" aria-hidden="true"></i> 	
									</div>	      						
								</div>
							</div>
							</md-card>
						</md-content>
						
				     </md-tab>	
				     
				     <md-tab label='{{translate.load("sbi.ds.advancedTab");}}'>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable">
							<md-card layout-padding>
							
							</md-card>
						</md-content>
						
						<md-content flex class="ToolbarBox miniToolbar noBorder mozTable">
							<md-card layout-padding>
							
							</md-card>
						</md-content>
						
				     </md-tab>						
					
				</md-tabs>
       		
       		</form>
       
       </detail>
       
	</angular-view-detail>

</body>
</html>