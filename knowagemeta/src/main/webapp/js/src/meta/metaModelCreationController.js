angular.module('metaManager').controller('metaModelCreationController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',metaModelCreationControllerFunction ]);
angular.module('metaManager').controller('metaModelCreationPhysicalController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config',metaModelCreationPhysicalControllerFunction ]);
angular.module('metaManager').controller('metaModelCreationBusinessController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config','metaModelServices','$mdPanel','sbiModule_config','sbiModule_user',metaModelCreationBusinessControllerFunction ]);
angular.module('metaManager').controller('businessModelPropertyController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',businessModelPropertyControllerFunction ]);
angular.module('metaManager').controller('businessModelAttributeController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config','metaModelServices',businessModelAttributeControllerFunction ]);
angular.module('metaManager').controller('calculatedBusinessColumnsController', [ '$scope','sbiModule_translate', 'sbiModule_restServices','$mdDialog','sbiModule_config','metaModelServices',calculatedBusinessColumnsControllerFunction ]);
angular.module('metaManager').controller('businessViewJoinRelationshipsController', [ '$scope','sbiModule_translate', 'sbiModule_restServices',businessViewJoinRelationshipsControllerFunction ]);


function metaModelCreationControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout) {
$scope.tabResource={selectedBusinessTab:"propertiestab"};

}

function metaModelCreationPhysicalControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config) {
	$scope.selectedPhysicalModel = {};

	$scope.currentPhysicalModelParameterCategories = [];

	$scope.selectPhysicalModel = function(node) {
		$scope.selectedPhysicalModel = node;
		angular.copy(parametersBuilder
				.extractCategories($scope.selectedPhysicalModel.properties),
				$scope.currentPhysicalModelParameterCategories);
	}
	
	$scope.openedItems = [];

	$scope.openBusinessModel = function(model,e){
		e.stopPropagation();
		if($scope.openedItems.indexOf(model.name)!==-1){
			$scope.openedItems.splice($scope.openedItems.indexOf(model.name),1);
		}else{
			$scope.openedItems.push(model.name);
		}
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
			clickOutsideToClose:false,
			escapeToClose :false,
			fullscreen: true
		}).then(function(){
			$scope.physicalModelTreeInterceptor.refreshTree();
		});
	}


	$scope.fkTableColumns=[
		                       {
		                    	   label:sbiModule_translate.load("sbi.generic.name"),
		                    	   name:'name'
		                       },
		                       {
		                    	   label:sbiModule_translate.load("sbi.meta.source.columns"),
		                    	   name:'sourceColumns',
		                    	   transformer:function(item){
			                    		var toret=[];
			                    		for(var i=0;i<item.length;i++){
			                    			 toret.push(item[i].tableName+"."+item[i].name);
			                    		}
			                    		return toret.join(",");
		                    	   }
		                       },
		                       {
		                    	   label:sbiModule_translate.load("sbi.meta.target.columns"),
		                    	   name:'destinationColumns',
		                    	   transformer:function(item){
		                    		   var toret=[];
			                    		for(var i=0;i<item.length;i++){
			                    			 toret.push(item[i].tableName+"."+item[i].name);
			                    		}
			                    		return toret.join(",");
		                    	   }
		                       }
	                       ]
	
	
	$scope.getPropertyAttributes = function(prop){
		return prop[Object.keys(prop)[0]];
	}
}

function metaModelCreationBusinessControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config,metaModelServices,$mdPanel,sbiModule_config,sbiModule_user){
	$scope.selectedBusinessModel = {};
	$scope.sbiModule_config=sbiModule_config;
	$scope.sbiModule_user=sbiModule_user;
	$scope.currentBusinessModelParameterCategories = [];
	$scope.openedItems = [];

	$scope.openBusinessModel = function(model,e){
		e.stopPropagation();
		if($scope.openedItems.indexOf(model.uniqueName)!==-1){
			$scope.openedItems.splice($scope.openedItems.indexOf(model.uniqueName),1);
		}else{
			$scope.openedItems.push(model.uniqueName);
		}
	}

	$scope.selectBusinessModel = function(node) {
		$scope.tabResource.selectedBusinessTab="propertiestab";
		$scope.selectedBusinessModel = node;
		angular.copy(parametersBuilder.extractCategories($scope.selectedBusinessModel.properties),
				$scope.currentBusinessModelParameterCategories);
	};

	$scope.getBusinessModelType=function(bm){
		var prop=bm.properties;
		for(var i=0;i<prop.length;i++){
			var key = Object.keys(prop[i])[0];
			if(angular.equals(key,"structural.tabletype")){
				return prop[i][key].value;
			}
		}
		return "generic";
	};

	$scope.getBusinessModelColumnsType=function(bm){
		var prop=bm.properties;
		for(var i=0;i<prop.length;i++){
			var key = Object.keys(prop[i])[0];
			if(angular.equals(key,"structural.columntype")){
				return prop[i][key].value;
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
	};

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
	};

	$scope.businessModel_isFolder = function(node) {
		if (node.hasOwnProperty("simpleBusinessColumns")) {
			return !node.expanded;
		}else{
			return true
		}
	};

	$scope.addBusinessModel=function(){
		$mdDialog.show({
			controller: addBusinessClassController,
			preserveScope: true,
			locals: {businessModel:$scope.meta.businessModels, physicalModel: $scope.meta.physicalModels},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/addBusinessClass.jsp',
			clickOutsideToClose:false,
			escapeToClose :false,
			fullscreen: true
		});
	};

	$scope.addBusinessView=function(editMode){
		$mdDialog.show({
			controller: addBusinessViewController,
			preserveScope: true,
			locals: { originalPhysicalModel: $scope.meta.physicalModels,selectedBusinessModel: $scope.selectedBusinessModel,editMode:editMode},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/addBusinessView.jsp',
			clickOutsideToClose:false,
			escapeToClose :false,
			fullscreen: true
		}).then(function(){
			$scope.businessViewTreeInterceptor.refreshTree();
		});
	};

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
					$scope.selectedBusinessModel=undefined;
			   },function(response){
				   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
			   })


		   }, function() {
		   });
	};

	$scope.editTemporalHierarchy=function(){
		var config = {
				attachTo:  angular.element(document.body),
				controller: editTemporalHierarchyController,
				disableParentScroll: true,
				templateUrl: sbiModule_config.contextName + '/js/src/meta/templates/editTemporalHierarchy.jsp',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: true,
				locals: {selectedBusinessModel:$scope.selectedBusinessModel,originalOlapModels:$scope.meta.olapModels},

		};

		$mdPanel.open(config);
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


	$scope.initRoleVisibility=function(rv,val){
		if(!angular.equals("",val)){
			angular.copy(val.split(";"),rv );
		}
	}
	$scope.buildRoleVisibility=function(rv,val){
		val.value=rv.join(";");
	}
	
	$scope.getPropertyAttributes = function(prop){
		return prop[Object.keys(prop)[0]];
	}
	
	$scope.getPropertyKey = function(prop){
		return Object.keys(prop)[0];
	}
}

function businessModelAttributeControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config,metaModelServices ){
	$scope.attributesList=[];

	$scope.loadAttributesList=function(){
		if($scope.selectedBusinessModel.hasOwnProperty("physicalTable")){
			angular.copy($scope.meta.physicalModels[$scope.selectedBusinessModel.physicalTable.physicalTableIndex].columns,$scope.attributesList);
		}else{
			for(var i=0;i<$scope.selectedBusinessModel.physicalTables.length;i++){
//				angular.extend($scope.attributesList,$scope.meta.physicalModels[$scope.selectedBusinessModel.physicalTables[i].physicalTableIndex].columns)
				angular.copy($scope.attributesList.concat($scope.meta.physicalModels[$scope.selectedBusinessModel.physicalTables[i].physicalTableIndex].columns),$scope.attributesList);

			}
		}
	}
	//load attributes list the first time
	$scope.loadAttributesList();

	$scope.selectedBusinessModelAttributes = [
	                              			{
	                              				label : sbiModule_translate.load("sbi.generic.name"),
	                              				name : "name",
	                              				transformer: function(row){
	                              					var bc=$scope.selectedBusinessModelAttributesScopeFunctions.BusinessColumnFromPc(row);
	                              					if(bc==null){
	                              						return row;
	                              					}else{
	                              						return bc.name;
	                              					}

	                              				}
	                              			},
	                              			{
	                              				label : sbiModule_translate.load("sbi.meta.model.table.primaryKey"),
	                              				name : "identifier",
	                              				transformer : function(row) {
	                              					return "<md-checkbox ng-disabled='!scopeFunctions.existsBusinessModel(row)' ng-checked='scopeFunctions.isBusinessModelColumnPK(row)' ng-click='scopeFunctions.toggleBusinessModelColumnPK(row)' aria-label='isPrimaryKey'></md-checkbox>"
	                              					// return "<md-checkbox ng-model='row.identifier'
	                              					// aria-label='isPrimaryKey'></md-checkbox>"
	                              				}
	                              			},
	                              			{
	                              				label : sbiModule_translate.load("sbi.meta.model.inuse"),
	                              				name : "added",
	                              				transformer : function(row) {
	                              					return "<md-checkbox ng-checked='scopeFunctions.existsBusinessModel(row)' ng-click='scopeFunctions.toggleBusinessModel(row)' aria-label='isPrimaryKey'></md-checkbox>"
	                              				}
	                              			}

	                              	];

	// add referenced table if is a business view
	if($scope.selectedBusinessModel.hasOwnProperty("joinRelationships")){
		$scope.selectedBusinessModelAttributes.push({
														label : sbiModule_translate.load("sbi.meta.model.sourcetable"),
										  				name : "tableName",
										  			});
	}

	$scope.selectedBusinessModelAttributesScopeFunctions = {
			translate:sbiModule_translate,
			BusinessColumnFromPc:function(physicalColumnName){
				for (var i = 0; i < $scope.selectedBusinessModel.columns.length; i++) {
					if ($scope.selectedBusinessModel.columns[i].physicalColumn!=undefined && angular.equals( $scope.selectedBusinessModel.columns[i].physicalColumn.name, physicalColumnName)) {
						return $scope.selectedBusinessModel.columns[i];
					}
				}
				return null;
			},
			indexOfBcFromPt : function(bk) {
				for (var i = 0; i < $scope.selectedBusinessModel.columns.length; i++) {
					if ($scope.selectedBusinessModel.columns[i].physicalColumn!=undefined &&  angular.equals( $scope.selectedBusinessModel.columns[i].physicalColumn.name, bk.name)) {
						return i;
					}
				}
				return -1;
			},
			indexOfSimpleBcFromPt : function(bk) {
				for (var i = 0; i < $scope.selectedBusinessModel.simpleBusinessColumns.length; i++) {
					if (angular.equals($scope.selectedBusinessModel.simpleBusinessColumns[i].physicalColumn!=undefined &&  $scope.selectedBusinessModel.simpleBusinessColumns[i].physicalColumn.name, bk.name)) {
						return i;
					}
				}
				return -1;
			},
			indexOfBc : function(bk) {
				for (var i = 0; i < $scope.selectedBusinessModel.columns.length; i++) {
					if (angular.equals( $scope.selectedBusinessModel.columns[i].uniqueName, bk.uniqueName)) {
						return i;
					}
				}
				return -1;
			},

			isBusinessModelColumnPK : function(row) {
				var index = this.indexOfBcFromPt(row);
				if (index != -1) {
					return $scope.selectedBusinessModel.columns[index].identifier;
				} else {
					return false;
				}

			},
			toggleBusinessModelColumnPK : function(row) {
				var index = this.indexOfBcFromPt(row);
				$scope.selectedBusinessModel.columns[index].identifier = !$scope.selectedBusinessModel.columns[index].identifier;
			},
			existsBusinessModel : function(row) {
				return (this.indexOfBcFromPt(row) != -1);
			},
			toggleBusinessModel : function(row) {
				var index = this.indexOfBcFromPt(row);
				if (index == -1) {
					var indexSimpleBC = this.indexOfSimpleBcFromPt(row);
					if(indexSimpleBC!=-1){
						var tmpbm=$scope.selectedBusinessModel.simpleBusinessColumns[indexSimpleBC];
						delete tmpbm.$parent;
						$scope.selectedBusinessModel.columns.push(tmpbm);
					}else{
						$scope.createBusinessColumnFromPhysicalColumns(row,$scope.selectedBusinessModel)
					}


				} else {
					var isPresentInHierarchy = $scope.isPresentInOlapHierarchy($scope.selectedBusinessModel.columns[index],$scope.selectedBusinessModel);
					if (isPresentInHierarchy != false){
						sbiModule_restServices.errorHandler("This column is in connection with a hierarchy " +isPresentInHierarchy,"Unable to remove this column");
					} else {
						var isPresentInCalcField=$scope.isPresentInCalculatedColumn($scope.selectedBusinessModel.columns[index],$scope.selectedBusinessModel);
						if(isPresentInCalcField!=false){
							sbiModule_restServices.errorHandler("This column is in connection with a calculated field " +isPresentInCalcField,"Unable to remove this column");
						}else{
							$scope.deleteBusinessColumn($scope.selectedBusinessModel.columns[index].uniqueName,$scope.selectedBusinessModel);
						}
					}
				}
			}
		}

	$scope.isPresentInCalculatedColumn=function(bc,businessModel){
		for(var i=0;i<businessModel.calculatedBusinessColumns.length;i++){
			for(var j=0;j<businessModel.calculatedBusinessColumns[i].referencedColumns.length;j++){
				if(angular.equals(businessModel.calculatedBusinessColumns[i].referencedColumns[j].uniqueName,bc.uniqueName)){
					return businessModel.calculatedBusinessColumns[i].uniqueName;
				}
			}

		}
		return false

	}

	$scope.isPresentInOlapHierarchy=function(businessColumn,businessClass){
		var olapModels = $scope.meta.olapModels;
		for (var i=0; i< olapModels.length; i++){
			var dimensions = olapModels[i].dimensions
			for (var j=0; j < dimensions.length; j++){
				var hierarchies = dimensions[j].hierarchies;
				for (var k=0; k < hierarchies.length; k++){
					var levels = hierarchies[k].levels;
					for (var z=0; z < levels.length; z++ ){
						if (angular.equals(levels[z].column.uniqueName,businessColumn.uniqueName)){
							return hierarchies[k].name;
						}
					}
				}
			}
		}
		return false;
	}

	$scope.createBusinessColumnFromPhysicalColumns=function(pc,businessModel){
		sbiModule_restServices.promisePost("1.0/metaWeb", "createBusinessColumn",metaModelServices.createRequestRest({physicalTableName:pc.tableName,physicalColumnName:pc.name,businessModelUniqueName:businessModel.uniqueName}))
		   .then(function(response){
				metaModelServices.applyPatch(response.data);
		   },function(response){
			   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
		   })
	}
	$scope.deleteBusinessColumn=function(businessColumnUniqueName,businessModel){
		sbiModule_restServices.promisePost("1.0/metaWeb", "deleteBusinessColumn",metaModelServices.createRequestRest({businessColumnUniqueName:businessColumnUniqueName ,businessModelUniqueName:businessModel.uniqueName}))
		.then(function(response){
			metaModelServices.applyPatch(response.data);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
		})
	}

}

function businessViewJoinRelationshipsControllerFunction($scope,sbiModule_translate, sbiModule_restServices){
$scope.selectedBusinessViewJoinRelationships=[
               		                       {
            		                    	   label:sbiModule_translate.load("sbi.generic.name"),
            		                    	   name:'name'
            		                       },
            		                       {
            		                    	   label:sbiModule_translate.load("sbi.meta.source.columns"),
            		                    	   name:'sourceColumns',
            		                    	   transformer:function(item){
            			                    		var toret=[];
            			                    		for(var i=0;i<item.length;i++){
            			                    			 toret.push(item[i].tableName+"."+item[i].name);
            			                    		}
            			                    		return toret.join(",");
            		                    	   }
            		                       },
            		                       {
            		                    	   label:sbiModule_translate.load("sbi.meta.target.columns"),
            		                    	   name:'destinationColumns',
            		                    	   transformer:function(item){
            		                    		   var toret=[];
            			                    		for(var i=0;i<item.length;i++){
            			                    			 toret.push(item[i].tableName+"."+item[i].name);
            			                    		}
            			                    		return toret.join(",");
            		                    	   }
            		                       }
            	                       ]
}

function calculatedBusinessColumnsControllerFunction($scope,sbiModule_translate, sbiModule_restServices,$mdDialog,sbiModule_config,metaModelServices ){
	$scope.selectedBusinessModelCalculatedBusinessColumns=[
		                                              {
		                                            	  label:sbiModule_translate.load("sbi.generic.name"),
		                                            	  name:'name'
		                                              }
	                                              ]
	$scope.selectedBusinessModelCalculatedBusinessColumnsScopeFunctions={
			translate:sbiModule_translate,
			addCalculatedField : function(){
				$scope.addCalculatedField(false);
			}
	}
	$scope.addCalculatedField=function(editMode,currentCF){
		$mdDialog.show({
			controller: addCalculatedFieldController,
			preserveScope: true,
			locals: {selectedBusinessModel:$scope.selectedBusinessModel,editMode:editMode,currentCF:currentCF},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/addCalculatedField.jsp',
			clickOutsideToClose:false,
			escapeToClose :false,
			fullscreen: true
		}).then(function(){
		});
	}

	$scope.calculatedFieldSpeedOption=[
	                                   {
										label : sbiModule_translate.load("sbi.generic.delete"),
										icon:'fa fa-trash' ,
										backgroundColor:'transparent',
										color:'black',
										action : function(item,event) {
											$scope.deleteCalculatedField(item);
										}
										 },
	                                   {
	                                	   label : sbiModule_translate.load("sbi.generic.edit"),
	                                	   icon:'fa fa-pencil' ,
	                                	   backgroundColor:'transparent',
	                                	   color:'black',
	                                	   action : function(item,event) {
	                                		   $scope.addCalculatedField(true,item);
	                                	   }
	                                   }
			];

	$scope.deleteCalculatedField=function(item){
		 var confirm = $mdDialog.confirm()
		 .title(sbiModule_translate.load("sbi.meta.delete.calculatedField"))
		 .ariaLabel('delete Calculated')
		 .ok(sbiModule_translate.load("sbi.general.continue"))
		 .cancel(sbiModule_translate.load("sbi.general.cancel"));
		   $mdDialog.show(confirm).then(function() {

			   //delete the item;
			   sbiModule_restServices.promisePost("1.0/metaWeb", "deleteCalculatedField",metaModelServices.createRequestRest({name:item.name,sourceTableName:$scope.selectedBusinessModel.uniqueName}))
			   .then(function(response){
					metaModelServices.applyPatch(response.data);
			   },function(response){
				   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
			   })


		   }, function() {
		   });
	}
}