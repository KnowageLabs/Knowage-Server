
var app = angular.module("ModalitiesCheckModule",["ngMaterial","angular_list","angular_table","sbiModule","angular_2_col","toastr"])
app.controller("ModalitiesCheckController",ModalitiesCheckFunction);
ModalitiesCheckFunction.$inject = ["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout","toastr"];
function ModalitiesCheckFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout,toastr){
	
	//VARIABLES
	
	$scope.showme = false; // flag for showing right side 
	$scope.showpred = false; // flag to show read only predefined details
	$scope.additionalField = false;
	$scope.dirtyForm = false; // flag to check for modification
	$scope.translate = sbiModule_translate;
	$scope.SelectedConstraint = {}; // main item
	$scope.PredefinedItem = {}; // predefined item
	$scope.PredefinedList = []; // array that hold predefined list
	$scope.label = "";
	$scope.ItemList = []; // array that hold custom list
	$scope.listType = []; // array that hold dropdown list from domain
			 
		 $scope.ccSpeedMenu= [
		                         {
		                            label:sbiModule_translate.load("sbi.generic.delete"),
		                            icon:'fa fa-trash-o fa-lg',
		                            color:'#153E7E',
		                            action:function(item,event){
		                                
		                            	$scope.deleteConstraint(item);
		                            }
		                         }
		                        ];
		 
		 
		 $scope.confirm = $mdDialog
		    .confirm()
		    .title(sbiModule_translate.load("sbi.catalogues.generic.modify"))
		    .content(
		            sbiModule_translate
		            .load("sbi.catalogues.generic.modify.msg"))
		            .ariaLabel('toast').ok(
		                    sbiModule_translate.load("sbi.general.continue")).cancel(
		                            sbiModule_translate.load("sbi.general.cancel"));
 
		 
		 
		
	 
	//FUNCTIONS	
		 
	angular.element(document).ready(function () { // on page load function
				$scope.getPredefined();
				$scope.getCustom();
				$scope.getDomainType();
		    });
	
	$scope.setDirty=function(){ 
		  $scope.dirtyForm=true;
	}
	
	$scope.clearRight=function(index){
		 if($scope.dirtyForm){
			   $mdDialog.show($scope.confirm).then(function(){
				$scope.dirtyForm=false;   
				$scope.showme = false;
				$scope.showpred = false;
			           
			   },function(){
			    
				   $scope.showme = false;
				   $scope.showpred = false;
			   });
			   
			  }else{
			 
				  $scope.showme = false;
				  $scope.showpred = false;
			  }
		
		
		
	}
	
	$scope.loadConstraints=function(item){  // this function is called when item from custom table is clicked
		console.log(item);
		$scope.changeType(item.valueTypeCd)
		 if($scope.dirtyForm){
			   $mdDialog.show($scope.confirm).then(function(){
				$scope.dirtyForm=false;   
				$scope.SelectedConstraint=angular.copy(item);
				$scope.showme=true;
				$scope.showpred = false;
				
				
			   },function(){
			    
				$scope.showme = true;
				$scope.showpred = false;
			   });
			   
			  }else{
			 
			  $scope.SelectedConstraint=angular.copy(item);
			  $scope.showme=true;
			  $scope.showpred = false;
			 
			  }
	} 	                
	
	$scope.cancel = function() { // on cancel button
		$scope.SelectedConstraint={};
		$scope.showme = false;
		$scope.dirtyForm=false;
		$scope.showpred = false;
		

	}
	
	
	$scope.loadPredefined=function(item){  // this function is called when item from predefined table is clicked
		$scope.showme = false;
		$scope.PredefinedItem=item;
		$scope.showpred=true;
	} 	                
	
	$scope.createConstraints =function(){ // this function is called when clicking on plus button
		 if($scope.dirtyForm){
			   $mdDialog.show($scope.confirm).then(function(){
				$scope.dirtyForm=false;   
				$scope.SelectedConstraint={};
				$scope.showme=true;
			    $scope.label = "";
			           
			   },function(){
			    
				$scope.showme = true;
			   });
			   
			  }else{
			 
			$scope.SelectedConstraint={};
			  $scope.showme=true;
			  }
	}
	
	$scope.saveConstraints= function(){  // this function is called when clicking on save button
		if($scope.SelectedConstraint.hasOwnProperty("checkId")){ // if item already exists do update PUT
			
			sbiModule_restServices.promisePut("2.0/customChecks",$scope.SelectedConstraint.checkId,$scope.SelectedConstraint)
			.then(function(response) {
				$scope.ItemList=[];
				$timeout(function(){								
					$scope.getCustom();
				}, 1000);
				toastr.success(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
				$scope.SelectedConstraint={};
				$scope.showme=false;
				$scope.dirtyForm=false;
				
			}, function(response) {
				toastr.error(response.data.errors[0].message, 'Error');
				
			});	
			
		}else{ // create new item in database POST
			console.log($scope.SelectedConstraint);
			sbiModule_restServices.promisePost("2.0/customChecks","",angular.toJson($scope.SelectedConstraint))
			.then(function(response) {
				$scope.ItemList=[];
				$timeout(function(){								
					$scope.getCustom();
				}, 1000);
				toastr.success(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
				$scope.SelectedConstraint={};
				$scope.showme=false;
				$scope.dirtyForm=false;
				
			}, function(response) {
				toastr.error(response.data.errors[0].message, 'Error');
				
			});			
		}
	}
	
	/*
     * 	function that adds VALUE_TR property to each Domain Type
     *  object because of internalization																	
     */
	 $scope.addTranslation = function() {
			
    	 for ( var l in $scope.listType) {
 			switch ($scope.listType[l].VALUE_CD) {
 			case "DATE":
 			$scope.listType[l].VALUE_TR = sbiModule_translate.load("sbi.modalities.check.details.date");
 			break;	
 			case "REGEXP":
 			$scope.listType[l].VALUE_TR = sbiModule_translate.load("sbi.modalities.check.details.regexp");
 			break;	
 			case "MAXLENGTH":
 			$scope.listType[l].VALUE_TR = sbiModule_translate.load("sbi.modalities.check.details.max");
 			break;	
 			case "RANGE":
 			$scope.listType[l].VALUE_TR = sbiModule_translate.load("sbi.modalities.check.details.range");
 			break;	
 			case "DECIMALS":
 			$scope.listType[l].VALUE_TR = sbiModule_translate.load("sbi.modalities.check.details.decimal");
 			break;
 			case "MINLENGTH":
 	 		$scope.listType[l].VALUE_TR = sbiModule_translate.load("sbi.modalities.check.details.min");
 	 		break;	
 			default:
 			break;
 			}
 		}
	 }
	 
	$scope.changeType = function(item) {
		 console.log(item);
			for (var i = 0; i < $scope.listType.length; i++) {
				if($scope.listType[i].VALUE_CD == item){
					$scope.SelectedConstraint.valueTypeId=$scope.listType[i].VALUE_ID;
					$scope.label = $scope.listType[i].VALUE_TR;
				}
			}
			 if ($scope.SelectedConstraint.valueTypeCd == "RANGE") {
				 $scope.additionalField= true;
				}else{
				$scope.additionalField= false;
				}
			
		}
	
	$scope.getPredefined = function(){ // service that gets predefined list GET		
		sbiModule_restServices.promiseGet("2.0", "predefinedChecks")
		.then(function(response) {
			$scope.PredefinedList = response.data;
		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});	
	}
	
	
	$scope.getCustom = function(){ // service that gets user created list GET
		sbiModule_restServices.promiseGet("2.0", "customChecks")
		.then(function(response) {
			$scope.ItemList = response.data;
		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
	}
			
	$scope.getDomainType = function(){ // service that gets domain types for dropdown GET
		sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=CHECK")
		.then(function(response) {
			$scope.listType = response.data;
			$scope.addTranslation();
		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
	}
	
	$scope.deleteConstraint = function(item){ // this function is called when clicking on delete button
		sbiModule_restServices.promiseDelete("2.0/customChecks",item.checkId)
		.then(function(response) {
			$scope.ItemList=[];
			$timeout(function(){								
				$scope.getCustom();
			}, 1000);
			toastr.success(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
			$scope.SelectedConstraint={};
			$scope.showme=false;
			$scope.dirtyForm=false;

		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
	}

};


