
var app = angular.module('configManagementApp',  ['angular_table','ngMaterial', 'ui.tree', 'angularUtils.directives.dirPagination', 'angular_list', 'angular-list-detail', 'sbiModule']);

app.controller('Controller', ['$angularListDetail', 'sbiModule_messaging','sbiModule_translate','sbiModule_restServices', '$scope', '$q', '$log', '$mdDialog','sbiModule_config', manageConfigFucntion ])

sbiM.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);

function manageConfigFucntion($angularListDetail,sbiModule_messaging, sbiModule_translate, sbiModule_restServices, $scope, $q, $log,  $mdDialog,sbiModule_config) {
	
	var path = "2.0/configs";
	var headers = {
			'Content-Type': 'application/json'
	};
	$scope.translate=sbiModule_translate;
	$scope.message = sbiModule_messaging;
	$scope.data=[]
	$scope.itemSelected= {};
	$scope.filterCategory=[];
	$scope.hashCategory={};
	
	var rowDefault = {
		id : "",
		label : "Label",
		name : "Name",
		valueCheck : "value check",
		valueTypeId : 407,
		category : "",
		active : "false"
	};
	
	$scope.configSpeedMenu = [{
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


	sbiModule_restServices.promiseGet(path, "", null).then(function(response) {
		//creating map containing filters categories
		if (response.data.errors != undefined){
			sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
			return;
		}
		var data = response.data;
		for (i=0;i<data.length;i++){
			if ($scope.hashCategory[data[i].category] === undefined){
				$scope.hashCategory[data[i].category]=data[i].category;
			}
			data[i].valueTypeId = data[i].valueTypeId == 407 ? 'NUM' : 'STRING';
		}
		//use the map to create filterCategory array for Grid filter
		for (var k in $scope.hashCategory){
			$scope.filterCategory.push({"value" :k , "label": k});
		}
		$scope.data = data;
	},function(data, status, headers, config){
		$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + data);
	});
	
	$scope.addConfig = function(){
		$scope.config = {};
		$angularListDetail.goToDetail();
	}
	
	$scope.closeDetail = function(){
		$angularListDetail.goToList();
	}
	
	$scope.editRow = function(item) {
		$scope.config = angular.copy(item);
		$scope.config.valueTypeId = item.valueTypeId == 'NUM' ? 407 : 408; 
		$angularListDetail.goToDetail();
	}
	
	$scope.saveRow = function(){
		var rowSelected = angular.copy($scope.config);
		if (rowSelected.id !== undefined) {
			$scope.saveModifiedRow(rowSelected);
		}else{
			$scope.saveNewRow(rowSelected);
		}
	};
	
	$scope.saveModifiedRow = function(item){
		var idx = $scope.indexOf($scope.data, item);
		sbiModule_restServices
			.put(path,item.id, angular.toJson(item), headers)
				.then(function(response){
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
						return;
					}
					item.valueTypeId = item.valueTypeId ==  407 ? 'NUM' : 'STRING';
					$scope.data[idx]=item;
					$angularListDetail.goToList();
					$scope.config = {};
					$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
				},function(){
					$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
				});
	}
	
	$scope.saveNewRow = function(item){
		sbiModule_restServices
		.post(path,"",angular.toJson(item),headers)
		.then(function successCallback(response) {
			if (response.status == 201){
				item.id = response.data;
				item.valueTypeId = item.valueTypeId == 407 ? 'NUM' : 'STRING';
				$scope.data.splice(0, 0, item);
				$angularListDetail.goToList();
				$scope.config = {};
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
	  					$oldValue = $scope.data.splice(idx, 1);
	  					sbiModule_restServices.delete(path, rowsSelected.id)
	  						.then(function(response){
	  							if (response.data.errors != undefined){
	  								sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
	  								return;
	  							}
	  							$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
	  						},function(data,status){
	  							$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + data);
	  						});
	  				}
	  			}
	  		},function(){});
	};
	
	$scope.labelDetailFunction = function(){
		if ($scope.config){
			if ($scope.config.id == undefined){
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
			if (myArray[i].id == myElement.id) {
				return i;
			}
		}
		return -1;
	};
};
