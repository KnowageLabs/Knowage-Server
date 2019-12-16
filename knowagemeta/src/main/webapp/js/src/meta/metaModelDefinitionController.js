angular.module('metaManager').controller('metaModelDefinitionController', [ '$location', '$scope', '$httpParamSerializer', 'sbiModule_translate', 'sbiModule_restServices', 'sbiModule_config', metaModelDefinitionControllerFunction]);

function metaModelDefinitionControllerFunction($location, $scope, $httpParamSerializer, sbiModule_translate,sbiModule_restServices, sbiModule_config) {
	$scope.dataSourceStructure = [];

	$scope.datasourceStructureColumnsList = [
			{
				label : sbiModule_translate.load("sbi.resources.tablename"),
				name : "columnName"
			},
			{
				label : sbiModule_translate.load("sbi.meta.model.physical"),
				name : "physicalModels",
				size : "150",
				customClass : "centerHeadText",
				transformer : function(item) {
					return "<md-checkbox  aria-label='check physical' class='centerCheckbox' ng-checked='scopeFunctions.isPhysicalModelChecked(row)' ng-click='scopeFunctions.togglePhysicalModel(row)' ></md-checkbox>"
				}

			},
			{
				label : sbiModule_translate.load("sbi.meta.model.business"),
				name : "businessModels",
				size : "150",
				transformer : function(item) {
					return "<md-checkbox aria-label='check business' class='centerCheckbox' ng-checked='scopeFunctions.isBusinessModelChecked(row)' ng-click='scopeFunctions.toggleBusinessModel(row)' ></md-checkbox>"
				}

			} ];

	$scope.datasourceStructureScopeFunctions = {
		translate : sbiModule_translate,
		isPhysicalModelChecked : function(row) {
			return ($scope.physicalModels.indexOf(row.columnName) != -1);
		},
		isBusinessModelChecked : function(row) {
			return ($scope.businessModels.indexOf(row.columnName) != -1);
		},
		togglePhysicalModel : function(row) {
			var index = $scope.physicalModels.indexOf(row.columnName);
			if (index != -1) {
				$scope.physicalModels.splice(index, 1);
				// remove also the businessModels if checked
				if (this.isBusinessModelChecked(row)) {
					this.toggleBusinessModel(row);
				}
			} else {
				$scope.physicalModels.push(row.columnName)
			}
		},
		toggleBusinessModel : function(row) {
			var index = $scope.businessModels.indexOf(row.columnName)
			if (index != -1) {
				$scope.businessModels.splice(index, 1);
			} else {
				$scope.businessModels.push(row.columnName)
				// add also physical model if is not checked
				if (!this.isPhysicalModelChecked(row)) {
					this.togglePhysicalModel(row);
				}

			}
		},
		allBusinessModelAreChecked : function() {
			return ($scope.businessModels.length == $scope.dataSourceStructure.length)
		},
		allPhysicalModelAreChecked : function() {
			return ($scope.physicalModels.length == $scope.dataSourceStructure.length)
		},
		toggleAllPhysicalModel : function() {
			var typeAddCheck = this.allPhysicalModelAreChecked();
			for (var i = 0; i < $scope.dataSourceStructure.length; i++) {
				if (this.isPhysicalModelChecked($scope.dataSourceStructure[i]) == typeAddCheck) {
					this.togglePhysicalModel($scope.dataSourceStructure[i]);
				}
			}

		},
		toggleAllBusinessModel : function() {
			var typeAddCheck = this.allBusinessModelAreChecked();
			for (var i = 0; i < $scope.dataSourceStructure.length; i++) {
				if (this.isBusinessModelChecked($scope.dataSourceStructure[i]) == typeAddCheck) {
					this.toggleBusinessModel($scope.dataSourceStructure[i]);
				}
			}

		}

	}

	$scope.sourceStructureBeautify = function(data) {
		var finalStructure = [];
		for ( var key in data) {
			var tmp = {};
			tmp.columnName = key;
			tmp.columns = [];
			for ( var colKey in data[key]) {
				tmp.columns.push(colKey)
			}
			finalStructure.push(tmp)
		}
		return finalStructure;
	}

	$scope.loadDatasourceTable = function() {
		// $scope.loadDatasourceSchemas(dsId)
		sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);

		var queryParameters = $location.search();

		var tablesFilters = {
				'tablePrefixLike' : queryParameters['tablePrefixLike'],
				'tablePrefixNotLike' : queryParameters['tablePrefixNotLike']
		};

		var queryString = $httpParamSerializer(tablesFilters);

		sbiModule_restServices.promiseGet("2.0/datasources","structure/" + $scope.datasourceId + ((queryString) ? '?' + queryString : ""))
				.then(
						function(response) {
							angular.copy($scope.sourceStructureBeautify(response.data),$scope.dataSourceStructure);
						},
						function(response) {
							sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.datasource.error"));
						});
	}

	if(translatedModel==null){
		$scope.loadDatasourceTable();
	}

}