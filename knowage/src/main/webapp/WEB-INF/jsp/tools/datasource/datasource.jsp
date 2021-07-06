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

<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@ page language="java" pageEncoding="UTF-8" session="true"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spagobi.commons.utilities.UserUtilities"%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>



<%
	Boolean superadmin =(Boolean)((UserProfile)userProfile).getIsSuperadmin();
	boolean isAdmin = UserUtilities.isAdministrator(userProfile);
%>

<script>
	var superadmin = <%= superadmin %>;
	var isAdmin = <%= isAdmin %>;
</script>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="dataSourceModule">

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<!-- Styles -->
<%-- 	<link rel="stylesheet" type="text/css"	href="/knowage/themes/commons/css/customStyle.css"> --%>
	<link rel="stylesheet" type="text/css"	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
	<!-- JavaScript -->
<%-- 	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/datasource/datasource.js"></script> --%>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/datasource/datasource.js")%>"></script>
</head>

<body class="bodyStyle kn-dataSource" ng-controller="dataSourceController as ctrl">
	<angular-list-detail  show-detail="showMe">

	 	<list label='translate.load("sbi.ds.dataSource")' new-function="createNewDatasource" show-new-button="<%= isAdmin %>">
			<angular-table
						flex
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
		</list>
			
			<extra-button>
			  <md-button class="md-flat" ng-click="testDataSource()" ng-disabled="readOnly" ng-show="showMe" >{{translate.load("sbi.datasource.testing")}}</md-button>
		</extra-button>

			<detail
				label='(selectedDataSource.label==undefined)? "" : selectedDataSource.label'
				save-function="saveOrUpdateDataSource"
				cancel-function="closeForm"
				disable-save-button="!dataSourceForm.$valid || readOnly == true"
				show-save-button="showMe"
				show-cancel-button="showMe"
			>
							<form name="dataSourceForm"  ng-disabled="readOnly" ng-submit="dataSourceForm.$valid && saveOrUpdateDataSource()" class="detailBody mozSize">
								<md-card>
									<md-card-content>
									<div>
									<!-- LABEL -->
										<md-input-container  class="md-block">
											<label>{{translate.load("sbi.ds.label")}}</label>
											<input ng-model="selectedDataSource.label" required ng-change="setDirty()"  ng-maxlength="100" ng-readonly="readOnly || modifyMode">
											<div ng-messages="dataSourceForm.label.$error" ng-show="!selectedDataSource.label">
												<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
											</div>
										</md-input-container>

									<!-- DESCRIPTION -->
										<md-input-container  class="md-block">
											<label>{{translate.load("sbi.ds.description")}}</label>
											<input ng-model="selectedDataSource.descr"
												ng-change="setDirty()" ng-maxlength="160" ng-readonly="readOnly">
										</md-input-container>
										
									<!-- DIALECT -->
										<md-input-container  class="md-block" >
											<label>{{translate.load("sbi.datasource.dialect")}}</label>
											<md-select  required ng-disabled="readOnly" ng-change="setDirty(); selectDatabase(selectedDataSource.dialectName)"  aria-label="aria-label"	ng-model="selectedDataSource.dialectName" >
												<md-option ng-repeat="d in databases" value="{{d.databaseDialect.value}}">{{d.name}} </md-option>
											</md-select>
											<div ng-messages="dataSourceForm.dialectName.$error" ng-show="!selectedDataSource.dialectName">
			          							<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
			        						</div>
										</md-input-container>

									<!-- MULTISCHEMA -->
										<md-input-container ng-if="jdbcOrJndi.type == 'JNDI'" class="md-block">
											<md-checkbox ng-disabled="readOnly" ng-change="setDirty()"  ng-model="selectedDataSource.multiSchema" aria-label="Multischema">{{translate.load("sbi.datasource.multischema")}}</md-checkbox>
										</md-input-container>

									<!-- SCHEMA ATTRIBUTE -->
											<md-input-container  class="md-block" ng-show="selectedDataSource.multiSchema" >
											<label>{{translate.load("sbi.datasource.multischema.attribute")}}</label>
											<input ng-change="setDirty()"  ng-model="selectedDataSource.schemaAttribute"
												ng-maxlength="45" ng-disabled="readOnly"> </md-input-container>

									<!-- READ ONLY -->
										<md-radio-group ng-disabled="readOnly" ng-change="setDirty()"  ng-model="selectedDataSource.readOnly"> {{translate.load("sbi.datasource.readonly")}}
					      					<md-radio-button  value="1" ng-disabled="selectedDataSource.writeDefault">{{translate.load("sbi.datasource.readonly")}}</md-radio-button>
					      					<md-radio-button  value="0" ng-disabled="!selectedDatabase.cacheSupported">{{translate.load("sbi.datasource.readwrite")}}</md-radio-button>
					    				</md-radio-group>

									<!-- WRITE DEFAULT -->
										<md-input-container class="md-block" ng-show="isSuperAdminFunction()">
											<md-checkbox ng-change="setDirty()"	ng-model="selectedDataSource.writeDefault"
											 ng-disabled="(readOnly || !selectedDatabase.cacheSupported || selectedDataSource.readOnly == 1) || !isSuperAdminFunction()" 
											 aria-label="Write Default">
												{{translate.load("sbi.datasource.writedefault")}}
											</md-checkbox>
										</md-input-container>

									<!-- TYPE -->
										<md-radio-group ng-model="jdbcOrJndi.type" ng-change="clearType()"> Type:
					      					<md-radio-button value="JDBC" ng-disabled="readOnly">JDBC</md-radio-button>
					      					<md-radio-button value="JNDI" ng-disabled="(readOnly || isAdmin) && !isSuperAdminFunction()">JNDI</md-radio-button>
					    				</md-radio-group>
									</div>
									<!-- JDBC -->
									<div ng-if= "jdbcOrJndi.type == 'JDBC'" layout="column">

										<!-- URL -->
										<div layout="row">
										<md-input-container flex="100" class="md-block" >
													<label>{{translate.load("sbi.datasource.type.jdbc.url")}}</label>
													<input ng-change="setDirty()"  ng-model="selectedDataSource.urlConnection" required
														ng-maxlength="500" ng-readonly="readOnly">
													<div ng-messages="forms.dataSourceForm.urlConnection.$error" ng-show="!selectedDataSource.urlConnection">
				          								<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
				        							</div>
												</md-input-container>
											</div>

										<!-- USER -->
										<div layout="row">
										<md-input-container flex="100" class="md-block" >
												<label>{{translate.load("sbi.datasource.type.jdbc.user")}}</label>
												<input ng-change="setDirty()"  ng-model="selectedDataSource.user"
													ng-maxlength="50" ng-readonly="readOnly"> </md-input-container>
											</div>


										<!-- PASSWORD -->
										<div layout="row">
										<!-- 
											Next <md-input-container> was duplicated because this version of Angular
											doesn't support conditional attribute (see placeholder in <input>).
											
											TODO : to fix
										 -->				
										<md-input-container ng-if="modifyMode" flex="100" class="md-block" >
												
												<label>{{translate.load("sbi.datasource.type.jdbc.password")}}</label>
												<input ng-change="setDirty()"  type="password" name="password" ng-model="selectedDataSource.pwd"
													ng-maxlength="50" ng-readonly="readOnly" placeholder="{{translate.load('sbi.datasource.type.jdbc.onlyIfChanged')}}"> </md-input-container>
												
										<md-input-container ng-if="!modifyMode" flex="100" class="md-block" >
												<label>{{translate.load("sbi.datasource.type.jdbc.password")}}</label>
												<input ng-change="setDirty()"  type="password" name="password" ng-model="selectedDataSource.pwd"
													ng-maxlength="50" ng-readonly="readOnly"> </md-input-container>
										
										<!--
											Replace previous duplication of <md-input-container>.
											
											Uncomment when Angular will be updated. 
										 -->
										<!-- <md-input-container flex="100" class="md-block" >
												<label>{{translate.load("sbi.datasource.type.jdbc.password")}}</label>
												<input ng-change="setDirty()"  type="password" name="password" ng-model="selectedDataSource.pwd"
													ng-maxlength="50" ng-readonly="readOnly" ng-attr-placeholder="{{ modifyMode ? translate.load('sbi.datasource.type.jdbc.onlyIfChanged') : undefined }}"> </md-input-container> -->
											</div>
									

										<!-- DRIVER -->
										<div layout="row">
										<md-input-container flex="100" class="md-block" >
													<label>{{translate.load("sbi.datasource.driver")}}</label>
													<input ng-change="setDirty()"  ng-model="selectedDataSource.driver" required
														ng-maxlength="160" ng-readonly="readOnly">
													<div ng-messages="forms.dataSourceForm.driver.$error" ng-show="!selectedDataSource.driver">
				          								<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
				        							</div>
												</md-input-container>
											</div>
											
										  <!-- ADVANCED OPTIONS -->
										  <div layout="row">
										  	<md-card layout="column" style="width:100%; margin:0;">
										  		<md-toolbar class="ternaryToolbar">
										  			  <div class="md-toolbar-tools">
												        <h2>{{translate.load("sbi.datasource.type.jdbc.advancedOptions")}}</h2>
												        <span flex></span>
												        <md-button aria-label="Toogle" class="md-icon-button" ng-click="showAdvancedOptions()">
												        	<md-icon ng-class="{'fa':true,'fa-chevron-down':!JDBCAdvancedOptionsShow,'fa-chevron-up':JDBCAdvancedOptionsShow}"></md-icon>
												        </md-button>
												      </div>									  			
										  		</md-toolbar>
										  		<md-card-content ng-if="JDBCAdvancedOptionsShow">
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<label>{{translate.load("sbi.datasource.type.jdbc.maxTotal")}}</label>
															<input type="number" ng-change="setDirty()" ng-model="selectedDataSource.jdbcPoolConfiguration.maxTotal"
																ng-maxlength="20" ng-readonly="readOnly">
															<div ng-messages="forms.dataSourceForm.driver.$error" ng-show="!selectedDataSource.jdbcPoolConfiguration.maxTotal">
				          										<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
				        									</div>
														</md-input-container>
														<md-icon ng-click="showMaxTotalInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  													  		
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<label>{{translate.load("sbi.datasource.type.jdbc.maxWait")}}</label>
															<input required type="number" ng-change="setDirty()" ng-model="selectedDataSource.jdbcPoolConfiguration.maxWait"
																ng-maxlength="30" ng-readonly="readOnly">
															<div ng-messages="forms.dataSourceForm.driver.$error" ng-show="!selectedDataSource.jdbcPoolConfiguration.maxWait">
				          										<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
				        									</div>
														</md-input-container>
														<md-icon ng-click="showMaxWaitInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  			
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<label>{{translate.load("sbi.datasource.type.jdbc.abandonedTimeout")}}</label>
															<input required type="number" ng-change="setDirty()" ng-model="selectedDataSource.jdbcPoolConfiguration.abandonedTimeout"
																ng-maxlength="20" ng-readonly="readOnly">
															<div ng-messages="forms.dataSourceForm.driver.$error" ng-show="!selectedDataSource.jdbcPoolConfiguration.abandonedTimeout">
				          										<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
				        									</div>
														</md-input-container>
														<md-icon ng-click="showAbandonedTimeoutInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  			
									  				<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<label>{{translate.load("sbi.datasource.type.jdbc.timeBetweenEvictionRuns")}}</label>
															<input required  type="number" ng-change="setDirty()" ng-model="selectedDataSource.jdbcPoolConfiguration.timeBetweenEvictionRuns"
																ng-maxlength="30" ng-readonly="readOnly">
															<div ng-messages="forms.dataSourceForm.driver.$error" ng-show="!selectedDataSource.jdbcPoolConfiguration.timeBetweenEvictionRuns">
				          										<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
				        									</div>
														</md-input-container>
														<md-icon ng-click="showTimeBetweenEvictionRunsInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  			
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<label>{{translate.load("sbi.datasource.type.jdbc.maxIdle")}}</label>
															<input type="number" ng-init="selectedDataSource.jdbcPoolConfiguration.maxIdle = 30" ng-change="setDirty()" ng-model="selectedDataSource.jdbcPoolConfiguration.maxIdle"
																ng-maxlength="30" ng-readonly="readOnly">
														</md-input-container>
														<md-icon ng-click="showMaxIdleInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  			
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<label>{{translate.load("sbi.datasource.type.jdbc.minEvictableIdleTimeMillis")}}</label>
															<input type="number" ng-change="setDirty()" ng-model="selectedDataSource.jdbcPoolConfiguration.minEvictableIdleTimeMillis"
																ng-maxlength="30" ng-readonly="readOnly">
															<div ng-messages="forms.dataSourceForm.driver.$error" ng-show="!selectedDataSource.jdbcPoolConfiguration.minEvictableIdleTimeMillis">
				          										<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
				        									</div>
														</md-input-container>
														<md-icon ng-click="showminEvictableIdleTimeMillisInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  			
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<label>{{translate.load("sbi.datasource.type.jdbc.validationQuery")}}</label>
															<input type="text" ng-change="setDirty()" ng-model="selectedDataSource.jdbcPoolConfiguration.validationQuery"
																ng-maxlength="200" ng-readonly="readOnly">														
														</md-input-container>
														<md-icon ng-click="showValidationQueryInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  			
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<label>{{translate.load("sbi.datasource.type.jdbc.validationQueryTimeout")}}</label>
															<input type="number" ng-change="setDirty()" ng-model="selectedDataSource.jdbcPoolConfiguration.validationQueryTimeout"
																ng-maxlength="30" ng-readonly="readOnly">														
														</md-input-container>
														<md-icon ng-click="showValidationQueryTimeoutInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  			
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<md-checkbox ng-disabled="readOnly" ng-change="setDirty()"  ng-model="selectedDataSource.jdbcPoolConfiguration.removeAbandonedOnBorrow" aria-label="Remove Abandoned On Borrow">{{translate.load("sbi.datasource.removeAbandonedOnBorrow")}}</md-checkbox>
														</md-input-container>
														<md-icon ng-click="showRemoveAbandonedOnBorrowInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  			
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<md-checkbox ng-disabled="readOnly" ng-change="setDirty()"  ng-model="selectedDataSource.jdbcPoolConfiguration.removeAbandonedOnMaintenance" aria-label="Remove Abandoned On Maintenance">{{translate.load("sbi.datasource.removeAbandonedOnMaintenance")}}</md-checkbox>
														</md-input-container>
														<md-icon ng-click="showRemoveAbandonedOnMaintenanceInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  			
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<md-checkbox ng-disabled="readOnly" ng-change="setDirty()"  ng-model="selectedDataSource.jdbcPoolConfiguration.logAbandoned" aria-label="Log Abandoned">{{translate.load("sbi.datasource.logAbandoned")}}</md-checkbox>
														</md-input-container>
														<md-icon ng-click="showGetLogAbandonedInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  			
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<md-checkbox ng-disabled="readOnly" ng-change="setDirty()"  ng-model="selectedDataSource.jdbcPoolConfiguration.testOnReturn" aria-label="Test On Return">{{translate.load("sbi.datasource.testOnReturn")}}</md-checkbox>
														</md-input-container>
														<md-icon ng-click="showTestOnReturnInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  			
										  			<div layout="row">
										  				<md-input-container flex="90" class="md-block">
															<md-checkbox ng-disabled="readOnly" ng-change="setDirty()"  ng-model="selectedDataSource.jdbcPoolConfiguration.testWhileIdle" aria-label="Test While Idle">{{translate.load("sbi.datasource.testWhileIdle")}}</md-checkbox>
														</md-input-container>
														<md-icon ng-click="showTestWhileIdleInfo($event)" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
										  			</div>
										  		</md-card-content>
										  </md-card>
										  </div>										  
										</div>

									

									<!-- JNDI -->
									<div flex=100 layout="row" ng-if= "jdbcOrJndi.type == 'JNDI'" >
										<md-input-container flex="90">
											<label>{{translate.load("sbi.datasource.type.jndi.name")}}</label>
											<input ng-model="selectedDataSource.jndi" ng-readonly="readOnly"  ng-required="jdbcOrJndi.type == 'JNDI'">
											<div ng-messages="forms.dataSourceForm.jndi.$error" ng-show="!selectedDataSource.jndi">
				          								<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
				        							</div>
										</md-input-container>
										<md-icon ng-click="showJdniInfo()" md-font-icon="fa fa-info-circle fa-lg" flex="10"></md-icon>
									</div>
									</md-card-content>
								</md-card>
							</form>

			</detail>

	</angular-list-detail>
</body>
</html>
