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
app.controller('alertDefinitionListController', ['$scope','$angularListDetail','sbiModule_translate','sbiModule_restServices',alertDefinitionListControllerFunction ]);

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
}
	
	
function alertDefinitionListControllerFunction($scope,$angularListDetail,sbiModule_translate,sbiModule_restServices){
	$scope.alertColumnsList=[{label:sbiModule_translate.load("name"),name:"name"}];
	
	$scope.alertClickEditFunction=function(item,index){
		sbiModule_restServices.promiseGet("1.0/alert",item.id+'/load')
		.then(function(response){  
			
			response.data.jsonOptions=JSON.parse(response.data.jsonOptions); 
			for(var i=0;i<response.data.jsonOptions.actions.length;i++){
				response.data.jsonOptions.actions[i].jsonActionParameters=JSON.parse(response.data.jsonOptions.actions[i].jsonActionParameters);
			} 
			
			response.data.frequency.cron=JSON.parse(response.data.frequency.cron);
			
			console.log("response.dat",response.data)
			angular.copy(response.data,$scope.alert)
			$angularListDetail.goToDetail();
		},function(response){
			sbiModule_restServices.errorHandler(response.data,"Errore durante il download delle alert**");
		});
		

		
	};
	
	$scope.newAlertFunction=function(){
		angular.copy($scope.emptyAlert,$scope.alert);
		$angularListDetail.goToDetail();
	}
	
	$scope.newAlertFunction=function(){
		angular.copy($scope.emptyAlert,$scope.alert)
		$angularListDetail.goToDetail();
	}
	
	sbiModule_restServices.promiseGet("1.0/alert", 'listAlert')
	.then(function(response){  
		angular.copy(response.data,$scope.listAlert);
	},function(response){
		sbiModule_restServices.errorHandler(response.data,"Errore durante il download delle alert**");
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
		$cronFrequency.parseForBackend(itemToSave);
		
		for(var i=0;i<itemToSave.jsonOptions.actions.length;i++){
			itemToSave.jsonOptions.actions[i].jsonActionParameters=JSON.stringify(itemToSave.jsonOptions.actions[i].jsonActionParameters);
		}
		
		itemToSave.jsonOptions=JSON.stringify(itemToSave.jsonOptions);
		sbiModule_restServices.promisePost("1.0/alert","save",itemToSave)
		.then(function(response){
			if($scope.alert.id==undefined){
				$scope.alert.id=response.data.id;
			}
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("alert salvato con successo**")).position('top').action($scope.translate.load("sbi.general.yes")).highlightAction(false).hideDelay(2000))
			
		}
		,function(response){
			sbiModule_restServices.errorHandler(response.data,"Error while attempt to save alert**");
			})
	}
	$scope.cancelAlertFunction=function(){
		console.log("cancellao")
		angular.copy($scope.emptyAlert,$scope.alert);
		$angularListDetail.goToList();
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


