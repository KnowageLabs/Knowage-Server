angular.module('workspace.directive', [])

.directive('documentViewWorkspace', function () {
	 return {
	      restrict: 'E',
	      replace: 'true',
	      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/documentViewWorkspace.html'
	  };
})

.directive('leftMainMenuWorkspace', function () {
	 return {
	      restrict: 'E',
	      replace: 'true',
	      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/leftMainMenuWorkspace.html'
	  };
});