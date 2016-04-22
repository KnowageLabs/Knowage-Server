angular
	.module('left_main_menu_workspace', [])

	/**
	 * The HTML content for the main menu on the left panel of the Workspace web page. 
	 * It offers five views for various files that user has added to its personal 
	 * workspace (folders, documents, datasets).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	.directive('leftMainMenuWorkspace', function () {		
		 return {			 
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/leftMainMenuWorkspace.html',
		      controller: leftMenuController
		  };	  
	});

function leftMenuController($scope){
	
	/**
	 * Left main menu options and their associated icons for the Angular list on the
	 * the Workspace web page.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.leftMainMenu = 	[
	                      	 	{"name": "Recent", 		"icon": "fa fa-clock-o",	"visible":true}, 
	                      	 	{"name": "Favorites", 	"icon": "fa fa-star",	"visible":true}, 
	                      	 	{"name": "Documents", 	"icon": "fa fa-file-text", "visible":true}, 
	                      	 	//{"name": "Data", 	"icon": "fa fa-bars", "visible":true}, 
	                      	 	{"name": "Datasets", 	"icon": "fa fa-database",	"visible":true},
	                      	 	{"name": "Models", 	"icon": "fa fa-cubes",	"visible":true},
	                      	 	{"name": "SmartFilters", 	"icon": "fa fa-filter",	"visible":true}, 
	                      	 	{"name": "Analysis", 	"icon": "fa fa-calculator","visible":true}
	                     	];

	
}