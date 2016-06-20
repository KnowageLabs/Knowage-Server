angular.module('metaManager').controller('metaModelCreationController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',metaModelCreationControllerFunction ]);

angular.module('metaManager').controller('metaModelCreationPhysicalController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',metaModelCreationPhysicalControllerFunction ]);

angular.module('metaManager').controller('metaModelCreationBusinessController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',metaModelCreationBusinessControllerFunction ]);
angular.module('metaManager').controller('businessModelPropertyController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',businessModelPropertyControllerFunction ]);
angular.module('metaManager').controller('businessModelAttributeController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',businessModelAttributeControllerFunction ]);
angular.module('metaManager').controller('businessModelInboundController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',businessModelInboundControllerFunction ]);
angular.module('metaManager').controller('businessModelOutboundController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',businessModelOutboundControllerFunction ]);

function metaModelCreationControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout) {
	$scope.getOpenFolderIcons = function(node) {
		return;
	}
}

function metaModelCreationPhysicalControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout) {
	$scope.selectedPhysicalModel = {};

	$scope.currentPhysicalModelParameterCategories = [];

	$scope.selectPhysicalModel = function(node) {
		$scope.selectedPhysicalModel = node;
		angular.copy(parametersBuilder
				.extractCategories($scope.selectedPhysicalModel.properties),
				$scope.currentPhysicalModelParameterCategories);
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

	$scope.physicalModelMiscInfo = [ {
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
	}];
}

function metaModelCreationBusinessControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout){
	$scope.selectedBusinessModel = {};
	$scope.currentBusinessModelParameterCategories = [];
	$scope.businessModelTreeInterceptor = {};

	$scope.tmpBMWatcher={};
	$scope.$watch(function() {
		var tmpSBM = {};
		angular.copy($scope.selectedBusinessModel, tmpSBM);
		delete tmpSBM.$parent;

		return tmpSBM;
	}, function(newValue, oldValue) {
		if (!angular.equals(newValue, oldValue)) {
			angular.copy(newValue,$scope.tmpBMWatcher);
			$timeout(function(){
				if(angular.equals(newValue,$scope.tmpBMWatcher)){
					$scope.businessModelTreeInterceptor.refreshTree();
				}
			},500);
		}
	}, true);

	$scope.selectBusinessModel = function(node) {
		$scope.selectedBusinessModel = node;
		angular.copy(parametersBuilder.extractCategories($scope.selectedBusinessModel.properties),
				$scope.currentBusinessModelParameterCategories);

	};

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

}
function businessModelPropertyControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout){
	$scope.businessModelMiscInfo = [ {
		name : "name",
		label : sbiModule_translate.load("name")
	}, {
		name : "description",
		label : sbiModule_translate.load("description")
	}
	];
}
function businessModelAttributeControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout){
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
}
function businessModelInboundControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout){
	$scope.isInbound = function(item) {
		return angular.equals(item.sourceTableName,
				$scope.selectedBusinessModel.name);
	}
}
function businessModelOutboundControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout){
	$scope.isOutbound = function(item) {
		return !angular.equals(item.sourceTableName,
				$scope.selectedBusinessModel.name);
	}
}