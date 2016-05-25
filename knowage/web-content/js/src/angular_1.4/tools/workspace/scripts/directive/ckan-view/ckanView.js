angular.module('ckan_view', ['ngMaterial'])
.directive('ckanView', function() {
	return {
		templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/scripts/directive/ckan-view/ckan-view.html',
		controller: ckanViewControllerFunction,
		 priority: 10,
		scope: {
			ngModel:"=",
			showGridView:"=?",
			tableSpeedMenuOption:"=?",
			selectedCkan:"=?",
			selectCkanAction:"&",
			//shareDatasetAction:"&",
			//shareDatasetEnabled:"=?",
			//previewDatasetAction:"&",
			//cloneDocumentAction:"&",
			//showQbeDatasetAction:"&",
			showDetailCkanAction:"&",
			editCkanAction:"&",
			//showQbeEnabled:"=?",
			//showDetailDatasetAction:"&",
			//showDetailDatasetEnabled:"=?",
			//executeDocumentAction:"&",
			orderingCkanCards:"=?",
		},
		
		link: function (scope, elem, attrs) { 
			
			elem.css("position","static")
			 if(!attrs.tableSpeedMenuOption){
				 scope.tableSpeedMenuOption=[];
			 }
			
		}
	}
});

function ckanViewControllerFunction($scope){
	$scope.clickCkan=function(item){
		
		 $scope.selectCkanAction({ckan: item});
		 
	}
	

}