var app = angular.module('kpiTarget', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'angular-list-detail','ui.codemirror','color.picker','angular_list']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('kpiTargetController', ['$scope','sbiModule_translate', 'sbiModule_restServices','$mdDialog','$q','$mdToast','$timeout',kpiTargetControllerFunction ]);

function kpiTargetControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdDialog,$q,$mdToast,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.target = {};
	//retry it after with a service rest
	$scope.targets = [
	                  {
	                	  'name':'Target1',
	                	  'category':'Categoria 1',
	                	  'startValidation':"2/03/2016",
	                	  'endValidation':"3/03/2016"
	                  },
	                  {
	                	  'name':'Target2',
	                	  'category':'Categoria 2',
	                	  'startValidation':"5/03/2016",
	                	  'endValidation':"10/04/2016"
	                  },
	                  {
	                	  'name':'Target3',
	                	  'category':'Categoria 3',
	                	  'startValidation':"1/03/2016",
	                	  'endValidation':"1/03/2017"
	                  },
	                  ];
	
	

}






