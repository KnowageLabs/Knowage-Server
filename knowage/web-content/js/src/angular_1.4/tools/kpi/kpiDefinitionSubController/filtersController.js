app.controller('kpiDefinitionFiltersController',['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout', kpiDefinitionFiltersControllerFunction ]);


function kpiDefinitionFiltersControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;
	

	$scope.$on('activateFiltersEvent', function(e) {  

		$scope.loadPlaceHolder();
	});
	

	$scope.loadPlaceHolder = function(){
		sbiModule_restServices.post("1.0/kpi", 'listPlaceholderByMeasures',$scope.kpi.definition.measures).success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {

						$scope.placeHolderList=data;
						console.log($scope.placeHolderList);
					}

				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);

				})
	}
}