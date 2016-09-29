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

var datasetModule = angular.module('datasetModule', ['ngMaterial','angular-list-detail', 'sbiModule', 'angular_table', 'file_upload', 'ui.codemirror']);

datasetModule.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);

datasetModule
	.controller('datasetController', ["$scope","$log", "sbiModule_config", "sbiModule_translate", "sbiModule_restServices", "sbiModule_messaging", "$mdDialog", "multipartForm", "$timeout", datasetFunction])
	.service('multipartForm',['$http',function($http){
			
			this.post = function(uploadUrl,data){
				
				var formData = new FormData();
	    		for(var key in data){
	    				formData.append(key,data[key]);
	    			}
				return	$http.post(uploadUrl,formData,{
						transformRequest:angular.identity,
						headers:{'Content-Type': undefined}
					})
			}
			
		}]);
function datasetFunction($scope, $log, sbiModule_config, sbiModule_translate, sbiModule_restServices, sbiModule_messaging, $mdDialog, multipartForm, $timeout){
	
	$scope.translate = sbiModule_translate;
	$scope.codeMirror = null;
	$scope.isSomething = false;
	
	$scope.dataSetListColumns = [
	    {"label":$scope.translate.load("sbi.generic.name"),"name":"name"},
	    {"label":$scope.translate.load("sbi.generic.label"),"name":"label"},
	    {"label":$scope.translate.load("sbi.generic.type"), "name":"dsTypeCd"},
	    {"label":$scope.translate.load("sbi.ds.numDocs"), "name":"usedByNDocs"}
    ];
	
	$scope.selectedDatasetVersion = null;
	
	$scope.dataSourceList = [];
	$scope.datamartList = [];
	
	/**
	 * The 'datasetsListPersisted' is the initial and DB aligned dataset list (collection) is needed when dealing with not
	 * yet saved dataset items (when cloning existing or creating new datasets, as well as when performing their removal
	 * from the AT). The 'datasetsListTemp' contains the current AT state.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.datasetsListTemp = [];
	$scope.datasetsListPersisted = [];
	
	$scope.fileObj={};
	
	/**
	 * Static (fixed) values for three comboboxes that appear when the CSV file is uploaded.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
	 */
	$scope.csvEncodingTypes = 
	[ 
	 	{value:"windows-1252",name:"windows-1252"},
	 	{value:"UTF-8",name:"UTF-8"},
	 	{value:"UTF-16",name:"UTF-16"},
	 	{value:"US-ASCII",name:"US-ASCII"},
	 	{value:"ISO-8859-1",name:"ISO-8859-1"}
 	];
	
	$scope.csvDelimiterCharacterTypes = 
	[ 
	 	{value:",",name:","}, 
	 	{value:";",name:";"},	 	
	 	{value:"\\t",name:"\\t"},	
	 	{value:"\|",name:"\|"}
 	];
	
	$scope.csvQuoteCharacterTypes = 
	[ 
	 	{value:"\"",name:"\""}, 
	 	{value:"\'",name:"\'"}
 	];
	
	/**
	 * Keep and change the values for three comboboxes that appear when user uploads a CSV file when creating a new Dataset.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
	 */
	$scope.chooseDelimiterCharacter = function(delimiterCharacterObj) {
		$scope.dataset.csvDelimiter = delimiterCharacterObj;
	}
	
	$scope.chooseQuoteCharacter = function(quoteCharacterObj) {
		$scope.dataset.csvQuote = quoteCharacterObj;
	}
	
	$scope.chooseEncoding = function(encodingObj) {
		$scope.dataset.csvEncoding = encodingObj;
	}
	
	/**
	 * Scope variables (properties) for the REST dataset.
	 * (danristo)
	 */
	
	/**
	 * HTTP methods collection needed for the REST dataset Type tab
	 * (danristo)
	 */
	$scope.httpMethods = [ 
	 	{value:"Get",name:"Get"},
	 	{value:"Post",name:"Post"},
	 	{value:"Put",name:"Put"},
	 	{value:"Delete",name:"Delete"}
 	];
	
	$scope.requestHeadersTableColumns = [
	     {
	    	 name:"name", 
	    	 label:"Name",
	    	 hideTooltip:true
	     },
	     
	     {
	         name:"value",
	         label:"Value",
	         hideTooltip:true
	     }
     ];
	
	$scope.requestHeaderNameValues = [
		{value:"Accept",name:"Accept"},
		{value:"Content-Type",name:"Content-Type"}
	 ];
	
	$scope.requestHeaderValueValues =  [
		{value:"application/json",name:"application/json"},
		{value:"text/plain",name:"text/plain"}
	 ];
	
	$scope.requestHeaderNameItem = '<md-select ng-model=row.name class="noMargin"><md-option ng-repeat="col in scopeFunctions.requestHeaderNameValues" value="{{col.name}}">{{col.name}}</md-option></md-select>';
	$scope.requestHeaderValueItem = '<md-select ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.requestHeaderValueValues" value="{{col.value}}">{{col.value}}</md-option></md-select>',
	
	$scope.metaScopeFunctions = {
		requestHeaderValueValues: $scope.requestHeaderValueValues,
		requestHeaderNameValues: $scope.requestHeaderNameValues
	};
	
	$scope.restRequestHeaders = [{"name":$scope.requestHeaderNameItem,"value":$scope.requestHeaderValueItem}];
	
	$scope.requestHeaderAddItem = function() {
		$scope.restRequestHeaders.push({"name":$scope.requestHeaderNameItem,"value":$scope.requestHeaderValueItem});
	}	
	
	/*
	 * 	service that loads all datasets
	 *   																	
	 */
	$scope.loadAllDatasets = function(){
		sbiModule_restServices.promiseGet("1.0/datasets","")
			.then(function(response) {
				$scope.datasetsListTemp = response.data.root;
				$scope.datasetsListPersisted = angular.copy($scope.datasetsListTemp);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});
	}
	
	$scope.loadAllDatasets();	
	
	/**
	 * Speed-menu option configuration for deleting of a dataset.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.manageDataset = 
	[
	 	// Delete the dataset.
	 	{
	 		label: $scope.translate.load("sbi.ds.deletedataset"),
		 	icon:'fa fa-trash' ,
		 	backgroundColor:'transparent',
	   
		 	action: function(item,event) {
		 				 		
		 		if (item.label!="..." && item.label!="") {
		 			
		 			var confirm = $mdDialog.confirm()
		 	          .title($scope.translate.load('sbi.ds.deletedataset'))
		 	          .targetEvent(event)	 	          
		 	          .textContent($scope.translate.format($scope.translate.load('sbi.ds.deletedataset.msg'), item.label))
		 	          .ariaLabel("Delete dataset")
		 	          .ok($scope.translate.load("sbi.general.yes"))
		 	          .cancel($scope.translate.load("sbi.general.No"));
		 			
		 			$mdDialog
		 				.show(confirm)
		 				.then(
		 						
		 						function() {
		 							
		 							sbiModule_restServices.promiseDelete("1.0/datasets", item.label, "/")
			 							.then(
			 									function(response) {
			 					
			 						   				sbiModule_messaging.showSuccessMessage($scope.translate.format($scope.translate.load('sbi.ds.deletedataset.success'), item.label));
			 						   				
			 						   				// If the dataset that is deleted is selected, deselect it and hence close its details.
			 						   				if ($scope.selectedDataSet && $scope.selectedDataSet.label == item.label) {
			 						   					$scope.selectedDataSet = null;			   					
			 						   				}
			 						   				
			 						   				// Find the dataset, that is deleted on the server-side, in the array of all datasets and remove it from the array.
			 						   				for (var i=0; i<$scope.datasetsListTemp.length; i++) {			   					
			 						   					if ($scope.datasetsListTemp[i].label == item.label) {
			 						   						$scope.datasetsListTemp.splice(i,1);
			 						   						break;
			 						   					}			   					
			 						   				}
			 						
			 						   			}, 
			 						   			
			 						   			function(response) {	
			 						   				
			 						   				// If there is a message that is provided when attempting to delete a dataset that is used in some documents.
				 					   				if (response.data && response.data.errors && Array.isArray(response.data.errors)) {
				 					   					sbiModule_messaging.showErrorMessage(response.data.errors[0].message);
				 					   				}
				 					   				else {
				 					   					sbiModule_messaging.showErrorMessage($scope.translate.format($scope.translate.load('sbi.ds.deletedataset.error'), item.label));
				 					   				}
			 						   				
			 						   			}
			 								);
		 							
		 						},
		 						
		 						function() {
		 							
		 						}
		 						
	 						);
			 		}
		 			else {
		 						 				
		 				var confirm = $mdDialog.confirm()
			 	          .title($scope.translate.load('sbi.ds.deletedataset'))
			 	          .targetEvent(event)	 	          
			 	          .textContent($scope.translate.load('sbi.ds.deletedataset.notsaved.msg'))
			 	          .ariaLabel("Delete dataset")
			 	          .ok($scope.translate.load("sbi.general.yes"))
			 	          .cancel($scope.translate.load("sbi.general.No"));
			 			
			 			$mdDialog
			 				.show(confirm)
			 				.then(		 						
			 						function() { 	
			 							
			 							$scope.datasetsListTemp.splice($scope.datasetsListTemp.length-1,1);		
			 							
			 							// If the newly added dataset is selected when deleting it.
			 							if ($scope.selectedDataSet.label=="..." || $scope.selectedDataSet.label=="") {			 								
				 							$scope.selectedDataSetInit = null; // Reset the selection (none dataset item will be selected) (danristo)
				 							$scope.selectedDataSet = null;
			 							}		
			 							
			 						}, 
			 						
			 						function(){} 
		 						);
		 				
		 			}
		 		}
	 			
		 	} ,
		 	
		 	// Clone the dataset.
		 	{
		 		label: $scope.translate.load("sbi.ds.clone.tooltip"),
			 	icon:'fa fa-files-o' , // alternative: 'fa fa-clone'
			 	backgroundColor:'transparent',
		   
			 	action: function(item,event) {
			 		
			 		if ($scope.datasetsListTemp.length < $scope.datasetsListPersisted.length + 1) {
				 		
			 			var datasetClone = angular.copy(item);	
				 		
				 		datasetClone.label = "...";
				 		datasetClone.dsVersions = [];
				 		datasetClone.usedByNDocs = 0;
				 		$scope.datasetsListTemp.push(datasetClone);
				 		$scope.selectedDataSet = $scope.datasetsListTemp[$scope.datasetsListTemp.length-1];
				 		$scope.selectedDataSetInit = $scope.datasetsListTemp[$scope.datasetsListTemp.length-1];
				 		
			 		}
			 		else {
			 			sbiModule_messaging.showErrorMessage($scope.translate.load('sbi.ds.clone.warning.onlyonenewdataset.msg'));
			 		}
		 			
			 	} 
		 	}
 		 	
	 ];
	
	/**
	 * Speed-menu option configuration for deleting of a dataset version.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.manageVersion = 
	[ 
	 	// Speed-menu option for deleting a dataset version.
     	{	           
			label: $scope.translate.load("sbi.ds.deleteVersion"),
	 		icon:'fa fa-trash' ,
	 		backgroundColor:'transparent',
	           
	 		action: function(item,event) {
	 				 			
	 			var confirm = $mdDialog.confirm()
	 	          .title($scope.translate.load('sbi.ds.version.delete.title'))
	 	          .targetEvent(event)	 	          
	 	          .textContent($scope.translate.format($scope.translate.load('sbi.ds.version.delete.msg'), item.versNum))
	 	          .ariaLabel("Delete dataset version")
	 	          .ok($scope.translate.load("sbi.general.yes"))
	 	          .cancel($scope.translate.load("sbi.general.No"));
	 			
	 			$mdDialog
	 				.show(confirm)
	 				.then(
	 						
	 					function() {
	 						
	 						sbiModule_restServices.promiseDelete("1.0/datasets", $scope.selectedDataSet.id+"/version/"+item.versNum,"/")
		 		   				.then(
		 		   						function(response) {
		 		   						
		 					   				sbiModule_messaging.showSuccessMessage($scope.translate.format($scope.translate.load('sbi.ds.version.delete.success'), item.versNum));
		 					   				
		 					   				for (var j=0; j<$scope.datasetsListTemp.length; j++) {
		 					   					
		 					   					if ($scope.selectedDataSet.id == $scope.datasetsListTemp[j].id) {
		 					
		 					   						for (var i=0; i<$scope.selectedDataSet.dsVersions.length; i++) {
		 					   							
		 					   							if (item.versNum == $scope.selectedDataSet.dsVersions[i].versNum) {		 					   								
		 					   								// Remove the dataset's version from the collection of all datasets (the array in the left angular-table).
		 					   								$scope.datasetsListTemp[j].dsVersions.splice(i,1);
		 					   								// Remove the version from the currently selected dataset (the item in the left angular-table).
		 					   								$scope.selectedDataSet.dsVersions.splice(i,1);//			 					   								
		 					   								break;
		 					   								
		 					   							}
		 					   								
		 					   						}
		 					   						
		 					   					}
		 					   				}   				
		 	   
		 					   			}, 
		 					   			
		 					   			function(response) {	
		 					   				sbiModule_messaging.showErrorMessage($scope.translate.format($scope.translate.load('sbi.ds.version.delete.error'), item.versNum));
		 					   			}
		 			   			);
	 						
	 					},
	 					
	 					function() {
	 					}
	 				
	 				);
	 			
           }
     	
     	},
     	
     	// Speed-menu option for restoring a dataset version.
     	{
     		label: $scope.translate.load('sbi.ds.restore'),
     		icon:'fa fa-retweet' ,
	 		backgroundColor:'transparent',     		
     		
     		action: function(item,event) {
     			
     			var confirm = $mdDialog.confirm()
	 	          .title($scope.translate.load('sbi.ds.version.restore.title'))
	 	          .targetEvent(event)	 	          
	 	          .textContent($scope.translate.format($scope.translate.load('sbi.ds.version.restore.msg'), item.versNum))
	 	          .ariaLabel("Restore dataset version")
	 	          .ok($scope.translate.load("sbi.general.yes"))
	 	          .cancel($scope.translate.load("sbi.general.No"));
	 			
	 			$mdDialog
	 				.show(confirm)
	 				.then(	 						
		 					function() {
		 						
		 						sbiModule_restServices.promiseGet("1.0/datasets", $scope.selectedDataSet.id + "/restore", "versionId=" + item.versNum)
			 		   				.then(
			 		   						function(response) {
			 		   						
			 					   				sbiModule_messaging.showSuccessMessage($scope.translate.format($scope.translate.load('sbi.ds.version.restore.success'), item.versNum),500);
			 					   				
			 					   				for (var i=0; i<$scope.datasetsListTemp.length; i++) {
			 					   					
			 					   					if ($scope.selectedDataSet.id == $scope.datasetsListTemp[i].id) {			 			
			 					   						
			 					   						// Remove the dataset's version from the collection of all datasets (the array in the left angular-table).
	 					   								$scope.datasetsListTemp[i] = response.data[0];
	 					   								// Remove the version from the currently selected dataset (the item in the left angular-table).
	 					   								$scope.selectedDataSet = response.data[0];	
	 					   								// Needed in order to have a copy of the selected dataset that will not influence the selected dataset in the AT while performing changes on it
	 					   								$scope.selectedDataSetInit = response.data[0];	
	 					   								// Call the scope function that is responsible for transformation of configuration data of the File dataset.
	 					   								($scope.selectedDataSet.dsTypeCd.toLowerCase()=="file") ? $scope.refactorFileDatasetConfig(response.data[0]) : null;
	 					   								
	 					   								break;
	 					   								
			 					   					}
			 					   					
			 					   				}   				
			 	   
			 					   			}, 
			 					   			
			 					   			function(response) {
			 					   				//sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			 					   				sbiModule_messaging.showErrorMessage($scope.translate.format($scope.translate.load('sbi.ds.version.restore.error'), item.versNum));
			 					   			}
			 			   			);
		 					},
		 					
		 					function() {
		 						
		 					}		 					
	 					);
     			
     		}
     	
     	}
     	
   ];
	
	// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	$scope.deleteAllDatasetVersions = function() {
				
		if ($scope.selectedDataSet.dsVersions && Array.isArray($scope.selectedDataSet.dsVersions) && $scope.selectedDataSet.dsVersions.length>0) {
						
			var confirm = $mdDialog.confirm()
	          .title($scope.translate.load('sbi.ds.allversions.delete.title')) 	          
	          .textContent($scope.translate.load('sbi.ds.allversions.delete.msg'))
	          .ariaLabel("Delete all dataset versions")
	          .ok($scope.translate.load("sbi.general.yes"))
	          .cancel($scope.translate.load("sbi.general.No"));
			
			$mdDialog
				.show(confirm)
				.then(	 						
						function() {
			
							sbiModule_restServices.promiseDelete("1.0/datasets", $scope.selectedDataSet.id+"/allversions","/")
								.then(
										function(response) {
										
							   				sbiModule_messaging.showSuccessMessage($scope.translate.load('sbi.ds.allversions.delete.success'));
							   				
							   				for (var j=0; j<$scope.datasetsListTemp.length; j++) {
							   					
							   					if ($scope.selectedDataSet && $scope.selectedDataSet.id == $scope.datasetsListTemp[j].id) {	
							   						$scope.datasetsListTemp[j].dsVersions.splice(0,$scope.selectedDataSet.dsVersions.length);
													$scope.selectedDataSet.dsVersions.splice(0,$scope.selectedDataSet.dsVersions.length);	 					   								// Needed in order to have a copy of the selected dataset that will not influence the selected dataset in the AT while performing changes on it
 					   								// Needed in order to have a copy of the selected dataset that will not influence the selected dataset in the AT while performing changes on it
													$scope.selectedDataSetInit = angular.copy($scope.selectedDataSet);
													break;
							   					}
							   				}	
					
							   			}, 
							   			
							   			function(response) {							   				
							   				sbiModule_messaging.showErrorMessage($scope.translate.load('sbi.ds.allversions.delete.error'));
							   			}
								);
						},
						
						function() {
							
						}
					);
			
		}
		else {
			
			$mdDialog
				.show(
						$mdDialog.alert()
					        .clickOutsideToClose(true)
					        .title('Dataset has no versions to delete')
					        .textContent('There are not dataset versions to delete for the selected dataset')
					        .ariaLabel('Dataset versions do not exist')
					        .ok('Ok')
				    );
			
		}
		
	}
	
	// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	$scope.selectDatasetVersion = function(item) {			
		$scope.selectedDatasetVersion = item;
	}
	
	/*
	 * 	service that loads all data sources
	 *   																	
	 */
	$scope.loadAllDataSources = function(){
		
		sbiModule_restServices.promiseGet("2.0","datasources")
			.then(
					function(response) {
						$scope.dataSourceList = response.data;
					}, 
					
					function(response) {
						sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
					}
				);
		
	}
	
	$scope.loadAllDataSources();
	
	$scope.loadAllDatamarts = function() {
		 
		sbiModule_restServices.promiseGet("2.0","businessmodels")
			.then(
					function(response) {
						$scope.datamartList = angular.copy(response.data);
					}, 
					
					function(response) {
						sbiModule_messaging.showErrorMessage(response.data);
					}
				);	
		
	}
	
	$scope.loadAllDatamarts();
	
	/*
	 * 	@GET service that gets domain types for
	 *  scope 																	
	 */
	$scope.getDomainTypeScope = function(){	
		sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=DS_SCOPE")
		.then(function(response) {
			$scope.scopeList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	}
	
	$scope.getDomainTypeScope();
	
	/*
	 * 	@GET service that gets domain types for
	 *  category 																	
	 */
	$scope.getDomainTypeCategory = function(){	
		sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=CATEGORY_TYPE")
		.then(function(response) {
			$scope.categoryList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message,'Error');
		});
	}
	
	$scope.getDomainTypeCategory();
	
	/*
	 * 	@GET service that gets domain types for
	 *  dataset types 																	
	 */
	$scope.getDomainTypeDataset = function(){	
		sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=DATA_SET_TYPE")
		.then(function(response) {
			$scope.datasetTypeList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	}
	
	$scope.getDomainTypeDataset();
	
	 /**
	  * Adds variable map(name,value) object to scenario property array variable.
	  */
	 $scope.addParameters = function(){ 
			var param={};
			if($scope.selectedDataSet.parameters==undefined){
				$scope.selectedDataSet.parameters = [];
			}
			$scope.selectedDataSet.parameters.push(param);
			console.log($scope.selectedDataSet.parameters);
			return param;
	 }
	 
	 /**
	  * Removes selected variable from array property of $scope.scenario object.
	  */
	 $scope.removeParameter=function(param){
			var index=$scope.selectedDataSet.parameters.indexOf(param);		
			$scope.selectedDataSet.parameters.splice(index, 1);
			console.log($scope.selectedDataSet.parameters);
	 }
	
	$scope.loadDataSet = function(item) {
//		$log.info(item);
		console.log(item);
		$scope.selectedDataSetInit = angular.copy(item);
		$scope.selectedDataSet = angular.copy(item);
		
		// Call the scope function that is responsible for transformation of configuration data of the File dataset.
		($scope.selectedDataSet.dsTypeCd.toLowerCase()=="file") ? $scope.refactorFileDatasetConfig(item) : null;
		
	};
	
	$scope.refactorFileDatasetConfig = function(item) {
					
		$scope.selectedDataSet.fileType = item!=undefined ? item.fileType : "";
		$scope.selectedDataSet.fileName = item!=undefined ? item.fileName : "";
		$scope.selectedDataSetInitialFileName = $scope.selectedDataSet.fileName;
		
		$scope.limitPreviewChecked = false;
		
		$scope.selectedDataSet.csvEncoding = item!=undefined ? item.csvEncoding : $scope.csvEncodingDefault; 
		$scope.selectedDataSet.csvDelimiter = item!=undefined ? item.csvDelimiter : $scope.csvDelimiterDefault; 
		$scope.selectedDataSet.csvQuote = item!=undefined ? item.csvQuote : $scope.csvQuoteDefault; 
		
		$scope.selectedDataSet.skipRows = item!=undefined ? Number(item.skipRows) : Number($scope.skipRowsDefault);
		
		/**
		 * Handle the limitRows property value deserialization (special case: it can be of a value NULL).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if (item!=undefined) {
		
			if (item.limitRows!=null && item.limitRows!="") {
				$scope.selectedDataSet.limitRows = Number(item.limitRows);
			}
			else {
				$scope.selectedDataSet.limitRows = item.limitRows;
			}
		}
		else {			
			$scope.selectedDataSet.limitRows = $scope.limitRowsDefault;			
		}

		$scope.selectedDataSet.xslSheetNumber = item!=undefined ? Number(item.xslSheetNumber) : Number($scope.xslSheetNumberDefault);
				
		$scope.selectedDataSet.catTypeVn = item!=undefined ? item.catTypeVn : "";
		$scope.selectedDataSet.catTypeId = item!=undefined ? Number(item.catTypeId) : null;
		
		$scope.selectedDataSet.id = item!=undefined ? item.id : "";
		$scope.selectedDataSet.label = item!=undefined ? item.label : "";
		$scope.selectedDataSet.name = item!=undefined ? item.name : "";
		$scope.selectedDataSet.description = item!=undefined ? item.description : ""; 
		$scope.selectedDataSet.meta = item!=undefined ? item.meta : [];
		
		$scope.selectedDataSet.fileUploaded = false;
		
	}
	
	$scope.createNewDataSet = function() {
//		$log.info("create");
		
		if ($scope.datasetsListTemp.length < $scope.datasetsListPersisted.length + 1) {
			
			var object = {
					actions: "",
					catTypeId:"",
					catTypeVn:"",
					dataSource:"",
					dateIn:"",
					description:"",
					dsTypeCd:"",
					dsVersions:"",
					id:"",
					isPersisted:"",
					isPersistedHDFS:"",
					label:"",
					meta:"",
					name:"",
					owner:"",
					pars:"",
					persistTableName:"",
					pivotIsNumRows:"",
					query:"",
					queryScript:"",
					queryScriptLanguage:"",
					scopeCd:"",
					scopeId:"",
					usedByNDocs:"",
					userIn:"",
					versNum:""
			}
			
			$scope.datasetsListTemp.push(object);
			$scope.selectedDataSet = $scope.datasetsListTemp[$scope.datasetsListTemp.length-1];
			$scope.selectedDataSetInit = $scope.datasetsListTemp[$scope.datasetsListTemp.length-1]; // Reset the selection (none dataset item will be selected) (danristo)
			
		}	
		else {
			sbiModule_messaging.showErrorMessage($scope.translate.load("sbi.ds.add.warning.onlyonenewdataset.msg"));
		}
		
	};
	
	$scope.saveDataSet = function() {
		$log.info("save");
		// TODO: reset the $scope.datasetsListPersisted value as well
	};
	
	$scope.cancelDataSet = function() {
//		$log.info("cancel");
		$scope.selectedDataSetInit = null; // Reset the selection (none dataset item will be selected) (danristo)
		$scope.selectedDataSet = null;
	};
	
	$scope.previewDataset = function () {
		console.log("info")
	}
	
	$scope.uploadFile= function(){
		
    	multipartForm.post(sbiModule_config.contextName +"/restful-services/selfservicedataset/fileupload",$scope.fileObj).success(

				function(data,status,headers,config){
					
					if(data.hasOwnProperty("errors")){						
						console.info("[UPLOAD]: DATA HAS ERRORS PROPERTY!");
						
						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						sbiModule_messaging.showErrorMessage($scope.translate.load(data.errors[0].message));
						
					}
					else {
						
						console.info("[UPLOAD]: SUCCESS!");
												
						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						sbiModule_messaging.showSuccessMessage($scope.translate.format($scope.translate.load('sbi.workspace.dataset.wizard.upload.success'), 
								$scope.fileObj.fileName));
					
						$scope.file={};
						$scope.selectedDataSet.fileType = data.fileType;
						$scope.selectedDataSet.fileName = data.fileName;
						
						/**
						 * When user re-uploads a file, we should reset all fields that we have on the bottom panel of the Step 1, for both file types 
						 * (CSV and XLS), so the user can start from the scratch when defining new/modifying existing file dataset. 
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						$scope.selectedDataSet.csvEncoding = $scope.csvEncodingDefault;
						$scope.selectedDataSet.csvDelimiter = $scope.csvDelimiterDefault;
						$scope.selectedDataSet.csvQuote = $scope.csvQuoteDefault;
						$scope.selectedDataSet.skipRows = $scope.skipRowsDefault;
						$scope.selectedDataSet.limitRows = $scope.limitRowsDefault;
						$scope.selectedDataSet.xslSheetNumber = $scope.xslSheetNumberDefault;
						
						/**
						 * Whenever we upload a file, keep the track of its name, in order to indicate when the new one is browsed but not uploaded.
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						$scope.prevUploadedFile = $scope.selectedDataSet.fileName;
						$scope.selectedDataSet.fileUploaded=true;
						$scope.changingFile = false;
						
					}
				}).error(function(data, status, headers, config) {
					console.info("[UPLOAD]: FAIL! Status: "+status);					

					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					sbiModule_messaging.showErrorMessage($scope.fileObj.fileName+" could not be uploaded."+data.errors[0].message);
					
				});
    	
    }
	
	$scope.changeUploadedFile=function(){
		
		console.info("CHANGE FILE [IN]");
		
		$scope.changingFile = true;
		
		/**
		 * If we are about to change the uploaded file in editing mode, we should keep the data about the name of the previously uploaded
		 * file, in order to keep it in the line for the browsed file.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
		 */
//		if ($scope.editingDatasetFile==true) {
			
			$scope.fileObj = 
			{
				file: { name:$scope.selectedDataSet.fileName }, 
				fileName: $scope.selectedDataSet.fileName
			};
			
//		}
		
		console.info("CHANGE FILE [OUT]");
		
	}
	
	$scope.codemirrorLoaded = function(_editor){
		 $scope.codeMirror = _editor;	  
	 }
	
	  // The ui-codemirror option
	  $scope.cmOption = {
	    lineNumbers: true,
	    tabMode: "indent",
	    onLoad : function(_cm){
	      
	      // HACK to have the codemirror instance in the scope...
	      $scope.modeChanged = function(type){
	    	  console.log(type)
	    	  if(type=='ECMAScript'||type=="MongoDB"){
	    		  _cm.setOption("mode", 'text/javascript');
	  		} else {
	  			_cm.setOption("mode", 'text/x-groovy');
	  		}
	        console.log(_cm)
	      };
	    }
	  };
	  	 
	 $scope.queryScripts = [
	                        {
	                         name: 'javascript',
		 					 mode: 'text/javascript'
	                        },
	                        {
	                         name: 'SQL',
	                         mode: 'text/x-sql'
	                        }
	                       ];
	
	 $scope.codemirrorOptions = {
			   mode : "text/x-sql",
			   indentWithTabs: true,
			   smartIndent: true,
			   lineWrapping : true,
			   matchBrackets : true,
			   autofocus: true,
			   theme:"eclipse",
			   lineNumbers: true,
			 };
	 
	 
	 $scope.getScriptTypes = function() {
			sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=SCRIPT_TYPE")
			.then(function(response) {
				$scope.listOfScriptTypes = response.data;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));
				
			});
		}
	 
	 $scope.getScriptTypes();
	 
	 $scope.openEditScriptDialog = function () {
		   $mdDialog
		   .show({
		    scope : $scope,
		    preserveScope : true,
		    parent : angular.element(document.body),
		    controllerAs : 'openEditScriptDialogCtrl',
		    templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/EditDataSetScript.html',
		    clickOutsideToClose : false,
		    hasBackdrop : false
		   });
	 }
	 
	 $scope.saveScript = function () {
		 console.log("save")
	 }
	
	 $scope.closeScript = function () {
		 $mdDialog.hide();
	 }
	 
	$scope.openQbe = function() {
		$log.info("OPEN QBE");
	}
	
	$scope.viewQbe = function() {
		$log.info("VIEW QBE QUERY");
		$scope.selectedDataSet.qbeJSONQuery = JSON.stringify(JSON.parse($scope.selectedDataSet.qbeJSONQuery),null,2);
		$mdDialog
		   .show({
			    scope : $scope,
			    preserveScope : true,
			    parent : angular.element(document.body),
			    controllerAs : 'datasetController',
			    templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/qbeQueryView.html',
			    clickOutsideToClose : false,
			    hasBackdrop : true
		   });
		
	}
	
	 // The ui-codemirror option
	$scope.cmOptionQbe = {
	    lineNumbers: true,
	    mode: "application/json",
//	    lineWrapping: true,
//	    scrollbarStyle: null,
//	    viewportMargin: 50,
	    theme: "eclipse",
	    showCursorWhenSelecting: false,
	    readOnly: true
	};
	
};