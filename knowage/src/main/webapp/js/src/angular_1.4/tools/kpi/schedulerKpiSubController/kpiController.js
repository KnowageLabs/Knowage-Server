var app = angular.module('schedulerKpi').controller('kpiController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout',KPIControllerFunction ]);

function KPIControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;

	$scope.tableFunction={
			translate:sbiModule_translate,
			loadListKPI: function(item,evt){
				var promise = $scope.loadListKPI();
				promise.then(function(result){
					angular.copy(result,$scope.selectedScheduler.kpis);
				});
			},
	}

	$scope.measureMenuOption= [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,
		backgroundColor:'transparent',	
		action : function(item,event) {
			$scope.removeKpi(item);
		}

	}];

	$scope.removeKpi = function(item){
		var confirm = $mdDialog.confirm()
		.title($scope.translate.load("sbi.kpi.measure.delete.title"))
		.content($scope.translate.load("sbi.kpi.measure.delete.content"))
		.ariaLabel('delete kpi') 
		.ok($scope.translate.load("sbi.general.yes"))
		.cancel($scope.translate.load("sbi.general.No"));
		$mdDialog.show(confirm).then(function() {
			if($scope.exists(item)){
				var index = $scope.indexInList(item, $scope.selectedScheduler.kpis);
				$scope.selectedScheduler.kpis.splice(index,1);
			}

		}, function() {
		});

	}

	$scope.loadListKPI = function(){
		var deferred = $q.defer();
		if($scope.selectedScheduler.kpis==undefined){
			$scope.selectedScheduler.kpis = [];
		} else if($scope.kpiSelected.length==0){
			angular.copy($scope.selectedScheduler.kpis,$scope.kpiSelected);
		}
		angular.copy($scope.selectedScheduler.kpis,$scope.kpiSelected);
		$mdDialog.show({
			controller: DialogControllerKPI,
			templateUrl: 'templatesaveKPI.html',
			clickOutsideToClose:true,
			preserveScope:true,
			locals: {items: deferred,kpi:$scope.kpi,kpiAllList:$scope.kpiAllList,engine:$scope.selectedScheduler, kpiSelected: $scope.kpiSelected, translate:sbiModule_translate}
		})
		.then(function(answer) {
			$scope.status = 'You said the information was "' + answer + '".';
			return deferred.promise;
		}, function() {
			$scope.status = 'You cancelled the dialog.';
		});
		return deferred.promise;
	};

	$scope.exists = function (item) {
		if($scope.selectedScheduler.kpis==undefined)return false;
		return  $scope.indexInList(item, $scope.selectedScheduler.kpis)!=-1;

	};


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

function DialogControllerKPI($scope,$mdDialog,items,kpi,kpiAllList,engine,kpiSelected, translate){
	//controller mdDialog to select kpi 
	$scope.translate=translate;
	$scope.tableFunction={
			exists: function(item,evt){
				return $scope.exists(item);
			}
	}
	$scope.kpi=kpi;
	$scope.kpiAllList = kpiAllList;
	$scope.selectedScheduler = engine;
	$scope.kpiSelected = kpiSelected;

	$scope.exists = function (item) {
		return  $scope.indexInList(item, $scope.selectedScheduler.kpis)==-1;

	};


	$scope.indexInList=function(item, list) {
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.id==item.id){
				return i;
			}
		}
		return -1;
	};
	$scope.close = function(){
		$mdDialog.cancel();
	}
	$scope.apply = function(){
		$mdDialog.cancel();
		items.resolve($scope.kpiSelected);
	}

	$scope.addKPIToCheck = function(){
		items.resolve($scope.kpiSelected);
		$mdDialog.cancel();
	}


}




