angular.module('importExportDocumentModule').controller('importControllerStep3', ["$scope",importStep3FuncController]);

function importStep3FuncController($scope) {
	$scope.nextStep = function(){
		$scope.stepControl.insertBread({name:"step4"})
	}
}
