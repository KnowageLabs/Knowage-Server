var scorecardApp = angular.module('scorecardManager', [ 'ngMaterial',  'angular_table' ,'angular_list','sbiModule', 'angular-list-detail','bread_crumb','kpi_semaphore_indicator']);
scorecardApp.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

scorecardApp.service('scorecardManager_semaphoreUtility',function(){
	this.typeColor=['RED','YELLOW','GREEN','GRAY'];
	this.getPriorityStatus=function(a,b){
		return this.typeColor.indexOf(a)<this.typeColor.indexOf(b) ? a : b; 
	}
});


scorecardApp.controller('scorecardMasterController', [ '$scope','sbiModule_translate','sbiModule_restServices','$angularListDetail','$timeout','$mdToast',scorecardMasterControllerFunction ]);
scorecardApp.controller('scorecardListController', [ '$scope','sbiModule_translate','sbiModule_restServices','$angularListDetail','$timeout',scorecardListControllerFunction ]);
scorecardApp.controller('scorecardDetailController', [ '$scope','sbiModule_translate','sbiModule_restServices','$angularListDetail','$timeout',scorecardDetailControllerFunction ]);

function scorecardMasterControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$angularListDetail,$timeout,$mdToast){
	$scope.translate=sbiModule_translate;
	$scope.emptyScorecard={name:"",perspectives:[]};
	$scope.emptyPerspective={name:"",criterion:{},status:"",groupedKpis:[],targets:[],options:{criterionPriority:[]}};
	$scope.emptyTarget={name:"",criterion:{},status:"",groupedKpis:[],kpis:[],options:{criterionPriority:[]}};
	$scope.currentScorecard= {};
	$scope.currentPerspective = {};
	$scope.currentTarget = {};
	$scope.selectedStep={value:0};
	$scope.editProperty = {target:{}, perspective:{}};
	
	$scope.broadcastCall=function(type){
		$scope.$broadcast(type);
	}
	
	$scope.showToast=function(text,times){
		var mills=times | 3000;
		$mdToast.show(
			      $mdToast.simple()
			        .content(text)
			        .position("TOP")
			        .hideDelay(mills)
			    );
	}
	
	
}

function scorecardListControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$angularListDetail,$timeout){
	$scope.scorecardList=[];
	$scope.scorecardColumnsList=[
	                             {label:"Name",name:"name"},
	                             {label:"Data",name:"date"},
	                             {label:"Author",name:"author"}];
	
	$scope.newScorecardFunction=function(){
		angular.copy($scope.emptyScorecard,$scope.currentScorecard);  
		$angularListDetail.goToDetail();
	};
	
	$scope.scorecardClickEditFunction=function(item){
		sbiModule_restServices.promiseGet("1.0/kpi",item.id+"/loadScorecard")
		.then(function(response){
			angular.copy(response.data,$scope.currentScorecard); 
			$angularListDetail.goToDetail();
			},function(response){
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.scorecard.load.error"));
		});
	
		
	}
	
	$scope.loadScorecardList=function(){
		sbiModule_restServices.promiseGet("1.0/kpi","listScorecard")
		.then(function(response){
			$scope.scorecardList=response.data;
			},function(response){
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.scorecard.load.error"));
		});
	};
	
	$scope.loadScorecardList();
	
	$scope.scorecardListAction =  [{  label : 'Remove',
							        icon:'fa fa-trash' , 
							        backgroundColor:'trasparent',
							        action : function(item,event) {
							      	  pos = 0;
							      	  pos = $scope.scorecardList.indexOf( item );
							      	  
							      	sbiModule_restServices.promiseDelete("1.0/kpi",$scope.scorecardList[pos].id + "/deleteScorecard")
									.then(function(response) {
										  $scope.scorecardList.splice(pos,1);
									}, function(response) {
										sbiModule_restServices.errorHandler(response.data.errors[0].message, 'Error');
										});	
							      	
	         			           }}];
}

function scorecardDetailControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$angularListDetail,$timeout){
	$scope.stepItem=[{name:'scorecard definition'}];
	
	$scope.stepControl; 
	$scope.criterionTypeList = [];
	
	$scope.cancelScorecardFunction=function(){
		$angularListDetail.goToList();
	}
	
	$scope.saveScorecardFunction=function(){
		
		
		if($scope.currentScorecard.name.trim()==""){
			$scope.showToast('Name is required'); 
			 return;
		}
		if($scope.currentScorecard.perspectives==undefined || $scope.currentScorecard.perspectives.length==0){
			$scope.showToast('Add at least one perspective'); 
			 return;
		}
		
		
	sbiModule_restServices.promisePost("1.0/kpi","saveScorecard",$scope.parseScorecard($scope.currentScorecard))
			.then(function(response) {
				alert("Salvato");
			}, function(response) {
				sbiModule_restServices.errorHandler(response.data.errors[0].message, 'Error');
				});	
	}
	
	
	$scope.parseScorecard=function(scorecard){
		 var tmpScorecard={};
		 angular.copy(scorecard,tmpScorecard);
		for(var i=0;i<tmpScorecard.perspectives.length;i++){
			var tmpTargetOptions=[];
			for(var targetIndex=0;targetIndex<tmpScorecard.perspectives[i].options.criterionPriority.length;targetIndex++){
				tmpTargetOptions.push(tmpScorecard.perspectives[i].options.criterionPriority[targetIndex].name);
			}
			tmpScorecard.perspectives[i].options.criterionPriority=tmpTargetOptions;
			tmpScorecard.perspectives[i].options=JSON.stringify(tmpScorecard.perspectives[i].options);
			for(var j=0;j<tmpScorecard.perspectives[i].targets.length;j++){
				var tmpKpiOptions=[];
				for(var kpiIndex=0;kpiIndex<tmpScorecard.perspectives[i].targets[j].options.criterionPriority.length;kpiIndex++){
					tmpKpiOptions.push(tmpScorecard.perspectives[i].targets[j].options.criterionPriority[kpiIndex].name);
				}
				tmpScorecard.perspectives[i].targets[j].options.criterionPriority=tmpKpiOptions;
				tmpScorecard.perspectives[i].targets[j].options=JSON.stringify(tmpScorecard.perspectives[i].targets[j].options);
				 	}
		}
		return tmpScorecard;
	}
	
	sbiModule_restServices.promiseGet("2.0/domains","listByCode/KPI_SCORECARD_CRITE")
	.then(function(response){ 
		angular.copy(response.data,$scope.criterionTypeList); 
		$scope.emptyPerspective.criterion=$scope.criterionTypeList[0];
		$scope.emptyTarget.criterion=$scope.criterionTypeList[0];
	},function(response){
		sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.generic.error")+" domains->KPI_SCORECARD_CRITE"); 
	});
	
	
}