angular.module('scorecardManager').controller('scorecardPerspectiveDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices','$mdDialog','$mdToast',scorecardPerspectiveDefinitionControllerFunction ]);

function scorecardPerspectiveDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdDialog,$mdToast){
	
	$scope.$on('savePerspective', function(event, args) {
		 if($scope.currentPerspective.name.trim()==""){
			 $mdToast.show(
				      $mdToast.simple()
				        .content('Name is required')
				        .position("TOP")
				        .hideDelay(3000)
				    );
			 return;
		}
		if($scope.currentPerspective.targets==undefined || $scope.currentPerspective.targets.length==0){
			 $mdToast.show(
				      $mdToast.simple()
				        .content('Add at least one targets ')
				        .position("TOP")
				        .hideDelay(3000)
				    );
			 return;
		}
		
		
		$scope.currentScorecard.perspectives.push(angular.extend({},$scope.currentPerspective));
		angular.copy($scope.emptyPerspective,$scope.currentPerspective);
		$scope.stepControl.prevBread();
 	});
	
	$scope.$on('cancelPerspective', function(event, args) {
		if(!angular.equals($scope.emptyPerspective,$scope.currentPerspective)){
	 		var confirm = $mdDialog.confirm()
	        .title(sbiModule_translate.load("sbi.layer.modify.progress"))
	        .content(sbiModule_translate.load("sbi.layer.modify.progress.message.modify"))
	        .ariaLabel('cancel perspective') 
			.ok(sbiModule_translate.load("sbi.general.yes"))
			.cancel(sbiModule_translate.load("sbi.general.No"));
			  $mdDialog.show(confirm).then(function() {
					$scope.stepControl.prevBread();
			  }, function() {
			   return;
			  });
 		}else{
 			$scope.stepControl.prevBread();
 		} 
 	});
	
	$scope.addTarget=function(){ 
		$scope.stepControl.insertBread({name: sbiModule_translate.load('sbi.kpi.scorecard.goal.definition.name')});
		angular.copy($scope.emptyTarget,$scope.currentTarget);
	};
}

