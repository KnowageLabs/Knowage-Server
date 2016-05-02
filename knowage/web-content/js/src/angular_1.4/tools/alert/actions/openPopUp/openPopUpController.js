angular.module('alertDefinitionManager').controller('openPopUpController', function($scope, $timeout) {
	 
	  $scope.validate=function(){ 
		  
		 return $scope.ngModel.test;
	  }
	});