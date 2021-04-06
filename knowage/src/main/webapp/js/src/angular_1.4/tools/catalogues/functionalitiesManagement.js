/**
   @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net)
 */

var app = angular.module("FunctionalitiesManagementModule", ['angular-list-detail', 'document_tree', 'sbiModule', 'angular_table' ]);
app.config([ '$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
} ]);
app.filter('i18n', function(sbiModule_i18n) {
    return function(label) {
        return sbiModule_i18n.getI18n(label);
    }
});
app.controller('FunctionalitiesManagementController', [ "$scope","sbiModule_restServices", "sbiModule_translate", "$mdDialog", "sbiModule_messaging", "sbiModule_i18n",
		FunctionalitiesManagementFunction ]);
function FunctionalitiesManagementFunction($scope, sbiModule_restServices,sbiModule_translate, $mdDialog,sbiModule_messaging, sbiModule_i18n) {
	sbiModule_i18n.loadI18nMap();
	// getting all functionalities
	$scope.getFolders = function() {
		sbiModule_restServices.promiseGet("2.0/functionalities", "").then(
				function(response) {
					for(var i=0; i<response.data.length; i++){
						response.data[i].expanded=true;

					}
					$scope.folders = angular.copy(response.data);
					$scope.folders_copy = angular.copy($scope.folders);
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
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

	angular.element(document).ready(function() { // on page load function
		$scope.getFolders();
		$scope.getRoles();

	});

	$scope.showme = false;
	$scope.showPlusButton = false;
	$scope.translate = sbiModule_translate;
	$scope.folders = [];
	$scope.selectedFolder = {};
	$scope.roles = [];
	$scope.dirtyForm = false;
	$scope.parent = {};
	$scope.cancel = function() { // on cancel button
		$scope.selectedFolder ={};
		$scope.showme = false;
		$scope.dirtyForm=false;

	}
	$scope.fake = {};



	$scope.save = function() {
	     if($scope.selectedFolder.hasOwnProperty("id")){
	    	 // if item already exists do update PUT
	    	 $scope.index = $scope.selectedFolder.path.lastIndexOf("/");
	    	 $scope.selectedFolder.path = $scope.selectedFolder.path.slice(0, $scope.index);
	    	 $scope.fake = {
	    			 id : $scope.selectedFolder.id,
	    			 codeType : $scope.selectedFolder.codType,
	    			 code : $scope.selectedFolder.code,
	    			 createRoles : $scope.selectedFolder.createRoles,
	    			 testRoles : $scope.selectedFolder.testRoles ,
	    			 execRoles : $scope.selectedFolder.execRoles,
	    			 devRoles :  $scope.selectedFolder.devRoles,
	    			 description :  $scope.selectedFolder.description,
	    			 name :  $scope.selectedFolder.name,
	    			 path :  $scope.selectedFolder.path+"/"+$scope.selectedFolder.name,
	    			 prog :  $scope.selectedFolder.prog,
	    			 parentId : $scope.selectedFolder.parentId

	    	 }
	    		sbiModule_restServices.promisePut("2.0/functionalities",$scope.fake.id, $scope.fake)
				.then(function(response) {
					$scope.folders_copy = $scope.getFolders();
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
					//$scope.selectedFolder ={};
					$scope.showme = true;
					$scope.dirtyForm=false;

				}, function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});

		}else{
		// post create new item
			 $scope.fake = {
	    			 codeType : $scope.selectedFolder.codType,
	    			 code : $scope.selectedFolder.code,
	    			 createRoles : $scope.selectedFolder.createRoles,
	    			 testRoles : $scope.selectedFolder.testRoles ,
	    			 execRoles : $scope.selectedFolder.execRoles,
	    			 devRoles :  $scope.selectedFolder.devRoles,
	    			 description :  $scope.selectedFolder.description,
	    			 name :  $scope.selectedFolder.name,
	    			 path :  $scope.path+"/"+$scope.selectedFolder.name,
	    			 parentId : $scope.selectedFolder.parentId

	    	 }
			sbiModule_restServices.promisePost('2.0/functionalities','',$scope.fake)
			.then(function(response) {
				$scope.folders_copy = $scope.getFolders();
				//$scope.selectedFolder ={};
				$scope.showme = true;
				$scope.dirtyForm=false;
				$scope.loadFolder (response.data);
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');

			}, function(response) {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');

			});
		}

	}

	$scope.setDirty = function() {
		$scope.dirtyForm = true;
	}

	$scope.parentID = null;
	$scope.codType = null;
	$scope.path = null;

	$scope.createFolder = function() {
		$scope.parentID = $scope.selectedFolder.id;
		$scope.codType = $scope.selectedFolder.codType;
		$scope.path = $scope.selectedFolder.path;



		$scope.selectedFolder.code = "";
		$scope.selectedFolder.description= "";
		$scope.selectedFolder.name= "";

		delete $scope.selectedFolder.id ;

		delete $scope.selectedFolder.prog ;

		$scope.selectedFolder.parentId= $scope.parentID;
		$scope.selectedFolder.codType=$scope.codType ;
		$scope.showme = true;
	}
	$scope.columnsArray = [
			{
				"label" : sbiModule_translate.load("sbi.folder.columns.roles"),
				"name" : "name",
				"size" : "50px",
				hideTooltip : false
			},
			{
				"label" : sbiModule_translate
						.load("sbi.folder.columns.development"),
				"name " : "",
				"size" : "50px",
				hideTooltip : true,
				transformer : function() {
					return " <md-checkbox  ng-checked=scopeFunctions.isChecked(row,'devRoles')    ng-click=scopeFunctions.checkRole(row,'devRoles')  ng-disabled=!scopeFunctions.isDisabled(row,'devRoles')   aria-label='' ></md-checkbox>";
				}
			},
			{
				"label" : sbiModule_translate.load("sbi.folder.columns.test"),
				"name" : "",
				"size" : "50px",
				hideTooltip : true,
				transformer : function() {
					return " <md-checkbox  ng-checked=scopeFunctions.isChecked(row,'testRoles')  ng-click=scopeFunctions.checkRole(row,'testRoles')  ng-disabled=!scopeFunctions.isDisabled(row,'testRoles')   aria-label='' ></md-checkbox>";
				}
			},
			{
				"label" : sbiModule_translate
						.load("sbi.folder.columns.execution"),
				"name" : "",
				"size" : "50px",
				hideTooltip : true,
				transformer : function() {
					return " <md-checkbox  ng-checked=scopeFunctions.isChecked(row,'execRoles')  ng-click=scopeFunctions.checkRole(row,'execRoles')  ng-disabled=!scopeFunctions.isDisabled(row,'execRoles')  aria-label='' ></md-checkbox>";
				}
			},
			{
				"label" : sbiModule_translate
						.load("sbi.folder.columns.creation"),
				"name" : "",
				"size" : "50px",
				hideTooltip : true,
				transformer : function() {
					return " <md-checkbox ng-checked=scopeFunctions.isChecked(row,'createRoles')  ng-click=scopeFunctions.checkRole(row,'createRoles')  ng-disabled=!scopeFunctions.isDisabled(row,'createRoles')  aria-label=''></md-checkbox>";
				}
			}

	];


	$scope.functMenuOpt = [
		{
			label : sbiModule_translate.load("sbi.folder.moveUp"),
			icon : 'fa fa-arrow-up',
			action : function(item) {
				$scope.moveUp(item.id)

			},
			showItem : function (item){
				return !(item.prog== 1);
			}
		},
		{
			label : sbiModule_translate.load("sbi.folder.moveDown"),
			icon : 'fa fa-arrow-down',
			action : function(item) {
				$scope.moveDown(item.id)
			},
			showItem : function (item){
				return $scope.canBeMovedDown(item);
			}
		} ,
		{
			label :  sbiModule_translate.load("sbi.folder.delete"),
			icon : 'fa fa-trash',
			action : function(item, parent, event) {
				$scope.confirmDelete(item,event);
			},
			showItem : function (item){

				return $scope.canBeDeleted(item);
			}
		}
	];
	$scope.tableFunction = {
		isChecked : function(row, criteria) {

			if ($scope.selectedFolder[criteria] != undefined) {
				for (var j = 0; j < $scope.selectedFolder[criteria].length; j++) {
					if ($scope.selectedFolder[criteria][j].name == row.name) {
						return true;
					}

				}
			}

		},

		/*
			method that disable choosing role that parent does not have
		*/
		isDisabled : function(row, criteria) {

			if($scope.parent!=undefined ){
				if($scope.parent.parentId){
					if ($scope.parent[criteria] != undefined ) {
						for (var j = 0; j < $scope.parent[criteria].length; j++) {
							if ($scope.parent[criteria][j].name == row.name) {
								return true;
							}

						}
					}
				}
				else return true;

			}

		},
		checkRole : function(item, criteria) {
			if ($scope.selectedFolder[criteria] == null) {
				$scope.selectedFolder[criteria] = [];
			}

			var index = $scope.indexInList(item,
					$scope.selectedFolder[criteria]);

			if (index != -1) {
				$scope.selectedFolder[criteria].splice(index, 1);
			} else {
				$scope.selectedFolder[criteria].push(item);
			}

		}

	};
	$scope.adSpeedMenu = [ {
		label :  sbiModule_translate.load("sbi.folder.tooltip.selectAll"),
		icon : 'fa fa-check',
		color : '#153E7E',
		action : function(item) {

			$scope.checkAllRolesInRow(item);
		}
	},{

		label :  sbiModule_translate.load("sbi.folder.tooltip.deSelectAll"),
		icon : 'fa fa-times',
		color : '#153E7E',
		action : function(item) {
			$scope.unCheckAllRolesInRow(item)

		}
	} ];

	 $scope.isCheckable = function(row, criteria) {
		var checkable = false;
		if($scope.path == '/Functionalities') {
			checkable = true;
		} else if($scope.parent[criteria] && $scope.parent[criteria].length > 0) {
			for(var i = 0; i < $scope.parent[criteria].length; i++) {
				if($scope.parent[criteria][i].name == row.name){
					checkable = true;
				}
			}
		}
	 	return checkable;
	 }

	$scope.checkRoleForCriteria = function(row, criteria) {
		var checkable = false;
		checkable = $scope.isCheckable(row, criteria);
		if ($scope.selectedFolder[criteria].length == 0 && checkable) {
			$scope.selectedFolder[criteria].push(row);
		} else {
			for (var j = 0; j < $scope.selectedFolder[criteria].length; j++) {
				if ($scope.selectedFolder[criteria][j].name != row.name && $scope.selectedFolder[criteria].indexOf(row) == -1 && checkable) {
					$scope.selectedFolder[criteria].push(row);
				}
			}
		}
		$scope.selectedFolder[criteria] = $scope.remove($scope.selectedFolder[criteria],"id");
	}

	$scope.checkAllRolesInRow = function(row) {
		var criterias = ['createRoles', 'execRoles', 'devRoles', 'testRoles'];
		for(var i = 0; i < criterias.length; i++) {
			$scope.checkRoleForCriteria(row, criterias[i]);
		}
	}

	$scope.remove = function (arr, prop){
		var new_arr = [];
	    var lookup  = {};

	    for (var i in arr) {
	        lookup[arr[i][prop]] = arr[i];
	    }

	    for (i in lookup) {
	        new_arr.push(lookup[i]);
	    }

	    return new_arr;
	}
	$scope.unCheckAllRolesInRow = function(row) {
		for (var j = 0; j < $scope.selectedFolder.createRoles.length ; j++) {
			if ($scope.selectedFolder.createRoles[j].name == row.name) {
				$scope.selectedFolder.createRoles.splice(j, 1);
				break;
			}
		}
		for (var m = 0; m < $scope.selectedFolder.execRoles.length ; m++) {
			if ($scope.selectedFolder.execRoles[m].name == row.name) {
				$scope.selectedFolder.execRoles.splice(m, 1);
				break;
			}
		}
		for (var n = 0; n < $scope.selectedFolder.devRoles.length ; n++) {
			if ($scope.selectedFolder.devRoles[n].name == row.name) {
				$scope.selectedFolder.devRoles.splice(n, 1);
				break;
			}
		}
		for (var s = 0; s < $scope.selectedFolder.testRoles.length; s++) {
			if ($scope.selectedFolder.testRoles[s].name == row.name) {
				$scope.selectedFolder.testRoles.splice(s, 1);
				break;
			}
		}

	}
	$scope.deleteFunct = function (id){
		sbiModule_restServices.promiseDelete("2.0/functionalities", id).then(
				function(response) {
					$scope.getFolders();
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
					$scope.selectedFolder = {};
					$scope.showme=false;
					$scope.dirtyForm=false;	;
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}
	$scope.moveUp = function (id){
		sbiModule_restServices.promiseGet("2.0/functionalities/moveUp", id).then(
				function(response) {
					$scope.getFolders();
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}
	$scope.moveDown = function (id){
		sbiModule_restServices.promiseGet("2.0/functionalities/moveDown", id).then(
				function(response) {
					$scope.getFolders();
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}

	$scope.getParent = function (id){
		sbiModule_restServices.promiseGet("2.0/functionalities/getParent", id).then(
				function(response) {
					$scope.parent = angular.copy(response.data);
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}

	$scope.canBeMovedDown = function (item){
		if($scope.folders_copy!= undefined){
			for (var i=0; i < $scope.folders_copy.length-1; i++){
				var f = $scope.folders_copy[i+1];
				if(item.parentId==f.parentId && item.prog < f.prog){
					return true;
				}


			}
			return false;
		}

	}

	$scope.canBeDeleted = function (item){
		if(item.parentId==null && item.codType=="LOW_FUNCT") return false
		else return true;
	}

	$scope.indexInList = function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if (object.name == item.name) {
				return i;
			}
		}

		return -1;
	}


	$scope.confirm = $mdDialog.confirm().title(
			sbiModule_translate.load("sbi.catalogues.generic.modify")).content(
			sbiModule_translate.load("sbi.catalogues.generic.modify.msg"))
			.ariaLabel('toast').ok(
					sbiModule_translate.load("sbi.general.continue")).cancel(
					sbiModule_translate.load("sbi.general.cancel"));


	$scope.confirmDelete = function(item,ev) {
		var confirm = $mdDialog.confirm()
			.title(sbiModule_translate.load("sbi.catalogues.toast.confirm.title"))
			.content(sbiModule_translate.load("sbi.catalogues.toast.confirm.content"))
			.ariaLabel("confirm_delete")
			.targetEvent(ev)
			.ok(sbiModule_translate.load("sbi.general.continue"))
			.cancel(sbiModule_translate.load("sbi.general.cancel"));
		$mdDialog.show(confirm).then(function() {
			$scope.deleteFunct(item.id);
	    }, function() {

	    });
	};

	$scope.loadFolder = function(item) {

		for(var i=0; i<$scope.folders.length; i++){
			$scope.folders[i].expanded=true;

		}
		if ($scope.dirtyForm) {
			$mdDialog.show($scope.confirm).then(function() {
				$scope.selectedFolder = angular.copy(item);
				if(item.parentId!=null  ){
					$scope.getParent(item.parentId);
				}
				$scope.showme = true;
				$scope.showPlusButton = true;
				$scope.dirtyForm = false;
			}, function() {
				$scope.showme = true;
			});

		} else {
			if (angular.copy(item).parentId !== null) {
				$scope.selectedFolder = angular.copy(item);
				if(item.parentId!=null){
					$scope.getParent(item.parentId);
				}
				$scope.showme = true;
				$scope.showPlusButton = true;
			} else {
				$scope.selectedFolder = angular.copy(item);
				if(item.parentId!=null){
					$scope.getParent(item.parentId);
				}
				$scope.showme = false;
				$scope.showPlusButton = true;

			}
		}
	}

};

