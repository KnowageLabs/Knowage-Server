var app = angular.module('hierManager');

app.controller('hierBackupController', ['sbiModule_config','sbiModule_translate','sbiModule_restServices','sbiModule_logger',"$scope",'$mdDialog', hierarchyBackupFunction ]);

var rootStructure = {
		name:'root',
		id:'root',
		root: true,
		children: [],
		leaf:false
		};

function hierarchyBackupFunction(sbiModule_config,sbiModule_translate,sbiModule_restServices, sbiModule_logger, $scope, $mdDialog){
	
	$scope.translate = sbiModule_translate;
	$scope.restService = sbiModule_restServices;
	$scope.log = sbiModule_logger;
	
	/*Initialization Source variable*/
	$scope.hierarchiesTypeSrc = ['Master', $scope.translate.load('sbi.hierarchies.type.technical')];
	$scope.hierarchiesSrcMap = {};
	$scope.dimensionSrc = [];
	$scope.hierarchiesSrc=[];
	$scope.hierTreeSrc = [];
	$scope.hierTreeMapSrc = {};
	$scope.metadataMap = {};
	
	$scope.backupTable = [];
	
	$scope.backupColumns = [{'name':'name','label':'name'},{'name':'code','label':'code'},{'name':'description','label':'description'},{'name':'type','label':'type'}];
	$scope.backupSpeedMenu = [{
    	label: $scope.translate.load('sbi.generic.update2'),
    	icon:'fa fa-pencil',
    	color:'#153E7E',
    	action:function(item,event){
    		//$scope.editBackup(item);
    		}
    	},{
        	label: $scope.translate.load('sbi.generic.confirmRestore'),
        	icon:'fa fa-undo',
        	color:'#153E7E',
        	action:function(item,event){
        	//	$scope.restoreBackup(item);
        	}
    	},{
        	label: $scope.translate.load('sbi.generic.delete'),
        	icon:'fa fa-trash',
        	color:'#153E7E',
        	action:function(item,event){
        		//$scope.removeBackup(item);
        	}
    	}];
	
	$scope.backupTable = angular.copy(backupTableFake);
	
	$scope.indexOf = function(myArray, myElement, key) {
		if (myArray ===undefined || myElement === undefined) return -1;
		for (var i = 0; i < myArray.length; i++) {
			if (myArray[i][key] !== undefined && myArray[i][key] !== null && myArray[i][key] == myElement[key]) {
				return i;
			}
		}
		return -1;
	};
	
	$scope.restService.get("dimensions","getDimensions")
		.success(
			function(data, status, headers, config) {
				$scope.dimensionSrc = angular.copy(data);
				$scope.dimensionTarget = angular.copy(data);
			})	
		.error(function(data, status){
			var message = 'GET dimensions error of ' + data + ' with status :' + status;
			$scope.showAlert('ERROR',message);
			
		});
	
	$scope.getHierarchies = function (choose){
		var type = choose == 'src' ? $scope.hierTypeSrc : 'Technical' ;
		var dim = choose == 'src' ?  $scope.dimSrc : $scope.dimTarget;
		var map = choose == 'src' ? $scope.hierarchiesSrcMap : $scope.hierarchiesTargetMap; 
		if (type !== undefined && dim !== undefined){
			var dimName = dim.DIMENSION_NM;
			var keyMap = type+'_'+dimName; 
			var serviceName = (type.toUpperCase() == 'AUTO' || type.toUpperCase() == 'MASTER' )? 'getHierarchiesMaster' : 'getHierarchiesTechnical';
			
			//if the hierarchies[dim][type] is not defined, get the hierarchies and save in the map. Else, get them from the map 
			if (map[keyMap] === undefined){
				$scope.restService.get("hierarchies",serviceName,"dimension="+dimName)
					.success(
						function(data, status, headers, config) {
							if (data.errors === undefined){
								map[keyMap] = data;
								choose == 'src' ?  $scope.hierarchiesSrc = angular.copy(data) : $scope.hierarchiesTarget = angular.copy(data);
							}else{
								$scope.showAlert('ERROR',data.errors[0].message);
							}
						})
					.error(function(data, status){
						var message='GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status;
						$scope.showAlert('ERROR',message);
						
					});
			}else{
				choose == 'src' ? $scope.hierarchiesSrc = map[keyMap] : $scope.hierarchiesTarget = map[keyMap];
			}
		}
		//get the metadata for the dimension
		var dimName = dim.DIMENSION_NM;
		if ($scope.metadataMap !== undefined && $scope.metadataMap[dimName] == undefined){
			$scope.restService.get("hierarchies","nodeMetadata","dimension="+dimName+"&excludeLeaf=false")
			.success(
				function(data, status, headers, config) {
					if (data.errors === undefined){
						$scope.metadataMap[dimName] = data;
					}else{
						$scope.showAlert('ERROR',data.errors[0].message);
					}
			})
			.error(function(data, status){
				var message = 'GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status;
				$scope.showAlert('ERROR',message);
			});
		}
	}
	
	$scope.getTree = function(choose){
		var type = choose == 'src' ? $scope.hierTypeSrc : 'Technical' ;
		var dim = choose == 'src' ?  $scope.dimSrc : $scope.dimTarget;
		var hier = choose == 'src' ?  $scope.hierSrc : $scope.hierTarget;
		if (type && dim && hier){
			var keyMap = type + '_' + dim.DIMENSION_NM + '_' + hier.HIER_NM + '_' + dateFormatted;
			var config = {};
			config.params = {
				dimension: dim.DIMENSION_NM,
				filterType : type,
				filterHierarchy : hier.HIER_NM
			};
			if ($scope.hierTreeMapSrc[keyMap] === undefined && $scope.hierTreeMapTarget[keyMap] === undefined ){
				$scope.restService.get("hierarchies","getHierarchyTree",null,config)
					.success(
						function(data, status, headers, config) {
							if (data !== undefined && data.errors === undefined){
								if (typeof data =='object'){
									data = [data];
								}
								choose =='src' ? $scope.hierTreeSrc = data : $scope.hierTreeTarget = data;
								choose =='src' ? $scope.hierTreeMapSrc[keyMap] = data : $scope.hierTreeMapTarget[keyMap] = data;
								$scope.targetIsNew = false;
							}else{
								var params = 'date = ' + date + ' dimension = ' + dim.DIMENSION_NM + ' type = ' +  type + ' hierachies = ' + hier.HIER_NM;
								$scope.showAlert('ERROR',data.errors[0].message);
							}
						})
					.error(function(data, status){
						var params = 'date = ' + date + ' dimension = ' + dim.DIMENSION_NM + ' type = ' +  type + ' hierachies = ' + hier.HIER_NM;
						var message='GET tree source error with parameters' + params + ' with status: "' + status+ '"';
						$scope.showAlert('ERROR',message);
					});
			}else{
				choose =='src' ? $scope.hierTreeSrc = $scope.hierTreeMapSrc[keyMap] : $scope.hierTreeTarget = $scope.hierTreeMapTarget[keyMap];
			}
		}	
	}
	
	 $scope.editNode = function(item,parent){
		 var parentEl = angular.element(document.body);
		 var dimName = $scope.dimTarget !== undefined ? $scope.dimTarget.DIMENSION_NM : 'ACCOUNT'; //TODO remove hard coded for test
		 var metTmp =  $scope.metadataMap[dimName];
		 if (metTmp === undefined){
			 $scope.showAlert('Error','No metadata found for dimension '+ dimName );
		 }
		 //take generals_fields if it is root[parent is null], leaf_fields if it is leaf or node_fields if it is node
		 var metadata = parent == undefined || parent == null ? metTmp.GENERAL_FIELDS : item.leaf == true ? metTmp.LEAF_FIELDS : metTmp.NODE_FIELDS;
		 metadata == undefined ? metadata =  metTmp.GENERAL_FIELDS : metadata = metadata; //TODO remove hard coded for test
		 return $mdDialog.show({
					templateUrl: sbiModule_config.contextName +'/js/src/angular_1.4/tools/hierarchies/templates/hierSrcDialog.html',
					parent: angular.element(document.body),
					locals: {
						   translate: $scope.translate,
				           hier:  item,
				           metadata : metadata
				         },
					preserveScope : true,
					clickOutsideToClose:false,
					controller: DialogController 
				});
	
	 	function DialogController($scope, $mdDialog, translate, hier, metadata) {
	 		$scope.translate = translate;
			$scope.hier = hier;
			$scope.metadata = metadata;
	        $scope.closeDialog = function() {
	        	$mdDialog.cancel();
	        }
	        $scope.saveHier = function(){
	        	$mdDialog.hide(hier);
	        }
		 }
	 }
	
	$scope.addHier =  function(item,parent,event){
		var promise = $scope.editNode({},parent);
		promise
			.then(function(newItem){
					var tmpItem =angular.copy(item);
					for ( key in newItem){ 
						key =='children' ? tmpItem.children = [] : tmpItem[key] = newItem[key];
					}
					tmpItem.expanded = false;
					item.children.splice(0,0,tmpItem);
					$scope.treeTargetDirty = true;
				},function(){
				//nothing to do, request cancelled.
			});
	}
	
	$scope.modifyHier =  function(item,parent,event){
		var newItem = $scope.editNode(item,parent);
		if (newItem !== null && newItem !== undefined){
			item = newItem;
			$scope.treeTargetDirty = true;
		}
		
	}
	
	$scope.duplicateLeaf =  function(item,parent,event){
		var newItem = angular.copy(item);
		if ($scope.dimTarget && $scope.dimTarget.DIMENSION_NM && $scope.dimTarget.DIMENSION_NM.length > 0){ 
			var idx = $scope.indexOf(parent.children,item,'id');
			var allowDuplicate = $scope.metadataMap[$scope.dimTarget.DIMENSION_NM].CONFIGS.ALLOW_DUPLICATE;
			if (allowDuplicate == false || allowDuplicate == "false"){
				//must modify the dates of validity
				var promise = $scope.editNode(newItem,parent);
				promise.then(
					function(newItem){
						//check if newItem is totally equal to the old
						var isEqual=true;
						for ( k in newItem){
							if (newItem[k] != item[k]){
								isEqual = false;
								break;
							}
						}
						//if it is equal show Alert
						if (isEqual){
							$show.alert('ERROR', 'The duplicate leaf can not be equal to the original');
						}else{
							if (idx >=0){
								parent.children.splice(idx,0,newItem);
								$scope.treeTargetDirty = true;
							}
						}
					},function(){}	
				);
			}else{
				if (idx >=0){
					parent.children.splice(idx,0,newItem);
					$scope.treeTargetDirty = true;
				}
			}
		}
	}
	
	$scope.deleteHier =  function(item,parent,event){
		//rest service for deleting
		var response=$scope.showConfirm(item);
		response.then(
			function() {
				if (parent !== undefined && parent !== null){
					var idx = $scope.indexOf(parent.children,item,'id');
					parent.children.splice(idx,1);
				}else{
					item = {};
				}
				$scope.treeTargetDirty = true;
			}, function() {
				//nothing to do, response is 'cancel'
		});
	}
	
	$scope.menuOption = [{
			label: $scope.translate.load('sbi.generic.add'),
			showItem : function(item,event){
				//visible if it is NOT a leaf
				return item !== undefined && (item.leaf === undefined || item.leaf == false);
				},
			action: $scope.addHier
		},{
			label: 'Duplicate',
			showItem : function(item,event){
				//visible if it IS a leaf
				return item !== undefined && item.leaf !== undefined && item.leaf == true;
				},
			action : $scope.duplicateLeaf
		},{
			label: $scope.translate.load('sbi.roles.edit'),
			action : $scope.modifyHier
		},{
			label: $scope.translate.load('sbi.generic.delete'),
			action: $scope.deleteHier
		}
	];
	 	
	$scope.showConfirm = function(hier) {
	    var confirm = $mdDialog
			.confirm()
			.title('Delete ' + hier.name.toUpperCase())
			.content('Would you like to delete the item?')
			.ariaLabel('Lucky day')
			.ok('Yes')
			.cancel('No');
	    return  $mdDialog.show(confirm);
  	};
  	
  	//Create an alert dialog with a message
	$scope.showAlert = function (title, message){
		$scope.log.log(message);
		//if angular material version < 1.0.0_rc5 not has textContent function
		if (typeof $mdDialog.alert().textContent == 'function'){
			$mdDialog.show( 
				$mdDialog.alert()
			        .parent(angular.element(document.body))
			        .clickOutsideToClose(false)
			        .title(title)
			        .textContent(message) //FROM angular material 1.0 
			        .ok('Ok')
				);
		}else {
			$mdDialog.show( 
				$mdDialog.alert()
			        .parent(angular.element(document.body))
			        .clickOutsideToClose(false)
			        .title(title)
			        .content(message)
			        .ok('Ok')
		        );
		}
	};
	
	$scope.createTree = function(){
		if ($scope.dateTarget && $scope.dimTarget && $scope.hierTarget){
			var promise = $scope.editNode(angular.copy(rootStructure),null);
			promise
				.then(function(newItem){
						$scope.hierTreeTarget.splice(0,0,newItem);
						$scope.treeTargetDirty = true;
						$scope.targetIsNew = true;
					},function(){
					//nothing to do, request cancelled.
				}); 
		}
	}
	
	$scope.saveTree = function(){
		if ($scope.dateTarget && $scope.dimTarget && $scope.hierTarget && $scope.hierTreeTarget){
			//saveHierarchy
			var root = {};
			root.dimension = $scope.dimTarget.DIMENSION_NM;
			root.code = $scope.hierTarget.HIER_CD;
			root.description = $scope.hierTarget.HIER_DS;
			root.name = $scope.hierTarget.HIER_NM;
			root.type = $scope.hierTarget.HIER_TP;
			root.isInsert = $scope.targetIsNew;
			root.root = Array.isArray($scope.hierTreeTarget) ? angular.copy($scope.hierTreeTarget[0]) : angular.copy($scope.hierTreeTarget);
			root.root.$parent = undefined;
			//remove c
			var elements = [root.root];
			do{
				var el = elements.shift();
				el.$parent=null;
				if (el.children !== undefined && el.children.length > 0){
					for (var i =0 ; i<el.children.length;i++){
						elements.push(el.children[i]);
					}
				}
			}while(elements.length > 0);
			
			var jsonString = angular.toJson(root);
			var promise = $scope.restService.post('hierarchies','saveHierarchy',jsonString);
			promise
				.success(function (data){
					if (data.errors === undefined){
						$scope.treeTargetDirty = false;
						$scope.targetIsNew = false;
						$scope.showAlert('INFO','Succesfull upate');
					}else{
						$scope.showAlert('ERROR',data.errors[0].message);
					}
				})
				.error(function(data,status){
					$scope.showAlert('ERROR','Impossible to save the Tree');
				});
		}
	}
	
	$scope.formatData = function (date){
		return date.getFullYear() + '-' + date.getMonth()+'-'+ date.getDate();
	}
	
	$scope.debug = function (){
		var none='none';
	}
};
