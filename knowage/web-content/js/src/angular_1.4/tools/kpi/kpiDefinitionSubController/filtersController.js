app.controller('kpiDefinitionFiltersController',['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout', kpiDefinitionFiltersControllerFunction ]);


function kpiDefinitionFiltersControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;


	$scope.$on('activateFiltersEvent', function(e) {  

		$scope.loadPlaceHolder();
	});


	$scope.loadPlaceHolder = function(){
		sbiModule_restServices.promisePost("1.0/kpi", 'listPlaceholderByMeasures',$scope.kpi.definition.measures).then(
				function(response) {
					if (response.data.hasOwnProperty("errors")) {
						$scope.showAction(response.data);
					} else {
						$scope.placeHolderList=response.data;
						if($scope.formulaModified.value){
							for(key in Object.keys($scope.placeholder)){
								if($scope.placeHolderList.indexOf(Object.keys($scope.placeholder)[key])==-1){
									//remove placeholder
									delete $scope.placeholder[Object.keys($scope.placeholder)[key]];
								}
							
							}
							$scope.kpi.placeholder =$scope.placeholder;
						}
					}

				},function(response) {
					$scope.errorHandler(response.data,"");
				})
		
	}
}