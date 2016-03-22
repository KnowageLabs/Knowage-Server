var app = angular.module('kpiTarget', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'angular-list-detail','ui.codemirror','color.picker','angular_list']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('kpiTargetController', ['$scope','sbiModule_translate', 'sbiModule_restServices','$mdDialog','$q','$mdToast','$timeout',kpiTargetControllerFunction ]);

function kpiTargetControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdDialog,$q,$mdToast,$timeout){
	$scope.translate=sbiModule_translate;


}






