angular.module('main_toolbar',[])
	.directive('mainToolbar', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/main/toolbar/mainToolbar.html'
		  };
	});
