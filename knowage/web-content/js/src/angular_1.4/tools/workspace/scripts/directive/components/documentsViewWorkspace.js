angular
	.module('documents_view_workspace', [])

	.directive('documentsViewWorkspace', function () {		
		 return {			 
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/documentsViewWorkspace.html',
		      controller: documentsController
		  };	  
	})

function documentsController($scope,sbiModule_restServices,sbiModule_translate,$window,$mdSidenav){
	
	$scope.selectedDocument = undefined;
	$scope.showDocumentInfo = false;
	
	$scope.loadAllDocuments=function(){
		sbiModule_restServices.promiseGet("2.0/documents", "")
		.then(function(response) {
			angular.copy(response.data,$scope.allDocuments);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	
	$scope.loadAllDocuments();
	
	$scope.showDocumentDetails = function() {
		return $scope.showDocumentInfo && $scope.isSelectedDocumentValid();
	};
	
	
	$scope.isSelectedDocumentValid = function() {
		return $scope.selectedDocument !== undefined;
	};
	
	$scope.setDetailOpen = function(isOpen) {
		if (isOpen && !$mdSidenav('right').isLockedOpen() && !$mdSidenav('right').isOpen()) {
			$scope.toggleDocumentDetail();
		}

		$scope.showDocumentInfo = isOpen;
	};
	
	$scope.toggleDocumentDetail = function() {
		$mdSidenav('right').toggle();
	};
	
	$scope.selectDocument= function ( document ) { 
		if (document !== undefined) {
			$scope.lastDocumentSelected = document;
		}
		var alreadySelected = (document !== undefined && $scope.selectedDocument === document);
		$scope.selectedDocument = document;
		if (alreadySelected) {
			$scope.selectedDocument=undefined;
			$scope.setDetailOpen(!$scope.showDocumentDetail);
		} else {
			$scope.setDetailOpen(document !== undefined);
		}
	};

}