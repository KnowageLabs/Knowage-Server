var app = angular.module('schedulerKpi').controller('listSchedulerController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout',KPIDefinitionListControllerFunction ]);

function KPIDefinitionListControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;


	$scope.addScheduler= function(){

		$angularListDetail.goToDetail();


	}
	$scope.loadEngine = function(item){
		$scope.selectedScheduler.name = item.name;
		//rest service to load kpis

		sbiModule_restServices.promiseGet("2.0/domains","listByCode/KPI_KPI_CATEGORY")
		.then(function(response){ 
			angular.copy(response.data,$scope.AttributeCategoryList);
		},function(response){

		});
		sbiModule_restServices.promiseGet("1.0/kpi", 'listMeasure')
		.then(function(response){ 

			$scope.measures=response.data;
		},function(response){
			$scope.errorHandler(response.data,"");
		});

		$scope.selectedScheduler.kpi = item.kpi;
		$scope.selectedScheduler.startDate = item.startDate;
		$scope.selectedScheduler.endDate = item.endDate;
		$scope.selectedScheduler.author = item.author;
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