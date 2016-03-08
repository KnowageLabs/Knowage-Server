angular.module('filter_card',[])
	.directive('filterCard', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/main/filter/filterCard.html'
		  };
	});
