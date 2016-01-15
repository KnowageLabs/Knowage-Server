var app = angular.module("AnalyticalDriversModule",["ngMaterial","angular_list","angular_table","sbiModule","angular_2_col"]);
app.controller("AnalyticalDriversController",["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout",AnalyticalDriversFunction]);
function AnalyticalDriversFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout){
	
	//VARIABLES
	
	$scope.showme = false; // flag for showing right side 
	$scope.showadMode = false; // flag that shows use mode details
	$scope.dirtyForm = false; // flag to check for modification
	$scope.translate = sbiModule_translate;
	$scope.selectedDriver = {}; // main item
	$scope.selectedParUse = {}; // main use mode item
	$scope.selectedTab = 0; // selected tab in interface
	$scope.adList = []; // array that hold drivers list
	$scope.listType = []; // array that hold driver types
	$scope.listDate = []; // array that hold lovs list
	$scope.listSelType = []; // array that hold list of ways to show lov
	$scope.layersList = []; // array that hold layers list
	$scope.rolesList = [];  // array that hold roles list
	$scope.role = [];
	$scope.checksList = []; // array that hold checks list
	$scope.useModeList= []; // array that hold use mode objects list
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
		 //speed menus for the tables   
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
		 $scope.dumSpeedMenu= [
		                         {
		                            label:sbiModule_translate.load("sbi.generic.delete"),
		                            icon:'fa fa-trash-o fa-lg',
		                            color:'#153E7E',
		                            action:function(item,event){
		                                
		                            	$scope.deleteUseMode(item);
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
	// functions that inits preselected fields  for objects
	$scope.driverInit = function(){
		$scope.selectedDriver.type = "DATE";
		$scope.selectedDriver.functional = true;
	}
	//TODO delete defaultrg property before saving
	$scope.useModeInit = function(){
		$scope.selectedParUse.valueSelection = "man_in";
		$scope.selectedParUse.defaultrg="none";
		
	}
	// function that handles changing of tabs in different cases
	 $scope.changeTab = function(item) {
		 if (item.type != null) {
	            $scope.selectedTab = 0;
	        }else{
	        	$scope.selectedTab = 1;
	        }
	       
	        
	    }
		
	$scope.loadDrivers=function(item){  // this function is called when item from custom table is clicked
		$scope.showadMode = true;
		$scope.getUseModesById(item);
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
		 $scope.changeTab(item);
	}
	
	$scope.cancel = function() { // on cancel button
		$scope.selectedDriver={};
		$scope.showme = false;
		$scope.showadMode = false;
		$scope.dirtyForm=false;
		$scope.selectedTab = 0;
	}
	// this function is called when clicking on drivers table plus button
	$scope.createDrivers =function(){ 
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
		 $scope.selectedTab = 0;
	}
	

	$scope.getDrivers = function(){ // service that gets list of drivers @GET
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
	
	$scope.getLayers = function(){ // service that gets list of layers @GET
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
	
	$scope.getRoles = function () { // service that gets list of roles @GET
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
	
	$scope.getChecks = function () { // service that gets list of checks @GET
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
	
	$scope.getDomainType = function(){ // service that gets driver types for dropdown @GET
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
	
	$scope.getSelTypes = function(){ // service that gets lovs selection types for dropdown @GET
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
	
	$scope.getLovDates = function(){ // service that gets list of lovs @GET
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
	//object to populate one of lovs dropdowns TODO maybe some service gets it?? 
	$scope.defaultFormula = [{
	   	 "f_value": "FIRST",
		  "name": "Main lov's first item"	 
	}, 
	{
		 "f_value": "LAST",
		  "name": "Main lov's last item"	 
	}]
	                         
	                     
	               
	/*
     * 	this function is used to properly format
     *  driver object for saving													
     */
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
		if($scope.selectedDriver.hasOwnProperty("id")){ // if item already exists do update @PUT
			
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
							$scope.selectedTab = 0;
							$scope.showme=false;
							$scope.showadMode = false;
							$scope.dirtyForm=false;	
						}
					}).error(function(data, status, headers, config) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));

					})	
			
		}else{ // create new item in database @POST
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
							$scope.selectedTab = 0;
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
						$scope.selectedTab = 0;
						$scope.showme=false;
						$scope.showadMode = false;
						$scope.dirtyForm=false;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
	// this function is called when item from use mode table is clicked
	$scope.loadUseMode=function(item){
		$scope.selectedParUse.defaultrg= null;
		 if($scope.dirtyForm){
			   $mdDialog.show($scope.confirm).then(function(){
				$scope.dirtyForm=false;   
				$scope.selectedParUse=angular.copy(item);
				$scope.showme=true;
				$scope.showadMode = true;
			           
			   },function(){
			    
				$scope.showme = true;
				$scope.showadMode = true;
			   });
			   
			  }else{
			 
			  $scope.selectedParUse=angular.copy(item);
			  $scope.showme=true;
			  $scope.showadMode = true;
			  }
		 $scope.changeTab(item);
		 $scope.setParUse();
	}
	// this function properly checks radio buttons
	$scope.setParUse = function () {		
	if($scope.selectedParUse.defaultFormula == null){
		$scope.selectedParUse.defaultrg = "none";
	}else{
		$scope.selectedParUse.defaultrg = "pickup";
	}
	if($scope.selectedParUse.idLovForDefault != null){
		$scope.selectedParUse.defaultrg = "lov";
	}	
	}
	
	$scope.setRoles = function(item,list) {
		
		return  $scope.indexInList(item, list)>-1;
		
	}
	
	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.id==item.id){
				return i;
			}
		}

		return -1;
	};
	// this function is called when clicking on plus button in use mode table
	$scope.createUseModes =function(){ 
		$scope.selectedParUse = {};
		 if($scope.dirtyForm){
			   $mdDialog.show($scope.confirm).then(function(){
				$scope.dirtyForm=false;
				$scope.useModeInit();
				$scope.showme=true;
				$scope.showadMode = true;
			           
			   },function(){
			    
				$scope.showme = true;
				$scope.showadMode = true;
			   });
			   
			  }else{
			   $scope.showme=true;
			   $scope.useModeInit();
			   $scope.showadMode = true;
			  }
		 $scope.selectedTab = 1;
	}
	// service that gets list of use modes for selected driver @GET
	$scope.getUseModesById = function (item) { 
        sbiModule_restServices.get("2.0/analyticalDrivers/"+item.id+"/modes", "").success(
            function (data, status, headers, config) {
                if (data.hasOwnProperty("errors")) {
                    console.log(sbiModule_translate.load("sbi.glossary.load.error"));
                } else {
                	$scope.useModeList = data;

                }
            }).error(function (data, status, headers, config) {
            console.log(sbiModule_translate.load("sbi.glossary.load.error"));

        })
    }
	$scope.deleteUseMode = function(item){ // this function is called when clicking on delete button
		sbiModule_restServices.delete("2.0/analyticalDrivers/modes", item.useID).success(
				function(data, status, headers, config) {
					
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.useModeList=[];
						$timeout(function(){								
							$scope.getUseModesById($scope.selectedDriver);
						}, 1000);
						$scope.showActionOK(sbiModule_translate.load("sbi.catalogues.toast.deleted"));
						$scope.selectedParUse = {};
						$scope.selectedTab = 0;
						$scope.showme=false;
						$scope.showadMode = false;
						$scope.dirtyForm=false;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
};
