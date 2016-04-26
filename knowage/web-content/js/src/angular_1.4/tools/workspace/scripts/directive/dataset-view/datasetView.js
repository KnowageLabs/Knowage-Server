angular.module('dataset_view', ['ngMaterial'])
.directive('datasetView', function() {
	return {
		templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/scripts/directive/dataset-view/dataset-view.html',
		controller: datasetViewControllerFunction,
		 priority: 10,
		scope: {
			ngModel:"=",
			showGridView:"=?",
			tableSpeedMenuOption:"=?",
			selectedDataset:"=?",
			selectDatasetAction:"&",
			shareDatasetAction:"&",
			shareDatasetEnabled:"=?",
			previewDatasetAction:"&",
			//cloneDocumentAction:"&",
			showQbeDatasetAction:"&",
			
			showQbeEnabled:"=?",
			showDetailDatasetAction:"&",
			showDetailDatasetEnabled:"=?",
			//executeDocumentAction:"&",
			orderingDatasetCards:"=?",
		},
		
		link: function (scope, elem, attrs) { 
			
			 elem.css("position","static");
			 
			 if(!attrs.tableSpeedMenuOption){
				 scope.tableSpeedMenuOption=[];
			 }
			
//			if(document.getElementsByTagName("body")[0].id == "workspaceWebPageBody")  {
//			    elem.css("position","static");
//			 }
//			 else {
//			    elem.css("position","relative")
//			 }
			
		}
	}
});

function datasetViewControllerFunction($scope){
	$scope.clickDataset=function(item){
		
		 $scope.selectDatasetAction({ds: item});
		 
	}
	

}