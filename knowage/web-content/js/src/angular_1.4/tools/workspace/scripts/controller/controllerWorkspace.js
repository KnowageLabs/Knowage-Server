angular
	.module('workspace.controller', ['workspace.directive', 'workspace.configuration'])
	.controller('workspaceController', ["$scope", "$http", "$mdDialog", "sbiModule_translate", "sbiModule_restServices", workspaceFunction]);

function workspaceFunction($scope,$http,$mdDialog,sbiModule_translate,sbiModule_restServices) {
	
	$scope.allDocuments = [];
	$scope.federationDefinitions=[];
	$scope.businessModels=[];
	$scope.datasets=[];
	
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
	
	$scope.currentOptionMainMenu = "";
	
	/**
	 * Flag that servers as indicator for toggling between grid and list view of documents.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.showGridView = true;
	
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
	
}