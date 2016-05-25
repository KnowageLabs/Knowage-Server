(function() {

var scripts = document.getElementsByTagName("script");
var currentScriptPath = scripts[scripts.length - 1].src;
currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

var kpiScorecardApp = angular.module('kpiScorecardModule', ['ngMaterial','expander-box','sbiModule']);
kpiScorecardApp.directive("kpiScorecard",function(){
		return {
			//restrict: 'E',
			templateUrl: currentScriptPath + 'template/kpi-scorecard.jsp',
			controller: kpiScorecardController,
			scope: {
				scorecard:"=",
				expanderStatus:"=?",
				resetExpander:"="
			},
			 link: function (scope, element, attrs, ctrl, transclude) {
				 if(attrs.expanderStatus || scope.resetExpander!=true){
					 if(scope.expanderStatus==undefined){
						 scope.expanderStatus={};
					 }
				 }else{
					 scope.expanderStatus={};
				 }
             }
		};
	});

function kpiScorecardController($scope, sbiModule_translate,$mdDialog,sbiModule_dateServices){
	$scope.translate = sbiModule_translate;
	$scope.nameList = "";
	$scope.kpiArray = [];
	$scope.criterion = "";
	$scope.criterionOption = "";
	$scope.localsScope={ 
			listKPer:function(id,event){
				$scope.listKpiPerspective(id,event);
			},
			critPers:function(id,event){
				$scope.criterionSelectedPerspective(id,event);
				},
			listKGoal:function(idGoal,idPers,event){
				$scope.listKpiGoal(idGoal,idPers,event);
			},
			critGoal:function(idGoal,idPers,event){
				$scope.criterionSelectedGoal(idGoal,idPers,event);
				},
			};
	
	$scope.listKpiGoal = function(goalId, perspectiveId, event){
		event.stopPropagation();
		$scope.kpiArray = [];
		var pos = 0;
		var pos2 = 0;
		while (pos < $scope.scorecard.scorecard.perspectives.length && $scope.scorecard.scorecard.perspectives[pos].id != perspectiveId)
			pos++;
		while (pos2 < $scope.scorecard.scorecard.perspectives[pos].targets.length && $scope.scorecard.scorecard.perspectives[pos].targets[pos2].id != goalId)
			pos2++;
		
		$scope.scorecard.scorecard.perspectives[pos].targets[pos2].kpis.forEach(function(entry2){
			entry2.dateCreation = sbiModule_dateServices.formatDate(new Date(entry2.dateCreation));
			entry2.kpiSemaphore = "<kpi-semaphore-indicator indicator-color=\"'" + entry2.status + "'\"></kpi-semaphore-indicator>";
			$scope.kpiArray.push(entry2);
			});
		
		$scope.nameList = "Goal " + $scope.scorecard.scorecard.perspectives[pos].targets[pos2].name; 
			
		$mdDialog.show({
			controller: DialogControllerKPI,
			templateUrl: 'templateKPI.html',
			clickOutsideToClose:false,
			preserveScope:true,
			locals: {
				scoreMaster:$scope.scorecard,
				kpiArray:$scope.kpiArray,
				namingList:$scope.nameList,
				criterion:"",
				criterionOption:""
				}
		})
		.then(function(data) {
			
		$timeout(function(){
		},0);
		});
		
	};

	$scope.criterionSelectedPerspective = function (perspectiveId, event){
		event.stopPropagation();
		var pos = 0;
		while (pos < $scope.scorecard.scorecard.perspectives.length && $scope.scorecard.scorecard.perspectives[pos].id != perspectiveId)
			pos++;
		$scope.criterion = $scope.scorecard.scorecard.perspectives[pos].criterion;
		$scope.criterionOption =  $scope.scorecard.scorecard.perspectives[pos].options;
		$scope.nameList = "Perspective " + $scope.scorecard.scorecard.perspectives[pos].name;
		
		$mdDialog.show({
			controller: DialogControllerKPI,
			templateUrl: 'templateCriterion.html',
			clickOutsideToClose:false,
			preserveScope:true,
			locals: {
				scoreMaster:$scope.scorecard,
				kpiArray:$scope.kpiArray,
				namingList:$scope.nameList,
				criterion:$scope.criterion,
				criterionOption:$scope.criterionOption
				
				}
		})
		.then(function(data) {
			
		$timeout(function(){
		},0);
		});
	};
	
	
	$scope.criterionSelectedGoal = function (GoalId, perspectiveId, event){
		event.stopPropagation();
		var pos = 0;
		var pos2 = 0;
		while (pos < $scope.scorecard.scorecard.perspectives.length && $scope.scorecard.scorecard.perspectives[pos].id != perspectiveId)
			pos++;
		while (pos2 < $scope.scorecard.scorecard.perspectives[pos].targets.length && $scope.scorecard.scorecard.perspectives[pos].targets[pos2].id != GoalId)
			pos2++;
		$scope.criterion = $scope.scorecard.scorecard.perspectives[pos].targets[pos2].criterion;
		$scope.criterionOption =  $scope.scorecard.scorecard.perspectives[pos].targets[pos2].options;
		$scope.nameList = "Goal " + $scope.scorecard.scorecard.perspectives[pos].targets[pos2].name;
		
		$mdDialog.show({
			controller: DialogControllerKPI,
			templateUrl: 'templateCriterion.html',
			clickOutsideToClose:false,
			preserveScope:true,
			locals: {
				scoreMaster:$scope.scorecard,
				kpiArray:$scope.kpiArray,
				namingList:$scope.nameList,
				criterion:$scope.criterion,
				criterionOption:$scope.criterionOption
				
				}
		})
		.then(function(data) {
			
		$timeout(function(){
		},0);
		});
	};
	
	$scope.listKpiPerspective = function(perspectiveId, event){
		event.stopPropagation();
		$scope.kpiArray = [];
		var pos = 0;
		while (pos < $scope.scorecard.scorecard.perspectives.length && $scope.scorecard.scorecard.perspectives[pos].id != perspectiveId)
			pos++;
		
		$scope.nameList = "Perspective " + $scope.scorecard.scorecard.perspectives[pos].name; 
		
		$scope.scorecard.scorecard.perspectives[pos].targets.forEach(function(entry2){
			entry2.kpis.forEach(function(entry3){
				var pos = 0;
				$scope.kpiArray.forEach(function(entry4) {
					if ($scope.kpiArray.length > 0)
						{
							if (entry3.id != entry4.id)
								pos++;
						}
				});
				if ($scope.kpiArray.length == pos){
					$scope.kpiArray.push(entry3);
				}
				$scope.kpiArray.forEach(function(entry4) {
					entry4.dateCreation = sbiModule_dateServices.formatDate(new Date(entry4.dateCreation));
					entry4.kpiSemaphore = "<kpi-semaphore-indicator indicator-color=\"'" + entry4.status + "'\"></kpi-semaphore-indicator>";
				});
			});
		})
		
		$mdDialog.show({
			controller: DialogControllerKPI,
			templateUrl: 'templateKPI.html',
			clickOutsideToClose:false,
			preserveScope:true,
			locals: {
				scoreMaster:$scope.scorecard,
				kpiArray:$scope.kpiArray,
				namingList:$scope.nameList,
				criterion:"",
				criterionOption:""
				}
		})
		.then(function(data) {
			
		$timeout(function(){
		},0);
		});
	};

	//	do{}
//	while ($scope.scorecard.scorecard == undefined)
	$scope.scorecardTarget = $scope.scorecard;
	 
};

function DialogControllerKPI($scope,$mdDialog,scoreMaster,sbiModule_dateServices, kpiArray, namingList, criterion, criterionOption,sbiModule_translate){
		$scope.translate = sbiModule_translate;
		$scope.criterionTypeList = [];
		$scope.criterionTypeList2 = [];
		$scope.arrayToShow = kpiArray;
		$scope.nameList=namingList;
		$scope.criterion = criterion;
		if (criterionOption != ""){
			$scope.criterionOption = JSON.parse(criterionOption);
		if ($scope.criterionOption.criterionPriority.length > 0)
			$scope.criterionTypeList2 = {"id" : $scope.criterionOption.criterionPriority[0], "value" : $scope.criterionOption.criterionPriority[0]};
		}
		$scope.closeDialog=function(){
			$mdDialog.cancel();
		};
	}
})();