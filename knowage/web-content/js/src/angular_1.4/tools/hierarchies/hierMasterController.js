var app = angular.module('hierManager');

app.controller('hierMasterController',[ "$timeout","sbiModule_config","sbiModule_logger","sbiModule_translate","$scope","$mdDialog","sbiModule_restServices","$mdDialog",masterControllerFunction ]);

function masterControllerFunction($timeout,sbiModule_config,sbiModule_logger,sbiModule_translate,$scope,$mdDialog,sbiModule_restServices,$mdDialog) {
	/* General initialization */
	$scope.translate = sbiModule_translate;
	$scope.restService = sbiModule_restServices;
	$scope.log = sbiModule_logger;
	$scope.hierarchiesType = [ 'MASTER', 'TECHNICAL' ];
	$scope.hierTreeCache = {};
	$scope.keys = {
		'subfolders' : 'children'
	};
	$scope.orderByFields = [ 'name', 'id' ];
	$scope.doBackup = true;
	$scope.showLoading = false;
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
	/* Initialization Left side variables */
	$scope.dimensions = []; // array of dimensions combo-box
	$scope.seeFilterDim = false; // visibility filter flag of left side
	$scope.dateDim = new Date();
	$scope.dimensionsTable = [];
	$scope.columnsTable = [];
	$scope.columnSearchTable = [];
	$scope.metadataDimMap = {};
	$scope.styleContainer = 'table-container'; // class for the container table
	$scope.styleTable = 'table-object';// class for the table
	/* Initialization Tree (right side) variables */
	$scope.hierTree = [];
	$scope.dateTree = new Date();
	$scope.metadataTreeMap = {};
	$scope.treeMasterDirty = false;
	$scope.masterIsNew = false;
	$scope.treeDirty = false;

	$scope.hierMasterNew = {};
	/* details button in the table (left side) */
	$scope.dimSpeedMenu = [ {
		label : $scope.translate.load('sbi.generic.details'),
		icon : 'fa fa-info-circle',
		color : '#153E7E',
		action : function(item, event) {
			$scope.showDetails(item);
		}
	} ];

	$scope.treeOptions = {
		accept : function(sourceNodeScope, destNodesScope, destIndex) {
			if (sourceNodeScope.$treeScope.cloneEnabled == false) {
				sourceNodeScope.$treeScope.cloneEnabled = true;
			}
			if (destNodesScope.$treeScope.cloneEnabled == false) {
				destNodesScope.$treeScope.cloneEnabled = true;
			}
			return true;
		},
		beforeDrop : function(e) {
			$scope.treeDirty = true;
			var dest = e.dest.nodesScope.$nodeScope.$modelValue;
			var source = e.source.nodeScope.$modelValue;
			$scope.removeFakeAndCorupt(dest.children);
			if (source.$parent.children.length == 1){
				source.$parent.children.push(angular.copy($scope.fakeNode));
			}
			if (source.leaf == true){
				source.LEAF_PARENT_NM = dest[dest.aliasName];
				source.LEAF_PARENT_CD = dest[dest.aliasId];
				source.LEAF_ORIG_PARENT_CD = dest[dest.aliasId];
			}
			var level = dest.LEVEL && dest.LEVEL >= 0 ? dest.LEVEL + 1 : 1;
			$scope.updateLevelRecursive(source, level);
			source.$parent = dest;
			return true;
		}
	}
	/*Drag and Drop options from table to tree. Create node or leaf to insert in the tree if confirmed the list dialog*/
	$scope.tableOptions = {
		beforeDrag : function(sourceNodeScope) {
			if (sourceNodeScope.$treeScope.cloneEnabled == false) {
				sourceNodeScope.$treeScope.cloneEnabled = true;
			}
			return true;
		},
		beforeDrop : function(e) {
			var dest = e.dest.nodesScope.$nodeScope;
			if (dest && dest.$modelValue
					&& dest.$modelValue.children !== undefined) {
				$scope.showListHierarchies()
					.then(
						function(data) {
							var source = e.source.cloneModel;
							var dest = e.dest.nodesScope.$nodeScope.$modelValue;
							var dimName = $scope.dim.DIMENSION_NM;
							var keyName = dimName + '_NM';
							var keyId = dimName + '_CD';
							source.name = source[keyName];
							source.id = source[keyId];
							source.LEAF_PARENT_NM = dest[dest.aliasName];
							source.LEAF_PARENT_CD = dest[dest.aliasId];
							source.LEAF_ORIG_PARENT_CD = dest[dest.aliasId];
							source.LEVEL = dest.LEVEL !== undefined ? dest.LEVEL + 1 : 1;
							var tmp = angular.copy($scope.createEmptyNode('leaf'));
							tmp.$parent = dest;
							var keys = Object.keys(tmp);
							for (var i = 0; i < keys.length; i++) {
								if (source[keys[i]] == undefined) {
									source[keys[i]] = tmp[keys[i]];
								}
							}
							//force leaf to have some metadata keys
							var matchingFields = $scope.metadataDimMap[dimName].MATCH_LEAF_FIELDS;
							for (var k in matchingFields){
								if (source[k]){
									source[matchingFields[k]] = source[k];
								}
							}
							$scope.removeFakeAndCorupt(dest.children);
							dest.children.splice(0, 0, source);
							$scope.treeDirty = true;
							e.source.cloneModel = source;
							return false;
						}, function() {
							return false;
						});
			}
			return false;
		}
	};

	$scope.updateLevelRecursive = function(node, level){
		node.LEVEL = level;
		if (node.children && node.children.length > 0){
			for (var i = 0; i< node.children.length;i++){
				$scope.updateLevelRecursive(node.children[i],level+1);
			}
		}
	}
	
	/*remove elements dropped by ui-tree that are wrong. These elements are dropped though you cancel the confirm dialog [showListHierarchies]*/
	$scope.removeFakeAndCorupt = function(array) {
		for (var i = 0; i < array.length; i++) {
			if (array[i].fake == true || (!array[i].leaf && !array[i].children)) {
				array.splice(i, 1);
				i--;
			}
		}
	}

	// $scope.hierTree.push(angular.copy(dataJson));
	/* Get dimensions for combo box */
	$scope.restService.get("dimensions", "getDimensions")
		.success(
			function(data, status, headers, config) {
				$scope.dimensions = angular.copy(data);
		}).error(
			function(data, status) {
				var message = 'GET dimensions error of ' + data + ' with status :' + status;
				$scope.showAlert($scope.translate.load("sbi.generic.error"), message);

		});
	/* When selected a dimension, get the JSON to create the table */
	$scope.getDimensionsTable = function(filterDate, filterHierarchy, removeFilter) {
		if ($scope.dateDim && $scope.dim) {
			var hasFilter = false;
			var hier = $scope.hierMaster;
			var dateFormatted = $scope.formatDate($scope.dateDim);
			var config = {};
			config.params = {
				dimension : $scope.dim.DIMENSION_NM,
				validityDate : dateFormatted
			}
			if (filterDate !== undefined && filterDate !== null) {
				config.params.filterDate = $scope.formatDate(filterDate);
				hasFilter = true;
			}
			if (hier && filterHierarchy !== undefined
					&& filterHierarchy !== null) {
				config.params.filterHierType = $scope.hierType.toUpperCase();
				config.params.filterHierarchy = hier.HIER_NM;
				hasFilter = true;
			}
			$scope.restService.get("dimensions", "dimensionData", null, config)
					.success(function(data, status, headers, config) {
						if (data.error == undefined) {
							$scope.createTable(data);
							if (!hasFilter && !removeFilter) {
								$scope.hierType = undefined;
								$scope.hierarchiesMaster = [];
								$scope.hierTree = [];
							} else if (removeFilter == true) {
								$scope.seeHideLeafDim = false;
								$scope.dateFilterDim = undefined;
							}
						} else {
							$scope.showAlert($scope.translate.load("sbi.generic.error"), data.error[0].message);
						}
					}).error(
							function(data, status) {
								var message = 'GET dimension table error of '+ data + ' with status :' + status;
								$scope.showAlert($scope.translate.load("sbi.generic.error"), message);
							});
		}
		$scope.getDimMetadata($scope.dim);
		$scope.getTreeMetadata($scope.dim);
	}

	/* Get the metadata for the dimension selected */
	$scope.getDimMetadata = function(dim) {
		if (dim) {
			var dimName = $scope.dim.DIMENSION_NM;
			if ($scope.metadataDimMap !== undefined && $scope.metadataDimMap[dimName] == undefined) {
				$scope.restService.get("dimensions", "dimensionMetadata","dimension=" + dimName).success(
						function(data, status, headers, config) {
							if (data.errors === undefined) {
								$scope.metadataDimMap[dimName] = data;
							} else {
								$scope.showAlert($scope.translate.load("sbi.generic.error"),data.errors[0].message);
							}
						})
						.error(
							function(data, status) {
								var message = 'GET hierarchies error of '+ type + '-' + dimName+ ' with status :' + status;
								$scope.showAlert($scope.translate.load("sbi.generic.error"), message);
							});
			}
		}
	}
	/* Initialize the variables of the table [table, columns, columns-search] */
	$scope.createTable = function(data) {
		$scope.dimensionsTable = data.root;
		$scope.columnsTable.splice(0, $scope.columnsTable.length);
		for (var i = 0; i < data.columns.length; i++) {
			if (data.columns[i].VISIBLE == true || data.columns[i].VISIBLE == "true") {
				$scope.columnsTable.push({'label' : data.columns[i].NAME, 'name' : data.columns[i].ID});
			}
		}
		$scope.columnSearchTable = data.columns_search;
		// calculate the width dynamically
		$timeout(function() {
			// var table = angular.element(document).find('table');
			// var width = ($scope.columnsTable.length + 2) * 150; // + 2
			// because the drag column and speedMenu column
			// table.css('width',width + 'px');
		}, 200, true);

	}
	/* Get hierarchies for combo-box */
	$scope.getHierarchies = function() {
		var type = $scope.hierType;
		var dim = $scope.dim;
		var map = $scope.hierTreeCache;
		if (type !== undefined && dim !== undefined) {
			var dimName = dim.DIMENSION_NM;
			var keyMap = type + '_' + dimName;
			var serviceName = (type.toUpperCase() == 'AUTO' || type.toUpperCase() == 'MASTER') ? 'getHierarchiesMaster': 'getHierarchiesTechnical';

			// if the hierarchies[dim][type] is not defined, get the hierarchies
			// and save in the map. Else, get them from the map
			if (map[keyMap] === undefined) {
				$scope.toogleLoading();
				$scope.restService.get("hierarchies", serviceName,"dimension=" + dimName)
					.success(
						function(data, status, headers, config) {
							if (data.errors === undefined) {
								map[keyMap] = data;
								$scope.hierarchiesMaster = angular.copy(data);
							} else {
								$scope.showAlert($scope.translate.load("sbi.generic.error"),data.errors[0].message);
							}
							$scope.toogleLoading();
						})
						.error(function(data, status) {
								var message = 'GET hierarchies error of '+ type + '-' + dimName+ ' with status :' + status;
								$scope.showAlert($scope.translate.load("sbi.generic.error"), message);
								$scope.toogleLoading();
							});
			} else {
				$scope.hierarchiesMaster = map[keyMap];
			}
		}
		$scope.getTreeMetadata(dim);
	}
	/* Get the of the tree basing on dimension selected */
	$scope.getTreeMetadata = function(dim) {
		if (dim !== undefined) {
			var dimName = dim.DIMENSION_NM;
			if ($scope.metadataTreeMap !== undefined
					&& $scope.metadataTreeMap[dimName] == undefined) {
				$scope.restService.get("hierarchies", "nodeMetadata","dimension=" + dimName + "&excludeLeaf=false")
					.success(
						function(data, status, headers, config) {
							if (data.errors === undefined) {
								$scope.metadataTreeMap[dimName] = data;
							} else {
								$scope.showAlert($scope.translate.load("sbi.generic.error"),data.errors[0].message);
							}
						})
						.error(function(data, status) {
							var message = 'GET hierarchies error of '+ type + '-' + dimName+ ' with status :' + status;
							$scope.showAlert($scope.translate.load("sbi.generic.error"), message);
						});
			}
		}
	}
	/* Get the tree when selected data, dimension, type hierarchy and hierarchy */
	$scope.getTree = function(dateFilter, seeElement) {
		var type = $scope.hierType;
		var dim = $scope.dim;
		var date = $scope.dateTree;
		var hier = $scope.hierMaster;
		if (type && dim && hier && date) {
			var dateFormatted = $scope.formatDate(date);
			var keyMap = type + '_' + dim.DIMENSION_NM + '_' + hier.HIER_NM+ '_' + dateFormatted;
			var config = {};
			config.params = {
				dimension : dim.DIMENSION_NM,
				filterType : type,
				filterHierarchy : hier.HIER_NM,
				validityDate : dateFormatted
			};
			if (dateFilter !== undefined && dateFilter !== null
					&& dateFilter.length > 0) {
				config.params.filterDate = '' + dateFilter;
				keyMap = keyMap + '_' + dateFilter;
			}
			if (seeElement == true) {
				config.params.filterDimension = seeElement;
				keyMap = keyMap + '_' + seeElement;
			}
			if (!$scope.hierTreeCache[keyMap]) {
				$scope.toogleLoading();
				$scope.restService.get("hierarchies", "getHierarchyTree", null, config)
					.success(
						function(data, status, headers, config) {
							if (data !== undefined && data.errors === undefined) {
								if (typeof data == 'object') {
									data = [ data ];
								}
								$scope.hierTree = data;
								$scope.hierTreeCache[keyMap] = angular.copy(data);
								$scope.IsNew = false;
							} else {
								var params = 'date = ' + date+ ' dimension = '+ dim.DIMENSION_NM + ' type = '+ type + ' hierachies = '+ hier.HIER_NM;
								$scope.showAlert($scope.translate.load("sbi.generic.error"),data.errors[0].message);
							}
							$scope.toogleLoading();
						})
					.error(
						function(data, status) {
							var params = 'date = ' + date+ ' dimension = '+ dim.DIMENSION_NM + ' type = '+ type + ' hierachies = '+ hier.HIER_NM;
							var message = 'GET tree source error with parameters'+ params+ ' with status: "'+ status + '"';
							$scope.showAlert($scope.translate.load("sbi.generic.error"), message);
							$scope.toogleLoading();
					});
			} else {
				$scope.hierTree = angular.copy($scope.hierTreeCache[keyMap]);
			}
		}
	}

	$scope.createEmptyNode = function(type) {
		var dimName = $scope.dim !== undefined ? $scope.dim.DIMENSION_NM : '';
		var metTmp = $scope.metadataTreeMap[dimName];
		if (metTmp === undefined) {
			$scope.showAlert($scope.translate.load("sbi.generic.error"), 'No metadata Node found for dimension '+ dimName);
			return null;
		}
		var metadata = type == "root" ? metTmp.GENERAL_FIELDS: (type == "node" ? metTmp.NODE_FIELDS : metTmp.LEAF_FIELDS);
		var node = {};
		for (var i = 0; i < metadata.length; i++) {
			if (metadata[i].TYPE == 'Number') {
				node[metadata[i].ID] = -1;
			} else if (metadata[i].TYPE == 'Date') {
				node[metadata[i].ID] = new Date();
			} else {
				node[metadata[i].ID] = '';
			}
			if (metadata[i].FIX_VALUE && metadata[i].FIX_VALUE.length > 0) {
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
		node.visible = true;
		node.type = "folder";
		node.checked = false;
		node.leaf = type == "leaf";
		return node;
	}

	/* Add new hierarchy in the tree with context menu */
	$scope.addHier = function(item, parent, event) {
		var promise = $scope.editNode({}, item,true);
		if (promise !== null) {
			promise.then(
				function(newItem) {
					var tmpItem = $scope.createEmptyNode("node");
					for (key in newItem) {
						tmpItem[key] = newItem[key];
					}
					var keyName = tmpItem.aliasName !== undefined ? tmpItem.aliasName: $scope.dim.DIMENSION_NM + "_NM_LEV";
					var keyId = tmpItem.aliasId !== undefined ? tmpItem.aliasId : $scope.dim.DIMENSION_NM + "_CD_LEV";
					tmpItem.name = tmpItem[keyName];
					tmpItem.id = tmpItem[keyId];
					tmpItem.$parent = item;
					tmpItem.LEVEL = item.LEVEL && item.LEVEL>=0 ? item.LEVEL + 1 : 1;
					if (item.children.length == 1 && item.children[0].fake == true) {
						item.children = [ tmpItem ];
					} else {
						item.children.splice(0, 0, tmpItem);
					}
					$scope.treeDirty = true;
				}, function() {
					// nothing to do, request cancelled.
				});
		}
	}
	/* Modify the hierarchy of the tree with context menu */
	$scope.modifyHier = function(item, parent, event) {
		var promise = $scope.editNode(item, parent);
		promise.then(
			function(newItem) {
				if (newItem !== null && newItem !== undefined) {
					var keyName = newItem.aliasName !== undefined ? newItem.aliasName: $scope.dim.DIMENSION_NM + "_NM_LEV";
					var keyId = newItem.aliasId !== undefined ? newItem.aliasId : $scope.dim.DIMENSION_NM + "_CD_LEV";
					newItem.name = newItem[keyName] !== undefined ? newItem[keyName] : item.name;
					newItem.id = newItem[keyId] !== undefined ? newItem[keyId] : item.name;
					newItem.$parent=item.$parent;
					if (parent && parent.children) {
						var idx = $scope.indexOf(parent.children,item, "id");
						if (idx > -1) {
							parent.children.splice(idx,1);						
							parent.children.splice(idx,0,newItem);
							/*for (var k in newItem){//copy fields
								parent.children[idx][k] = angular.copy(newItem[k]);
							}*/
						}
					} else {
						$scope.hierTree = [ newItem ];
					}
					$scope.treeDirty = true;
				}
			}, function() {
		});
	}
	/*
	 * Clone the hierarchy of the tree with context menu. If the hier not allows
	 * duplicate, show Dialog to modify the new hier
	 */
	$scope.duplicateLeaf = function(item, parent, event) {
		var newItem = angular.copy(item);
		if ($scope.dim && $scope.dim.DIMENSION_NM
				&& $scope.dim.DIMENSION_NM.length > 0) {
			var idx = $scope.indexOf(parent.children, item, 'id');
			var allowDuplicate = $scope.metadataTreeMap[$scope.dim.DIMENSION_NM].CONFIGS.ALLOW_DUPLICATE;
			if (allowDuplicate == false || allowDuplicate == "false") {
				// must modify the dates of validity
				newItem.BEGIN_DT = new Date();
				newItem.END_DT = new Date();
				var promise = $scope.editNode(newItem, parent);
				if (promise !== null) {
					promise.then(
						function(newItem) {
								// check if newItem is totally equal to the old
							var isEqual = true;
							for (k in newItem) {
								if (newItem[k] != item[k]) {
									isEqual = false;
									break;
								}
							}
							// if it is equal show Alert
							if (isEqual) {
								$show.alert($scope.translate.load("sbi.generic.error"),'The duplicate leaf can not be equal to the original');
							} else {
								if (idx >= 0) {
									parent.children.splice(idx, 0,newItem);
									$scope.treeDirty = true;
								}
							}
						}, function() {
					});
				}
			} else {
				if (idx >= 0) {
					parent.children.splice(idx, 0, newItem);
					$scope.treeDirty = true;
				}
			}
		}
	}
	/* Visualize the edit dialog to modify the item with context menu */
	$scope.editNode = function(item, parent,forceEditable) {
		var parentEl = angular.element(document.body);
		var dimName = $scope.dim !== undefined ? $scope.dim.DIMENSION_NM : '';
		var metTmp = angular.copy($scope.metadataTreeMap[dimName]);
		if (metTmp === undefined) {
			$scope.showAlert($scope.translate.load("sbi.generic.error"), 'No metadata found for dimension '+ dimName);
			return null;
		}
		// take generals_fields if it is root[parent is null], leaf_fields if it
		// is leaf or node_fields if it is node
		var metadata = parent == undefined || parent == null ? metTmp.GENERAL_FIELDS : item.leaf == true ? metTmp.LEAF_FIELDS : metTmp.NODE_FIELDS;
		if (metadata && forceEditable == true){
			 for (var i = 0 ; i < metadata.length; i++){
				 metadata[i].EDITABLE=true;
				 metadata[i].VISIBLE=true;
			 }
		 }
		return $mdDialog.show({
					templateUrl : sbiModule_config.contextName	+ '/js/src/angular_1.4/tools/hierarchies/templates/hierSrcDialog.html',
					parent : angular.element(document.body),
					locals : {
						translate : $scope.translate,
						hier : item,
						metadata : metadata
					},
					preserveScope : true,
					clickOutsideToClose : false,
					controller : $scope.hierSrcDialogController
				});
	}
	/* Visualize the confirm dialog to delete the item and call the rest service */
	$scope.deleteHier = function(item, parent, event) {
		// rest service for deleting
		var response = $scope.showConfirm(item);
		response.then(function() {
			if (parent !== undefined && parent !== null) {
				var idx = $scope.indexOf(parent.children, item, 'id');
				parent.children.splice(idx, 1);
				if (parent.children.length == 0) {
					parent.children.push(angular.copy($scope.fakeNode));
				}
			} else {
				item = {};
			}
			$scope.treeDirty = true;
		}, function() {
			// nothing to do, response is 'cancel'
		});
	}
	/* Line Menu of Tree */
	$scope.menuOptionTree = [
			{
				label : $scope.translate.load('sbi.generic.add'),
				icon : "fa fa-plus-circle",
				showItem : function(item, event) {
					// visible if it is NOT a leaf
					return item !== undefined && (item.leaf === undefined || item.leaf == false) && item.fake != true;
				},
				action : $scope.addHier
			},
			{
				label : $scope.translate.load('sbi.generic.clone'),
				icon : "fa fa-clone",
				showItem : function(item, event) {
					// visible if it IS a leaf
					return item !== undefined && item.leaf !== undefined && item.leaf == true && item.fake != true;
				},
				action : $scope.duplicateLeaf
			}, {
				label : $scope.translate.load('sbi.roles.edit'),
				icon : "fa fa-pencil",
				showItem : function(item, event) {
					return item !== undefined && item.fake != true;
				},
				action : $scope.modifyHier
			}, {
				label : $scope.translate.load('sbi.generic.delete'),
				icon : "fa fa-trash",
				showItem : function(item, event) {
					return item !== undefined && item.fake != true;
				},
				action : $scope.deleteHier
			} ];

	$scope.cleanTree = function(tree) {
		var treeCleaned = angular.copy(tree);
		var elements = [ treeCleaned ];
		do {
			var el = elements.shift();
			el.checked = el.visible = el.expanded = el.type = el.sortDirection = undefined;
			el.$parent = null;
			for ( var k in el) {
				if (el[k] instanceof Date) {
					el[k] = $scope.formatDate(el[k]);
				}
			}
			if (el.children !== undefined && el.children.length > 0) {
				for (var i = 0; i < el.children.length; i++) {
					if ((!el.children[i].leaf && !el.children[i].children) || el.children[i].fake == true) {
						el.children.splice(i, 1);
						i--;
					} else {
						elements.push(el.children[i]);
					}
				}
			}
		} while (elements.length > 0);

		return treeCleaned;
	}

	/*Save the tree when clicked the button. Create the parameters for the POST request and remove cyclic object */
	$scope.saveTree = function() {
		if ($scope.dateTree && $scope.dim && $scope.hierMaster
				&& $scope.hierTree && $scope.hierTree.length > 0) {
			// saveHierarchy
			var root = {};
			root.dimension = $scope.dim.DIMENSION_NM;
			root.code = $scope.hierMaster.HIER_CD;
			root.description = $scope.hierMaster.HIER_DS;
			root.name = $scope.hierMaster.HIER_NM;
			root.type = $scope.hierMaster.HIER_TP;
			root.dateValidity = $scope.formatDate($scope.dateTree);
			root.isInsert = false;
			root.doBackup = $scope.doBackup !== undefined ? $scope.doBackup : false;
			// remove cycle object [E.g. possible cycle ->
			// item.$parent.children[0] = item]
			root.root = Array.isArray($scope.hierTree) ? $scope.cleanTree($scope.hierTree[0]) : $scope.cleanTree($scope.hierTree);
			root.root.$parent = undefined;
			var jsonString = angular.toJson(root);
			$scope.toogleLoading();
			var promise = $scope.restService.post('hierarchies','saveHierarchy', jsonString);
			promise.success(
					function(data) {
						if (data.errors === undefined) {
							$scope.treeDirty = false;
							/* clean cache map */
							var keyMap = root.type + '_'+ root.dimension + '_' + root.name+ '_' + root.dateValidity;
							if ($scope.dateFilterTree) {
								keyMap = keyMap+ '_'+ $scope.formatDate($scope.dateFilterTree);
							}
							
							if ($scope.seeHideLeafTree) {
								keyMap = keyMap + '_'+ $scope.seeHideLeafTree;
							}
							$scope.hierTreeCache[keyMap] = undefined;
							$scope.showAlert($scope.translate.load("sbi.generic.info"),$scope.translate.load("sbi.hierarchies.save.correct"));
						} else {
							$scope.showAlert($scope.translate.load("sbi.generic.error"),data.errors[0].message);
						}
						$scope.toogleLoading();
					}).error(
						function(data, status) {
							$scope.showAlert($scope.translate.load("sbi.generic.error"),'Impossible to save the Tree');
							$scope.toogleLoading();
					});
		}
	}

	/* Confirm dialog to delete the item */
	$scope.showConfirm = function(hier) {
		var confirm = $mdDialog.confirm()
			.title('Delete ' + hier.name.toUpperCase())
			.content('Would you like to delete the item?')
			.ariaLabel('Lucky day')
			.ok('Yes')
			.cancel('No');
		return $mdDialog.show(confirm);
	};

	/*
	 * Visualize the create new master hierarchy dialog. If confirmed save the
	 * new element
	 */
	$scope.crateMasterHier = function(filterDate, filterHierarchy) {
		if ($scope.dim && $scope.dateDim) {
			var dialog = $scope.showCreateMaster();
			dialog.then(function(newHier) {
				var dateFormatted = $scope.formatDate($scope.dateDim);
				var item = {
					dimension : $scope.dim.DIMENSION_NM,
					validityDate : dateFormatted
				};
				if (filterDate !== undefined && filterDate !== null) {
					item.filterDate = $scope.formatDate(filterDate);
				}
				if (newHier && filterHierarchy !== undefined
						&& filterHierarchy !== null) {
					item.filterHierType = $scope.hierType.toUpperCase();
					item.filterHierarchy = newHier.HIER_NM;
				}
				for ( var k in newHier) {
					item[k] = angular.copy(newHier[k]);
				}
				var promise = $scope.restService.post('hierarchies','createHierarchyMaster', item);
				promise
					.success(function(data) {
						if (data.errors === undefined) {
							$scope.showAlert($scope.translate.load("sbi.generic.info"), 'Succesfull creation');
						} else {
							$scope.showAlert($scope.translate.load("sbi.generic.error"), data.errors[0].message);
						}
					}).error(
						function(data, status) {
							$scope.showAlert($scope.translate.load("sbi.generic.error"),'Impossible to save the Master hierarchy');
						});
				// TODO fix REST service connection
			}, function() {
			});
		}
	}
	/* Dialog to create the master hierarchy */
	$scope.showCreateMaster = function() {
		if ($scope.dim && $scope.dateDim) {
			var dimName = $scope.dim.DIMENSION_NM;
			if (!$scope.metadataTreeMap[dimName]) {
				$scope.getTreeMetadata($scope.dim);
			}
			return $mdDialog
					.show({
						templateUrl : sbiModule_config.contextName+ '/js/src/angular_1.4/tools/hierarchies/templates/newHierMasterDialog.html',
						parent : angular.element(document.body),
						locals : {
							translate : $scope.translate,
							item : $scope.dim,
							metadataDim : $scope.metadataDimMap[dimName].DIM_FIELDS,
							metadataTree : $scope.metadataTreeMap[dimName].GENERAL_FIELDS
						},
						preserveScope : true,
						clickOutsideToClose : false,
						controller : $scope.newHierarchyController
					});
		}
	}
	/* Dialog to show the info dimension when clicked the info icon in the table */
	$scope.showDetails = function(item) {
		return $mdDialog
				.show({
					templateUrl : sbiModule_config.contextName+ '/js/src/angular_1.4/tools/hierarchies/templates/detailsMasterDialog.html',
					parent : angular.element(document.body),
					locals : {
						translate : $scope.translate,
						item : item,
						metadataDim : $scope.metadataDimMap[$scope.dim.DIMENSION_NM].DIM_FIELDS
					},
					preserveScope : true,
					clickOutsideToClose : false,
					controller : $scope.showDetailsController
				});
	}
	/*
	 * Dialog to show the hierarchies list when dropped and element from table
	 * to tree
	 */
	$scope.showListHierarchies = function() {
		return $mdDialog
				.show({
					templateUrl : sbiModule_config.contextName+ '/js/src/angular_1.4/tools/hierarchies/templates/listHierarchiesDialog.html',
					parent : angular.element(document.body),
					locals : {
						translate : $scope.translate,
						listHierarchies : $scope.hierarchiesMaster
					},
					preserveScope : true,
					clickOutsideToClose : false,
					controller : $scope.showListHierarchyController
				});
	}

	$scope.applyFilter = function(choose) {
		// use to apply the filter only when is clicked the icon
		var date = $scope.dateFilterTree;
		var seeElement = $scope.seeHideLeafTree;
		var dateFormatted;
		if (date !== undefined) {
			dateFormatted = $scope.formatDate(date);
		}
		// get the Tree if one off two filters are active. Else if the filters were applyed before, but not now, get the tree without them
		if ((seeElement !== undefined && seeElement == true) || (dateFormatted !== undefined && dateFormatted.length > 0)) {
			$scope.getTree(dateFormatted, seeElement);
			$scope.hasFilterElementOrDate = true;
		}else if ($scope.hasFilterElementOrDate == true){
			$scope.getTree();
			$scope.hasFilterElementOrDate = false;
		}
		// apply filter on source side (left) or Tree side (right)
		$scope.filterByTreeTrigger = angular.copy($scope.filterByTree);
		$scope.orderByTreeTrigger = angular.copy($scope.orderByTree);
	}
	/* remove the applied filters */
	$scope.removeFilter = function(choose) {
		$scope.filterByTreeTrigger = "";
		$scope.filterByTree = "";
		$scope.orderByTreeTrigger = "";
		$scope.orderByTree = "";
		// get tree without filters if they were active
		if (($scope.seeHideLeafTree !== undefined && $scope.seeHideLeafTree != false) || ($scope.dateFilterTree !== undefined && $scope.dateFilterTree.length > 0)) {
			$scope.getTree();
		}
		$scope.dateFilterTree = undefined;
		$scope.seeHideLeafTree = false;
		$scope.hasFilterElementOrDate = false;
	}
	// toggle the filters visibility when clicked the filter icon
	$scope.toggleSeeFilter = function(choose) {
		if (choose == 'dim') {
			$scope.seeFilterDim = !$scope.seeFilterDim;
		} else {
			$scope.seeFilterTree = !$scope.seeFilterTree;
		}
	}

	$scope.hierSrcDialogController = function($scope, $mdDialog, translate, hier, metadata) {
		$scope.translate = translate;
		$scope.hier = angular.copy(hier);
		$scope.hier.BEGIN_DT = hier.BEGIN_DT !== undefined ? new Date(hier.BEGIN_DT) : new Date();
		$scope.hier.END_DT = hier.END_DT !== undefined ? new Date(hier.END_DT) : new Date();
		$scope.metadata = metadata;
		$scope.closeDialog = function() {
			$mdDialog.cancel();
		}
		$scope.saveHier = function() {
			$mdDialog.hide($scope.hier);
		}
	}

	$scope.showListHierarchyController = function($scope, $mdDialog, translate, listHierarchies) {
		$scope.translate = translate;
		$scope.listHierarchies = angular.copy(listHierarchies);
		$scope.all = false;
		// create path for each element
		for (var i = 0; i < $scope.listHierarchies.length; i++) {
			var path = '';
			for (var p = $scope.listHierarchies[i]; p != null && p != undefined; p = p.$parent) {
				path = p.HIER_NM + '\\' + path;
			}
			$scope.listHierarchies[i].path = path;
		}

		$scope.selectAll = function() {
			$scope.all = !$scope.all;
			for (var i = 0; i < $scope.listHierarchies.length; i++) {
				$scope.listHierarchies[i].checked = $scope.all;
			}
		}
		
		$scope.closeDialog = function() {
			$mdDialog.hide();
		}
		$scope.cancelDialog = function() {
			$mdDialog.cancel();
		}
	}

	$scope.showDetailsController = function($scope, $mdDialog, translate, item,
			metadataDim) {
		$scope.translate = translate;
		$scope.item = item;
		$scope.metadataDim = angular.copy(metadataDim);

		$scope.closeDialog = function() {
			$mdDialog.cancel();
		}
	}

	$scope.newHierarchyController = function($scope, $mdDialog, translate, item, metadataDim, metadataTree) {
		$scope.translate = translate;
		$scope.dim = {};
		$scope.metadataDim = angular.copy(metadataDim);
		$scope.metadataTree = angular.copy(metadataTree);
		$scope.codes = angular.copy(metadataDim);
		$scope.descriptions = angular.copy(metadataDim);
		$scope.selectedItemsLeft = [];
		$scope.selectedItemsRight = [];
		$scope.metadataDimExport = [];
		$scope.hierNew = {};
		var level = 1;
		$scope.showWarningMessage = false;
		// force BEGIN_DT and END_DT in metadataTree
		$scope.metadataTree.push({
			"ID" : "BEGIN_DT",
			"NAME" : "Begin Date",
			"VISIBLE" : true,
			"EDITABLE" : true,
			"PARENT" : false,
			"TYPE" : "Date"
		});
		$scope.metadataTree.push({
			"ID" : "END_DT",
			"NAME" : "End Date",
			"VISIBLE" : true,
			"EDITABLE" : true,
			"PARENT" : false,
			"TYPE" : "Date"
		});

		$scope.removeElement = function(array, el) {
			var idx = $scope.indexOf(array, el);
			if (idx >= 0) {
				array.splice(idx, 1);
			}
		}

		$scope.indexOf = function(array, el) {
			for (var i = 0; i < array.length; i++) {
				if (el.ID !== undefined && array[i].ID == el.ID) {
					return i;
				} else if (el.code && el.name && array[i].name.ID == el.name.ID && array[i].code.ID == el.code.ID) {
					return i;
				}
			}
		}

		$scope.removeRecursive = function() {
			var code = $scope.mtRecursive.code;
			var name = $scope.mtRecursive.name;
			var hasCopy = $scope.mtRecursive.hasCopy;
			$scope.mtRecursive = undefined;
			code.level = undefined;
			name.level = undefined;
			code.isSelected = undefined;
			name.isSelected = undefined;
			$scope.metadataDim.push(code);
			if (!hasCopy){
				$scope.metadataDim.push(name);
			}
		}

		$scope.moveToRecursive = function(item) {
			item.isLast = undefined;
			var pos = $scope.metadataDimExport.length - 1;
			$scope.metadataDimExport.splice(pos, 1);
			if ($scope.metadataDimExport.length > 0) {
				$scope.metadataDimExport[pos - 1].isLast = true;
			}
			$scope.mtRecursive = item;
			level--;
		}

		$scope.toRight = function(source, dest, itemsSource) {
			//you can move to right if 1 or 2 elements are selcted. If only 1 item is selected, use it as 2° item and set flag hasCopy = true 
			if (itemsSource.length == 2 || itemsSource.length == 1) {
				var newLevel = {};
				newLevel.code = itemsSource[0];
				newLevel.name = itemsSource[1] !== undefined ? itemsSource[1] : angular.copy(itemsSource[0]);
				newLevel.hasCopy = itemsSource[1] === undefined;
				newLevel.code.level = angular.copy(level);
				newLevel.name.level = angular.copy(level);
				level++;
				for (var i = 0; i < itemsSource.length; i++) {
					$scope.removeElement(source, itemsSource[i]);
					itemsSource[i].isSelected = undefined;
				}
				newLevel.isLast = true;
				if (dest.length > 0) {
					dest[dest.length - 1].isLast = false;
				}
				itemsSource.splice(0, itemsSource.length);
				dest.push(newLevel);
			}
		}

		$scope.toLeft = function(source, dest, itemsSource) {
			var maxLevel = -1;
			if (itemsSource.length > 0) {
				for (var i = 0; i < itemsSource.length; i++) {
					if (maxLevel < itemsSource[i].code.level) {
						maxLevel = itemsSource[i].code.level;
					}
					itemsSource[i].code.level = undefined;
					itemsSource[i].name.level = undefined;
					dest.push(itemsSource[i].code);
					if (!itemsSource[i].hasCopy){
						dest.push(itemsSource[i].name);
					}
					itemsSource[i].isSelected = undefined;
					$scope.removeElement(source, itemsSource[i]);
				}
				itemsSource.splice(0, itemsSource.length);
				if (maxLevel != (level - 1)) {
					for (var i = 0; i < source.length; i++) {
						if (source[i].code.level > maxLevel) {
							source[i].code.level--;
							source[i].name.level--;
						}
					}
				}
				if (source.length > 0) {
					source[source.length - 1].isLast = true;
				}
				level = source.length + 1;
			}
		}

		$scope.moveUp = function(item) {
			var array = $scope.metadataDimExport;
			var i = $scope.indexOf(array, item);
			if (i > 0 && array.length > 1) {
				if (array[i].isLast) {
					array[i].isLast = undefined;
					array[i - 1].isLast = true;
				}
				var tmp = angular.copy(array[i - 1]);
				array[i - 1] = angular.copy(array[i]);
				array[i] = angular.copy(tmp);
				array[i - 1].code.level--;
				array[i - 1].name.level--;
				array[i].code.level++;
				array[i].name.level++;
			}
		}

		$scope.moveDown = function(item) {
			var array = $scope.metadataDimExport;
			var i = $scope.indexOf(array, item);
			if (i < (array.length - 1) && array.length > 1) {
				if (array[i + 1].isLast) {
					array[i + 1].isLast = undefined;
					array[i].isLast = true;
				}
				var tmp = angular.copy(array[i + 1]);
				array[i + 1] = angular.copy(array[i]);
				array[i] = angular.copy(tmp);
				array[i + 1].code.level++;
				array[i + 1].name.level++;
				array[i].code.level--;
				array[i].name.level--;
			}
		}

		// move selected item from posSelected to posDestination if they are set
		// and different
		$scope.moveTo = function(posDestination) {
			if (posDestination && posDestination.length > 0) {
				var dest = posDestination == 'right' ? $scope.metadataDimExport : $scope.metadataDim;
				var source = posDestination == 'right' ? $scope.metadataDim : $scope.metadataDimExport;
				var itemsSource = posDestination == 'right' ? $scope.selectedItemsLeft : $scope.selectedItemsRight;
				if (posDestination == "right") {
					$scope.toRight($scope.metadataDim, $scope.metadataDimExport, $scope.selectedItemsLeft);
				} else if (posDestination == "left") {
					$scope.toLeft($scope.metadataDimExport, $scope.metadataDim, $scope.selectedItemsRight);
				}
			}
			$scope.showWarningMessage = false;
		}
		/*
		 * Toggle the element clicked and [remove,add] it to the array of
		 * selected items
		 */
		$scope.toggleItem = function(item, pos) {
			var selected = item.isSelected == undefined ? true : !item.isSelected;
			var arraySelected = pos == 'right' ? $scope.selectedItemsRight : $scope.selectedItemsLeft;
			if (selected == true && pos == 'left' && arraySelected.length >= 2) {
				$scope.showWarningMessage = true;
				return;
			}
			if (selected == true) {
				item.isSelected = selected;
				arraySelected.push(item);
			} else {
				item.isSelected = selected;
				$scope.removeElement(arraySelected, item);
			}
			$scope.showWarningMessage = false;
		}

		$scope.closeDialog = function() {
			$mdDialog.cancel();
		}

		$scope.saveHier = function() {
			var levels = [];
			for (var i = 0; i < $scope.metadataDimExport.length; i++) {
				levels.push({
					"CD" : $scope.metadataDimExport[i].code.ID,
					"NM" : $scope.metadataDimExport[i].name.ID
				});
			}
			$scope.hierNew.levels = levels;
			if ($scope.mtRecursive && $scope.code && $scope.descr) {
				$scope.hierNew.recursive = {
					"NM" : $scope.mtRecursive.name.ID,
					"CD" : $scope.mtRecursive.code.ID,
					"NM_PARENT" : $scope.descr.ID,
					"CD_PARENT" : $scope.code.ID
				};
			}
			$mdDialog.hide($scope.hierNew);
		}
	}

	// Create an alert dialog with a message
	$scope.showAlert = function(title, message) {
		$scope.log.log(message);
		// if angular material version < 1.0.0_rc5 not has textContent function
		if (typeof $mdDialog.alert().textContent == 'function') {
			$mdDialog.show(
					$mdDialog.alert()
					.parent(angular.element(document.body))
					.clickOutsideToClose(false)
					.title(title)
					.textContent(message) // FROM angular material 1.0
					.ok('Ok'));
		} else {
			$mdDialog.show($mdDialog.alert()
					.parent(angular.element(document.body))
					.clickOutsideToClose(false)
					.title(title)
					.content(message)
					.ok('Ok'));
		}
	};

	$scope.formatDate = function(date) {
		var mm = date.getMonth() < 10 ? '0' + (date.getMonth() + 1) : ''+ (date.getMonth() + 1);
		return date.getFullYear() + '-' + mm + '-' + date.getDate();
	}

	$scope.indexOf = function(myArray, myElement, key) {
		if (myArray === undefined || myElement === undefined){
			return -1;
		}
		for (var i = 0; i < myArray.length; i++) {
			if (myArray[i][key] !== undefined && myArray[i][key] !== null && myArray[i][key] == myElement[key]) {
				return i;
			}
		}
		return -1;
	};

	$scope.toogleLoading = function() {
		if ($scope.showLoading) {
			$timeout(function() {
				$scope.showLoading = false;
			}, 400, true);
		} else {
			$timeout(function() {
				$scope.showLoading = true;
			}, 0, true);
		}
	}
}
