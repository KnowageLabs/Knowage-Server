
var app = angular.module('domainManagementApp', ['angular_table','ngMaterial', 'ui.tree', 'angularUtils.directives.dirPagination', 'angular_list', 'angular-list-detail', 'sbiModule']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

app.controller('Controller', ['$angularListDetail', 'sbiModule_messaging', 'sbiModule_translate','sbiModule_restServices', '$scope', '$q', '$log', '$mdDialog',"sbiModule_config", manageDomainFucntion ]);


function manageDomainFucntion($angularListDetail,sbiModule_messaging, sbiModule_translate, sbiModule_restServices, $scope, $q, $log,  $mdDialog,sbiModule_config) {
	
	$scope.path = "2.0/domains";
	var headers = {
			'Content-Type': 'application/json'
	};
	$scope.translate = sbiModule_translate;
	$scope.message = sbiModule_messaging;
	$scope.data=[]
	$scope.itemSelected= {};
	 
	
	var rowDefault = {
		valueId : "",
		valueCd : "",
		valueName : "Value Name",
		domainCode : "Domain Code",
		domainName : "Domain Name",
		valueDescription : "Description Default"
	};
	
	$scope.domainSpeedMenu = [{
    	label: $scope.translate.load('sbi.generic.edit'),
    	icon:'fa fa-pencil',
    	color:'#153E7E',
    	action:function(item,event){
    		$scope.editRow(item);
    	}
	},{
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
	
	$scope.addDomain = function(){
		$scope.domain = {};
		$angularListDetail.goToDetail();
	}
	
	$scope.closeDetail = function(){
		$angularListDetail.goToList();
		$scope.domainForm.$setPristine();
		$scope.domainForm.$setUntouched();
	}
	
	$scope.editRow = function(item) {
		$scope.domain = angular.copy(item);
		$angularListDetail.goToDetail();
	}
	
	$scope.saveRow = function(){
		var rowSelected = angular.copy($scope.domain);
		if (rowSelected.valueId !== undefined) {
			$scope.saveModifiedRow(rowSelected);
		}else{
			$scope.saveNewRow(rowSelected);
		}
		$scope.domainForm.$setPristine();
		$scope.domainForm.$setUntouched();
	};
	
	$scope.saveModifiedRow = function(item){
		var idx = $scope.indexOf($scope.data, item);
		sbiModule_restServices
			.promisePut($scope.path,item.valueId, angular.toJson(item), headers)
				.then(function(response){
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
						return;
					}
					$scope.data[idx]=item;
					$angularListDetail.goToList();
					$scope.domain = {};
					$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
				},function(){
					$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
				});
	}
	
	
	$scope.saveNewRow = function(item){
		sbiModule_restServices
		.post($scope.path,"",item,headers)
		.then(function successCallback(response) {
			if (response.status == 201){
				item.valueId = response.data;
				$scope.data.splice(0, 0, item);
				$angularListDetail.goToList();
				$scope.domain = {};
				$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
			}
			else {
				item.valueId = "";
				if (response.data.errors != undefined){
					sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
				}else{
					$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
				}
			}
		},
		function errorCallback(response) {
			item.valueId = "";
			$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
		});	
	}
	
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
  	  			if (rowsSelected.valueId !== undefined) {
  	  				var idx = $scope.indexOf($scope.data, rowsSelected);
  	  				if (idx>=0){
  	  					sbiModule_restServices.promiseDelete($scope.path, rowsSelected.valueId)
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
	
	$scope.labelDetailFunction = function(){
		if ($scope.domain){
			if ($scope.domain.valueId == undefined){
				return $scope.translate.load('sbi.generic.new');
			}else{
				return $scope.translate.load('sbi.generic.edit');
			}
		}
		return '';
	}
	
	//search function for data array
	$scope.indexOf = function(myArray, myElement) {
		for (var i = 0; i < myArray.length; i++) {
			if (myArray[i].valueId == myElement.valueId) {
				return i;
			}
		}
		return -1;
	};
};
