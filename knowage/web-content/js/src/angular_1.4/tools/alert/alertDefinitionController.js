var app = angular.module('alertDefinitionManager', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'angular-list-detail','angular_list']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('alertDefinitionController', ['$scope','sbiModule_translate', 'sbiModule_restServices','$mdDialog','$q','$mdToast','$timeout',alertDefinitionControllerFunction ]);

function alertDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdDialog,$q,$mdToast,$timeout){
	$scope.translate=sbiModule_translate;

	$scope.alert = {
		selectedListener: ''
		,templateUrl: ''
	};
	$scope.listeners=[];

	sbiModule_restServices.promiseGet("1.0/alert", 'listListener')
	.then(function(response){ 
		$scope.listeners=response.data;
	},function(response){
		sbiModule_restServices.errorHandler(response.data,"");
	});

	$scope.changeListener = function(){
		$scope.alert.templateUrl = $scope.alert.selectedListener.template;
	}
	



}






