angular.module('favorite_view', ['ngMaterial'])
.directive('favoriteView', function() {
	return {
		templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/scripts/directive/favorite-view/favorite-view.html',
		controller: favoriteViewControllerFunction,
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

function favoriteViewControllerFunction($scope){
	
	$scope.clickDocument=function(item){
		
		 $scope.selectDocumentAction({doc: item});
	}
}