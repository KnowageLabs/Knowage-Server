angular.module('metaManager').controller('metaModelCreationController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',metaModelCreationControllerFunction ]);

angular.module('metaManager').controller('metaModelCreationPhysicalController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config',metaModelCreationPhysicalControllerFunction ]);

angular.module('metaManager').controller('metaModelCreationBusinessController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config','metaModelServices',metaModelCreationBusinessControllerFunction ]);
angular.module('metaManager').controller('businessModelPropertyController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',businessModelPropertyControllerFunction ]);
angular.module('metaManager').controller('businessModelAttributeController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',businessModelAttributeControllerFunction ]);
angular.module('metaManager').controller('businessModelInboundController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config','metaModelServices',businessModelInboundControllerFunction ]);
angular.module('metaManager').controller('businessModelOutboundController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config','metaModelServices',businessModelOutboundControllerFunction ]);


function metaModelCreationControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout) {


}

function metaModelCreationPhysicalControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config) {
	$scope.selectedPhysicalModel = {};
	$scope.physicalModelTreeInterceptor = {};
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
		if (node.hasOwnProperty("columns")) {
			return !node.expanded;
		}else{
			return true
		}
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

	$scope.refreshPhysicalModel=function(){
		$mdDialog.show({
			controller: refreshPhysicalModelController,
			preserveScope: true,
			locals: {businessModel:$scope.meta.businessModels, physicalModel: $scope.meta.physicalModels},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/refreshPhysicalModel.jsp',
			clickOutsideToClose:true,
			escapeToClose :true,
			fullscreen: true
		}).then(function(){
			$scope.physicalModelTreeInterceptor.refreshTree();
		});
	}

}

function metaModelCreationBusinessControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config,metaModelServices){
	$scope.selectedBusinessModel = {};
	$scope.currentBusinessModelParameterCategories = [];
	$scope.businessModelTreeInterceptor = {};
	$scope.businessViewTreeInterceptor = {};


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


	$scope.addBusinessModel=function(){
		$mdDialog.show({
			controller: addBusinessModelController,
			preserveScope: true,
			locals: {businessModel:$scope.meta.businessModels, physicalModel: $scope.meta.physicalModels},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/addBusinessModel.jsp',
			clickOutsideToClose:true,
			escapeToClose :true,
			fullscreen: true
		});
	}

	$scope.addBusinessView=function(){
		$mdDialog.show({
			controller: addBusinessViewController,
			preserveScope: true,
			locals: {businessModel:$scope.meta.businessModels, originalPhysicalModel: $scope.meta.physicalModels},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/addBusinessView.jsp',
			clickOutsideToClose:true,
			escapeToClose :true,
			fullscreen: true
		}).then(function(){
			$scope.businessViewTreeInterceptor.refreshTree();
		});
	}

	$scope.deleteCurrentBusiness=function(){
		var isBusinessClass=!$scope.selectedBusinessModel.hasOwnProperty("joinRelationships");

		 var confirm = $mdDialog.confirm()
		 .title( isBusinessClass ? sbiModule_translate.load("sbi.meta.delete.businessclass") : sbiModule_translate.load("sbi.meta.delete.businessview") )
		 .ariaLabel('delete Business')
		 .ok(sbiModule_translate.load("sbi.general.continue"))
		 .cancel(sbiModule_translate.load("sbi.general.cancel"));
		   $mdDialog.show(confirm).then(function() {

			   //delete the item;
			   sbiModule_restServices.promisePost("1.0/metaWeb",(isBusinessClass ? "deleteBusinessClass" : "deleteBusinessView"),metaModelServices.createRequestRest({name:$scope.selectedBusinessModel.uniqueName}))
			   .then(function(response){
					metaModelServices.applyPatch(response.data);
			   },function(response){
				   sbiModule_restServices.errorHandler(response.data,"Error while delete item");
			   })


		   }, function() {
		   });
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
function businessModelInboundControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config,metaModelServices){
	$scope.isInbound = function(item) {
		return angular.equals(item.sourceTableName,$scope.selectedBusinessModel.uniqueName);
	}

	$scope.inboundColumns = [{label:'Name',name:'name'},
	                      {label:'Source Table',name:'destinationTableName'},
	                      {label:'Source Columns',name:'destinationColumns',transformer:function(data){
	                    	  var ret = [];
	                    	  data.forEach(function(entry) {
	                    		    ret.push(entry.name);
	                    		  }, this);
	                    	  return ret.join(", ")
	                      }},
	                      {label:'Target Table',name:'sourceTableName'},
	                      {label:'Target Columns',name:'sourceColumns',transformer:function(data){
	                    	  var retD = [];
	                    	  data.forEach(function(entry) {
	                    		    retD.push(entry.name);
	                    		  }, this);
	                    	  return retD.join(", ")
	                      }}
	                      ];

	$scope.addNewInbound = function(){
			$mdDialog.show({
				controller: inboundModelPageControllerFunction,
				preserveScope: true,
				locals: {businessModel:$scope.meta.businessModels, selectedBusinessModel:$scope.selectedBusinessModel, sbiModule_restServices:sbiModule_restServices,metaModelServices:metaModelServices},
				templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/inboundModel.jsp',
				clickOutsideToClose:true,
				escapeToClose :true,
				fullscreen: true,
				});
	}

	$scope.inboundFunctions = {
			translate:sbiModule_translate,
			addNewInbound:$scope.addNewInbound
	};


}

function businessModelOutboundControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config,metaModelServices){
	$scope.isOutbound = function(item) {
		return !angular.equals(item.sourceTableName,$scope.selectedBusinessModel.uniqueName);
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
		$mdDialog.show({
			controller: outboundModelPageControllerFunction,
			preserveScope: true,
			locals: {businessModel:$scope.meta.businessModels, selectedBusinessModel:$scope.selectedBusinessModel, sbiModule_restServices:sbiModule_restServices,metaModelServices:metaModelServices},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/outboundModel.jsp',
			clickOutsideToClose:true,
			escapeToClose :true,
			fullscreen: true
		});
}

	$scope.outboundFunctions = {
			translate:sbiModule_translate,
			addNewOutbound:$scope.addNewOutbound
	};
}


function inboundModelPageControllerFunction($scope,$mdDialog, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout, businessModel, selectedBusinessModel,metaModelServices){
	$scope.translate = sbiModule_translate;
	$scope.cardinality = [{name:'1 to 1',value:'one-to-one'},{name:'1 to N',value:'one-to-many'},{name:'N to 1',value:'many-to-one'},
	                      {name:' 1* to 1',value:'optional-one-to-one'},{name:'1 to 1*',value:'one-to-optional-one'},{name:'1* to N',value:'optional-one-to-many'},
	                      {name:'1 to N*',value:'one-to-optional-many'}, {name:'N* to 1',value:'optional-many-to-one'}, {name:'N to 1*',value:'many-to-optional-one'}];
	$scope.businessName;
	$scope.businessModel = angular.copy(businessModel);
	$scope.selectedBusinessModel = angular.copy(selectedBusinessModel);
	$scope.leftElement = {};
	$scope.rightElement = {};
	$scope.dataSend = {};

	$scope.cardinalityValue = 0;

	$scope.tableToSimpleBound = function( model ){
		var a = [];
		if(model){
			if(model.columns)
				model.columns.forEach(function(item){
					a.push({name:item.name,uname:item.uniqueName, links:[]});
					});
				}
		return a;
	};

	$scope.simpleLeft = $scope.tableToSimpleBound($scope.selectedBusinessModel);
	 $scope.simpleRight = [];


	$scope.alterTableToSimpleBound = function(item){
		$scope.simpleRight = $scope.tableToSimpleBound(item);
	}

	$scope.createInbound = function(){
		$scope.dataSend.sourceColumns = [];
		$scope.dataSend.destinationColumns = [];
		$scope.dataSend.sourceTableName = $scope.selectedBusinessModel.uniqueName;
		$scope.dataSend.destinationTableName = $scope.rightElement.uniqueName;
		$scope.simpleLeft.forEach(function(entry) {
			if (entry.links.length > 0){
				$scope.dataSend.destinationColumns.push(entry.links[0].uname);
				$scope.dataSend.sourceColumns.push(entry.uname);
			}
		});

		var send = metaModelServices.createRequestRest($scope.dataSend);
		sbiModule_restServices.promisePost("1.0/metaWeb","addBusinessRelation",send)
		.then(function(response){
			metaModelServices.applyPatch(response.data);
		    $mdDialog.hide();
		}
		,function(response){
			sbiModule_restServices.errorHandler(response.data,"");
		})
	}

	$scope.cancel = function(){
		$mdDialog.cancel();
	}

	$scope.checkData = function(){
		var x = 0;
		$scope.simpleLeft.forEach(function(item){
			if (item.links.length > 0)
				x += 1;
		});
		return x > 0 ? false : true ;
		}
}

function outboundModelPageControllerFunction($scope,$mdDialog, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout, businessModel, selectedBusinessModel,metaModelServices){
	$scope.translate = sbiModule_translate;
	$scope.cardinality = [{name:'1 to 1',value:'one-to-one'},{name:'1 to N',value:'one-to-many'},{name:'N to 1',value:'many-to-one'},
	                      {name:' 1* to 1',value:'optional-one-to-one'},{name:'1 to 1*',value:'one-to-optional-one'},{name:'1* to N',value:'optional-one-to-many'},
	                      {name:'1 to N*',value:'one-to-optional-many'}, {name:'N* to 1',value:'optional-many-to-one'}, {name:'N to 1*',value:'many-to-optional-one'}];
	$scope.businessName;
	$scope.businessModel = businessModel;
	$scope.selectedBusinessModel = selectedBusinessModel;
	$scope.leftElement = {};
	$scope.rightElement = {};
	$scope.dataSend = {};

	$scope.cardinalityValue = 0;

	$scope.tableToSimpleBound = function( model ){
		var a = [];
		if(model){
			if(model.columns)
				model.columns.forEach(function(item){
					a.push({name:item.name,uname:item.uniqueName, links:[]});
					});
				}
		return a;
	};

	$scope.simpleLeft = $scope.tableToSimpleBound($scope.selectedBusinessModel);
	$scope.simpleRight = [];


	$scope.alterTableToSimpleBound = function(item){
		$scope.simpleRight = $scope.tableToSimpleBound(item);
	}

	$scope.createOutbound = function(){
		$scope.dataSend.sourceColumns = [];
		$scope.dataSend.destinationColumns = [];
		$scope.dataSend.sourceTableName = $scope.rightElement.uniqueName;
		$scope.dataSend.destinationTableName = $scope.selectedBusinessModel.uniqueName;
		$scope.simpleRight.forEach(function(entry) {
			if (entry.links.length > 0){
				$scope.dataSend.sourceColumns.push(entry.uname);
				$scope.dataSend.destinationColumns.push(entry.links[0].uname);
			}

		});

		var send = metaModelServices.createRequestRest($scope.dataSend);
		sbiModule_restServices.promisePost("1.0/metaWeb","addBusinessRelation",send)
		.then(function(response){
			metaModelServices.applyPatch(response.data);
		    $mdDialog.hide();
		}
		,function(response){
			sbiModule_restServices.errorHandler(response.data,"");
		})
	}

	$scope.cancel = function(){
		$mdDialog.cancel();
	}

	$scope.checkData = function(){
		var x = 0;
		$scope.simpleRight.forEach(function(item){
			if (item.links.length > 0)
				x += 1;
		});
		return x > 0 ? false : true ;
	}

}