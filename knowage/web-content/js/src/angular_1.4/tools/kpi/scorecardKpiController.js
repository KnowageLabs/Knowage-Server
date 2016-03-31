var scorecardApp = angular.module('scorecardManager', [ 'ngMaterial',  'angular_table' ,'angular_list','sbiModule', 'angular-list-detail','bread_crumb','kpi_semaphore_indicator']);
scorecardApp.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

scorecardApp.controller('scorecardMasterController', [ '$scope','sbiModule_translate','sbiModule_restServices','$angularListDetail','$timeout',scorecardMasterControllerFunction ]);
scorecardApp.controller('scorecardListController', [ '$scope','sbiModule_translate','sbiModule_restServices','$angularListDetail','$timeout',scorecardListControllerFunction ]);
scorecardApp.controller('scorecardDetailController', [ '$scope','sbiModule_translate','sbiModule_restServices','$angularListDetail','$timeout',scorecardDetailControllerFunction ]);

function scorecardMasterControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.emptyScorecard={name:"",perspectives:[]};
	$scope.emptyPerspective={name:"",criterion:{},status:"",groupedKpis:[],targets:[]};
	$scope.emptyTarget={name:"",criterion:{},status:"",groupedKpis:[],kpis:[]};
	$scope.currentScorecard= {};
	$scope.currentPerspective = {};
	$scope.currentTarget = {};
	$scope.selectedStep={value:0};
}

function scorecardListControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$angularListDetail,$timeout){
	$scope.scorecardList=[];
	$scope.scorecardColumnsList=[
	                             {label:"Name",name:"name"},
	                             {label:"Data",name:"date"},
	                             {label:"Author",name:"author"}];
	
	$scope.newScorecardFunction=function(){
		angular.copy($scope.emptyScorecard,$scope.currentScorecard);  
		for(var i=0;i<2;i++){
			var tmp=angular.extend({}, $scope.emptyPerspective);
			tmp.name="Prospettiva"+1;
			tmp.groupedKpis=[{status:"RED",count:2},{status:"YELLOW",count:1},{status:"GREEN",count:3}];
			$scope.currentScorecard.perspectives.push(tmp);
		}
		
		
		$angularListDetail.goToDetail();
	};
}

function scorecardDetailControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$angularListDetail,$timeout){
	$scope.stepItem=[{name:'scorecard definition'}];
	
	$scope.stepControl; 
	$scope.criterionTypeList = [];
	
	$scope.saveScorecardFunction=function(){
		console.log($scope.currentScorecard);
		console.log($scope.currentPerspective);
		console.log($scope.currentTarget);
	}
	
	sbiModule_restServices.promiseGet("2.0/domains","listByCode/KPI_SCORECARD_CRITE")
	.then(function(response){ 
		angular.copy(response.data,$scope.criterionTypeList); 
	},function(response){
		sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.generic.error")+" domains->KPI_SCORECARD_CRITE"); 
	});
}