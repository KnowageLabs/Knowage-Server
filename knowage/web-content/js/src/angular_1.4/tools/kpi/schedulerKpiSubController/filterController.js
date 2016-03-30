var app = angular.module('schedulerKpi').controller('filterController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout',KPIControllerFunction ]);

function KPIControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;
	
}