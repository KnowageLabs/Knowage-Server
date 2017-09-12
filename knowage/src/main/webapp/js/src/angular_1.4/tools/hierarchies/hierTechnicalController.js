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
	$scope.fakeNode = {
			fake : true,
			name : $scope.translate.load("sbi.hierarchies.new.empty"),
			id : '',
			leaf: true,
			root: false,
			visible : true,
			checked : false,
			expanded : false
		};
	$scope.hasFilterElementOrDate = {};
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
	
	$scope.keys = {'subfolders' : 'children', 'iconDocument' : 'fa fa-leaf'};
	$scope.fakeNode = {
			fake : true,
			name : $scope.translate.load("sbi.hierarchies.new.empty"),
			id : '',
			visible : true,
			checked : false,
			expanded : false
		};

	/*Drag and Drop option*/
	$scope.treeSrcOptions = {
		beforeDrop : function(e){
			$scope.beforeDrop(e,false); //false because the Drop is not inside Tree, but is between two different trees
		},
		beforeDrag : function(sourceNodeScope){
			if (sourceNodeScope.$treeScope.cloneEnabled==false){
				sourceNodeScope.$treeScope.cloneEnabled = true;
			}
			return true;
		}
	};
	
	$scope.treeTargetOptions = {
			beforeDrop : function(e){
				$scope.beforeDrop(e,true); //true because the Drop is inside Tree
			},
			accept: function(sourceNodeScope, destNodesScope, destIndex){
				if (destNodesScope.$treeScope.cloneEnabled==false){
					destNodesScope.$treeScope.cloneEnabled = true;
				}
				return true;
			}
		};
	
	$scope.beforeDrop = function(e,isInsideTree){
		//set dirty the tree and update the level of the object dragged
		$scope.treeTargetDirty = true;
		var dest = e.dest.nodesScope.$nodeScope.$modelValue;
		//var source = e.source.cloneModel ? e.source.cloneModel : e.source.nodeScope.$modelValue;
		var source = e.source.nodeScope.$modelValue;
		$scope.removeFakeAndCorupt(dest.children);
		if (!isInsideTree){
			$scope.hierSourceCode = $scope.hierSrc.HIER_CD;
			$scope.hierSourceName = $scope.hierSrc.HIER_NM;
			$scope.hierSourceType = $scope.hierSrc.HIER_TP;
		}
		else if (isInsideTree && source.$parent.children.length <= 1){
			//ui-tree work in a copy object. Need to find the real parent in the tree
			var realParent = $scope.findRealParent(source.$parent);
			if (realParent){
				realParent.children.push(angular.copy($scope.fakeNode));
			}
		}
		if (source.leaf == true){
			source.LEAF_PARENT_NM = dest[dest.aliasName];
			source.LEAF_PARENT_CD = dest[dest.aliasId];
			source.LEAF_ORIG_PARENT_CD = dest[dest.aliasId];
		}
		var level =  dest.LEVEL && dest.LEVEL >= 0 ? dest.LEVEL + 1 : 1;
		$scope.updateLevelRecursive(source, level);
		source.$parent = dest;
		if(e.source.cloneModel){
			for (var k in source){
				e.source.cloneModel[k] = angular.copy(source[k]);
			}
		}
	}
	
	$scope.findRealParent = function(node){
		var elements = [$scope.hierTreeTarget[0]];
		do{
			var el = elements.shift();
			if (el[el.aliasName] == node[node.aliasName] && el[el.aliasId] == node[node.aliasId]){
				return el;
			}
			for (var i = 0 ; el.children && i<el.children.length;i++){
				elements.push(el.children[i]);
			}
		}while(elements.length>0);
		return null;
	}
	
	$scope.updateLevelRecursive = function(node, level){
		node.LEVEL = level;
		if (node.children && node.children.length > 0){
			for (var i = 0; i< node.children.length;i++){
				$scope.updateLevelRecursive(node.children[i],level+1);
			}
		}
	}
	
	/*remove elements dropped by ui-tree that are wrong. These elements are dropped though you cancel the confirm dialog [showListHierarchies]*/
	$scope.removeFakeAndCorupt = function (array){
		for (var i = 0;i< array.length ; i++){
			if (array[i].fake == true || (!array[i].leaf && !array[i].children)){
				array.splice(i,1);
				i--;
			}
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
			$scope.showAlert($scope.translate.load("sbi.generic.error"),message);
			
		});
	
	$scope.getHierarchies = function (choose, forceGetHierarchies){
		var type = choose == 'src' ? $scope.hierTypeSrc : 'Technical' ;
		//var dim = choose == 'src' ?  $scope.dimSrc : $scope.dimTarget;
		var dim = $scope.dimSrc;
		var map = choose == 'src' ? $scope.hierarchiesSrcMap : $scope.hierarchiesTargetMap; 
		if (type !== undefined && dim !== undefined){
			var dimName = dim.DIMENSION_NM;
			var keyMap = type+'_'+dimName; 
			var serviceName = (type.toUpperCase() == 'AUTO' || type.toUpperCase() == 'MASTER' )? 'getHierarchiesMaster' : 'getHierarchiesTechnical';
			var service = (type.toUpperCase() == 'AUTO' || type.toUpperCase() == 'MASTER' )? 'hierarchiesMaster' : 'hierarchiesTechnical';
			
			//if the hierarchies[dim][type] is not defined, get the hierarchies and save in the map. Else, get them from the map 
			if (map[keyMap] === undefined || forceGetHierarchies==true){
				$scope.toogleLoading(choose,true);
				$scope.restService.get(service,serviceName,"dimension="+dimName)
					.success(
						function(data, status, headers, config) {
							if (data.errors === undefined){
								map[keyMap] = data;
								choose == 'src' ?  $scope.hierarchiesSrc = angular.copy(data) : $scope.hierarchiesTarget = angular.copy(data);
							}else{
								$scope.showAlert($scope.translate.load("sbi.generic.error"),data.errors[0].message);
							}
							$scope.toogleLoading(choose,false);
						})
					.error(function(data, status){
						var message='GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status;
						$scope.showAlert($scope.translate.load("sbi.generic.error"),message);
						$scope.toogleLoading(choose,false);
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
							$scope.showAlert($scope.translate.load("sbi.generic.error"),data.errors[0].message);
						}
				})
				.error(function(data, status){
					var message = 'GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status;
					$scope.showAlert($scope.translate.load("sbi.generic.error"),message);
				});
			}
		}
	}
	
	$scope.getTree = function(choose,dateFilter,seeElement, forceDownload){
		var type = choose == 'src' ? $scope.hierTypeSrc : 'TECHNICAL' ;
		//var dim = choose == 'src' ?  $scope.dimSrc : $scope.dimTarget;
		var dim = $scope.dimSrc ;
		var date = choose == 'src' ? $scope.dateSrc : $scope.dateTarget;
		var hier = choose == 'src' ?  $scope.hierSrc : $scope.hierTarget;
		if (type && dim && hier && date){
			if (choose == 'src' && $scope.treeTargetDirty == true){
				$scope.showConfirm($scope.translate.load("sbi.generic.info"),'Are you sure to descard the hierarchy modification?');
				//TODO active this
			}
			var dateFormatted = $scope.formatDate(date);
			var keyMap = type + '_' + dim.DIMENSION_NM + '_' + hier.HIER_NM + '_' + dateFormatted;
			var config = {};
			config.params = {
				dimension: dim.DIMENSION_NM,
				filterType : type,
				filterHierarchy : hier.HIER_NM,
				validityDate : dateFormatted
			};
			if (dateFilter !== undefined && dateFilter!== null && dateFilter.length > 0){
				config.params.filterDate = ''+ dateFilter;
				keyMap = keyMap + '_' + dateFilter;
			}
			if (seeElement == true){
//				config.params.filterDimension = seeElement; //filteDimension is always false into the TECHNICAL tab
				//In show missing element filter are passed the date and hierarchy of the other tree (if Source tree, are passed date and tree of Target, and vice versa)
				config.params.optionDate =  choose == 'src'  ?  $scope.formatDate($scope.dateTarget) : $scope.formatDate($scope.dateSrc);
				config.params.optionHierarchy = choose == 'src'  ? ($scope.hierTarget ? $scope.hierTarget.HIER_NM : undefined) : ($scope.hierSrc ? $scope.hierSrc.HIER_NM : undefined);
				config.params.optionHierType = choose == 'src'  ? ($scope.hierTarget ? $scope.hierTarget.HIER_TP : undefined) : ($scope.hierSrc ? $scope.hierSrc.HIER_TP : undefined);
				keyMap = keyMap + '_' + seeElement;
			}
			var hierMap = choose == 'src' ? $scope.hierTreeCacheSrc : $scope.hierTreeCacheTarget;
		
			//in source tree force the download, because the D&D do a messy with the source tree. Is better to restart from the original
			if (hierMap[keyMap] === undefined || choose == "src" || forceDownload){ 
				$scope.toogleLoading(choose,true);
				$scope.restService.get("hierarchies","getHierarchyTree",null,config)
					.success(
						function(data, status, headers, config) {
							if (data !== undefined && data.errors === undefined){
								if (typeof data =='object'){
									data = [data];
								}
								choose =='src' ? $scope.hierTreeSrc = data : $scope.hierTreeTarget = data;
								choose =='src' ? $scope.hierTreeCacheSrc[keyMap] = angular.copy(data) : $scope.hierTreeCacheTarget[keyMap] = angular.copy(data);
								$scope.targetIsNew = choose =='src' ? $scope.targetIsNew : false;
								$scope.treeTargetDirty = choose == "src" ? $scope.treeTargetDirty : false;
							}else{
								var params = 'date = ' + date + ' dimension = ' + dim.DIMENSION_NM + ' type = ' +  type + ' hierachies = ' + hier.HIER_NM;
								$scope.showAlert($scope.translate.load("sbi.generic.error"),data.errors[0].message);
							}
							$scope.toogleLoading(choose,false);
						})
					.error(function(data, status){
						var params = 'date = ' + date + ' dimension = ' + dim.DIMENSION_NM + ' type = ' +  type + ' hierachies = ' + hier.HIER_NM;
						var message='GET tree source error with parameters' + params + ' with status: "' + status+ '"';
						$scope.showAlert($scope.translate.load("sbi.generic.error"),message);
						$scope.toogleLoading(choose,false);
					});
			}else{
				choose =='src' ? $scope.hierTreeSrc = angular.copy($scope.hierTreeCacheSrc[keyMap]) : $scope.hierTreeTarget = angular.copy($scope.hierTreeCacheTarget[keyMap]);
			}
		}	
	}
	
	$scope.resetCache = function(choose,dim,type,hier){
		var key = type + '_' + dim.DIMENSION_NM + '_' + hier.HIER_NM;
		var cache = choose == "src" ? $scope.hierTreeCacheSrc  : $scope.hierTreeCacheTarget;
		for (var k in cache){
			if (k.indexOf(key)>-1){
				cache[k] = undefined;
			}
		}
		$scope.getTree(choose,undefined,undefined,true);
	}
	
	 $scope.editNode = function(item,parent,forceEditable){
		 var parentEl = angular.element(document.body);
		 var dimName = $scope.dimSrc  !== undefined ? $scope.dimSrc.DIMENSION_NM : '';
		 var metTmp =  angular.copy($scope.metadataMap[dimName]);
		 if (metTmp === undefined){
			 $scope.showAlert($scope.translate.load("sbi.generic.error"),'No metadata found for dimension '+ dimName );
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
		$scope.hier.BEGIN_DT = hier.BEGIN_DT !== undefined ? new Date(hier.BEGIN_DT) : new Date();
		$scope.hier.END_DT = hier.END_DT !== undefined ? new Date(hier.END_DT) : new Date();
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
			$scope.showAlert($scope.translate.load("sbi.generic.error"),'No metadata Node found for dimension '+ dimName );
			return null;
		}
		var metadata = type == "root" ? metTmp.GENERAL_FIELDS: (type =="node" ? metTmp.NODE_FIELDS  : metTmp.LEAF_FIELDS);
		var node = {};
		for (var i =0; i < metadata.length; i++){
			if (metadata[i].TYPE == 'Number'){
				node[metadata[i].ID] = -1;
			}else if(metadata[i].TYPE == 'Date'){
				node[metadata[i].ID] = new Date();
			}else{
				node[metadata[i].ID] = '';
			}
			if (metadata[i].FIX_VALUE && metadata[i].FIX_VALUE.length > 0){
				node[metadata[i].ID] = metadata[i].FIX_VALUE;
			}
		}
		//force aliasId and aliasName
		if (type == "root" || type == "node"){
			node.aliasId = type == "root" ? "HIER_CD" : dimName+"_CD_LEV";
			node.aliasName = type == "root" ? "HIER_NM" : dimName+"_NM_LEV";
		}
		node.children = type == "leaf" ? [] : [ angular.copy($scope.fakeNode) ];
		node.expanded = false;
		node.visible=true;
		node.type="folder";
		node.checked = false;
		node.leaf = type == "leaf";
		node.root = type == "root";
		return node;
	}
	
	$scope.addHier =  function(item,parent,event){
		var promise = $scope.editNode({},item,true);
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
					tmpItem.LEVEL = item.LEVEL && item.LEVEL>=0 ? item.LEVEL + 1 : 1;
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
				newItem.$parent=item.$parent;
				if (parent && parent.children){
					var idx = $scope.indexOf(parent.children,item,"id");
					if (idx > 0){
						parent.children.splice(idx,1);						
						parent.children.splice(idx,0,newItem);
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
							$show.alert($scope.translate.load("sbi.generic.error"), 'The duplicate leaf can not be equal to the original');
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
		var response=$scope.showConfirm($scope.translate.load('sbi.generic.delete') +' '+ item.name.toUpperCase(),$scope.translate.load('sbi.hierarchies.delete.confirm'));
		response.then(
			function() {
				if (parent !== undefined && parent !== null){
					var idx = $scope.indexOf(parent.children,item,'id');
					parent.children.splice(idx,1);
					if (parent.children.length == 0) {
						parent.children.push(angular.copy($scope.fakeNode));
					}
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
			$scope.showAlert($scope.translate.load("sbi.generic.error"),'No metadata found for dimension '+ dimName );
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
				controller: showDetailsDialogController 
			});
	 
		function showDetailsDialogController($scope, $mdDialog, translate, hier, metadata) {
	 		$scope.translate = translate;
			$scope.hier = angular.copy(hier);
			$scope.metadata = angular.copy(metadata);
			$scope.showOnlyConfirm = true;
			for (var k in hier){
				if (hier[k] instanceof Date){
					$scope.hier[k] = new Date(hier[k]); //convert the date from server to client
				}
			}
			$scope.hier.BEGIN_DT = hier.BEGIN_DT !== undefined ? new Date(hier.BEGIN_DT) : new Date();
			$scope.hier.END_DT = hier.END_DT !== undefined ? new Date(hier.END_DT) : new Date();
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
			icon: "fa fa-plus-circle",
			showItem : function(item,event){
				//visible if it is NOT a leaf
				return item !== undefined && (item.leaf === undefined || item.leaf == false) && item.fake != true;
				},
			action: $scope.addHier
		},{
			label: $scope.translate.load('sbi.generic.clone'),
			icon: "fa fa-clone",
			showItem : function(item,event){
				//visible if it IS a leaf
				return item !== undefined && item.leaf !== undefined && item.leaf == true && item.fake != true;
				},
			action : $scope.duplicateLeaf
		},{
			label: $scope.translate.load('sbi.roles.edit'),
			icon: "fa fa-pencil",
			showItem : function(item,event){
				return item !== undefined && item.fake != true;
				},
			action : $scope.modifyHier
		},{
			label: $scope.translate.load('sbi.generic.delete'),
			icon: "fa fa-trash",
			showItem : function(item,event){
				return item !== undefined && item.fake != true;
				},
			action: $scope.deleteHier
		},{
			label: $scope.translate.load('sbi.generic.details'),
			icon: "fa fa-info-circle",
			showItem : function(item,event){
				return item !== undefined && item.fake != true;
				},
			action : $scope.showDetailsNode
		}
	];
	
	$scope.menuSrcOption = [{
		label: $scope.translate.load('sbi.generic.details'),
		icon: "fa fa-info-circle",
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
		//get the Tree if one off two filters are active. Else if the filters were applyed before, but not now, get the tree without them
		if ((seeElement !== undefined &&  seeElement == true) || (dateFormatted !== undefined && dateFormatted.length>0)){
			$scope.getTree(choose, dateFormatted, seeElement);
			$scope.hasFilterElementOrDate[choose] = true;
		}else if ($scope.hasFilterElementOrDate && $scope.hasFilterElementOrDate[choose] == true){
			$scope.getTree(choose);
			$scope.hasFilterElementOrDate[choose] = false;
		}
		//apply filter on source side (left) or target side (right)
		choose == 'src' ? $scope.filterBySrcTrigger = angular.copy($scope.filterBySrc) : $scope.filterByTargetTrigger = angular.copy($scope.filterByTarget);
		choose == 'src' ? $scope.orderBySrcTrigger = angular.copy($scope.orderBySrc) : $scope.orderByTargetTrigger = angular.copy($scope.orderByTarget);
	}
	
	$scope.removeFilter = function(choose){
		if (choose == 'src'){
			$scope.filterBySrcTrigger = "";
			$scope.filterBySrc = "";
			$scope.orderBySrcTrigger = "";
			$scope.orderBySrc = "";
			//get tree without filters if they were active
			if (($scope.seeHideLeafSrc !== undefined &&  $scope.seeHideLeafSrc != false) || ($scope.dateFilterSrc !== undefined && $scope.dateFilterSrc.toString().length>0)){
				$scope.getTree('src');
			}
			$scope.dateFilterSrc = undefined;
			$scope.seeHideLeafSrc = false;
		}else if (choose == 'target'){
			$scope.filterByTargetTrigger = "";
			$scope.filterByTarget = "";
			$scope.orderByTargetTrigger = "";
			$scope.orderByTarget = "";
			//get tree without filters if they were active
			if (($scope.seeHideLeafTarget !== undefined &&  $scope.seeHideLeafTarget != false) || ($scope.dateFilterTarget !== undefined && $scope.dateFilterTarget.toString().length>0)){
				$scope.getTree('target');
			}
			$scope.dateFilterTarget = undefined;
			$scope.seeHideLeafTarget = false;
		}
		$scope.toogleSeeFilter(choose);
		$scope.hasFilterElementOrDate[choose] = false;
	}
	
	$scope.toogleSeeFilter= function(choose){
		if (choose == 'src'){
			$scope.seeFilterSrc = !$scope.seeFilterSrc;
		}else{
			$scope.seeFilterTarget = !$scope.seeFilterTarget;
		}
	}
	
	$scope.showConfirm = function(title,message) {
	    var confirm = $mdDialog
			.confirm()
			.title(title)
			.content(message)
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
		if ($scope.dimSrc){
			//crate new root and force it to have type TECHNICAL
			var newNode = $scope.createEmptyNode("root");
			newNode.HIER_TP = "TECHNICAL";
			var promise = $scope.editNode(newNode,null,true);
			promise
				.then(function(newItem){
					if (newItem !== null && newItem !== undefined){
						var keyName = newItem.aliasName !== undefined ? newItem.aliasName : 'HIER_NM';
						var keyId = newItem.aliasId !== undefined ? newItem.aliasId : "HIER_CD";
						newItem.name = newItem[keyName] !== undefined ? newItem[keyName] : rootStructure.name;
						newItem.id = newItem[keyId] !== undefined ? newItem[keyId] : rootStructure.name;
						$scope.hierTreeTarget = [newItem];
						$scope.treeTargetDirty = true;
						$scope.targetIsNew = true;
						$scope.hierTarget = {};
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
		var missimgPlaceholder = false;
		do{
			var el = elements.shift();
			el.checked = el.visible = el.expanded = el.type = undefined;
			el.$parent=null;
			for (var k in el){
				if (el[k] instanceof Date){
					el[k] = $scope.formatDate(el[k]);
				}
			}
			if (el.leaf == true){
				el.MAX_DEPTH = el.LEVEL;
			} else if (el.children !== undefined && el.children.length > 0) {
				for (var i = 0; i < el.children.length; i++) {
					if ((!el.children[i].leaf && !el.children[i].children) || el.children[i].fake == true) {
						el.children.splice(i, 1);
						i--;
					} else {
						elements.push(el.children[i]);
					}
				}
			}else if(el.children !== undefined && el.children.length == 0){
				missimgPlaceholder = true;
			}
			if(el.leaf != true && el.children !== undefined && el.children.length == 0){
				missimgPlaceholder = true;
			}
		}while(elements.length > 0);
		
		return {
			treeCleaned : treeCleaned,
			missimgPlaceholder : missimgPlaceholder
		};
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
			root.hierSourceCode  = $scope.hierSourceCode;
			root.hierSourceName = $scope.hierSourceName;
			root.hierSourceType = $scope.hierSourceType;
			//remove cycle object [E.g. possible cycle -> item.$parent.children[0] = item]
			var cleanResponse = Array.isArray($scope.hierTreeTarget) ? $scope.cleanTree($scope.hierTreeTarget[0]) : $scope.cleanTree($scope.hierTreeTarget);
			root.root = cleanResponse.treeCleaned;
			root.root.$parent = undefined;
			//In case are present empty node, show confirm message for saving
			if (cleanResponse.missimgPlaceholder == true){
				$scope.showConfirm($scope.translate.load('sbi.hierarchies.save.changes'),$scope.translate.load('sbi.hierarchies.save.emptynodes'))
					.then(function(){
						$scope.callSaveTree(root,true);
					},function(){})
			}else{
				$scope.callSaveTree(root);
			}
		}
	}
	
	$scope.callSaveTree = function(root,forceDownload){
		var jsonString = angular.toJson(root);
		$scope.toogleLoading('target',true);
		var promise = $scope.restService.post('hierarchies','saveHierarchy',jsonString);
		promise
			.success(function (data){
				if (data.errors === undefined){
					if ($scope.targetIsNew == true){
						$scope.getHierarchies("src",true);
						$scope.getHierarchies("target",true);
					}
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
					$scope.showAlert($scope.translate.load("sbi.generic.info"),$scope.translate.load("sbi.hierarchies.save.correct"));
					if (forceDownload == true){
						$scope.getTree('target',$scope.dateFilterTarget,$scope.seeHideLeafTarget,true);
					}
				}else{
					$scope.showAlert($scope.translate.load("sbi.generic.error"),data.errors[0].message);
				}
				$scope.toogleLoading('target',false);
			})
			.error(function(data,status){
				$scope.showAlert($scope.translate.load("sbi.generic.error"),'Impossible to save the Tree');
				$scope.toogleLoading('target',false);
			});
	}
	
	$scope.formatDate = function (date){
		if (date){
			var mm = (date.getMonth()+1) < 10 ? '0' + (date.getMonth() + 1) : ''+ (date.getMonth() + 1);
			var dd = date.getDate() < 10 ? '0' + date.getDate() : '' + date.getDate();
			return date.getFullYear() + '-' + mm + '-' + dd;
		}
		return undefined;
	}
	
	$scope.toogleLoading = function(choose, forceValue){
		var loading;
		if (forceValue !== undefined){
			loading = !forceValue;
		}else{
			 loading = choose ==  "src" ? $scope.showLoadingSrc : $scope.showLoadingTarget;
		}
		$timeout(function(){
			choose == "src" ? $scope.showLoadingSrc = !loading : $scope.showLoadingTarget = !loading;
		},100,true);
		
	}
};
