angular
	.module('workspace.controller', ['workspace.directive', 'workspace.configuration'])
	.controller('workspaceController', ["$scope", "$http", "$mdDialog", "sbiModule_translate", "sbiModule_restServices","sbiModule_config", workspaceFunction]);

function workspaceFunction($scope,$http,$mdDialog,sbiModule_translate,sbiModule_restServices,sbiModule_config) {

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

//	$scope.setSearchInput = function(searchInput) {
//		console.log(searchInput);
//	}

	$scope.setSearchInput = function (newSearchInput) {
		$scope.searchInput = newSearchInput;
		setFocus("searchInput");

		$timeout(function(){
			if (newSearchInput == $scope.searchInput) {
				if (newSearchInput.length > 0){
					sbiModule_restServices.promiseGet("2.0/documents", "searchDocument?attributes=all&value=" + newSearchInput + "*", null)
					.then(function(response) {
						$scope.searchDocuments = response.data;
					},function(response){
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.document.search.error'))
						.finally(function(){
							$scope.searchDocuments = [];
						})
					});
				}else{
					$scope.searchDocuments = [];
				}
			}
		}, 400);
	}

	/**
	 * Set the currently active tab of the Analysis perspective in order to
	 * enable managing of visibility of "Add analysis document" button. This
	 * button should not be visible if the user is seeing all the documents.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.setActiveTabState = function(item) {		
		$scope.activeTabAnalysis = item.toUpperCase();
	};

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
