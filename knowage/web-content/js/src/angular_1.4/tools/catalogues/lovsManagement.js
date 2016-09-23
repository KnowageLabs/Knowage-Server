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
	 	'angular-list-detail',
	 	'ui.codemirror'
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
	$scope.selectedJavaClass = {};
	$scope.selectedDataset = {};
	$scope.lovItemEnum ={
		"SCRIPT" : "SCRIPT",
		"QUERY" : "QUERY",
		"FIX_LOV" : "FIX_LOV",
		"JAVA_CLASS" : "JAVA_CLASS",
		"DATASET" : "DATASET"
	};
	var lovTypeEnum = {
		"MAIN": undefined,	
		"SCRIPT":"script",
		"QUERY":"query",
		"DATASET":"dataset"
	}; 
	var lovProviderEnum ={
			"SCRIPT" : "SCRIPTLOV",
			"QUERY" : "QUERY",
			"FIX_LOV" : "FIXLISTLOV",
			"JAVA_CLASS" : "JAVACLASSLOV",
			"DATASET" : "DATASET"	
	};
	
	   $scope.cmOption = {
			   indentWithTabs: true,
				smartIndent: true,
				lineWrapping : true,
				matchBrackets : true,
				autofocus: true,
				theme:"eclipse",
				lineNumbers: true,
			     onLoad : function(_cm){
			       
			       // HACK to have the codemirror instance in the scope...
			       $scope.modeChanged = function(type){
			        console.log(type)
			        if(type=='ECMAScript'){
			         _cm.setOption("mode", 'text/javascript');
			     } else {
			      _cm.setOption("mode", 'text/x-groovy');
			     }
			       };
			     }
			   };

	
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
	
	$scope.fixLovSpeedMenu= [
	                                 {
	                                    label:sbiModule_translate.load("sbi.generic.delete"),
	                                    icon:'fa fa-trash-o fa-lg',
	                                    color:'#153E7E',
	                                    action:function(item,event){
	                                        
	                                    	$scope.confirmDelete(item,event);
	                                    }
	                                 },
	                                 {
		                                    label:sbiModule_translate.load("sbi.behavioural.lov.fixlov.up"),
		                                    icon:'fa fa-arrow-up fa-lg',
		                                    color:'#153E7E',
		                                    action:function(item,event){
		                                        
		                                    	$scope.moveFixLovUp(item,event);
		                                    },
		                                    visible:function(row){
		                                    	return checkArrowVisibility(row,'up');
		                                    }
		                                    
	                                 
		                             },
		                             {
		                                    label:sbiModule_translate.load("sbi.behavioural.lov.fixlov.down"),
		                                    icon:'fa fa-arrow-down fa-lg',
		                                    color:'#153E7E',
		                                    action:function(item,event){
		                                        
		                                    	$scope.moveFixLovDown(item,event);
		                                    },
		                                    visible:function(row){
		                                    	return checkArrowVisibility(row,'down');
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
	    	if(item.lovProvider != null){
	    		deleteLovItem(item);
	    	}else{
	    		deleteFixedLovItem(item);
	    	}
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
				$scope.listForFixLov = [];
				$scope.showMe = true;
			    $scope.label = "";
			           
			   },function(){
			    
				   $scope.showMe = true;
			   });
			   
			  }else{
			 
			  $scope.selectedLov = {};
			  $scope.showMe = true;
			  $scope.listForFixLov = [];
			  
			  }
	}
	/**
	 * Function that add additional fields depending on selection
	 * from combobox
	 * @author: spetrovic (Stefan.Petrovic@mht.net)
	 */
	$scope.changeType = function(item,type) {
		if(type == lovTypeEnum.MAIN){
			 switch (item) {
				case $scope.lovItemEnum.SCRIPT:
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.scriptWizard");
					$scope.infoTitle= sbiModule_translate.load("sbi.behavioural.lov.info.syntax")
					cleanSelections();
					
					break;
				case $scope.lovItemEnum.QUERY:
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.queryWizard");
					$scope.infoTitle= sbiModule_translate.load("sbi.behavioural.lov.info.syntax")
					cleanSelections();
					
					break;
				case $scope.lovItemEnum.FIX_LOV:
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.fixedListWizard");
					$scope.infoTitle= sbiModule_translate.load("sbi.behavioural.lov.info.rules")
					cleanSelections();
					
					break;	
				case $scope.lovItemEnum.JAVA_CLASS:
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.javaClassWizard");
					$scope.infoTitle= sbiModule_translate.load("sbi.behavioural.lov.info.rules")
					cleanSelections();
					
					break;
				case $scope.lovItemEnum.DATASET:
					$scope.toolbarTitle = sbiModule_translate.load("sbi.behavioural.lov.details.datasetWizard");
					cleanSelections();
					
					break;	
				default:
					break;
				}
			}else if (type == lovTypeEnum.SCRIPT) {
					
			}else if (type == lovTypeEnum.QUERY) {
				
			}else if (type == lovTypeEnum.DATASET) {
				
				
			}
		}
	
	var cleanSelections = function() {
		$scope.selectedScriptType={};
		$scope.selectedQuery = {};
		$scope.selectedFIXLov = {};
		$scope.selectedJavaClass = {};
		$scope.selectedDataset = {};
		$scope.listForFixLov = [];
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
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));
			
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
		if($scope.selectedLov.itypeCd != $scope.lovItemEnum.DATASET){
			console.lo
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
		$scope.selectedFIXLov= {};
		
	}
	
	/**
	 * Action that will happen when user clicks on the "Add" button that
	 * adds new pair (label, description) for current Fixed LOV item 
	 * (second panel on the right side of the page).
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	$scope.addNewFixLOV = function()
	{	
		if($scope.listForFixLov.length> 0){
		
			for (var i = 0; i < $scope.listForFixLov.length; i++) {
				if($scope.selectedFIXLov._VALUE !=  $scope.listForFixLov[i]._VALUE && $scope.selectedFIXLov._DESCRIPTION !=  $scope.listForFixLov[i]._DESCRIPTION){
					console.log("new one");
					$scope.listForFixLov.push($scope.selectedFIXLov);
					$scope.selectedFIXLov= {};
					break;
				}else{
					console.log("editing");
					var index =  $scope.listForFixLov.indexOf($scope.listForFixLov[i]);
					$scope.listForFixLov.splice(index, 1);
					$scope.listForFixLov.push($scope.selectedFIXLov);
					$scope.selectedFIXLov= {};
					break;
				}
			}
		
	} else{
		$scope.listForFixLov.push($scope.selectedFIXLov);
		$scope.selectedFIXLov= {};
	}
		
		
	}
	
	/**
	 * Function that handles what should be done when user clicks on the
	 * LOV item on the left side of the page (the one from the catalog).
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	$scope.itemOnClick = function(item)
	{	
		$scope.changeType(item.itypeCd);
		var lovProvider = parseLovProvider(item);
		console.log(lovProvider);
		if(lovProvider != null){
			if (lovProvider.hasOwnProperty(lovProviderEnum.SCRIPT)) {
				
				$scope.selectedScriptType.language = lovProvider.SCRIPTLOV.LANGUAGE;			
				$scope.selectedScriptType.text = lovProvider.SCRIPTLOV.SCRIPT;
				
			}else if(lovProvider.hasOwnProperty(lovProviderEnum.QUERY)){
				
				$scope.selectedQuery.datasource = lovProvider.QUERY.CONNECTION;
				$scope.selectedQuery.query = lovProvider.QUERY.STMT;
				
			}else if (lovProvider.hasOwnProperty(lovProviderEnum.FIX_LOV)) {
				
				if(lovProvider.FIXLISTLOV.ROWS.ROW && Array === lovProvider.FIXLISTLOV.ROWS.ROW.constructor){
					
					$scope.listForFixLov = [];
					$scope.listForFixLov = lovProvider.FIXLISTLOV.ROWS.ROW;
				}else{
					$scope.listForFixLov = [];
					$scope.listForFixLov.push(lovProvider.FIXLISTLOV.ROWS.ROW);
				}
				
			}else if(lovProvider.hasOwnProperty(lovProviderEnum.JAVA_CLASS)){
				
				$scope.selectedJavaClass.name = lovProvider.JAVACLASSLOV.JAVA_CLASS_NAME;
				
			}else if (lovProvider.hasOwnProperty(lovProviderEnum.DATASET)) {
				
				$scope.selectedDataset.id = lovProvider.DATASET.ID;
			}
		}else{
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.behavioural.lov.xml.error"), sbiModule_translate.load("sbi.generic.toastr.title.error"));
		}
		
		
		
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
	 * Function that bind fixed lov item with model
	 * @author: spetrovic (Stefan.Petrovic@mht.net)
	 */
	$scope.itemOnClickFixLov = function(item) {
		$scope.selectedFIXLov=angular.copy(item);
	}
	/**
	 * Function that shows or hides arrows in table if
	 * if its first or last items
	 * @author: spetrovic (Stefan.Petrovic@mht.net)
	 */
	var checkArrowVisibility = function(row,direction) {
		var firstRow = $scope.listForFixLov[0];
		var lastRow = $scope.listForFixLov[$scope.listForFixLov.length-1];
		if(direction == 'up'){
			
			if(row._VALUE == firstRow._VALUE){
				return false;
			}else{
				return true;
			}
			
		}else if (direction == 'down') {
			if(row._VALUE == lastRow._VALUE){
				return false;
			}else{
				return true;
			}
		}
		
		
	}
	/**
	 * Functions that moves items in table up or down
	 * @author: spetrovic (Stefan.Petrovic@mht.net)
	 */
	$scope.moveFixLovUp = function(item) {
		var index = $scope.listForFixLov.indexOf(item);
		var nextIndex = index-1;
		var temp = $scope.listForFixLov[index];
		$scope.listForFixLov[index] = $scope.listForFixLov[nextIndex];
		$scope.listForFixLov[nextIndex] = temp;
	}
	$scope.moveFixLovDown = function(item) {
		var index = $scope.listForFixLov.indexOf(item);
		var nextIndex = index+1;
		var temp = $scope.listForFixLov[index];
		$scope.listForFixLov[index] = $scope.listForFixLov[nextIndex];
		$scope.listForFixLov[nextIndex] = temp;
	}
	/**
	 * Function that delete fixlov
	 * @author: spetrovic (Stefan.Petrovic@mht.net)
	 */
	var deleteFixedLovItem = function(item){
		var index = $scope.listForFixLov.indexOf(item);		
		$scope.listForFixLov.splice(index, 1);
	}
	/**
	 * Call all necessary services when getting all LOV items (all items
	 * in the LOV catalog for our page).
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
		$scope.getAllLovs = function(){ // service that gets list of drivers @GET
			sbiModule_restServices.promiseGet("2.0", "lovs")
			.then(function(response) {
				console.log(response);
				$scope.listOfLovs = response.data;
			}, function(response) {

				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));
				
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
				$scope.listOfInputTypes = response.data;
			}, function(response) {
				
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));
				
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
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));
				
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
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));
				
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
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));
				
			});
		}
		
		$scope.testLov = function() {
			console.log("opening test");
			$mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				parent : angular.element(document.body),
				controllerAs : 'LOVSctrl',
				templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/lovTest.html',
				clickOutsideToClose : false,
				hasBackdrop : false
			});
		}
		var deleteLovItem = function(item) {
			
			sbiModule_restServices
					.promisePost("2.0/lovs/deleteSmth","", item)
					.then(
							function(response) {
								
								console.log(response);
							},
							function(response) {
								sbiModule_messaging
										.showErrorMessage(
												"An error occured while getting properties for selected member",
												'Error');

							});
		}
		
		/**
		 * Function that parse  xml LovProvider property of the object
		 * to json so it can be seen in interface.
		 * x2js library is used. https://github.com/abdmob/x2js
		 * @author: spetrovic (Stefan.Petrovic@mht.net)
		 */
		var parseLovProvider = function(item) {
			var x2js = new X2JS(); 
            var json = x2js.xml_str2json(item.lovProvider); 
            return json; 
			
		}
		

};