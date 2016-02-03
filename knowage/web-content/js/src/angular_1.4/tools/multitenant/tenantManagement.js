
var app = angular.module('tenantManagementApp',['angular_table','ngMaterial', 'ui.tree', 'angularUtils.directives.dirPagination', 'ng-context-menu',
                                                'sbiModule','angular_2_col']);
		
app.controller('Controller', ['sbiModule_logger','sbiModule_translate','sbiModule_restServices', '$scope', '$q', '$log', '$mdDialog', manageTenantFunction ])


function manageTenantFunction(sbiModule_logger,sbiModule_translate, sbiModule_restServices, $scope, $q, $log,  $mdDialog) {
	
	var path = "multitenant";
	$scope.log = sbiModule_logger;
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
	
	$scope.idx_tab = 0;
	$scope.showForm = false;
	$scope.indexOf = function(myArray, myElement) {
		for (var i = 0; i < myArray.length; i++) {
			if (myArray[i].MULTITENANT_ID == myElement.MULTITENANT_ID) {
				return i;
			}
		}
	};
	
	sbiModule_restServices.get(path, "", null).success(function(data) {
		if (data.root !== undefined && data.root.length > 0){
			$scope.tenants = data.root;
			for (var i = 0; i < $scope.tenants.length ; i++){
				$scope.tenants[i].MULTITENANT_THEME = $scope.tenants[i].MULTITENANT_THEME === undefined ? "" : $scope.tenants[i].MULTITENANT_THEME ;
			}
		}
	});
	
	sbiModule_restServices.get(path, "themes").success(function(data) {
		if (data.root !== undefined && data.root.length > 0){
			$scope.themes = data.root;
		}
		else{
			$scope.themes = [];
		}
	});
	
	sbiModule_restServices.get(path, "datasources", null).success(function(data) {
		if (data.root !== undefined && data.root.length > 0){
			$scope.datasources= data.root;
			$scope.datasourcesDefault= angular.copy($scope.datasources);
		}
	});

	sbiModule_restServices.get(path, "producttypes").success(function(data) {
		if (data.root !== undefined && data.root.length > 0){
			$scope.productTypes = data.root;
			$scope.productTypesDefault = angular.copy($scope.productTypes);
		}
	});
	
	$scope.toggleCheckBox = function(item,cell,listId){
		item.CHECKED=!item.CHECKED;
	}
	
	$scope.copyRowInForm = function(item,cell,listId) {
		//Empty the form data sources and products type
		$scope.datasourcesSelected.splice(0,$scope.datasourcesSelected.length);
		$scope.productsSelected.splice(0,$scope.productsSelected.length);
		//not productTypes for this tenant? Get them!
		$scope.item=item;
		if (item.productTypes === undefined){
			sbiModule_restServices.get(path, "producttypes", "TENANT="+item.MULTITENANT_NAME).success(function(data) {
				$scope.item.productTypes = data.root;
				$scope.copySelectedElement($scope.item.productTypes,$scope.productsSelected);
				$scope.productTypes = $scope.item.productTypes;
			});
		} else{
			$scope.copySelectedElement($scope.item.productTypes,$scope.productsSelected);
			$scope.productTypes = $scope.item.productTypes;
		}
		//not datasources for this tenant? Get them!
		if (item.datasources === undefined){
			sbiModule_restServices.get(path, "datasources", "TENANT="+item.MULTITENANT_NAME).success(function(data) {
				$scope.item.datasources = data.root;
				$scope.copySelectedElement($scope.item.datasources,$scope.datasourcesSelected);
				$scope.datasources = $scope.item.datasources;
			});
		} else{
			$scope.copySelectedElement($scope.item.datasources,$scope.datasourcesSelected);
			$scope.datasources = $scope.item.datasources;
		}
		$scope.tenant = $scope.item;
		$scope.tenantSelected = $scope.item; 
		$scope.showForm = true;
	};
	
	//Each AngularTable has an array of selected item. Insert the elements selected in this array
	$scope.copySelectedElement = function(source,selected){
		for (var i = 0 ; i< source.length;i++){
			if(source[i].CHECKED == true){
				selected.push(source[i]);
			}
		}
	}
	
	$scope.resetForm = function(form){
		$scope.productTypes = $scope.productTypesDefault;
		$scope.datasourcesSelected.splice(0,$scope.datasourcesSelected.length);
		$scope.productsSelected.splice(0,$scope.productsSelected.length);
		$scope.tenant = {};
		$scope.tenantSelected = undefined;
		if (form !== undefined){
			form.$setUntouched();
			form.$setPristine();
		}
		$scope.idx_tab = 0;
	};
	
	$scope.solveMissingId = function (tenant){
		sbiModule_restServices.get(path, tenant.MULTITENANT_NAME)
			.success(function(data){
				if (data.root.MULTITENANT_ID !== undefined){
					tenant.MULTITENANT_ID = data.root.MULTITENANT_ID ;
					$scope.tenants.splice(0,0,angular.copy(tenant));
					var name = tenant.MULTITENANT_NAME;
					var message = $scope.translate.load('sbi.multitenant.saved') + ' "' +name.toLowerCase()+'_admin"';
					$scope.showAlert('INFO - '+ name , message);
				} else{
					$scope.showAlert('ERROR', ' Impossible to insert Tenant');
				}
			})
	};
	
	//SPEED MENU TRASH ITEM
	$scope.tenantSpeedMenu= [{
		                    	label:'delete',
		                    	icon:'fa fa-trash-o fa-lg',
		                    	color:'#153E7E',
		                    	action:function(item,event){
		                    		$scope.deleteItem(item);
		                    	}
	                     	}];
	
	$scope.deleteItem = function(item){
		$scope.tenantSelected = item;
		$scope.deleteTenant();
	}
	
	$scope.deleteTenant = function(form) {
		//get the tenant selected, JSON creation for the body request
		if ($scope.tenantSelected !== undefined && $scope.tenantSelected.MULTITENANT_ID !== undefined){
			config = {};
			var toDelete = {};
			toDelete.MULTITENANT_ID = $scope.tenantSelected.MULTITENANT_ID; //convert to string
			toDelete.MULTITENANT_NAME = ""+ $scope.tenantSelected.MULTITENANT_NAME;
			toDelete.MULTITENANT_THEME = ""+ $scope.tenantSelected.MULTITENANT_THEME;
			config.data = angular.toJson(toDelete); 
			sbiModule_restServices.delete(path, "", undefined, config ).success(function(data){
				if (data.errors === undefined){
				//remove tenant from the tenants list
					var idx = $scope.indexOf($scope.tenants,$scope.tenantSelected)
					$scope.tenants.splice(idx, 1);
					$scope.resetForm(form);
					$scope.showAlert('INFO - ' + toDelete.MULTITENANT_NAME, $scope.translate.load('sbi.multitenant.deleted'));
				}else{
					$scope.showAlert('ERROR', ' Impossible to delete Tenant '+ toDelete.MULTITENANT_NAME + '_admin');
				}
			})
			.error(function(){
				$scope.showAlert('ERROR', ' Impossible to delete Tenant '+ toDelete.MULTITENANT_NAME + '_admin');
			});
		} else {
			$scope.showAlert('INFO', 'No Tenant selected');
		}
	};
	
	$scope.formTenant = function(){
		$scope.resetForm();
		$scope.tenant = {};
		//search default theme
		var themeDefault = $scope.themes.find(function (element, index, array){
			if (element.VALUE_CHECK == "sbi_default"){
				return element;
			}
			return false;
		});
		$scope.tenant.MULTITENANT_THEME = themeDefault ? themeDefault.VALUE_CHECK : "sbi_default";
		$scope.showForm=true;
	}
	
	$scope.saveTenant = function(form){
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
		sbiModule_restServices.post(path,"save",angular.toJson(newTenant)).success(function(data){
			if (data.errors === undefined){
				var tenantReceived = angular.fromJson(data);
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
					}
				}else{
					//updating the table
					var idx = $scope.indexOf($scope.tenants,newTenant);
					$scope.tenants[idx]=angular.copy(newTenant);
					var name = newTenant.MULTITENANT_NAME;
					var message = $scope.translate.load('sbi.multitenant.saved') + ' "' +name.toLowerCase()+'_admin"';
					$scope.showAlert('INFO - '+ name , message);
					}
			}else{
				$scope.showAlert('ERROR', 'Impossible to save tenant');
			}
			
			$scope.resetForm(form);
			
		});
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

