angular.module('alertDefinitionManager').controller('sendMailController', function($scope, $timeout) {
	  $scope.validate=function(){
		  console.log("check mail valiity")
		  var valid=true;
		  
		  if($scope.ngModel.subject== undefined || $scope.ngModel.subject.trim()=="")
			  valid=false;
		  
		  if($scope.ngModel.body== undefined || $scope.ngModel.body.trim()=="")
			  valid=false;
		  
		  if($scope.ngModel.mailTo== undefined || $scope.ngModel.mailTo.length==0)
			  valid=false;
		  
		 return valid;
	  }
	  
	  $scope.initNgModel=function(){
		  if($scope.ngModel==undefined){
			  $scope.ngModel=angular.extend({});
		  } 
		  
		  if(!$scope.ngModel.hasOwnProperty("mailTo")){
			  	$scope.ngModel.mailTo=[];
			  }
	  }
	  
	});