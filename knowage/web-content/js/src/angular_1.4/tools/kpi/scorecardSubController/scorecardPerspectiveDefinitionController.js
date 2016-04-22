angular.module('scorecardManager').controller('scorecardPerspectiveDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices','$mdDialog','$mdToast','scorecardManager_perspectiveUtility','scorecardManager_semaphoreUtility','scorecardManager_targetUtility',scorecardPerspectiveDefinitionControllerFunction ]);


angular.module('scorecardManager').service('scorecardManager_perspectiveUtility',function(scorecardManager_semaphoreUtility, $q, sbiModule_restServices){
		
	this.addGroupedTargetsItem=function(perspective,type){
		for(var i=0;i<perspective.groupedTargets.length;i++){
			if(angular.equals(perspective.groupedTargets[i].status,type)){
				perspective.groupedTargets[i].count++;
				return;
			}
		}
		 perspective.groupedTargets.push({status:type,count:1});
	}
	
	this.addTotalGroupedKpisItem=function(perspective,target){
		
		for(var i=0;i<target.groupedKpis.length;i++){
			var tmpGroupedKpis=target.groupedKpis[i];
			var find=false;
			for(var j=0;j<perspective.groupedKpis.length;j++){
				if(angular.equals( perspective.groupedKpis[j].status,tmpGroupedKpis.status)){
					perspective.groupedKpis[j].count+=tmpGroupedKpis.count;
					find=true;
					break;
				}
			}
			if(!find){
				perspective.groupedKpis.push({status:tmpGroupedKpis.status,count:tmpGroupedKpis.count});
			}
		}
		
	}
	
	this.loadPerspectiveStatus = function( idCr, arrayData, deferred){
		sbiModule_restServices.promisePost("1.0/kpi",idCr + "/evaluateCriterion",arrayData)
		.then(function(response){
			deferred.resolve(response.data.status);
		},
				function(response) {
			deferred.reject();
		}
		);
	}
	
	this.loadGroupedTarget=function(selPerspective){
		var deferred=$q.defer();
		if(!selPerspective.hasOwnProperty("groupedTargets")){
			selPerspective.groupedTargets=[];
		} 
		if(!selPerspective.hasOwnProperty("groupedKpis")){
			selPerspective.groupedKpis = [];
		}
		for(var i=0;i<selPerspective.targets.length;i++){
			this.addGroupedTargetsItem(selPerspective,selPerspective.targets[i].status);
			this.addTotalGroupedKpisItem(selPerspective,selPerspective.targets[i]);
		}
		//selPerspective.status=this.getPerspectiveStatus(selPerspective);
		var statusArray = [];
		for(i=0; i < selPerspective.targets.length;i++)
			statusArray.push({status: selPerspective.targets[i].status, priority: false });
		
		for(i=0; i <selPerspective.options.criterionPriority.length;i++)
			for(j=0; j < selPerspective.targets.length;j++)
				if (selPerspective.options.criterionPriority[i].id == selPerspective.targets[j].id)
					statusArray[i].priority = true;
		this.loadPerspectiveStatus( selPerspective.criterion.valueId, statusArray, deferred);
		return deferred.promise;
	};
});




function scorecardPerspectiveDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdDialog,$mdToast,scorecardManager_perspectiveUtility,scorecardManager_semaphoreUtility,scorecardManager_targetUtility){
	 
	$scope.$on('savePerspective', function(event, args) {
		 if($scope.currentPerspective.name.trim()==""){
				$scope.showToast(sbiModule_translate.load("sbi.kbi.scorecard.alert.name.missing"));
			 return;
		}
		if($scope.currentPerspective.targets==undefined || $scope.currentPerspective.targets.length==0){
			$scope.showToast(sbiModule_translate.load("sbi.kbi.scorecard.alert.target.missing"));
			 return;
		}

		if ($scope.editProperty.perspective.index != undefined){
			$scope.currentPerspective.groupedTargets=[];
			$scope.currentPerspective.groupedKpis=[];
			scorecardManager_perspectiveUtility.loadGroupedTarget($scope.currentPerspective)
			.then(
					function(response){
						$scope.currentPerspective.status = response;
						angular.copy($scope.currentPerspective,$scope.currentScorecard.perspectives[$scope.editProperty.perspective.index]);
						angular.copy($scope.emptyPerspective,$scope.currentPerspective);

						$scope.steps.stepControl.prevBread();
					},
					function(){});
		}
		else{
			scorecardManager_targetUtility.loadGroupedKpis($scope.currentTarget)
			.then(
					function(response){
						$scope.currentTarget.status = response;
						$scope.currentScorecard.perspectives.push(angular.extend({},$scope.currentPerspective));	
						angular.copy($scope.emptyTarget,$scope.currentTarget);

						$scope.steps.stepControl.prevBread();
					}
					,
					function(){});
			
		}
	
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
					$scope.steps.stepControl.prevBread();
			  }, function() {
			   return;
			  });
 		}else{
 			$scope.steps.stepControl.prevBread();
 		} 
 	});
	
	$scope.addTarget=function(editTarget, index){ 
		$scope.steps.stepControl.insertBread({name: sbiModule_translate.load('sbi.kpi.scorecard.goal.definition.name')});
		if(editTarget == undefined)
		{
			angular.copy($scope.emptyTarget,$scope.currentTarget);
			$scope.editProperty.target.index = undefined;
		}
		else
			{
				$scope.editProperty.target.index = index;
				angular.copy(editTarget,$scope.currentTarget);
			}
		
	};
	

	$scope.deleteTarget = function(target, $index){
		var confirm = $mdDialog.confirm()
	    .title(sbiModule_translate.load("sbi.kpi.delete.progress"))
	    .content(sbiModule_translate.load("sbi.layer.delete.progress.message.delete"))
	    .ariaLabel('cancel perspective') 
	    .ok(sbiModule_translate.load("sbi.general.yes"))
	    .cancel(sbiModule_translate.load("sbi.general.No"));
	      $mdDialog.show(confirm).then(
	    		  function() {
	    	    	  $scope.currentPerspective.targets.splice($index,1);
	    	    	  $scope.updateCriterionPriority();
	    		  });
	};
	
	$scope.updateCriterionPriority=function(){
		if( $scope.currentPerspective.options.hasOwnProperty("criterionPriority") && $scope.currentPerspective.options.criterionPriority.length>0){
			for(var cp=0;cp<$scope.currentPerspective.options.criterionPriority.length;cp++){
				if($scope.itemNameInList($scope.currentPerspective.targets,$scope.currentPerspective.options.criterionPriority[cp])==-1){
					$scope.currentPerspective.options.criterionPriority.splice(cp,1);
					cp--;
				}
			}
		}
	}

	
}

