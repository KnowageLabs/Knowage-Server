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

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="exportersCatalogueModule">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>


<link rel="stylesheet" type="text/css"    href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/catalogues/exportersCatalogue.js")%>"></script>


</head>
<body class="bodyStyle kn-exportersMenu" ng-controller="exportersCatalogueController as ctrl" >
    
   
    
	<angular-list-detail  show-detail="showMe">
		 	<list label='translate.load("sbi.exporters.title")' new-function="createExporters"> 	
				
			   <div ng-show=false ng-repeat="exporter in myExporters"></div>
               
               <angular-table  
				        flex
 						id="exportersList"
 						ng-model="myExporters" 
 						columns='exportersListColumns'
 						columns-search='["engineLabel","domainLabel"]'
 						highlights-selected-item=true
 						show-search-bar=true
 						speed-menu-option='exporterSpeedMenu'
 						click-function="loadExporter(item)">
 		      </angular-table>
 		    
 		   
 		    
	</list> 
	<detail label='(selectedExporter.exporterName==undefined)? "" : selectedExporter.exporterName'  save-function="saveOrUpdateExporter"
		cancel-function="cancel"		
		show-save-button="showMe" show-cancel-button="showMe"
		disable-save-button="!exportersForm.$valid">
		
			<md-card>
				<md-card-content>
					<div class="kn-info">{{translate.load("sbi.catalogues.exporters.info")}}</div>
					<form name="exportersForm" ng-submit="exportersForm.$valid && saveOrUpdateExporter()" layout="column">
					
						<md-input-container ng-show="showMe" class="firstSelection" >
						<label>{{translate.load("sbi.catalogues.exporters.engine")}}</label>
				         <md-select ng-required="true" class="exporters"
				         	ng-model="selectedExporter.engineId" 
				         	placeholder={{translate.load("sbi.exporters.select.engine")}}
				         	md-on-close="clearEngineSearch()">
				         	  <md-select-header>
							        <md-input-container class="md-block">
							          <input
							            type="search"
							            ng-model="searchEngineText"
							            ng-keydown="$event.stopPropagation()"
							            md-autofocus
							            placeholder="Search..">
							          </md-input-container>				        
							  </md-select-header>
				 		      <md-option ng-value="engine.id" ng-repeat="engine in engines | orderBy : 'name' | filter:searchEngineText">{{engine.name}}</md-option>
				 		 </md-select>
				 		 <div ng-messages="exportersForm.engineId.$error" ng-show="!selectedExporter.engineId">
						      <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
						 </div>
				 	   </md-input-container>
				 		 
				 	   <md-input-container ng-show="showMe">
				 	   <label>{{translate.load("sbi.catalogues.exporters.exporter")}}</label>
					 	 <md-select ng-required="true" class="exporters"
					 	 	ng-model="selectedExporter.domainId" 
					 	 	placeholder={{translate.load("sbi.exporters.select.domain")}}
					 	 	md-on-close="clearDomainSearch()">
					 	 		<md-select-header>
							        <md-input-container class="md-block">
							          <input
							            type="search"
							            ng-model="searchDomainText"
							            ng-keydown="$event.stopPropagation()"
							            md-autofocus
							            placeholder="Search..">
							          </md-input-container>				        
							    </md-select-header>
							    <md-option  ng-value="domain.valueId" ng-repeat="domain in domains | orderBy : 'valueCd' | filter:searchDomainText">{{domain.valueCd}}</md-option>
					 	 </md-select>
					 		 <div ng-messages="exportersForm.domainId.$error" ng-show="!selectedExporter.domainId">
						          <div ng-message="required">{{translate.load("sbi.catalogues.generic.reqired")}}</div>
						     </div>
				       </md-input-container>
					</form>	
				</md-card-content>
			</md-card>
		   	
	</detail>	
	</angular-list-detail>
</body>
</html>
