var app = angular.module('schedulerKpi').controller('listSchedulerController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout',KPIDefinitionListControllerFunction ]);

function KPIDefinitionListControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;

	
	$scope.loadEngine = function(item){
//		$scope.selectedScheduler.name = item.name;
		sbiModule_restServices.promiseGet("1.0/kpi",item.id+"/loadSchedulerKPI")
		.then(function(response){ 
			$scope.fixDataAfterLoad(response.data);

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