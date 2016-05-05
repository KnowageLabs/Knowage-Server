var app = angular.module('alertDefinitionManager', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'angular-list-detail','angular_list',"expander-box",'ngWYSIWYG','cron_frequency']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
	
	
}]);


app.factory("alertDefinition_actions",function(){
	return [];
});
app.factory("alertDefinition_listeners",function(){
	return [];
});

app.controller('alertDefinitionController', ['$scope', alertDefinitionControllerFunction ]);
app.controller('alertDefinitionDetailController', ['$scope','$angularListDetail','sbiModule_translate', 'sbiModule_restServices','$mdDialog','$q','$mdToast','$timeout','sbiModule_config','alertDefinition_actions','alertDefinition_listeners','$cronFrequency','$mdToast',alertDefinitionDetailControllerFunction ]);
app.controller('alertDefinitionListController', ['$scope','$angularListDetail','sbiModule_translate','sbiModule_restServices','$mdToast','$mdDialog','$timeout',alertDefinitionListControllerFunction ]);

function alertDefinitionControllerFunction($scope){
	$scope.listAlert=[];
	$scope.emptyAlert = {
			alertListener: {},
			jsonOptions:{},
			frequency:{}
			};
	$scope.alert = {
			alertListener: {},
			jsonOptions:{},
			frequency:{}
	};
	$scope.temporaneyAlert={};
		
	$scope.loadBroadcastLoadListAlert=function(){
		$scope.$broadcast("loadListAlert");
	}
}
	
	
function alertDefinitionListControllerFunction($scope,$angularListDetail,sbiModule_translate,sbiModule_restServices,$mdToast,$mdDialog,$timeout){
	$scope.alertColumnsList=[{label:sbiModule_translate.load("sbi.generic.name"),name:"name"},{label:sbiModule_translate.load("sbi.generic.state"),name:"jobStatus"}];
	
	$scope.alertListAction=[
	                        {
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,
		backgroundColor:'transparent',
		action : function(item,event) {
			
			 var confirm = $mdDialog.confirm()
	         .title($scope.translate.load("sbi.kpi.measure.delete.title"))
	         .content($scope.translate.load("sbi.kpi.measure.delete.content"))
	         .ariaLabel('delete measure') 
	         .ok($scope.translate.load("sbi.general.yes"))
	         .cancel($scope.translate.load("sbi.general.No"));
			   $mdDialog.show(confirm).then(function() { 		 
			sbiModule_restServices.promiseDelete("1.0/alert",item.id+'/delete')
			.then(function(response){  
				$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.catalogues.toast.deleted")).position('top').action(
				'OK').highlightAction(false).hideDelay(2000))
				 $scope.listAlert.splice($scope.listAlert.indexOf(item),1);
			},function(response){sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.deletingItemError"))});
			   });
			
			
			}
	
		},
		{ 
			label: function(row){
				return angular.equals(row.jobStatus.toUpperCase(),"SUSPENDED") ? sbiModule_translate.load('sbi.alert.resume') : sbiModule_translate.load('sbi.alert.suspend');
			},
			icon: function(row){
				return angular.equals(row.jobStatus.toUpperCase(),"SUSPENDED") ? 'fa fa-play' : 'fa fa-pause';
			}, 
			backgroundColor:'transparent',
			action : function(item,event) { 
				var data="?jobGroup=KPI_SCHEDULER_GROUP&triggerGroup=KPI_SCHEDULER_GROUP&jobName="+item.id+"&triggerName="+item.id;
				
				sbiModule_restServices.promisePost("scheduler",(angular.equals(item.jobStatus.toUpperCase(),"SUSPENDED") ? 'resumeTrigger' : 'pauseTrigger')+""+data)
				.then(function(response){  
//					$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.catalogues.toast.deleted")).position('top').action(
//					'OK').highlightAction(false).hideDelay(2000)) ;
					item.jobStatus=angular.equals(item.jobStatus.toUpperCase(),"SUSPENDED") ? 'ACTIVE' : 'SUSPENDED';
				},function(response){
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.deletingItemError"))});
				}
		
			}];
	
	
	$scope.alertClickEditFunction=function(item,index){
		sbiModule_restServices.promiseGet("1.0/alert",item.id+'/load')
		.then(function(response){  
			
			response.data.jsonOptions=JSON.parse(response.data.jsonOptions); 
			for(var i=0;i<response.data.jsonOptions.actions.length;i++){
				response.data.jsonOptions.actions[i].jsonActionParameters=JSON.parse(response.data.jsonOptions.actions[i].jsonActionParameters);
			} 
			
			response.data.frequency.cron=JSON.parse(response.data.frequency.cron);
			 
			angular.copy(response.data,$scope.alert);
			$timeout(function(){
				angular.copy($scope.alert,$scope.temporaneyAlert);
			},500)
			$angularListDetail.goToDetail();
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.alert.load.error"));
		});
		

		
	};
	
	$scope.newAlertFunction=function(){
		angular.copy($scope.emptyAlert,$scope.alert);
		$timeout(function(){
			angular.copy($scope.alert,$scope.temporaneyAlert);
		},500)
		$angularListDetail.goToDetail();
	}
	
	
	
	$scope.loadListAlert=function(){
		sbiModule_restServices.promiseGet("1.0/alert", 'listAlert')
		.then(function(response){  
			angular.copy(response.data,$scope.listAlert);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.alert.load.error"));
		});
	};
	$scope.loadListAlert();
	
	$scope.$on('loadListAlert', function() {
 		$scope.loadListAlert();
 	});
	
}

function alertDefinitionDetailControllerFunction($scope,$angularListDetail,sbiModule_translate,sbiModule_restServices,$mdDialog,$q,$mdToast,$timeout,sbiModule_config,alertDefinition_actions,alertDefinition_listeners,$cronFrequency,$mdToast){
	$scope.translate=sbiModule_translate;  
	$scope.isValidListener={status:false};
	$scope.isValidListenerCrono={status:false};
	$scope.listeners=alertDefinition_listeners; 

	
	$scope.saveAlertFunction=function(){ 
		var itemToSave={};
		angular.copy($scope.alert,itemToSave); 
		$cronFrequency.parseForBackend(itemToSave.frequency);
		
		for(var i=0;i<itemToSave.jsonOptions.actions.length;i++){
			itemToSave.jsonOptions.actions[i].jsonActionParameters=JSON.stringify(itemToSave.jsonOptions.actions[i].jsonActionParameters);
		}
		
		itemToSave.jsonOptions=JSON.stringify(itemToSave.jsonOptions);
		sbiModule_restServices.promisePost("1.0/alert","save",itemToSave)
		.then(function(response){
			if($scope.alert.id==undefined){
				$scope.alert.id=response.data.id;
			}
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.alert.save.success")).position('top').action($scope.translate.load("sbi.general.yes")).highlightAction(false).hideDelay(2000))
			$scope.loadBroadcastLoadListAlert();
			angular.copy($scope.alert,$scope.temporaneyAlert);
		}
		,function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.alert.save.error"));
			})
	}
	$scope.cancelAlertFunction=function(){

 		if(!angular.equals($scope.temporaneyAlert,$scope.alert)){
	 		var confirm = $mdDialog.confirm()
	        .title(sbiModule_translate.load("sbi.layer.modify.progress"))
	        .content(sbiModule_translate.load("sbi.layer.modify.progress.message.modify"))
	        .ariaLabel('cancel metadata') 
			.ok(sbiModule_translate.load("sbi.general.yes"))
			.cancel(sbiModule_translate.load("sbi.general.No"));
			  $mdDialog.show(confirm).then(function() {
				  angular.copy($scope.emptyAlert,$scope.alert); 
					$angularListDetail.goToList();
			  }, function() {
			   return;
			  });
 		}else{
 			angular.copy($scope.emptyAlert,$scope.alert);
 			$angularListDetail.goToList();
 		} 
 	

		
		
		
		
		
	}
	$scope.listenerIsSelected=function(){
		return !angular.equals({},$scope.alert.alertListener);
	}
	
	sbiModule_restServices.promiseGet("1.0/alert", 'listListener')
	.then(function(response){  
		angular.copy(response.data,alertDefinition_listeners);
	},function(response){
		sbiModule_restServices.errorHandler(response.data,"");
	});
	
	sbiModule_restServices.promiseGet("1.0/alert", 'listAction')
	.then(function(response){  
		angular.copy(response.data,alertDefinition_actions);
	},function(response){
		sbiModule_restServices.errorHandler(response.data,"");
	});

}


//test directive


app.directive('actionMaker', function($compile,$timeout) {
	  return {
	    template:"<span layout flex ng-include=\"contextName+'/'+templateUrl\"></span>",
	    controller: actionMakerFunction,
//	    replace: true,
	    scope: {
	    	ngModel:'=',
	    	templateUrl:"=",
	    	isValid:"="
	    	},
	      link: function (scope, elm, attrs) {  
	    	  elm.addClass("layout");
	    	  var firstCheck=true;
	    	  scope.$watch(function(){return scope.templateUrl}, function (newVal,oldVal) {
            	  if(newVal!=oldVal || firstCheck){ 
            		  if(scope.isValid!=undefined){
            			  scope.isValid.status=true;
            		  }
            		  
            		  if(!firstCheck){
            			  angular.copy({},scope.ngModel);           			  
            		  }
            		  firstCheck=false;
            	  }
              },true);
	    	   
	      }
	  }
	  	});


	function actionMakerFunction($scope,sbiModule_translate,$timeout,sbiModule_config){ 
		$scope.translate=sbiModule_translate;
		$scope.contextName=sbiModule_config.contextName;
	}
	
	app.directive('actionMakerValidator', function($compile,$timeout) {
		  return {
			  link: function (scope, ele, attrs) {
				  var firstCheck=true;
                  scope.$watch(attrs.actionMakerValidator, function (newVal,oldVal) {
                	  if(newVal!=oldVal || firstCheck){ 
                		  firstCheck=false; 
                		  if(scope.isValid!=undefined){
	                		  if(newVal.validator!=undefined){
	                			  scope.isValid.status=newVal.validator();
	                		  }else{
	                			  scope.isValid.status=true;
	                		  }
                		  }
                	  }
                  },true);
                  
                  scope.$on(
                          "$destroy",
                          function handleDestroyEvent() { 
                        	  //when change the template, the ActionMakerDirective will be destroy and then,
                        	 // if the new template has the  actionMakerValidator,
                        	  //it calculate the validation 
                        	  if(scope.isValid!=undefined){
                        		  scope.isValid.status=true;
                        	  }
                          }
                      );
                  
              }
          };
		    	    
		  	});


