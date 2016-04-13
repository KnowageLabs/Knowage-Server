angular
	.module('documents_view_workspace', [])

	/**
	 * The HTML content of the Recent view (recent documents).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	.directive('documentsViewWorkspace', function () {		
		 return {			 
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/documentsViewWorkspace.html',
		      controller: documentsController
		  };	  
	})

function documentsController($scope,sbiModule_restServices,sbiModule_translate){
	
	$scope.loadAllDocuments=function(){
		sbiModule_restServices.promiseGet("2.0/documents", "")
		.then(function(response) {
			angular.copy(response.data,$scope.allDocuments);
			console.log($scope.allDocuments);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	
	$scope.loadAllDocuments();
}