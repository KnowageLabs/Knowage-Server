angular.module('importExportDocumentModule').controller('importControllerStep4', ["$scope",importStep4FuncController]);

function importStep4FuncController($scope) {
	$scope.nextStep = function(){
		alert("FINITO")
		$scope.stepControl.resetBreadCrumb();
		$scope.stepControl.insertBread({name:"step1"})
	}
}
