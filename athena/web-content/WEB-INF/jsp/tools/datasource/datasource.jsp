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
<link rel="stylesheet" type="text/css"	href="/athena/themes/glossary/css/generalStyle.css">

<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/datasource/datasource.js"></script>
	
<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>

</head>
<body class="bodyStyle" ng-controller="dataSourceController as ctrl">
	<angular_2_col>
		<left-col>
			<div class="leftBox">
				<md-toolbar class="md-blue minihead">
					<div class="md-toolbar-tools">
						<div>{{translate.load("sbi.ds.dataSource");}}</div>
						<md-button 
							class="md-fab md-ExtraMini addButton"
							style="position:absolute; right:11px; top:0px;"
							ng-click="loadDataSourceList(null)"> 
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
						id="dataSourceList"
						ng-model="dataSourceList"
						columns='["DATASOURCE_LABEL","DESCRIPTION"]'
						columns-search='["DATASOURCE_LABEL","DESCRIPTION"]'
						show-search-bar=true
						highlights-selected-item=true
						click-function="loadDataSource(item)"						
					>						
					</angular-table>
				</md-content>
			</div>			
		</left-col>
			
		<right-col>
		
			<form name="contactForm" layout-fill id="layerform"
				ng-submit="contactForm.$valid && saveDataSource()"
				class="detailBody md-whiteframe-z1" novalidate>
				
			<div ng-show="showme">
			
			<md-toolbar class="md-blue minihead">
				<div class="md-toolbar-tools h100">
					<div style="text-align: center; font-size: 24px;">{{translate.load("sbi.ds.dataSource");}}</div>
					<div style="position: absolute; right: 0px" class="h100">
						<md-button type="button" tabindex="-1" aria-label="cancel"
							class="md-raised md-ExtraMini " style=" margin-top: 2px;"
							ng-click="cancel()">{{translate.load("sbi.browser.defaultRole.cancel");}}
						</md-button>
						<md-button ng-disabled="!contactForm.$valid" type="submit"
							aria-label="save layer" class="md-raised md-ExtraMini "
							style=" margin-top: 2px;"
							ng-disabled=" selectedItem.name.length === 0 ||  selectedItem.type.length === 0">
						{{translate.load("sbi.browser.defaultRole.save");}} </md-button>
					</div>
				</div>
				</md-toolbar>
				
				<md-content flex style="margin-left:20px;" class="ToolbarBox miniToolbar noBorder">
					
					<div layout="row" layout-wrap>
						<div flex=25>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.ds.label")}}</label>
							<input ng-model="selectedDataSource.label" required
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=25>
							<md-input-container class="small counter"> 
							<label>{{translate.load("sbi.ds.description")}}</label>
							<input ng-model="selectedDataSource.descr"
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=25>
							<md-input-container class="small counter"> 
							<label>{{translate.load("sbi.datasource.dialect")}}</label>
							<md-select aria-label="aria-label"
								ng-model="selectedDataSource.dialect_id"> <md-option
								ng-repeat="d in dialects" value="{{d.VALUE_ID}}">{{d.VALUE_NM}} </md-option>
							</md-select> </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=3 style="line-height: 40px">
							<label>{{translate.load("sbi.datasource.multischema")}}:</label>
						</div>
	
						<md-input-container class="small counter"> 
						<md-checkbox
							ng-model="selectedDataSource.multischema" aria-label="Multischema">
						</md-checkbox> </md-input-container>
					</div>
					
					<div layout="row" layout-wrap>
						<md-radio-group ng-model="selectedDataSource.readOnly"> Read only:
	      					<md-radio-button value="Read only" class="md-primary">Read only</md-radio-button>
	      					<md-radio-button value="Read and write"> Read and write </md-radio-button>
	    				</md-radio-group>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=3 style="line-height: 40px">
							<label>{{translate.load("sbi.datasource.writedefault")}}:</label>
						</div>
	
						<md-input-container class="small counter"> <md-checkbox
							ng-model="selectedDataSource.writeDefault" aria-label="WriteDefault">
						</md-checkbox> </md-input-container>
					</div>
					
					<div layout="row" layout-wrap>
						<md-radio-group ng-model="selectedDataSource.type"> Type:
	      					<md-radio-button value="Read only" class="md-primary">JDBC</md-radio-button>
	      					<md-radio-button value="Read and write">JNDI</md-radio-button>
	    				</md-radio-group>
					</div>
										
					<div layout="row" layout-wrap>
						<div flex=25>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.datasource.type.jdbc.url")}}</label>
							<input ng-model="selectedDataSource.url" required
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=25>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.datasource.type.jdbc.user")}}</label>
							<input ng-model="selectedDataSource.user" required
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
					</div>
					
					<div layout="row" layout-wrap>
						<div flex=25>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.datasource.type.jdbc.password")}}</label>
							<input type="password" name="password" ng-model="selectedDataSource.password" required
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
					</div>
					
					
					<div layout="row" layout-wrap>
						<div flex=25>
							<md-input-container class="small counter">
							<label>{{translate.load("sbi.datasource.driver")}}</label>
							<input ng-model="selectedDataSource.driver" required
								maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
						</div>
					</div>
					
				</md-content>
			</form>
		</right-col>
	</angular_2_col>	
</body>
</html>