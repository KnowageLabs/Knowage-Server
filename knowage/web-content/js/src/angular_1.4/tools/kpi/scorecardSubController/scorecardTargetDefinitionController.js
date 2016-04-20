angular.module('scorecardManager').controller('scorecardTargetDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices','sbiModule_config','$filter','$mdDialog','$mdToast','scorecardManager_targetUtility','scorecardManager_semaphoreUtility','$timeout',scorecardTargetDefinitionControllerFunction ]);


angular.module('scorecardManager').service('scorecardManager_targetUtility',function(scorecardManager_semaphoreUtility,$q, sbiModule_restServices ){
	
	this.addGroupedKpisItem=function(target,type){
		for(var i=0;i<target.groupedKpis.length;i++){
			if(angular.equals(target.groupedKpis[i].status,type)){
				target.groupedKpis[i].count++;
				return;
			}
		}
			target.groupedKpis.push({status:type,count:1});
	}
	
	this.loadTargetStatus = function( idCr, arrayData, deferred){
		sbiModule_restServices.promisePost("1.0/kpi",idCr + "/evaluateCriterion",arrayData)
		.then(function(response){
			deferred.resolve(response.data.status);
		},
				function(response) {
			deferred.reject();
		}
		);
	}
	 
	this.loadGroupedKpis=function(selTarget){
		var deferred=$q.defer();
		if(!selTarget.hasOwnProperty("groupedKpis")){
			selTarget.groupedKpis = [];
		}
		
		for(var i=0;i<selTarget.kpis.length;i++){
			this.addGroupedKpisItem(selTarget,selTarget.kpis[i].status);
		}
		//selTarget.status=this.getTargetStatus(selTarget);
		var statusArray = [];
		for(i=0; i < selTarget.kpis.length;i++)
			statusArray.push({status: selTarget.kpis[i].status, priority: false });
		
		for(i=0; i <selTarget.options.criterionPriority.length;i++)
			for(j=0; j < selTarget.kpis.length;j++)
				if (selTarget.options.criterionPriority[i].id == selTarget.kpis[j].id)
					statusArray[i].priority = true;
		this.loadTargetStatus( selTarget.criterion.valueId, statusArray, deferred);
		return deferred.promise;
		}
	
});


function scorecardTargetDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices,sbiModule_config,$filter,$mdDialog,$mdToast,scorecardManager_targetUtility,scorecardManager_semaphoreUtility,$timeout){
	$scope.kpiList=[];
	$scope.targetListAction =  [
			           {
				              label : 'Remove',
				              icon:'fa fa-trash' , 
				              backgroundColor:'#trasparent',
				              action : function(item,event) {
				            	  $scope.currentTarget.kpis.splice( $scope.currentTarget.kpis.indexOf(item),1);
				            	  $scope.updateCriterionPriority();
				              }
				           }
					];
	
	$scope.updateCriterionPriority=function(){
		if( $scope.currentTarget.options.hasOwnProperty("criterionPriority") && $scope.currentTarget.options.criterionPriority.length>0){
			for(var cp=0;cp<$scope.currentTarget.options.criterionPriority.length;cp++){
				if($scope.itemNameInList($scope.currentTarget.kpis,$scope.currentTarget.options.criterionPriority[cp])==-1){
					$scope.currentTarget.options.criterionPriority.splice(cp,1);
					cp--;
				}
			}
		}
	}
	
	$scope.parseDate = function(date){
		result = "";
		if(date == "d/m/Y"){
			result = "dd/MM/yyyy";
		}
		if(date =="m/d/Y"){
			result = "MM/dd/yyyy"
		}
		return result;
	};
	
	$scope.getListKPI = function(){
		sbiModule_restServices.promiseGet("1.0/kpi","listKpiWithResult")
		.then(function(response){ 
			for(var i=0;i<response.data.length;i++){
				var obj = angular.extend({},response.data[i]);
				var dateFormat = $scope.parseDate(sbiModule_config.localizedDateFormat);
				//parse date based on language selected
				obj["datacreation"]=$filter('date')(response.data[i].dateCreation, dateFormat);
				obj.kpiSemaphore="<kpi-semaphore-indicator indicator-color=\"'"+obj.status+"'\"></kpi-semaphore-indicator>";
				$scope.kpiList.push(obj);
			}
		},function(response){
			sbiModule_restServices.errorhandler(response.data,"");
		});
	};
	$scope.getListKPI();
	
	$scope.addKpiToTarget=function(){ 
		var tmpTargetKpis=[];
		if($scope.currentTarget.kpis==undefined){
			$scope.currentTarget.kpis = [];
		} 
		
		angular.copy($scope.currentTarget.kpis,tmpTargetKpis); 
		$mdDialog.show({
			controller: DialogControllerKPI,
			templateUrl: 'templatesaveKPI.html',
			clickOutsideToClose:false,
			preserveScope:true,
			locals: {
				kpiList: $scope.kpiList,
				tmpTargetKpis: tmpTargetKpis}
		})
		.then(function(data) {
		angular.copy(data,$scope.currentTarget.kpis);
		$timeout(function(){
				$scope.updateCriterionPriority();
		},0);
		});
		
	};

	var DialogControllerKPI= function($scope,kpiList,tmpTargetKpis){
		$scope.kpiAllList=kpiList;
		$scope.kpiSelected=tmpTargetKpis;
		
		$scope.saveKpiToTarget=function(){
			  $mdDialog.hide($scope.kpiSelected);
		}
		$scope.close=function(){
			$mdDialog.cancel();
		}
	}
		
	
	
	$scope.$on('saveTarget', function(event, args) {
		if($scope.currentTarget.name.trim()==""){
			$scope.showToast(sbiModule_translate.load("sbi.kbi.scorecard.alert.name.missing"));
			 return;
		}
		if($scope.currentTarget.kpis==undefined || $scope.currentTarget.kpis.length==0){
			$scope.showToast(sbiModule_translate.load("sbi.kbi.scorecard.alert.kpi.missing")); 
			return;
		}
		
		
		if ($scope.editProperty.target.index != undefined){
			$scope.currentTarget.groupedKpis = [];
			scorecardManager_targetUtility.loadGroupedKpis($scope.currentTarget)
			.then(
					function(response){
						$scope.currentTarget.status = response;
						var critPriIndex= $scope.itemNameInList($scope.currentPerspective.options.criterionPriority,$scope.currentPerspective.targets[$scope.editProperty.target.index])
						if(critPriIndex!=-1){
							angular.copy($scope.currentTarget,$scope.currentPerspective.options.criterionPriority[critPriIndex]);
						}
						
						angular.copy($scope.currentTarget,$scope.currentPerspective.targets[$scope.editProperty.target.index]);
					
						angular.copy($scope.emptyTarget,$scope.currentTarget);

						$scope.steps.stepControl.prevBread();
						},
					function(){});
			//update the perspective option criterionPriority if present
			
		}
		else{
			scorecardManager_targetUtility.loadGroupedKpis($scope.currentTarget)
			.then(
					function(response){
						$scope.currentTarget.status = response;
						$scope.currentPerspective.targets.push(angular.extend({},$scope.currentTarget));	
						angular.copy($scope.emptyTarget,$scope.currentTarget);

						$scope.steps.stepControl.prevBread();
					}
					,
					function(){});
		}
		
 	});
	
	$scope.$on('cancelTarget', function(event, args) {
		if(!angular.equals($scope.emptyTarget,$scope.currentTarget)){
	 		var confirm = $mdDialog.confirm()
	        .title(sbiModule_translate.load("sbi.layer.modify.progress"))
	        .content(sbiModule_translate.load("sbi.layer.modify.progress.message.modify"))
	        .ariaLabel('cancel targetr') 
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
	
}