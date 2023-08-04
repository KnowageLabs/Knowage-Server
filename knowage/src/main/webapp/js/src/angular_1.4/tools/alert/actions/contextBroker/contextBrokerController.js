angular.module('alertDefinitionManager').controller('contextBrokerController', function($scope, $timeout,sbiModule_translate,sbiModule_restServices) {
	$scope.listUserEmail = []; 
	
	$scope.validate=function(){
		  console.log("check context broker definition validity")
		  var valid=true;
		  
		  if($scope.ngModel.contextBrokerUrl== undefined || $scope.ngModel.contextBrokerUrl.trim()=="")
			  valid=false;

		  if($scope.ngModel.contextBrokerType== undefined || $scope.ngModel.contextBrokerType.trim()=="")
			  valid=false;
		  
		  
		 return valid;
	  }
	  
	  $scope.initNgModel=function(){
		  if($scope.ngModel==undefined){
			  $scope.ngModel=angular.extend({});
		  } 
	  }
	

	});