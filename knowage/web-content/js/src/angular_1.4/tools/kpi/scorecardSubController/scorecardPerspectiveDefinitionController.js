angular.module('scorecardManager').controller('scorecardPerspectiveDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',scorecardPerspectiveDefinitionControllerFunction ]);

function scorecardPerspectiveDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.addTarget=function(){ 
		$scope.stepControl.insertBread({name: sbiModule_translate.load('sbi.kpi.scorecard.goal.definition.name')});
		angular.copy($scope.emptyTarget,$scope.currentTarget);
	};
}

