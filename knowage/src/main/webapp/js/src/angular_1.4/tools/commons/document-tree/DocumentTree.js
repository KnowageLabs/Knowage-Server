/**
 * @authors Alessio Conese (alessio.conese@eng.it)
 *
 */

angular.module('document_tree', [ 'ngMaterial', 'ui.tree','sbiModule'])
.filter('i18n', function(sbiModule_i18n) {
		return function(label) {
			return sbiModule_i18n.getI18n(label);
		}
	})
.directive('documentTree',
		function($compile,sbiModule_config) {
	return {
		templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/commons/document-tree/template/document-tree.html',
		transclude : true,
		priority:1000,
		scope: {
			ngModel : '='
				, id : "@"
					, personalFolders: "@?"
						, createTree:"=?" //if true, the ngModel data will be parsed, if not the JSON is already in correct form
							, clickFunction : "&" //function to call when click into element list
								, selectedItem : "=?" //optional to get the selected  item value
									, showFiles : '=?' //boolean value
										, multiSelect : "=?" //boolean value
											, multiSelectLeafes : "=?" //boolean value
												, textSearch : "=?" //text to search
													, fieldsSearch : '=?' //array of the fields on which apply the filter
														, orderBy : '=?' //field on which order the array
															, menuOption : "=?" //menu to show on hover
																, keys : '=?' //object of the keys
																	, enableDrag:"=?"
																		, highlightsSelectedItem: "=?"
																			, optionsDragDrop:"=?"
																				, enableClone:"=?"
																					, showEmptyPlaceholder :"=?"
																						, noDropEnabled:"=?"
																							, translate:"@?"
		},
		controller: DocumentTreeControllerFunction,
		controllerAs: 'ctrl',
		compile: function (tElement, tAttrs, transclude) {
			return {
				pre: function preLink(scope, element, attrs, ctrl, transclud) {
				},
				post: function postLink(scope, element, attrs, ctrl, transclud) {
					//Customize the keys to use different JSON
					var elementId = scope.keys !== undefined && scope.keys.id !==undefined && scope.keys.id.length > 0 ? scope.keys.id : 'id' ;
					var parentId = scope.keys !== undefined && scope.keys.parentId !==undefined && scope.keys.parentId.length > 0 ? scope.keys.parentId : 'parentId' ;
					scope.subfoldersId = scope.keys !== undefined && scope.keys.subfolders !==undefined && scope.keys.subfolders.length > 0 ? scope.keys.subfolders : 'subfolders' ;
					scope.label = scope.keys !== undefined && scope.keys.label!==undefined && scope.keys.label.length > 0 ? scope.keys.label: 'name' ;
					scope.iconDocument = scope.keys !== undefined && scope.keys.iconDocument !==undefined && scope.keys.iconDocument.length > 0 ? scope.keys.iconDocument: 'fa fa-file' ;

					var subfoldersId = scope.subfoldersId;

					scope.iconFolder 		= scope.keys !== undefined && scope.keys.iconFolder !==undefined && scope.keys.iconFolder.length > 0 ? scope.keys.iconFolder: 'fa fa-square' ;
					scope.iconFolderOpen 	= scope.keys !== undefined && scope.keys.iconFolderOpen !==undefined && scope.keys.iconFolderOpen.length > 0 ? scope.keys.iconFolderOpen: 'fa fa-square-o' ;
					scope.multiFolders 		= scope.keys !== undefined && scope.keys.multiFolders !==undefined && scope.keys.multiFolders.length > 0 ? scope.keys.multiFolders: 'fa fa-plus-square' ;
					scope.multiFoldersOpen 	= scope.keys !== undefined && scope.keys.multiFoldersOpen !==undefined && scope.keys.multiFoldersOpen.length > 0 ? scope.keys.multiFoldersOpen: 'fa fa-minus-square' ;

					scope.createTreeStructure = function (folders){
						if (attrs.createTree !==undefined  && (attrs.createTree == true || attrs.createTree == "true")){
							if (folders !== undefined && folders.length > 0 && folders[0][subfoldersId] === undefined){
								var mapFolder = {};

								for (var i = 0 ; i < folders.length; i ++ ){
									folders[i][subfoldersId] = [];
									mapFolder[folders[i][elementId]] = folders[i];
								}

								var treeFolders = [];
								var personalFolders = {
										id: 1,
										codType: "LOW_FUNCT",
										code: "Personal_Folders",
										createRoles: [],
										description: "Personal Folders",
										name: scope.personalFolders,
										parentId: null,
										subfolders: [],
										path: "/Personal-Folders"};
								if(scope.personalFolders) treeFolders.push(personalFolders);
								for (var i = 0 ; i < folders.length; i ++ ){
									//if folder has not father, is a root folder
									if (folders[i][parentId] == null || folders[i][parentId] == "null"){
										if(scope.personalFolders && folders[i].code == 'Functionalities') treeFolders.push(folders[i]);
										else scope.personalFolders ? treeFolders[0].subfolders.push(folders[i]) : treeFolders.push(folders[i]);
									}
									else{
										//search parent folder with hashmap and attach the son
										var parent = mapFolder[folders[i][parentId]];
										if(parent){
											parent[subfoldersId].push(folders[i]);
											folders[i].$parent = parent;
											for (var j = 0; folders[i].biObjects !==undefined && j < folders[i].biObjects.length ; j++){
												folders[i].biObjects[j].$parent = parent;
											}
										}
									}
									//update linear structure with tree structure
								}
								folders=treeFolders;
							}
						}
						return folders;
					}

					scope.initializeFoldersAndCreateTreeStructure = function (folders, parent){
						var newFolders = [];
						angular.copy(folders, newFolders);
						scope.initializeFolders(newFolders, parent);
						return scope.createTreeStructure(newFolders);
					}


					scope.initializeFolders = function (folders, parent){

						if(folders){
							if(scope.translate == true || scope.translate == "true" || scope.translate == undefined){
								this.i18n.loadI18nMap().then(function() {
									scope.initializeFoldersRec(folders,parent);
								}); // end of load I 18n
							}
							else{
								scope.initializeFoldersRec(folders,parent);
							}


						}
					}


					scope.initializeFoldersRec = function (folders, parent){

						if(folders.length>0){
							var myScope = this;

							for (var i = 0 ; i < folders.length; i ++ ){

								if(scope.translate == true || scope.translate == "true" || scope.translate == undefined){
									folders[i].name = myScope.i18n.getI18n(folders[i].name);
								}

								folders[i].checked = folders[i].checked === undefined ? false : folders[i].checked;
								folders[i].expanded = folders[i].expanded === undefined ? false : (scope.multiSelectLeafes ? false : folders[i].expanded);
								folders[i].type = folders[i].type === undefined ? "folder" : folders[i].type;
								folders[i].visible = folders[i].visible === undefined ? true : folders[i].visible;
								folders[i].$parent = parent;

								if (folders[i][subfoldersId] !== undefined && folders[i][subfoldersId].length > 0){
									scope.initializeFoldersRec(folders[i][subfoldersId], folders[i]);
									if (attrs.orderBy){
										folders[i].sortDirection = folders[i].sortDirection === undefined ? "desc" : folders[i].sortDirection;
									}
								}
								for (var j = 0; folders[i].biObjects !==undefined && j < folders[i].biObjects.length ; j++){
									folders[i].biObjects[j].type = folders[i].biObjects[j].type == undefined ?  "biObject" : folders[i].biObjects[j].type;
									folders[i].biObjects[j].checked = folders[i].biObjects[j].checked == undefined ? false : folders[i].biObjects[j].checked;
									folders[i].biObjects[j].visible = folders[i].biObjects[j].visible == undefined ?  true : folders[i].biObjects[j].visible;
								}
								if (scope.orderBy) scope.sortFolders(folders);
							}

					
						}

					}
					
					scope.sortFolders = function(folders) {		
						var field = scope.orderBy;
						folders.sort(scope.orderFunction(field,"asc"));
						for (var i = 0 ; i < folders.length; i ++ ){		
							if (folders[i][subfoldersId] !== undefined && folders[i][subfoldersId].length > 0){

							folders[i][subfoldersId].sort(scope.orderFunction(field,"asc"));
					
							}	
						}
					}

					scope.orderFunction = function(key,direction){
						return function(a,b){
							var x = a[key]; var y = b[key];
							var val = ((x < y) ? -1 : ((x > y) ? 1 : 0));
							return direction =='asc' ? val : -val;
						};
					}
					
					scope.ngModel = scope.initializeFoldersAndCreateTreeStructure(scope.ngModel, null);
					scope.folders=scope.ngModel;

					var id="dcTree";
					if(attrs.id){
						id=attrs.id;
					}

					var treeElement = angular.element(element[0].querySelector('#tree-container'));

					var treeElement = angular.element(element[0].querySelector('#tree-container'));
					if (scope.enableClone == true){
						//treeElement.attr('data-clone-enabled','true');
					}
					if (scope.showEmptyPlaceholder == true){
						treeElement.attr('data-empty-placeholder-enabled','true');
					}
					if (scope.noDropEnabled == true){
						//treeElement.attr('data-nodrop-enabled','true');
					}
					if (scope.optionsDragDrop){
						//treeElement.attr('ui-tree',"optionsDragDrop");
					}
					if(attrs.multiSelect && (attrs.multiSelect == true || attrs.multiSelect == "true") ){
						if (!attrs.selecteditem) {
							scope.selectedItem = [];
						}
					}

					scope.seeTree = false;
				}
			}
		},
	}
});


function DocumentTreeControllerFunction($scope,$timeout,$mdDialog, sbiModule_i18n){

	$scope.i18n = sbiModule_i18n;

	$scope.toogleSelected = function(element, parent){

		if (element !== undefined && $scope.multiSelect){
			//check the element as the parent. If not the parent doesn't exist, toggle the element check
			element.checked = parent === undefined ? !element.checked : parent.checked;
			//different insertion if is allowed the multi-selection
			if ( element.checked ){ //if the element is just checked, insert into selectedItem, else remove it
				$scope.selectedItem.push(element);
			}else{
				var idx = $scope.selectedItem.indexOf(element);
				$scope.selectedItem.splice(idx, 1);
			}

			if (element.type == "folder" && !$scope.multiSelectLeafes){
				for (var i =0 ; i < element[$scope.subfoldersId].length; i++){
					$scope.toogleSelected(element[$scope.subfoldersId][i],element);
				}
				for (var j=0; element.biObjects !==undefined && j < element.biObjects.length ; j++ ){
					$scope.toogleSelected(element.biObjects[j],element);
				}
			}
		}
	}

	$scope.openFolder = function (folder,doClickAction){
		if (folder[$scope.subfoldersId] && folder[$scope.subfoldersId].length >= 15){
			// if many children show the loading message for n_child * 100 milliseconds
			$scope.seeTree = false;

			$timeout(function(){
				folder.expanded = !folder.expanded;
				if(doClickAction){
					$scope.setSelected(folder);
				}
			},100,true);

			$timeout(function(){
				$scope.seeTree = true;
			},folder[$scope.subfoldersId].length*100,true);
		}else{
			folder.expanded = !folder.expanded;
			if(doClickAction){
				$scope.setSelected(folder);
			}
		}
	}

	$scope.setSelected = function (element){
		if (!$scope.multiSelect){
			$scope.selectedItem=element;
		}
		//if present a click function, use it
		if (typeof $scope.clickFunction == "function"){
			$scope.clickFunction({item : element});
		}
	}

	$scope.$watchCollection(
			'ngModel'
			, function(){
				if(typeof $scope.ngModel != "undefined"
					&& $scope.ngModel != null
					&& $scope.ngModel.length != null
					&& $scope.ngModel.length > 0){
					$scope.seeTree = false;
					var newModel = $scope.initializeFoldersAndCreateTreeStructure($scope.ngModel, null);
					var replacer = function (key, value) {
						if (key == "subfolders" || key == "$$hashKey" || key == "$parent") {
							return undefined;
						}
						return value;
					};
					if(JSON.stringify($scope.ngModel,replacer) != JSON.stringify(newModel,replacer)){
						$scope.ngModel = newModel;
						$scope.folders= $scope.ngModel;
					}
					$timeout(function(){
						$scope.seeTree = true;
					});
				}
			});

	$scope.toogleSort = function(element){
		if(element.sortDirection && element[$scope.subfoldersId]){
			element.sortDirection = element.sortDirection == 'asc' ? 'desc' : 'asc';
			var field = $scope.orderBy;
			element[$scope.subfoldersId].sort($scope.orderFunction(field,element.sortDirection));
		}
	}



	$scope.orderFunction = function(key,direction){
		return function(a,b){
			var x = a[key]; var y = b[key];
			var val = ((x < y) ? -1 : ((x > y) ? 1 : 0));
			return direction =='asc' ? val : -val;
		};
	}

	//call each time that the orederBy value change
	$scope.$watch('orderBy', function (){
		if ($scope.orderBy !== undefined && $scope.orderBy.length > 0){
			var field = $scope.orderBy;
			if ($scope.selectedItem !== undefined){
				//take the parent
				$scope.toogleSort($scope.selectedItem.$parent);
			}else{
				$scope.toogleSort($scope.ngModel);
			}
		}
	});

	$scope.$watch('textSearch',
			function(){
		if ($scope.textSearch !== undefined && $scope.textSearch.length > 0){
			for (var i = 0; i < $scope.ngModel.length; i++){
				$scope.filterString($scope.ngModel[i]);
			}
		}
		if ($scope.textSearch !== undefined && $scope.textSearch.length == 0){
			for (var i = 0; i < $scope.ngModel.length; i++){
				$scope.resetVisible($scope.ngModel[i]);
			}
		};
	});


	$scope.resetVisible = function(element){
		element.visible = true;
		if (element[$scope.subfoldersId] !== undefined){
			for (var i =0 ;i < element[$scope.subfoldersId].length; i++){
				$scope.resetVisible(element[$scope.subfoldersId][i]);
			}
			for (var j=0;element.biObjects !==undefined && j < element.biObjects.length ; j++ ){
				$scope.resetVisible(element.biObjects[j]);
			}
		}
	}

	$scope.filterString = function (element) {
		var visible = true;
		if ($scope.textSearch && $scope.fieldsSearch){
			//if the filters is empty, visible = true, else start with visible = false
			visible = $scope.fieldsSearch.length == 0 || $scope.textSearch.length == 0;
			//search the text filter in each fields specify in filterBy object, until visible == false
			for (var i =0; visible == false && i < $scope.fieldsSearch.length;i++){
				visible =  element[$scope.fieldsSearch[i]].toUpperCase().indexOf($scope.textSearch.toUpperCase()) > -1;
			}

			if (element.type == "folder" && element[$scope.subfoldersId] !==undefined ){
				for (var i =0 ; i < element[$scope.subfoldersId].length; i++){
					if ($scope.filterString(element[$scope.subfoldersId][i]) == true ){
						visible = true;
					}
				}
				for (var j=0; element.biObjects !==undefined && j < element.biObjects.length ; j++ ){
					if ($scope.filterString(element.biObjects[j]) == true){
						visible = true;
					}
				}
			}
		}
		element.visible=visible;
		return visible;
	};

	$scope.detectBrowser = function(){
		var userAgent = window.navigator.userAgent;
		var browsers = {chrome: /chrome/i, safari: /safari/i, firefox: /firefox/i, ie: /internet explorer/i};

		for(var key in browsers) {
			if (browsers[key].test(userAgent)) {
				return key;
			}
		};
		return 'unknown';
	}

	$scope.checkSeeTree = function(){
		return $scope.seeTree;
	}

	$scope.browser = $scope.detectBrowser();

	if ($scope.browser == 'firefox'){
		$scope.classLayout='layout-padding';
	}else{
//		$scope.classLayout='layout-fill';
	}
}
