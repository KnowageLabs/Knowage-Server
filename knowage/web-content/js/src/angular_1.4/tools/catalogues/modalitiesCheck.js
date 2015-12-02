var app=angular.module("ModalitiesCheckModule",["ngMaterial","angular_list","angular_table","sbiModule","angular_2_col"]);
app.controller("ModalitiesCheckController",["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast",ModalitiesCheckFunction]);
function ModalitiesCheckFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast){
	
	//VARIABLES
	
	$scope.showme = false; // flag for showing right side 
	$scope.additionalField= false;
	$scope.dirtyForm=false; // flag to check for modification
	$scope.translate=sbiModule_translate;
	$scope.SelectedConstraint={}; // main item
	$scope.predefined =[]; // array that hold predefined list
	$scope.label="";
	$scope.ItemList=[]; // array that hold custom list
	$scope.listType=[]; // array that hold dropdown list from domain
	$scope.showActionOK = function(msg) {
		  var toast = $mdToast.simple() 
		  .content(msg)
		  .action('OK')
		  .highlightAction(false)
		  .hideDelay(3000)
		  .position('top')

		  $mdToast.show(toast).then(function(response) {

		   if ( response == 'ok' ) {


		   }
		  });
		 };
		 
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
		$scope.setDirty=function(){
			  $scope.dirtyForm=true;
			 }
	 
	//FUNCTIONS	
		 
	angular.element(document).ready(function () {
				$scope.getPredefined();
				$scope.getCustom();
				$scope.getDomainType();
		    });
	 

	$scope.loadConstraints=function(item){  // this function is called when item is loaded on right side
		$scope.SelectedConstraint=angular.copy(item);
		$scope.showme=true;
		$scope.label = "";
	} 	                
	
	
	
	$scope.createConstraints =function(){ // this function is called when clicking on plus button
		$scope.SelectedConstraint={};
		$scope.showme=true;
	}
	
	$scope.checkRequired = function(){
		if($scope.SelectedConstraint.label.length>0){
			return true;
		}
	}
	
	$scope.saveConstraints= function(){  // this function is called when clicking on save button
		
		if($scope.SelectedConstraint.hasOwnProperty("checkId")){ // if item already exists do update PUT
			
			sbiModule_restServices
		    .put("2.0/customChecks",$scope.SelectedConstraint.checkId,$scope.SelectedConstraint).success(
					function(data, status, headers, config) {
						console.log(data);
						if (data.hasOwnProperty("errors")) {
							console.log(sbiModule_translate.load("sbi.glossary.load.error"));
						} else {
							$scope.ItemList=data;
							$scope.showActionOK("Item updated successfully");
							$scope.showme = false;
							
							
						}
					}).error(function(data, status, headers, config) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));

					})	
			
		}else{ // create new item in database POST
			console.log($scope.SelectedConstraint);
			sbiModule_restServices
		    .post("2.0/customChecks","",angular.toJson($scope.SelectedConstraint)).success(
					function(data, status, headers, config) {
						console.log(data);
						if (data.hasOwnProperty("errors")) {
							console.log(sbiModule_translate.load("sbi.glossary.load.error"));
						} else {
							$scope.ItemList=data;
							$scope.showActionOK("Item created successfully");
							$scope.showme = false;
							
							
						}
					}).error(function(data, status, headers, config) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));

					})	
			
			
		}
		
	}
	$scope.cancel = function() { // on cancel button
		$scope.showme = false;
		$scope.SelectedConstraint={};
		

	}
	

	
	$scope.FieldsCheck = function(l){ // function that checks if field is necessary and assigns few values to main item on click
		
		$scope.label = l.VALUE_DS;
		$scope.SelectedConstraint.valueTypeId=l.VALUE_ID;
	 $scope.SelectedConstraint.valueTypeCd=l.VALUE_CD;
		if(l.VALUE_NM == "Range"){
			$scope.additionalField= true;
		}else{
			$scope.additionalField= false;
		}
	}
	$scope.getPredefined = function(){ // service that gets predefined list GET
		sbiModule_restServices.get("2.0", "predefinedChecks").success(
				function(data, status, headers, config) {
					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.predefined = data;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
	
	
	$scope.getCustom = function(){ // service that gets user created list GET
		sbiModule_restServices.get("2.0", "customChecks").success(
				function(data, status, headers, config) {
					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.ItemList = data;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
			
	
	$scope.getDomainType = function(){ // service that gets domain types for dropdown GET
		sbiModule_restServices.get("domains", "listValueDescriptionByType","DOMAIN_TYPE=CHECK").success(
				function(data, status, headers, config) {
					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						console.log(data);
						$scope.listType = data;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
	
	$scope.deleteConstraint = function(item){ // service that gets domain types for dropdown GET
		sbiModule_restServices.delete("2.0/customChecks",item.checkId).success(
				function(data, status, headers, config) {
					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.ItemList=data;
						$scope.showActionOK("Item deleted successfully");
						$scope.showme = false;
						
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
};
