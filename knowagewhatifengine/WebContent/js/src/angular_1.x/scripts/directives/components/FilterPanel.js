angular.module('filter_panel',[])
	.directive('filterPanel', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/main/filter/filterPanel.html'
		  };
	});
