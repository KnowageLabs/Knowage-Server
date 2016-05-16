/**
 * Dependencies for the Workspace main controller:
 * 		- document_viewer: Directive that provides possibility to execute a document in separate
 * 		iframe (window) that has a button for closing the executed document. When user do that,
 * 		the iframe closes and we are having the initial page (the one from which we wished to
 * 		execute the document).
 */
angular
	.module('workspace.controller', ['workspace.directive','workspace.configuration'])
	.controller('workspaceController', ["$scope","$http","$mdDialog","$timeout","$documentViewer","sbiModule_translate","sbiModule_restServices","sbiModule_config","sbiModule_user", workspaceFunction])
	.directive('fileModel',['$parse',function($parse){
		
		return {
			restrict:'A',
			link: function(scope,element,attrs){
				var model = $parse(attrs.fileModel);
				var modelSetter = model.assign;
				
				element.bind('change',function(){
					scope.$apply(function(){
						modelSetter(scope,element[0].files[0]);
						
					})
				})
			}
		}
		
		
	}])
   .service('multipartForm',['$http',function($http){
		
		this.post = function(uploadUrl,data){
			
			var formData = new FormData();
			
			formData.append("file",data.file);

			return	$http.post(uploadUrl,formData,{
					transformRequest:angular.identity,
					headers:{'Content-Type': undefined}
				})
		}
		
	}]);
;

function workspaceFunction($scope,$http,$mdDialog,$timeout,$documentViewer,sbiModule_translate,sbiModule_restServices,sbiModule_config,sbiModule_user) {

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
	$scope.currentOptionMainMenu = "recent";
	
	$scope.isDocumentFavorite = false;

	/**
	 * Flag that servers as indicator for toggling between grid and list view of documents.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.showGridView = true;

	$scope.translate = sbiModule_translate;

	/**
	 * Scope variables needed for showing details about the currently selected document in
	 * the Workspace. Details will be shown inside the right side navigation panel.
	 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var selectedDocument = undefined;
	var showDocumentInfo = false;	
	
	/**
	 * On-click listener function for the left main menu of the Workspace web page.
	 * We will keep the lastly chosen option from this menu inside scope variable.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.leftMenuItemPicked = function(item) {
		
		$scope.currentOptionMainMenu = item.name.toLowerCase();
	
		if (searchedBefore) {
			$scope.searchInput = "";			
			$scope.setSearchInput($scope.searchInput);
		}
		
		/**
		 * Unselect the already selected document when changing the option in the left menu.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.selectedDocument = undefined;
		
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
		console.log(newSearchInput);
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
					angular.copy($scope.allAnalysisDocsInitial,$scope.allAnalysisDocs); 
					angular.copy($scope.cockpitAnalysisDocsInitial,$scope.cockpitAnalysisDocs);
					angular.copy($scope.geoAnalysisDocsInitial,$scope.geoAnalysisDocs);
					break;
				
				/**
				 * SEARCH FOR DATASETS
				 */
				case "datasets":
					console.info("We will add functionality for searching through DATASETS");
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
//		selectedDocument ? console.log(selectedDocument) : null;
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