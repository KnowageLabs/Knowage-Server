<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="dataSourceModule">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->
<link rel="stylesheet" type="text/css"	href="/knowage/themes/glossary/css/generalStyle.css">
<link rel="stylesheet" type="text/css"	href="/knowage/themes/catalogue/css/catalogue.css">

<!-- JavaScript -->	
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/datasource/datasource.js"></script>

</head>
<body class="bodyStyle" ng-controller="dataSourceController as ctrl">
	<angular_2_col>
		<left-col>
			<div class="leftBox">
				<md-toolbar class="header">
					<div class="md-toolbar-tools">
						<div>{{translate.load("sbi.ds.dataSource");}}</div>
											
						<md-button
							id="createNewDataSourceForm" 
							class="md-fab md-ExtraMini addButton"
							style="position:absolute; right:11px; top:0px;"
							ng-click="createNewForm()"> 
							<md-icon
								md-font-icon="fa fa-plus" 
								style=" margin-top: 6px ; color: white;">
							</md-icon> 
						</md-button>
						
					</div>
				</md-toolbar>
				
				<md-content flex layout-padding style="background-color: rgb(236, 236, 236);" class="ToolbarBox miniToolbar noBorder leftListbox">
					<angular-table 
						layout-fill
						id="dataSourceList"
						ng-model="dataSourceList"
						columns='[{"label":"Label","name":"label","size":"50px"},{"label":"Description","name":"descr","size":"70px"}]'
						columns-search='["label","descr"]'
						show-search-bar=true
						highlights-selected-item=true
						click-function="loadSelectedDataSource(item)"
						selected-item="selectedDataSourceItems"
						speed-menu-option="dsSpeedMenu"					
					>						
					</angular-table>
				</md-content>
			</div>			
		</left-col>
			
		<right-col>
		
			<form name="forms.dataSourceForm" layout-fill ng-submit="forms.dataSourceForm.$valid && saveOrUpdateDataSource()" class="detailBody md-whiteframe-z1">
				
			<div ng-show="showme">
			
				<md-toolbar class="header">
					<div class="md-toolbar-tools">
						<div>{{translate.load("sbi.ds.dataSource");}}</div>
						<div style="position: absolute; right: 0px" class="h100">
						
							<md-button id="saveorUpdateDataSourceBtn" type="submit"
								aria-label="save datasource" class="md-raised md-ExtraMini rightHeaderButtonBackground"
								style=" margin-top: 2px;"
								ng-click="closeForm()">
							{{translate.load("sbi.generic.cancel");}} 
							</md-button>
							
							<md-button type="button"
								aria-label="test datasource" class="md-raised md-ExtraMini rightHeaderButtonBackground"
								style=" margin-top: 2px;"
								ng-disabled="!forms.dataSourceForm.$valid"
								ng-click="testDataSource()">
							{{translate.load("sbi.datasource.testing");}} 
							</md-button>
							
							<md-button type="submit"
								aria-label="save datasource" class="md-raised md-ExtraMini rightHeaderButtonBackground"
								style=" margin-top: 2px;"
								ng-disabled="!forms.dataSourceForm.$valid">
							{{translate.load("sbi.browser.defaultRole.save");}} 
							</md-button>
							
						</div>
					</div>
					</md-toolbar>
					
					<md-content flex style="margin-left:20px;" class="md-padding ToolbarBox miniToolbar noBorder">
						
						<div layout="row" layout-wrap>
							<div flex=100>
								<md-input-container class="small counter">
								<label>{{translate.load("sbi.ds.label")}}</label>
								<input ng-model="selectedDataSource.label" required
									ng-change="setDirty()" ng-maxlength="50">
									
								<div ng-messages="forms.dataSourceForm.label.$error" ng-show="!selectedDataSource.label">
	          							<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
	        					</div> 
								
								</md-input-container>
							</div>
						</div>
						
						<div layout="row" layout-wrap>
							<div flex=100>
								<md-input-container class="small counter"> 
								<label>{{translate.load("sbi.ds.description")}}</label>
								<input ng-model="selectedDataSource.descr"
									ng-change="setDirty()" ng-maxlength="160"> </md-input-container>
							</div>
						</div>
						
						<div layout="row" layout-wrap>
							<div flex=95>
								<md-input-container class="small counter"> 
									<label>{{translate.load("sbi.datasource.dialect")}}</label>
									<md-select  ng-change="setDirty()"  aria-label="aria-label"
										ng-model="selectedDataSource.dialectId"> <md-option
										ng-repeat="d in dialects" value="{{d.VALUE_ID}}">{{d.VALUE_NM}} </md-option>
									</md-select>
									<div ng-messages="forms.dataSourceForm.dialectId.$error" ng-show="!selectedDataSource.dialectId">
	          							<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
	        						</div>  
								</md-input-container>
							</div>
						</div>
						
						<div layout="row" layout-wrap>
							<div flex=3 style="line-height: 40px">
								<label>{{translate.load("sbi.datasource.multischema")}}:</label>
							</div>
		
							<md-input-container class="small counter"> 
								<md-checkbox
									ng-change="setDirty()"  ng-model="selectedDataSource.multiSchema" aria-label="Multischema">
								</md-checkbox> 
							</md-input-container>
						</div>
						
						<div ng-show= "selectedDataSource.multiSchema == true " layout="row" layout-wrap>
							<div flex=100>
								<md-input-container class="small counter"> 
								<label>{{translate.load("sbi.datasource.multischema.attribute")}}</label>
								<input ng-change="setDirty()"  ng-model="selectedDataSource.schemaAttribute"
									ng-maxlength="45"> </md-input-container>
							</div>
						</div>
						
						<div layout="row" layout-wrap>
							<md-radio-group  ng-change="setDirty()"  ng-model="selectedDataSource.readOnly"> Read only:
		      					<md-radio-button  value="1" ng-disabled="selectedDataSource.writeDefault">Read only</md-radio-button>
		      					<md-radio-button  value="0"> Read and write </md-radio-button>
		    				</md-radio-group>
						</div>
						
						<div layout="row" layout-wrap>
							<div flex=3 style="line-height: 40px">
								<label>{{translate.load("sbi.datasource.writedefault")}}:</label>
							</div>
		
							<md-input-container> 
								<md-checkbox ng-change="setDirty()" 
									ng-model="selectedDataSource.writeDefault" ng-disabled="selectedDataSource.readOnly == 1" aria-label="Write Default">
								</md-checkbox> 
							</md-input-container>
							
						</div>
						
						<div layout="row" ng-init="type='JDBC'" layout-wrap>
							<md-radio-group ng-model="type"> Type:
		      					<md-radio-button ng-class="{'md-checked':!selectedDataSource.jndi.length}"  value="JDBC">JDBC</md-radio-button>
		      					<md-radio-button ng-class="{'md-checked':selectedDataSource.jndi.length}" value="JNDI">JNDI</md-radio-button>
		    				</md-radio-group>
						</div>
						
						<div ng-show= "type == 'JDBC'" ng-hide="selectedDataSource.jndi.length">
											
							<div layout="row" layout-wrap>
								<div flex=100>
									<md-input-container class="small counter">
										<label>{{translate.load("sbi.datasource.type.jdbc.url")}}</label>
										<input ng-change="setDirty()"  ng-model="selectedDataSource.urlConnection" ng-required="type=='JDBC'"
											ng-maxlength="500">
										<div ng-messages="forms.dataSourceForm.urlConnection.$error" ng-show="!selectedDataSource.urlConnection">
	          								<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
	        							</div>  
									</md-input-container>
								</div>
							</div>
							
							<div layout="row" layout-wrap>
								<div flex=100>
									<md-input-container class="small counter">
									<label>{{translate.load("sbi.datasource.type.jdbc.user")}}</label>
									<input ng-change="setDirty()"  ng-model="selectedDataSource.user" 
										ng-maxlength="50"> </md-input-container>
								</div>
							</div>
							
							<div layout="row" layout-wrap>
								<div flex=100>
									<md-input-container class="small counter">
									<label>{{translate.load("sbi.datasource.type.jdbc.password")}}</label>
									<input ng-change="setDirty()"  type="password" name="password" ng-model="selectedDataSource.pwd" 
										ng-maxlength="50"> </md-input-container>
								</div>
							</div>						
							
							<div layout="row" layout-wrap>
								<div flex=100>
									<md-input-container class="small counter">
										<label>{{translate.load("sbi.datasource.driver")}}</label>
										<input ng-change="setDirty()"  ng-model="selectedDataSource.driver" ng-required="type=='JDBC'"
											ng-maxlength="160">
										<div ng-messages="forms.dataSourceForm.driver.$error" ng-show="!selectedDataSource.driver">
	          								<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
	        							</div>  
									</md-input-container>
								</div>
							</div>
						</div>
						
						<div flex=95 layout="row" ng-show= "type == 'JNDI' || selectedDataSource.jndi.length">
							<md-input-container flex> 
								<label>{{translate.load("sbi.datasource.type.jndi.name")}}</label>
								<input ng-model="selectedDataSource.jndi"> 
							</md-input-container>
							<md-icon ng-click="showJdniInfo()" md-font-icon="fa fa-info-circle fa-lg"></md-icon>
						</div>

			</md-content>
				</div>
			</form>
		</right-col>
	</angular_2_col>	
</body>
</html>