angular.module('alertDefinitionManager').controller('sendMailController', function($scope, $timeout,sbiModule_translate) {
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
	  
	  $scope.addMailAddressCallBack=function(chip){
		  
		  var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		  if(re.test(chip)){
			  return chip;
		  }else{
			  var errorEmailMessages=sbiModule_translate.load("sbi.alert.action.sendMail.invalidMailAddress");
			  $timeout(function(){ 
				  var index=$scope.ngModel.mailTo.indexOf(errorEmailMessages);
				  if(index!=-1){
					  $scope.ngModel.mailTo.splice(index,1);
				  }
				  },1000)
			  return errorEmailMessages;
		  }
	  }
	});