var app = angular.module('alertDefinitionManager', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'angular-list-detail','angular_list',"expander-box",'ngRoute','ngWYSIWYG','cron_frequency']);
app.config(['$mdThemingProvider', function($mdThemingProvider,$routeProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
	
	
}]);

app.config(function($routeProvider){
    $routeProvider
        .when('/alarmRouteProvider/:url*', {
            templateUrl: function(urlAttr){ 
            	return "/"+urlAttr.url;
            }
        })
        ;
});

app.controller('alertDefinitionController', ['$scope','sbiModule_translate', 'sbiModule_restServices','$mdDialog','$q','$mdToast','$timeout','$location','sbiModule_config',alertDefinitionControllerFunction ]);

function alertDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdDialog,$q,$mdToast,$timeout,$location,sbiModule_config){
	$scope.translate=sbiModule_translate; 
	$scope.alert = {
		selectedListener: {},
//		frequency:{}
		frequency:{"startTime":"12:03","endTime":"02:03","crono":{"type":"week","parameter":{"days":["1","2","3","5","6"]}},"startDate":"2016-04-11T22:00:00.000Z","endDate":"2016-04-18T22:00:00.000Z"}
	};
	$scope.listeners=[];

	$scope.listenerIsSelected=function(){
		return !angular.equals({},$scope.alert.selectedListener);
	}
	
	sbiModule_restServices.promiseGet("1.0/alert", 'listListener')
	.then(function(response){ 
		$scope.listeners=response.data;
	},function(response){
		sbiModule_restServices.errorHandler(response.data,"");
	});

	$scope.changeListener = function(){
		$location.path(  "alarmRouteProvider"+sbiModule_config.contextName+""+$scope.alert.selectedListener.template);
	} 
}





