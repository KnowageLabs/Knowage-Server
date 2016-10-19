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
	 	'ui.codemirror',
	 	'ui.tree'
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
	 	"$timeout",
	 	lovsManagementFunction
	 ]
);

function lovsManagementFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,sbiModule_messaging,sbiModule_config,$timeout)
{
	/**
	 * =====================
	 * ===== Variables =====
	 * =====================
	 */
	$scope.showMe = false;
	$scope.dirtyForm = false; // flag to check for modification
	$scope.enableTest = false;
	$scope.translate = sbiModule_translate;
	$scope.listOfLovs = [];
	$scope.previewLovModel = []
	$scope.testLovTreeModel = [];
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
	$scope.paginationObj ={
		"paginationStart" : 0,
		"paginationLimit" : 20,	
		"paginationEnd" : 20
	};
	$scope.paramsList = [];
	$scope.paramObj = {
		
		"paramName":'',
		"paramValue":''
	};
	$scope.userAttributes = [];
	$scope.testLovColumns = [
	   {
		   label:"Name",
           name:"name",
           size: "200px",
           hideTooltip:true,                   
       },
       {
		   label:"Value",
           name:"",
           hideTooltip:true,
           transformer:function(){
               return " <md-checkbox ng-checked=scopeFunctions.getItem(row,'value') ng-click = scopeFunctions.setItem(row,'value')  aria-label='buttonValue'></md-checkbox>";
           }                                    
       },
       {
		   label:"Description",
           name:"",
           hideTooltip:true,
           transformer:function(){
               return " <md-checkbox ng-checked=scopeFunctions.getItem(row,'description') ng-click = scopeFunctions.setItem(row,'description') aria-label='buttonDescription'></md-checkbox>";
           }                                    
       },
       {
		   label:"Visible",
           name:"",
           hideTooltip:true,
           transformer:function(){
               return " <md-checkbox ng-checked=scopeFunctions.getItem(row,'visible') ng-click = scopeFunctions.setItem(row,'visible') aria-label='buttonVisible'></md-checkbox>";
           }                                    
       }                     
	    ]
	$scope.testLovTreeRightColumns = [
	                  	   {
	                  		   label:"Level",
	                             name:"level",
	                             size: "200px",
	                             hideTooltip:true,                   
	                         },
	                         
	                         {
	                  		   label:"Value",
	                             name:"value",
	                             hideTooltip:true,
	                             editable:true           
	                         },
	                         {
	                  		   label:"Description",
	                             name:"description",
	                             hideTooltip:true,
	                             editable:true                         
	                         }                     
	                  	    ]
	
	
	$scope.TreeListType = [
	  
	   {
		   "name": "Simple",
		   "value" : "simple"
	   },
	   {
		   "name": "Tree",
		   "value" : "tree"
	   },
	   {
		   "name": "Tree selectable inner nodes",
		   "value" : "treeinner"
	   }
	                       
	                       ]
	
	$scope.formatedVisibleValues = [];
	$scope.formatedInvisibleValues = [];
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
		$scope.getUserAttributes();
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
				$scope.enableTest = false;
			    $scope.label = "";
			           
			   },function(){
			    
				   $scope.showMe = true;
			   });
			   
			  }else{
			 
			  $scope.selectedLov = {};
			  $scope.showMe = true;
			  $scope.listForFixLov = [];
			  $scope.enableTest = false;
			  
			  }
	}
	/**
	 * Function that add additional fields depending on selection
	 * from combobox
	 * @author: spetrovic (Stefan.Petrovic@mht.net)
	 */
	$scope.changeType = function(item,type) {
		
		for (var i = 0; i < $scope.listOfInputTypes.length; i++) {
			if($scope.listOfInputTypes[i].VALUE_CD == item){
				$scope.selectedLov.itypeId = ""+$scope.listOfInputTypes[i].VALUE_ID;
			}
		}
		
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
		$scope.enableTest = false;
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
	
	$scope.getUserAttributes = function() {
		
		sbiModule_restServices.promiseGet("2.0/users/attributes", '')
		.then(function(response) {
			$scope.userAttributes = response.data;
			console.log($scope.userAttributes);
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));
			
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
		$scope.previewLovModel = [];
		$mdDialog.cancel();
	}
	/**
	 * When clicking on Save button in the header of the right panel, 
	 * this function will be called and the functionality for saving
	 * LOV into the DB will be run. 	 * 
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	$scope.saveLov = function(){  // this function is called when clicking on save button
		
		formatForSave();
		console.log($scope.selectedLov);
		
if($scope.selectedLov.hasOwnProperty("id")){ // if item already exists do update PUT
			
			sbiModule_restServices.promisePut("2.0/lovs","",$scope.selectedLov)
			.then(function(response) {
				$scope.listOfLovs = [];
				$timeout(function(){								
					$scope.getAllLovs();
				}, 1000);
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
				$scope.selectedLov={};
				$scope.showme=false;
				$scope.dirtyForm=false;
				closeDialogFromLOV();
				
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});	
			
		}else{
		
		sbiModule_restServices.promisePost("2.0/lovs","",$scope.selectedLov)
		.then(function(response) {
			$scope.listOfLovs = [];
			$timeout(function(){								
				$scope.getAllLovs();
			}, 1000);
			sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
			$scope.selectedLov={};
			$scope.showme=false;
			$scope.dirtyForm=false;
			closeDialogFromLOV();
			
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});			
		}
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
		$scope.enableTest = false;
		$scope.changeType(item.itypeCd);
		var lovProvider = parseLovProvider(item);
		console.log(item);
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
			
			
			 $scope.buildTestTable();
			
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
		 $scope.tableFunction = {
		 
			 getItem : function(row,column){
				 if(column == 'description' && row.name == $scope.treeListTypeModel['DESCRIPTION-COLUMN']){
					 return true;
				 }
				 if(column == 'value' && row.name == $scope.treeListTypeModel['VALUE-COLUMN']){
					 return true;
				 }
				 if(column == 'visible'){
					 
						 for (var i = 0; i < $scope.formatedVisibleValues.length; i++) {
								if($scope.formatedVisibleValues[i] == row.name){
									return true;
								}
							}
				 }
			 },
		 	
		 	setItem :  function(row,column){
		 		 if(column == 'description'){
		 			$scope.treeListTypeModel['DESCRIPTION-COLUMN'] = row.name;
				 }	
		 		 if(column == 'value'){
			 			$scope.treeListTypeModel['VALUE-COLUMN'] = row.name;
		 		 }
		 		 if(column == 'visible'){
		 			 	
		 			$scope.formatedVisibleValues.push(row.name);
		 			 	
		 			 	
		 		 }
		 	}
		 }
		
		var deleteLovItem = function(item) {
			
			sbiModule_restServices
					.promisePost("2.0/lovs/deleteSmth","", item)
					.then(
							function(response) {
								$scope.listOfLovs = [];
								$timeout(function(){								
									$scope.getAllLovs();
								}, 1000);
								sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
								$scope.selectedLov={};
								$scope.showme=false;
								$scope.dirtyForm=false;

							},
							function(response) {
								sbiModule_messaging
										.showErrorMessage(
												"An error occured while getting properties for selected member",
												'Error');

							});
		}
		
		
		var formatForTest = function(item,state){
			
			var tempObj = {}
			var property = item.itypeCd;
			
			if(state == 'new'){
				
				tempObj[property] = {
						"DESCRIPTION-COLUMN" : "",
						"INVISIBLE-COLUMNS" : "",
						"LOVTYPE" : "",
						"TREE-LEVELS-COLUMNS" : "",
						"VALUE-COLUMN" : "",
						"VISIBLE-COLUMNS" : ""
						};
				
			}else{
				
				var lovProvider = parseLovProvider($scope.selectedLov);
			    tempObj = lovProvider;
			}
			
			switch (item.itypeCd) {
			case lovProviderEnum.QUERY:
				tempObj[property].CONNECTION = $scope.selectedQuery.datasource;
				tempObj[property].STMT = $scope.selectedQuery.query;
				
			break;
			case lovProviderEnum.SCRIPT:
				tempObj[property].LANGUAGE = $scope.selectedScriptType.language;
				tempObj[property].SCRIPT =  $scope.selectedScriptType.text;
			break;
			case lovProviderEnum.FIX_LOV:
				if($scope.listForFixLov.length > 0){
				tempObj[property].FIXLISTLOV.ROWS.ROW = $scope.listForFixLov;
				}
			break;
			case lovProviderEnum.JAVA_CLASS:
				tempObj[property].JAVA_CLASS_NAME = $scope.selectedJavaClass.name;
			break;
			case lovProviderEnum.DATASET:
				tempObj[property].ID = $scope.selectedDataset.id;
				tempObj[property].LABEL =  $scope.selectedDataset.label;	
			break;
			}
			var x2js = new X2JS(); 
			var xmlAsStr = x2js.json2xml_str(tempObj); 
			$scope.selectedLov.lovProvider = xmlAsStr;
		}
		
		var formatForSave = function() {
			
			var result = {}
			var lovProvider = parseLovProvider($scope.selectedLov);
			var property = $scope.selectedLov.itypeCd;
			var tempObj = lovProvider[property];
			tempObj['DESCRIPTION-COLUMN'] = $scope.treeListTypeModel['DESCRIPTION-COLUMN'];
			tempObj['VALUE-COLUMN'] = $scope.treeListTypeModel['VALUE-COLUMN'];
			tempObj['VISIBLE-COLUMNS'] = $scope.formatedVisibleValues.join();
			  for (var i = 0; i < $scope.testLovModel.length; i++) {
			    if ($scope.formatedVisibleValues.indexOf($scope.testLovModel[i].name) === -1) {
			    	$scope.formatedInvisibleValues.push($scope.testLovModel[i].name);
			    }
			  }
			tempObj['INVISIBLE-COLUMNS'] = $scope.formatedInvisibleValues.join();
			tempObj.LOVTYPE = $scope.treeListTypeModel.LOVTYPE;
			
			result[property] = tempObj
			var x2js = new X2JS();
			var xmlAsStr = x2js.json2xml_str(result); 
			$scope.selectedLov.lovProvider = xmlAsStr;
		
		}
		
		$scope.formatColumns = function(array) {
			var arr = [];
			var size = array.length;
			for (var i = 0; i < size; i++) {
				var obj = {};
				obj.label = array[i].name;
				obj.name = array[i].name;
				if(size <=10){
				obj.size = "60px"	
				}
				arr.push(obj);
			}
			return arr;
		}
		
	
		$scope.openPreviewDialog = function() {
			
			console.log($scope.selectedLov);
			$scope.paginationObj.paginationStart = 0;
			$scope.paginationObj.paginationLimit = 20;
			$scope.paginationObj.paginationEnd = 20;
			if(!$scope.selectedLov.hasOwnProperty('lovProvider')){
				
				formatForTest($scope.selectedLov,'new');
			}else{
				formatForTest($scope.selectedLov,'edit');
				console.log("editing")
			}
			$scope.checkForParams($scope.selectedLov);
			if($scope.paramFlag){
				$mdDialog
				.show({
					scope : $scope,
					preserveScope : true,
					parent : angular.element(document.body),
					controllerAs : 'LOVSctrl',
					templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/lovParams.html',
					clickOutsideToClose : false,
					hasBackdrop : false
				});
				
			}else{
				$scope.previewLov();
			}
		}
		
		$scope.previewLov = function() {
			var toSend ={};
			toSend.data = $scope.selectedLov;
			toSend.pagination = $scope.paginationObj;
			console.log(toSend);
			
			$scope.previewLovModel = [];
			
			sbiModule_restServices
					.promisePost("2.0", "lovs/preview",toSend)
					.then(
							function(response) {
							$scope.tableModelForTest = response.data.metaData.fields;
							$scope.previewLovColumns = $scope.formatColumns(response.data.metaData.fields);
							$scope.previewLovModel = response.data.root;
							$scope.paginationObj.size = response.data.results;
							
							
							$mdDialog
							.show({
								scope : $scope,
								preserveScope : true,
								parent : angular.element(document.body),
								controllerAs : 'LOVSctrl',
								templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/lovPreview.html',
								clickOutsideToClose : false,
								hasBackdrop : false
							});
							
							},
							function(response) {
								sbiModule_messaging
										.showErrorMessage(
												"An error occured while getting properties for selected member",
												'Error');

							});
			
			$scope.enableTest = true;
		}
		
		$scope.getNextPreviewSet = function() {
			console.log("page up");
			$scope.paginationObj.paginationStart = $scope.paginationObj.paginationStart + $scope.paginationObj.paginationLimit;
			
			$scope.paginationObj.paginationEnd = $scope.paginationObj.paginationStart+$scope.paginationObj.paginationLimit;
			if($scope.paginationObj.paginationEnd > $scope.paginationObj.size){
				$scope.paginationObj.paginationEnd = $scope.paginationObj.size;
			}
			$scope.previewLov();
		}
		$scope.getBackPreviewSet = function() {
			console.log("page down");
			var temp = $scope.paginationObj.paginationStart;
			$scope.paginationObj.paginationStart = $scope.paginationObj.paginationStart - $scope.paginationObj.paginationLimit;
			$scope.paginationObj.paginationEnd = temp ;
			
			$scope.previewLov();
		}
		
		$scope.checkArrows = function(type) {
			if($scope.paginationObj.paginationStart == 0 && type == 'back'){
				return true;
			}
			if($scope.previewLovModel.length != 20 && type == 'next'){
				return true;
			}
		}
		
	$scope.moveToTree = function(item) {
		var defObj = {};
		defObj.level = item.name;
		defObj.value = item.name;
		defObj.description = item.name;
		$scope.testLovTreeModel.push(defObj);
	}
		
	 $scope.buildTestTable = function() {
		 if($scope.selectedLov != null){
			 $scope.treeListTypeModel = {};
			 var propName = $scope.selectedLov.itypeCd;
			 var prop = lovProviderEnum[propName];
			 var provider = parseLovProvider($scope.selectedLov);
			 $scope.treeListTypeModel = provider[prop];
			 console.log($scope.treeListTypeModel);
			 if($scope.selectedLov.id != undefined){
				 console.log("we have existing one")
				 $scope.formatedVisibleValues = $scope.treeListTypeModel['VISIBLE-COLUMNS'].split(",");
				 $scope.formatedInvisibleValues = $scope.treeListTypeModel['INVISIBLE-COLUMNS'].split(",");
				 $scope.formatedDescriptionValues = $scope.treeListTypeModel['DESCRIPTION-COLUMN'].split(",");
				 $scope.formatedValues = $scope.treeListTypeModel['VALUE-COLUMN'].split(",");
				 
			 }else{
				 console.log("we have new one")
				 $scope.treeListTypeModel.LOVTYPE = 'simple';
			 }
			 if($scope.treeListTypeModel.LOVTYPE != 'simple' && $scope.treeListTypeModel.LOVTYPE != ''){
				 $scope.formatedTreeValues = $scope.treeListTypeModel['TREE-LEVELS-COLUMNS'].split(",");
				for (var i = 0; i < $scope.formatedValues.length; i++) {
					var defObj = {};
					defObj.level = $scope.formatedValues[i];
					defObj.value = $scope.formatedValues[i];
					defObj.description = $scope.formatedValues[i];
					$scope.testLovTreeModel.push(defObj);
				} 
			 }
		 }
		 $scope.testLovModel = $scope.tableModelForTest;
		 
		 
	 }
	
		
		$scope.checkForParams = function(item) {
			var lovProvider = parseLovProvider(item);
			$scope.paramFlag = false;
			
			
			if (lovProvider.hasOwnProperty(lovProviderEnum.SCRIPT)) {
				var script = lovProvider.SCRIPTLOV.SCRIPT; 
				if(script.match(/{(.*)}/)){
					$scope.paramObj.paramName = script.match(/{(.*)}/).pop();
					var check = false;
					for (var i = 0; i < $scope.userAttributes.length; i++) {
						if($scope.paramObj.paramName == $scope.userAttributes[i].Name ){
							console.log("param " +$scope.paramObj.paramName+ " found in user attributes");
							check = true;
							break;
						}
					}
					if(!check){
						console.log("opening dialog");
						$scope.paramFlag = true;
					}
					
				}else{
					console.log("No params in script");
					$scope.paramFlag = false;
				}
				
			}else if(lovProvider.hasOwnProperty(lovProviderEnum.QUERY)){
				var query = lovProvider.QUERY.STMT;
				if(query.match(/{(.*)}/)){
					$scope.paramObj.paramName = query.match(/{(.*)}/).pop();
					var check = false;
					for (var i = 0; i < $scope.userAttributes.length; i++) {
						if($scope.paramObj.paramName == $scope.userAttributes[i].Name ){
							console.log("param " +$scope.paramObj.paramName+ " found in user attributes");
							check = true;
							break;
						}
					}
					if(!check){
						console.log("opening dialog");
						$scope.paramFlag = true;
					}
					
					
				}else{
					console.log("No params in query");
					$scope.paramFlag = false;
				}
				
			}else if (lovProvider.hasOwnProperty(lovProviderEnum.FIX_LOV)) {
				
				if(lovProvider.FIXLISTLOV.ROWS.ROW && Array === lovProvider.FIXLISTLOV.ROWS.ROW.constructor){
					var fixLovArray = [];
					fixLovArray = lovProvider.FIXLISTLOV.ROWS.ROW;
					for (var i = 0; i < fixLovArray.length; i++) {
						var one = fixLovArray[i]._VALUE;
						if(one.match(/{(.*)}/)){
							$scope.paramObj = {};
							$scope.paramObj.paramName = one.match(/{(.*)}/).pop();
							$scope.paramsList.push($scope.paramObj);
							
						}
					}
					for (var i = 0; i < $scope.userAttributes.length; i++) {
						for (var j = 0; j < $scope.paramsList.length; j++) {
							if($scope.paramsList[j].paramName == $scope.userAttributes[i].Name){
								$scope.paramsList.splice(j,1);
								
							}
						}
					}
					if($scope.paramsList.length > 0){
						$scope.paramFlag = true;
						console.log("opening dialog");
					}else{
						$scope.paramFlag = false;
						console.log("No params in query");	
					}
				}else{
					var row = lovProvider.FIXLISTLOV.ROWS.ROW;
					if(row._VALUE.match(/{(.*)}/)){
						$scope.paramObj.paramName = row._VALUE.match(/{(.*)}/).pop();
						var check = false;
						for (var i = 0; i < $scope.userAttributes.length; i++) {
							if($scope.paramObj.paramName == $scope.userAttributes[i].Name ){
								console.log("param " +$scope.paramObj.paramName+ " found in user attributes");
								check = true;
								break;
							}
						}
						if(!check){
							console.log("opening dialog");
							$scope.paramFlag = true;
						}
						
						
					}else{
						console.log("No params in query");
						$scope.paramFlag = false;
					}
					
				}
				
			}
				
		}

};