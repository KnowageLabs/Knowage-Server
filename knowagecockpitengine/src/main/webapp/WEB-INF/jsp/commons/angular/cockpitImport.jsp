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

<!-- ---------------------------------------------------------------------------------------
	urlBuilder - for dynamically getting the full URL path to the specific resource.
	spagoBiContext - context path of core engine: /knowage
	cockpitEngineContext - context name of particular engine, in this case: /cockpitengine  
  -------------------------------------------------------------------------------------- -->

	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/cockpit.js")%>"></script>
	
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-sheet/cockpitSheet.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-grid/cockpitGrid.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/cockpitWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-toolbar/cockpitToolbar.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-style-configurator/cockpitStyleConfigurator.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-cross-configurator/cockpitCrossConfigurator.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-columns-configurator/cockpitColumnsConfigurator.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-column-variables/cockpitColumnVariables.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-selector-configurator/cockpitSelectorConfigurator.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-filters-configuration/cockpitFiltersConfiguration.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-text-configuration/cockpitTextConfiguration.js")%>"></script>
	
	<!-- services -->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_widgetServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_customWidgetServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_backwardCompatibility.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_datasetServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_customWidgetServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/datastoreService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_documentServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_generalServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_widgetSelection.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_nearRealtimeServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_realtimeServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_templateServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_utilstServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_crossServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_mapServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_mapThematizerServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_exportWidgetService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_helperDescriptors.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_variableService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/services/cockpitModule_catalogFunctionService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/services/knModule.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/services/knModule_fontIconsService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "/js/src/angular_1.4/tools/commons/services/knModule_aggridLabels.js")%>"></script>
	
	<!-- Drivers -->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "js/src/angular_1.4/tools/driversexecution/driversExecutionModule.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "js/src/angular_1.4/tools/driversexecution/driversExecutionService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "js/src/angular_1.4/tools/driversexecution/driversDependencyService.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "js/src/angular_1.4/tools/documentexecution/documentParamenterElement/documentParamenterElementController.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "js/src/angular_1.4/tools/businessmodelopening/businessModelOpeningModule.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "js/src/angular_1.4/tools/businessmodelopening/businessModelOpeningServices.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(spagoBiContext, "js/src/angular_1.4/tools/driversexecution/renderparameters/renderParameters.js")%>"></script>
	
	<!-- Factory -->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/factory/cockpitModule_gridsterOptions.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/factory/cockpitModule_generalOptions.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/factory/cockpitModule_widgetSelectionUtils.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/factory/cockpitModule_defaultTheme.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/factory/cockpitModule_highchartsLocales.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/factory/cockpitModule_userPalette.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/factory/knModule_chartOptions.js")%>"></script>
		
	<!-- Widgets -->
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/imageWidget/imageWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/textWidget/textWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/chartWidget/chartWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/discoveryWidget/discoveryWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/discoveryWidget/discoveryWidgetEdit.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/advancedTableWidget/advancedTableWidgetEdit.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/advancedTableWidget/advancedTableWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/mapWidget/mapWidgetEdit.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/mapWidget/mapWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/htmlWidget/htmlWidgetEdit.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/htmlWidget/htmlWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/pythonWidget/pythonMode.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/pythonWidget/pythonWidgetEdit.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/pythonWidget/pythonWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/RWidget/RWidgetEdit.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/RWidget/RWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/selectorWidget/selectorWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/documentWidget/documentWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/staticPivotTableWidget/staticPivotTableWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/selectionWidget/selectionWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/customChartWidget/customChartWidget.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/customChartWidget/customChartWidgetEdit.js")%>"></script>
	
	
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-general-configurator/cockpitGeneralConfiguratorController.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-data-configuration/cockpitDataConfigurationController.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-data-configuration/subController/dataAssociationController.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/cockpit-data-configuration/subController/dataIndexesController.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/commons/calculated-field/calculatedField.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/commons/catalog-function/catalogFunction.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/commons/dataset-selector/datasetSelector.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/commons/dataset-selector/multiDatasetSelector.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/commons/document-selector/documentSelector.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/commons/icon-manager/icon-manager.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourcePath(cockpitEngineContext, "/js/src/angular_1.4/cockpit/directives/commons/gallery-selector/gallerySelector.js")%>"></script>

