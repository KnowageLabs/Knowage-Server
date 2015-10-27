<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include
	file="/WEB-INF/jsp/commons/angular/includeMessageResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="layerWordManager">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- non c'entra	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/glossary/commons/LayerTree.js"></script> -->
<link rel="stylesheet" type="text/css"
	href="/athena/themes/glossary/css/tree-style.css">
<link rel="stylesheet" type="text/css"
	href="/athena/themes/glossary/css/generalStyle.css">
<link rel="stylesheet" type="text/css"
	href="/athena/themes/layer/css/layerStyle.css">
<link rel="stylesheet" type="text/css"
	href="/athena/themes/glossary/css/gestione_glossario.css">
<!-- controller -->
<script type="text/javascript"
	src="/athena/js/src/angular_1.4/tools/layer/layerCatalogue.js"></script>


</head>


<body class="bodyStyle">

	<div ng-controller="Controller " layout="row" layout-wrap layout-fill>
		<!-- left BOX -->

		<div flex="20" layout-fill class="leftBox h100">

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

			<md-content layout-padding style="background-color: rgb(236, 236, 236);" class="ToolbarBox miniToolbar noBorder leftListbox"> 
				
				<angular-list 
					
					ng-click="showme=true" layout-fill id='layerlist' ng-model=layerList
					item-name='name' show-search-bar=true highlights-selected-item=true
					click-function="loadLayerList(item)" menu-option=menuLayer>
				</angular-list> 
				
			</md-content>

		</div>

		<!-- RIGHT -->
		<div flex layout-fill ng-show="showme" class="h100" layout-padding>
			<form name="contactForm" layout-fill id="layerform"
				ng-submit="contactForm.$valid && saveLayer()"
				class="detailBody md-whiteframe-z1" novalidate>

				<md-toolbar class="md-blue minihead">
				<div class="md-toolbar-tools h100" >
					<div style="text-align: center; font-size: 24px;">{{translate.load("sbi.layer");}}</div>
					<div style="position: absolute; right: 0px" class="h100">
						<md-button type="button" tabindex="-1" aria-label="cancel"
							class="md-raised md-ExtraMini " style=" margin-top: 2px;"
							ng-click="cancel()">{{translate.load("sbi.browser.defaultRole.cancel");}} </md-button>
						<md-button ng-disabled="!contactForm.$valid" type="submit"
							aria-label="save layer" class="md-raised md-ExtraMini "	style=" margin-top: 2px;"
							ng-disabled=" selectedItem.name.length === 0 ||  selectedItem.type.length === 0">
						{{translate.load("sbi.browser.defaultRole.save");}} </md-button>
					</div>
				</div>
				</md-toolbar>

				<md-content flex style="margin-left:20px;"
					class="ToolbarBox miniToolbar noBorder">
				<div layout="row" layout-wrap>
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-bookmark"></md-icon>
					</div>
					<div flex=90>
						<md-input-container> <label>{{translate.load("sbi.behavioural.lov.details.label")}}</label>
						<input ng-model="selectedLayer.label" ng-change="isRequired=true"
							ng-required="isRequired" maxlength="100" ng-maxlength="100"
							md-maxlength="100"> </md-input-container>
					</div>
				</div>
				<div layout="row" layout-wrap>
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-pencil-square-o"></md-icon>
					</div>
					<div flex=90>
						<md-input-container> <label>{{translate.load("sbi.behavioural.lov.details.name")}}</label>
						<input ng-model="selectedLayer.name" ng-change="isRequired=true"
							ng-required="isRequired" maxlength="100" ng-maxlength="100"
							md-maxlength="100"> </md-input-container>
					</div>
				</div>
				<div layout="row" layout-wrap>
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-file-text-o"></md-icon>
					</div>
					<div flex=90>
						<md-input-container> <label>{{translate.load("sbi.tools.layer.props.descprition")}}</label>
						<input ng-model="selectedLayer.descr" maxlength="100"
							ng-maxlength="100" md-maxlength="100"> </md-input-container>
					</div>
				</div>

				<div layout="row" layout-wrap>
					<div flex=3 style="line-height: 40px">
						<md-icon md-font-icon="fa fa-flag"></md-icon>
						<label>{{translate.load("sbi.tools.layer.baseLayer")}}</label>
					</div>

					<md-input-container> <md-checkbox
						ng-model="selectedLayer.baseLayer" aria-label="BaseLayer">
					</md-checkbox> </md-input-container>

				</div>
				<div layout="row" layout-wrap>
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-caret-square-o-down"></md-icon>
					</div>
					<div flex=25>
						<md-input-container> <label>{{translate.load("sbi.tools.layer.props.type")}}</label>
							<md-select ng-required="isRequired" ng-change="isRequired=true" ng-show="flagtype" aria-label="aria-label" ng-model="selectedLayer.type" ng-change=""> 
								<md-option	ng-repeat="type in listType" value="{{type.value}}">{{type.label}}</md-option>
							</md-select> 
						</md-input-container>
					</div>
				</div>
				<div style="margin-top: -25px;margin-left: 15px;">
						<md-select-label  ng-show="!flagtype">{{selectedLayer.type}}</md-select-label>
					</div>
				<br><br>
				<div layout="row" layout-wrap>
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-bookmark"></md-icon>
					</div>

					<div flex=90>
						<md-input-container> <label>{{translate.load("sbi.tools.layer.props.label")}}</label>
						<input ng-model="selectedLayer.layerLabel"
							ng-change="isRequired=true" ng-required="isRequired"
							maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
					</div>
				</div>
				<div layout="row" layout-wrap>
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-pencil-square-o"></md-icon>
					</div>

					<div flex=90>
						<md-input-container> <label>{{translate.load("sbi.tools.layer.props.name")}}</label>
						<input ng-model="selectedLayer.layerName"
							ng-change="isRequired=true" ng-required="isRequired"
							maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
					</div>
				</div>
				<div layout="row" layout-wrap>
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-tag"></md-icon>
					</div>
					<div flex=90>
						<md-input-container> <label>{{translate.load("sbi.tools.layer.props.id")}}</label>
						<input ng-model="selectedLayer.layerId2"
							ng-change="isRequired=true" ng-required="isRequired"
							maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
					</div>
				</div>
				
				<div layout="row" layout-wrap>
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-spinner"></md-icon>
					</div>
					<div flex=90>
						<md-input-container> <label>{{translate.load("sbi.tools.layer.props.order")}}</label>
						<input ng-model="selectedLayer.layerOrder"
							ng-change="isRequired=true" ng-required="isRequired"
							type="number" min="0"> </md-input-container>
					</div>
				</div>
				<!-- inizio campi variabili -->
				<div layout="row" layout-wrap ng-if="selectedLayer.type == 'File'">
					<div flex=3 style="margin-top: 10px;">
						<md-icon md-font-icon="fa fa-upload"></md-icon>
					</div>
					<div>
						<md-input-container> 
							<input ng-model="selectedLayer.layerFile" type="file" fileread="selectedLayer.layerFile"> 
						</md-input-container>
					</div>
				</div>


				<div layout="row" layout-wrap
					ng-if="selectedLayer.type == 'WFS' || selectedLayer.type == 'WMS' || selectedLayer.type == 'TMS' ">
					<div flex=3 style="margin-top: 25px;">
						<md-icon md-font-icon="fa fa-link"></md-icon>
					</div>
					<div flex=90>
						<md-input-container> <label>{{translate.load("sbi.tools.layer.props.url")}}</label>
						<input ng-model="selectedLayer.layerURL" required maxlength="100"
							ng-maxlength="100" md-maxlength="100"> </md-input-container>
					</div>
				</div>
				<div layout="row" layout-wrap
					ng-if="selectedLayer.type == 'Google' || selectedLayer.type == 'WMS' || selectedLayer.type == 'TMS' ">
					<div flex=3 style="margin-top: 25px;">
						<md-icon md-font-icon="fa fa-cogs"></md-icon>
					</div>
					<div flex=90>
						<md-input-container> <label>{{translate.load("sbi.tools.layer.props.options")}}</label>
						<input ng-model="selectedLayer.layerOptions" required
							maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
					</div>
				</div>
				<div layout="row" layout-wrap ng-if="selectedLayer.type == 'WMS'">
					<div flex=3 style="margin-top: 30px;">
						<md-icon md-font-icon="fa fa-ellipsis-v"></md-icon>
					</div>
					<div flex=90>
						<md-input-container> <label>{{translate.load("sbi.tools.layer.props.params")}}</label>
						<input ng-model="selectedLayer.layerParams" required
							maxlength="100" ng-maxlength="100" md-maxlength="100"> </md-input-container>
					</div>
				</div>

				</md-content>

			</form>
		</div>

	</div>
</body>
</html>








