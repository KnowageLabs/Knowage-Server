/* Knowage, Open Source Business Intelligence suite
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
along with this program.  If not, see <http://www.gnu.org/licenses/>. */

(function() {	
	
	angular.module('impExpModule').directive('exportCatalog', ['sbiModule_config','sbiModule_translate','sbiModule_download','importExportDocumentModule_importConf','sbiModule_restServices',
	function(sbiModule_config, sbiModule_translate, sbiModule_download, importExportDocumentModule_importConf,sbiModule_restServices){

		return {
			restrict: 'E',
			scope: {
				typeCatalog : "@",
				pathCatalog : "@",
				catalogData : "=",
				catalogSelected: "="
		  	},
			templateUrl: sbiModule_config.dynamicResourcesBasePath 
				+ '/angular_1.4/tools/servermanager/directive/ngExportCatalogTemplate.html',
				link: link
		};
		
		
		function link(scope, element, attrs) {
			console.log("Managing catalog type [" + scope.typeCatalog + "]...");
			
			scope.translate = sbiModule_translate;
			scope.flagCheck=false;
			scope.nameExport="";
			scope.showDataset=false;
			scope.showBM=false;
			scope.showSchema=false;
			scope.showSVG=false;
			scope.showLayer=false;
			scope.showCatalogImported=false;
			scope.showAnalyticalDrivers=false;
			scope.listType=[];
			scope.listTypeImported=[];
			scope.listDestType=[];
			scope.importFile = {};
			scope.exportedDataset =[];
			scope.download = sbiModule_download;
			scope.flagUser = false;
			scope.typeSaveMenu="Missing";
			scope.filterDate;
			
			scope.stepItem = [ {
				name : scope.translate.load('sbi.ds.file.upload.button')
			} ];
			scope.selectedStep = 0;
			scope.stepControl;
			scope.IEDConf = importExportDocumentModule_importConf;

			scope.filterCatalog = function(item){
				if (!item)  item = scope.typeCatalog;
				var serviceName = 'get' + scope.typeCatalog.toLowerCase();
				if(scope.filterDate!=undefined){
					sbiModule_restServices.promiseGet("1.0/serverManager/importExport/" + scope.pathCatalog, serviceName,"dateFilter="+scope.filterDate).then(
							function(response, status, headers, config) {
								scope.catalogData = scope.decorateData(response.data, item);
							},function(response, status, headers, config) {
								console.log("Catalogs for ["+ scope.typeCatalog +"] not getted " + status);
								sbiModule_restServices.errorHandler(response.data,"");
							});
				}else{
					scope.loadAllElementsForCategory(item);
				}
			}
			
			scope.removeFilter = function(){
				scope.filterDate = undefined;
				scope.filterCatalog();
			}
			
			//export utilities 
			scope.loadAllElementsForCategory = function(item){
				if (!item)  item = scope.typeCatalog;
				scope.wait = true;
				var serviceName = 'get' + item.toLowerCase();
				sbiModule_restServices.promiseGet("1.0/serverManager/importExport/" + scope.pathCatalog, serviceName).then(
						function(response, status, headers, config) {
							scope.catalogData = scope.decorateData(response.data, item);							
							scope.wait=false;
						},function(response, status, headers, config) {
							console.log("Catalogs for ["+ scope.typeCatalog +"] not getted " + status);
							sbiModule_restServices.errorHandler(response.data,"");
							scope.wait=false;
						})
			}
			
			scope.decorateData = function (data, type){
				var toReturn = [];
				
				if (data.length==0) return data;
				
				for(var d=0; d<data.length; d++){
					var tmpData = data[d];
					tmpData.catalogType = type;
					toReturn.push(tmpData);
				}
				
				return toReturn;
			}
			
			scope.toggle = function (item, list) {				
				if (!list) list = [];
				
				scope.checkCategory(item);
				var index = scope.indexInList(item, list);

				if(index != -1){
					list.splice(index,1);
				}else{
					list.push(item);
				}

			};

			scope.exists = function (item, list) {

				return  scope.indexInList(item, list)>-1;

			};

			scope.indexInList=function(item, list) {
				if (!list) list = [];
				for (var i = 0; i < list.length; i++) {
					var object = list[i];
					if(object==item){
						return i;
					}
				}

				return -1;
			};

			scope.checkCategory= function(item){			
				var loadData = false;
				//how to generalize??
				switch(item){
				case 'Dataset':
					if(!scope.showDataset){
						scope.showDataset=true;		
						loadData = true;
					}else if(scope.showDataset){
						scope.showDataset=false;
						var loadData = false;
					}					
					break;
				case 'BusinessModel':
					if(!scope.showBM){
						scope.showBM=true;
						loadData = true;
					}else if(scope.showBM){
						scope.showBM=false;
						var loadData = false;
					}					
					break;
				case 'MondrianSchema':
					if(!scope.showSchema){
						scope.showSchema=true;
						loadData = true;
					}else if(scope.showSchema){
						scope.showSchema=false;
						var loadData = false;
					}					
					break;
				case 'SVG':
					if(!scope.showSVG){
						scope.showSVG=true;
						loadData = true;
					}else if(scope.showSVG){
						scope.showSVG=false;
						var loadData = false;
					}					
					break;
				case 'Layer':
					if(!scope.showLayer){
						scope.showLayer=true;
						loadData = true;
					}else if(scope.showLayer){
						scope.showLayer=false;
						var loadData = false;
					}					
					break;
				case 'DatasetImported':
					loadData = false;
					if(!scope.showCatalogImported){
						scope.showCatalogImported=true;
					}else if(scope.showCatalogImported){
						scope.showCatalogImported=false;
					}
					break;
				case 'AnalyticalDrivers':
					loadData = false;
					if(!scope.showAnalyticalDrivers){
						scope.showAnalyticalDrivers=true;
						loadData = true;
					}else if(scope.showAnalyticalDrivers){
						scope.showAnalyticalDrivers=false;
						loadData = false;
					}
					break;
				}
				if (loadData)
					scope.loadAllElementsForCategory(item);

			}
			scope.selectAll = function(){
				if(!scope.flagCheck){
					//if it was false then the user check 
					scope.flagCheck=true;
					scope.catalogSelected=[];
					for(var i=0;i<scope.catalogData.length;i++){
						scope.catalogSelected.push(scope.catalogData[i]);
					}
				}else{
					scope.flagCheck=false;
					scope.catalogSelected=[];
				}


			}
			
			// for last call the toggle method
			scope.toggle(scope.typeCatalog, scope.catalogData);
			
		}
	}]);

})();