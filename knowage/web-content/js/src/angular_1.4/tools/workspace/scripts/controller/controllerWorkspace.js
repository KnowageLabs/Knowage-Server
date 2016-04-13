angular.module('workspace.controller', ['workspace.directive','workspace.configuration'])
.controller('workspaceController',["$scope","$http","$mdDialog","sbiModule_translate","sbiModule_restServices",workspaceFunction]);

function workspaceFunction($scope,$http,$mdDialog,sbiModule_translate,sbiModule_restServices){
	
	$scope.allDocuments = [];
	$scope.analysisDocs = [];
	
	$scope.currentOptionMainMenu = "";
	
	/**
	 * On-click listener function for the left main menu of the Workspace web page.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.showInfo = function(item) {		
		$scope.currentOptionMainMenu = item.name.toLowerCase();		
	}	
		
	$scope.loadMyAnalysisDocuments = function() {
		
		sbiModule_restServices
			.promiseGet("documents", "myAnalysisDocsList")
			.then(
					function(response) {					
						angular.copy(response.data,$scope.analysisDocs);
						console.log($scope.analysisDocs);
					},
					
					function(response) {
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
					}
				);
	}

	$scope.loadMyAnalysisDocuments();
}