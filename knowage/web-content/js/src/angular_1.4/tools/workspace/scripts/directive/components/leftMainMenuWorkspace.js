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

function leftMenuController($scope, sbiModule_translate){
	 
	/**
	 * Left main menu options and their associated icons for the Angular list on the
	 * the Workspace web page.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.leftMainMenu = 	[   
	                      	    
	                      	 	{"name": "Recent", "label":sbiModule_translate.load('sbi.workspace.menu.recent'),		"icon": "fa fa-clock-o",	"visible":true, "active":false, "selected":true}, 
	                      	 	{"name": "Favorites", "label":sbiModule_translate.load('sbi.workspace.menu.favorites'), 	"icon": "fa fa-star",	"visible":true, "active":false, "selected":false}, 
	                      	 	{"name": "Documents", "label":sbiModule_translate.load('sbi.workspace.menu.documents'),	"icon": "fa fa-file-text", "visible":false, "active":false, "selected":false}, 
	                      	 	{"name": "Data", "label":sbiModule_translate.load('sbi.workspace.menu.data'), 	"icon": "fa fa-angle-down", "activeIcon":"fa fa-angle-up" ,"visible":true, "active":false,
	                      	 		"submenuOptions":[	{"name": "Datasets", "label":sbiModule_translate.load('sbi.workspace.menu.datasets'), 	"icon": "fa fa-database",	"visible":true, "selected":false},
	                    	                      	 	{"name": "Models","label":sbiModule_translate.load('sbi.workspace.menu.models'), 	"icon": "fa fa-cubes",	"visible":true, "selected":false},
	                    	                      	 	{"name": "SmartFilters","label":sbiModule_translate.load('sbi.workspace.menu.smartFilter'), 	"icon": "fa fa-filter",	"visible":"smartFilterEnabled", "selected":false}
	                      	 	                     ]}, 
	                      	  
	                      	 	{"name": "Analysis", "label":sbiModule_translate.load('sbi.workspace.menu.analysis'), 	"icon": "fa fa-calculator","visible":true, "active":false, "selected":false}
	                     	];
	
	$scope.selectedMenuItem=$scope.leftMainMenu[0]; 

	$scope.setExpanded=function(option){	
		
		option.active=!option.active;
		
		/**
		 * If the currently selected item from the left main menu is one of three suboptions of the 'Data' option (Datasets, Models, SmartFilters) and the Data option 
		 * is collapsed, the Data option will be selected by taking the 'true' value of the model that it's style tracks - the 'suboptionActive' value.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.isSuboptionActive = $scope.currentOptionMainMenu=="models" || $scope.currentOptionMainMenu=="datasets" || $scope.currentOptionMainMenu=="smartfilters";
		
		if ($scope.isSuboptionActive && !option.active) {
			$scope.suboptionActive = true;
		}
		else {
			$scope.suboptionActive = false;
		}
	}
	
	$scope.selectMenuItem= function(item){
		$scope.selectedMenuItem.selected=false;// previous to false
		$scope.selectedMenuItem=item; // change selected
		$scope.selectedMenuItem.selected=true; // mark new selected true
		
	}
}