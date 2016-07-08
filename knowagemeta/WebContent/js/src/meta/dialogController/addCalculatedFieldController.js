function addCalculatedFieldController($scope, $mdDialog,sbiModule_translate,sbiModule_restServices, businessModel){
	$scope.translate=sbiModule_translate;
	$scope.cancel = function(){
		$mdDialog.cancel();
	}

}