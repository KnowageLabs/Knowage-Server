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

var datasetModule = angular.module('datasetModule', ['ngMaterial','angular-list-detail', 'sbiModule', 'angular_table']);

datasetModule.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);

datasetModule.controller('datasetController', ["$scope","$log", "sbiModule_translate", "sbiModule_restServices", "sbiModule_messaging", datasetFunction]);

function datasetFunction($scope, $log, sbiModule_translate, sbiModule_restServices, sbiModule_messaging){
	
	$scope.translate = sbiModule_translate;
	
	$scope.dataSetListColumns = [
    {"label":$scope.translate.load("sbi.generic.name"),"name":"name"},
    {"label":$scope.translate.load("sbi.generic.label"),"name":"label"},
    {"label":$scope.translate.load("sbi.generic.type"), "name":"dsTypeCd"},
    {"label":$scope.translate.load("sbi.ds.numDocs"), "name":"usedByNDocs"}
    ];
	
	/*
	 * 	service that loads all datasets
	 *   																	
	 */
	$scope.loadAllDatasets = function(){
		sbiModule_restServices.promiseGet("1.0/datasets","")
		.then(function(response) {
				$scope.datasetsList = response.data.root;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	}
	
	$scope.loadAllDatasets();
	
	/*
	 * 	service that loads all data sources
	 *   																	
	 */
	$scope.loadAllDataSources = function(){
		sbiModule_restServices.promiseGet("2.0/datasources", "")
		.then(function(response) {
			$scope.dataSourceList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	}
	
	$scope.loadAllDataSources();
	
	/*
	 * 	@GET service that gets domain types for
	 *  scope 																	
	 */
	$scope.getDomainTypeScope = function(){	
		sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=DS_SCOPE")
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
		sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=CATEGORY_TYPE")
		.then(function(response) {
			$scope.categoryList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
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
		$log.info(item);
		$scope.selectedDataSet = angular.copy(item);
	};
	
	$scope.createNewDataSet = function() {
		$log.info("create");
	};
	
	$scope.saveDataSet = function() {
		$log.info("save");
	};
	
	$scope.cancelDataSet = function() {
		$log.info("cancel");
	};
	
	$scope.previewDataset = function () {
		console.log("info")
	}
	
	
};
