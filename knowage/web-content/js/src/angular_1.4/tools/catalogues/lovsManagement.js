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
	 	'angular_2_col'
	 ]
);

app.controller
(
	'lovsManagementController',
	
	[
	 	"sbiModule_translate",
	 	"sbiModule_restServices",
	 	"$scope",
	 	"$mdDialog",
	 	"$mdToast",
	 	lovsManagementFunction
	 ]
);

function lovsManagementFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast)
{
	/**
	 * =====================
	 * ===== Variables =====
	 * =====================
	 */
	$scope.showMe = false;
	$scope.translate = sbiModule_translate;
	$scope.listOfLovs = [];
	$scope.listOfInputTypes = [];
	$scope.listOfScriptTypes = [];
	$scope.listOfScriptTypes = [];
	$scope.listOfDatasources = [];
	$scope.listOfDatasets = [];
	$scope.listForFixLov = [];
	$scope.selectedLov = {};
	
//	$scope.lovsManagementSpeedMenu = 
//	[
//	     {
//	    	 label:sbiModule_translate.load("sbi.generic.delete"),
//	    	 icon:'fa fa-minus',
//	    	 backgroundColor:'red',
//	    	 color:'white',
//	    	 
//		     action:function(item,a)
//		     {
////		    	console.log("-- remove single LOV item from the list --");
//		    	console.log(item);
//		    	console.log(a);
//		    	
//		    	sbiModule_restServices
//				.remove("LOV","")
//				.success
//				(
//					function(data, status, headers, config) 
//					{
//						if (data.hasOwnProperty("errors")) 
//						{
//							//change sbi.glossary.load.error
//							console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
//						} 
//						else 
//						{
//							console.log("remove LOV:");
//							console.log(data);
//							//$scope.listOfDatasets = data.root;					
//						}
//					}
//				)
//				.error
//				(
//					function(data, status, headers, config) 
//					{
//						console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
//					}
//				);
//		     }
//	     }
//    ];
	
	/**
	 * Speed menu for handling the deleting action on one 
	 * particular LOV item.
	 */
	$scope.lovsManagementSpeedMenu = 
	[
	 	{
			label : "Delete",
			
			action : function(item) 
			{
				console.log(item);
				console.log(JSON.stringify(item));
//					sbiModule_restServices.remove("LOV", 'deleteSmth', "",JSON.stringify(item))
//					.success(
//
//							function(data, status, headers, config) {
//								console.log(data)
//								if (data.hasOwnProperty("errors")) {
//									console.log("layer non Ottenuti");
//								} else {
////									$scope.loadLayer();
////									$scope.closeForm();
////									$scope.showActionDelete();
//									console.log("HERE I AM!!");
//								}
////
//							}
//	 	).error(function(data, status, headers, config) {
//								console.log("layer non Ottenuti " + status);
//
//							});
		    	
		    	sbiModule_restServices
					.delete("LOV","", "",'{"id":"5"}')
					.success
					(
						function(data, status, headers, config) 
						{
							if (data.hasOwnProperty("errors")) 
							{
								//change sbi.glossary.load.error
								console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
							} 
							else 
							{
								console.log("remove LOV:");
								console.log(data);
								//$scope.listOfDatasets = data.root;					
							}
						}
					)
					.error
					(
						function(data, status, headers, config) 
						{
							console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
						}
					);
			}
		}
	];
	
	/**
	 * =====================
	 * ===== Functions =====
	 * =====================
	 */
	
	/**
	 * When clicking on plus button on the left panel, this function 
	 * will be called and we should enable showing of the right panel 
	 * on the main page for LOVs management (variable "showMe" will 
	 * provide this functionality).	 * 
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	$scope.createLov = function()
	{
		$scope.showMe = true;
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
		console.log("-- LOV item with ID: " + item.id + " is clicked --");
		console.log(item);		
		/**
		 * 
		 */
		$scope.selectedLov = angular.copy(item);
		$scope.showMe = true;
	}
	
	/**
	 * Call all necessary services when getting all LOV items (all items
	 * in the LOV catalog for our page).
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	$scope.getAllLovs = function()
	{		
		/**
		 * Get all LOV items from the DB for the LOV catalog.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		sbiModule_restServices
			.get("2.0", "lovs")
			.success
			(
				function(data, status, headers, config) 
				{
					if (data.hasOwnProperty("errors")) 
					{
						//change sbi.glossary.load.error
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} 
					else 
					{		
						console.log("all LOVs:");
						console.log(data);
						$scope.listOfLovs = data;						
					}
				}
			)
			.error
			(
				function(data, status, headers, config) 
				{
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
				}
			);	
		
		/**
		 * Get all input types for populating the GUI item that
		 * holds them when specifying the LOV item. This is used
		 * for specifying what kind (type) of LOV item user wants
		 * to define.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		sbiModule_restServices
			.get("domains", "listValueDescriptionByType", "DOMAIN_TYPE=INPUT_TYPE")
			.success
			(
				function(data, status, headers, config) 
				{
					if (data.hasOwnProperty("errors")) 
					{
						//change sbi.glossary.load.error
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} 
					else 
					{
						console.log("input types:");
						console.log(data);
						$scope.listOfInputTypes = data;					
					}
				}
			)
			.error
			(
				function(data, status, headers, config) 
				{
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
				}
			);
		
		/**
		 * Get all script types from the DB in order to populate
		 * its GUI element so user can pick the script type he 
		 * wants for the Script input type.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		sbiModule_restServices
			.get("domains", "listValueDescriptionByType", "DOMAIN_TYPE=SCRIPT_TYPE")
			.success
			(
				function(data, status, headers, config) 
				{
					if (data.hasOwnProperty("errors")) 
					{
						//change sbi.glossary.load.error
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} 
					else 
					{	
						console.log("script types:");
						console.log(data);
						$scope.listOfScriptTypes = data;					
					}
				}
			)
			.error
			(
				function(data, status, headers, config) 
				{
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
				}
			);
		
		/**
		 * Get datasources from the DB in order to populate the combo box
		 * that servers as a datasource picker for Query input type for
		 * LOV.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		sbiModule_restServices
			.get("datasources","")
			.success
			(
				function(data, status, headers, config) 
				{
					if (data.hasOwnProperty("errors")) 
					{
						//change sbi.glossary.load.error
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} 
					else 
					{
						console.log("datasources:");
						console.log(data);
						$scope.listOfDatasources = data.root;					
					}
				}
			)
			.error
			(
				function(data, status, headers, config) 
				{
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
				}
			);	
		
		/**
		 * Get datasets from the DB in order to populate the combo box
		 * that servers as a dataset picker for Dataset input type for
		 * LOV.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		sbiModule_restServices
			.get("1.0/datasets","")
			.success
			(
				function(data, status, headers, config) 
				{
					if (data.hasOwnProperty("errors")) 
					{
						//change sbi.glossary.load.error
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} 
					else 
					{
						console.log("datasets:");
						console.log(data);
						$scope.listOfDatasets = data.root;					
					}
				}
			)
			.error
			(
				function(data, status, headers, config) 
				{
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
				}
			);	
	}
	
	$scope.getAllLovs();
	
	$scope.parseProviderXml = function(item)
	{
		
	}
};