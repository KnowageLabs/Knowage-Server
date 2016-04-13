angular.module('workspace.controller', ['workspace.directive','workspace.configuration'])
.controller('workspaceController',["$scope","$http","$mdDialog","sbiModule_translate","sbiModule_restServices",workspaceFunction]);

function workspaceFunction($scope,$http,$mdDialog,sbiModule_translate,sbiModule_restServices){
	
	console.log("main")
	$scope.folderDocuments = [];
	
	/**
	 * Left main menu options and their associated icons for the Angular list on the
	 * the Workspace web page.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.leftMainMenu = 	[
	                      	 	{"name": "Recent", 		"icon": "fa fa-clock-o"}, 
	                      	 	{"name": "Favorites", 	"icon": "fa fa-star"}, 
	                      	 	{"name": "Documents", 	"icon": "fa fa-file-text"}, 
	                      	 	{"name": "Datasets", 	"icon": "fa fa-bars"}, 
	                      	 	{"name": "Analysis", 	"icon": "fa fa-calculator"}
	                     	];
	
	/**
	 * On-click listener function for the left main menu of the Workspace web page.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.showInfo = function(item) {		
		alert(item.name);		
	}
}