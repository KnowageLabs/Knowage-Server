/**
 * Dependencies for the Workspace main controller:
 * 		- document_viewer: Directive that provides possibility to execute a document in separate
 * 		iframe (window) that has a button for closing the executed document. When user do that,
 * 		the iframe closes and we are having the initial page (the one from which we wished to
 * 		execute the document).
 */
angular
	.module('workspace.controller', ['workspace.directive','workspace.configuration'])
	.controller('workspaceController', ["$scope","$http","$mdDialog","$timeout","$documentViewer","sbiModule_translate","sbiModule_restServices","sbiModule_config", workspaceFunction]);

function workspaceFunction($scope,$http,$mdDialog,$timeout,$documentViewer,sbiModule_translate,sbiModule_restServices,sbiModule_config) {

	$scope.allDocuments = [];
	$scope.federationDefinitions=[];
	$scope.businessModels=[];
	$scope.favoriteDocumetnsList = [];

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
	$scope.currentOptionMainMenu = "";
	$scope.activeTabAnalysis = null;	
	
	$scope.isDocumentFavorite = false;

	/**
	 * Flag that servers as indicator for toggling between grid and list view of documents.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.showGridView = true;

	/**
	 * Scope variables needed for showing details about the currently selected document in
	 * the Workspace. Details will be shown inside the right side navigation panel.
	 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.selectedDocument = undefined;
	$scope.showDocumentInfo = false;	
	$scope.translate = sbiModule_translate;
	
	/**
	 * On-click listener function for the left main menu of the Workspace web page.
	 * We will keep the lastly chosen option from this menu inside scope variable.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.showInfo = function(item) {
		$scope.currentOptionMainMenu = item.name.toLowerCase();
	}

	/**
	 * Function for toggling grid/list view by changing the responsible flag (showGridView).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.toogleGridListViewOfDocs = function() {
		$scope.showGridView = !$scope.showGridView;
	}

//	$scope.setSearchInput = function(newSearchInput) {
//
//		$scope.searchInput = newSearchInput;
//		
//		if ($scope.currentOptionMainMenu == "analysis") {
//			
//			if (newSearchInput.length > 0) {
//				
//				$scope.allAnalysisDocs = [];
//				$scope.geoAnalysisDocs = [];
//				$scope.cockpitAnalysisDocs = [];
//				
//				for (i=0; i<allAnalysisDocs.length; i++) {
//																		
//					console.log(searchDocuments[i].typeCode);
//					if (searchDocuments[i].typeCode == "DOCUMENT_COMPOSITE") {
//						$scope.allAnalysisDocs.push(searchDocuments[i]);
//						$scope.cockpitAnalysisDocs.push(searchDocuments[i]);
//					}
//					else if (searchDocuments[i].typeCode == "MAP") {
//						$scope.allAnalysisDocs.push(searchDocuments[i]);
//						$scope.geoAnalysisDocs.push(searchDocuments[i]);
//					}
//					
//				}
//				
//			}
//			else {
//				$scope.allAnalysisDocs = $scope.allAnalysisDocsInitial;
//			}
//			
//		}
//		
//	}

	/**
	 * TODO: 
	 * Preview (execute) a particular Analysis document.
	 */
	$scope.executeDocument = function(document) {
		
		console.info("[EXECUTION]: Execution of Analysis document with the label '" + document.label + "' is started.");		
		$documentViewer.openDocument(document.id, document.label, document.name);
		
	}
	
	/**
	 * [START] Block of functions responsible for showing the details for 
	 * currently selected document. Details will be shown inside the right 
	 * side navigation panel.
	 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.showDocumentDetails = function() {
		$scope.selectedDocument ? console.log($scope.selectedDocument) : null;
		return $scope.showDocumentInfo && $scope.isSelectedDocumentValid();
	};	
	
	$scope.isSelectedDocumentValid = function() {
		return $scope.selectedDocument !== undefined;
	};
	
	$scope.setDocumentDetailOpen = function(isOpen) {
		
		if (isOpen && !$mdSidenav('rightDoc').isLockedOpen() && !$mdSidenav('rightDoc').isOpen()) {
			$scope.toggleDocumentDetail();
		}

		$scope.showDocumentInfo = isOpen;
	};
	
	$scope.toggleDocumentDetail = function() {
		$mdSidenav('rightDoc').toggle();
	};
	
	$scope.selectDocument= function ( document ) { 
		
		if (document !== undefined) {
			$scope.lastDocumentSelected = document;
		}
		
		var alreadySelected = (document !== undefined && $scope.selectedDocument === document);
		
		$scope.selectedDocument = document;
		
		if (alreadySelected) {
			$scope.selectedDocument=undefined;
			$scope.setDocumentDetailOpen(!$scope.showDocumentDetail);
		} else {
			$scope.setDocumentDetailOpen(document !== undefined);
		}
	};
	/**
	 * [END] Block of functions responsible for showing the details for 
	 * currently selected document. Details will be shown inside the right 
	 * side navigation panel.
	 */
}