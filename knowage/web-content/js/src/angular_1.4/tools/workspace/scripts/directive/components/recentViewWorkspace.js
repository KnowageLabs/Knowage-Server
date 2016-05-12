angular
	.module('recent_view_workspace', [])

	.directive('recentViewWorkspace', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/recentViewWorkspace.html',
		      controller: recentController
		  };
	});

function recentController($scope){

}
