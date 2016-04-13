angular
	.module('main_toolbar_workspace', [])

	.directive('mainToolbarWorkspace', function () {		
		 return {			 
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/mainToolbarWorkspace.html',
		      controller: toolbarController
		  };	  
	});

function toolbarController($scope){
	
}