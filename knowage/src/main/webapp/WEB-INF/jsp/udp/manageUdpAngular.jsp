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


<%@ page language="java" pageEncoding="UTF-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!doctype html>
<html ng-app="udpManagementApp">

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/udp/manageUdp.js")%>"></script>

	<link rel="stylesheet" type="text/css"   href="<%=urlBuilder.getResourceLinkByTheme(request, "/themes/commons/css/customStyle.css", currTheme)%>">
		
</head>

<body>
	<div ng-controller="Controller as ctrl" layout-wrap layout-fill>
		<angular-list-detail layout-column show-detail="showMe">
			<list label="translate.load('sbi.udp.udpManagement')" new-function="addUdp" >
				 <angular-table flex
						id="table" ng-model="data" 
						columns='["label","name"]'
						columns-search='["label","name"]'
						highlights-selected-item = "true"
						show-search-bar="true"
						no-pagination="true"
						selected-item="itemSelected"
						click-function = "copyRowInForm(item,cell,listId)"
						speed-menu-option="udpSpeedMenu"
					></angular-table>
			</list>
			<detail label="property == undefined ? '' : translate.load('sbi.generic.details')" save-function="saveProperty" cancel-function="resetForm" disable-save-button="udpForm.$invalid"
				show-save-button="showMe" show-cancel-button="showMe">
				 <form name="udpForm">
				 	<md-card>
				 		<md-card-content layout-wrap layout="row">
				 			<div flex="100">
							<md-input-container class="md-block">
			  					<label>{{translate.load("sbi.udp.label")}}</label>
			  					<input ng-model="property.label" required  type="text" ng-change="checkChange()">
			  				</md-input-container>
			  				</div>
			  				<div flex="100">
			  				<md-input-container class="md-block">	
			  					<label>{{translate.load("sbi.udp.name")}}</label>
			  					<input ng-model="property.name" required  type="text" ng-change="checkChange()">
							</md-input-container>
							</div>
	
							<div flex="100">
							<md-input-container class="md-block">	
			  					<label>{{translate.load("sbi.udp.description")}}</label>
			  					<textarea ng-model="property.description" md-maxlength="2500" ng-change="checkChange()"></textarea>
							</md-input-container>
							</div>
							<div flex="100">
							<md-input-container class="md-block">
						       <md-checkbox ng-model="property.multivalue" aria-label="Checkbox 1" ng-change="checkChange()">
						         {{translate.load("sbi.udp.multivalue")}}
						     	</md-checkbox>
						     </md-input-container>
							</div>
						     <md-input-container flex="50" class="md-block">
						     	<label>{{translate.load('sbi.udp.type')}}</label>
						        <md-select flex placeholder="{{translate.load('sbi.udp.type')}}" ng-model="property.type" required ng-change="checkChange()">
								   <md-option value="Boolean">Boolean</md-option>
								   <md-option value="Text">Text</md-option>
								   <md-option value="Integer">Integer</md-option>
								</md-select>
								</md-input-container>
		 			     	 <md-input-container flex="50" class="md-block">
		 			     	 	<label>{{translate.load('sbi.udp.family')}}</label>
						        <md-select flex placeholder="{{translate.load('sbi.udp.family')}}" ng-model="property.family" required ng-change="checkChange()">
								   <md-option value="Model">Model</md-option>
								   <md-option value="Kpi">Kpi</md-option>
								   <md-option value="Glossary">Glossary</md-option>
			  					</md-select>
		  					 </md-input-container>
	  					 </md-card-content>
  					 </md-card>
				 </form>
			</detail>
		</angular-list-detail>
	</div>
</body>
</html>
