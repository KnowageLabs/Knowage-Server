var app = angular.module('schedulerKpi').controller('listSchedulerController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout',KPIDefinitionListControllerFunction ]);

function KPIDefinitionListControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;


	$scope.addScheduler= function(){
		angular.copy({},$scope.selectedScheduler);
		angular.copy([],$scope.kpi);
		angular.copy([],$scope.kpiSelected);
		$angularListDetail.goToDetail();


	}
	$scope.loadEngine = function(item){
//		$scope.selectedScheduler.name = item.name;
		sbiModule_restServices.promiseGet("1.0/kpi",item.id+"/loadSchedulerKPI")
		.then(function(response){ 
			angular.copy(response.data,$scope.selectedScheduler);
			angular.copy(response.data.kpis,$scope.selectedScheduler.kpis);
			if($scope.selectedScheduler.kpis!=undefined){
				for(var i=0;i<$scope.selectedScheduler.kpis.length;i++){
					$scope.selectedScheduler.kpis[i]["valueCd"] = $scope.selectedScheduler.kpis[i].category.valueCd;
				}
			}

		},function(response){

		});

		$angularListDetail.goToDetail();
	}

	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.id==item.id){
				return i;
			}
		}

		return -1;
	};
}