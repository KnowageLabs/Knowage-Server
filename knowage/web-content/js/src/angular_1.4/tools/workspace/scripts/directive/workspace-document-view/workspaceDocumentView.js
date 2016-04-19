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
			tableSpeedMenuOption:"=?",
			selectedDocument:"=?",
			selectDocumentAction:"&",
			deleteDocumentAction:"&",
			executeDocumentAction:"&",
			addToOrganizerAction:"&",
			orderingDocumentCards:"=?",
		},
		link: function (scope, elem, attrs) { 
			elem.css("position","relative")
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