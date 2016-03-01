var app = angular.module('kpiDefinitionManager', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'angular-list-detail','ui.codemirror']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);
app.controller('kpiDefinitionMasterController', ['$scope','sbiModule_translate', 'sbiModule_restServices',kpiDefinitionMasterControllerFunction ]);

function kpiDefinitionMasterControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.translate=sbiModule_translate;
	$scope.checkFormula = false;
	$scope.kpi = {};
	
	$scope.parseFormula = function(){
		 $scope.$broadcast ('parseEvent');
		
	}
	
	
	$scope.showAction = function(text) {
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	};
}