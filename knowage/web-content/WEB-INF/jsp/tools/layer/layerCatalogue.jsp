<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="layerWordManager">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- non c'entra	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/commons/LayerTree.js"></script> -->
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/tree-style.css">
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/generalStyle.css">
<link rel="stylesheet" type="text/css"
	href="/knowage/themes/layer/css/layerStyle.css">
<!--  <link rel="stylesheet" type="text/css"
	href="/knowage/themes/glossary/css/gestione_glossario.css">-->
<!-- controller -->
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/layer/layerCatalogue.js"></script>

<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>




<script type="text/ng-template" id="dialog1.tmpl.html">
<md-dialog aria-label="Select type of download"  ng-cloak>
  <form>
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h1>Select type of download</h1>
        <span flex></span>
        <md-button  ng-click="closeFilter()">
          <md-icon md-font-icon="fa fa-times" aria-label="Close dialog"></md-icon>
        </md-button>
      </div>
    </md-toolbar>
    <md-dialog-content style="max-width:800px;max-height:810px; ">
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
	<md-button style="margin-left:60%;" ng-click="getDownload(selectedLayer)"><md-icon md-font-icon="fa fa-download" aria-label="Download"></md-icon></md-button>
	</div>
   	 </md-dialog-content>
  </form>
</md-dialog>
</script>
</head>


<body class="bodyStyle">

	<div ng-controller="Controller " layout="row" layout-wrap layout-fill>
		<angular-2-col> <!-- left BOX --> <left-col>
		<div class="leftBox">

			<md-toolbar class="md-blue minihead ">
			<div class="md-toolbar-tools">
				<div ng-model="selectLayer">{{translate.load("sbi.layercatalogue");}}</div>
				<md-button ng-click="loadLayerList(null)" aria-label="new Label"
					class="md-fab md-ExtraMini addButton"
					style="position:absolute; right:11px; top:0px;"> <md-icon
					md-font-icon="fa fa-plus" style=" margin-top: 6px ; color: white;">
				</md-icon> </md-button>
			</div>
			</md-toolbar>

			<md-content layout-padding
				style="background-color: rgb(236, 236, 236);"
				class="ToolbarBox miniToolbar noBorder leftListbox"> 
				<angular-table  layout-fill id='layerlist' 
				ng-model=layerList
				columns='[{"label":"Name","name":"name"},{"label":"Type","name":"type","size":"60px"},{"label":" ","name":"icon","size":"30px"}]'
				columnsSearch='["name","type", "layerURL"]' 
				show-search-bar=true
				highlights-selected-item=true 
				click-function="loadLayerList(item);"
				menu-option=menuLayer 
				scope-functions=tableFunction>
				</angular-table> 
			</md-content>

		</div>
		</left-col> <right-col> <!-- 
					RIGHT 
					-->

		<div  ng-show="showme">

			<form name="forms.contactForm" layout-fill id="layerform"
				ng-submit="forms.contactForm.$valid && saveLayer()"
				class="detailBody mozSize md-whiteframe-z1" novalidate>

				<md-toolbar class="md-blue minihead">
				<div class="md-toolbar-tools h100 ">
					<div style="text-align: center; font-size: 24px;">{{translate.load("sbi.layer");}}</div>
					<div style="position: absolute; right: 0px" class="h100">
						<md-button type="button" tabindex="-1" aria-label="cancel"
							class="md-raised md-ExtraMini mozilla " style=" margin-top: 2px;"
							ng-click="cancel();">{{translate.load("sbi.browser.defaultRole.cancel");}}
						</md-button>
						<md-button ng-disabled="!forms.contactForm.$valid" type="submit"
							aria-label="save layer" class="md-raised md-ExtraMini mozilla"
							style=" margin-top: 2px;"
							ng-disabled=" selectedItem.name.length === 0 ||  selectedItem.type.length === 0">
						{{translate.load("sbi.browser.defaultRole.save");}} </md-button>
					</div>
				</div>
				</md-toolbar>

				<md-tabs md-select="Layer" md-dynamic-height
					class="mozScroll hideTabs h100" md-border-bottom> <md-tab
					label="Layer" md-on-select="setTab('Layer')"
					md-active="isSelectedTab('Layer')"> <md-content flex
					style="margin-left:20px;"
					class=" ToolbarBox miniToolbar noBorder mozTable ">

				<div layout="row" layout-wrap>
					<!--<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-flag-o"></md-icon>
					</div>
				-->
					<div flex=25>
						<md-input-container class="small counter"> <label>{{translate.load("sbi.glossary.category")}}</label>
							<md-select aria-label="aria-label"
								ng-model="selectedLayer.category_id"> <md-option
								ng-repeat="ct in category" value="{{ct.VALUE_ID}}">{{ct.VALUE_NM}}</md-option>
							</md-select> 
						</md-input-container>
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
								maxlength="100" ng-maxlength="100" md-maxlength="100"> 
						</md-input-container>
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
								maxlength="100" ng-maxlength="100" md-maxlength="100"> 
						</md-input-container>
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

					<div flex=3 style="line-height: 40px">
						<!-- <md-icon md-font-icon="fa fa-flag"></md-icon> -->
						<label>{{translate.load("sbi.tools.layer.baseLayer")}}</label>
					</div>

					<md-input-container class="small counter"> <md-checkbox
						ng-model="selectedLayer.baseLayer" aria-label="BaseLayer">
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
							</md-select> 
						</md-input-container>
					</div>
				</div>
				<div style="margin-top: 0px; margin-left: 15px;">
					<md-select-label ng-show="!flagtype">{{selectedLayer.type}}</md-select-label>
				</div>
				<br>
				<br>
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
						<md-input-container class="small counter"> 
							<label>{{translate.load("sbi.tools.layer.props.order")}}</label>
							<input class="input_class" ng-model="selectedLayer.layerOrder" required type="number" min="0"> 
						</md-input-container>
					</div>
				</div>
				<br>
				<div layout="row" layout-wrap ng-show="pathFileCheck"
					style="margin-top: -15px;">
					<p>{{translate.load("sbi.layer.pathfile")}}: <b>{{selectedLayer.pathFile}}</b></p>
				</div>

				<!-- inizio campi variabili -->
				<div layout="row" layout-wrap ng-if="selectedLayer.type == 'File'">
					<!--<div flex=3 style="margin-top: 10px;">
						<md-icon md-font-icon="fa fa-upload"></md-icon>
					</div>
					  -->
					<div flex=100>
						<md-input-container class="small counter"> 
							<input id="layerFile" ng-model="selectedLayer.layerFile" type="file"
							fileread="selectedLayer.layerFile" accept=".json"> 
						</md-input-container>

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
							maxlength="500" ng-maxlength="500" md-maxlength="500"> 
						</md-input-container>


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
				</md-content>
			</form>

			</md-tab>
			<md-tab label="Filter" md-on-select="setTab('Filter')"
				md-active="isSelectedTab('Filter')" ng-click="loadFilter();">
			<md-toolbar class="md-blue minihead "
				style="border-bottom: 2px solid grey;">

			<div class="md-toolbar-tools" layout="row" layout-wrap layout-fill>
				<div flex=50 style="font-size: 16px;">
					<h2>{{translate.load("sbi.layerfilter");}}</h2>
				</div>
				<div flex=50 style="font-size: 16px;">
					<h2>{{translate.load("sbi.layerfilteradded");}}</h2>
				</div>
			</div>
			</md-toolbar> <md-content layout-padding style="background-color: #F1F1F1;"
				class="ToolbarBox miniToolbar noBorder leftListbox">
			<div layout="row" layout-wrap layout-fill>
				<div flex=50>
					<angular-list class="mozSelect" style="margin-left: 55px;"
						flex="25" layout-fill id='sx' ng-model=filter item-name='property'
						click-function="addFilter(item)" show-search-bar=true />
				</div>
				<div flex=50>
					<angular-list class="mozSelect" style="margin-left: 55px;"
						flex="35" layout-fill id='right' ng-model=filter_set
						item-name='property' click-function="removeFilter(item)"
						show-search-bar=true speed-menu-option="removeIcon" />

				</div>
			</div>
			</md-content> </md-tab>
			</md-tabs>
		</div>


		</right-col> </angular-2-col>
	</div>
</body>
</html>