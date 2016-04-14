angular.module('federation_view', ['ngMaterial'])
.directive('federationView', function() {
	return {
		 templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/scripts/directive/federation-view/federation-view.html',
		controller: federationViewControllerFunction,
		 priority: 10,
		scope: {
			ngModel:"=",
			showGridView:"=?",
			tableSpeedMenuOption:"=?",
			selectedFederation:"=?",
			selectFederationAction:"&",
			showFederationAction:"&",
			editFederationAction:"&",
			//cloneDocumentAction:"&",
			deleteFederationAction:"&",
			//executeDocumentAction:"&",
			orderingFederationCards:"=?",
		},
		
		link: function (scope, elem, attrs) { 
			elem.css("position","relative")
			 if(!attrs.tableSpeedMenuOption){
				 scope.tableSpeedMenuOption=[];
			 }
		}
	}
});

function federationViewControllerFunction($scope){
	$scope.clickDocument=function(item){
		
		 $scope.selectFederationAction({federation: item});
	}
}