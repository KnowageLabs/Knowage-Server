/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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

(function(){
	angular.module("cockpitModule")
		.service("cockpitModule_exportWidgetService", ['$q', '$httpParamSerializer', '$mdToast', 'sbiModule_config', 'cockpitModule_analyticalDrivers', 'sbiModule_user', 'sbiModule_download', 'sbiModule_translate', 'sbiModule_restServices', 'sbiModule_messaging', 'sbiModule_cockpitDocument', 'cockpitModule_datasetServices', 'cockpitModule_widgetSelection','cockpitModule_properties', exportWidgetService]);

	function exportWidgetService ($q, $httpParamSerializer, $mdToast, sbiModule_config, cockpitModule_analyticalDrivers, sbiModule_user, sbiModule_download, sbiModule_translate, sbiModule_restServices, sbiModule_messaging, sbiModule_cockpitDocument, cockpitModule_datasetServices, cockpitModule_widgetSelection, cockpitModule_properties) {
		var objToReturn = {};

		objToReturn.exportWidgetToPdf = function (widget, options) {


			/**
			 * Last parameter is set to TRUE for exporting only one widget, rather than whole document (all table and chart widgets in cockpit)
			 */
			createRequestForPdf(widget, options)
				.then(function(requestConfig){
					var config = {"responseType": "arraybuffer"};
					var exportingToast = sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.cockpit.widgets.exporting"), 'Success!', 0);
					var documentLabel = requestConfig.DOCUMENT_LABEL;

					sbiModule_restServices.promisePost('1.0/cockpit/export', 'pdf', requestConfig, config)
						.then(function(response){
							var mimeType = response.headers("Content-type");
							var fileName = 'exported_widget';
							if (documentLabel != undefined) {
								fileName = documentLabel;
							}
							$mdToast.hide(exportingToast);
							sbiModule_download.getBlob(response.data, fileName,"application/pdf", "pdf");
						}, function(error){
							$mdToast.cancel(exportingToast);
							sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.cockpit.widgets.exporting.error"), 'Error');
						});
				});
		}

		objToReturn.exportWidgetToExcel = function (type, widget, options) {


			/**
			 * Last parameter is set to TRUE for exporting only one widget, rather than whole document (all table and chart widgets in cockpit)
			 */
			createRequest(type, widget, true, options)
				.then(function(requestConfig){
					var config = {"responseType": "arraybuffer"};
					var exportingToast = sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.cockpit.widgets.exporting"), 'Success!', 0);
					var documentLabel = requestConfig.DOCUMENT_LABEL;

					sbiModule_restServices.promisePost('1.0/cockpit/export', 'excel', requestConfig, config)
						.then(function(response){
							var mimeType = response.headers("Content-type");
							var fileName = 'exported_widget';
							if (documentLabel != undefined) {
								fileName = documentLabel;
							}
							$mdToast.hide(exportingToast);
							sbiModule_download.getBlob(response.data, fileName, mimeType, type);
						}, function(error){
							$mdToast.cancel(exportingToast);
							sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.cockpit.widgets.exporting.error"), 'Error');
						});
				});
		}


		var createRequest = function (type, widget, exportWidget, options) {
			var deferred = $q.defer();
			var requestUrl = {
					user_id: sbiModule_user.userUniqueIdentifier,
					outputType: type,
					document: sbiModule_cockpitDocument.docId,
					widget: widget.id,
					DOCUMENT_LABEL: sbiModule_cockpitDocument.docLabel,
					SBI_COUNTRY: sbiModule_config.curr_country,
					SBI_LANGUAGE: sbiModule_config.curr_language,
					SBI_SCRIPT: sbiModule_config.curr_script,
					options : options
			}

			if (exportWidget) {
				requestUrl.exportWidget = exportWidget;
			}

			if (!angular.equals(cockpitModule_properties.VARIABLES,{})) {
				for (var k in widget.content.columnSelectedOfDataset) {
					if(Array.isArray(widget.content.columnSelectedOfDataset[k].variables) && widget.content.columnSelectedOfDataset[k].variables.length) {
						if (widget.type == "table" && widget.content.columnSelectedOfDataset[k].variables[0].action == 'header') {
							for (var j in cockpitModule_properties.VARIABLES) {
								if (j == widget.content.columnSelectedOfDataset[k].variables[0].variable){
									widget.content.columnSelectedOfDataset[k].aliasToShow = cockpitModule_properties.VARIABLES[j];
								}
							}
						}

					}
				}
			}

			var drivers = formatDrivers(cockpitModule_analyticalDrivers);
			if (widget.type == "map") {
				var spatialAttributesToFilter = getSpatialAttributesToFilter(widget.content.layers);
				requestUrl.COCKPIT_SELECTIONS = [];
				for (var k=0; k<widget.datasetId.length; k++) {
					var dsId = widget.datasetId[k];
					var dataset = cockpitModule_datasetServices.getDatasetById(dsId);
					var aggregation;
					if (widget.settings) {
					 aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset, widget.settings.sortingColumn,widget.settings.sortingOrder);
					}
					else {
						aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset)
					}
					aggregation = filterSpatialAttributes(aggregation, spatialAttributesToFilter);
					var selections = cockpitModule_datasetServices.getWidgetSelectionsAndFilters(widget, dataset, false);
					var parameters = cockpitModule_datasetServices.getDatasetParameters(dsId);
					var parametersString = cockpitModule_datasetServices.getParametersAsString(parameters);
					var paramsToSend = angular.fromJson(parametersString);
					requestUrl.COCKPIT_SELECTIONS[k] = {};
					requestUrl.COCKPIT_SELECTIONS[k].datasetId = dsId;
					requestUrl.COCKPIT_SELECTIONS[k].aggregations = aggregation;
					requestUrl.COCKPIT_SELECTIONS[k].parameters = paramsToSend;
					requestUrl.COCKPIT_SELECTIONS[k].drivers = drivers;
					requestUrl.COCKPIT_SELECTIONS[k].selections = selections;
					requestUrl.COCKPIT_VARIABLES = cockpitModule_properties.VARIABLES;
					requestUrl.options = options;
				}
			} else {
				var dsId = widget.dataset.dsId;
				var dataset = cockpitModule_datasetServices.getDatasetById(dsId);
				var aggregation;
				if (widget.settings) {
				 aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset, widget.settings.sortingColumn,widget.settings.sortingOrder);
				}
				else {
					aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset)
				}
				var loadDomainValues = widget.type == "selector" ? true : false;
				var selections = cockpitModule_datasetServices.getWidgetSelectionsAndFilters(widget, dataset, loadDomainValues);
				var parameters = cockpitModule_datasetServices.getDatasetParameters(dsId);
				var parametersString = cockpitModule_datasetServices.getParametersAsString(parameters);
				var paramsToSend = angular.fromJson(parametersString);
				requestUrl.COCKPIT_SELECTIONS = {};
				requestUrl.COCKPIT_SELECTIONS.aggregations = aggregation;
				requestUrl.COCKPIT_SELECTIONS.parameters = paramsToSend;
				requestUrl.COCKPIT_SELECTIONS.drivers = drivers;
				requestUrl.COCKPIT_SELECTIONS.selections = selections;
				requestUrl.COCKPIT_VARIABLES = cockpitModule_properties.VARIABLES;
				requestUrl.options = options;
			}

			deferred.resolve(requestUrl);
			return deferred.promise;
		}

		var createRequestForPdf = function (widget, options) {
			var deferred = $q.defer();
			var requestUrl = {
					user_id: sbiModule_user.userUniqueIdentifier,
					document: sbiModule_cockpitDocument.docId,
					widget: widget.id,
					DOCUMENT_LABEL: sbiModule_cockpitDocument.docLabel,
					SBI_COUNTRY: sbiModule_config.curr_country,
					SBI_LANGUAGE: sbiModule_config.curr_language,
					SBI_SCRIPT: sbiModule_config.curr_script,
					options : options
			}

			if (!angular.equals(cockpitModule_properties.VARIABLES,{})) {
				for (var k in widget.content.columnSelectedOfDataset) {
					if(Array.isArray(widget.content.columnSelectedOfDataset[k].variables) && widget.content.columnSelectedOfDataset[k].variables.length) {
						if (widget.type == "table" && widget.content.columnSelectedOfDataset[k].variables[0].action == 'header') {
							for (var j in cockpitModule_properties.VARIABLES) {
								if (j == widget.content.columnSelectedOfDataset[k].variables[0].variable){
									widget.content.columnSelectedOfDataset[k].aliasToShow = cockpitModule_properties.VARIABLES[j];
								}
							}
						}

					}
				}
			}

			var drivers = formatDrivers(cockpitModule_analyticalDrivers);

			var dsId = widget.dataset.dsId;
			var dataset = cockpitModule_datasetServices.getDatasetById(dsId);
			var aggregation;
			if (widget.settings) {
			 aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset, widget.settings.sortingColumn,widget.settings.sortingOrder);
			}
			else {
				aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset)
			}
			var selections = cockpitModule_datasetServices.getWidgetSelectionsAndFilters(widget, dataset, false);
			var parameters = cockpitModule_datasetServices.getDatasetParameters(dsId);
			var parametersString = cockpitModule_datasetServices.getParametersAsString(parameters);
			var paramsToSend = angular.fromJson(parametersString);
			requestUrl.COCKPIT_SELECTIONS = {};
			requestUrl.COCKPIT_SELECTIONS.aggregations = aggregation;
			requestUrl.COCKPIT_SELECTIONS.parameters = paramsToSend;
			requestUrl.COCKPIT_SELECTIONS.drivers = drivers;
			requestUrl.COCKPIT_SELECTIONS.selections = selections;
			requestUrl.COCKPIT_VARIABLES = cockpitModule_properties.VARIABLES;
			requestUrl.options = options;

			deferred.resolve(requestUrl);
			return deferred.promise;
		}

		var getSpatialAttributesToFilter = function (layers) {
			toReturn = {};
			if (layers) {
				for (var i=0; i<layers.length; i++) {
					var attrsToFilter = [];
					if (layers[i].content && layers[i].content.columnSelectedOfDataset) {
						for (var j=0; j<layers[i].content.columnSelectedOfDataset.length; j++) {
							var dsCol = layers[i].content.columnSelectedOfDataset[j];
							if (dsCol.fieldType == 'SPATIAL_ATTRIBUTE' && dsCol.properties.coordType != 'string') {
								attrsToFilter.push(dsCol.name);
							}
						}
					}
					var dsName = layers[i].name;
					toReturn[dsName] = attrsToFilter;
				}
			}
			return toReturn;
		}

		var filterSpatialAttributes = function (aggregation, spatialAttributesToFilter) {
			var toReturn = {};
			toReturn.dataset = aggregation.dataset;
			toReturn.measures = [];
			toReturn.categories = [];
			var toFilter = spatialAttributesToFilter[aggregation.dataset];
			for (var i=0; i<aggregation.categories.length; i++) {
				var col = aggregation.categories[i];
				if (!toFilter.includes(col.columnName)) {
					toReturn.categories.push(col);
				}
			}
			for (var i=0; i<aggregation.measures.length; i++) {
				var col = aggregation.measures[i];
				if (!toFilter.includes(col.columnName)) {
					toReturn.measures.push(col);
				}
			}
			return toReturn;
		}

		var formatDrivers = function (analyticalDrivers) {
			var toReturn = {};
			for (var key in analyticalDrivers) {
				if (key.endsWith("_description")) continue;
				var key_description = key + "_description";
				toReturn[key] = [{'value': analyticalDrivers[key], 'description': analyticalDrivers[key_description]}];
				var foo = 0;
			}
			return toReturn;
		}

		var cleanAggregation = function (widget, aggregation) {
		    if(widget.type == "chart"
                && widget.content
                && widget.content.chartTemplate
                && widget.content.chartTemplate.CHART
                && widget.content.chartTemplate.CHART.type == "SCATTER"){
                var usedCategories = [];
                for(var i in widget.content.chartTemplate.CHART.VALUES.CATEGORY){
                    var category = widget.content.chartTemplate.CHART.VALUES.CATEGORY[i];
                    if(category.fakeCategory == false){
                        usedCategories.push(category.column);
                    }
                }

                for(var i=aggregation.categories.length-1; i>=0; i--){
                    if(usedCategories.indexOf(aggregation.categories[i].columnName) == -1){
                        aggregation.categories.splice(i, 1);
                    }
                }

                var usedMeasures = [];
                for(var i in widget.content.chartTemplate.CHART.VALUES.SERIE){
                    var serie = widget.content.chartTemplate.CHART.VALUES.SERIE[i];
                    if(serie.fakeSerie == false){
                        usedMeasures.push(serie.column);
                    }
                }

                for(var i=aggregation.measures.length-1; i>=0; i--){
                    if(usedMeasures.indexOf(aggregation.measures[i].columnName) == -1){
                        aggregation.measures.splice(i, 1);
                    }
                }
		    }
        }

		return objToReturn;
	}

})();