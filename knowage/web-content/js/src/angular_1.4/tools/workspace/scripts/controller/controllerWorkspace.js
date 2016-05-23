/**
 * Dependencies for the Workspace main controller:
 * 		- document_viewer: Directive that provides possibility to execute a document in separate
 * 		iframe (window) that has a button for closing the executed document. When user do that,
 * 		the iframe closes and we are having the initial page (the one from which we wished to
 * 		execute the document).
 */
angular
	.module('workspace.controller', ['workspace.directive','workspace.configuration'])
	.controller('workspaceController', ["$scope","$http","$mdDialog","$timeout","$documentViewer","sbiModule_translate","sbiModule_restServices","sbiModule_config","sbiModule_user","sbiModule_messaging", workspaceFunction])
   .service('multipartForm',['$http',function($http){
		
		this.post = function(uploadUrl,data){
			
			var formData = new FormData();
    		for(var key in data){
    				formData.append(key,data[key]);
    			}
			return	$http.post(uploadUrl,formData,{
					transformRequest:angular.identity,
					headers:{'Content-Type': undefined}
				})
		}
		
	}]);
;

function workspaceFunction($scope,$http,$mdDialog,$timeout,$documentViewer,sbiModule_translate,sbiModule_restServices,sbiModule_config,sbiModule_user,sbiModule_messaging) {

	$scope.allDocuments = [];
	$scope.federationDefinitions=[];
	$scope.businessModels=[];
	$scope.favoriteDocumentsList = [];
	$scope.recentDocumetnsList = [];
	/**
	 * variables for data management
	 */
	$scope.datasets=[];  //all
	$scope.myDatasets= [];
	$scope.enterpriseDatasets=[];
	$scope.sharedDatasets=[];
	$scope.notDerivedDatasets=[];
     
	/**
	 * smart filters
	 */
	$scope.smartFiltersList=[];
	
	$scope.searchInput = "";

	/**
	 * Variables for Analysis view of the Workspace (option):
	 * 		allAnalysisDocs - array of all Analysis documents available in user's workspace (Cockpit, Geo and Ad hoc)
	 * 		cockpitAnalysisDocs - array of all Analysis documents of type Cockpit available in user's workspace
	 * 		adhocReportAnalysisDocs - array of all Analysis documents of type Ad hoc available in user's workspace
	 * 		geoAnalysisDocs - array of all Analysis documents of type Geo available in user's workspace
	 */
	$scope.allAnalysisDocs = [];
	$scope.cockpitAnalysisDocs = [];
	$scope.adhocReportAnalysisDocs = [];
	$scope.geoAnalysisDocs = [];
	$scope.allAnaylticsDocsTypes = ["MAP","DOCUMENT_COMPOSITE"];

	/**
	 * currentOptionMainMenu - 	which of all available perspectives (options) from the left menu is selected (picked) 
	 * 							by the user.
	 * activeTabAnalysis - 		which of all three available tabs for Analysis perspective (option) is active at the moment
	 * 							(ALL, COCKPIT, GEO).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	(whereAreWeComingFrom == "NewCockpit") ? $scope.currentOptionMainMenu = "analysis" : $scope.currentOptionMainMenu = "recent";	
	
	$scope.isDocumentFavorite = false;

	/**
	 * Flag that servers as indicator for toggling between grid and list view of documents.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.showGridView = true;

	var recentDocumentsLoaded = false;
	var favoritesDocumentsLoaded = false;
	var recentDocumentsLoaded = false;
	var organizerDocumentsLoaded = false;
	var datasetsDocumentsLoaded = false;
	var modelsDocumentsLoaded = false;
	var smartFiltersDocumentsLoaded = false;
	
	/**
	 * Attached to the scope because we need this information inside the controller for managing the Analysis documents and interface after coming to the Workspace
	 * interface (web page) from the interface for the creation of a new Cockpit document.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.analysisDocumentsLoaded = false;
	
	$scope.translate = sbiModule_translate;

	/**
	 * Scope variables needed for showing details about the currently selected document in
	 * the Workspace. Details will be shown inside the right side navigation panel.
	 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var selectedDocument = undefined;
	var showDocumentInfo = false;

	/**
	 * TODO: For Dataset wizard
	 */
	$scope.dataset = {};
	
	$scope.dataset.fileType = "";
	$scope.dataset.fileName = "";
	
	$scope.limitPreviewChecked = false;
	
	$scope.dataset.csvEncoding = "UTF-8"; 
	$scope.dataset.csvDelimiter = ","; 
	$scope.dataset.csvQuote = "\""; 
	
	$scope.dataset.skipRows = 0;
	$scope.dataset.limitRows = null;
	$scope.dataset.xslSheetNumber = 1;
	
	$scope.dataset.catTypeVn = "";
	$scope.dataset.catTypeId = null;
	
//	$scope.dataset.id = "";
//	$scope.dataset.type = "File";
//	$scope.dataset.label = "";
//	$scope.dataset.name = ""
//	$scope.dataset.description = "";
//	$scope.dataset.persist = false;
//	$scope.dataset.tablePrefix = datasetParameters.TABLE_NAME_PREFIX;
//	$scope.dataset.tableName = "";
//	$scope.dataset.fileUploaded = true;
//	$scope.dataset.meta = [];
	
	$scope.csvEncodingTypes = 
	[ 
	 	{value:"windows-1252",name:"windows-1252"}, 
	 	{value:"UTF-8",name:"UTF-8"},	 	
	 	{value:"UTF-16",name:"UTF-16"},	
	 	{value:"US-ASCII",name:"US-ASCII"},	 	
	 	{value:"ISO-8859-1",name:"ISO-8859-1"}
 	];
	
	$scope.csvDelimiterCharacterTypes = 
	[ 
	 	{value:",",name:","}, 
	 	{value:";",name:";"},	 	
	 	{value:"\\t",name:"\\t"},	
	 	{value:"\|",name:"\|"}
 	];
	
	$scope.csvQuoteCharacterTypes = 
	[ 
	 	{value:"\"",name:"\""}, 
	 	{value:"\'",name:"\'"}
 	];
	
	$scope.chooseDelimiterCharacter = function(delimiterCharacterObj) {
		$scope.dataset.csvDelimiter = delimiterCharacterObj.value;
//		console.log($scope.csvDelimiter);
	}
	
	$scope.chooseQuoteCharacter = function(quoteCharacterObj) {
		$scope.dataset.csvQuote = quoteCharacterObj.value;
//		console.log($scope.csvQuote);
	}
	
	$scope.chooseEncoding = function(encodingObj) {
		$scope.dataset.csvEncoding = encodingObj.value;
//		console.log($scope.csvEncoding);
	}
		
	$scope.chooseCategory = function(category) {
		$scope.dataset.catTypeVn = category.VALUE_CD;
		$scope.dataset.catTypeId = category.VALUE_ID;
	}
	
	/**
	 * On-click listener function for the left main menu of the Workspace web page.
	 * We will keep the lastly chosen option from this menu inside scope variable.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.leftMenuItemPicked = function(item) {
		
		$scope.currentOptionMainMenu = item.name.toLowerCase();
		$scope.selectMenuItem(item);
		
		/**
		 * If the previously selected item from the left main menu was one of three suboptions of the 'Data' option (Datasets, Models, SmartFilters) and the newly selected
		 * item is not among those three, whilst the Data option is collapsed, unselect the Data option.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var isSuboptionActiveTemp = $scope.currentOptionMainMenu=="models" || $scope.currentOptionMainMenu=="datasets" || $scope.currentOptionMainMenu=="smartfilters";
			
		if (!isSuboptionActiveTemp && $scope.isSuboptionActive) {
			$scope.suboptionActive = false;
		}
	
		if (searchedBefore) {
			$scope.searchInput = "";			
			$scope.setSearchInput($scope.searchInput);
		}
		
		/**
		 * Unselect the already selected document when changing the option in the left menu.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.selectedDocument = undefined;
		
		/**
		 * Provide loading for different Workspace options from the left main menu only when it is chosen (clicked)
		 * for the first time (after loading the main page). By default, the Recent documents are displayed first.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		switch($scope.currentOptionMainMenu) {
			
			case "analysis":
				
				if ($scope.analysisDocumentsLoaded==false) {
					console.info("[LOAD START]: Loading of Analysis documents is started.");
					$scope.loadAllMyAnalysisDocuments();
					$scope.analysisDocumentsLoaded = true;
				}

				break;

			case "favorites":
				
				if (favoritesDocumentsLoaded==false) {
					console.info("[LOAD START]: Loading of Favorites documents is started.");
					$scope.loadFavoriteDocumentExecutionsForUser();
					favoritesDocumentsLoaded = true;
				}
				
				break;
				
			case "documents":
				
				if (organizerDocumentsLoaded==false) {
					console.info("[LOAD START]: Loading of Organizer documents is started.");
					$scope.loadAllDocuments();
					organizerDocumentsLoaded = true;
				}
				
				break;
				
			case "datasets":
				
				if (datasetsDocumentsLoaded==false) {
					
					console.info("[LOAD START]: Loading of Datasets is started.");
					
					$scope.loadDatasets();
					$scope.loadMyDatasets();
					$scope.loadEnterpriseDatasets();
					$scope.loadSharedDatasets();
						
					datasetsDocumentsLoaded = true;
				}
				
				break;
							
			case "models":
				
				if (modelsDocumentsLoaded==false) {
					
					console.info("[LOAD START]: Loading of Models is started.");
					
					$scope.loadFederations();
					$scope.loadBusinessModels();
					
					modelsDocumentsLoaded = true;
				}
				
				break;
				
			case "smartfilters":
				
				if (smartFiltersDocumentsLoaded==false) {
					
					console.info("[LOAD START]: Loading of Smart filters is started.");					
					/**
					 * TODO: Add functionality for loading all smart filters.
					 */		
					$scope.loadSmartFilters();
					smartFiltersDocumentsLoaded = true;
				}
				
				break;
				
		}
		
	}

	/**
	 * Function for toggling grid/list view by changing the responsible flag (showGridView).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.toogleGridListViewOfDocs = function() {
		$scope.showGridView = !$scope.showGridView;
	}

	/**
	 * Filter the sent collection of data (documents, analysis, datasets, etc.)
	 * according to the searching term (sequence) user entered, 'newSearchInput'.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var filterThroughCollection = function(newSearchInput,inputCollection,propertyName) {
		
		/**
		 * Resulting collection to return.
		 */
		var filteredCollection = [];
		
		if (inputCollection!=null) {
		
			var item = null;
			
			for (i=0; i<inputCollection.length; i++) {
				
				item = inputCollection[i];
				
				/**
				 * NOTE: If we want to search just according to the starting sequence of the name
				 * of a document, change this expression to this criteria: ... == 0).
				 */
				if (item[propertyName].toLowerCase().indexOf(newSearchInput.toLowerCase()) >= 0) {					
					filteredCollection.push(item);					
				}
				
			}
			
		}
		
		/**
		 * Set the flag for displaying a circular loading animation to true, so it can be shown
		 * when searching (filtering) is in progress.
		 */
		$scope.searching = false;
		
		return filteredCollection;		
	}

	/**
	 * Flag to indicate if we need to reload data when changing between options on the left menu.
	 * For example, if user searched through the collection for one left menu option (e.g. Analysis)
	 * and after that goes to another option (e.g. Datasets), he need to clear the searching input
	 * field, as well as to reload all analysis documents that were filtered just before option change.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var searchedBefore = false;
	
	/**
	 * Function that is called when user is starting a search among some document collection (dataset,
	 * analysis, documents, etc.).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.setSearchInput = function(newSearchInput) {		
		
		$scope.searchInput = newSearchInput;
		var currentOptionMainMenu = $scope.currentOptionMainMenu;		
		
		/**
		 * Collection through which we will search for diverse documents.
		 */
		var allAnalysisDocsTemp = null;
		var cockpitAnalysisDocsTemp = null;
		var geoAnalysisDocsTemp = null;
		
		var filteredCollection = [];
		
		var collectionForFiltering = null;		
		
		if (newSearchInput=="") { 
			
			switch(currentOptionMainMenu) {
			
				/**
				 * SEARCH FOR ANALYSIS
				 */
				case "analysis":				
					angular.copy($scope.cockpitAnalysisDocsInitial,$scope.cockpitAnalysisDocs);
					break;
				
				/**
				 * SEARCH FOR DATASETS
				 */
				case "datasets":
					//console.info("We will add functionality for searching through DATASETS");
					angular.copy($scope.datasetsInitial,$scope.datasets); 
					angular.copy($scope.myDatasetsInitial,$scope.myDatasets);
					angular.copy($scope.enterpriseDatasetsInitial,$scope.enterpriseDatasets);
					angular.copy($scope.sharedDatasetsInitial,$scope.sharedDatasets);
					angular.copy($scope.ckanDatasetsListInitial,$scope.ckanDatasetsList);
				
					break;
				
				/**
				 * SEARCH FOR FAVORITES
				 */
				case "favorites":
					$scope.favoriteDocumentsList = $scope.favoriteDocumentsInitial;
					break;
				
				/**
				 * SEARCH FOR RECENT
				 */
				case "recent":
					$scope.recentDocumetnsList = $scope.recentDocumentsInitial;
					break;
					
				case "models":
					angular.copy($scope.federationDefinitionsInitial,$scope.federationDefinitions); 
				    angular.copy($scope.businessModelsInitial,$scope.businessModels);
				    break;
				    
				case "smartfilters":
					  angular.copy($scope.smartFiltersListInitial,$scope.smartFiltersList);
					break;
			}
		}
		else {
			
			$scope.searching = true;
			
			$timeout
			(
				function() {
					
					!searchedBefore ? searchedBefore = true : null;
					
					switch(currentOptionMainMenu) {
					
						/**
						 * SEARCH FOR ANALYSIS
						 */
						case "analysis":
							
								var allAnalysisDocsFinal = [];
								
								$scope.cockpitAnalysisDocs = filterThroughCollection(newSearchInput,$scope.cockpitAnalysisDocsInitial,"name");
								$scope.geoAnalysisDocs = filterThroughCollection(newSearchInput,$scope.geoAnalysisDocsInitial,"name");
								
								angular.copy($scope.cockpitAnalysisDocs,allAnalysisDocsFinal);
								
								for (i=0; i<$scope.geoAnalysisDocs.length; i++) {									
									allAnalysisDocsFinal.push($scope.geoAnalysisDocs[i]);
								}
								
								$scope.allAnalysisDocs = allAnalysisDocsFinal;
							
							break;
						
						/**
						 * SEARCH FOR RECENT
						 */
						case "recent":
							$scope.recentDocumetnsList = filterThroughCollection(newSearchInput,$scope.recentDocumentsInitial,"documentName");
							break;
						
						/**
						 * SEARCH FOR FAVORITES
						 */
						case "favorites":
							$scope.favoriteDocumentsList = filterThroughCollection(newSearchInput,$scope.favoriteDocumentsInitial,"name");
							break;
							
						/**
						 * SEARCH FOR DATASETS
						 */	
						case "datasets":
							$scope.datasets=filterThroughCollection(newSearchInput,$scope.datasetsInitial,"name");
							$scope.myDatasets= filterThroughCollection(newSearchInput,$scope.myDatasetsInitial,"name");
							$scope.enterpriseDatasets=filterThroughCollection(newSearchInput,$scope.enterpriseDatasetsInitial,"name");
							$scope.sharedDatasets=filterThroughCollection(newSearchInput,$scope.sharedDatasetsInitial,"name");
							$scope.ckanDatasetsList=filterThroughCollection(newSearchInput,$scope.ckanDatasetsListInitial,"name");
							break;
							
						/**
						 * SEARCH FOR MODELS
						 */	
						case "models":
							$scope.federationDefinitions=filterThroughCollection(newSearchInput,$scope.federationDefinitionsInitial,"name");
							$scope.businessModels=filterThroughCollection(newSearchInput,$scope.businessModelsInitial,"name");
							break;
							
							
						case "smartfilters":
							$scope.smartFiltersList=filterThroughCollection(newSearchInput,$scope.smartFiltersListInitial,"name");
							break;
					}	
					
				}, 100
			);		
			
		}			
					
	}

	/**
	 * Preview (execute) a particular document.
	 */
	$scope.executeDocument = function(document) {
		console.info("[EXECUTION]: Execution of document with the label '" + document.label + "' is started.");		
		$documentViewer.openDocument(document.id, document.label, document.name);
		
	}
	
	/**
	 * [START] Block of functions responsible for showing the details for 
	 * currently selected document. Details will be shown inside the right 
	 * side navigation panel.
	 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var showDocumentDetails = function() {
		return showDocumentInfo && isSelectedDocumentValid();
	};	
	
	isSelectedDocumentValid = function() {
		return selectedDocument !== undefined;
	};
	
	setDocumentDetailOpen = function(isOpen) {
		
		if (isOpen && !$mdSidenav('rightDoc').isLockedOpen() && !$mdSidenav('rightDoc').isOpen()) {
			$scope.toggleDocumentDetail();
		}

		showDocumentInfo = isOpen;
	};
	
	$scope.toggleDocumentDetail = function() {
		$mdSidenav('rightDoc').toggle();
	};
	
	$scope.selectDocument= function ( document ) { 
		
//		if (document !== undefined) {
//			$scope.lastDocumentSelected = document;
//		}
		
		var alreadySelected = (document !== undefined && selectedDocument === document);
		
		selectedDocument = document;
		
		if (alreadySelected) {
			selectedDocument=undefined;
			setDocumentDetailOpen(!showDocumentDetails);
		} else {
			setDocumentDetailOpen(document !== undefined);
		}
	};
	/**
	 * [END] Block of functions responsible for showing the details for 
	 * currently selected document. Details will be shown inside the right 
	 * side navigation panel.
	 */
}