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
		.service("cockpitModule_exportWidgetService", ['$q', '$httpParamSerializer', '$mdToast', 'sbiModule_config', 'sbiModule_user', 'sbiModule_download', 'sbiModule_translate', 'sbiModule_restServices', 'sbiModule_messaging', 'sbiModule_cockpitDocument', 'cockpitModule_datasetServices', 'cockpitModule_widgetSelection', exportWidgetService]);

	function exportWidgetService ($q, $httpParamSerializer, $mdToast, sbiModule_config, sbiModule_user, sbiModule_download, sbiModule_translate, sbiModule_restServices, sbiModule_messaging, sbiModule_cockpitDocument, cockpitModule_datasetServices, cockpitModule_widgetSelection) {
		var objToReturn = {};

		objToReturn.exportWidgetToExcel = function (type, widget) {
			/**
			 * Last parameter is set to TRUE for exporting only one widget, rather than whole document (all table and chart widgets in cockpit)
			 */
			createRequest(type, widget, true)
				.then(function(requestConfig){
					var config = {"responseType": "arraybuffer"};
					var exportingToast = sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.cockpit.widgets.exporting"), 'Success!', 0);
															
					sbiModule_restServices.promisePost('1.0/cockpit/export', 'excel', requestConfig, config)
						.then(function(response){
							var mimeType = response.headers("Content-type");
							var fileName = 'exported_widget';
							if (widget.content) {
								fileName = widget.content.name;
							}
							$mdToast.hide(exportingToast);
							sbiModule_download.getBlob(response.data, fileName, mimeType, type);
						}, function(error){
							$mdToast.cancel(exportingToast);
							sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.cockpit.widgets.exporting.error"), 'Error');
						});
				});
		}

		var createRequest = function (type, widget, exportWidget) {
			var deferred = $q.defer();
			var requestUrl = {
					user_id: sbiModule_user.userUniqueIdentifier,
					outputType: type,
					document: sbiModule_cockpitDocument.docId,
					widget: widget.id,
					DOCUMENT_LABEL: sbiModule_cockpitDocument.docLabel,
					SBI_COUNTRY: sbiModule_config.curr_country,
					SBI_LANGUAGE: sbiModule_config.curr_language
			}

			if (exportWidget) {
				requestUrl.exportWidget = exportWidget;
			}

			var dsId = widget.dataset.dsId;
			var dataset = cockpitModule_datasetServices.getDatasetById(dsId);

			var aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset);
			cleanAggregation(widget, aggregation);

			var loadDomainValues = widget.type == "selector" ? true : false;
			var selections = cockpitModule_datasetServices.getWidgetSelectionsAndFilters(widget, dataset, loadDomainValues);

			var parameters = cockpitModule_datasetServices.getDatasetParameters(dsId);
			var parametersString = cockpitModule_datasetServices.getParametersAsString(parameters);
			var paramsToSend = angular.fromJson(parametersString);
			requestUrl.COCKPIT_SELECTIONS = {};
			requestUrl.COCKPIT_SELECTIONS.aggregations = aggregation;
			requestUrl.COCKPIT_SELECTIONS.parameters = paramsToSend;
			requestUrl.COCKPIT_SELECTIONS.selections = selections;

			deferred.resolve(requestUrl);

			return deferred.promise;
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