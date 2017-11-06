/**
 *
 */
var app = angular.module('exportersCatalogueModule',['ngMaterial','angular_list','angular_table','sbiModule','angular_2_col','angular-list-detail']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);
app.controller('exportersCatalogueController',["sbiModule_translate","sbiModule_restServices","$scope", "$http", "$mdDialog","$mdToast","$timeout","sbiModule_messaging",exportersCatalogueFunction]);

function exportersCatalogueFunction(sbiModule_translate,sbiModule_restServices,$scope,$http,$mdDialog,$mdToast,$timeout,sbiModule_messaging) {

	$scope.showMe = false;
	$scope.dirtyForm = false;

	$scope.selectedExporter = {
			engineId: "",
			domainId: "",
			domainLabel: "",
			engineLabel: "",
			defaultValue: false
	}

    $scope.engines = [];
	$scope.domains = [];

	$scope.getExporters = function () {
		sbiModule_restServices.promiseGet("2.0/exporters", '')
		.then(function(response) {
			$scope.myExporters = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
    }

	sbiModule_restServices.promiseGet("2.0/engines", '')
	.then(function(response) {
		$scope.engines = response.data;
	}, function(response) {
		sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
	});


	sbiModule_restServices.promiseGet("2.0/domains", '')
	.then(function(response) {
		$scope.domains = response.data;
	}, function(response) {
		sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
	});

	$scope.getExporters();

	$scope.deleteIcon = [{
		label: 'delete',
		icon: 'fa fa-trash',
		backgroundColor: 'transparent',
		action: function(item) {
			console.log(item);
			$scope.deleteExporter(item.engineId, item.domainId);
		}
	}];

	$scope.deleteExporter = function(engineId, domainId) {
		sbiModule_restServices.promiseDelete("2.0/exporters"+ "/" + engineId + "/" + domainId, '')
		.then(function(response) {
			$scope.getExporters();
			$scope.cancel();
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	};


	$scope.createExporters = function() {
      $scope.showMe = true;
	};

	$scope.saveExporter = function() {
		console.log($scope.selectedExporter);
		sbiModule_restServices.promisePost("2.0/exporters", "", angular.toJson($scope.selectedExporter))
		.then(function(response) {
			$scope.getExporters();
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	};

	$scope.loadExporter = function(item) {
       if($scope.dirtyForm) {
    	   $mdDialog.show($scope.confirm).then(function() {
    		   $scope.dirtyForm = false;
    		   $scope.selectedExporter = angular.copy(item);
    		   $scope.showMe = true;
    	   }, function() {
    		   $scope.showMe = true;
    	   });
       } else {
    	   $scope.selectedExporter = angular.copy(item);
    	   $scope.showMe = true;
       }
	}

	$scope.cancel = function() {
		$scope.selectedExporter.engineId = "";
		$scope.selectedExporter.domainId = "";
		$scope.selectedExporter.engineLabel = "";
		$scope.selectedExporter.domainLabel = "";
		$scope.selectedExporter.defaultValue = false;
		$scope.showMe = false;
		$scope.dirtyForm = false;
	}

}

