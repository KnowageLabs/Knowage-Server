angular.module('left_panel',[])
	.directive('leftPanel', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/left/leftPanel.html'
		  };
	});
