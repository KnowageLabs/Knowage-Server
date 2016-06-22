angular.module('metaManager').controller('metaModelCreationController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',metaModelCreationControllerFunction ]);

angular.module('metaManager').controller('metaModelCreationPhysicalController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',metaModelCreationPhysicalControllerFunction ]);

angular.module('metaManager').controller('metaModelCreationBusinessController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',metaModelCreationBusinessControllerFunction ]);
angular.module('metaManager').controller('businessModelPropertyController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',businessModelPropertyControllerFunction ]);
angular.module('metaManager').controller('businessModelAttributeController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',businessModelAttributeControllerFunction ]);
angular.module('metaManager').controller('businessModelInboundController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config',businessModelInboundControllerFunction ]);
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


	$scope.getBusinessModelType=function(bm){
		var prop=bm.properties;
		for(var i=0;i<prop.length;i++){
			if(angular.equals(prop[i].key,"structural.tabletype")){
				return prop[i].value.value;
			}
		}
		return "generic";
	};

	$scope.getBusinessModelColumnsType=function(bm){
		var prop=bm.properties;
		for(var i=0;i<prop.length;i++){
			if(angular.equals(prop[i].key,"structural.columntype")){
				return prop[i].value.value;
			}
		}
	};
	$scope.businessModelIconType={
			"generic" :"fa fa-table",
			"cube":"fa fa-cube",
			"dimension":"fa fa-square-o",
			"temporal dimension":"fa fa-calendar",
			"time dimension":"fa fa-clock-o",
			"geographic dimension":"fa fa-globe",
			"measure":"fa fa-barcode",
			"attribute":"fa fa-circle-o",
			"calendar":"fa fa-calendar-check-o",
	}

	$scope.businesslModel_getlevelIcon = function(node) {
		if (node.hasOwnProperty("simpleBusinessColumns")) {
			// is business model node

			// TO-DO manage folder by type
			return $scope.businessModelIconType[$scope.getBusinessModelType(node)] ||  "fa fa-table";
		} else {
			// is column node
			if (node.identifier == true) {
				return "fa fa-key goldKey"
			}
			return $scope.businessModelIconType[$scope.getBusinessModelColumnsType(node)];

		}
	}
	$scope.businessModel_isFolder = function(node) {
		if (node.hasOwnProperty("simpleBusinessColumns")) {
			return !node.expanded;
		}else{
			return true
		}
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
function businessModelInboundControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config){
	$scope.isInbound = function(item) {
		return angular.equals(item.sourceTableName,
				$scope.selectedBusinessModel.name);
	}

	$scope.inboundColumns = [{label:'Name',name:'name'},
	                      {label:'Source Table',name:'sourceTableName'},
	                      {label:'Source Columns',name:'sourceColumns',transformer:function(data){
	                    	  var ret = [];
	                    	  data.forEach(function(entry) {
	                    		    ret.push(entry.name);
	                    		  }, this);
	                    	  return ret.join(", ")
	                      }},
	                      {label:'Target Table',name:'destinationTableName'},
	                      {label:'Target Columns',name:'destinationColumns',transformer:function(data){
	                    	  var retD = [];
	                    	  data.forEach(function(entry) {
	                    		    retD.push(entry.name);
	                    		  }, this);
	                    	  return retD.join(", ")
	                      }}
	                      ];

	$scope.addNewInbound = function(){
			$mdDialog.show({

				preserveScope: true,
				templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/inboundModel.jsp',
				clickOutsideToClose:true,
				escapeToClose :true,
				fullscreen: true,
//				locals:{url:sbiModule_config.contextName+'/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/meta/metaDefinition.jsp&datasourceId='+dsId}
//				locals:{url:"/knowagemeta/restful-services/1.0/pages/edit?datasourceId="+dsId+"&user_id="+sbiModule_user.userId}
			});

	}

	$scope.inboundFunctions = {
			translate:sbiModule_translate,
			addNewInbound:$scope.addNewInbound
	};


}
function businessModelOutboundControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout){
	$scope.isOutbound = function(item) {
		return !angular.equals(item.sourceTableName,
				$scope.selectedBusinessModel.name);
	}

	$scope.outboundColumns = [{label:'Name',name:'name'},
		                      {label:'Source Table',name:'destinationTableName'},
		                      {label:'Source Columns',name:'destinationColumns',transformer:function(data){
		                    	  var retD = [];
		                    	  data.forEach(function(entry) {
		                    		    retD.push(entry.name);
		                    		  }, this);
		                    	  return retD.join(", ")
		                      }},
		                      {label:'Target Table',name:'sourceTableName'},
		                      {label:'Target Columns',name:'sourceColumns',transformer:function(data){
		                    	  var ret = [];
		                    	  data.forEach(function(entry) {
		                    		    ret.push(entry.name);
		                    		  }, this);
		                    	  return ret.join(", ")
		                      }}
		                      ];

	$scope.addNewOutbound = function(){

	}

	$scope.outboundFunctions = {
			translate:sbiModule_translate,
			addNewOutbound:$scope.addNewOutbound
	};
}