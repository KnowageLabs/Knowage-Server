(function() {
	var kpiViewerModule = angular.module('kpiViewerModule');
	
	kpiViewerModule.controller('kpiViewerController', 
			['$scope', 'documentData','sbiModule_restServices', kpiViewerControllerFn]);
	
	function kpiViewerControllerFn($scope, documentData,sbiModule_restServices) {
		$scope.documentData = documentData;
		
		$scope.init = function(){
			sbiModule_restServices.promisePost("1.0/jsonKpiTemplate","readKpiTemplate",$scope.documentData.template)
			.then(function(response){ 
				console.log("WOW",response.data);
			});
		}
		
	};
	
	
	
})();