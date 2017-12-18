angular.module('alertDefinitionManager').controller('sendMailController', function($scope, $timeout,sbiModule_translate,sbiModule_restServices) {
	$scope.listUserEmail = []; 
	
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
	  $scope.loadMAil = function(){
	  
		  sbiModule_restServices.promiseGet("2.0", "users")
			.then(function(response) {
				
				for(var i=0;i<response.data.length;i++){
					var obj = {};
					var attributes = response.data[i].sbiUserAttributeses;
					for(var key in attributes){
						if(attributes[key]["email"]!=undefined){
							obj["name"] = response.data[i].fullName;
							obj["userId"] = response.data[i].userId;
							obj["email"] = attributes[key]["email"];
							
							$scope.listUserEmail.push(obj);
						}
					}
				}
				
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});
	  }
	  $scope.loadMAil();
	  $scope.querySearch=function (criteria) {
		  var array = [];
	      for(var index in $scope.listUserEmail){
	    	  if($scope.listUserEmail[index].name.startsWith(criteria) || $scope.listUserEmail[index].userId.startsWith(criteria)){
	    		  array.push($scope.listUserEmail[index])
	    	  }
	      }
	      if(array.length==0){
	    	  var obj = {};
	    	  obj["name"] = criteria;
	    	  obj["userId"] = "";
	    	  obj["email"] = criteria;
	    	  array.push(obj)
	      }
	      return array;
	    }
	  
	  $scope.addMailAddressCallBack=function(chip){
		  var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		  var errorEmailMessages=sbiModule_translate.load("sbi.alert.action.sendMail.invalidMailAddress");
		  var multipleEmailMessages=sbiModule_translate.load("sbi.alert.action.sendMail.multipleMailAddress");
		  var duplicatedEmailMessages=sbiModule_translate.load("sbi.alert.action.sendMail.duplicatedMailAddress");
		  var arrChip=chip.split(";");
		  if(arrChip.length>1){
			 //multiple email added
			  $timeout(function(){ 
				  var index=$scope.ngModel.mailTo.indexOf(multipleEmailMessages);
				  if(index!=-1){
					  $scope.ngModel.mailTo.splice(index,1);
				  }
				  var errEmail=0;
				  var duplicateEmail=0;
				  for(var i=0;i<arrChip.length;i++){
					  if(re.test(arrChip[i])){
						  if($scope.ngModel.mailTo.indexOf(arrChip[i])!=-1){
							  duplicateEmail++;
						  }else{
							  $scope.ngModel.mailTo.push(arrChip[i]);							  
						  }
					  }else{ 
						  errEmail++;
					  }
				  }
				  
				  if(errEmail>0){
					  $scope.ngModel.mailTo.push(errEmail+" "+errorEmailMessages);
					  $timeout(function(){ 
						  var index=$scope.ngModel.mailTo.indexOf(errEmail+" "+errorEmailMessages);
						  $scope.ngModel.mailTo.splice(index,1);
						  },1000)  
				  }
				  if(duplicateEmail>0){
					  $scope.ngModel.mailTo.push(duplicateEmail+" "+duplicatedEmailMessages);
					  $timeout(function(){ 
						  var index=$scope.ngModel.mailTo.indexOf(duplicateEmail+" "+duplicatedEmailMessages);
						  $scope.ngModel.mailTo.splice(index,1);
					  },1000)  
				  }
				  
				  },1000)
				  
			  return multipleEmailMessages;
		 }else{
			 //single email added
			 if(re.test(chip)){
				  return chip;
			  }else{
				  
				  $timeout(function(){ 
					  var index=$scope.ngModel.mailTo.indexOf(errorEmailMessages);
					    $scope.ngModel.mailTo.splice(index,1); 
					  },1000)
				  return errorEmailMessages;
			  }
		 }
		  
		  
		  
		 
	  }
	});