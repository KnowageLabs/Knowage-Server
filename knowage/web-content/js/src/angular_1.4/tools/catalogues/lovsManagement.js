/**
 * JS file for managing LOVs management catalog panel.
 * 
 * @author: danristo (danilo.ristovski@mht.net)
 */

var app = angular.module
(
	'lovsManagementModule',
	
	[
	 	'ngMaterial',
	 	'angular_list',
	 	'angular_table',	 	
	 	'sbiModule',
	 	'angular-list-detail'
	 ]
);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);

app.controller
(
	'lovsManagementController',
	
	[
	 	"sbiModule_translate",
	 	"sbiModule_restServices",
	 	"$scope",
	 	"$mdDialog",
	 	"$mdToast",
	 	"sbiModule_messaging",
	 	"sbiModule_config",
	 	lovsManagementFunction
	 ]
);

function lovsManagementFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,sbiModule_messaging,sbiModule_config)
{
	/**
	 * =====================
	 * ===== Variables =====
	 * =====================
	 */
	$scope.showMe = false;
	$scope.dirtyForm = false; // flag to check for modification
	$scope.translate = sbiModule_translate;
	$scope.listOfLovs = [];
	$scope.listOfInputTypes = [];
	$scope.listOfScriptTypes = [];
	$scope.listOfDatasources = [];
	$scope.listOfDatasets = [];
	$scope.listForFixLov = [];
	$scope.listOfProfileAttributes = [];
	$scope.selectedLov = {};
	$scope.toolbarTitle ="";
	$scope.infoTitle="";
	$scope.selectedScriptType={};
	$scope.selectedQuery = {};
	$scope.selectedFIXLov = {};
	$scope.selectedJavaClass = null;
	$scope.selectedDataset = {};
	

	
	/**
	 * Speed menu for handling the deleting action on one 
	 * particular LOV item.
	 */
	$scope.lovsManagementSpeedMenu= [
                         {
                            label:sbiModule_translate.load("sbi.generic.delete"),
                            icon:'fa fa-trash-o fa-lg',
                            color:'#153E7E',
                            action:function(item,event){
                                
                            	$scope.confirmDelete(item,event);
                            }
                         }
                        ];

	
	$scope.confirmDelete = function(item,ev) {
	    var confirm = $mdDialog.confirm()
	          .title(sbiModule_translate.load("sbi.catalogues.toast.confirm.title"))
	          .content(sbiModule_translate.load("sbi.catalogues.toast.confirm.content"))
	          .ariaLabel("confirm_delete")
	          .targetEvent(ev)
	          .ok(sbiModule_translate.load("sbi.general.continue"))
	          .cancel(sbiModule_translate.load("sbi.general.cancel"));
	    $mdDialog.show(confirm).then(function() {
	    	$scope.deleteLov(item);
	    }, function() {
	
	    });
	  };
	/**
	 * =====================
	 * ===== Functions =====
	 * =====================
	 */
	
	angular.element(document).ready(function () { // on page load function
		$scope.getAllLovs();
		$scope.getInputTypes();
		$scope.getScriptTypes();
		$scope.getDatasources();
		$scope.getDatasets();
		
		
		
    });
	
	$scope.setDirty=function()
	{ 
		  $scope.dirtyForm=true;
	}
	
	/**
	 * When clicking on plus button on the left panel, this function 
	 * will be called and we should enable showing of the right panel 
	 * on the main page for LOVs management (variable "showMe" will 
	 * provide this functionality).	 * 
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	$scope.createLov = function()
	{
		 if($scope.dirtyForm){
			   $mdDialog.show($scope.confirm).then(function(){
				$scope.dirtyForm=false;   
				$scope.selectedLov = {};
				$scope.showMe = true;
			    $scope.label = "";
			           
			   },function(){
			    
				   $scope.showMe = true;
			   });
			   
			  }else{
			 
			  $scope.selectedLov = {};
			  $scope.showMe = true;
			  }
	}
	/**
	 * Function that add additional fields depending on selection
	 * from combobox
	 * @author: spetrovic (Stefan.Petrovic@mht.net)
	 */
	$scope.changeType = function(type,item) {
		if(type == 'lov'){
			
			 switch (item) {
				case "SCRIPT":
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.scriptWizard");
					$scope.infoTitle= "Show Sintax...";
					cleanSelections();
					
					break;
				case "QUERY":
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.queryWizard");
					$scope.infoTitle= "Show Sintax...";
					cleanSelections();
					
					break;
				case "FIX_LOV":
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.fixedListWizard");
					$scope.infoTitle= "Rules";
					cleanSelections();
					
					break;	
				case "JAVA_CLASS":
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.javaClassWizard");
					$scope.infoTitle= "Rules";
					cleanSelections();
					
					break;
				case "DATASET":
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.datasetWizard");
					cleanSelections();
					
					break;	
				default:
					break;
				}
			
		}else if (type == 'script') {
			
		}else if (type == 'datasource') {
			
		}
		
		}
	
	var cleanSelections = function() {
		$scope.selectedScriptType={};
		$scope.selectedQuery = {};
		$scope.selectedFIXLov = {};
		$scope.selectedJavaClass = null;
		$scope.selectedDataset = {};
	}
	/**
	 * Function opens dialog with available
	 * profile attributes when clicked
	 * @author: spetrovic (Stefan.Petrovic@mht.net)
	 */
	$scope.openAttributesFromLOV = function() {
		
		sbiModule_restServices.promiseGet("2.0/attributes", '')
		.then(function(response) {
			$scope.listOfProfileAttributes = response.data;
			console.log($scope.listOfProfileAttributes);
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		
		$mdDialog
		.show({
			scope : $scope,
			preserveScope : true,
			parent : angular.element(document.body),
			controllerAs : 'LOVSctrl',
			templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/profileAttributes.html',
			clickOutsideToClose : false,
			hasBackdrop : false
		});
	}
	/**
	 * Function opens dialog with information
	 * about selection
	 * @author: spetrovic (Stefan.Petrovic@mht.net)
	 */
	$scope.openInfoFromLOV = function() {
		if($scope.selectedLov.itypeCd !='DATASET'){
			$mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				parent : angular.element(document.body),
				controllerAs : 'LOVSctrl',
				templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/Info.html',
				clickOutsideToClose : false,
				hasBackdrop : false
			});
			
		}
			
	}
	$scope.closeDialogFromLOV = function() {
		$mdDialog.cancel();
	}
	/**
	 * When clicking on Save button in the header of the right panel, 
	 * this function will be called and the functionality for saving
	 * LOV into the DB will be run. 	 * 
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	$scope.saveLov = function()
	{
		console.log("-- function saveLov() --");
	}
	
	/**
	 * When clicking on Cancel button in the header of the right panel, 
	 * this function will be called and the right panel will be hidden.	  
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	$scope.cancel = function()
	{
		$scope.showMe = false;
		$scope.dirtyForm=false;
		$scope.selectedLov = {};
	}
	
	/**
	 * Action that will happen when user clicks on the "Add" button that
	 * adds new pair (label, description) for current Fixed LOV item 
	 * (second panel on the right side of the page).
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	$scope.addFixLovItemIntoGrid = function()
	{
		console.log("-- add Fix LOV item into the Fix LOV grid list --");
	}
	
	/**
	 * Function that handles what should be done when user clicks on the
	 * LOV item on the left side of the page (the one from the catalog).
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	$scope.itemOnClick = function(item)
	{
		console.log(item);
		$scope.changeType(item.itypeCd)
		 if($scope.dirtyForm){
			   $mdDialog.show($scope.confirm).then(function(){
				$scope.dirtyForm=false;   
				$scope.selectedLov=angular.copy(item);
				$scope.showMe=true;
				
				
			   },function(){
			    
				$scope.showMe = true;
			   });
			   
			  }else{
			 
			  $scope.selectedLov=angular.copy(item);
			  $scope.showMe=true;
			  }
	}
	
	/**
	 * Call all necessary services when getting all LOV items (all items
	 * in the LOV catalog for our page).
	 * @author: danristo (danilo.ristovski@mht.net)
	 */

		/**
		 * Get all LOV items from the DB for the LOV catalog.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		$scope.getAllLovs = function(){ // service that gets list of drivers @GET
			sbiModule_restServices.promiseGet("2.0", "lovs")
			.then(function(response) {
				console.log(response)
				$scope.listOfLovs = response.data;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});
		}
		
		/**
		 * Get all input types for populating the GUI item that
		 * holds them when specifying the LOV item. This is used
		 * for specifying what kind (type) of LOV item user wants
		 * to define.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		
		$scope.getInputTypes = function() {
			sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=INPUT_TYPE")
			.then(function(response) {
				console.log(response);
				$scope.listOfInputTypes = response.data;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});
		}
		
		/**
		 * Get all script types from the DB in order to populate
		 * its GUI element so user can pick the script type he 
		 * wants for the Script input type.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		
		$scope.getScriptTypes = function() {
			sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=SCRIPT_TYPE")
			.then(function(response) {
				$scope.listOfScriptTypes = response.data;
				console.log($scope.listOfScriptTypes);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});
		}
		
		
		/**
		 * Get datasources from the DB in order to populate the combo box
		 * that servers as a datasource picker for Query input type for
		 * LOV.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		$scope.getDatasources = function() {
			sbiModule_restServices.promiseGet("2.0/datasources", "")
			.then(function(response) {
				$scope.listOfDatasources = response.data;
				console.log($scope.listOfDatasources);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});
		}
		
		/**
		 * Get datasets from the DB in order to populate the combo box
		 * that servers as a dataset picker for Dataset input type for
		 * LOV.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		
		$scope.getDatasets = function() {
			sbiModule_restServices.promiseGet("1.0/datasets","")
			.then(function(response) {
				$scope.listOfDatasets = response.data.root;
				console.log($scope.listOfDatasets);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});
		}
		
	
	$scope.parseProviderXml = function(item)
	{
		
	}
};