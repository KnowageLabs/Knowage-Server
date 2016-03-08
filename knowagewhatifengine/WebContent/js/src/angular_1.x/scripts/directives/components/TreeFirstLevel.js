angular.module('filter_tree_first_level',[])
	.directive('filterTreeFirstLevel', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/main/filter/treeFirstLevel.html'
		  };
	});
