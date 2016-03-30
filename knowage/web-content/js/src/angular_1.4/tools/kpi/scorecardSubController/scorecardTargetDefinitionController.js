angular.module('scorecardManager').controller('scorecardTargetDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',scorecardTargetDefinitionControllerFunction ]);

function scorecardTargetDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	
	$scope.test="target";
}