angular.module('scorecardManager').controller('scorecardDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',scorecardDefinitionControllerFunction ]);

function scorecardDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.addPerspective=function(editPerspective, index){ 
		if(editPerspective == undefined)
		{
			angular.copy($scope.emptyPerspective,$scope.currentPerspective); 
			$scope.editProperty.perspective.index = undefined;
		}
		else
			{
				$scope.editProperty.perspective.index = index;
				angular.copy(editPerspective,$scope.currentPerspective);
			}

		$scope.stepControl.insertBread({name: sbiModule_translate.load('sbi.kpi.scorecard.perspective.definition.name')});
	};

	
}