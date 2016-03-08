angular.module('olap_top_toolbar',[])
	.directive('olapTopToolbar', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/main/olap/topToolbar.html'
		  };
	});
