angular.module('importExportDocumentModule').controller('importControllerStep2', ["$scope",importStep2FuncController]);

function importStep2FuncController($scope) {
	$scope.nextStep = function(){
		$scope.stepControl.insertBread({name:"step3"})
	}
}
