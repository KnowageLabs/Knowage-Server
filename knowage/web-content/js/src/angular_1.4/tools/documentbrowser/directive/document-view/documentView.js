angular.module('document_view', ['ngMaterial'])
.directive('documentView', function() {
	return {
		 templateUrl: '/knowage/js/src/angular_1.4/tools/documentbrowser/directive/document-view/document-view.html',
		controller: documentViewControllerFunction,
		 priority: 10,
		scope: {
			ngModel:"=",
			showGridView:"=?",
			tableSpeedMenuOption:"=?",
			selectedDocument:"=?",
			selectDocumentAction:"&",
			editDocumentAction:"&",
			cloneDocumentAction:"&",
			deleteDocumentAction:"&",
			executeDocumentAction:"&",
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

function documentViewControllerFunction($scope){
	$scope.clickDocument=function(item){
		
		 $scope.selectDocumentAction({doc: item});
	}
}