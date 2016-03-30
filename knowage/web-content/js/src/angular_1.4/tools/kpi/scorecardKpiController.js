var scorecardApp = angular.module('scorecardManager', [ 'ngMaterial',  'angular_table' ,'angular_list','sbiModule', 'angular-list-detail']);
scorecardApp.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

scorecardApp.controller('scorecardMasterController', [ '$scope','sbiModule_translate','sbiModule_restServices','$angularListDetail','$timeout',scorecardMasterControllerFunction ]);
scorecardApp.controller('scorecardListController', [ '$scope','sbiModule_translate','sbiModule_restServices','$angularListDetail','$timeout',scorecardListControllerFunction ]);
scorecardApp.controller('scorecardDetailController', [ '$scope','sbiModule_translate','sbiModule_restServices','$angularListDetail','$timeout',scorecardDetailControllerFunction ]);

function scorecardMasterControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.emptyScorecard={};
	$scope.currentScorecard={};
}

function scorecardListControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$angularListDetail,$timeout){
	$scope.newScorecardFunction=function(){
		angular.copy($scope.emptyScorecard,$scope.currentScorecard);  
		$angularListDetail.goToDetail();
	};
}

function scorecardDetailControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$angularListDetail,$timeout){
 
}