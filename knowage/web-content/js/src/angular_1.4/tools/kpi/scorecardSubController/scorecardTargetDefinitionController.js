angular.module('scorecardManager').controller('scorecardTargetDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices','sbiModule_config','$filter','$mdDialog','$mdToast','scorecardManager_targetUtility','scorecardManager_semaphoreUtility',scorecardTargetDefinitionControllerFunction ]);


angular.module('scorecardManager').service('scorecardManager_targetUtility',function(scorecardManager_semaphoreUtility){
	this.getTargetStatus=function(target){ 
		if(angular.equals(target.criterion.valueCd,"MAJORITY")){
				return loadTargetByMajority(target);
			}else{
				//load by priority
				if(target.options.criterionPriority.length==0){
					//if no priority kpi are selected return the global value 
					return loadTargetByMajority(target);
				}else if(target.options.criterionPriority.length==1){ 
					// if there is only one kpi selected, and theyr status in different of GRAY return his status, else the global status
					return (target.options.criterionPriority[0].status=="GRAY" || target.options.criterionPriority[0].status=="GREEN")  ?   loadTargetByMajority(target) : target.options.criterionPriority[0].status;
				}
				else{
					return loadTargetByMajorityWithPriority(target);
				}
			}
	};
	
	function loadTargetByMajorityWithPriority(target){
		 var masterPriorityStatus=target.options.criterionPriority[0].status
		 for(var i=1;i<target.options.criterionPriority.length;i++){
			 masterPriorityStatus=scorecardManager_semaphoreUtility.getPriorityStatus(target.options.criterionPriority[i].status,masterPriorityStatus);
		 }
		
		if(angular.equals("GREEN",masterPriorityStatus)){
			return loadTargetByMajority(target);
		}else{
			return masterPriorityStatus
		}
		 
	};
	
	function loadTargetByMajority(target){ 
		var maxTargetCount=target.groupedKpis[0].count;
		var maxTarget=target.groupedKpis[0].status;
		for(var i=1;i<target.groupedKpis.length;i++){
			if(!angular.equals("GRAY",target.groupedKpis[i].status)){
				if(target.groupedKpis[i].count>maxTargetCount || angular.equals("GRAY",maxTarget)){
					maxTargetCount=target.groupedKpis[i].count;
					maxTarget=target.groupedKpis[i].status;
				}else if(target.groupedKpis[i].count==maxTargetCount){
					maxTargetCount=target.groupedKpis[i].count;
					maxTarget=scorecardManager_semaphoreUtility.getPriorityStatus(target.groupedKpis[i].status,maxTarget);
				}
			}
		}  
		return maxTarget ;
	}
	
	this.addGroupedKpisItem=function(target,type){
		for(var i=0;i<target.groupedKpis.length;i++){
			if(angular.equals(target.groupedKpis[i].status,type)){
				target.groupedKpis[i].count++;
				return;
			}
		}
			target.groupedKpis.push({status:type,count:1});
	}
	 
	this.loadGroupedKpis=function(selTarget){
		if(!selTarget.hasOwnProperty("groupedKpis")){
			selTarget.groupedKpis = [];
		}
		
		for(var i=0;i<selTarget.kpis.length;i++){
			this.addGroupedKpisItem(selTarget,selTarget.kpis[i].status);
		}
		selTarget.status=this.getTargetStatus(selTarget);
		}
	
});


function scorecardTargetDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices,sbiModule_config,$filter,$mdDialog,$mdToast,scorecardManager_targetUtility,scorecardManager_semaphoreUtility){
	$scope.kpiList=[];
	$scope.targetListAction =  [
			           {
				              label : 'Remove',
				              icon:'fa fa-trash' , 
				              backgroundColor:'#trasparent',
				              action : function(item,event) {
				            	  pos = 0;
				            	  while ($scope.currentTarget.kpis[pos].name != item.name)
				            		  pos++;
				            	  $scope.currentTarget.kpis.splice(pos,1);
				              }
				           }
					];
	
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
			$scope.showToast('Name is required');
			 return;
		}
		if($scope.currentTarget.kpis==undefined || $scope.currentTarget.kpis.length==0){
			$scope.showToast('Select at least one kpi'); 
			return;
		}
		
		
		if ($scope.editProperty.target.index != undefined){
			$scope.currentTarget.groupedKpis = [];
			scorecardManager_targetUtility.loadGroupedKpis($scope.currentTarget);
			angular.copy($scope.currentTarget,$scope.currentPerspective.targets[$scope.editProperty.target.index]);
		}
		else{
			scorecardManager_targetUtility.loadGroupedKpis($scope.currentTarget);
			$scope.currentPerspective.targets.push(angular.extend({},$scope.currentTarget));	
		}
		angular.copy($scope.emptyTarget,$scope.currentTarget);

		$scope.stepControl.prevBread();
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
					$scope.stepControl.prevBread();
			  }, function() {
			   return;
			  });
 		}else{
 			$scope.stepControl.prevBread();
 		} 
 	});
	
}