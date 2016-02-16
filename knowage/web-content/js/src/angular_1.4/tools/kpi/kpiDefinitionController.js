var app = angular.module('kpiDefinitionManager', [ 'ngMaterial',  'angular_table' ,'sbiModule', 'angular-list-detail']);

app.controller('kpiDefinitionMasterController', ['$scope','sbiModule_translate', kpiDefinitionMasterControllerFunction ]);

function kpiDefinitionMasterControllerFunction($scope,sbiModule_translate){
	$scope.translate=sbiModule_translate;
}