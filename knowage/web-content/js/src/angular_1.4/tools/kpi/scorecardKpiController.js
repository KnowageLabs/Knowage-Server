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
scorecardApp.controller('scorecardListController', [ '$scope','sbiModule_translate','sbiModule_restServices','$angularListDetail','$timeout','$mdDialog','scorecardManager_targetUtility','scorecardManager_perspectiveUtility',scorecardListControllerFunction ]);
scorecardApp.controller('scorecardDetailController', [ '$scope','sbiModule_translate','sbiModule_restServices','$angularListDetail','$timeout','$mdDialog',scorecardDetailControllerFunction ]);

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

function scorecardListControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$angularListDetail,$timeout,$mdDialog,scorecardManager_targetUtility,scorecardManager_perspectiveUtility){
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
			angular.copy($scope.parseScorecardForFrontend(response.data),$scope.currentScorecard); 
			$angularListDetail.goToDetail();
			},function(response){
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.scorecard.load.error"));
		});
	}
	
	$scope.parseScorecardForFrontend=function(tmpScorecard){
		for(var persp=0;persp<tmpScorecard.perspectives.length;persp++){
			var tmpPersp=tmpScorecard.perspectives[persp];
			//convert the target to frontend object
			for(var targ=0;targ<tmpPersp.targets.length;targ++){
				var tmpTarg=tmpPersp.targets[targ];
				var tmpOption=JSON.parse(tmpTarg.options);
				var tmpCritPri=[];
				if(tmpOption.hasOwnProperty("criterionPriority")){
					for(var critP=0;critP<tmpOption.criterionPriority.length;critP++){
						for(var kp=0;kp<tmpTarg.kpis.length;kp++){
							if(angular.equals(tmpTarg.kpis[kp].name,tmpOption.criterionPriority[critP])){
								tmpCritPri.push(tmpTarg.kpis[kp])
								break;
							}
						}
					}
					tmpOption.criterionPriority=tmpCritPri;
				}
				tmpTarg.options=tmpOption;
				scorecardManager_targetUtility.loadGroupedKpis(tmpTarg);
			}
			
			//convert the target to frontend object
			var tmpPerspOption=JSON.parse(tmpPersp.options);
			var tmpPerspCritPri=[];
			if(tmpPerspOption.hasOwnProperty("criterionPriority")){		
				for(var critP=0;critP<tmpPerspOption.criterionPriority.length;critP++){
					for(var targ=0;targ<tmpPersp.targets.length;targ++){
						if(angular.equals(tmpPersp.targets[targ].name,tmpPerspOption.criterionPriority[critP])){
							tmpPerspCritPri.push(tmpPersp.targets[targ])
							break;
						}
					}
				}
					tmpPerspOption.criterionPriority=tmpPerspCritPri;
			}
			tmpPersp.options=tmpPerspOption;
			scorecardManager_perspectiveUtility.loadGroupedTarget(tmpPersp);

		}
		return tmpScorecard;
	};
	
	
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
								 		var confirm = $mdDialog.confirm()
								        .title($scope.translate.load("sbi.kpi.measure.delete.title"))
								         .content($scope.translate.load("sbi.kpi.measure.delete.content"))
								         .ariaLabel('delete scorecard') 
										.ok(sbiModule_translate.load("sbi.general.yes"))
										.cancel(sbiModule_translate.load("sbi.general.No"));
										  $mdDialog.show(confirm).then(function() {
												sbiModule_restServices.promiseDelete("1.0/kpi",item.id + "/deleteScorecard")
												.then(function(response) {
													  $scope.scorecardList.splice( $scope.scorecardList.indexOf( item ),1);
												}, function(response) {
													sbiModule_restServices.errorHandler(response.data.errors[0].message, 'Error');
													});	
										  });  
	         			           }}];
}

function scorecardDetailControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$angularListDetail,$timeout,$mdDialog){
	$scope.stepItem=[{name:'scorecard definition'}];
	
	$scope.stepControl; 
	$scope.criterionTypeList = [];
	
	$scope.cancelScorecardFunction=function(){
		 if(true){
	 		var confirm = $mdDialog.confirm()
	        .title(sbiModule_translate.load("sbi.layer.modify.progress"))
	        .content(sbiModule_translate.load("sbi.layer.modify.progress.message.modify"))
	        .ariaLabel('cancel perspective') 
			.ok(sbiModule_translate.load("sbi.general.yes"))
			.cancel(sbiModule_translate.load("sbi.general.No"));
			  $mdDialog.show(confirm).then(function() {
				  $angularListDetail.goToList();
				  $scope.stepControl.resetBreadCrumb();
				  angular.copy({},$scope.currentScorecard);
				  
			  }, function() {
			   return;
			  });
 		}else{
 			$angularListDetail.goToList();
 		} 
 	
		
		
		
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
		var tmpPreSaveScorecard=$scope.parseScorecardForBackend($scope.currentScorecard);
		sbiModule_restServices.promisePost("1.0/kpi","saveScorecard",tmpPreSaveScorecard)
			.then(function(response) {
				$scope.showToast(sbiModule_translate.load("sbi.glossary.success.save")); 
			}, function(response) {
				sbiModule_restServices.errorHandler(response.data.errors[0].message, 'Error');
				});	
	}
	
	
	$scope.parseScorecardForBackend=function(scorecard){
		 var tmpScorecard={};
		 angular.copy(scorecard,tmpScorecard);
		for(var i=0;i<tmpScorecard.perspectives.length;i++){
			if(!angular.isString(tmpScorecard.perspectives[i].options)){
				//convert if is json
				var tmpTargetOptions=[];
				for(var targetIndex=0;targetIndex<tmpScorecard.perspectives[i].options.criterionPriority.length;targetIndex++){
					tmpTargetOptions.push(tmpScorecard.perspectives[i].options.criterionPriority[targetIndex].name);
				}
				tmpScorecard.perspectives[i].options.criterionPriority=tmpTargetOptions;
				tmpScorecard.perspectives[i].options=JSON.stringify(tmpScorecard.perspectives[i].options);
			}
			
			for(var j=0;j<tmpScorecard.perspectives[i].targets.length;j++){
				if(!angular.isString(tmpScorecard.perspectives[i].targets[j].options)){
					var tmpKpiOptions=[];
					for(var kpiIndex=0;kpiIndex<tmpScorecard.perspectives[i].targets[j].options.criterionPriority.length;kpiIndex++){
						tmpKpiOptions.push(tmpScorecard.perspectives[i].targets[j].options.criterionPriority[kpiIndex].name);
					}
					tmpScorecard.perspectives[i].targets[j].options.criterionPriority=tmpKpiOptions;
					tmpScorecard.perspectives[i].targets[j].options=JSON.stringify(tmpScorecard.perspectives[i].targets[j].options);
				 }
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