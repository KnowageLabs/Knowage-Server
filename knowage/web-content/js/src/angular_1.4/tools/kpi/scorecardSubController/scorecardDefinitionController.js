angular.module('scorecardManager').controller('scorecardDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',scorecardDefinitionControllerFunction ]);

function scorecardDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.addPerspective=function(){ 
		angular.copy($scope.emptyPerspective,$scope.currentPerspective);
		$scope.stepControl.insertBread({name: 'definizione prospettiva'});
	};

	$scope.scorecardName = undefined;
	
}