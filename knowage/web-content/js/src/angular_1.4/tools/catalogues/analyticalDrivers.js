var app = angular.module("AnalyticalDriversModule",["ngMaterial","angular_list","angular_table","sbiModule","angular_2_col","toastr"]);
app.controller("AnalyticalDriversController",["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout","toastr",AnalyticalDriversFunction]);
function AnalyticalDriversFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout,toastr){
	
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
	$scope.associatedRoles=[]; // temp array that hold selected object roles list
	$scope.associatedChecks=[]; // temp array that hold selected object checks list
	$scope.checksList = []; // array that hold checks list
	$scope.useModeList= []; // array that hold use mode objects list
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

	$scope.useModeInit = function(){
		$scope.selectedParUse.idLov= -1;
		$scope.selectedParUse.idLovForDefault= -1;
		$scope.selectedParUse.selectionType= null;
		$scope.selectedParUse.defaultFormula = null;
		$scope.selectedParUse.valueSelection = "man_in";
		$scope.selectedParUse.maximizerEnabled = true;
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
		sbiModule_restServices.promiseGet("2.0", "analyticalDrivers")
		.then(function(response) {
			$scope.adList = response.data;
		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
	}
	
	$scope.getLayers = function(){ // service that gets list of layers @GET
		sbiModule_restServices.promiseGet("2.0/analyticalDrivers/layers/", "")
		.then(function(response) {
			$scope.layersList = response.data;
		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
	}
	
	$scope.getRoles = function () { // service that gets list of roles @GET
		sbiModule_restServices.promiseGet("2.0", "roles")
		.then(function(response) {
			$scope.rolesList = response.data;
		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
    }
	
	$scope.getChecks = function () { // service that gets list of checks @GET
		sbiModule_restServices.promiseGet("2.0/analyticalDrivers/checks/", "")
		.then(function(response) {
			$scope.checksList = response.data;
		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
    }
	
	$scope.getDomainType = function(){ // service that gets driver types for dropdown @GET
		sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=PAR_TYPE")
		.then(function(response) {
			$scope.listType = response.data;
		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
	}
	
	$scope.getSelTypes = function(){ // service that gets lovs selection types for dropdown @GET
		sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=SELECTION_TYPE")
		.then(function(response) {
			$scope.listSelType = response.data;
		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
	}
	
	$scope.getLovDates = function(){ // service that gets list of lovs @GET
		sbiModule_restServices.promiseGet("2.0", "lovs")
		.then(function(response) {
			$scope.listDate = response.data;
		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
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
     * 	this functions are used to properly format
     *   objects for saving													
     */
	$scope.formatDriver = function() {
		$scope.selectedDriver.length = 0; // length of what??
		for ( var l in $scope.listType) {
			
			if ($scope.selectedDriver.type == $scope.listType[l].VALUE_CD) {
				$scope.selectedDriver.typeId = $scope.listType[l].VALUE_ID;
			}
			}
	}
	$scope.formatUseMode = function() {
		$scope.selectedParUse.manualInput = ($scope.selectedParUse.valueSelection == 'man_in') ? 1 : 0;
		$scope.selectedDriver.valueSelection = $scope.selectedParUse.valueSelection;
		$scope.selectedParUse.id = $scope.selectedDriver.id;
		$scope.selectedParUse.associatedRoles = $scope.associatedRoles;
		$scope.selectedParUse.associatedChecks = $scope.associatedChecks;
		$scope.selectedParUse.idLov = ($scope.selectedParUse.idLov === null) ? -1 : parseInt($scope.selectedParUse.idLov);
		$scope.selectedParUse.idLovForDefault = ($scope.selectedParUse.idLovForDefault === null) ? -1 : parseInt($scope.selectedParUse.idLovForDefault);
		switch ($scope.selectedParUse.defaultrg) {
		case "none":
			$scope.selectedParUse.defaultFormula = null;
			$scope.selectedParUse.idLovForDefault = -1;
			break;
		case "lov":
			$scope.selectedParUse.defaultFormula = null;
			break;
		case "pickup":
			$scope.selectedParUse.idLovForDefault = -1;
			break;
		default:
			break;
		}
		delete $scope.selectedParUse.defaultrg;
	}
	 
	$scope.save= function(){  // this function is called when clicking on save button
		
		var saveDriver = function() {
			$scope.formatDriver();
			
			if($scope.selectedDriver.hasOwnProperty("id")){ // if item already exists do update @PUT
				
				sbiModule_restServices.promisePut("2.0/analyticalDrivers",$scope.selectedDriver.id, $scope.selectedDriver)
				.then(function(response) {
					$scope.adList=[];
					$timeout(function(){								
						$scope.getDrivers();
					}, 1000);
					toastr.success(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
					$scope.selectedDriver={};
					$scope.selectedTab = 0;
					$scope.showme=false;
					$scope.showadMode = false;
					$scope.dirtyForm=false;	
					
				}, function(response) {
					toastr.error(response.data.errors[0].message, 'Error');
					
				});			
							
			}else{ // create new item in database @POST
				sbiModule_restServices.promisePost("2.0/analyticalDrivers","",angular.toJson($scope.selectedDriver))
				.then(function(response) {
					$scope.adList=[];
					$timeout(function(){								
						$scope.getDrivers();
					}, 1000);
					toastr.success(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
					$scope.selectedDriver={};
					$scope.selectedTab = 0;
					$scope.showme=false;
					$scope.showadMode = false;
					$scope.dirtyForm=false;	
					
				}, function(response) {
					toastr.error(response.data.errors[0].message, 'Error');
					
				});	
				
			}
		}
		var saveUseMode= function(){  // this function is called when clicking on save button
			$scope.formatUseMode();
			if($scope.selectedParUse.hasOwnProperty("useID")){ // if item already exists do update @PUT	
				sbiModule_restServices.promisePut("2.0/analyticalDrivers/modes",$scope.selectedParUse.useID , $scope.selectedParUse)
				.then(function(response) {
					$scope.useModeList=[];
					$timeout(function(){								
						$scope.getUseModesById($scope.selectedDriver);
					}, 1000);
					toastr.success(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
					$scope.selectedTab = 0;
					$scope.showme=false;
					$scope.showadMode = false;
					$scope.dirtyForm=false;	
					
				}, function(response) {
					toastr.error(response.data.errors[0].message, 'Error');
					
				});	
				
			}else{ // create new item in database @POST
				sbiModule_restServices.promisePost("2.0/analyticalDrivers/modes","",angular.toJson($scope.selectedParUse))
				.then(function(response) {
					$scope.useModeList=[];
					$timeout(function(){								
						$scope.getUseModesById($scope.selectedDriver);
					}, 1000);
					toastr.success(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
					$scope.selectedParUse={};
					$scope.selectedTab = 0;
					$scope.showme=false;
					$scope.showadMode = false;
					$scope.dirtyForm=false;
					
				}, function(response) {
					toastr.error(response.data.errors[0].message, 'Error');
					
				});	
			}
		}
		
		if($scope.selectedTab==0){
			console.log("SAVING DRIVER");
			console.log($scope.selectedDriver);
			saveDriver();
		}else {
			console.log("SAVING USEMODE");
			console.log($scope.selectedParUse);
			saveUseMode();
		}
		
	}
	
	$scope.deleteDrivers = function(item){
		// this function is called when clicking on delete button
		sbiModule_restServices.promiseDelete("2.0/analyticalDrivers",item.id)
		.then(function(response) {
			$scope.adList=[];
			$timeout(function(){								
				$scope.getDrivers();
				$scope.useModeList=[];
			}, 1000);
			toastr.success(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
			$scope.selectedDriver={};
			$scope.selectedTab = 0;
			$scope.showme=false;
			$scope.showadMode = false;
			$scope.dirtyForm=false;

		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
	}
	
	// this function is called when item from use mode table is clicked
	$scope.loadUseMode=function(item){
		$scope.associatedRoles = item.associatedRoles;
		$scope.associatedChecks = item.associatedChecks;
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
	
	/*
	 * this set of functions handle getting and setting of roles
	 *  and checks checkboxes TODO when save set temp arrays to object fields
	 */ 
	$scope.checkCheckboxes = function (item, list) {
		if(item.hasOwnProperty("id")){
			var index = $scope.indexInList(item, list);

			if(index != -1){
				$scope.associatedRoles.splice(index,1);
			}else{
				$scope.associatedRoles.push(item);
			}
		} 
		if (item.hasOwnProperty("checkId")) {
			var index = $scope.indexInList(item, list);

			if(index != -1){
				$scope.associatedChecks.splice(index,1);
			}else{
				$scope.associatedChecks.push(item);
			}
		}
	};
	
	$scope.getCheckboxes = function(item,list) {
		
		return  $scope.indexInList(item, list)>-1;
	}
	$scope.indexInList=function(item, list) {
		if(item.hasOwnProperty("id")){
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.id==item.id){
				return i;
			}
		}
		}
		if(item.hasOwnProperty("checkId")){
			for (var i = 0; i < list.length; i++) {
				var object = list[i];
				if(object.checkId==item.checkId){
					return i;
				}
			}
			}
		return -1;
	}
	
	$scope.disableSelectedRoles = function( item ) {
		
	}
	
	// this function is called when clicking on plus button in use mode table
	$scope.createUseModes =function(){ 
		$scope.selectedParUse = {};
		$scope.associatedRoles= [];
		$scope.associatedChecks= [];
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
		sbiModule_restServices.promiseGet("2.0/analyticalDrivers/"+item.id+"/modes", "")
		.then(function(response) {
			$scope.useModeList = response.data;
		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
    }
	
	$scope.deleteUseMode = function(item){ // this function is called when clicking on delete button
		sbiModule_restServices.promiseDelete("2.0/analyticalDrivers/modes", item.useID)
		.then(function(response) {
			$scope.useModeList=[];
			$timeout(function(){								
				$scope.getUseModesById($scope.selectedDriver);
			}, 1000);
			toastr.success(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
			$scope.selectedParUse = {};
			$scope.selectedTab = 0;
			$scope.showme=false;
			$scope.showadMode = false;
			$scope.dirtyForm=false;

		}, function(response) {
			toastr.error(response.data.errors[0].message, 'Error');
			
		});
	}
};
