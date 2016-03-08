angular.module('filter_tree_deeper_levels',[])
	.directive('filterTreeDeeperLevels', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/main/filter/treeDeeperLevels.html'
		  };
	});
