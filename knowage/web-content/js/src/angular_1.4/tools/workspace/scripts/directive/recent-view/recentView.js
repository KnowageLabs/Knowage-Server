angular.module('recent_view', ['ngMaterial'])
.directive('recentView', function() {
	return {
		templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/scripts/directive/recent-view/recent-view.html',
		controller: recentViewControllerFunction,
		priority: 10,
		scope: {
			ngModel:"=",
			showGridView:"=?",
			showAddToOrganizer:"=?",
			selectedRow:"=?",
			tableSpeedMenuOption:"=?",
			selectedDocument:"=?",
			selectDocumentAction:"&",
			deleteDocumentAction:"&",
			executeDocumentAction:"&",
			addToOrganizerAction:"&",
			orderingDocumentCards:"=?"
		},
		link: function (scope, elem, attrs) { 
			elem.css("position","relative")
			 if(!attrs.tableSpeedMenuOption){
				 scope.tableSpeedMenuOption=[];
			 }
		}
	}
});

function recentViewControllerFunction($scope){
	
	$scope.clickDocument=function(item){
		
		 $scope.selectDocumentAction({doc: item});
	}
}