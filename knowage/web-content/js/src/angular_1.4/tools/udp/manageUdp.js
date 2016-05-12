
var app = angular.module('udpManagementApp', ['angular_table','ngMaterial', 'ui.tree', 'angularUtils.directives.dirPagination', 'angular_list', 'angular-list-detail', 'sbiModule']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

app.controller('Controller', ['$angularListDetail', 'sbiModule_messaging', 'sbiModule_device','sbiModule_translate','sbiModule_restServices', '$scope', '$q', '$log', '$mdDialog', manageUdpFucntion ])


function manageUdpFucntion($angularListDetail,sbiModule_messaging, sbiModule_device,sbiModule_translate, sbiModule_restServices, $scope, $q, $log,  $mdDialog) {
	
	$scope.path = "2.0/udp";
	
	$scope.translate=sbiModule_translate;
	$scope.message = sbiModule_messaging;
	$scope.data=[]
	$scope.itemSelected = {};
	$scope.showMe = false;
	
	var rowDefault = {
			"id" : "",
			"label" : "",
			"name" : "",
			"type" : "",
			"family" : "",
			"description" : "",
			"multivalue" : ""
		};
	
	$scope.udpSpeedMenu = [{
    	label: $scope.translate.load('sbi.generic.delete'),
    	icon:'fa fa-trash',
    	color:'#153E7E',
    	action:function(item,event){
    		$scope.deleteRow(item);
    	}
	}];
	
	sbiModule_restServices.promiseGet($scope.path, "", null)
		.then(function(response) {
				if (response.data.errors != undefined){
					sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
					return;
				}
				$scope.data = response.data;
			}, function(data, status, headers, config){
				$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + data);
		});
	
	
	$scope.copyRowInForm = function(item,cell,listId) {
		$scope.property = angular.copy(item);
		$scope.showMe = true;
	}
	
	$scope.addUdp = function(){
		$scope.property = {};
		$scope.udpForm.$setUntouched();
		$scope.udpForm.$setPristine();
		$scope.showMe = true;
	}
	
	$scope.resetForm = function(){
		$scope.property = undefined;
		$scope.udpForm.$setUntouched();
		$scope.udpForm.$setPristine();
		$scope.showMe = false;
	}
	
	$scope.saveProperty = function(){
		var rowSelected = angular.copy($scope.property);
		if (rowSelected.id !== undefined) {
			$scope.saveModifiedProperty(rowSelected);
		}else{
			$scope.saveNewProperty(rowSelected);
		}
		$scope.udpForm.$setPristine();
		$scope.udpForm.$setUntouched();
	};
	
	$scope.saveNewProperty = function(item) {
		//if id not set, add new row, else update old row
		item.multivalue = $scope.property.multivalue === undefined ? "false" : $scope.property.multivalue;
		item.description = $scope.property.description === undefined ? "" : $scope.property.description;
		item.id = "";
		sbiModule_restServices
			.promisePost($scope.path,"",angular.toJson(item))
			.then(function successCallback(response) {
				if (response.status == 201){
					item.id = response.data;
					$scope.data.splice(0, 0, item);
					$scope.property = {};
					$scope.showMe = false;
					$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
				}
				else {
					item.id = "";
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
					}else{
						$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
					}
				}
			},
			function errorCallback(response) {
				item.id = "";
				$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
			});	
	} 
	
	$scope.saveModifiedProperty = function(item){
		sbiModule_restServices
		.promisePut($scope.path,item.id,angular.toJson(item))
			.then(function(response){
				if (response.data.errors != undefined){
					sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
					return;
				}
				var idx = $scope.indexOf($scope.data,item);
				$scope.data[idx]=item;
				//update value in the grid
				for (key in item){
					$scope.data[idx][key] = item[key];
				}
				$scope.property = {};
				$scope.showMe = false;
				$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
			},function(response){
				$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
			});
	};

	// delete selected rows and update DB
	$scope.deleteRow = function(item) {
		var confirm = $mdDialog.confirm()
        .title($scope.translate.load('sbi.generic.delete'))
        .content($scope.translate.load('sbi.generic.confirmDelete'))
        .ok($scope.translate.load('sbi.general.yes'))
        .cancel($scope.translate.load('sbi.general.No'));

		$mdDialog
		.show(confirm)		      
		.then(function(){
			var rowsSelected = item;
			if (rowsSelected.id !== undefined) {
				var idx = $scope.indexOf($scope.data, rowsSelected);
				if (idx>=0){
					sbiModule_restServices.promiseDelete($scope.path, rowsSelected.id)
						.then(function(response){
							if (response.data.errors != undefined){
								sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
								return;
							}
							$scope.oldValue = $scope.data.splice(idx, 1);
							$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
						},function(data,status){
							$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + data);
						});
				}
			}
		},function(){});
	};
	
	//search function for data array
	$scope.indexOf = function(myArray, myElement) {
		for (var i = 0; i < myArray.length; i++) {
			if (myArray[i].id == myElement.id) {
				return i;
			}
		}
		return -1;
	};

};

