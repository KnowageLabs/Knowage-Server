angular.module('workspace.controller', ['workspace.directive','workspace.configuration'])
.controller('workspaceController',["$scope","$http","$mdDialog","sbiModule_translate","sbiModule_restServices",workspaceFunction]);

function workspaceFunction($scope,$http,$mdDialog,sbiModule_translate,sbiModule_restServices){
	
	$scope.folderDocuments = [];

	$scope.loadAllDocuments=function(){
		sbiModule_restServices.promiseGet("2.0/documents", "")
		.then(function(response) {
			angular.copy(response.data,$scope.folderDocuments);
			console.log($scope.folderDocuments);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadAllDocuments();
	
}