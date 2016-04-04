angular.module('scorecardManager').controller('scorecardTargetDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices','sbiModule_config','$filter','$mdDialog','$mdToast','scorecardManager_targetUtility','scorecardManager_semaphoreUtility',scorecardTargetDefinitionControllerFunction ]);


angular.module('scorecardManager').service('scorecardManager_targetUtility',function(scorecardManager_semaphoreUtility){
	this.getTargetStatus=function(target){ 
	if(angular.equals(target.criterion.valueCd,"MAJORITY")){
			return loadTargetByMajority(target);
		}else{
			//load by priority
			if(target.criterionPriority.length==0){
				//if no priority kpi are selected return the global value 
				return loadTargetByMajority(target);
			}else if(target.criterionPriority.length==1){ 
				// if there is only one kpi selected, and theyr status in different of GRAY return his status, else the global status
				return (target.criterionPriority[0].status=="GRAY" || target.criterionPriority[0].status=="GREEN")  ?   loadTargetByMajority(target) : target.criterionPriority[0].status;
			}
			else{
				return loadTargetByMajorityWithPriority(target);
			}
		}
		
	};
	
	function loadTargetByMajorityWithPriority(target){
		 var masterPriorityStatus=target.criterionPriority[0].status
		 for(var i=1;i<target.criterionPriority.length;i++){
			 masterPriorityStatus=scorecardManager_semaphoreUtility.getPriorityStatus(target.criterionPriority[i].status,masterPriorityStatus);
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
		sbiModule_restServices.promiseGet("1.0/kpi","listKpi")
		.then(function(response){ 
			for(var i=0;i<response.data.length;i++){
				var obj = angular.extend({},response.data[i]);
				var dateFormat = $scope.parseDate(sbiModule_config.localizedDateFormat);
				//parse date based on language selected
				obj["datacreation"]=$filter('date')(response.data[i].dateCreation, dateFormat);
				obj.status=scorecardManager_semaphoreUtility.typeColor[Math.floor(Math.random() * 4)];
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
		console.log(data)
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
		
	$scope.addGroupedKpisItem=function(type){
		for(var i=0;i<$scope.currentTarget.groupedKpis.length;i++){
			if(angular.equals($scope.currentTarget.groupedKpis[i].status,type)){
				$scope.currentTarget.groupedKpis[i].count++;
				return;
			}
		}
		 $scope.currentTarget.groupedKpis.push({status:type,count:1});
	}
	 
	
	$scope.loadGroupedKpis=function(){
			for(var i=0;i<$scope.currentTarget.kpis.length;i++){
				$scope.addGroupedKpisItem($scope.currentTarget.kpis[i].status);
			}
			$scope.currentTarget.status=scorecardManager_targetUtility.getTargetStatus($scope.currentTarget);
		}
	
	$scope.$on('saveTarget', function(event, args) {
		if($scope.currentTarget.name.trim()==""){
			 $mdToast.show(
				      $mdToast.simple()
				        .content('Name is required')
				        .position("TOP")
				        .hideDelay(3000)
				    );
			 return;
		}
		if($scope.currentTarget.kpis==undefined || $scope.currentTarget.kpis.length==0){
			 $mdToast.show(
				      $mdToast.simple()
				        .content('Select at least one kpi ')
				        .position("TOP")
				        .hideDelay(3000)
				    );
			 return;
		}
		
		$scope.loadGroupedKpis();
		
		$scope.currentPerspective.targets.push(angular.extend({},$scope.currentTarget));
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