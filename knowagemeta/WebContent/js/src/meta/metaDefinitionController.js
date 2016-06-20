var app = angular.module('metaManager', [ 'ngMaterial', 'angular_table','sbiModule', 'componentTreeModule', 'expander-box' ]);

app.config([ '$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
} ]);

app.controller('metaDefinitionController', [ '$scope', 'sbiModule_translate','sbiModule_restServices', metaDefinitionControllerFunction ]);



function metaDefinitionControllerFunction($scope, sbiModule_translate,sbiModule_restServices) {
	$scope.translate = sbiModule_translate;
	$scope.steps = {
		current : 0
	};
	$scope.datasourceId = datasourceId;
	$scope.meta = {};
	$scope.physicalModels = []; // array of table to transform in physical model
	$scope.businessModels = []; // array of table to transform in business model

	$scope.physicalModel = [];
	$scope.businessModel = [];

	$scope.closeMetaDefinition = function() {
		if ($scope.steps.current == 1) {
			$scope.steps.current = 0;
		} else {
			alert("close");
		}
	}
	$scope.continueToMeta = function() {
		if ($scope.steps.current == 1) {
			alert("Finish");
		} else {

			if ($scope.businessModels.length == 0) {
				sbiModule_restServices.errorHandler(sbiModule_translate
						.load("sbi.meta.model.business.select.required"), "");
			} else {
				$scope.createMeta();
			}

		}
	}

	$scope.createMeta = function() {
		var dataToSent = {};
		dataToSent.datasourceId = $scope.datasourceId;
		dataToSent.physicalModels = $scope.physicalModels;
		dataToSent.businessModels = $scope.businessModels;
		// TODO set model name here
		dataToSent.modelName = 'test_model_hard_coded';
		sbiModule_restServices.alterContextPath("/knowagemeta");
		sbiModule_restServices
				.promisePost("1.0/metaWeb", "create", dataToSent)
				.then(
						function(response) {
							$scope.steps.current = 1;
							angular.copy(response.data.businessModel,
									$scope.businessModel);
							angular.copy(response.data.physicalModel,
									$scope.physicalModel);
						},
						function(response) {
							sbiModule_restServices
									.errorHandler(
											response.data,
											sbiModule_translate
													.load("sbi.kpi.rule.load.datasource.error"));
						});

	}

}


angular.module('metaManager').filter('filterByCategory', function() {
	return function(items, categoryName) {

		var filtered = [];
		angular.forEach(items, function(item) {
			if (angular.equals(item.key.split(".")[0], categoryName)) {
				filtered.push(item);
			}
		});
		return filtered;
	};
});

angular.module('metaManager').service("parametersBuilder", function() {
	this.extractCategories = function(properties) {
		var propertiesCat = [];
		for (var i = 0; i < properties.length; i++) {
			var tmpProp = properties[i];
			var struct = tmpProp.key.split(".");
			if (propertiesCat.indexOf(struct[0]) == -1) {
				propertiesCat.push(struct[0]);
			}
		}
		return propertiesCat;
	}

})