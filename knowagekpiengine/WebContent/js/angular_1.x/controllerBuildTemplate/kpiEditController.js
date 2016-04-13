var app = angular.module('templateBuid', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'expander-box','dinamic-list']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('templateBuildController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$timeout',templateBuildControllerFunction ]);

function templateBuildControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.addKpis = [];
	$scope.selectedKpis = [];
	
}