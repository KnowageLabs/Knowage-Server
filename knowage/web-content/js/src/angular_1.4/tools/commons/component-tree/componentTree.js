/**
 * @authors Alessio Conese (alessio.conese@eng.it)
 * @authors Benedetto Milazzo (benedetto.milazzo@eng.it)
 * 
 * N.B. Component in WIP
 */

(function() {
	var scripts = document.getElementsByTagName('script')
	var componentTreePath = scripts[scripts.length-1].src;

	angular.module('componentTreeModule', [ 'ngMaterial', 'ui.tree'])
	.directive('componentTree', function($compile) {
		return {
			templateUrl: componentTreePath.substring(0, componentTreePath.lastIndexOf('/') + 1) + 'template/component-tree.html',
			transclude : true,
			priority: 1000,
			scope: {
				ngModel : '='
				, id : '@'
				, createTree: '=?' //if true, the ngModel data will be parsed, if not the JSON is already in correct form
				, clickFunction : '&?' //function to call when click into element list
				, selectedItem : '=?' //optional to get the selected item value
				, showFiles : '=?' //boolean value
				, multiSelect : '=?' //boolean value
				, textSearch : '=?' //text to search
				, fieldsSearch : '=?' //array of the fields on which apply the filter
				, orderBy : '=?' //field on which order the array
				, menuOption : '=?' //menu to show on hover
				, keys : '=?' //object of the keys 
				, enableDrag: '=?'
				, optionsDragDrop: '=?'
				, enableClone: '=?'
				, showEmptyPlaceholder: '=?'
				, noDropEnabled: '=?'
				, subnodeKey : '=?'
				, textToShowKey : '=?'
				, iconLeafCls : '=?'
				, iconDocumentCls : '=?'
				, isFolderFn : '&?'
				, isOpenFolderFn : '&?'
				, isDocumentFn : '&?'
				, dynamicTree : '@?'
			},
			controller: componentTreeControllerFunction,
			controllerAs: 'ctrl',

			compile: function (tElement, tAttrs, transclude) {
				return {
					pre: function (scope, element, attrs, ctrl, transclud) { 

					},

					post: function (scope, element, attrs, ctrl, transclud) {
						//Customize the keys to use different JSON 
						var elementId = scope.keys !== undefined && scope.keys.id !==undefined && scope.keys.id.length > 0 ? scope.keys.id : 'id' ;
						var parentId = scope.keys !== undefined && scope.keys.parentId !==undefined && scope.keys.parentId.length > 0 ? scope.keys.parentId : 'parentId' ;
//						var subfoldersId = scope.keys !== undefined && scope.keys.subfolders !==undefined && scope.keys.subfolders.length > 0 ? scope.keys.subfolders : 'subfolders' ;
						var subfoldersId = (attrs.subnodeKey &&  attrs.subnodeKey.trim() != '')? attrs.subnodeKey.trim() : 'subfolders' ;
//						var label = scope.keys !== undefined && scope.keys.label!==undefined && scope.keys.label.length > 0 ? scope.keys.label: 'name' ;
						var label = (attrs.textToShowKey &&  attrs.textToShowKey.trim() != '')? attrs.textToShowKey.trim() : 'name' ;
						
						var iconLeafCls = (attrs.iconLeafCls &&  attrs.iconLeafCls != '')? attrs.iconLeafCls : 'fa fa-leaf';
						var iconDocumentCls = (attrs.iconDocumentCls &&  attrs.iconDocumentCls != '')? attrs.iconDocumentCls : 'fa fa-file';

						scope.label = label;
						scope.subfoldersId = subfoldersId;

						scope.iconFolder = 'fa fa-folder';
						scope.iconFolderOpen = 'fa fa-folder-open';	
						scope.iconLeaf = iconLeafCls;
						scope.iconDocument = iconDocumentCls;

						scope.seeTree = false;

						scope.createTreeStructure = function (folders) {
							if (attrs.createTree !==undefined  && (attrs.createTree ==true || attrs.createTree =='true')) {
								if (folders !== undefined && folders.length > 0 && folders[0][subfoldersId] === undefined) {
									var mapFolder = {};	

									for (var i = 0 ; i < folders.length; i ++ ) {
										folders[i][subfoldersId] = [];
										mapFolder[folders[i][elementId]] = folders[i]; 
									}

									var treeFolders = [];
									for (var i = 0 ; i < folders.length; i ++ ) {
										//if folder has not father, is a root folder
										if (folders[i][parentId] == null || folders[i][parentId] == 'null') {
											treeFolders.push(folders[i]);
										}
										else{
											//search parent folder with hasmap and attach the son
											mapFolder[folders[i][parentId]][subfoldersId].push(folders[i]);
										}
										//update linear structure with tree structure
									}
									folders = treeFolders; 
								}
							}
							return folders;
						};

						scope.initializeFolders = function (folders, parent) {
							for (var i = 0 ; i < folders.length; i ++ ) {
								var folder = folders[i];

								folder.checked = folder.checked === undefined ? false : folder.checked;
								folder.expanded = folder.expanded === undefined ? false : folder.expanded;
								folder.type = folder.type === undefined ? 'folder' : folder.type;
								folder.visible = folder.visible === undefined ? true : folder.visible;
								folder.$parent = parent;

								if (folder[subfoldersId] !== undefined && folder[subfoldersId].length > 0) {
									scope.initializeFolders(folder[subfoldersId], folder);
									if (attrs.orderBy) {
										folder.sortDirection = folder.sortDirection === undefined ? 'desc' : folder.sortDirection;
									}
								}
								for (var j = 0; folder.biObjects !== undefined && j < folder.biObjects.length ; j++) {
									var folderBiObject = folder.biObjects[j];

									folderBiObject.type = folderBiObject.type == undefined ?  'biObject' : folderBiObject.type;
									folderBiObject.checked = folderBiObject.checked == undefined ? false : folderBiObject.checked;
									folderBiObject.visible = folderBiObject.visible == undefined ?  true : folderBiObject.visible;
									folderBiObject.$parent = parent;
								}
							}
						};

						scope.initializeFolders(scope.ngModel, null);
						scope.ngModel = scope.createTreeStructure(scope.ngModel);
						scope.folders = scope.ngModel;

						var id = 'dcTree';
						if(attrs.id) {
							id = attrs.id;
						}

						var treeElement = angular.element(element[0].querySelector('#tree-container'));
						if (scope.enableClone == true) {
							treeElement.attr('data-clone-enabled','true');
						}
						if (scope.showEmptyPlaceholder == true) {
							treeElement.attr('data-empty-placeholder-enabled','true');
						}
						if (scope.noDropEnabled == true) {
							treeElement.attr('data-nodrop-enabled','true');
						}
						if (scope.optionsDragDrop) {
							//treeElement.attr('ui-tree','optionsDragDrop');
						}
						
						if(scope.multiSelect && (scope.multiSelect == true || scope.multiSelect == 'true')) {
							if (!attrs.selecteditem) {
								scope.selectedItem = [];
							}
						}

//						if(scope.isFolderFn && (typeof scope.isFolderFn == 'function')) {
//							scope.isFolder = scope.isFolderFn;
//						} else {
//							scope.isFolder = function(node) {
//								return(
//										!node.expanded 
//										&& node[scope.subfoldersId] !== undefined 
//										&& (node[scope.subfoldersId].length > 0 
//												|| node.biObjects.length > 0
//										)
//								);
//							};
//						}
						scope.isFolder = function(node) {
							if(scope.isFolderFn && (typeof scope.isFolderFn == 'function')) {
								return scope.isFolderFn({node: node});
							} else {
								return(
									!node.expanded 
									&& (
										(node[scope.subfoldersId] !== undefined 
												&& node[scope.subfoldersId].length > 0) 
										|| (node.biObjects !== undefined 
												&& node.biObjects.length > 0)
										)
								);
							}
							
						};
						
//						if(scope.isOpenFolderFn && (typeof scope.isOpenFolderFn == 'function')) {
//							scope.isOpenFolder = scope.isOpenFolderFn;
//						} else {
//							scope.isOpenFolder = function(node) {
//								return(
//										node.expanded 
//										&& node[scope.subfoldersId] !== undefined 
//										&& (node[scope.subfoldersId].length > 0 
//												|| node.biObjects.length > 0
//										)
//								);
//							};
//						}
						scope.isOpenFolder = function(node) {
							if(scope.isOpenFolderFn && (typeof scope.isOpenFolderFn == 'function')) {
								return scope.isOpenFolderFn({node: node});
							} else {
								return(
									node.expanded 
									&& (
										(node[scope.subfoldersId] !== undefined 
												&& node[scope.subfoldersId].length > 0) 
										|| (node.biObjects !== undefined 
												&& node.biObjects.length > 0)
										)
								);
							}
						};

//						if(scope.isDocumentFn && (typeof scope.isDocumentFn == 'function')) {
//							scope.isDocument = scope.isDocumentFn;
//						} else {
//							scope.isDocument = function(node) {
//								return(
//										node[scope.subfoldersId] === undefined 
//										|| ( Array.isArray(node[scope.subfoldersId])
//												&& node[scope.subfoldersId].length != 0 
//												&& (node.biObjects != undefined 
//														|| node.biObjects.length != 0)
//										)
//								);
//							};
//						}
						scope.isDocument = function(node) {
							if(scope.isDocumentFn && (typeof scope.isDocumentFn == 'function')) {
								return scope.isDocumentFn({node: node});
							} else {
								return(
										node[scope.subfoldersId] === undefined 
										|| (
											(Array.isArray(node[scope.subfoldersId])
												&& node[scope.subfoldersId].length == 0 )
											|| (node.biObjects != undefined 
												&& node.biObjects.length == 0)
										)
								);
							}
						};
						
						scope.seeTree = true;
					}
				};
			},
		};
	});

	function componentTreeControllerFunction($scope,$timeout,$mdDialog) {
		$scope.toogleSelected = function(element, parent) {
			if (element !== undefined && $scope.multiSelect) {
				//check the element as the parent. If not the parent doesn't exist, toggle the element check
				element.checked = parent === undefined ? !element.checked : parent.checked; 
				//different insertion if is allowed the multi-selection
				if ( element.checked ) { //if the element is just checked, insert into selectedItem, else remove it
					$scope.selectedItem.push(element);
				}else{
					var idx = $scope.selectedItem.indexOf(element);
					$scope.selectedItem.splice(idx, 1);
				}

				if (element.type == 'folder') {
					for (var i = 0; element[$scope.subfoldersId] && i < element[$scope.subfoldersId].length; i++) {
						$scope.toogleSelected(element[$scope.subfoldersId][i], element);
					}
					for (var j = 0; element.biObjects !== undefined && j < element.biObjects.length ; j++ ) {
						$scope.toogleSelected(element.biObjects[j],element);
					}
				}
			}
		};

		$scope.openFolder = function (node) {
			node.expanded = !node.expanded;
			$scope.setSelected(node);
		};

		$scope.setSelected = function (item) {
			if (!$scope.multiSelect) {
				$scope.selectedItem = item;
			}

			//if present a click function, use it
			if (typeof $scope.clickFunction == 'function') {
				$scope.clickFunction({item : item});
			}
		};

		$scope.toogleSort = function(element) {
			if(element.sortDirection && element[$scope.subfoldersId]) {
				element.sortDirection = element.sortDirection == 'asc' ? 'desc' : 'asc';
				var field = $scope.orderBy;
				element[$scope.subfoldersId].sort($scope.orderFunction(field,element.sortDirection));
			}
		};

		$scope.orderFunction = function(key,direction) {
			return function(a,b) {
				var x = a[key]; var y = b[key];
				var val = ((x < y) ? -1 : ((x > y) ? 1 : 0));
				return direction =='asc' ? val : -val;
			};
		};

		//call each time that the orederBy value changes
		$scope.$watch('orderBy', function () {
			if ($scope.orderBy !== undefined && $scope.orderBy.length > 0) {
				var field = $scope.orderBy;
				if ($scope.selectedItem !== undefined) {
					//take the parent
					$scope.toogleSort($scope.selectedItem.$parent);
				}else{
					$scope.toogleSort($scope.ngModel);
				}
			}
		});

		$scope.$watch('textSearch', function() {
			if ($scope.textSearch !== undefined && $scope.textSearch.length > 0) {
				for (var i = 0; i < $scope.ngModel.length; i++) {
					$scope.filterString($scope.ngModel[i]);
				}
			}
			if ($scope.textSearch !== undefined && $scope.textSearch.length == 0) {
				for (var i = 0; i < $scope.ngModel.length; i++) {
					$scope.resetVisible($scope.ngModel[i]);
				}
			};
		});
		
		
		$scope.dynamicTree = 
			($scope.dynamicTree != undefined 
			&& ($scope.dynamicTree == true 
				|| $scope.dynamicTree.trim() == "" 
				|| $scope.dynamicTree.toLowerCase() != 'false'));
		
		
		var checkIgnoreDeepObjectChange = function(newValue, oldValue) {
			var flagToReturn = false;

			if(newValue === oldValue) {
				flagToReturn = true;
			} else {
				if(Array.isArray(newValue) && Array.isArray(oldValue) && newValue.length == oldValue.length) {
					for(var i = 0; i < newValue.length; i++) {
						var newValueArrayItem = newValue[i];
						var oldValueArrayItem = oldValue[i];
						
						if((newValueArrayItem.checked != oldValueArrayItem.checked)
								|| (newValueArrayItem.expanded != oldValueArrayItem.expanded)) {
							
							console.log('TRUE (change of "checked" or "expanded")');
							flagToReturn = true;
						}
						
						if(!flagToReturn) {
							var newValueArrayItemChildren = newValueArrayItem[$scope.subfoldersId];
							var oldValueArrayItemChildren = oldValueArrayItem[$scope.subfoldersId];
							
							if(newValueArrayItemChildren 
									&& newValueArrayItemChildren.length > 0
									&& oldValueArrayItemChildren 
									&& oldValueArrayItemChildren.length > 0
									&& newValueArrayItemChildren.length == oldValueArrayItemChildren.length) {
								
								console.log('RECURSIVE');
								console.log('oldValueArrayItemChildren -> ', oldValueArrayItemChildren);
								console.log('newValueArrayItemChildren -> ', newValueArrayItemChildren);
								
								flagToReturn = flagToReturn || checkIgnoreDeepObjectChange(newValueArrayItemChildren, oldValueArrayItemChildren);
							} 
						}
					}
				}
			}
			
			return flagToReturn;
		};
		
//		var watchedNgModel = null;
		var watchedNgModel = function() { return $scope.ngModel;};
		
		var updateWatchedItem = function(newValue, oldValue) {
			if(checkIgnoreDeepObjectChange(newValue, oldValue)) {
				return;
				
			} else {
				$scope.seeTree = false;
				
				$scope.initializeFolders($scope.ngModel, null);
				$scope.ngModel = $scope.createTreeStructure($scope.ngModel);
				$scope.folders = $scope.ngModel;
				
				$timeout(function() {
					$scope.seeTree = true;
				},400,true);
			}
		};
		
		
		if($scope.dynamicTree) {
			$scope.$watch( watchedNgModel, updateWatchedItem, true);
		} else {
			$scope.$watchCollection( watchedNgModel, updateWatchedItem, true);
		}
		
		$scope.resetVisible = function(element) {
			element.visible = true;
			if (element[$scope.subfoldersId] !== undefined) {
				for (var i =0 ;i < element[$scope.subfoldersId].length; i++) {
					$scope.resetVisible(element[$scope.subfoldersId][i]);
				}
				for (var j = 0; element.biObjects !== undefined && j < element.biObjects.length; j++ ) {
					$scope.resetVisible(element.biObjects[j]);
				}
			}
		};

		$scope.filterString = function(element) {
			var visible = true;
			if ($scope.textSearch && $scope.fieldsSearch) {
				//if the filters is empty, visible = true, else start with visible = false
				visible = $scope.fieldsSearch.length == 0 || $scope.textSearch.length == 0; 
				//search the text filter in each fields specify in filterBy object, until visible == false
				for (var i =0; visible == false && i < $scope.fieldsSearch.length;i++) {
					visible = element[$scope.fieldsSearch[i]].toUpperCase().indexOf($scope.textSearch.toUpperCase()) > -1;
				}

				if (element.type == 'folder' && element[$scope.subfoldersId] !==undefined ) {
					for (var i =0 ; i < element[$scope.subfoldersId].length; i++) {
						if ($scope.filterString(element[$scope.subfoldersId][i]) == true ) {
							visible = true;
						}
					}
					for (var j = 0; element.biObjects !==undefined && j < element.biObjects.length ; j++ ) {
						if ($scope.filterString(element.biObjects[j]) == true) {
							visible = true;
						}
					}
				}
			}
			element.visible = visible;
			return visible;
		};

		$scope.detectBrowser = function() {
			var userAgent = window.navigator.userAgent;
			var browsers = {
					chrome: /chrome/i, 
					safari: /safari/i, 
					firefox: /firefox/i, 
					ie: /internet explorer/i
			};

			for(var key in browsers) {
				if (browsers[key].test(userAgent)) {
					return key;
				}
			};
			return 'unknown';
		};

		$scope.checkSeeTree = function() {
			return $scope.seeTree;
		};

		$scope.browser = $scope.detectBrowser();

		if ($scope.browser == 'firefox') {
			$scope.classLayout='layout-padding';
		}else{
//			$scope.classLayout='layout-fill';
		}
	};
})();