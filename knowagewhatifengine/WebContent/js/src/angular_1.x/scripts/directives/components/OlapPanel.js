angular.module('olap_panel',[])
	.directive('olapPanel', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/main/olap/olapPanel.html'
		  };
	});
