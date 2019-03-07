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

<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/cockpit.js"></script>

<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-sheet/cockpitSheet.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-grid/cockpitGrid.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/cockpitWidget.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-toolbar/cockpitToolbar.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-style-configurator/cockpitStyleConfigurator.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-cross-configurator/cockpitCrossConfigurator.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-columns-configurator/cockpitColumnsConfigurator.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-selector-configurator/cockpitSelectorConfigurator.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-filters-configuration/cockpitFiltersConfiguration.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-text-configuration/cockpitTextConfiguration.js"></script>


<!-- services -->
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_widgetServices.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_backwardCompatibility.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_datasetServices.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_documentServices.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_generalServices.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_widgetSelection.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_nearRealtimeServices.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_realtimeServices.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_templateServices.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_utilstServices.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_crossServices.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_exportWidgetService.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/services/cockpitModule_helperDescriptors.js"></script>
<script type="text/javascript" src="<%=spagoBiContext%>/js/src/angular_1.4/tools/commons/services/knModule.js"></script>
<script type="text/javascript" src="<%=spagoBiContext%>/js/src/angular_1.4/tools/commons/services/knModule_fontIconsService.js"></script>



<!-- Factory -->
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/factory/cockpitModule_gridsterOptions.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/factory/cockpitModule_generalOptions.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/factory/cockpitModule_widgetSelectionUtils.js"></script>

<!-- Widget -->
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/imageWidget/imageWidget.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/textWidget/textWidget.js"></script>

<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/chartWidget/chartWidget.js"></script>

<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/tableWidget/tableWidget.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/advancedTableWidget/advancedTableWidgetEdit.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/advancedTableWidget/advancedTableWidget.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/mapWidget/mapWidget.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/htmlWidget/htmlWidgetEdit.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/htmlWidget/htmlWidget.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/selectorWidget/selectorWidget.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/documentWidget/documentWidget.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/staticPivotTableWidget/staticPivotTableWidget.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/selectionWidget/selectionWidget.js"></script>


<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-general-configurator/cockpitGeneralConfiguratorController.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-data-configuration/cockpitDataConfigurationController.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/cockpit-data-configuration/subController/dataAssociationController.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/commons/calculated-field/calculatedField.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/commons/dataset-selector/datasetSelector.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/commons/dataset-selector/multiDatasetSelector.js"></script>
<script type="text/javascript" src="<%=engineContext%>/js/src/angular_1.4/cockpit/directives/commons/document-selector/documentSelector.js"></script>

