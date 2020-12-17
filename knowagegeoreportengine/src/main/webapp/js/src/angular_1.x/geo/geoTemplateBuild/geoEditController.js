/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function () {

	var app = angular.module('geoTemplateBuild', ['ngMaterial',
		'angular_table', 'sbiModule', 'expander-box', 'document_viewer'
	]);
	app.config(['$mdThemingProvider', function ($mdThemingProvider) {
		$mdThemingProvider.theme('knowage')
		$mdThemingProvider.setDefaultTheme('knowage');
	}]);

	app.controller('geoTemplateBuildController', ['$scope', 'channelMessaging', 'sbiModule_translate', 'sbiModule_restServices',
		'sbiModule_config', "$mdDialog", 'sbiModule_messaging', '$documentViewer', '$http', '$window',
		geoTemplateBuildControllerFunction
	]);

	function geoTemplateBuildControllerFunction($scope, channelMessaging, sbiModule_translate,
		sbiModule_restServices, sbiModule_config, $mdDialog, sbiModule_messaging, $documentViewer, $http, $window) {

		$scope.tecnicalUser = isTechnicalUser === "true";
		$scope.template = angular.fromJson(docTemplate);
		$scope.docLabel = documentLabel;
		$scope.isNewDocument = $scope.docLabel === "";

		$scope.translate = sbiModule_translate;
		$scope.layerCatalogs = [];
		$scope.selectedLayer = [];
		$scope.selectedFilters = [];
		$scope.allFilters = [];

		$scope.layerProperties = [];

		$scope.allDriverParamteres = [];
		$scope.selectedDriverParamteres = [];

		$scope.selectedDatasetLabel = $scope.tecnicalUser ? dataset : datasetLabel;

		$scope.isDatasetChosen = $scope.selectedDatasetLabel != '';
		$scope.datasetLabel = $scope.selectedDatasetLabel != '' ? $scope.selectedDatasetLabel : $scope.translate.load('gisengine.desiner.datasetNotChosen');
		$scope.isOpenedLayer = !$scope.isNewDocument && (!$scope.isDatasetChosen || $scope.tecnicalUser);
		$scope.isOpenedDataset = $scope.isDatasetChosen || $scope.isNewDocument;
		$scope.allDatasets = [];

		$scope.disableChooseDs = false;

		$scope.datasetFields = [];
		$scope.datasetJoinColumns = [];

		// indicators
		$scope.datasetIndicators = [];
		$scope.measureFields = [];

		//dataset filters
		$scope.datasetFilters = [];
		$scope.attributeFields = [];

		//geo visibility controls
		$scope.visibility = {
			showRightConfigMenu: true,
			showLegendButton: true,
			showDistanceCalculator: true,
			showDownloadButton: true,
			showSelectMode: true,
			showLayer: true,
			showBaseLayer: true,
			showMapConfig: true
		};
		$scope.showCrossOptions = $scope.template.crossNavigation == true;
		$scope.crossOption = {
			crossNavigationMultiselect: true
		};


		//indicator section metadata (transformed for the required manual input)
		$scope.indicatorColumns = [{
				"label": "Measure",
				"name": "indicatorNameView",
				hideTooltip: true
			},
			{
				"label": "Label",
				"name": "indicatorLabel",
				transformer: function (item) {
					var template = "<md-input-container flex class=\"md-block\"> " +
						"<input class=\"input_class\" ng-model=row.indicatorLabel required />" +
						"</md-input-container>";
					return template;
				},
				hideTooltip: true
			}
		];

		//filter section metadata (transformed for the manual input)
		$scope.filterColumns = [{
				"label": "Name",
				"name": "dsFilterNameView",
				hideTooltip: true
			},
			{
				"label": "Label",
				"name": "dsFilterLabel",
				transformer: function (item) {
					var template = "<md-input-container flex class=\"md-block\"> " +
						"<input class=\"input_class\" ng-model=row.dsFilterLabel />" +
						"</md-input-container>";
					return template;
				},
				hideTooltip: true
			}
		];

		// if there is no template at all
		$scope.editDisabled = $scope.template.targetLayerConf == undefined;

		$scope.loadTemplate = function () {
			if (!$scope.tecnicalUser) {
				if ($scope.docLabel != '') {

					sbiModule_restServices
						.alterContextPath(sbiModule_config.externalBasePath);
					sbiModule_restServices.promiseGet("restful-services/1.0/documents", $scope.docLabel + "/usertemplate")
						.then(
							function (response) {
								$scope.template = angular.fromJson(response.data);
								$scope.editDisabled = $scope.template.targetLayerConf == undefined;
								if ($scope.template.targetLayerConf != undefined) {
									$scope.disableChooseDs = true;
								}
								initializeFromTemplate();
								$scope.loadLayers();
							},
							function (response) {
								sbiModule_messaging.showErrorMessage(response.data, sbiModule_translate.load('gisengine.error.load.tempate'));
							});

				} else {
					$scope.loadLayers();
				}
			}
		}

		$scope.clearDataset = function () {
			$scope.selectedDataset = [];
			$scope.selectedDatasetLabel = '';
			$scope.datasetLabel = '';
			$scope.isDatasetChosen = false;
			$scope.resetAllVariables();


		}

		$scope.resetAllVariables = function () {
			$scope.selectedLayer = [];
			$scope.layerProperties = [];
			$scope.selectedFilters = [];
			$scope.selectedDriverParamteres = [];
			$scope.datasetJoinColumns = [];
			$scope.datasetIndicators = [];
			$scope.datasetFilters = [];
			$scope.datasetFields = [];
		}

		$scope.loadLayers = function () {
			sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
			sbiModule_restServices.promiseGet("restful-services/layers", "")
				.then(
					function (response) {
						angular.copy(response.data.root, $scope.layerCatalogs);
						initializeSelectedLayer();
					},
					function (response) {
						sbiModule_messaging.showErrorMessage(response.data, sbiModule_translate.load('gisengine.error.load.layers'));
					});
		}

		$scope.editMap = function () {
			sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath + "restful-services/");

			$documentViewer.editDocumentByLabel($scope.docLabel, $scope, "edit_map");

			$scope.$on("documentClosed", function () {
				//if($scope.tecnicalUser){
				sbiModule_restServices
					.alterContextPath(sbiModule_config.externalBasePath);
				sbiModule_restServices.promiseGet("restful-services/1.0/documents",
						$scope.docLabel + "/usertemplate")
					.then(
						function (response) {
							$scope.template = angular.fromJson(response.data);
						},
						function (response) {
							sbiModule_messaging.showErrorMessage(response.data, sbiModule_translate.load('gisengine.error.load.tempate'));
						});
			});
		}

		$scope.saveTemplate = function () {
			var template = $scope.buildTemplate();
			if (template.error) {

				sbiModule_messaging.showWarningMessage(template.error, sbiModule_translate.load('gisengine.designer.tempate.error'));


			} else {
				// call service that will save the template, then redirect to gis document for configuring style
				if ($scope.docLabel == '') {


					$mdDialog
						.show({
							controller: DialogControllerSaveDoc,
							templateUrl: sbiModule_config.contextName + '/js/src/angular_1.x/geo/geoTemplateBuild/templates/templateSaveNewMapDocument.html',
							clickOutsideToClose: false,
							preserveScope: true,
							scope: $scope

						});

				} else {
					var temp = {};
					temp.TEMPLATE = template;
					temp.DOCUMENT_LABEL = $scope.docLabel;
					temp.DATASET_LABEL = $scope.selectedDatasetLabel;

					sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
					sbiModule_restServices.promisePost("restful-services/1.0/documents", "saveGeoReportTemplate", temp).then(
						function (response) {
							$scope.template = template;
							$scope.editDisabled = false;
							sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('gisengine.designer.tempate.save.message'), sbiModule_translate.load('gisengine.designer.tempate.save.success'));
						},
						function (response) {
							sbiModule_messaging.showErrorMessage(response.data, sbiModule_translate.load('gisengine.designer.tempate.save.error'));
						});
				}
			}

		}

		$scope.loadAnalyticalDrivers = function () {

			sbiModule_restServices
				.alterContextPath(sbiModule_config.externalBasePath);
			sbiModule_restServices.promiseGet("restful-services/1.0/documents", $scope.docLabel + "/parameters").then(
				function (response) {
					angular.copy(response.data.results, $scope.allDriverParamteres);
					initializeLayerFilters();
				},
				function (response) {
					sbiModule_messaging.showErrorMessage(response.data, sbiModule_translate.load('gisengine.error.load.dataset.drivers'));
				});
		}

		if ($scope.tecnicalUser && $scope.docLabel != '') {
			$scope.loadAnalyticalDrivers();
		}


		$scope.tableFunctionSingleLayer = {
			translate: sbiModule_translate,
			loadListLayers: function (item, evt) {

				$scope.newLayerCatalog = undefined;
				$mdDialog
					.show({
						controller: DialogControllerLayerList,
						templateUrl: sbiModule_config.contextName + '/js/src/angular_1.x/geo/geoTemplateBuild/templates/templateLayerList.html',
						clickOutsideToClose: false,
						preserveScope: true,
						scope: $scope,
						locals: {
							multiSelect: false
						}

					});
			}
		};

		$scope.tableFunctionMultiLayer = {
			translate: sbiModule_translate,
			loadListLayers: function (item, evt) {

				$scope.newLayerCatalog = [];
				$mdDialog
					.show({
						controller: DialogControllerLayerList,
						templateUrl: sbiModule_config.contextName + '/js/src/angular_1.x/geo/geoTemplateBuild/templates/templateLayerList.html',
						clickOutsideToClose: false,
						preserveScope: true,
						scope: $scope,
						locals: {
							multiSelect: true
						}
					});
			}
		};

		$scope.multipleLayerSpeedMenu = [{
			label: 'remove',
			icon: 'fa fa-trash',
			action: function (item) {
				$scope.removeFromSelected(item);
				$scope.selectedFilters = [];
			}
		}];

		$scope.removeFromSelected = function (item) {
			var index = null;
			for (var i = 0; i < $scope.selectedLayer.length; i++) {

				if (item.layerId == $scope.selectedLayer[i].layerId)
					index = i;
			}
			if (index != null) {
				$scope.selectedLayer.splice(index, 1);
			}


		}

		loadLayerFilters = function (layerId) {
			sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
			sbiModule_restServices.promiseGet("restful-services/layers", "getFilter", "id=" + layerId).then(
				function (response) {

					for (var i = 0; i < response.data.length; i++) {
						$scope.allFilters.push(response.data[i]);
					}

				},
				function (response) {
					sbiModule_messaging.showErrorMessage(response.data, sbiModule_translate.load('gisengine.error.load.layer.filters'));
				});
		}

		$scope.loadAllFilters = function () {
			$scope.allFilters = [];
			for (var i = 0; i < $scope.selectedLayer.length; i++) {
				loadLayerFilters($scope.selectedLayer[i].layerId);
			}
		}

		$scope.tableFunctionFilters = {
			translate: sbiModule_translate,
			loadFilters: function (item, evt) {
				$scope.loadAllFilters();

				$scope.newFilter = [];
				$scope.newDriverParameter = [];
				$mdDialog
					.show({
						controller: DialogControllerFilter,
						templateUrl: sbiModule_config.contextName + '/js/src/angular_1.x/geo/geoTemplateBuild/templates/templateFilterList.html',
						clickOutsideToClose: false,
						preserveScope: true,
						scope: $scope
					});

			}
		};

		$scope.filtersSpeedMenu = [{
			label: 'remove',
			icon: 'fa fa-trash',
			action: function (item) {
				$scope.removeFilterFromSelected(item);
			}
		}];

		$scope.removeFilterFromSelected = function (item) {
			var index = $scope.selectedDriverParamteres.indexOf(item);
			if (index > -1) {

				$scope.selectedDriverParamteres.splice(index, 1);
			}
		};

		$scope.loadDatasetColumns = function (label) {
			sbiModule_restServices
				.alterContextPath(sbiModule_config.externalBasePath);
			sbiModule_restServices.promiseGet("restful-services/1.0/datasets", label + "/fields").then(
				function (response) {
					angular.copy(response.data.results, $scope.datasetFields);
					$scope.loadMeasures();

				},
				function (response) {
					sbiModule_messaging.showErrorMessage(response.data, sbiModule_translate.load('gisengine.error.load.datasetcolumn'));
				});
		};

		$scope.loadMeasures = function () {
			var tempMeas = [];
			var tempAttr = [];

			for (var i = 0; i < $scope.datasetFields.length; i++) {
				if ($scope.datasetFields[i].nature === "measure") {
					tempMeas.push($scope.datasetFields[i]);
				} else {
					tempAttr.push($scope.datasetFields[i]);
				}
			}

			angular.copy(tempMeas, $scope.tableFunctionIndicator.datasetMeasuresStore);
			angular.copy(tempAttr, $scope.tableFunctionsJoin.datasetColumnsStore);
		};


		if ($scope.isDatasetChosen) {
			$scope.loadDatasetColumns($scope.selectedDatasetLabel);
			// $scope.loadMeasures();
		};

		$scope.tableFunctionDataset = {
			translate: sbiModule_translate
		};

		$scope.datasetSpeedMenu = [{
			label: 'remove',
			icon: 'fa fa-trash',
			action: function (item) {
				$scope.removeDatasetFromSelected(item);
			}
		}];

		$scope.removeDatasetFromSelected = function (item) {
			$scope.clearDataset();
		};

		$scope.tableFunctionDataset.choseDataset = function (isFirstDataset) {
			if (isFirstDataset) {
				$scope.showDatasetList();
			} else {
				//if a dataset was already selected, ask confirm because will reset all other configuration
				var confirm = $mdDialog.confirm()
					.title($scope.translate.load('gisengine.designer.confirm.title'))
					.textContent($scope.translate.load('gisengine.designer.confirm.msg'))
					.ariaLabel('Confirm change dataset')
					.ok($scope.translate.load('sbi.generic.modify'))
					.cancel($scope.translate.load('sbi.generic.cancel'));

				$mdDialog.show(confirm).then(function () {
					$scope.showDatasetList();
				}, function () {
					console.log("User choose don't change the dataset !");
				});
			}

		};

		$scope.showDatasetList = function () {
			sbiModule_restServices
				.alterContextPath(sbiModule_config.externalBasePath);
			sbiModule_restServices.promiseGet("restful-services/1.0/datasets", "mydatanoparams")
				.then(
					function (response) {
						$scope.allDatasets = [];
						angular.copy(response.data.root, $scope.allDatasets);
						$mdDialog
							.show({
								controller: DialogControllerDataset,
								templateUrl: sbiModule_config.contextName + '/js/src/angular_1.x/geo/geoTemplateBuild/templates/templateDatasetList.html',
								clickOutsideToClose: false,
								preserveScope: true,
								scope: $scope
							});


					},
					function (response) {
						sbiModule_messaging.showErrorMessage(response.data, sbiModule_translate.load('gisengine.error.load.datasets'));
					});
		};

		$scope.tableFunctionsJoin = {
			translate: sbiModule_translate,
			datasetColumnsStore: $scope.attributeFields
		};

		$scope.tableFunctionsJoin.addJoinColumn = function () {
			if ($scope.selectedLayer.length > 0) {
				var layerId = $scope.selectedLayer[0].layerId;
				sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
				sbiModule_restServices.promiseGet("restful-services/layers", "getFilter", "id=" + layerId).then(
					function (response) {

						$scope.layerProperties = response.data;
						$scope.tableFunctionsJoin.layerColumnsStore = $scope.layerProperties;
						var newRow = {
							datasetColumn: '',
							layerColumn: '',
							datasetColumnView: '<md-select ng-model=row.datasetColumn class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetColumnsStore" value="{{col.id}}">{{col.id}}</md-option></md-select>',
							layerColumnView: '<md-select ng-model=row.layerColumn class="noMargin"><md-option ng-repeat="col in scopeFunctions.layerColumnsStore" value="{{col.property}}">{{col.property}}</md-option></md-select>'
						};

						$scope.datasetJoinColumns.push(newRow);


					},
					function (response) {
						sbiModule_messaging.showErrorMessage(response.data, "error loading layer filters");
					});

			} else {
				sbiModule_messaging.showWarningMessage(sbiModule_translate.load('gisengine.designer.layerFirst'), sbiModule_translate.load('gisengine.designer.layerMiss'));
			}
		};

		$scope.datasetJoinSpeedMenu = [{
			label: 'remove',
			icon: 'fa fa-trash',
			action: function (item) {
				$scope.removeJoinFromSelected(item);
			}
		}];

		$scope.removeJoinFromSelected = function (item) {
			var index = $scope.datasetJoinColumns.indexOf(item);
			if (index > -1) {
				$scope.datasetJoinColumns.splice(index, 1);
			}
		};

		// INDICATORS
		$scope.tableFunctionIndicator = {
			translate: sbiModule_translate,
			datasetMeasuresStore: $scope.measureFields
		};

		$scope.tableFunctionIndicator.addIndicator = function () {
			$scope.tableFunctionIndicator.datasetMeasuresStore = $scope.measureFields;
			var newRow = {
				indicatorName: '',
				indicatorLabel: '',
				indicatorNameView: '<md-select ng-model=row.indicatorName class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetMeasuresStore" value="{{col.id}}">{{col.id}}</md-option></md-select>',
				//indicatorLabelView : '<md-input-container class="md-block"><label>indicator label</label><input type="text" ng-model="row.indicatorLabel"></md-input-container>'
			};

			$scope.datasetIndicators.push(newRow);

		};

		$scope.indicatorsSpeedMenu = [{
			label: 'remove',
			icon: 'fa fa-trash',
			action: function (item) {
				$scope.removeIndicatorFromSelected(item);
			}
		}];

		$scope.removeIndicatorFromSelected = function (item) {
			var index = $scope.datasetIndicators.indexOf(item);
			if (index > -1) {
				$scope.datasetIndicators.splice(index, 1);
			}
		}

		// DATASET FILTERS

		$scope.tableFunctionDatasetFilters = {
			translate: sbiModule_translate,
			datasetAttributeStore: $scope.attributeFields
		};

		$scope.tableFunctionDatasetFilters.addDatasetFilter = function () {
			$scope.tableFunctionDatasetFilters.datasetAttributeStore = $scope.attributeFields;
			var newRow = {
				dsFilterName: '',
				dsFilterLabel: '',
				dsFilterNameView: '<md-select ng-model=row.dsFilterName class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetAttributeStore" value="{{col.id}}">{{col.id}}</md-option></md-select>',
				//indicatorLabelView : '<md-input-container class="md-block"><label>indicator label</label><input type="text" ng-model="row.indicatorLabel"></md-input-container>'
			};

			$scope.datasetFilters.push(newRow);

		}

		$scope.dsFiltersSpeedMenu = [{
			label: 'remove',
			icon: 'fa fa-trash',
			action: function (item) {
				$scope.removeDsFilterFromSelected(item);
			}
		}];

		$scope.removeDsFilterFromSelected = function (item) {
			var index = $scope.datasetFilters.indexOf(item);
			if (index > -1) {
				$scope.datasetFilters.splice(index, 1);
			}
		}

		$scope.buildTemplate = function () {
			var template = {};
			angular.copy($scope.template, template);

			if ($scope.isDatasetChosen) {
				// template building when dataset is selected
				if ($scope.selectedLayer.length == 0) {
					template.error = sbiModule_translate.load('gisengine.designer.tempate.nolayer');
					return template;
				} else {
					// from interface no more than one layer can be selected
					template.targetLayerConf = [];
					var layerConf = {};
					layerConf.label = $scope.selectedLayer[0].name;
					template.targetLayerConf.push(layerConf);
				}

				if ($scope.datasetJoinColumns.length == 0) {
					template.error = sbiModule_translate
						.load('gisengine.designer.tempate.noJoinColumns');
					return template;
				} else {
					template.datasetJoinColumns = "";
					template.layerJoinColumns = "";
					for (var i = 0; i < $scope.datasetJoinColumns.length; i++) {
						if ($scope.datasetJoinColumns[i].datasetColumn && $scope.datasetJoinColumns[i].datasetColumn != '' &&
							$scope.datasetJoinColumns[i].layerColumn && $scope.datasetJoinColumns[i].layerColumn != '') {
							template.datasetJoinColumns += $scope.datasetJoinColumns[i].datasetColumn;
							template.layerJoinColumns += $scope.datasetJoinColumns[i].layerColumn;
							if (i != $scope.datasetJoinColumns.length - 1) {
								template.datasetJoinColumns += ",";
								template.layerJoinColumns += ",";
							}
						} else {
							template.error = sbiModule_translate
								.load('gisengine.designer.tempate.noJoinColumns');
							return template;
						}
					}
				}

				if ($scope.datasetIndicators.length == 0) {
					template.error = sbiModule_translate
						.load('gisengine.designer.tempate.noIndicators');
					return template;
				} else {
					template.indicators = [];
					for (var i = 0; i < $scope.datasetIndicators.length; i++) {
						if ($scope.datasetIndicators[i].indicatorName && $scope.datasetIndicators[i].indicatorName != '' &&
							$scope.datasetIndicators[i].indicatorLabel && $scope.datasetIndicators[i].indicatorLabel != '') {
							var indicator = {};
							indicator.name = $scope.datasetIndicators[i].indicatorName;
							indicator.label = $scope.datasetIndicators[i].indicatorLabel;
							template.indicators.push(indicator);
						} else {
							template.error = sbiModule_translate
								.load('gisengine.designer.tempate.noIndicatorLabel');
							return template;
						}
					}
					if (template.indicators.length == 0) {
						template.error = sbiModule_translate
							.load('gisengine.designer.tempate.noIndicators');
						return template;
					}
				}


				if ($scope.datasetFilters.length > 0) {
					template.filters = [];
					for (var i = 0; i < $scope.datasetFilters.length; i++) {
						if ($scope.datasetFilters[i].dsFilterName != '' && $scope.datasetFilters[i].dsFilterLabel != '') {
							var filter = {};
							filter.name = $scope.datasetFilters[i].dsFilterName;
							filter.label = $scope.datasetFilters[i].dsFilterLabel;
							template.filters.push(filter);
						}

					}
				}
			} else {
				// template building when dataset is not selected
				if ($scope.selectedLayer.length == 0) {
					template.error = sbiModule_translate
						.load('gisengine.designer.tempate.nolayer');
					return template;
				} else {
					// from interface no more than one layer can be selected
					template.targetLayerConf = [];
					for (var i = 0; i < $scope.selectedLayer.length; i++) {
						var layer = {};
						layer.label = $scope.selectedLayer[i].name;
						template.targetLayerConf.push(layer);
					}

				}

				template.analitycalFilter = [];
				for (var i = 0; i < $scope.selectedDriverParamteres.length; i++) {
					template.analitycalFilter.push($scope.selectedDriverParamteres[i].url);
				}

			}
			template.visibilityControls = $scope.visibility;
			template.crossNavigationMultiselect = $scope.crossOption.crossNavigationMultiselect;

			return template;
		}


		function initializeFromTemplate() {
			if ($scope.template.mapName) {
				$scope.mapName = $scope.template.mapName;
			}
			if ($scope.selectedDatasetLabel != null && $scope.selectedDatasetLabel != undefined && $scope.selectedDatasetLabel != "") {
				initializeDataset($scope.selectedDatasetLabel);
			}
			initializeIndicators();
			initilizeDatasetFilters();
			if (!$scope.tecnicalUser) {
				initializeLayerFilters();
			}
			if ($scope.template.visibilityControls) {
				$scope.visibility = $scope.template.visibilityControls;
			}
			if ($scope.template.crossNavigationMultiselect !== undefined) {
				$scope.crossOption.crossNavigationMultiselect = $scope.template.crossNavigationMultiselect;
			}

		}

		function initializeDataset(label) {

			sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
			sbiModule_restServices.promiseGet("restful-services/1.0/datasets", label).then(
				function (response) {
					if (response.data) {
						$scope.selectedDataset = [];
						$scope.selectedDataset.push(response.data[0]);
					}
				},
				function (response) {
					sbiModule_messaging.showErrorMessage(response.data, sbiModule_translate.load('gisengine.error.load.datasets'));
				});

		}

		function initializeSelectedLayer() {
			if ($scope.isDatasetChosen) {
				if ($scope.template.targetLayerConf) {
					for (var i = 0; i < $scope.layerCatalogs.length; i++) {
						if ($scope.layerCatalogs[i].name === $scope.template.targetLayerConf[0].label) {
							$scope.selectedLayer.push($scope.layerCatalogs[i]);
						}
					}
				}
				initializeDatasetJoinColumns();

			} else {
				if ($scope.template.targetLayerConf) {
					for (var i = 0; i < $scope.layerCatalogs.length; i++) {
						for (var j = 0; j < $scope.template.targetLayerConf.length; j++) {

							if ($scope.layerCatalogs[i].name === $scope.template.targetLayerConf[j].label) {
								$scope.selectedLayer.push($scope.layerCatalogs[i]);
							}
						}
					}
				}
			}
		}

		function initializeDatasetJoinColumns() {

			if ($scope.template.datasetJoinColumns && $scope.template.layerJoinColumns) {
				var dsJoinCols = $scope.template.datasetJoinColumns.split(',');
				var layerJoinCols = $scope.template.layerJoinColumns.split(',');

				var layerId = $scope.selectedLayer[0].layerId;

				sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
				sbiModule_restServices.promiseGet("restful-services/layers", "getFilter", "id=" + layerId).then(
					function (response) {

						$scope.layerProperties = response.data;
						$scope.tableFunctionsJoin.layerColumnsStore = $scope.layerProperties;
						for (var i = 0; i < dsJoinCols.length; i++) {
							var newRow = {
								datasetColumn: dsJoinCols[i],
								layerColumn: layerJoinCols[i],
								datasetColumnView: '<md-select ng-model=row.datasetColumn class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetColumnsStore" value="{{col.id}}">{{col.id}}</md-option></md-select>',
								layerColumnView: '<md-select ng-model=row.layerColumn class="noMargin"><md-option ng-repeat="col in scopeFunctions.layerColumnsStore" value="{{col.property}}">{{col.property}}</md-option></md-select>'
							};

							$scope.datasetJoinColumns.push(newRow);
						}

					},
					function (response) {
						sbiModule_messaging.showErrorMessage(response.data, "error loading layer filters");
					});

			}
		}

		function initializeIndicators() {
			if ($scope.template.indicators) {
				for (var i = 0; i < $scope.template.indicators.length; i++) {
					var newRow = {
						indicatorName: $scope.template.indicators[i].name,
						indicatorLabel: $scope.template.indicators[i].label,
						indicatorNameView: '<md-select ng-model=row.indicatorName class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetMeasuresStore" value="{{col.id}}">{{col.id}}</md-option></md-select>',
					};


					$scope.datasetIndicators.push(newRow);
				}
			}
		}

		function initilizeDatasetFilters() {
			if ($scope.template.filters) {
				for (var i = 0; i < $scope.template.filters.length; i++) {
					var newRow = {
						dsFilterName: $scope.template.filters[i].name,
						dsFilterLabel: $scope.template.filters[i].label,
						dsFilterNameView: '<md-select ng-model=row.dsFilterName class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetAttributeStore" value="{{col.id}}">{{col.id}}</md-option></md-select>',
						//	indicatorLabelView : '<md-input-container class="md-block"><label>indicator label</label><input type="text" ng-model="row.indicatorLabel"></md-input-container>'
					};


					$scope.datasetFilters.push(newRow);
				}
			}
		}

		function initializeLayerFilters() {
			if ($scope.template.analitycalFilter) {
				for (var i = 0; i < $scope.template.analitycalFilter.length; i++) {
					var driver = false;
					for (var j = 0; j < $scope.allDriverParamteres.length; j++) {
						driver = false;
						if ($scope.template.analitycalFilter[i] === $scope.allDriverParamteres[j].url) {
							$scope.selectedDriverParamteres.push($scope.allDriverParamteres[j]);
							driver = true;
						} else {
							//							var filter={};
							//							filter.property=$scope.template.analitycalFilter[i];
							//							$scope.selectedFilters.push(filter);
						}

					}
				}
			}
		}


		$scope.cancelBuildTemplateAdmin = function () {
			channelMessaging.sendMessage();
		}

		$scope.cancelBuildTemplate = function () {

			if ($window.frameElement.name === "angularIframe") {
				$window.parent.angular.element(window.frameElement).scope().cancelMapDesignerDialog();
			}


		}

		//loads sections...
		if ($scope.tecnicalUser) {
			initializeFromTemplate()
			$scope.loadLayers();
		} else {
			$scope.loadTemplate();
		}
	} //end of geoTemplateBuildController function


	// dialog controllers
	function DialogControllerLayerList($scope, $mdDialog, multiSelect) {
		$scope.multiSelect = multiSelect;
		if (multiSelect == true) {
			$scope.newLayerCatalog = [];
		}
		$scope.closeDialog = function () {
			//$scope.newLayerCatalog = undefined;
			$mdDialog.cancel();
		}

		$scope.changeSelectedLayer = function () {
			if ($scope.multiSelect == true) {

				for (var i = 0; i < $scope.newLayerCatalog.length; i++) {

					if (!checkIfExists($scope.newLayerCatalog[i])) {
						$scope.selectedLayer.push($scope.newLayerCatalog[i]);
						/**
						 * every time selected layers are changed selected filters will
						 * be cleared prevent case that selected filter can be related
						 * to unselected layer
						 */

						$scope.selectedFilters = [];

					}
				}


			} else {
				$scope.selectedLayer = [];
				$scope.selectedLayer.push($scope.newLayerCatalog);
				$scope.layerProperties = [];
				$scope.datasetJoinColumns = [];
			}

			$mdDialog.cancel();
		}

		function checkIfExists(elem) {
			for (var i = 0; i < $scope.selectedLayer.length; i++) {
				if ($scope.selectedLayer[i].layerId == elem.layerId) {
					return true;
				}
			}
			return false;
		}

	}

	function DialogControllerFilter($scope, $mdDialog) {
		$scope.closeFilterDialog = function () {
			$scope.newFilter = [];
			$scope.newDriverParameter = [];
			$mdDialog.cancel();
		}


		$scope.changeSelectedFilters = function () {
			for (var i = 0; i < $scope.newFilter.length; i++) {

				if (!checkIfSelected($scope.newFilter[i])) {
					$scope.selectedFilters.push($scope.newFilter[i]);
				}
			}

			for (var i = 0; i < $scope.newDriverParameter.length; i++) {

				if ($scope.selectedDriverParamteres.indexOf($scope.newDriverParameter[i]) == -1) {
					$scope.selectedDriverParamteres.push($scope.newDriverParameter[i]);
				}
			}

			$mdDialog.cancel();
		}

		function checkIfSelected(elem) {
			for (var i = 0; i < $scope.selectedFilters.length; i++) {
				if (elem.property == $scope.selectedFilters[i].property)
					return true;
			}
			return false;
		}

	}

	function DialogControllerDataset($scope, $mdDialog) {
		$scope.closeDatasetDialog = function () {

			$mdDialog.cancel();
		}

		$scope.changeSelectedDataset = function () {
			$scope.resetAllVariables();
			$scope.selectedDatasetLabel = $scope.chosenDataset.label;
			$scope.isDatasetChosen = true;
			$scope.datasetLabel = $scope.chosenDataset.label;
			$scope.loadDatasetColumns($scope.chosenDataset.label);
			//update gui fileds table
			$scope.selectedDataset = [];
			$scope.selectedDataset.push($scope.chosenDataset);
			//			$scope.resetAllVariables();

			$mdDialog.cancel();

		}
	}

	function DialogControllerSaveDoc($scope, $mdDialog, $http, sbiModule_config, sbiModule_translate, sbiModule_messaging, sbiModule_restServices) {
		$scope.closeSaveDocDialog = function () {

			$mdDialog.cancel();
			$scope.newMapName = undefined;
			$scope.newMapDescription = undefined;
		}

		$scope.saveNewMapDoc = function () {
			var d = new Date();
			var docLabel = 'geomap_' + d.getTime() % 10000000;
			var template = $scope.buildTemplate();
			var descr = $scope.newMapDescription ? $scope.newMapDescription : '';
			var datasetLabel = $scope.selectedDatasetLabel;
			var formData = {};
			formData.name = $scope.newMapName;
			formData.label = docLabel;
			formData.description = descr;
			formData.typeid = "MAP";
			formData.template = JSON.stringify(template);
			formData.dataset_label = datasetLabel;

			debugger;


			var url = sbiModule_config.adapterPath;
			url = url + "?ACTION_NAME=SAVE_DOCUMENT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&standardUrl=true&MESSAGE_DET=DOC_SAVE";
			url = url + "&user_id=" + sbiModule_config.userId;
			$http({
				method: 'POST',
				url: url,
				data: formData,
				headers: {
					'Content-Type': 'application/x-www-form-urlencoded'
				},

				transformRequest: function (obj) {

					var str = [];

					for (var p in obj)
						str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));

					return str.join("&");

				}
			}).then(function successCallback(response) {
				$scope.docLabel = docLabel;
				$scope.template = template;
				$scope.editDisabled = false;
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('gisengine.designer.tempate.save.message'), sbiModule_translate.load('gisengine.designer.tempate.save.success'));
				$mdDialog.cancel();
			}, function errorCallback(response) {
				sbiModule_messaging.showErrorMessage(response.data, "error saving template");
			});


		}

	}

})();