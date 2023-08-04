/**
   @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net)
 */

var app = angular.module("MenuConfigurationModule", ['angular-list-detail', 'ui.tree', 'sbiModule', 'angular_table', 'knModule', 'ngMaterial', 'file_upload' ]);
app.config([ '$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
} ]);

app.controller('MenuConfigurationController', MenuConfigurationFunction);
function MenuConfigurationFunction($scope, sbiModule_config, sbiModule_restServices, sbiModule_translate, $mdDialog, sbiModule_messaging, knModule_fontIconsService) {

	// getting all menus
	$scope.getListOfMenu = function() {
		sbiModule_restServices.promiseGet("2.0/menu", "").then(
				function(response) {
					$scope.listOfMenu = angular.copy(response.data);

					$scope.listOfMenu_copy = angular.copy($scope.listOfMenu);
					$scope.listOfMenu_copy = $scope.generateTree($scope.listOfMenu_copy);
					$scope.listOfMenu_copy.unshift({
						name : "Menu tree",
						menuId : null
					});

				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	};



	$scope.generateTree = function( menus, parent) {
    		if (menus !== undefined && menus.length > 0 ){
	    		var mapmenu = {};

				for (var i = 0 ; i < menus.length; i ++ ){
					menus[i].lstChildren = [];
					mapmenu[menus[i].menuId]= menus[i];
				}

				var treemenus = [];
				for (var i = 0 ; i < menus.length; i ++ ){
					//if menu has not father, is a root menu
					if (menus[i].parentId == null || menus[i].parentId == "null"){
						treemenus.push(menus[i]);
					}
					else{
						//search parent menu with hashmap and attach the son
						var parent = mapmenu[menus[i].parentId];
						if(parent){
							parent.lstChildren.push(menus[i]);
							menus[i].$parent = parent;
							for (var j = 0; menus[i].lstChildren !==undefined && j < menus[i].lstChildren.length ; j++){
								menus[i].lstChildren[j].parentId= parent.menuId;
							}
						}
					}
					//update linear structure with tree structure
				}
				menus=treemenus;
    		}

		return menus;
	};
	// getting all roles
	$scope.getRoles = function() {
		sbiModule_restServices.promiseGet("2.0/roles/short", "").then(
				function(response) {
					$scope.roles = response.data;
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	};

	// getting all Folders
	$scope.getListOfFolders = function() {
		sbiModule_restServices.promiseGet("2.0/menu/functionalities","").then(
				function(response) {
					$scope.folders = response.data.functionality;
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.errors[0].message), 'Error');
				});
	};


	$scope.getHTMLFiles= function() {


		sbiModule_restServices.promiseGet("2.0/menu/htmls","").then(
				function(response) {
					$scope.files = response.data;

				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	};
	angular.element(document).ready(function() { // on page load function
		$scope.getListOfMenu();
		$scope.getListOfFolders();
		$scope.getRoles();
		$scope.getHTMLFiles();


	});

	$scope.showme = false;
	$scope.showPlusButton = false;
	$scope.translate = sbiModule_translate;
	$scope.listOfMenu = [];
	$scope.selectedMenu = {};
	$scope.roles = [];
	$scope.role = [];
	$scope.dirtyForm = false;
	$scope.files =[];
	$scope.folders =[];

	$scope.parent = {};
	$scope.selectedMenu.icon = {};
	$scope.selectedMenu.custIcon = null;
	$scope.importFile = {};
	$scope.customIcons = [];

	$scope.cancel = function() { // on cancel button
		$scope.selectedMenu ={};
		$scope.role = [];
		$scope.showme = false;
		$scope.dirtyForm=false;

	}
	
	$scope.existsANodeWithSameNameAtSameLevel = function() {
		if ($scope.selectedMenu.parentId) {
			var tmp = $scope.listOfMenu;
			for (var i = 0; i < tmp.length; i++){
				if (angular.equals(tmp[i].name,$scope.selectedMenu.name) 
						&& tmp[i].menuId != $scope.selectedMenu.menuId 
						&& tmp[i].parentId == $scope.selectedMenu.parentId) {
					return false;
				}
			}
		} else {
			for (var i = 0; i < $scope.listOfMenu_copy.length; i++) {
				if ($scope.listOfMenu_copy[i].level == 1 && angular.equals($scope.listOfMenu_copy[i].name,$scope.selectedMenu.name)
						&& $scope.listOfMenu_copy[i].menuId!=$scope.selectedMenu.menuId) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	$scope.fake = {};
	
	$scope.save = function() {
		if (!$scope.existsANodeWithSameNameAtSameLevel()) {
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.menu.brotherWithSameNameError"));
		} else 
		if ($scope.selectedMenu.hasOwnProperty("menuId")) {
			// if item already exists do update PUT
			
			$scope.fake = {
				menuId : $scope.selectedMenu.menuId,
				descr : $scope.selectedMenu.descr,

				externalApplicationUrl : $scope.selectedMenu.externalApplicationUrl,
				functionality : $scope.selectedMenu.functionality,
				initialPath : $scope.selectedMenu.initialPath,

				hasChildren : $scope.selectedMenu.hasChildren,
				hideSliders : $scope.selectedMenu.hideSliders,
				hideToolbar : $scope.selectedMenu.hideToolbar,
				level : $scope.selectedMenu.level,
				name : $scope.selectedMenu.name,
				objId : !$scope.selectedMenu.document ? null
						: $scope.selectedMenu.document.fromDocId,
				objParameters : $scope.selectedMenu.objParameters,
				parentId : $scope.selectedMenu.parentId,
				prog : $scope.selectedMenu.prog,
				roles : $scope.selectedMenu.roles || [],
				staticPage : $scope.selectedMenuItem.page,
				viewIcons : $scope.selectedMenu.viewIcons,
				adminsMenu : $scope.selectedMenu.adminsMenu,
				icon: $scope.selectedMenu.icon,
				custIcon: $scope.selectedMenu.custIcon
			}

			sbiModule_restServices.promisePut("2.0/menu", $scope.fake.menuId,
				$scope.fake).then(
					function(response) {
						$scope.listOfMenu_copy = $scope.getListOfMenu();
						sbiModule_messaging.showSuccessMessage(
								sbiModule_translate
										.load("sbi.catalogues.toast.updated"),
								'Success!');
						//$scope.selectedMenu = {};
						$scope.showme = true;
						$scope.dirtyForm = false;

					},
					function(response) {
						sbiModule_messaging.showErrorMessage(
								sbiModule_translate
										.load(response.data.errors[0].message),
								'Error');
					});

		} else {
			// post create new item
			$scope.fake = {
				descr : $scope.selectedMenu.descr,
				externalApplicationUrl : $scope.selectedMenu.externalApplicationUrl,
				functionality : $scope.selectedMenu.functionality,
				initialPath : $scope.selectedMenu.initialPath,

				hideSliders : $scope.selectedMenu.hideSliders,
				hideToolbar : $scope.selectedMenu.hideToolbar,

				level : $scope.selectedMenu.level,

				name : $scope.selectedMenu.name,
				objId : !$scope.selectedMenu.document ? null
						: $scope.selectedMenu.document.fromDocId,
				objParameters : $scope.selectedMenu.objParameters,
				parentId : $scope.parentID,

				roles : $scope.selectedMenu.roles || [],
				staticPage : $scope.selectedMenuItem.page,
				viewIcons : $scope.selectedMenu.viewIcons,
				adminsMenu : $scope.selectedMenu.adminsMenu,
				icon: $scope.selectedMenu.icon,
				custIcon: $scope.selectedMenu.custIcon

			}

			sbiModule_restServices.promisePost('2.0/menu', '', $scope.fake).
				then(function(response) {
					$scope.listOfMenu_copy = $scope.getListOfMenu();
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.
							load("sbi.catalogues.toast.created"),'Success!');
				//	$scope.selectedMenu = {};
					$scope.showme = true;
					$scope.dirtyForm = false;
					$scope.showSelectedMenu(response.data)
				}, function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.
							load(response.data.errors[0].message), 'Error');
				});
		}

	}

	$scope.setDirty = function() {
		$scope.dirtyForm = true;
	}


	$scope.nodeTemp = {};

	$scope.mouseenter = function(node) {
		$scope.nodeTemp = node;
	}

	$scope.mouseleave = function(node) {
		$scope.nodeTemp = null;
	}

	$scope.tooggle = function (scope){
		scope.toggle();
	}

	$scope.createMenu = function() {
		$scope.parent = angular.copy($scope.selectedMenu)
		$scope.parentID = $scope.selectedMenu.menuId;
		if ($scope.selectedMenu.menuId == null) {
			$scope.role = $scope.roles;
			$scope.selectedMenu.level = 0;
			$scope.parentID = null;
		}

		$scope.selectedMenu.descr = "";
		$scope.selectedMenu.name = "";

		delete $scope.selectedMenu.menuId;
		delete $scope.selectedMenu.prog;
		$scope.selectedMenu.hideToolbar = false;
		$scope.selectedMenu.hideSliders = false;
		$scope.selectedMenuItem = null;
		$scope.selectedMenu.externalApplicationUrl = "";
		$scope.selectedMenu.functionality = "";
		$scope.selectedMenu.initialPath = "";
		$scope.selectedMenu.viewIcons = false;
		$scope.selectedMenu.document = null;
		$scope.selectedMenu.objParameters = "";
		$scope.selectedMenu.staticPage = null;
		$scope.selectedMenu.parentId = $scope.parentID;
		$scope.showme = true;


	}
	$scope.columnsArray = [
		{

			"label" : sbiModule_translate.load("sbi.menu.columns.roles"),
			"name" : "name",
			"size" : "50px",
			hideTooltip : true
		},
	    {
			"label" : " ",
			"name" : "",
			"size" : "50px",
			hideTooltip : true,
			transformer : function() {
				return " <md-checkbox  ng-checked=scopeFunctions.isChecked(row)  ng-click=scopeFunctions.checkRole(row)   ng-disabled=!scopeFunctions.isDisabled(row)  aria-label='' ></md-checkbox>";
			}
	    }
	];

	$scope.allTypes = [ {
		name : "",
		id : 0,
		label : sbiModule_translate.load("sbi.menu.nodeEmpty")

	}, {
		name : "page",
		id : 3,
		label : sbiModule_translate.load("sbi.menu.nodeStaticPage")
	}, {
		name : "doc",
		id : 1,
		label : sbiModule_translate.load("sbi.menu.nodeDocument")
	}, {
		name : "app",
		id : 2,
		label : sbiModule_translate.load("sbi.menu.nodeExternalApp")
	}, {
		name : "intLink",
		id : 4,
		label : sbiModule_translate.load("sbi.menu.functionality")
	}
	];

	$scope.allFunctionalities = [ {
		code : "DocumentUserBrowser",
		name : sbiModule_translate.load("sbi.menu.linkDocumentBrowser")

	}, {
		code : "WorkspaceManagement",
		name : sbiModule_translate.load("sbi.menu.linkWorkspace")
	}
	];

	$scope.allWorkspacePaths = [
		{
			code : "recent",
			name : sbiModule_translate.load("sbi.menu.ws.recent")
		},
		{
			code : "documents",
			name : sbiModule_translate.load("sbi.menu.ws.documents")
		},
		{
			code : "datasets",
			name : sbiModule_translate.load("sbi.menu.ws.datasets")
		},
		{
			code : "models",
			name : sbiModule_translate.load("sbi.menu.ws.models")
		},
		{
			code : "analysis",
			name : sbiModule_translate.load("sbi.menu.ws.analysis")
		},
		{
			code : "schedulation",
			name : sbiModule_translate.load("sbi.menu.ws.schedulation")
		}
		];

	$scope.selectedMenuItem = {};

	$scope.tableFunction = {
			isChecked : function(row) {

				if ($scope.selectedMenu.roles != undefined) {
					for (var j = 0; j < $scope.selectedMenu.roles.length; j++) {
						if ($scope.selectedMenu.roles[j].name == row.name) {
							return true;
						}

					}
				}

			},

			/*
				method that disable choosing role that parent does not have
			*/
			isDisabled : function(row) {
				if($scope.parent){
					if($scope.parent.parentId!=null || $scope.parent.roles){
						if ($scope.parent.roles != undefined ) {
							for (var j = 0; j < $scope.parent.roles.length; j++) {
								if ($scope.parent.roles[j].name == row.name) {
									return true;
								}

							}
						}
					}
					else return true;
				}
				else return true;

			},
			checkRole : function(item) {
				if ($scope.selectedMenu.roles == null) {
					$scope.selectedMenu.roles = [];
				}

				var index = $scope.indexInList(item,
						$scope.selectedMenu.roles);

				if (index != -1) {
					$scope.selectedMenu.roles.splice(index, 1);
				} else {
					$scope.selectedMenu.roles.push(item);
				}

			}

		};
	$scope.indexInList = function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if (object.name == item.name) {
				return i;
			}
		}

		return -1;
	}
	$scope.getParent = function (id){
		sbiModule_restServices.promiseGet("2.0/menu/getParent", id).then(
				function(response) {
					$scope.parent = angular.copy(response.data);
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}
	$scope.checkHtml = function(menuItem) {
		if (menuItem.staticPage) {

			for (i = 0; i < $scope.files.length; i++) {
				if ($scope.files[i].name.toLowerCase() === menuItem.staticPage.toLowerCase()) {
					$scope.selectedMenuItem.page = $scope.files[i].name;
					break;
				}
			}

		}

	}
	$scope.checkPropertiesFromSelectedMenu = function(menuItem) {

		if (menuItem.functionality != null && menuItem.functionality != "") {
			for (i = 0; i < $scope.allTypes.length; i++) {
				if ($scope.allTypes[i].name.toLowerCase() === "intlink") {
					$scope.selectedMenuItem.typeId = $scope.allTypes[i].id;
					break;
				}
			}
		} else if (menuItem.externalApplicationUrl != null && menuItem.externalApplicationUrl != "") {
			for (i = 0; i < $scope.allTypes.length; i++) {
				if ($scope.allTypes[i].name.toLowerCase() === "app") {
					$scope.selectedMenuItem.typeId = $scope.allTypes[i].id;
					break;
				}
			}
		} else if (menuItem.staticPage) {

			for (i = 0; i < $scope.allTypes.length; i++) {
				if ($scope.allTypes[i].name.toLowerCase() === "page") {
					$scope.selectedMenuItem.typeId = $scope.allTypes[i].id;
					break;
				}
			}

		} else if (menuItem.objId) {

			for (i = 0; i < $scope.allTypes.length; i++) {
				if ($scope.allTypes[i].name.toLowerCase() === "doc") {
					$scope.selectedMenuItem.typeId = $scope.allTypes[i].id;
					break;
				}
			}

		} else {
			for (i = 0; i < $scope.allTypes.length; i++) {
				if ($scope.allTypes[i].name.toLowerCase() === "") {
					$scope.selectedMenuItem.typeId = $scope.allTypes[i].id;
					break;
				}
			}

		}

	}
	$scope.setPropertiesForCombo = function() {
 		$scope.selectedMenuItem.page = null;
		$scope.selectedMenu.document = null;
		$scope.selectedMenu.objParameters = null;
		$scope.selectedMenu.hideToolbar = false;
		$scope.selectedMenu.hideSliders = false;
		$scope.selectedMenu.functionality = null;
		$scope.selectedMenu.initialPath = null;
		$scope.selectedMenu.externalApplicationUrl = null;

	}


	$scope.setRoles = function() {
		$scope.role = [];
		for (var i = 0; i < $scope.roles.length; i++) {
			for (var j = 0; j < $scope.selectedMenu.roles.length; j++) {
				if ($scope.selectedMenu.roles[j].id == $scope.roles[i].id) {
					$scope.role.push($scope.roles[i]);
				}
			}
		}


	}
	$scope.formatMenu = function() {
		var tmpR = [];
		var tmpA = {};
		for (var i = 0; i < $scope.role.length; i++) {
			tmpR.push($scope.role[i]);
		}
		$scope.selectedMenu.roles = tmpR;

	}

	$scope.listAllDocuments = function() {
		$scope.listDocuments(function(item, listId, closeDialog) {
			if (!$scope.selectedMenu.document)
				$scope.selectedMenu.document = {};
			$scope.selectedMenu.document.fromDocId = item.DOCUMENT_ID;
			$scope.selectedMenu.document.fromDoc = item.DOCUMENT_NAME;

			loadInputParameters(item.DOCUMENT_LABEL, function(data) {
				$scope.selectedMenu.fromPars = data;
				loadOutputParameters(item.DOCUMENT_ID, function(data) {
					$scope.selectedMenu.fromPars = $scope.selectedMenu.fromPars
							.concat(data);
					closeDialog();
				});
			});
		});
	};

	function loadOutputParameters(documentId, callbackFunction) {
		sbiModule_restServices.promiseGet(
				'2.0/documents/' + documentId + '/listOutParams', "", null)
				.then(function(response) {
					var data = response.data;
					var parameters = [];
					for (var i = 0; i < data.length; i++) {
						parameters.push({
							'id' : data[i].id,
							'name' : data[i].name,
							'type' : 0
						});
					}
					callbackFunction(parameters);
				}, function(response) {
				});

	}

	$scope.listDocuments = function(clickOnSelectedDocFunction, item) {
		$mdDialog.show({
			controller : DialogController,
			templateUrl : 'dialog1.tmpl.html',
			parent : angular.element(document.body),
			clickOutsideToClose : false,
			locals : {
				clickOnSelectedDoc : clickOnSelectedDocFunction,
				translate : sbiModule_translate
			}
		});

		function DialogController($scope, $mdDialog, clickOnSelectedDoc,
				translate) {
			$scope.closeDialog = function() {
				$mdDialog.hide();
			};
			$scope.changeDocPage = function(searchValue, itemsPerPage,
					currentPageNumber, columnsSearch, columnOrdering,
					reverseOrdering) {
				if (searchValue == undefined || searchValue.trim().lenght == 0) {
					searchValue = '';
				}
				var item = "Page=" + currentPageNumber + "&ItemPerPage="
						+ itemsPerPage + "&label=" + searchValue+ "&name=" + searchValue;
				$scope.loadListDocuments(item);
			};
			$scope.clickOnSelectedDoc = clickOnSelectedDoc;
			$scope.translate = translate;
			$scope.loading = true;
			$scope.totalCount = 0
			$scope.loadListDocuments = function(item) {
				sbiModule_restServices.promiseGet("2.0/documents",
						"listDocument", item).then(function(response) {
					$scope.loading = false;
					$scope.listDoc = response.data.item;
					$scope.totalCount = response.data.itemCount;
				},

				function(response) {
					sbiModule_restServices.errorHandler(response.data, "")
				})
			}

		}
	}
	function loadInputParameters(documentLabel, callbackFunction) {
		sbiModule_restServices.promiseGet(
				'2.0/documents/' + documentLabel + '/parameters', "", null)
				.then(function(response) {
					var data = response.data;
					var parameters = [];
					for (var i = 0; i < data.length; i++) {
						parameters.push({
							'id' : data[i].id,
							'name' : data[i].label,
							'type' : 1
						});
					}
					callbackFunction(parameters);
				}, function(response) {
				});
	}
	$scope.openDocuments = function() {

	}
	$scope.styleO = "";
	$scope.showSelectedMenu = function(item) {
		$scope.nodeTempT = item;
		if ($scope.selectedMenu.menuId != item.menuId) {
			$scope.selectedMenu = {};
			$scope.selectedMenuItem = {
				page : ""
			};
			if ($scope.dirtyForm) {
				$mdDialog.show($scope.confirm).then(function() {
					$scope.selectedMenu = angular.copy(item);
					if(item.parentId!=null  ){
						$scope.getParent(item.parentId);
					}
					$scope.checkPropertiesFromSelectedMenu($scope.selectedMenu);
					if ($scope.selectedMenu.staticPage) {

						 $scope.checkHtml($scope.selectedMenu);
					}
					if ($scope.selectedMenu.objId) {
						$scope.getDocName(item.objId);
					}
					$scope.showme = true;
					$scope.dirtyForm = false;
				}, function() {
					$scope.showme = true;
				});

			} else {
				angular.copy(item).parentId !== null
				$scope.selectedMenu = angular.copy(item);
				if(item.parentId!=null  ){
					$scope.getParent(item.parentId);
				}
				else {
					//$scope.parent = $scope.selectedMenu;
					$scope.parent = null;
				}
				if ($scope.selectedMenu.menuId == null || item.menuId==null) {
					$scope.showme = false;
					$scope.dirtyForm = false;
				} else {
					$scope.checkPropertiesFromSelectedMenu($scope.selectedMenu);
					if ($scope.selectedMenu.staticPage) {
						 $scope.checkHtml($scope.selectedMenu);
					}
					if ($scope.selectedMenu.objId) {
						$scope.getDocName(item.objId);
					}
					$scope.showme = true;
				}
			}
		}


	}
	var map = {};
	$scope.canBeMovedDown = function(item) {

		Array.prototype.max = function() {
			return Math.max.apply(null, this);
		};

		Array.prototype.min = function() {
			return Math.min.apply(null, this);
		};

		if (item.lstChildren != undefined) {

			if (item.parentId == null) {

				if (!map.hasOwnProperty("null")) {
					map["null"] = new Array();
				} else {
					if (map["null"].indexOf(item.prog) < 0) {
						map["null"].push(item.prog);
					}
				}
			}
			if (item.lstChildren.length > 0) {
				for (j = 0; j < item.lstChildren.length; j++) {
					if (!map.hasOwnProperty(item.lstChildren[j].parentId + "")) {
						map[item.lstChildren[j].parentId + ""] = new Array();
					} else {
						if (map[item.lstChildren[j].parentId + ""]
								.indexOf(item.lstChildren[j].prog) < 0) {
							map[item.lstChildren[j].parentId + ""]
									.push(item.lstChildren[j].prog);
						}
					}
				}
			}
			if(map[item.parentId+""] && map[item.parentId+""].length>1 && map[item.parentId+""].max() > item.prog) {
				return true;
			}
			else {
				return false;
			}
		}

	}
	$scope.canBeMovedUp = function(item) {
		//!(node.prog== 1) && node.menuId!=null
		/*var father = {};
		if(item.parentId== null){
			father = item;
		} else {
			father = getFather(item.parentId) ;
		}
		if( item.menuId!=null) {
			if(father.lstChildren.length>1){
				if(item.prog!=1){
					return true
				} else return false
			} else return false
		} else return false;*/
		if(item.menuId!=null){
			if(item.prog!=1 ) {
				return true
			} else return false;
		} else return false;
	}

	$scope.canBeChangedWithFather = function (item) {
		if(item.roles ){
			var father = {};
			if(item.parentId== null){
				father = item;
			} else {
				father = getFather(item.parentId) ;
			}
			//$scope.getParent(item.menuId);
			if(item.roles.length==father.roles.length && item.parentId!=null){
				return true
			}
			else return false
		}
		else return false;


	}
	var getFather = function (parentId){
		for (var i = 0; i < $scope.listOfMenu.length; i++) {
			if($scope.listOfMenu[i].menuId==parentId)
				return $scope.listOfMenu[i]
		}

	}
	//(node) node.parentId!=null && node.menuId!=null
	$scope.moveUp = function (item){
		sbiModule_restServices.promiseGet("2.0/menu/moveUp", item.menuId).then(
				function(response) {
					$scope.getListOfMenu();
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}
	$scope.moveDown = function (item){
		sbiModule_restServices.promiseGet("2.0/menu/moveDown", item.menuId).then(
				function(response) {
					$scope.getListOfMenu();
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}
	$scope.changeWithFather = function (item){
		sbiModule_restServices.promiseGet("2.0/menu/changeWithFather", item.menuId).then(
				function(response) {
					$scope.getListOfMenu();
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}
	$scope.getDocName = function(objId) {
		sbiModule_restServices.promiseGet("2.0/documents", objId).then(
				function(response) {
					$scope.selectedMenu.document = {
						fromDoc : null
					};
					$scope.selectedMenu.document.fromDoc = response.data.name;
					$scope.selectedMenu.document.fromDocId = response.data.id

				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate
							.load(response.data.errors[0].message), 'Error');
				});
	}

	$scope.deleteMenu= function (item,$event){
		var confirm = $mdDialog.confirm()
		.title(sbiModule_translate.load("sbi.catalogues.toast.confirm.title"))
		.content(sbiModule_translate.load("sbi.catalogues.toast.confirm.content"))
		.ariaLabel("confirm_delete")
		.targetEvent($event)
		.ok(sbiModule_translate.load("sbi.general.continue"))
		.cancel(sbiModule_translate.load("sbi.general.cancel"));
	$mdDialog.show(confirm).then(function() {
		$scope.deleteMenuItem(item.menuId);
    }, function() {

    });


	}
	$scope.deleteMenuItem= function (id){
		sbiModule_restServices.promiseDelete("2.0/menu", id).then(
				function(response) {
					$scope.getListOfMenu();
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
					$scope.selectedMenu = {};
					$scope.showme=false;
					$scope.dirtyForm=false;
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}



	$scope.toggleDocFunct = function(doc, funct) {
		if(funct != undefined) {
			if(doc.initialPath == funct) { //unselect}
				doc.initialPath = null;
			}
			else{
				doc.initialPath = funct;
			}
		}
	};

	$scope.isChecked = function (item, funct, condition) {
		if(condition) {
			if(funct != undefined){
				return item == funct;
			}
			else{
				return false;
			}
		} else {
			return false;
		}
	};



	$scope.confirm = $mdDialog.confirm().title(
			sbiModule_translate.load("sbi.catalogues.generic.modify")).content(
			sbiModule_translate.load("sbi.catalogues.generic.modify.msg"))
			.ariaLabel('toast').ok(
					sbiModule_translate.load("sbi.general.continue")).cancel(
					sbiModule_translate.load("sbi.general.cancel"));
	
	$scope.chooseMenuIcon = function (e) {
		e.preventDefault();
		e.stopImmediatePropagation();
			
		$mdDialog.show({ 
			templateUrl:  
				sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/menu/templates/menuTableWidgetAddIconDialog.html',
			parent : angular.element(document.body),
			clickOutsideToClose:false,
			escapeToClose :false,
			preserveScope: false,
			autoWrap:false,
			fullscreen: true,
			controller: function (scope, $mdDialog, knModule_fontIconsService, sbiModule_translate) {
				scope.translate = sbiModule_translate;
				scope.availableIcons = [];
				angular.copy(knModule_fontIconsService.icons, scope.availableIcons);

				scope.searchVal = "";
				
				scope.setIcon = function(family,icon) {
					scope.selectedIcon = icon;
				}
				
				scope.chooseIcon = function(){					
					$mdDialog.hide(scope.selectedIcon);
				}
				
				scope.cancel = function(){
					$mdDialog.cancel();
				}
				
				scope.removeIcon = function(){			
					scope.selectedIcon = null;
					$mdDialog.hide(scope.selectedIcon);
				}
				
				scope.insertMenu = function(e){

					var file = e.files[0];
					if (file) {
						
						var validType = false;
						
						switch(file.type) {
							case "image/svg+xml":
							case "image/png":
							case "image/x-icon":
								validType = true;
								break;
							}
						
						if (!validType) {
						
							sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.menu.fileTypeIncompatible"));
							scope.selectedIcon = null;
							
						} else if(file.size > 50000) {
							
							sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.menu.fileSizeIncompatible"));
							scope.selectedIcon = null;
							
						} else {
							var reader = new FileReader();
					
							reader.onload = function (e) {
							   var b64 = e.target.result;
							   scope.selectedIcon = {
									   label: file.name, 
									   src: b64, 
									   visible: true, 
									   className: "custom", 
									   category :"custom", 
									   id:null, 
									   unicode: null
							   		};
							   scope.$apply();
							};
							
							reader.readAsDataURL(file);
							
						}
					}
				}				
			}
			
		}).then(function(icon) {
			$scope.setDirty();
			if (icon) {
				if (icon.category == "custom") {
					$scope.selectedMenu.custIcon = icon;
					$scope.selectedMenu.icon = null;
				} else {
					$scope.selectedMenu.icon = icon;
					$scope.selectedMenu.custIcon = null;
				}
			}
		}, function() {});
	};
	
	$scope.removeIcon = function(){			
		$scope.selectedMenu.icon = null;
		$scope.selectedMenu.custIcon = null;
	}

};

