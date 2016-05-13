angular
	.module('recent_view_workspace', [])

	.directive('recentViewWorkspace', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/recentViewWorkspace.html',
		      controller: recentController
		  };
	});

function recentController($scope,sbiModule_restServices,sbiModule_translate,$documentViewer){
	
	$scope.loadRecentDocumentExecutionsForUser =function(){
		sbiModule_restServices.promiseGet("2.0/recents","")
		.then(function(response) {
			angular.copy(response.data,$scope.recentDocumetnsList);
			$scope.recentDocumentsInitial = $scope.recentDocumetnsList;
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadRecentDocumentExecutionsForUser();
	
	$scope.recentSpeedMenu=[{
		label : sbiModule_translate.load('sbi.generic.run'),
		icon:'fa fa-play-circle' ,
		backgroundColor:'transparent',	
		action : function(item,event) {
			$scope.executeRecent(item);
		}
	} ];
	
	$scope.executeRecent = function(document) {
		$documentViewer.openDocument(document.objId, document.documentLabel, document.documentName);
	}
	
}
