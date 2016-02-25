var measureRoleApp = angular.module('measureRoleManager', [ 'ngMaterial',  'angular_table' ,'sbiModule', 'angular-list-detail','ui.codemirror']);
measureRoleApp.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

measureRoleApp.controller('measureRoleMasterController', [ '$scope','sbiModule_translate','$mdDialog','sbiModule_config','sbiModule_restServices' ,measureRoleMasterControllerFunction ]);
measureRoleApp.controller('measureListController', [ '$scope','sbiModule_translate','$mdDialog' ,measureListControllerFunction ]);
measureRoleApp.controller('measureDetailController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices','sbiModule_messaging',measureDetailControllerFunction ]); 

function measureRoleMasterControllerFunction($scope,sbiModule_translate,$mdDialog,sbiModule_config,sbiModule_restServices){
	$scope.translate=sbiModule_translate;
	$scope.aliasList=[];
	$scope.currentRole={
			dataSourceId:{},
			definition:"SELECT * FROM employee as cur",
			metadata:{}
				};
	
 	$scope.newMeasureFunction=function(){
	} 
 	
 	$scope.saveMeasureFunction=function(){
 		 $mdDialog.show({
 		      controller: DialogSaveController,
 		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/kpi/measureRoleSubController/saveDialogTemplate.jsp',  
 		      clickOutsideToClose:true,
 		      fullscreen: true,
 		      locals:{
 		    	 currentRole:$scope.currentRole,
 		    	  alias:$scope.aliasList}
 		    })
 		    .then(function(answer) {
 		      $scope.status = 'You said the information was "' + answer + '".';
 		    }, function() {
 		      $scope.status = 'You cancelled the dialog.';
 		    });
 	} 
 	
 	$scope.cancelMeasureFunctionMeasureFunction=function(){
 		alert("cancelMeasureFunction")
 	} 
 	
 	$scope.loadAliasList=function(){
 		sbiModule_restServices.promiseGet("1.0/kpi","listAlias")
		.then(function(response){ 
			var aliasTmpList={}; 
			for(var i=0;i<response.data.length;i++){
				aliasTmpList[response.data[i].name]=response.data[i]
			} 
			angular.copy(aliasTmpList,$scope.aliasList);
			
		},function(response){
			console.log("errore")
		});
 	};
 	$scope.loadAliasList();
	
}

function DialogSaveController($scope, $mdDialog,currentRole,alias) {
	 
	$scope.presentAlias=[];
	$scope.newAlias=[];
	$scope.currentRole=currentRole;
	for(var key in currentRole.metadata){
		if(alias.hasOwnProperty(currentRole.metadata[key].label)){
			$scope.presentAlias.push(currentRole.metadata[key].label);
		}else{
			$scope.newAlias.push(currentRole.metadata[key].label);
		}
	}

	
	  $scope.hide = function() {
		    $mdDialog.hide();
		  };
		  $scope.cancel = function() {
		    $mdDialog.cancel();
		  };
		  $scope.save = function() {
			  var tmpRoleObj={};
			  angular.copy(currentRole,tmpRoleObj);
			 var ruleOutputs=[]; 
			 for(var key in tmpRoleObj.metadata){
				 tmpRoleObj.metadata[key].alias=tmpRoleObj.metadata[key].label;
				 delete tmpRoleObj.metadata[key].label;
				 delete tmpRoleObj.metadata[key].name;
				 ruleOutputs.push(tmpRoleObj.metadata[key]);
			 };
			 tmpRoleObj.ruleOutputs=ruleOutputs;
			 delete tmpRoleObj.metadata;
			 console.log(tmpRoleObj) 
			  
//		    $mdDialog.hide();
		  };
		}
 
function measureDetailControllerFunction($scope,sbiModule_translate,sbiModule_restServices,sbiModule_messaging){
	$scope.detailProperty={
			dataSourcesIsSelected:false,
			queryChanged:true,
			previewData:{rows:[],metaData:{fields:[]}},
			};
	
	$scope.columnToMetadata=function(columns){
		var tmpMeas=[];
		for(var index in  columns){
			tmpMeas.push( columns[index].label);
			if(!$scope.currentRole.metadata.hasOwnProperty( columns[index].label)){
				$scope.currentRole.metadata[columns[index].label]= columns[index]
			}
		} 
		for(var index in $scope.currentRole.metadata){
			if(tmpMeas.indexOf($scope.currentRole.metadata[index].label)==-1){
				delete $scope.currentRole.metadata[index];
			}
		} 
	}
	 
	$scope.loadMetadata=function(){
		var postData={dataSourceId:$scope.currentRole.dataSourceId,
				query:$scope.currentRole.definition,
				maxItem:1
				} 
		sbiModule_restServices.promisePost("1.0/kpi","queryPreview",postData)
		.then(function(response){
			
			$scope.columnToMetadata(response.data.columns);
			 
		},function(response){
			console.log("errore")
		});
	}
	
	$scope.loadPreview=function(){
		var postData={dataSourceId:$scope.currentRole.dataSourceId,
				query:$scope.currentRole.definition,
				maxItem:10
				}
		
		sbiModule_restServices.promisePost("1.0/kpi","queryPreview",postData)
		.then(function(response){
			 $scope.detailProperty.queryChanged=false;
			$scope.columnToMetadata(response.data.columns);
			angular.copy(response.data,$scope.detailProperty.previewData);
		},function(response){
			sbiModule_messaging.showErrorMessage("errore ","jaskdajsl")
		});
	}
	
	
}

function measureListControllerFunction($scope,sbiModule_translate,$mdDialog){
	$scope.translate=sbiModule_translate;
	$scope.measureList=[
	                    {"measureName":"measure1","rulesName":"regola1","dateCreation":"20/1/2016","category":"Cat1","author":"pino"},
	                    {"measureName":"measure2","rulesName":"regola1","dateCreation":"23/1/2016","category":"Cat1","author":"gino"},
	                    {"measureName":"measure3","rulesName":"regola2","dateCreation":"21/1/2016","category":"Cat2","author":"lino"},
	                    {"measureName":"measure4","rulesName":"regola3","dateCreation":"22/1/2016","category":"Cat3","author":"mario"},
	                    {"measureName":"measure5","rulesName":"regola3","dateCreation":"24/1/2016","category":"Cat3","author":"Acilly"},
	                    {"measureName":"measure1","rulesName":"regola1","dateCreation":"20/1/2016","category":"Cat1","author":"pino"},
	                    {"measureName":"measure2","rulesName":"regola1","dateCreation":"23/1/2016","category":"Cat1","author":"gino"},
	                    {"measureName":"measure3","rulesName":"regola2","dateCreation":"21/1/2016","category":"Cat2","author":"lino"},
	                    {"measureName":"measure4","rulesName":"regola3","dateCreation":"22/1/2016","category":"Cat3","author":"mario"},
	                    {"measureName":"measure5","rulesName":"regola3","dateCreation":"24/1/2016","category":"Cat3","author":"Acilly"},
	                    {"measureName":"measure1","rulesName":"regola1","dateCreation":"20/1/2016","category":"Cat1","author":"pino"},
	                    {"measureName":"measure2","rulesName":"regola1","dateCreation":"23/1/2016","category":"Cat1","author":"gino"},
	                    {"measureName":"measure3","rulesName":"regola2","dateCreation":"21/1/2016","category":"Cat2","author":"lino"},
	                    {"measureName":"measure4","rulesName":"regola3","dateCreation":"22/1/2016","category":"Cat3","author":"mario"},
	                    {"measureName":"measure5","rulesName":"regola3","dateCreation":"24/1/2016","category":"Cat3","author":"Acilly"},
	                    ];
	$scope.measureColumnsList=[
	                           {"label":$scope.translate.load("sbi.kpi.measureName"),"name":"measureName"},
	                           {"label":$scope.translate.load("sbi.kpi.rulesName"),"name":"rulesName"},
	                          {"label":$scope.translate.load("sbi.generic.author"),"name":"author"},
	                           ];
	
	$scope.measureClickFunction=function(item){
		console.log("click",item);
	}
	
	$scope.deleteMeasure=function(item,event){
		 var confirm = $mdDialog.confirm()
         .title($scope.translate.load("sbi.kpi.measure.delete.title"))
         .content($scope.translate.load("sbi.kpi.measure.delete.content"))
         .ariaLabel('delete measure') 
         .ok($scope.translate.load("sbi.general.yes"))
         .cancel($scope.translate.load("sbi.general.No"));
		   $mdDialog.show(confirm).then(function() {
		     console.log( 'You decided to get rid of your debt.');
		   }, function() {
		    console.log("annulla")
		   });
	}
	
	$scope.measureMenuOption= [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,	 
		action : function(item,event) {
			$scope.deleteMeasure(item,event);
			}
	
		}];
}