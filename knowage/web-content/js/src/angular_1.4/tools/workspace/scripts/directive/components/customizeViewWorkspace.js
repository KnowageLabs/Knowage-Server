angular
	.module('customize_view_workspace', [])

	.directive('customizeViewWorkspace', function () {		
		 return {			 
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/customizeViewWorkspace.html',
		      controller: customizeController
		  };	  
	});

function customizeController($scope){
	
}