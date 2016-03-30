angular.module('scorecardManager').controller('scorecardPerspectiveDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',scorecardPerspectiveDefinitionControllerFunction ]);

function scorecardPerspectiveDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.addTarget=function(){ 
		$scope.stepControl.insertBread({name: 'definizione obiettivo'});
	};
	$scope.test="nikabot";
}