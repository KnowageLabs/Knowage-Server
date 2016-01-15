var app = angular.module('hierManager');

app.controller('hierTechnController', ['$timeout','sbiModule_config','sbiModule_translate','sbiModule_restServices','sbiModule_logger',"$scope",'$mdDialog', hierarchyTechFunction ]);

function hierarchyTechFunction($timeout,sbiModule_config,sbiModule_translate,sbiModule_restServices, sbiModule_logger, $scope, $mdDialog){
	
	$scope.translate = sbiModule_translate;
	$scope.restService = sbiModule_restServices;
	$scope.log = sbiModule_logger;
	$scope.filterBySrc = '';
	$scope.orderByFields = ['name','id'];
	$scope.doBackup = true;
	$scope.metadataMap = {};
	
	/*Initialization Source variable*/
	$scope.hierarchiesTypeSrc = ['MASTER','TECHNICAL'];
	$scope.dateSrc = new Date();
	$scope.hierarchiesSrcMap = {};
	$scope.dimensionSrc = [];
	$scope.hierarchiesSrc=[];
	$scope.hierTreeSrc = [];
	$scope.hierTreeCacheSrc = {};
	$scope.seeFilterSrc=false;
	$scope.showLoadingSrc= false;
	
	/*Initialization Target variable*/
	$scope.dateTarget = new Date();
	$scope.hierarchiesTargetMap = {};
	$scope.hierTreeTarget = [];
	$scope.hierarchiesTarget = [];
	$scope.hierTreeCacheTarget = {};
	$scope.seeFilterTarget=false;
	$scope.treeTargetDirty=false;
	$scope.targetIsNew = false; //flag, if is true, the tree create from user, else is get from server
	$scope.showLoadingTarget = false;
	
	$scope.keys = {'subfolders' : 'children'};
	
//	$scope.hierTreeSrc.push(angular.copy(dataJson));
//	$scope.hierTreeTarget.push(angular.copy(dataJson));
	
	/*Drag and Drop option*/
	$scope.treeTargetOptions = {
		beforeDrop : function(e){
			//set dirty the tree and update the level of the object dragged
			$scope.treeTargetDirty = true;
			e.source.cloneModel.LEVEL = e.dest.nodesScope.$nodeScope.$modelValue.LEVEL + 1;
		},
		beforeDrag : function(sourceNodeScope){
			if (sourceNodeScope.$treeScope.cloneEnabled==false){
				sourceNodeScope.$treeScope.cloneEnabled = true;
			}
			return true;
		}
	};
	
	$scope.toogleSeeFilter= function(choose){
		if (choose == 'src'){
			$scope.seeFilterSrc = !$scope.seeFilterSrc;
		}else{
			$scope.seeFilterTarget = !$scope.seeFilterTarget;
		}
	}
	
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
				//$scope.dimensionTarget = angular.copy(data);
			})	
		.error(function(data, status){
			var message = 'GET dimensions error of ' + data + ' with status :' + status;
			$scope.showAlert('ERROR',message);
			
		});
	
	$scope.getHierarchies = function (choose){
		var type = choose == 'src' ? $scope.hierTypeSrc : 'Technical' ;
		//var dim = choose == 'src' ?  $scope.dimSrc : $scope.dimTarget;
		var dim = $scope.dimSrc;
		var map = choose == 'src' ? $scope.hierarchiesSrcMap : $scope.hierarchiesTargetMap; 
		if (type !== undefined && dim !== undefined){
			var dimName = dim.DIMENSION_NM;
			var keyMap = type+'_'+dimName; 
			var serviceName = (type.toUpperCase() == 'AUTO' || type.toUpperCase() == 'MASTER' )? 'getHierarchiesMaster' : 'getHierarchiesTechnical';
			
			//if the hierarchies[dim][type] is not defined, get the hierarchies and save in the map. Else, get them from the map 
			if (map[keyMap] === undefined){
				$scope.toogleLoading(choose);
				$scope.restService.get("hierarchies",serviceName,"dimension="+dimName)
					.success(
						function(data, status, headers, config) {
							if (data.errors === undefined){
								map[keyMap] = data;
								choose == 'src' ?  $scope.hierarchiesSrc = angular.copy(data) : $scope.hierarchiesTarget = angular.copy(data);
							}else{
								$scope.showAlert('ERROR',data.errors[0].message);
							}
							$scope.toogleLoading(choose);
						})
					.error(function(data, status){
						var message='GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status;
						$scope.showAlert('ERROR',message);
						$scope.toogleLoading(choose);
					});
			}else{
				choose == 'src' ? $scope.hierarchiesSrc = map[keyMap] : $scope.hierarchiesTarget = map[keyMap];
			}
		}
		//get the metadata for the dimension
		$scope.getMetadata(dim);
	}
	
	$scope.getMetadata = function (dim){
		if (dim){
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
	}
	
	$scope.getTree = function(choose,dateFilter,seeElement){
		var type = choose == 'src' ? $scope.hierTypeSrc : 'Technical' ;
		//var dim = choose == 'src' ?  $scope.dimSrc : $scope.dimTarget;
		var dim = $scope.dimSrc ;
		var date = choose == 'src' ? $scope.dateSrc : $scope.dateTarget;
		var hier = choose == 'src' ?  $scope.hierSrc : $scope.hierTarget;
		if (type && dim && hier && date){
			var dateFormatted =$scope.formatDate(date);
			var keyMap = type + '_' + dim.DIMENSION_NM + '_' + hier.HIER_NM + '_' + dateFormatted;
			var config = {};
			config.params = {
				dimension: dim.DIMENSION_NM,
				filterType : type,
				filterHierarchy : hier.HIER_NM,
				validityDate : dateFormatted
			};
			if (dateFilter !== undefined && dateFilter!== null && dateFilter.length > 0){
				config.params.filterDate = ''+dateFilter;
				keyMap = keyMap + '_' + dateFilter;
			}
			if (seeElement == true){
				config.params.filterDimension = seeElement;
				keyMap = keyMap + '_' + seeElement;
			}
			var hierMap = choose == 'src' ? $scope.hierTreeCacheSrc : $scope.hierTreeCacheTarget;
			if (hierMap[keyMap] === undefined){
				$scope.toogleLoading(choose);
				$scope.restService.get("hierarchies","getHierarchyTree",null,config)
					.success(
						function(data, status, headers, config) {
							if (data !== undefined && data.errors === undefined){
								if (typeof data =='object'){
									data = [data];
								}
								choose =='src' ? $scope.hierTreeSrc = data : $scope.hierTreeTarget = data;
								choose =='src' ? $scope.hierTreeCacheSrc[keyMap] = angular.copy(data) : $scope.hierTreeCacheTarget[keyMap] = angular.copy(data);
								$scope.targetIsNew = false;
							}else{
								var params = 'date = ' + date + ' dimension = ' + dim.DIMENSION_NM + ' type = ' +  type + ' hierachies = ' + hier.HIER_NM;
								$scope.showAlert('ERROR',data.errors[0].message);
							}
							$scope.toogleLoading(choose);
						})
					.error(function(data, status){
						var params = 'date = ' + date + ' dimension = ' + dim.DIMENSION_NM + ' type = ' +  type + ' hierachies = ' + hier.HIER_NM;
						var message='GET tree source error with parameters' + params + ' with status: "' + status+ '"';
						$scope.showAlert('ERROR',message);
						$scope.toogleLoading(choose);
					});
			}else{
				choose =='src' ? $scope.hierTreeSrc = angular.copy($scope.hierTreeCacheSrc[keyMap]) : $scope.hierTreeTarget = angular.copy($scope.hierTreeCacheTarget[keyMap]);
			}
		}	
	}
	
	 $scope.editNode = function(item,parent,forceEditable){
		 var parentEl = angular.element(document.body);
		 var dimName = $scope.dimSrc  !== undefined ? $scope.dimSrc.DIMENSION_NM : '';
		 var metTmp =  $scope.metadataMap[dimName];
		 if (metTmp === undefined){
			 $scope.showAlert('Error','No metadata found for dimension '+ dimName );
		 }
		 //take generals_fields if it is root[parent is null], leaf_fields if it is leaf or node_fields if it is node
		 var metadata = parent == undefined || parent == null ? metTmp.GENERAL_FIELDS : item.leaf == true ? metTmp.LEAF_FIELDS : metTmp.NODE_FIELDS;
		 if (metadata && forceEditable == true){
			 for (var i = 0 ; i < metadata.length; i++){
				 metadata[i].EDITABLE=true;
				 metadata[i].VISIBLE=true;
			 }
		 }
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
					controller: $scope.hierSrcDialogController 
				});
	 }
	
	$scope.hierSrcDialogController = function($scope, $mdDialog, translate, hier, metadata) {
		$scope.translate = translate;
		$scope.hier = hier;
		$scope.metadata = metadata;
		$scope.closeDialog = function() {
			$mdDialog.cancel();
		}
		$scope.saveHier = function(){
			$mdDialog.hide($scope.hier);
		}
	}
	
	$scope.createEmptyNode = function (type){
		var dimName = $scope.dimSrc !== undefined ? $scope.dimSrc.DIMENSION_NM : ''; 
		var metTmp =  $scope.metadataMap[dimName];
		if (metTmp === undefined){
			$scope.showAlert('Error','No metadata Node found for dimension '+ dimName );
			return null;
		}
		var metadata = type == "root" ? metTmp.GENERAL_FIELDS: type =="node" ? metTmp.NODE_FIELDS  : metTmp.LEAF_FIELDS;
		var node = {};
		for (var i =0; i < metadata.length; i++){
			if (metadata[i].TYPE == 'Number'){
				node[metadata[i].ID] = -1;
			}else if(metadata[i].TYPE == 'Date'){
				node[metadata[i].ID] = new Date();
			}else{
				node[metadata[i].ID] = '';
			}
		}
		node.children = [{fake:true,name:'',id:'',visible:true,checked:false,expanded:false}];
		node.expanded = false;
		node.visible=true;
		node.type="folder";
		node.checked = false;
		node.leaf = type == "leaf";
		node.root = type == "root";
		return node;
	}
	
	$scope.addHier =  function(item,parent,event){
		var promise = $scope.editNode({},parent);
		promise //TODO correggere inserimento
			.then(function(newItem){
					var tmpItem = $scope.createEmptyNode('node');
					for ( key in newItem){ 
						tmpItem[key] = newItem[key];
					}
					var keyName = tmpItem.aliasName !== undefined ? tmpItem.aliasName : $scope.dimSrc.DIMENSION_NM + "_NM_LEV";
					var keyId = tmpItem.aliasId !== undefined ? tmpItem.aliasId : $scope.dimSrc.DIMENSION_NM + "_CD_LEV";
					tmpItem.name = tmpItem[keyName];
					tmpItem.id = tmpItem[keyId];
					tmpItem.$parent = item;
					tmpItem.LEVEL = tmpItem.$parent.LEVEL + 1; 
					if (item.children.length == 1 && item.children[0].fake == true){
						item.children = [tmpItem];
					}else{
						item.children.splice(0,0,tmpItem);
					}
					$scope.treeTargetDirty = true;
				},function(){
				//nothing to do, request cancelled.
			});
	}
	
	$scope.modifyHier =  function(item,parent,event){
		var promise = $scope.editNode(item,parent);
		promise.then(function (newItem){
			if (newItem !== null && newItem !== undefined){
				var keyName = newItem.aliasName !== undefined ? newItem.aliasName : $scope.dimSrc.DIMENSION_NM + "_NM_LEV";
				var keyId = newItem.aliasId !== undefined ? newItem.aliasId : $scope.dimSrc.DIMENSION_NM + "_CD_LEV";
				newItem.name = newItem[keyName] !== undefined ? newItem[keyName] : item.name;
				newItem.id = newItem[keyId] !== undefined ? newItem[keyId] : item.name;
				if (parent && parent.children){
					var idx = $scope.indexOf(parent.children,item,"id");
					if (idx > 0){
						//copy fields
						for (var k in newItem){
							parent.children[idx][k] = newItem[k];
						}
					}
				}else{
					//copy in the object directly
					for (var k in newItem){
						item[k] = newItem[k];
					}
				}
				$scope.treeTargetDirty = true;
			}
		},function(){});
	}
	/*Clone the hierarchy of the tree with context menu. If the hier not allows duplicate, show Dialog to modify the new hier*/
	$scope.duplicateLeaf =  function(item,parent,event){
		var newItem = angular.copy(item);
		if ($scope.dimSrc && $scope.dimSrc.DIMENSION_NM && $scope.dimSrc.DIMENSION_NM.length > 0){ 
			var idx = $scope.indexOf(parent.children,item,'id');
			var allowDuplicate = $scope.metadataMap[$scope.dimSrc.DIMENSION_NM].CONFIGS.ALLOW_DUPLICATE;
			if (allowDuplicate == false || allowDuplicate == "false"){
				//must modify the dates of validity
				newItem.BEGIN_DT = new Date();
				newItem.END_DT = new Date();
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
	
	$scope.showDetailsNode =  function(item,parent,event){
		var parentEl = angular.element(document.body);
		var dimName = $scope.dimSrc !== undefined ? $scope.dimSrc.DIMENSION_NM : ''; 
		var metTmp =  $scope.metadataMap[dimName];
		if (metTmp === undefined){
			$scope.showAlert('Error','No metadata found for dimension '+ dimName );
			return null;
		}
		 //take generals_fields if it is root[parent is null], leaf_fields if it is leaf or node_fields if it is node
		var metadata = parent == undefined || parent == null ? metTmp.GENERAL_FIELDS : item.leaf == true ? metTmp.LEAF_FIELDS : metTmp.NODE_FIELDS;
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
			$scope.hier = angular.copy(hier);
			$scope.metadata = angular.copy(metadata);
			$scope.showOnlyConfirm = true;
			for (var i = 0 ; i <$scope.metadata.length; i++){
				$scope.metadata[i].EDITABLE = false;
			}
			
	        $scope.closeDialog = function() {
	        	$mdDialog.cancel();
	        }
	        $scope.saveHier = function(){
	        	$mdDialog.hide(hier);
	        }
	 	}
	}
	
	$scope.menuTargetOption = [{
			label: $scope.translate.load('sbi.generic.add'),
			showItem : function(item,event){
				//visible if it is NOT a leaf
				return item !== undefined && (item.leaf === undefined || item.leaf == false) && item.fake != true;
				},
			action: $scope.addHier
		},{
			label: $scope.translate.load('sbi.generic.clone'),
			showItem : function(item,event){
				//visible if it IS a leaf
				return item !== undefined && item.leaf !== undefined && item.leaf == true && item.fake != true;
				},
			action : $scope.duplicateLeaf
		},{
			label: $scope.translate.load('sbi.roles.edit'),
			showItem : function(item,event){
				return item !== undefined && item.fake != true;
				},
			action : $scope.modifyHier
		},{
			label: $scope.translate.load('sbi.generic.delete'),
			showItem : function(item,event){
				return item !== undefined && item.fake != true;
				},
			action: $scope.deleteHier
		}
	];
	
	$scope.menuSrcOption = [{
		label: $scope.translate.load('sbi.generic.details'),
		action : $scope.showDetailsNode
	}];
	 
	$scope.applyFilter = function(choose){
		//use to apply the filter only when is clicked the icon
		var date = choose == 'src' ? $scope.dateFilterSrc : $scope.dateFilterTarget;
		var seeElement = choose == 'src' ? $scope.seeHideLeafSrc : $scope.seeHideLeafTarget;
		var dateFormatted;
		
		if (date !== undefined){
			dateFormatted = $scope.formatDate(date);
		}
		//get the Tree if one off two filters are active
		if ((seeElement !== undefined &&  seeElement != false) || (dateFormatted !== undefined && dateFormatted.length>0)){
			$scope.getTree(choose, dateFormatted, seeElement);
		}
		//apply filter on source side (left) or target side (right)
		choose == 'src' ? $scope.filterBySrcTrigger = angular.copy($scope.filterBySrc) : $scope.filterByTargetTrigger = angular.copy($scope.filterByTarget);
		choose == 'src' ? $scope.orderBySrcTrigger = angular.copy($scope.orderBySrc) : $scope.orderByTargetTrigger = angular.copy($scope.orderByTarget);
	}
	
	$scope.removeFilter = function(choose){
		if (choose = 'src'){
			$scope.filterBySrcTrigger = "";
			$scope.filterBySrc = "";
			$scope.orderBySrcTrigger = "";
			$scope.orderBySrc = "";
			//get tree without filters if they were active
			if (($scope.seeHideLeafSrc !== undefined &&  $scope.seeHideLeafSrc != false) || ($scope.dateFilterSrc !== undefined && $scope.dateFilterSrc.length>0)){
				$scope.getTree('src');
			}
			$scope.dateFilterSrc = undefined;
			$scope.seeHideLeafSrc = false;
		}else if (choose = 'target'){
			$scope.filterByTargetTrigger = "";
			$scope.filterByTarget = "";
			$scope.orderByTargetTrigger = "";
			$scope.orderByTarget = "";
			//get tree without filters if they were active
			if (($scope.seeHideLeafTarget !== undefined &&  $scope.seeHideLeafTarget != false) || ($scope.dateFilterTarget !== undefined && $scope.dateFilterTarget.length>0)){
				$scope.getTree('target');
			}
			$scope.dateFilterTarget = undefined;
			$scope.seeHideLeafTarget = false;
		}
	}
	
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
		if ($scope.dateTarget && $scope.dimSrc && $scope.hierTarget){
			var promise = $scope.editNode($scope.createEmptyNode("root"),null,true);
			promise
				.then(function(newItem){
					if (newItem !== null && newItem !== undefined){
						var keyName = newItem.aliasName !== undefined ? newItem.aliasName : 'HIER_NM';
						var keyId = newItem.aliasId !== undefined ? newItem.aliasId : "HIER_CD";
						newItem.name = newItem[keyName] !== undefined ? newItem[keyName] : rootStructure.name;
						newItem.id = newItem[keyId] !== undefined ? newItem[keyId] : rootStructure.name;
						newItem.root = true;
						newItem.leaf = false;
						$scope.hierTreeTarget = [newItem];
						$scope.treeTargetDirty = true;
						$scope.targetIsNew = true;
					}
				},function(){
					//nothing to do, request cancelled.
				}); 
		}
	}
	//clean object from cycle and undesired element inject with drag
	$scope.cleanTree = function(tree){
		var treeCleaned =angular.copy(tree);
		var elements = [treeCleaned];
		do{
			var el = elements.shift();
			el.checked = el.visible = el.expanded = el.type = undefined;
			el.$parent=null;
			if ((!el.children[i].leaf && !el.children[i].children) || el.children[i].fake == true){
				for (var i =0 ; i<el.children.length;i++){
					elements.push(el.children[i]);
				}
			}
		}while(elements.length > 0);
		
		return treeCleaned;
	}
	
	$scope.saveTree = function(){
		if ($scope.dateTarget && $scope.dimSrc && $scope.hierTarget && $scope.hierTreeTarget){
			//saveHierarchy
			var root = {};
			root.dimension = $scope.dimSrc.DIMENSION_NM;
			root.code = $scope.hierTarget.HIER_CD;
			root.description = $scope.hierTarget.HIER_DS;
			root.name = $scope.hierTarget.HIER_NM;
			root.type = $scope.hierTarget.HIER_TP;
			root.dateValidity = $scope.formatDate($scope.dateTarget);
			root.isInsert = $scope.targetIsNew;
			root.doBackup = $scope.doBackup !== undefined ? $scope.doBackup : false;
			//remove cycle object [E.g. possible cycle -> item.$parent.children[0] = item]
			root.root = Array.isArray($scope.hierTreeTarget) ? $scope.cleanTree($scope.hierTreeTarget[0]) : $scope.cleanTree($scope.hierTreeTarget);
			root.root.$parent = undefined;
			
			var jsonString = angular.toJson(root);
			var promise = $scope.restService.post('hierarchies','saveHierarchy',jsonString);
			promise
				.success(function (data){
					if (data.errors === undefined){
						$scope.treeTargetDirty = false;
						$scope.targetIsNew = false;
						/*clean cache map*/
						var keyMap = root.type + '_' + root.dimension + '_' + root.name + '_' + root.dateValidity;
						if ($scope.dateFilterTarget){
							keyMap = keyMap + '_' + $scope.formatDate($scope.dateFilterTarget);
						}
						if ($scope.seeHideLeafTarget){
							keyMap = keyMap + '_' + $scope.seeHideLeafTarget;
						}
						$scope.hierTreeCacheTarget[keyMap]=undefined;
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
	
	$scope.formatDate = function (date){
		var mm = date.getMonth() < 10 ? '0'+ (date.getMonth()+1) : ''+(date.getMonth()+1);
		return date.getFullYear() + '-' + mm +'-'+ date.getDate();
	}
	
	$scope.toogleLoading = function(choose){
		var loading = choose ==  "src" ? $scope.showLoadingSrc : $scope.showLoadingTarget;
		if (loading){
			$timeout(function(){
				choose == "src" ? $scope.showLoadingSrc = false : $scope.showLoadingTarget = false;
			},400,true);
		}else{
			$timeout(function(){
				choose == "src" ? $scope.showLoadingSrc = true : $scope.showLoadingTarget = true;
			},0,true);
		}
	}
};
