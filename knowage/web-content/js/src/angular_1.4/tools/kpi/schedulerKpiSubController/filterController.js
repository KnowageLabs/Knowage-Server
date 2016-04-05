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

	$scope.isVisible = function(kpi){
		var index = -1;
		var index2 = -1;
		if($scope.selectedScheduler.filters!=undefined){
			index = $scope.indexInList(kpi.name,$scope.selectedScheduler.filters,"kpiName");
		}else{
			index = -1;
		}
		var keys = Object.keys($scope.placeHolder);
		index2 = $scope.indexInList(kpi.name,keys,null);
		if($scope.placeHolder[keys[index2]]=="[]"){
			index2=-1;
		}
		if(index==-1 && index2==-1){
			return false;
		}

		return true;
	}

	$scope.indexInList=function(item, list,param) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(param!=null){
				if(object[param]==item){
					return i;
				}
			}else{
				if(object==item){
					return i;
				}
			}

		}

		return -1;
	};
}