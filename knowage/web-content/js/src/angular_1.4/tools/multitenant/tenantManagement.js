
var app = angular.module('tenantManagementApp',['angular_table','ngMaterial', 'ui.tree', 'angularUtils.directives.dirPagination', 'angular_list', 'angular-list-detail', 'sbiModule']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

app.controller('Controller', ['$angularListDetail', 'sbiModule_messaging', '$timeout','sbiModule_logger','sbiModule_translate','sbiModule_restServices', '$scope', '$q', '$log', '$mdDialog', manageTenantFunction ])


function manageTenantFunction($angularListDetail,sbiModule_messaging, $timeout,sbiModule_logger,sbiModule_translate, sbiModule_restServices, $scope, $q, $log,  $mdDialog) {
	
	$scope.path = "multitenant";
	$scope.log = sbiModule_logger;
	$scope.message = sbiModule_messaging;
	$scope.translate=sbiModule_translate;
	$scope.tenants=[];
	$scope.tenant = {};
	$scope.tenantSelected = {};
	$scope.themes = [];
	$scope.productTypesDefault = [];
	$scope.engines = [];
	$scope.datasources = [];
	$scope.datasourcesDefault = [];
	
	$scope.datasourcesSelected = [];
	$scope.productsSelected = [];
	$scope.loadinMessage = false;
	$scope.selectedIndex = {idx : 0};
	$scope.showMe = false;
	
	/********************************************************
	 * Get tenants, product types, data-sources and themes	*
	 ********************************************************/
	 
	sbiModule_restServices.promiseGet($scope.path, "", null)
		.then(function(response) {
			if (response.data.errors != undefined){
				sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
				return;
			}
			var data = response.data;
			if (data.root !== undefined && data.root.length > 0){
				$scope.tenants = data.root;
				for (var i = 0; i < $scope.tenants.length ; i++){
					$scope.tenants[i].MULTITENANT_THEME = $scope.tenants[i].MULTITENANT_THEME === undefined ? "" : $scope.tenants[i].MULTITENANT_THEME ;
				}
			}
		}, function(response, status, headers, config){
			if (response.data.errors != undefined){
				sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
			}
			$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg'));
	});
	
	sbiModule_restServices.promiseGet($scope.path, "themes")
		.then(function(response) {
			if (response.data.errors != undefined){
				sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
				return;
			}
			$scope.themes = response.data.root !== undefined && response.data.root.length > 0 ? response.data.root : [];
		}, function(response, status, headers, config){
			if (response.data.errors != undefined){
				sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
			}
			$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg'));
	});
	
	sbiModule_restServices.promiseGet($scope.path, "datasources", null)
		.then(function(response) {
			if (response.data.errors != undefined){
				sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
				return;
			}
			var data = response.data;
			if (data.root !== undefined && data.root.length > 0){
				$scope.datasources= data.root;
				$scope.datasourcesDefault= angular.copy($scope.datasources);
			}
		}, function(response, status, headers, config){
			if (response.data.errors != undefined){
				sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
			}
			$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg'));
	});

	sbiModule_restServices.promiseGet($scope.path, "producttypes")
		.then(function(response) {
			if (response.data.errors != undefined){
				sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
				return;
			}
			var data = response.data;
			if (data.root !== undefined && data.root.length > 0){
				$scope.productTypes = data.root;
				$scope.productTypesDefault = angular.copy($scope.productTypes);
			}
		}, function(response, status, headers, config){
			if (response.data.errors != undefined){
				sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
			}
			$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') );
	});
	
	/************************
	 * Functions			*
	 ************************/
	
	//SPEED MENU TRASH ITEM
	$scope.tenantSpeedMenu= [{
		                    	label:'delete',
		                    	icon:'fa fa-trash-o fa-lg',
		                    	color:'#153E7E',
		                    	action:function(item,event){
		                    		$scope.deleteItem(item);
		                    	}
	                     	}];
	
	$scope.resetForm = function(){
		$scope.productTypes = $scope.productTypesDefault;
		$scope.datasourcesSelected.splice(0,$scope.datasourcesSelected.length);
		$scope.productsSelected.splice(0,$scope.productsSelected.length);
		$scope.tenant = {};
		$scope.tenantSelected = undefined;
		$scope.tenantForm.$setUntouched();
		$scope.tenantForm.$setPristine();
		$scope.selectedIndex.idx = undefined;
		$scope.showMe= false;
	};
	
	$scope.addTenant = function(){
		$scope.resetForm();
		//search for a default theme
		var themeDefault = $scope.themes.find(function (element, index, array){
			if (element.VALUE_CHECK == "sbi_default"){
				return element;
			}
			return false;
		});
		$scope.tenant.MULTITENANT_THEME = themeDefault ? themeDefault.VALUE_CHECK : "sbi_default";
		$scope.showMe = true;
		$scope.selectedIndex.idx = 0;
	}
	
	$scope.deleteItem = function(item){
		var confirm = $mdDialog.confirm()
				.title($scope.translate.load("sbi.multitenant.delete.title"))
				.content($scope.translate.load("sbi.multitenant.delete.msg"))
				.ok($scope.translate.load("sbi.general.yes"))
				.cancel($scope.translate.load("sbi.general.No"));
		$mdDialog.show(confirm)
			.then(function(){
				$scope.deleteTenant(item);
			},function(){});
	}
	
	$scope.copyRowInForm = function(item,cell,listId) {
		//Empty the form data sources and products type
		$scope.datasourcesSelected.splice(0,$scope.datasourcesSelected.length);
		$scope.productsSelected.splice(0,$scope.productsSelected.length);
		//not productTypes for this tenant? Get them!
		$scope.item=item;
		if ($scope.item.productTypes === undefined){
			sbiModule_restServices.promiseGet($scope.path, "producttypes", "TENANT="+item.MULTITENANT_NAME)
				.then(function(response) {
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
						return;
					}
					$scope.item.productTypes = response.data.root;
					$scope.copySelectedElement($scope.item.productTypes,$scope.productsSelected);
					$scope.productTypes = $scope.item.productTypes;
				}, function(response, status, headers, config){
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
					}
					$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg'));
			});
		} else{
			$scope.copySelectedElement($scope.item.productTypes,$scope.productsSelected);
			$scope.productTypes = $scope.item.productTypes;
		}
		//not datasources for this tenant? Get them!
		if ($scope.item.datasources === undefined){
			sbiModule_restServices.promiseGet($scope.path, "datasources", "TENANT="+item.MULTITENANT_NAME)
				.then(function(response) {
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
						return;
					}
					$scope.item.datasources = response.data.root;
					$scope.copySelectedElement($scope.item.datasources,$scope.datasourcesSelected);
					$scope.datasources = $scope.item.datasources;
				}, function(response, status, headers, config){
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
					}
					$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg'));
			});
		} else{
			$scope.copySelectedElement($scope.item.datasources,$scope.datasourcesSelected);
			$scope.datasources = $scope.item.datasources;
		}
		$scope.tenant = $scope.item;
		$scope.tenantSelected = $scope.item; 
		$scope.showMe = true;
	};
	
	$scope.deleteTenant = function(item) {
		//get the tenant selected, JSON creation for the body request
		if (item !== undefined && item.MULTITENANT_ID !== undefined){
			config = {};
			var toDelete = {};
			toDelete.MULTITENANT_ID = item.MULTITENANT_ID; //convert to string
			toDelete.MULTITENANT_NAME = ""+ item.MULTITENANT_NAME;
			toDelete.MULTITENANT_THEME = ""+ item.MULTITENANT_THEME;
			config.data = angular.toJson(toDelete);
			$scope.showLoading(true);
			sbiModule_restServices.promiseDelete($scope.path, "", undefined, config )
				.then(function(response){
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
						return;
					}	
					//remove tenant from the tenants list
					var idx = $scope.indexOf($scope.tenants, item)
					$scope.tenants.splice(idx, 1);
					$scope.message.showSuccessMessage($scope.translate.load('sbi.multitenant.deleted'));
					$scope.showLoading(false);
					$scope.resetForm();
				}, function(response,status){
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
					}
					$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg'));
					$scope.showLoading(false);
				});
		} else {
			$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg'));
			$scope.showLoading(false);
		}
	};
	
	$scope.saveTenant = function(){
		var newTenant = {};
		$scope.typeSave =""; //UPDATE or INSERT
		if ($scope.tenantSelected === undefined || $scope.tenantSelected.MULTITENANT_ID === undefined ){
			newTenant.MULTITENANT_ID =  "";
			newTenant.MULTITENANT_NAME = $scope.tenant.MULTITENANT_NAME;
			newTenant.MULTITENANT_THEME = $scope.tenant.MULTITENANT_THEME;
			$scope.typeSave = "INSERT";
		}else{
			newTenant.MULTITENANT_ID =  ""+$scope.tenantSelected.MULTITENANT_ID; //original field
			newTenant.MULTITENANT_NAME = $scope.tenantSelected.MULTITENANT_NAME; //original field
			newTenant.MULTITENANT_THEME = $scope.tenant.MULTITENANT_THEME; //modified field
			$scope.typeSave= "UPDATE";
		}
		newTenant.PRODUCT_TYPE_LIST = [];
		for ( var i =0;i<$scope.productsSelected.length;i++){
			newTenant.PRODUCT_TYPE_LIST.push({ "ID" : ""+$scope.productsSelected[i].ID, "LABEL" : ""+$scope.productsSelected[i].LABEL});
		}
		newTenant.DS_LIST= [];
		for ( var i =0;i<$scope.datasourcesSelected.length;i++){
			newTenant.DS_LIST.push({ "ID" : ""+$scope.datasourcesSelected[i].ID, "LABEL" : ""+$scope.datasourcesSelected[i].LABEL, "DESCRIPTION" : ""+$scope.datasourcesSelected[i].DESCRIPTION });
		}
		$scope.newTenant = newTenant;
		$scope.showLoading(true);
		sbiModule_restServices.promisePost($scope.path,"save",angular.toJson(newTenant))
			.then(function(response){
				if (response.data.errors != undefined){
					sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
					return;
				}
				var tenantReceived = angular.fromJson(response.data);
				var newTenant ={};
				newTenant.MULTITENANT_ID = tenantReceived.MULTITENANT_ID;
				newTenant.MULTITENANT_NAME = ""+$scope.newTenant.MULTITENANT_NAME;
				newTenant.MULTITENANT_THEME = ""+$scope.newTenant.MULTITENANT_THEME;
				
				if ($scope.typeSave == "INSERT"){
					//add element to the table
					if ( newTenant.MULTITENANT_ID === undefined ){
						$scope.solveMissingId(newTenant);
					}else{
						$scope.tenants.splice(0,0,angular.copy(newTenant));
						var name = newTenant.MULTITENANT_NAME;
						var message = $scope.translate.load('sbi.multitenant.saved') + ' "' +name.toLowerCase()+'_admin"';
						$scope.showAlert('INFO - '+ name , message);
						$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
						$scope.showLoading(false);
						$scope.resetForm();
					}
				}else{
					//updating the table
					var idx = $scope.indexOf($scope.tenants,newTenant);
					$scope.tenants[idx]=angular.copy(newTenant);
					var name = newTenant.MULTITENANT_NAME;
					var message = $scope.translate.load('sbi.multitenant.updated');
					$scope.message.showSuccessMessage(message);
					$scope.showLoading(false);
					$scope.resetForm();
				}
				
			},function(response,status){
				if (response.data.errors != undefined){
					sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
				}
				$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg'));
				$scope.showLoading(false);
			});
	};

	$scope.solveMissingId = function (tenant){
		sbiModule_restServices.promiseGet($scope.path, tenant.MULTITENANT_NAME)
			.then(function(response) {
				if (response.data.errors != undefined){
					sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
					return;
				}
				var data = response.data;
				if (data.root.MULTITENANT_ID !== undefined){
					tenant.MULTITENANT_ID = data.root.MULTITENANT_ID ;
					$scope.tenants.splice(0,0,angular.copy(tenant));
					var name = tenant.MULTITENANT_NAME;
					var message = $scope.translate.load('sbi.multitenant.saved') + ' "' +name.toLowerCase()+'_admin"';
					$scope.showAlert('INFO - '+ name , message);
					$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
					$scope.resetForm();
				} else{
					$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg'));
				}
				$scope.showLoading(false);
			}, function(reponse, status, headers, config){
				if (response.data.errors != undefined){
					sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
				}
				$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg'));
				$scope.showLoading(false);
			});
	};
	

	//Each AngularTable has an array of selected item. Insert the elements selected in this array
	$scope.copySelectedElement = function(source,selected){
		for (var i = 0 ; i< source.length;i++){
			if(source[i].CHECKED == true){
				selected.push(source[i]);
			}
		}
	}
	
	$scope.toggleCheckBox = function(item,cell,listId){
		item.CHECKED=!item.CHECKED;
	}

	$scope.showLoading = function(value){
		$timeout(function(){
			$scope.loadinMessage = value;
		},0,true);
	}
	
	$scope.indexOf = function(myArray, myElement) {
		for (var i = 0; i < myArray.length; i++) {
			if (myArray[i].MULTITENANT_ID == myElement.MULTITENANT_ID) {
				return i;
			}
		}
	};
	
	//Create an alert dialog with a message
	$scope.showAlert = function (title, message){
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
};

