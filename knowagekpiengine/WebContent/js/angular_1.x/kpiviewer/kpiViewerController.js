(function() {
	var kpiViewerModule = angular.module('kpiViewerModule');
	
	kpiViewerModule.controller('kpiViewerController', 
			['$scope', 'documentData', kpiViewerControllerFn]);
	
	function kpiViewerControllerFn($scope, documentData) {
		$scope.documentData = documentData;
	};
	
})();