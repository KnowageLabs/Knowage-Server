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
    
	<div class="footer">
	<md-button class="dialogButton" ng-click="getDownload(selectedLayer)" md-autofocus>Download <md-icon md-font-icon="fa fa-download buttonIcon" aria-label="Download"></md-icon></md-button>
	</div>
   	 </md-dialog-content>
  </form>
</md-dialog>
</script>
</head>


<body class="bodyStyle kn-layerCatalogue">


	<angular-list-detail ng-controller="Controller"
		 show-detail="showme">
	<list label='translate.load("sbi.layercatalogue")' new-function="loadLayerList"> 
	<angular-table flex
		id='layerlist' ng-model=layerList
		columns='[{"label":"Name","name":"name"},{"label":"Type","name":"type","size":"60px"},{"label":" ","name":"icon","size":"30px"}]'
		columnsSearch='["name","type", "layerURL"]' show-search-bar=true
		highlights-selected-item=true click-function="loadLayerList(item);"
		speed-menu-option=menuLayer scope-functions=tableFunction> </angular-table>

	</list> 
	<detail label='selectedLayer.label==undefined? "" : selectedLayer.label'  save-function="saveLayer"
		cancel-function="cancel"
		disable-save-button="!forms.contactForm.$valid"
		show-save-button="showme" show-cancel-button="showme">

	<div layout-fill class="containerDiv">
		<!-- 				ng-submit="forms.contactForm.$valid && saveLayer()" -->
		<form name="forms.contactForm" layout-fill id="layerform"
			class="detailBody mozSize md-whiteframe-z1" novalidate>

			<md-tabs md-select="Layer" class="mozScroll hideTabs h100"
				md-border-bottom> 
			<md-tab label="Layer"
				md-on-select="setTab('Layer')" md-active="isSelectedTab('Layer')">
			<md-content flex layout-fill
				class=" ToolbarBox miniToolbar noBorder mozTable ">
			<md-card>
			<div layout="row" layout-wrap>
				
				<div flex=25>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.glossary.category")}}</label>
					<md-select aria-label="aria-label" ng-model="selectedLayer.category_id">
						<md-option ng-repeat="ct in category" value="{{ct.VALUE_ID}}">{{ct.VALUE_NM}}</md-option>
					</md-select> </md-input-container>
				</div>
			</div>

			<div layout="row" layout-wrap>
				<!--<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-bookmark"></md-icon>
					</div>
					-->
				<div flex=100>
					<md-input-container class="small counter" class="small counter">
					<label>{{translate.load("sbi.behavioural.lov.details.label")}}</label>
					<input class="input_class" ng-model="selectedLayer.label" required
						maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
				</div>
			</div>
			<div layout="row" layout-wrap>
				<!--<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-pencil-square-o"></md-icon>
					</div>
					  -->
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.behavioural.lov.details.name")}}</label>
					<input class="input_class" ng-model="selectedLayer.name" required
						maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
				</div>
			</div>
			<div layout="row" layout-wrap>
				<!--<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-file-text-o"></md-icon>
					</div>
					  -->
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.tools.layer.props.descprition")}}</label>
					<input class="input_class" ng-model="selectedLayer.descr"
						maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
				</div>
			</div>

			<div layout="row" layout-wrap>
				<md-input-container class="small counter"> <md-checkbox
					ng-model="selectedLayer.baseLayer" aria-label="BaseLayer">
					{{translate.load("sbi.tools.layer.baseLayer")}}
				</md-checkbox> </md-input-container>

			</div>
			<div layout="row" layout-wrap>
				<!-- 
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-caret-square-o-down"></md-icon>
					</div>
					 -->
				<div flex=25>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.tools.layer.props.type")}}</label>
					<md-select
						placeholder='{{translate.load("sbi.generic.select")}} {{translate.load("sbi.tools.layer.props.type")}}'
						ng-required="isRequired" ng-mouseleave="isRequired=true"
						ng-show="flagtype" aria-label="aria-label"
						ng-model="selectedLayer.type" ng-change=""> <md-option
						ng-repeat="type in listType" value="{{type.value}}">{{type.label}}</md-option>
					</md-select> </md-input-container>
				</div>
			</div>
			<div style="margin-top: 0px; margin-left: 15px;">
				<md-select-label ng-show="!flagtype">{{selectedLayer.type}}</md-select-label>
			</div>
			<div layout="row" layout-wrap>
				<!-- 
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-bookmark"></md-icon>
					</div>
					 -->

				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.tools.layer.props.label")}}</label>
					<input class="input_class" ng-model="selectedLayer.layerLabel"
						required maxlength="100" ng-maxlength="100" md-maxlength="100">
					</md-input-container>
				</div>
			</div>
			<div layout="row" layout-wrap>
				<!--  
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-pencil-square-o"></md-icon>
					</div>
					-->
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.tools.layer.props.name")}}</label>
					<input class="input_class" ng-model="selectedLayer.layerName"
						required maxlength="100" ng-maxlength="100" md-maxlength="100">
					</md-input-container>
				</div>
			</div>
			<div layout="row" layout-wrap>
				<!--
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-tag"></md-icon>
					</div>
					  -->
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.tools.layer.props.id")}}</label>
					<input class="input_class" ng-model="selectedLayer.layerIdentify"
						required maxlength="100" ng-maxlength="100" md-maxlength="100">
					</md-input-container>
				</div>
			</div>

			<div layout="row" layout-wrap>
				<!-- 
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-spinner"></md-icon>
					</div> -->
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.tools.layer.props.order")}}</label>
					<input class="input_class" ng-model="selectedLayer.layerOrder"
						required type="number" min="0"> </md-input-container>
				</div>
			</div>
			<br>
			<div layout="row" layout-wrap ng-show="pathFileCheck">
				<p>
					{{translate.load("sbi.layer.pathfile")}}: <b>{{selectedLayer.pathFile}}</b>
				</p>
			</div>

			<!-- inizio campi variabili -->
			<div layout="row" layout-wrap ng-if="selectedLayer.type == 'File'">
				<!--<div flex=3 style="margin-top: 10px;">
						<md-icon md-font-icon="fa fa-upload"></md-icon>
					</div>
					  -->
				<div flex=100>
					<md-input-container class="small counter"> <input
						id="layerFile" ng-model="selectedLayer.layerFile" type="file"
						fileread="selectedLayer.layerFile" accept=".json"> </md-input-container>

				</div>
			</div>

			<div layout="row" layout-wrap
				ng-if="selectedLayer.type == 'WFS' || selectedLayer.type == 'WMS' || selectedLayer.type == 'TMS' ">
				<!--<div flex=3 style="margin-top: 25px;">
						<md-icon md-font-icon="fa fa-link"></md-icon>
					</div>
					 -->
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.tools.layer.props.url")}}</label>
					<input class="input_class" placeholder="Es:http://www.google.it"
						type="url" ng-model="selectedLayer.layerURL" required
						maxlength="500" ng-maxlength="500" md-maxlength="500"> </md-input-container>


				</div>
			</div>
			<div layout="row" layout-wrap
				ng-if="selectedLayer.type == 'Google' || selectedLayer.type == 'WMS' || selectedLayer.type == 'TMS' ">
				<!--<div flex=3 style="margin-top: 25px;">
						<md-icon md-font-icon="fa fa-cogs"></md-icon>
					</div>  -->
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.tools.layer.props.options")}}</label>
					<input class="input_class" ng-model="selectedLayer.layerOptions"
						required maxlength="100" ng-maxlength="100" md-maxlength="100">
					</md-input-container>
				</div>
			</div>
			<div layout="row" layout-wrap ng-if="selectedLayer.type == 'WMS'">
				<!--<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-ellipsis-v"></md-icon>
					</div>  -->
				<div flex=100>
					<md-input-container class="small counter"> <label>{{translate.load("sbi.tools.layer.props.params")}}</label>
					<input class="input_class" ng-model="selectedLayer.layerParams"
						required maxlength="100" ng-maxlength="100" md-maxlength="100">
					</md-input-container>
				</div>
			</div>


			<div layout="row" layout-wrap>
				<!--<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-flag-o"></md-icon>
					</div> 
					<div flex=45>
						<md-input-container> <label>{{translate.load("sbi.execution.roleselection.title")}}</label>
							<md-select ng-change="isRequired=true" multiple aria-label="aria-label" ng-model="selectedLayer.roles" ng-change=""> 
								<md-option	ng-repeat="rs in roles"  value="{{rs.id}}">{{rs.name}}</md-option>
							</md-select> 
						</md-input-container>
					</div>
					
					  <md-chips ng-model="rolesItem" readonly="true"> <md-chip-template>
						<span> <strong> {{$chip.name}}</strong></span>
						<md-chip-remove ng-click="deleteRole($chip.id)"><md-icon md-font-icon="fa fa-times"></md-icon></md-chip-remove>
					  	</md-chip-template> </md-chips>
					-->
				<!-- role selection with checkbox -->

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
		<md-tab label="Filter" md-on-select="setTab('Filter')"
			md-active="isSelectedTab('Filter')" ng-click="loadFilter();">
		<md-card>	
			<div layout="column" layout-wrap layout-fill>
		<md-toolbar class="md-blue minihead ">
	
		<div class="md-toolbar-tools" layout="row" layout-wrap >
			<div flex=50 >
				<h2>{{translate.load("sbi.layerfilter");}}</h2>
			</div>
			<div flex=50 >
				<h2>{{translate.load("sbi.layerfilteradded");}}</h2>
			</div>
		</div>
		</md-toolbar>
		 <md-content  layout="row" layout-wrap flex class="layerFilterContent">
		
			<div flex=50 class="layerFilter">
				<angular-list class="mozSelect" 
					layout-fill id='sx' ng-model=filter item-name='property'
					click-function="addFilter(item)" show-search-bar=true />
			</div>
			<div flex=50 class="layerFilter">
				<angular-list class="mozSelect" 
					layout-fill id='right' ng-model=filter_set item-name='property'
					click-function="removeFilter(item)" show-search-bar=true
					speed-menu-option="removeIcon" />

			
		</div>
		</md-content> 
		</div></md-card></md-tab>
		</md-tabs>
	</div>


	</detail> </angular-list-detail>

</body>
</html>
