var app = angular.module('schedulerKpi').controller('filterController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout',KPIControllerFunction ]);

function KPIControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.listType = [];
	$scope.funcTemporal = [];
	$scope.lov = [];
	
	$scope.loadKpiType = function(){
		sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=KPI_PLACEHOLDER_TYPE")
		.then(function(response){ 
			angular.copy(response.data,$scope.listType);
			console.log("type",$scope.listType);
		},function(response){
		});
		
		sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=KPI_PLACEHOLDER_FUNC")
		.then(function(response){ 
			angular.copy(response.data,$scope.funcTemporal);
			console.log("typefunc",$scope.funcTemporal);
		},function(response){
		});
		
		sbiModule_restServices.promiseGet("2.0/lovs","")
		.then(function(response){ 
			angular.copy(response.data,$scope.lov);
			console.log("typelov",$scope.lov);
		},function(response){
		});
	}

	$scope.loadKpiType();
}