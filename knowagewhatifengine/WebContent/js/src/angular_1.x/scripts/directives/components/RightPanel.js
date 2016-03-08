angular.module('right_panel',[])
	.directive('rightPanel', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/right/rightPanel.html'
		  };
	});
