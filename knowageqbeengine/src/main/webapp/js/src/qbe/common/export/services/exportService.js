/**
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

(function(){

	angular.module('exportModule').factory('exportService', function(sbiModule_action_builder,sbiModuleDownloadService,sbiModule_messaging, sbiModule_restServices, sbiModule_config){

		var exporters = [];
		exporters.push(new Exporter('csv','text/csv'));
		exporters.push(new Exporter('xlsx','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'));
		var query = {};
		var bodySend = {};
		var exportLimit = '';

		return {

			exportQueryResults:function(mimeType){
				if(mimeType=='text/csv') {
					var config = {"responseType": "arraybuffer"};
					var q="?SBI_EXECUTION_ID="+sbiModule_config.sbiExecutionID+"&currentQueryId="+query.id;
					var promise = sbiModule_restServices.promisePost('qbequery/export', q, bodySend, config);
					var fileName = "report.csv";
					var fileExtension = 'csv';
					promise.then(function(response){

						sbiModuleDownloadService.getBlob(response, fileName, fileExtension);

					},function(response){
						var decodedString = String.fromCharCode.apply(null, new Uint8Array(response.data));
						var obj = JSON.parse(decodedString);
						sbiModule_messaging.showErrorMessage(obj.errors[0].message, 'Error');
					});
				} else {
					var exportResultAction = sbiModule_action_builder.getActionBuilder("POST");
					exportResultAction.actionName = "EXPORT_RESULT_ACTION";
					exportResultAction.queryParams.MIME_TYPE = mimeType;
					exportResultAction.formParams.query = query;
					exportResultAction.formParams.pars = bodySend.pars;
					exportResultAction.conf.responseType = 'arraybuffer';
					exportResultAction.queryParams.limit = exportLimit;
					exportResultAction.executeAction().then(function(response){

						sbiModuleDownloadService.getBlob(response);


					},function(response){
						var decodedString = String.fromCharCode.apply(null, new Uint8Array(response.data));
						var obj = JSON.parse(decodedString);
						sbiModule_messaging.showErrorMessage(obj.errors[0].message, 'Error');
					});
				}

			},

			getExporters:function(){
				return exporters;
			},
			setQuery : function (q) {
				query = q;
			},
			setBody : function (b) {
				bodySend = b;
			},
			setExportLimit : function (el) {
				exportLimit = el;
			},
			getExportLimitation: function(){
				sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
	   			return sbiModule_restServices.promiseGet("2.0/configs","EXPORT.LIMITATION");
	   		}
		}

	})

	function Exporter(name,mymeType){
		this.name = name;
		this.mimeType = mymeType;
	}


})();