angular.module('scorecardManager').controller('scorecardDefinitionController', [ '$scope','$mdDialog', 'sbiModule_translate' ,'sbiModule_restServices',scorecardDefinitionControllerFunction ]);

function scorecardDefinitionControllerFunction($scope,$mdDialog, sbiModule_translate,sbiModule_restServices){
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
	 
	
	$scope.deletePerspective = function(target, $index){
		var confirm = $mdDialog.confirm()
	    .title(sbiModule_translate.load("sbi.kpi.delete.progress"))
	    .content(sbiModule_translate.load("sbi.layer.delete.progress.message.delete"))
	    .ariaLabel('cancel perspective') 
	    .ok(sbiModule_translate.load("sbi.general.yes"))
	    .cancel(sbiModule_translate.load("sbi.general.No"));
	      $mdDialog.show(confirm).then(function() {
	    	  $scope.currentScorecard.perspectives.splice($index,1);
	      });
	}
	
}