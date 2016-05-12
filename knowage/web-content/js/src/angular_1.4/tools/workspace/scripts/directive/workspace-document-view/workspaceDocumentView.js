angular.module('workspace_document_view', ['ngMaterial'])
.directive('workspaceDocumentView', function() {
	return {
		templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/scripts/directive/workspace-document-view/workspace-document-view.html',
		controller: workspaceDocumentViewControllerFunction,
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
			cloneDocumentAction:"&",
			addToOrganizerAction:"&",
			addToFavoritesAction:"&",
			orderingDocumentCards:"=?",
			cloneEnabled:"=?"
		},
		link: function (scope, elem, attrs) { 
			
			/**
			 * Changed from 'relative' to 'static' so the Grid/List toggling button could 
			 * work for the Analysis option as well.
			 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			elem.css("position","static")
			
			 if(!attrs.tableSpeedMenuOption){
				 scope.tableSpeedMenuOption=[];
			 }
		}
	}
});

function workspaceDocumentViewControllerFunction($scope){
	
	$scope.clickDocument=function(item){
		
		 $scope.selectDocumentAction({doc: item});
	}
}