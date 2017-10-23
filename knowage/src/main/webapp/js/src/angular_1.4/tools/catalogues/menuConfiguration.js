/**
   @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net) 
 */

var app = angular.module("MenuConfigurationModule", ['angular-list-detail', 'ui.tree', 'sbiModule', 'angular_table' ]);
app.config([ '$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
} ]);

app.controller('MenuConfigurationController', [ "$scope","sbiModule_restServices", "sbiModule_translate", "$mdDialog", "sbiModule_messaging",
		MenuConfigurationFunction ]);
function MenuConfigurationFunction($scope, sbiModule_restServices,sbiModule_translate, $mdDialog,sbiModule_messaging) {
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

	$scope.parent = {};

	$scope.cancel = function() { // on cancel button
		$scope.selectedMenu ={};
		$scope.role = [];
		$scope.showme = false;
		$scope.dirtyForm=false;
		
	}
	$scope.fake = {};
	$scope.save = function() {
		if ($scope.selectedMenu.hasOwnProperty("menuId")) {
			// if item already exists do update PUT
			$scope.fake = {
				menuId : $scope.selectedMenu.menuId,
				descr : $scope.selectedMenu.descr,

				externalApplicationUrl : $scope.selectedMenu.externalApplicationUrl,

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
				roles : $scope.selectedMenu.roles,
				staticPage : $scope.selectedMenuItem.page,
				viewIcons : $scope.selectedMenu.viewIcons,
				adminsMenu : $scope.selectedMenu.adminsMenu

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

				hideSliders : $scope.selectedMenu.hideSliders,
				hideToolbar : $scope.selectedMenu.hideToolbar,
				level : $scope.selectedMenu.level,

				name : $scope.selectedMenu.name,
				objId : !$scope.selectedMenu.document ? null
						: $scope.selectedMenu.document.fromDocId,
				objParameters : $scope.selectedMenu.objParameters,
				parentId : $scope.parentID,

				roles : $scope.selectedMenu.roles,
				staticPage : $scope.selectedMenuItem.page,
				viewIcons : $scope.selectedMenu.viewIcons,
				adminsMenu : $scope.selectedMenu.adminsMenu

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
			"label" : "label",
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

		if (menuItem.externalApplicationUrl != null && menuItem.externalApplicationUrl != "") {
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
						+ itemsPerPage + "&label=" + searchValue;
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
					$scope.parent = $scope.selectedMenu;
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
	$scope.confirm = $mdDialog.confirm().title(
			sbiModule_translate.load("sbi.catalogues.generic.modify")).content(
			sbiModule_translate.load("sbi.catalogues.generic.modify.msg"))
			.ariaLabel('toast').ok(
					sbiModule_translate.load("sbi.general.continue")).cancel(
					sbiModule_translate.load("sbi.general.cancel"));



};

