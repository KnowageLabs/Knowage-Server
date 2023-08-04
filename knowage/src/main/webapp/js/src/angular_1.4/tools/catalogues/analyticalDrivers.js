var app = angular.module("AnalyticalDriversModule",["ngMaterial", "ngMessages", "angular_list","angular_table","sbiModule","angular_2_col","angular-list-detail", 'angularXRegExp']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);
app.controller("AnalyticalDriversController",["sbiModule_translate","sbiModule_restServices", "kn_regex", "$scope","$mdDialog","$mdToast","$timeout","sbiModule_messaging","sbiModule_config","sbiModule_user",AnalyticalDriversFunction]);

function AnalyticalDriversFunction(sbiModule_translate, sbiModule_restServices, kn_regex, $scope, $mdDialog, $mdToast,$timeout,sbiModule_messaging,sbiModule_config,sbiModule_user){

	//VARIABLES
	$scope.regex = kn_regex;
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

	var showMapDriver = sbiModule_user.functionalities.indexOf("MapDriverManagement")>-1;

    $scope.valueSelectionRadioGroup = [
	                               	 {label: sbiModule_translate.load("sbi.analytical.drivers.usemode.lov"), value: 'lov'},
	                               	 {label: sbiModule_translate.load("sbi.analytical.drivers.usemode.manualinput"), value: 'man_in'}
	                                   ];
	 if (showMapDriver)
		 $scope.valueSelectionRadioGroup.push({label: sbiModule_translate.load("sbi.analytical.drivers.usemode.mapinput"), value: 'map_in'});

	$scope.searchByName = {label:"Name" ,isSelected:true};
	$scope.searchByRole = {label:"Role", isSelected:false};
	$scope.searchLovText = "";
	$scope.searchLayerText="";


	//speed menus for the tables
	$scope.adSpeedMenu= [
		                         {
		                            label:sbiModule_translate.load("sbi.generic.delete"),
		                            icon:'fa fa-trash-o fa-lg',
		                            color:'#153E7E',
		                            action:function(item,event){

		                            	$scope.confirmDelete(item,event);
		                            }
		                         }
		                        ];
		 $scope.dumSpeedMenu= [
		                         {
		                            label:sbiModule_translate.load("sbi.generic.delete"),
		                            icon:'fa fa-trash-o fa-lg',
		                            color:'#153E7E',
		                            action:function(item,event){

		                            	$scope.confirmDelete(item,event);

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


		 $scope.confirmDelete = function(item,ev) {
			 console.log(item);
			    var confirm = $mdDialog.confirm()
			          .title(sbiModule_translate.load("sbi.catalogues.toast.confirm.title"))
			          .content(sbiModule_translate.load("sbi.catalogues.toast.confirm.content"))
			          .ariaLabel("confirm_delete")
			          .targetEvent(ev)
			          .ok(sbiModule_translate.load("sbi.general.continue"))
			          .cancel(sbiModule_translate.load("sbi.general.cancel"));
			    $mdDialog.show(confirm).then(function() {
			    	if(item.type != null ){
			    		console.log("DELETING DRIVER");
			    		$scope.deleteDrivers(item);
			    	}else{
			    		console.log("DELETING USEMODE");
			    		$scope.deleteUseMode(item);
			    	}
			    }, function() {

			    });
			  };

	// search functionality
			$scope.searchInput = "";
			$scope.changeSearchMode = function(){
				$scope.changeSearchMode = function(item){
					item.isSelected = !item.isSelected
					if(item.label == 'Name' && item.isSelected){
						$scope.searchByRole.isSelected = false;
					}else if (item.label == 'Role' && item.isSelected) {
						$scope.searchByName.isSelected = false;
					}
				}

			}
			var searchCleanAndReload = function() {

				  angular.copy($scope.useModeList,$scope.useModeListTemp);
				  $scope.getUseModesById($scope.selectedDriver);
				  $scope.searchInput = "";
			};

			$scope.clearLovSearch = function() {

				$scope.searchLovText = "";
				$scope.searchLayerText="";
			};

			var filterThroughCollection = function(newSearchInput,inputCollection,mode) {
				/**
				 * Resulting collection to return.
				 */
				var filteredCollection = [];

				if (inputCollection!=null) {

					var item = null;
					var roles = null;

					for (i=0; i<inputCollection.length; i++) {

						item = inputCollection[i];
						roles = inputCollection[i].associatedRoles;

						/**
						 * NOTE: If we want to search just according to the starting sequence of the name
						 * of a document, change this expression to this criteria: ... == 0).
						 */
						if(mode == 'name'){
							if (item['name'].toLowerCase().indexOf(newSearchInput.toLowerCase()) >= 0) {
								filteredCollection.push(item);
							}
						}else{

							for (var j = 0; j < roles.length; j++) {
								if (roles[j]['name'].toLowerCase().indexOf(newSearchInput.toLowerCase()) >= 0) {
									filteredCollection.push(item);
								}
							}

						}


					}

				}

				/**
				 * Set the flag for displaying a circular loading animation to true, so it can be shown
				 * when searching (filtering) is in progress.
				 */
				return filteredCollection;
			}
			$scope.setSearchInput = function(newSearchInput) {


				/**
				 * Collection through which we will search for diverse documents.
				 */


						if (newSearchInput=="") {

							/**
							 * If the search field is cleared (previously it had some content), unselect potentially selected document
							 * and close the right-side navigation panel. Do this for all documents, datasets and models in the Workspace
							 * (for all available options from the left menu).
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 */


								searchCleanAndReload();




						}else {
							var mode = "";
							if($scope.searchByName.isSelected){
								mode = "name";
							}else{
								mode = "role";
							}


							/**
							 * If the search is started, unselect potentially selected document and close the right-side navigation panel. Do this for all documents,
							 * datasets and models in the Workspace (for all available options from the left menu).
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 */

							$scope.useModeListTemp = filterThroughCollection(newSearchInput,$scope.useModeListTemp,mode);

						}



				/**
				 * Set the current search content to the new one. We are doing this on the end of the function, in order to have the
				 * correct information about the previous search sequence when comparing to the new one (ar the beginning).
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				$scope.searchInput = newSearchInput;
			}


	//FUNCTIONS

	angular.element(document).ready(function () { // on page load function
				$scope.getDrivers();
				$scope.getDomainType();
				$scope.getLovDates();
				$scope.getSelTypes();
				$scope.getRoles();
				$scope.getChecks();
				if (showMapDriver)
					$scope.getLayers();
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
		$scope.selectedParUse.maximizerEnabled = false;
		$scope.selectedParUse.defaultrg = "none";
		$scope.selectedParUse.maxrg = "none";

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
		console.log(item);
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
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

	if (showMapDriver){
		$scope.getLayers = function(){ // service that gets list of layers @GET
			sbiModule_restServices.promiseGet("2.0/analyticalDriversee", "layers")
			.then(function(response) {
				$scope.layersList = response.data;
				console.log($scope.layersList);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});
		}
	}

	$scope.getRoles = function () { // service that gets list of roles @GET
		sbiModule_restServices.promiseGet("2.0", "roles")
		.then(function(response) {
			$scope.rolesList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
    }

	$scope.getChecks = function () { // service that gets list of checks @GET
		sbiModule_restServices.promiseGet("2.0/analyticalDrivers", "checks")
		.then(function(response) {
			$scope.checksList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
    }

	$scope.getDomainType = function(){ // service that gets driver types for dropdown @GET
		sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=PAR_TYPE")
		.then(function(response) {
			$scope.listType = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

	$scope.getSelTypes = function(){ // service that gets lovs selection types for dropdown @GET
		sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=SELECTION_TYPE")
		.then(function(response) {
			$scope.listSelType = response.data;
			for (var i = 0; i < $scope.listSelType.length; i++) {
				if($scope.listSelType[i].VALUE_CD == "SLIDER"){
					$scope.listSelType.splice(i,1);
				}
			}
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

	$scope.canHaveMaxValue = function() {
		return "DATE" == $scope.selectedDriver.type;
	}

	$scope.getLovDates = function(){ // service that gets list of lovs @GET
		sbiModule_restServices.promiseGet("2.0", "lovs/get/all")
		.then(function(response) {
			$scope.listDate = response.data;
			console.log($scope.listDate);
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

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
		$scope.selectedParUse.idLovForMax = ($scope.selectedParUse.idLovForMax === null) ? -1 : parseInt($scope.selectedParUse.idLovForMax);
		// If a change type with one that doesn't support max value
		if (!$scope.canHaveMaxValue()) {
			$scope.selectedParUse.maxrg = "none";
		}
		// Cleanup temp attributes (defaultFormula and/or idLovForDefault) for default
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
		// Cleanup temp attributes (idLovForMax) for max
		switch ($scope.selectedParUse.maxrg) {
		case "none":
			$scope.selectedParUse.idLovForMax = -1;
			break;
		default:
			break;
		}
		//
		delete $scope.selectedParUse.defaultrg;
		delete $scope.selectedParUse.maxrg;
		delete $scope.selectedParUse.showMapDriver;
	}

	$scope.save= function(){  // this function is called when clicking on save button

		var saveDriver = function(path) {
			$scope.formatDriver();

			if($scope.selectedDriver.hasOwnProperty("id")){ // if item already exists do update @PUT

				sbiModule_restServices.promisePut("2.0/analyticalDrivers",$scope.selectedDriver.id, $scope.selectedDriver)
				.then(function(response) {
					$scope.adList=[];
					$timeout(function(){
						$scope.getDrivers();

					}, 1000);
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');

					if (response.data.warnings && response.data.warnings.length > 0)
						sbiModule_messaging.showErrorMessage(response.data.warnings[0], 'Warning');

					//$scope.selectedDriver={};
					if(path == 'usemode'){
						$scope.selectedTab = 1;
					}else{
						$scope.selectedTab = 0;
					}

					//$scope.showme=false;
					$scope.showadMode = true;
					$scope.dirtyForm=false;

				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

				});

			}else{ // create new item in database @POST
				sbiModule_restServices.promisePost("2.0/analyticalDrivers","",angular.toJson($scope.selectedDriver))
				.then(function(response) {
					$scope.adList=[];
					$timeout(function(){
						$scope.getDrivers();
						$scope.loadDrivers(response.data);
					}, 1000);
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
					//$scope.selectedDriver={};
					if(path == 'usemode'){
						$scope.selectedTab = 1;
					}else{
						$scope.selectedTab = 0;
					}
					//$scope.showme=false;





					$scope.showadMode = true;
					$scope.selectedTab = 1;
					$scope.dirtyForm=false;

				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

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
			//saveUseMode();
			saveDriver('usemode');
			$scope.selectedTab=1;
			$scope.closeDialogFromAD();
		}

	}

	$scope.saveUseMode= function(){  // this function is called when clicking on save button
		$scope.formatUseMode();
		$scope.updateMainDriverWithCurrentUseMode($scope.selectedParUse);
		if($scope.selectedParUse.hasOwnProperty("useID")){ // if item already exists do update @PUT
			sbiModule_restServices.promisePut("2.0/analyticalDrivers/modes",$scope.selectedParUse.useID , $scope.selectedParUse)
			.then(function(response) {
				$scope.useModeList=[];
				$timeout(function(){
					$scope.getUseModesById($scope.selectedDriver);
				}, 1000);
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
				$scope.selectedTab = 1;
				$scope.showme=true;
				$scope.showadMode = true;
				$scope.dirtyForm=false;

			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});

		}else{ // create new item in database @POST
			sbiModule_restServices.promisePost("2.0/analyticalDrivers/modes","",angular.toJson($scope.selectedParUse))
			.then(function(response) {
				$scope.useModeList=[];
				$timeout(function(){

					$scope.getUseModesById($scope.selectedDriver);
				}, 1000);
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
				$scope.selectedParUse={};
				$scope.selectedTab = 1;
				$scope.showme=true;
				$scope.showadMode = true;
				$scope.dirtyForm=false;

			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});
		}

		$scope.closeDialogFromAD();
	}

	$scope.updateMainDriverWithCurrentUseMode = function(useMode) {
		switch (useMode.valueSelection) {
		case "lov":
			$scope.selectedDriver.valueSelection = useMode.valueSelection;
			$scope.selectedDriver.selectedLayer = null;
			$scope.selectedDriver.selectedLayerProp = null;
			break;
		case "map_in":
			$scope.selectedDriver.valueSelection = useMode.valueSelection;
			$scope.selectedDriver.selectedLayer = useMode.selectedLayer;
			$scope.selectedDriver.selectedLayerProp = useMode.selectedLayerProp;
			break;
		case "man_in":
			$scope.selectedDriver.valueSelection = useMode.valueSelection;
			$scope.selectedDriver.selectedLayer = null;
			$scope.selectedDriver.selectedLayerProp = null;
			break;
		default:
			break;
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
			sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
			$scope.selectedDriver={};
			$scope.selectedTab = 0;
			$scope.showme=false;
			$scope.showadMode = false;
			$scope.dirtyForm=false;

		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

	// this function is called when item from use mode table is clicked
	$scope.loadUseMode=function(item){
		$scope.associatedRoles = item.associatedRoles;
		$scope.associatedChecks = item.associatedChecks;
		$scope.selectedParUse.defaultrg= null;
		$scope.selectedParUse.maxrg= null;
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

	$scope.openUseModeDetails = function(item){
		console.log('item detail: ',item);
		//$scope.disableSelectedRoles();
		$scope.associatedRoles = item.associatedRoles;
		$scope.associatedChecks = item.associatedChecks;
		$scope.selectedParUse.defaultrg= null;
		$scope.selectedParUse.maxrg= null;
		$scope.searchLovText = "";
		$scope.selectedParUse=angular.copy(item);
		$scope.selectedParUse.showMapDriver = showMapDriver;
		$scope.setParUse();
		 $mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				autoWrap: false,
				parent : angular.element(document.body),
				controllerAs : 'AnalyticalDriversController',
				templateUrl : sbiModule_config.dynamicResourcesBasePath +'/angular_1.4/tools/catalogues/templates/useModeDetails.html',
				clickOutsideToClose : false,
				hasBackdrop : true
			});
	}

	$scope.closeDialogFromAD = function() {
		$scope.selectedParUse = {};
		$mdDialog.cancel();


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

		if($scope.selectedParUse.idLovForMax != null){
			$scope.selectedParUse.maxrg = "lov";
		} else {
			$scope.selectedParUse.maxrg = "none";
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
	// TODO if needed
	$scope.usedRolesCopy = [];
	$scope.disableUsedRoles = function(currentRole,useModeRoles) {
		var allRoles = [];

		 var currentRoles = useModeRoles
		$scope.usedRoles = [];
		for (var i = 0; i < $scope.useModeList.length; i++) {
			allRoles = $scope.useModeList[i].associatedRoles;
			if(allRoles == currentRoles){
				continue;
			}
			for (var k = 0; k < allRoles.length; k++) {
				$scope.usedRoles.push(allRoles[k]);
			}
		}
		$scope.usedRolesCopy = angular.copy($scope.usedRoles);
		for (var i = 0; i < $scope.usedRoles.length; i++) {
			if (currentRole.name == $scope.usedRoles[i].name) {

				return true;
			}
		}
	}

	var findWithAttr =	function(array, attr, value) {
	    for(var i = 0; i < array.length; i += 1) {
	        if(array[i][attr] === value) {
	            return i;
	        }
	    }
	    return -1;
	}

	//this function checks all roles
	$scope.checkAllRoles = function(associated) {



		  // Looping through arr1 to find elements that do not exist in arr2
		  for (var i = 0; i < $scope.rolesList.length; i++) {
		    if (findWithAttr($scope.usedRolesCopy,'name',$scope.rolesList[i].name) === -1){
		      // Pushing the unique to arr1 elements to the newArr
		    	if (findWithAttr($scope.associatedRoles,'name',$scope.rolesList[i].name) === -1){
		    		$scope.associatedRoles.push($scope.rolesList[i]);
		    	}

		    }
		  }
	}
	// this function unchecks all roles
	$scope.uncheckAllRoles = function() {
		$scope.associatedRoles.length = 0;
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
		 $scope.selectedParUse.showMapDriver = showMapDriver;
		 $scope.selectedTab = 1;
		 $mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				parent : angular.element(document.body),
				controllerAs : 'AnalyticalDriversController',
				templateUrl : sbiModule_config.dynamicResourcesBasePath +'/angular_1.4/tools/catalogues/templates/useModeDetails.html',
				clickOutsideToClose : false,
				hasBackdrop : true
			});

	}
	// service that gets list of use modes for selected driver @GET
	$scope.getUseModesById = function (item) {

		sbiModule_restServices.promiseGet("2.0/analyticalDrivers/"+item.id+"/modes", "")
		.then(function(response) {
			$scope.useModeList = response.data;
			$scope.useModeListTemp = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
    }

	$scope.deleteUseMode = function(item){ // this function is called when clicking on delete button
		sbiModule_restServices.promiseDelete("2.0/analyticalDrivers/modes", item.useID)
		.then(function(response) {
			$scope.useModeList=[];
			$timeout(function(){
				$scope.getUseModesById($scope.selectedDriver);
			}, 1000);
			sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
			$scope.selectedParUse = {};
			//$scope.selectedTab = 0;
			//$scope.showme=false;
			//$scope.showadMode = false;
			$scope.dirtyForm=false;

		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}
};
