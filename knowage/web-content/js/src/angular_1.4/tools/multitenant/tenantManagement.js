
var app = angular.module('tenantManagementApp',['angular_table','ngMaterial', 'ui.tree', 'angularUtils.directives.dirPagination', 'ng-context-menu',
                                                'sbiModule', 'angular_list']);
		
app.controller('Controller', ['sbiModule_logger','sbiModule_translate','sbiModule_restServices', '$scope', '$q', '$log', '$mdDialog', manageTenantFunction ])


function manageTenantFunction(sbiModule_logger,sbiModule_translate, sbiModule_restServices, $scope, $q, $log,  $mdDialog) {
	
	var path = "multitenant";
	$scope.log = sbiModule_logger;
	$scope.translate=sbiModule_translate;
	$scope.tenants=[];
	$scope.tenantSelected = {};
	$scope.themes = [];
	$scope.productTypesDefault = [];
	$scope.engines = [];
	$scope.datasources = [];
	$scope.datasourcesDefault = [];
	
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
			for (var i=0;i<$scope.datasources.length;i++){
				$scope.datasources[i].checkbox = '<md-checkbox ng-model=\"datasources[' + i+ ']\"  aria-label=\"datasources	[' + i+ '].CHECKED\" ng-init=\"'+$scope.datasources[i].CHECKED+'\"></md-checkbox>';
			}
			$scope.datasourcesDefault= angular.fromJson(angular.toJson($scope.datasources));
		}
	});

	sbiModule_restServices.get(path, "producttypes").success(function(data) {
		if (data.root !== undefined && data.root.length > 0){
			$scope.productTypes = data.root;
			for (var i=0;i<$scope.productTypes.length;i++){
				$scope.productTypes[i].checkbox = '<md-checkbox ng-model=\"productTypes[' + i+ ']\"  aria-label=\"productTypes[' + i+ '].CHECKED\" ng-init=\"'+$scope.productTypes[i].CHECKED+'\"></md-checkbox>';
			}
			$scope.productTypesDefault = angular.fromJson(angular.toJson($scope.productTypes));
		}
	});
	
	$scope.toogle = function(){
		var result = !this.CHECKED;
		return result;
		
	}
	$scope.copyRowInForm = function(item,cell,listId) {
		//not productTypes for this tenant? Get them!
		if (item.productTypes === undefined){
			$scope.item=item;
			sbiModule_restServices.get(path, "producttypes", "TENANT="+item.MULTITENANT_NAME).success(function(data) {
				$scope.item.productTypes = data.root;
				for (var i=0;i<$scope.item.productTypes.length;i++){
					$scope.item.productTypes[i].checkbox = '<md-checkbox ng-model=\"productTypes[' + i+ '].CHECKED\" ng-init=\"productTypes[' + i+ '].CHECKED='+$scope.item.productTypes[i].CHECKED+'\" aria-label=\"productTypes[' + i+ '].CHECKED\"></md-checkbox>';
				}
				$scope.productTypes = $scope.item.productTypes;
			});
		} else{
			$scope.productTypes = item.productTypes;
		}
		//not datasources for this tenant? Get them!
		if (item.datasources === undefined){
			$scope.item=item;
			sbiModule_restServices.get(path, "datasources", "TENANT="+item.MULTITENANT_NAME).success(function(data) {
				$scope.item.datasources = data.root;
				for (var i=0;i<$scope.item.datasources.length;i++){
					$scope.item.datasources[i].checkbox = '<md-checkbox ng-model=\"datasources[' + i+ '].CHECKED\" ng-init=\"datasources[' + i+ '].CHECKED='+$scope.item.datasources[i].CHECKED+'\" aria-label=\"datasources[' + i+ '].CHECKED\"></md-checkbox>';
				}
				$scope.datasources = $scope.item.datasources;
			});
		} else{
			$scope.datasources = item.datasources;
		}
		
		$scope.tenant = angular.fromJson(angular.toJson(item));
	}
	
	$scope.resetForm = function(){
		$scope.productTypes = $scope.productTypesDefault;
		$scope.datasources = $scope.datasourcesDefault;
		$scope.tenant = undefined;
		$scope.tenantSelected = undefined;
		$scope.tenantForm.$setUntouched();
		$scope.tenantForm.$setPristine();
	}
	
	//toogle the tenant.CHECKED value if the checkbox is clicked
	$scope.toogleCheckBox = function(item,cell,listId){
		if (cell.indexOf("<md-checkbox") >= 0 ){
			item.CHECKED = !item.CHECKED;
		}
	}
	
	$scope.solveMissingId = function (tenant){
		sbiModule_restServices.get(path, tenant.MULTITENANT_NAME)
			.success(function(data){
				if (data.root.MULTITENANT_ID !== undefined){
					tenant.MULTITENANT_ID = data.root.MULTITENANT_ID ;
					$scope.tenants.splice(0,0,angular.fromJson(angular.toJson(tenant)));
				}
			})
	}
	
	$scope.deleteTenant = function() {
		//get the tenant selected, JSON creation for the body request
		if ($scope.tenantSelected !== undefined ){
			config = {};
			var toDelete = {};
			toDelete.MULTITENANT_ID = $scope.tenantSelected.MULTITENANT_ID; //convert to string
			toDelete.MULTITENANT_NAME = ""+ $scope.tenantSelected.MULTITENANT_NAME;
			toDelete.MULTITENANT_THEME = ""+ $scope.tenantSelected.MULTITENANT_THEME;
			config.data = angular.toJson(toDelete); 
			sbiModule_restServices.delete(path, "", undefined, config );
			//remove tenant from the tenants list
			var idx = $scope.indexOf($scope.tenants,$scope.tenantSelected)
			$scope.tenants.splice(idx, 1);
			$scope.resetForm();
		}
	}
	
	$scope.addTenant = function(){
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
		for ( var i =0;i<$scope.productTypes.length;i++){
			if ($scope.productTypes[i].CHECKED == true){
				newTenant.PRODUCT_TYPE_LIST.push({ "ID" : ""+$scope.productTypes[i].ID, "LABEL" : ""+$scope.productTypes[i].LABEL});
			}
		}
		newTenant.DS_LIST= [];
		for ( var i =0;i<$scope.datasources.length;i++){
			if ($scope.datasources[i].CHECKED == true){
				newTenant.DS_LIST.push({ "ID" : ""+$scope.datasources[i].ID, "NAME" : ""+$scope.datasources[i].NAME, "DESCRIPTION" : ""+$scope.datasources[i].DESCRIPTION });
			}
		}
		$scope.newTenant = newTenant;
		sbiModule_restServices.post(path,"save",angular.toJson(newTenant)).success(function(data){
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
					$scope.tenants.splice(0,0,angular.fromJson(angular.toJson(newTenant)));
				}
			}else{
				//updating the table
				var idx = $scope.indexOf($scope.tenants,newTenant);
				$scope.tenants[idx]=angular.fromJson(angular.toJson(newTenant));
				}
			$scope.resetForm();
		});
	}
};

