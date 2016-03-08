angular.module('olap_left_toolbar_plus_main',[])
	.directive('olapLeftToolbarPlusMain', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/main/olap/leftToolbarPlusMain.html'
		  };
	});
