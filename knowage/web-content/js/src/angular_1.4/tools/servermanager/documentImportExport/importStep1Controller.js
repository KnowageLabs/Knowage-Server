angular.module('importExportDocumentModule').controller('importControllerStep1', ["$scope",importStep1FuncController]);

function importStep1FuncController($scope,importExportDocumentModule_importConf) {
	$scope.nextStep = function(){
		$scope.stepControl.insertBread({name:"step2"})
	}
	
	
	
	
	
}
