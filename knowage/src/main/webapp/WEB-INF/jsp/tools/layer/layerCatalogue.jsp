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
<html ng-app="layerWordManager">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- Styles -->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/layer/layerCatalogue.js")%>"></script>

<script type="text/ng-template" id="dialog1.tmpl.html">
<md-dialog aria-label="Select type of download"  ng-cloak>
  <form>
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h1>Select type of download</h1>
        <span flex></span>
        <md-button class="md-icon-button" ng-click="closeFilter()">
          <md-icon md-font-icon="fa fa-times closeIcon" aria-label="Close dialog"></md-icon>
        </md-button>
      </div>
    </md-toolbar>
    <md-dialog-content >
     <div class="md-dialog-content">
		 <md-radio-group ng-show="isWFS" ng-model="typeWFS">
     		 <md-radio-button  value="geojson" >GeoJSON</md-radio-button>
     		 <md-radio-button  value="kml"> KML </md-radio-button>
      		<md-radio-button  value="shp">SHAPEFILE</md-radio-button>
   		 </md-radio-group>
		<md-radio-group ng-show="!isWFS" ng-model="typeWFS">
     		 <md-radio-button value="geojson" class="md-primary">GeoJSON</md-radio-button>
   		 </md-radio-group>
     </div>
    
 <md-dialog-actions layout="row">
      <span flex></span>
      <md-button class="dialogButton" ng-click="getDownload(selectedLayer)" md-autofocus>Download</md-button>
	
    </md-dialog-actions>
   	 </md-dialog-content>
  </form>
</md-dialog>
</script>
</head>


	<body class="bodyStyle kn-layerCatalogue">
	
		<angular-list-detail ng-controller="Controller"
			 show-detail="showme">
			<list label='translate.load("sbi.layercatalogue")' new-function="loadLayerList"> 
				<angular-table 
					flex
					id='layerlist' 
					ng-model=layerList
					columns='[{"label":"Name","name":"name"},{"label":"Type","name":"type","size":"60px"},{"label":" ","name":"icon","size":"30px"}]'
					columns-search='["name","type"]' 
					show-search-bar=true
					highlights-selected-item=true 
					click-function="loadLayerList(item);"
					speed-menu-option=menuLayer 
					scope-functions=tableFunction> </angular-table>
			</list> 
			<detail label='selectedLayer.label==undefined? "" : selectedLayer.label'  save-function="saveLayer"
				cancel-function="cancel"
				disable-save-button="!forms.contactForm.$valid"
				show-save-button="showme" show-cancel-button="showme">
	
				<div layout-fill class="containerDiv">
					<form name="forms.contactForm" layout-fill id="layerform" class="detailBody mozSize md-whiteframe-z1" novalidate>
						<md-tabs md-select="Layer" class="mozScroll hideTabs h100"	md-border-bottom> 
							<md-tab label="Layer" md-on-select="setTab('Layer')" md-active="isSelectedTab('Layer')">
								<md-content flex layout-fill class=" ToolbarBox miniToolbar noBorder mozTable ">
								
									<md-card layout-padding>
										<div flex="100">
											<md-input-container class="md-block"> 
												<label>{{translate.load("sbi.glossary.category")}}</label>
												<md-select aria-label="aria-label" ng-model="selectedLayer.category_id">
													<md-option ng-repeat="ct in category" value="{{ct.VALUE_ID}}">{{ct.VALUE_NM}}</md-option>
												</md-select> 
											</md-input-container>
										</div>
										<div flex=100>
											<md-input-container class="md-block" class="small counter">
												<label>{{translate.load("sbi.behavioural.lov.details.label")}}</label>
												<input class="input_class" ng-model="selectedLayer.label" required	maxlength="100" ng-maxlength="100" md-maxlength="100"> 
											</md-input-container>
										</div>
										<div flex=100>
											<md-input-container class="md-block"> <label>{{translate.load("sbi.behavioural.lov.details.name")}}</label>
											<input class="input_class" ng-model="selectedLayer.name" required
												maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
										</div>
										<div flex=100>
											<md-input-container class="md-block"> 
												<label>{{translate.load("sbi.tools.layer.props.descprition")}}</label>
												<input class="input_class" ng-model="selectedLayer.descr" maxlength="100" ng-maxlength="100" md-maxlength="100"> 
											</md-input-container>
										</div>
										<md-input-container class="md-block"> 
											<md-checkbox ng-model="selectedLayer.baseLayer" aria-label="BaseLayer">{{translate.load("sbi.tools.layer.baseLayer")}}</md-checkbox> 
										</md-input-container>
	
										<div layout="row">
											<div layout="column" flex>
												<div flex>
													<md-input-container class="md-block"> 
														<label>{{translate.load("sbi.tools.layer.props.type")}}</label>
														<md-select
															placeholder='{{translate.load("sbi.generic.select")}} {{translate.load("sbi.tools.layer.props.type")}}'
															ng-required="isRequired" ng-mouseleave="isRequired=true"
															ng-show="flagtype" aria-label="aria-label"
															ng-model="selectedLayer.type" ng-change=""> <md-option
															ng-repeat="type in listType" value="{{type.value}}">{{type.label}}</md-option>
														</md-select> 
													</md-input-container>
												</div>
												<div flex style="margin-top: 0px; margin-left: 15px;">
													<md-select-label ng-show="!flagtype">{{selectedLayer.type}}</md-select-label>
												</div>
											</div>
											<div ng-if="selectedLayer.type == 'File'" flex="50">
												<file-upload  flex id="layerFile" ng-model="selectedLayer.layerFile"  ng-init ="selectedLayer.layerFile = {}" file-max-size="fileMaxSize"></file-upload>
											</div>
											<span flex></span>
										</div>
										<div layout="row" layout-wrap ng-show="pathFileCheck">
											<p>
												{{translate.load("sbi.layer.pathfile")}}: <b>{{selectedLayer.pathFile}}</b>
											</p>
										</div>
										<div flex=100>
											<md-input-container class="md-block"> <label>{{translate.load("sbi.tools.layer.props.label")}}</label>
												<input class="input_class" ng-model="selectedLayer.layerLabel" required maxlength="100" ng-maxlength="100" md-maxlength="100">
											</md-input-container>
										</div>
										<div flex=100>
											<md-input-container class="md-block"> <label>{{translate.load("sbi.tools.layer.props.name")}}</label>
												<input class="input_class" ng-model="selectedLayer.layerName" required maxlength="100" ng-maxlength="100" md-maxlength="100">
											</md-input-container>
										</div>
										<div flex=100>
											<md-input-container class="md-block"> <label>{{translate.load("sbi.tools.layer.props.id")}}</label>
												<input class="input_class" ng-model="selectedLayer.layerIdentify" required maxlength="100" ng-maxlength="100" md-maxlength="100">
											</md-input-container>
										</div>
										<div flex=100>
											<md-input-container class="md-block"> <label>{{translate.load("sbi.tools.layer.props.order")}}</label>
												<input class="input_class" ng-model="selectedLayer.layerOrder" required type="number" min="0"> </md-input-container>
										</div>
										<br>
	
										<div layout="row" layout-wrap ng-if="selectedLayer.type == 'WFS' || selectedLayer.type == 'WMS' || selectedLayer.type == 'TMS' ">
											<div flex=100>
												<md-input-container class="md-block"> <label>{{translate.load("sbi.tools.layer.props.url")}}</label>
													<input class="input_class" placeholder="Es:http://www.google.it" type="url" ng-model="selectedLayer.layerURL" required	maxlength="500" ng-maxlength="500" md-maxlength="500"> 
												</md-input-container>
											</div>
										</div>
										<div layout="row" layout-wrap ng-if="selectedLayer.type == 'Google' || selectedLayer.type == 'WMS' || selectedLayer.type == 'TMS' ">
											<div flex=100>
												<md-input-container class="md-block"> <label>{{translate.load("sbi.tools.layer.props.options")}}</label>
												<input class="input_class" ng-model="selectedLayer.layerOptions"
													maxlength="100" ng-maxlength="100" md-maxlength="100">
												</md-input-container>
											</div>
										</div>
										<div layout="row" layout-wrap ng-if="selectedLayer.type == 'WMS'">
											<div flex=100>
												<md-input-container class="md-block"> <label>{{translate.load("sbi.tools.layer.props.params")}}</label>
												<input class="input_class" ng-model="selectedLayer.layerParams"
													maxlength="100" ng-maxlength="100" md-maxlength="100">
												</md-input-container>
											</div>
										</div>
							
										<div layout="row" layout-wrap flex>
											<div flex="50" ng-repeat="rl in roles">
												<md-checkbox ng-checked="exists(rl, rolesItem)"
													ng-click="toggle(rl, rolesItem)"> {{ rl.name }} </md-checkbox>
							
											</div>
										</div>
									</md-card>
				
								</md-content>
							</form>
						</md-tab>
						
						<md-tab label="Filter" md-on-select="setTab('Filter')" ng-if="showFilters" md-active="isSelectedTab('Filter')">
							<div layout="row">
								<md-card flex>	
									<md-toolbar class="secondaryToolbar">
										<div class="md-toolbar-tools" layout="row" layout-wrap >
											<h2>{{translate.load("sbi.layerfilter");}}</h2>
										</div>
									</md-toolbar>
									<md-card-content class="noPadding">
										<div layout="row" layout-align="center" ng-if="!filter || filter.length==0">
											<div class="kn-noItems">
												{{translate.load("kn.tools.layer.nofilters");}}
											</div>
										</div>
										<md-input-container class="md-icon-float md-block" style="margin-left:8px; margin-right:8px;" ng-show="filter.length>0">
											<label>{{translate.load("sbi.general.search")}}</label>
											<md-icon md-font-icon="fa fa-search"></md-icon>
											<input ng-model="filterSearch" type="text">
									    </md-input-container>
										<md-list ng-if="filter.length>0" class="kn-custom-list">
											<md-list-item ng-click="addFilter(f)" ng-repeat="f in filter | filter:filterSearch">
											    <p> {{ f.property }} </p>
											    <md-icon md-font-icon="fa fa-arrow-right"></md-icon>
											    <md-divider></md-divider>
										  	</md-list-item>
										</md-list>
									</md-card-content>
								</md-card>
								
								<md-card flex>	
									<md-toolbar class="secondaryToolbar">
										<div class="md-toolbar-tools" layout="row" layout-wrap >
											<h2>{{translate.load("sbi.layerfilteradded");}}</h2>
										</div>
									</md-toolbar>
									<md-card-content class="noPadding">
										<div layout="row" layout-align="center" ng-if="!filter_set || filter_set.length==0">
											<div class="kn-noItems">
												{{translate.load("kn.tools.layer.nofiltersadded");}}
											</div>
										</div>
										<md-input-container ng-show="filter_set.length>0" class="md-icon-float md-block" style="margin-left:8px; margin-right:8px;">
											<label>{{translate.load("sbi.general.search")}}</label>
											<md-icon md-font-icon="fa fa-search"></md-icon>
											<input ng-model="filterFsSearch" type="text">
									    </md-input-container>
										<md-list ng-if="filter_set.length>0" class="kn-custom-list">
											<md-list-item ng-click="removeFilter(fs)" ng-repeat="fs in filter_set | filter:filterFsSearch">
												<md-icon md-font-icon="fa fa-arrow-left"></md-icon>
											    <p> {{ fs.property }} </p>
											    <md-divider></md-divider>
										  	</md-list-item>
										</md-list>
									</md-card-content>
								</md-card>
							
							</div>
						</md-tab>
					</md-tabs>
				</div>
	
			</detail> 
		</angular-list-detail>
	</body>
</html>
