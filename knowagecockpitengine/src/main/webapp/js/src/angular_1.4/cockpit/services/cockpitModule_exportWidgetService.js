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
					var requestParams = '?' + $httpParamSerializer(requestConfig);
					var config = {"responseType": "arraybuffer"};
					var exportingToast = sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.cockpit.widgets.exporting"), 'Success!', 0);
					
					sbiModule_restServices.promiseGet('1.0/cockpit/export', 'excel' + requestParams, undefined, config)
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
			var dsLabel = dataset.label;
			var aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset);
			var selections = cockpitModule_datasetServices.getFiltersWithoutParams();
			var parameters = cockpitModule_datasetServices.getDatasetParameters(dsId);
			requestUrl.COCKPIT_SELECTIONS = {};
			requestUrl.COCKPIT_SELECTIONS.aggregations = aggregation;			
			requestUrl.COCKPIT_SELECTIONS.parameters = parameters;
			requestUrl.COCKPIT_SELECTIONS.selections = selections;

			deferred.resolve(requestUrl);
			
			return deferred.promise;
		}
			
		
		return objToReturn;		
	}	
})();