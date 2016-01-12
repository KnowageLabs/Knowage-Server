var app = angular.module("AnalyticalDriversModule",["ngMaterial","angular_list","angular_table","sbiModule","angular_2_col"]);
app.controller("AnalyticalDriversController",["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout",AnalyticalDriversFunction]);
function AnalyticalDriversFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout){
	
	//VARIABLES
	
	$scope.showme = false; // flag for showing right side 
	$scope.showadMode = false; // flag that shows use mode details
	$scope.dirtyForm = false; // flag to check for modification
	$scope.translate = sbiModule_translate;
	$scope.selectedDriver = {}; // main item
	$scope.selectedParUse = {}; // main item
	$scope.adList = []; // array that hold custom list
	$scope.listType = [];
	$scope.listDate = [];
	$scope.listSelType = [];
	$scope.layersList = [];
	$scope.rolesList = [];
	$scope.checksList = [];
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
		 
		 $scope.adSpeedMenu= [
		                         {
		                            label:sbiModule_translate.load("sbi.generic.delete"),
		                            icon:'fa fa-trash-o fa-lg',
		                            color:'#153E7E',
		                            action:function(item,event){
		                                
		                            	$scope.deleteDrivers(item);
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
				$scope.getDrivers();
				$scope.getDomainType();
				$scope.getLovDates();
				$scope.getSelTypes();
				$scope.getLayers();
				$scope.getRoles();
				$scope.getChecks();
				
		    });
	
	$scope.setDirty=function(){ 
		  $scope.dirtyForm=true;
	}
	
	$scope.driverInit = function(){
		$scope.selectedDriver.type = "DATE";
		$scope.selectedDriver.functional = true;
	}
		
	$scope.loadDrivers=function(item){  // this function is called when item from custom table is clicked
		$scope.showadMode = true;
		 if($scope.dirtyForm){
			   $mdDialog.show($scope.confirm).then(function(){
				$scope.dirtyForm=false;   
				$scope.selectedDriver=angular.copy(item);
				$scope.showme=true;
				$scope.showadMode = true;
			           
			   },function(){
			    
				$scope.showme = true;
				$scope.showadMode = true;
			   });
			   
			  }else{
			 
			  $scope.selectedDriver=angular.copy(item);
			  $scope.showme=true;
			  $scope.showadMode = true;
			  }
	} 	                
	
	$scope.cancel = function() { // on cancel button
		$scope.selectedDriver={};
		$scope.showme = false;
		$scope.showadMode = false;
		$scope.dirtyForm=false;
	}

	$scope.createDrivers =function(){ // this function is called when clicking on plus button
		$scope.selectedDriver = {};
		 if($scope.dirtyForm){
			   $mdDialog.show($scope.confirm).then(function(){
				$scope.dirtyForm=false;
				$scope.driverInit();
				$scope.showme=true;
				$scope.showadMode = false;
			           
			   },function(){
			    
				$scope.showme = true;
				$scope.showadMode = false;
			   });
			   
			  }else{
			   $scope.driverInit();
			   $scope.showme=true;
			   $scope.showadMode = false;
			  }
	}
	

	$scope.getDrivers = function(){ // service that gets user created list GET
		sbiModule_restServices.get("2.0", "analyticalDrivers").success(
				function(data, status, headers, config) {
					
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.adList = data;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
	
	$scope.getLayers = function(){ // service that gets user created list GET
		sbiModule_restServices.get("2.0/analyticalDrivers/layers/", "").success(
				function(data, status, headers, config) {
					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.layersList = data;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
	
	$scope.getRoles = function () { // service that gets list of roles GET
        sbiModule_restServices.get("2.0", "roles").success(
            function (data, status, headers, config) {
                if (data.hasOwnProperty("errors")) {
                    console.log(sbiModule_translate.load("sbi.glossary.load.error"));
                } else {
                	$scope.rolesList = data;

                }
            }).error(function (data, status, headers, config) {
            console.log(sbiModule_translate.load("sbi.glossary.load.error"));

        })
    }
	
	$scope.getChecks = function () { // service that gets list of roles GET
        sbiModule_restServices.get("2.0/analyticalDrivers/checks/", "").success(
            function (data, status, headers, config) {
                if (data.hasOwnProperty("errors")) {
                    console.log(sbiModule_translate.load("sbi.glossary.load.error"));
                } else {
                	$scope.checksList = data;

                }
            }).error(function (data, status, headers, config) {
            console.log(sbiModule_translate.load("sbi.glossary.load.error"));

        })
    }
	
	$scope.getDomainType = function(){ // service that gets domain types for dropdown GET
		sbiModule_restServices.get("domains", "listValueDescriptionByType","DOMAIN_TYPE=PAR_TYPE").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.listType = data;
			
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
	
	$scope.getSelTypes = function(){ // service that gets domain types for dropdown GET
		sbiModule_restServices.get("domains", "listValueDescriptionByType","DOMAIN_TYPE=SELECTION_TYPE").success(
				function(data, status, headers, config) {
					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.listSelType = data;
			
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
	
	$scope.getLovDates = function(){ // service that gets domain types for dropdown GET
		sbiModule_restServices.get("2.0", "lovs").success(
				function(data, status, headers, config) {
					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.listDate = data;
			
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
	
	$scope.formatDriver = function() {
		$scope.selectedDriver.length = 0; // length of what??
		for ( var l in $scope.listType) {
		
		if ($scope.selectedDriver.type == $scope.listType[l].VALUE_CD) {
			$scope.selectedDriver.typeId = $scope.listType[l].VALUE_ID;
		}
		}
	}
	
	$scope.saveDrivers= function(){  // this function is called when clicking on save button
		$scope.formatDriver();
		console.log($scope.selectedDriver);
		if($scope.selectedDriver.hasOwnProperty("id")){ // if item already exists do update PUT
			
			sbiModule_restServices
		    .put("2.0/analyticalDrivers",$scope.selectedDriver.id, $scope.selectedDriver).success(
					function(data, status, headers, config) {
						
						if (data.hasOwnProperty("errors")) {
							console.log(sbiModule_translate.load("sbi.glossary.load.error"));
						} else {
							$scope.adList=[];
							$timeout(function(){								
								$scope.getDrivers();
							}, 1000);
							$scope.showActionOK(sbiModule_translate.load("sbi.catalogues.toast.updated"));
							$scope.selectedDriver={};
							$scope.showme=false;
							$scope.showadMode = false;
							$scope.dirtyForm=false;	
						}
					}).error(function(data, status, headers, config) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));

					})	
			
		}else{ // create new item in database POST
			console.log($scope.selectedDriver);
			sbiModule_restServices
		    .post("2.0/analyticalDrivers","",angular.toJson($scope.selectedDriver)).success(
					function(data, status, headers, config) {
						
							$scope.adList=[];
							$timeout(function(){								
								$scope.getDrivers();
							}, 1000);
							$scope.showActionOK(sbiModule_translate.load("sbi.catalogues.toast.created"));
							$scope.selectedDriver={};
							$scope.showme=false;
							$scope.showadMode = false;
							$scope.dirtyForm=false;
						
					}).error(function(data, status, headers, config) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));

					})	
		}
	}

	$scope.deleteDrivers = function(item){ // this function is called when clicking on delete button
		sbiModule_restServices.delete("2.0/analyticalDrivers",item.id).success(
				function(data, status, headers, config) {
					
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.adList=[];
						$timeout(function(){								
							$scope.getDrivers();
						}, 1000);
						$scope.showActionOK(sbiModule_translate.load("sbi.catalogues.toast.deleted"));
						$scope.selectedDriver={};
						$scope.showme=false;
						$scope.showadMode = false;
						$scope.dirtyForm=false;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
};
