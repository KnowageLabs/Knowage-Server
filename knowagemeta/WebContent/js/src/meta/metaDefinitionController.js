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



	$scope.saveModel=function(){
		var dataToSend={};
		dataToSend.datasourceId = $scope.datasourceId;
		dataToSend.physicalModels = $scope.physicalModels;
		dataToSend.businessModels = $scope.businessModels;
		// TODO set model name here
		dataToSend.modelName = 'test_model_hard_coded';
		dataToSend.physicalModel = $scope.removeCircularDependency(angular.extend([],$scope.physicalModel));
		dataToSend.businessModel =  $scope.removeCircularDependency(angular.extend([],$scope.businessModel));

		sbiModule_restServices
		.promisePost("1.0/metaWeb", "generateModel", dataToSend)
		.then(
				function(response) {

				},function(response) {
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.meta.model.generate.error"));
				});
	}

	$scope.closeMetaDefinition = function() {
		//TO-DO chiedere conferma prima di chiudere
		alert("chiude")
	}

	$scope.continueToMeta = function() {
			if ($scope.businessModels.length == 0) {
				sbiModule_restServices.errorHandler(sbiModule_translate.load("sbi.meta.model.business.select.required"), "");
			} else {
				$scope.createMeta();
			}
	};

	$scope.gobackToMetaDefinition=function(){
		//TO-DO chiedere conferma prima di andare indietro
		$scope.steps.current = 0;
	}

	 $scope.removeCircularDependency=function(data){
		 for(var i=0;i<data.length;i++){
			 for(var j=0;j<data[i].columns.length;j++){
				 delete data[i].columns[j].$parent;
			 }
		 }
		 return data;
	 }

	$scope.createMeta = function() {
		var dataToSend = {};
		dataToSend.datasourceId = $scope.datasourceId;
		dataToSend.physicalModels = $scope.physicalModels;
		dataToSend.businessModels = $scope.businessModels;
		// TODO set model name here
		dataToSend.modelName = 'test_model_hard_coded';
		sbiModule_restServices.alterContextPath("/knowagemeta");
		sbiModule_restServices
				.promisePost("1.0/metaWeb", "create", dataToSend)
				.then(
						function(response) {
							$scope.steps.current = 1;
							angular.copy(response.data.businessModel,
									$scope.businessModel);
							angular.copy(response.data.physicalModel,
									$scope.physicalModel);
						},
						function(response) {
							sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.datasource.error"));
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