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
}