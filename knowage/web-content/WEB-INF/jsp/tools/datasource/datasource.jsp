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
<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<%
	Boolean superadmin =(Boolean)((UserProfile)userProfile).getIsSuperadmin();
%>

<script>
	var superadmin = <%= superadmin %>;
</script>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="dataSourceModule">

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<!-- Styles -->
	<link rel="stylesheet" type="text/css"	href="/knowage/themes/commons/css/customStyle.css">
	<!-- JavaScript -->
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/datasource/datasource.js"></script>
</head>

<body class="bodyStyle" ng-controller="dataSourceController as ctrl">
	<angular-list-detail  show-detail="showMe">

	 	<list label='translate.load("sbi.ds.dataSource")' new-function="createNewDatasource">
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
							<form name="dataSourceForm"  ng-disabled="readOnly"ng-submit="dataSourceForm.$valid && saveOrUpdateDataSource()" class="detailBody mozSize">
								<md-card layout-padding>

									<!-- LABEL -->
									<div layout="row" layout-wrap>
											<div flex=100>
												<md-input-container class="small counter">
													<label>{{translate.load("sbi.ds.label")}}</label>
													<input ng-model="selectedDataSource.label" required ng-change="setDirty()"  ng-maxlength="100" ng-readonly="readOnly">
													<div ng-messages="dataSourceForm.label.$error" ng-show="!selectedDataSource.label">
														<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
													</div>
												</md-input-container>
											</div>
									</div>

									<!-- DESCRIPTION -->
									<div layout="row" layout-wrap>
										<div flex=100>
											<md-input-container class="small counter">
												<label>{{translate.load("sbi.ds.description")}}</label>
												<input ng-model="selectedDataSource.descr"
													ng-change="setDirty()" ng-maxlength="160" ng-readonly="readOnly">
											</md-input-container>
										</div>
									</div>

									<!-- DIALECT -->
									<div layout="row" layout-wrap>
											<div flex=100>
												<md-input-container class="small counter" >
													<label>{{translate.load("sbi.datasource.dialect")}}</label>
													<md-select  ng-disabled="readOnly" ng-change="setDirty()"  aria-label="aria-label"	ng-model="selectedDataSource.dialectId" >
														<md-option	required ng-repeat="d in dialects" value="{{d.VALUE_ID}}">{{d.VALUE_DS}} </md-option>
													</md-select>
													<div ng-messages="dataSourceForm.dialectId.$error" ng-show="!selectedDataSource.dialectId">
			          							<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
			        						</div>
												</md-input-container>
											</div>
									</div>

									<!-- MULTICHEMA -->
									<div layout="row" layout-wrap style="margin-bottom:-19px;">
										<label style="padding-top:7px;">{{translate.load("sbi.datasource.multischema")}}:</label>
										<md-input-container class="small counter">
											<md-checkbox	ng-disabled="readOnly" ng-change="setDirty()"  ng-model="selectedDataSource.multiSchema" aria-label="Multischema"></md-checkbox>
										</md-input-container>
									</div>

									<!-- SCHEMA ATTRIBUTE -->
									<div ng-show= "selectedDataSource.multiSchema == true " layout="row" layout-wrap>
										<div flex=100>
											<md-input-container class="small counter">
											<label>{{translate.load("sbi.datasource.multischema.attribute")}}</label>
											<input ng-change="setDirty()"  ng-model="selectedDataSource.schemaAttribute"
												ng-maxlength="45" ng-disabled="readOnly"> </md-input-container>
										</div>
									</div>

									<!-- READ ONLY -->
									<div layout="row" layout-wrap>
										<md-radio-group ng-disabled="readOnly" ng-change="setDirty()"  ng-model="selectedDataSource.readOnly"> Read only:
					      					<md-radio-button  value="1" ng-disabled="selectedDataSource.writeDefault">Read only</md-radio-button>
					      					<md-radio-button  value="0"> Read and write </md-radio-button>
					    				</md-radio-group>
									</div>

									<!-- WRITE DEFAULT -->
									<div layout="row" layout-wrap style="margin-bottom:-19px;">
										<label style="padding-top:7px;">{{translate.load("sbi.datasource.writedefault")}}:</label>
										<md-input-container class="small counter">
											<md-checkbox ng-disabled="readOnly" ng-change="setDirty()"
												ng-model="selectedDataSource.writeDefault" ng-disabled="(selectedDataSource.readOnly == 1) || !isSuperAdminFunction()" aria-label="Write Default">
											</md-checkbox>
										</md-input-container>
									</div>

									<!-- TYPE -->
									<div layout="row" layout-wrap>
										<md-radio-group   ng-model="jdbcOrJndi.type" ng-change="clearType()"> Type:
					      					<md-radio-button value="JDBC" ng-disabled="readOnly">JDBC</md-radio-button>
					      					<md-radio-button value="JNDI" ng-disabled="!isSuperAdminFunction() || readOnly">JNDI</md-radio-button>
					    				</md-radio-group>
									</div>

									<!-- JDBC -->
									<div ng-if= "jdbcOrJndi.type == 'JDBC'">

										<!-- URL -->
										<div layout="row" layout-wrap>
											<div flex=100>
												<md-input-container class="small counter">
													<label>{{translate.load("sbi.datasource.type.jdbc.url")}}</label>
													<input ng-change="setDirty()"  ng-model="selectedDataSource.urlConnection" required
														ng-maxlength="500" ng-readonly="readOnly">
													<div ng-messages="forms.dataSourceForm.urlConnection.$error" ng-show="!selectedDataSource.urlConnection">
				          								<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
				        							</div>
												</md-input-container>
											</div>
										</div>

										<!-- USER -->
										<div layout="row" layout-wrap>
											<div flex=100>
												<md-input-container class="small counter">
												<label>{{translate.load("sbi.datasource.type.jdbc.user")}}</label>
												<input ng-change="setDirty()"  ng-model="selectedDataSource.user"
													ng-maxlength="50" ng-readonly="readOnly"> </md-input-container>
											</div>
										</div>

										<!-- PASSWORD -->
										<div layout="row" layout-wrap>
											<div flex=100>
												<md-input-container class="small counter">
												<label>{{translate.load("sbi.datasource.type.jdbc.password")}}</label>
												<input ng-change="setDirty()"  type="password" name="password" ng-model="selectedDataSource.pwd"
													ng-maxlength="50" ng-readonly="readOnly"> </md-input-container>
											</div>
										</div>

										<!-- DRIVER -->
										<div layout="row" layout-wrap>
											<div flex=100>
												<md-input-container class="small counter">
													<label>{{translate.load("sbi.datasource.driver")}}</label>
													<input ng-change="setDirty()"  ng-model="selectedDataSource.driver" required
														ng-maxlength="160" ng-readonly="readOnly">
													<div ng-messages="forms.dataSourceForm.driver.$error" ng-show="!selectedDataSource.driver">
				          								<div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
				        							</div>
												</md-input-container>
											</div>
										</div>

									</div>

									<!-- JNDI -->
									<div layout="row" layout-wrap ng-if= "jdbcOrJndi.type != 'JDBC'">
										<md-input-container class="small counter" flex=95>
											<label>{{translate.load("sbi.datasource.type.jndi.name")}}</label>
											<input ng-model="selectedDataSource.jndi" ng-readonly="readOnly">
										</md-input-container>
										<md-icon ng-click="showJdniInfo()" md-font-icon="fa fa-info-circle fa-lg"></md-icon>
									</div>
								</md-card>
							</form>

			</detail>

	</angular-list-detail>
</body>
</html>
