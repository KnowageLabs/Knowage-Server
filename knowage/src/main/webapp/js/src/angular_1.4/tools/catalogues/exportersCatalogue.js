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
	$scope.translate = sbiModule_translate;

	$scope.selectedExporter = {
			engineId: "",
			domainId: "",
			domainLabel: "",
			engineLabel: "",
			defaultValue: false,
			updateEngineId: "",
			updateDomainId: ""
	}

    $scope.engines = [];
	$scope.domains = [];

	$scope.searchEngineText = "";
	$scope.searchDomainText = "";

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
		$scope.domains = [];
		for (var i = 0; i < response.data.length; i++) {
			if(response.data[i].domainCode == "EXPORT_TYPE") {
				$scope.domains.push(response.data[i]);
			}
		}
	}, function(response) {
		sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
	});

	$scope.getExporters();

	$scope.exporterSpeedMenu = [
		{
			label: $scope.translate.load("sbi.exporters.delete"),
			icon: 'fa fa-trash',
			backgroundColor: 'transparent',
			action: function(item, event) {
				console.log(item);
				$scope.confirmDelete(item, event);
		    }
		}];

	$scope.deleteExporter = function(engineId, domainId) {
		sbiModule_restServices.promiseDelete("2.0/exporters"+ "/" + engineId + "/" + domainId, '')
		.then(function(response) {
			//$scope.getExporters();
			for (var i = 0; i < $scope.myExporters.length; i++) {
				if($scope.myExporters[i].engineId==engineId && $scope.myExporters[i].domainId==domainId){
					$scope.myExporters.splice(i,1);
				}
			}
			$scope.cancel();
			sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	};


	$scope.createExporters = function() {
      $scope.showMe = true;
      $scope.searchEngineText = "";
      $scope.searchDomainText = "";
      $scope.selectedExporter = {
				engineId: "",
				domainId: "",
				domainLabel: "",
				engineLabel: "",
				defaultValue: false
		}
	};

	$scope.saveOrUpdateExporter = function() {

		if($scope.selectedExporter.hasOwnProperty("persisted")) {
			console.log($scope.selectedExporter);
			sbiModule_restServices.promisePut("2.0/exporters"+ "/" + $scope.selectedExporter.updateEngineId + "/" + $scope.selectedExporter.updateDomainId, "", angular.toJson($scope.selectedExporter))
			.then(function(response) {
				$scope.getExporters();
				$scope.dirtyForm = false;
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});

		}else{
			console.log($scope.selectedExporter);

			sbiModule_restServices.promisePost("2.0/exporters", "", angular.toJson($scope.selectedExporter))

			.then(function(response) {
				$scope.getExporters();
				$scope.dirtyForm = false;
				$scope.showMe = false;
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});
	}
}

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

	$scope.exportersListColumns = [
		{"label": $scope.translate.load("sbi.exporters.engine.name"), "name": "engineLabel"},
		{"label": $scope.translate.load("sbi.exporters.domain.name"), "name": "domainLabel"}
	];

	$scope.confirm = $mdDialog
	.confirm()
	.title(sbiModule_translate.load("sbi.catalogues.generic.modify"))
	.content(
			sbiModule_translate
			.load("sbi.catalogues.generic.modify.msg"))
			.ariaLabel('Lucky day').ok(
					sbiModule_translate.load("sbi.general.yes")).cancel(
							sbiModule_translate.load("sbi.general.No"));

	 $scope.confirmDelete = function(item,ev) {
		    var confirm = $mdDialog.confirm()
		          .title(sbiModule_translate.load("sbi.catalogues.toast.confirm.title"))
		          .content(sbiModule_translate.load("sbi.catalogues.toast.confirm.content"))
		          .ariaLabel("confirm_delete")
		          .targetEvent(ev)
		          .ok(sbiModule_translate.load("sbi.general.continue"))
		          .cancel(sbiModule_translate.load("sbi.general.cancel"));
		    $mdDialog.show(confirm).then(function() {
		    	$scope.deleteExporter(item.engineId, item.domainId);
		    }, function() {

		    });
	};

	$scope.clearEngineSearch = function() {

		$scope.searchEngineText = "";
		$scope.searchDomainText = "";
	};
}

