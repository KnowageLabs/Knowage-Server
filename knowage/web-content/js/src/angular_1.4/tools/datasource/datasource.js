/**
 * 
 */
var app = angular.module('dataSourceModule', ['ngMaterial', 'angular_list', 'angular_table' ,'sbiModule', 'angular_2_col']);

app.controller('dataSourceController', ["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", dataSourceFunction]);

function dataSourceFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast){
	
	$scope.showme=false;
	$scope.translate = sbiModule_translate;
	$scope.dataSourceList = [];
	$scope.dialects = [];
	
	$scope.getDataSources = function(){
		console.log("Get DSRC");
		sbiModule_restServices.get("datasources", '').success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						//change sbi.glossary.load.error
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} else {
						console.log("took the datasources");
						
						//mostro tutti i ruoli
						$scope.dataSourceList = data.root;
						console.log($scope.dataSourceList);
						
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

				})	
	}
	$scope.getDataSources();
	
	$scope.loadDialects = function() {
		sbiModule_restServices.get("domains", "listValueDescriptionByType","DOMAIN_TYPE=DIALECT_HIB").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						//change sbi.glossary.load.error
						/*showToast(sbiModule_translate.load("sbi.glossary.load.error"),
								3000);*/
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} else {

						$scope.dialects = data;
						console.log("took the domains")
						console.log($scope.dialects);
					
					
					}
				}).error(function(data, status, headers, config) {
			showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

		})
	}
	$scope.loadDialects();
	
	$scope.loadDataSourceList = function(item) {
		$scope.showme=true;
	}

	
};