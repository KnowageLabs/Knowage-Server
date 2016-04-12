angular.module('workspace.controller', ['workspace.directive','workspace.configuration'])
.controller('workspaceController',["$scope","$http","$mdDialog","sbiModule_translate","sbiModule_restServices",workspaceFunction]);

function workspaceFunction($scope,$http,$mdDialog,sbiModule_translate,sbiModule_restServices){
	
	$scope.folderDocuments = [];
	$scope.loadFolderDocuments=function(folderId){
		sbiModule_restServices.promiseGet("2.0","documents/getDocumentsByFolder?folderId=" +folderId)
		.then(function(response) {
			angular.copy(response.data,$scope.folderDocuments);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadFolders=function(){
		sbiModule_restServices.promiseGet("2.0/folders", "")
		.then(function(response) {
			angular.copy(response.data,$scope.folders);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadFolders();
	console.log($scope.folderDocuments);
}