var app = angular.module('metaManager', [ 'ngMaterial', 'angular_table',
		'sbiModule', 'componentTreeModule', 'expander-box' ]);
app.config([ '$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
} ]);

app.controller('metaDefinitionController', [ '$scope', 'sbiModule_translate',
		'sbiModule_restServices', metaDefinitionControllerFunction ]);
app.controller('metaModelDefinitionController', [ '$scope',
		'sbiModule_translate', 'sbiModule_restServices',
		metaModelDefinitionControllerFunction ]);
app.controller('metaModelCreationController', [ '$scope',
		'sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder',
		metaModelCreationControllerFunction ]);

function metaDefinitionControllerFunction($scope, sbiModule_translate,
		sbiModule_restServices) {
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
		//TODO set model name here
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
function metaModelDefinitionControllerFunction($scope, sbiModule_translate,
		sbiModule_restServices) {
	$scope.dataSourceStructure = [];

	$scope.datasourceStructureColumnsList = [
			{
				label : sbiModule_translate.load("sbi.resources.columnname"),
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
		sbiModule_restServices
				.promiseGet("2.0/datasources",
						"structure/" + $scope.datasourceId)
				.then(
						function(response) {
							angular.copy($scope
									.sourceStructureBeautify(response.data),
									$scope.dataSourceStructure);
						},
						function(response) {
							sbiModule_restServices
									.errorHandler(
											response.data,
											sbiModule_translate
													.load("sbi.kpi.rule.load.datasource.error"));
						});
	}

	$scope.loadDatasourceTable();

}

function metaModelCreationControllerFunction($scope, sbiModule_translate,
		sbiModule_restServices, parametersBuilder) {
	$scope.selectedBusinessModel = {};
	$scope.selectedPhysicalModel = {};
	$scope.currentBusinessModelParameterCategories = [];
	$scope.currentPhysicalModelParameterCategories = [];
	$scope.businessModelTreeInterceptor = {};

	$scope.$watch(function() {
		var tmpSBM = {};
		angular.copy($scope.selectedBusinessModel, tmpSBM);
		delete tmpSBM.$parent;
		;
		return tmpSBM;
	}, function(newValue, oldValue) {
		if (!angular.equals(newValue, oldValue)) {
			$scope.businessModelTreeInterceptor.refreshTree();
		}
	}, true);

	$scope.selectedBusinessModelAttributes = [
			{
				label : sbiModule_translate.load("sbi.generic.name"),
				name : "name"
			},
			{
				label : sbiModule_translate
						.load("sbi.meta.model.table.primaryKey"),
				name : "identifier",
				transformer : function(row) {
					return "<md-checkbox ng-disabled='!scopeFunctions.existsBusinessModel(row)' ng-checked='scopeFunctions.isBusinessModelColumnPK(row)' ng-click='scopeFunctions.toggleBusinessModelColumnPK(row)' aria-label='isPrimaryKey'></md-checkbox>"
					// return "<md-checkbox ng-model='row.identifier'
					// aria-label='isPrimaryKey'></md-checkbox>"
				}
			},
			{
				label : sbiModule_translate.load("inUse"),
				name : "added",
				transformer : function(row) {
					return "<md-checkbox ng-checked='scopeFunctions.existsBusinessModel(row)' ng-click='scopeFunctions.toggleBusinessModel(row)' aria-label='isPrimaryKey'></md-checkbox>"
				}
			}

	];

	$scope.selectedBusinessModelAttributesScopeFunctions = {
		indexOfBc : function(bk) {
			for (var i = 0; i < $scope.selectedBusinessModel.columns.length; i++) {
				if (angular.equals(
						$scope.selectedBusinessModel.columns[i].uniqueName,
						bk.uniqueName)) {
					return i;
				}
			}
			return -1;
		},

		isBusinessModelColumnPK : function(row) {
			var index = this.indexOfBc(row);
			if (index != -1) {
				return $scope.selectedBusinessModel.columns[index].identifier;
			} else {
				return false;
			}

		},
		toggleBusinessModelColumnPK : function(row) {
			var index = this.indexOfBc(row);
			$scope.selectedBusinessModel.columns[index].identifier = !$scope.selectedBusinessModel.columns[index].identifier;
		},
		existsBusinessModel : function(row) {
			return (this.indexOfBc(row) != -1);
		},
		toggleBusinessModel : function(row) {
			var index = this.indexOfBc(row);
			if (index == -1) {
				$scope.selectedBusinessModel.columns.push(row);
			} else {
				$scope.selectedBusinessModel.columns.splice(index, 1);
			}
		}
	}

	$scope.selectBusinessModel = function(node) {
		$scope.selectedBusinessModel = node;
		angular.copy(parametersBuilder
				.extractCategories($scope.selectedBusinessModel.properties),
				$scope.currentBusinessModelParameterCategories);

	}
	$scope.selectPhysicalModel = function(node) {
		$scope.selectedPhysicalModel = node;
		angular.copy(parametersBuilder
				.extractCategories($scope.selectedPhysicalModel.properties),
				$scope.currentPhysicalModelParameterCategories);
	}

	$scope.businesslModel_getlevelIcon = function(node) {
		if (node.hasOwnProperty("simpleBusinessColumns")) {
			// is business model node

			// TO-DO manage folder by type
			return "fa fa-table";
		} else {
			// is column node
			if (node.identifier == true) {
				return "fa fa-key goldKey"
			}
			return "fa fa-columns";

		}
	}
	$scope.businessModel_isFolder = function(node) {
		return true;
	}

	$scope.getOpenFolderIcons = function(node) {
		return;
	}

	$scope.physicalModel_getlevelIcon = function(node) {
		if (node.hasOwnProperty("columns")) {
			// is business model node

			// TO-DO manage folder by type
			return "fa fa-table";
		} else {
			// is column node
			if (node.primaryKey == true) {
				return "fa fa-key goldKey"
			}
			return "fa fa-columns";

		}

	}
	$scope.physicalModel_isFolder = function(node) {
		return true;
	}

	$scope.isInbound = function(item) {
		return angular.equals(item.sourceTableName,
				$scope.selectedBusinessModel.name);
	}
	$scope.isOutbound = function(item) {
		return !angular.equals(item.sourceTableName,
				$scope.selectedBusinessModel.name);
	}

	$scope.modelMiscInfo = [ {
		name : "name",
		label : sbiModule_translate.load("name")
	}, {
		name : "description",
		label : sbiModule_translate.load("description")
	}, {
		name : "comment",
		label : sbiModule_translate.load("comment")
	}, {
		name : "dataType",
		label : sbiModule_translate.load("dataType")
	}, {
		name : "decimalDigits",
		label : sbiModule_translate.load("decimalDigits")
	}, {
		name : "typeName",
		label : sbiModule_translate.load("typeName")
	}, {
		name : "size",
		label : sbiModule_translate.load("size")
	}, {
		name : "octectLength",
		label : sbiModule_translate.load("octectLength")
	}, {
		name : "radix",
		label : sbiModule_translate.load("radix")
	}, {
		name : "defaultValue",
		label : sbiModule_translate.load("defaultValue")
	}, {
		name : "nullable",
		label : sbiModule_translate.load("nullable")
	}, {
		name : "position",
		label : sbiModule_translate.load("position")
	}, {
		name : "table",
		label : sbiModule_translate.load("table")
	}, ]
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

	// this.build=function(properties){
	// var propertiesStructure={};
	// for(var i=0;i<properties.length;i++){
	// var tmpProp=properties[i];
	// var struct=tmpProp.key.split(".");
	// if(!propertiesStructure.hasOwnProperty(struct[0])){
	// propertiesStructure[struct[0]]=[];
	// }
	//
	// // propertiesStructure[struct[0]][propertiesStructure[struct[0]].length]=
	// tmpProp.value;
	// propertiesStructure[struct[0]].push(properties[i]);
	//
	// }
	//
	// return propertiesStructure;
	// }

})