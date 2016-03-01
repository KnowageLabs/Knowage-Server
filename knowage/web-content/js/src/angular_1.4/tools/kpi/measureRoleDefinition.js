var measureRoleApp = angular.module('measureRoleManager', [ 'ngMaterial',  'angular_table' ,'angular_list','sbiModule', 'angular-list-detail','ui.codemirror','angularUtils.directives.dirPagination']);
measureRoleApp.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

measureRoleApp.controller('measureRoleMasterController', [ '$scope','sbiModule_translate','$mdDialog','sbiModule_config','sbiModule_restServices','$q' ,measureRoleMasterControllerFunction ]);
measureRoleApp.controller('measureListController', [ '$scope','sbiModule_translate','$mdDialog','sbiModule_restServices' ,measureListControllerFunction ]);
measureRoleApp.controller('measureDetailController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',measureDetailControllerFunction ]); 

function measureRoleMasterControllerFunction($scope,sbiModule_translate,$mdDialog,sbiModule_config,sbiModule_restServices,$q){
	$scope.translate=sbiModule_translate;
	$scope.aliasList=[];
	$scope.placeholderList=[];
	$scope.tipologiesType=[]; 
	$scope.currentRule={
			dataSourceId:{},
			definition:"SELECT * FROM employee as cur",
			metadata:{},
			placeholder:{}
				};
	$scope.detailProperty={
			dataSourcesIsSelected:false,
			queryChanged:true,
			previewData:{rows:[],metaData:{fields:[]}},
			};
	
	$scope.clearPreviewAndMetadata=function(){
		$scope.detailProperty.previewData={rows:[],metaData:{fields:[]}};
		$scope.currentRule.metadata={};
	}
	
	$scope.errorHandler=function(text,title){
		var titleFin=title || "";
		 var confirm = $mdDialog.confirm()
		.title(titleFin)
		.content(text)
		.ariaLabel('error') 
		.ok('OK') 
		return $mdDialog.show(confirm);
	}
	
	
 	$scope.newMeasureFunction=function(){
	} 
 	
 	$scope.saveMeasureFunction=function(){ 
 		$scope.checkValidityMeasureRole().then(function(){
 			 $mdDialog.show({
 	 		      controller: DialogSaveController,
 	 		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/kpi/measureRoleSubController/saveDialogTemplate.jsp',  
 	 		      clickOutsideToClose:true,
 	 		      fullscreen: true,
 	 		      locals:{
 	 		    	 currentRule:$scope.currentRule,
 	 		    	 aliasExsist:$scope.aliasExtist}
 	 		    })
 	 		    .then(function(answer) {
 	 		      }, function() {
 	 		       });
 		},function(message){
 			$scope.errorHandler(message.text,message.title); 
 			return;
 		})
 		
 		
 		
 		
 		
 	} 
 	
 	$scope.checkValidityMeasureRole=function(){
 		var deferred = $q.defer();
 		
 		//test query and set metadata if not setted
 		var postData={dataSourceId:$scope.currentRule.dataSourceId,
				query:$scope.currentRule.definition,
				maxItem:1
				} 
		sbiModule_restServices.promisePost("1.0/kpi","queryPreview",postData)
		.then(function(response){
			$scope.columnToMetadata(response.data.columns);
			
			//check if metadata are presents
	 		if(Object.keys($scope.currentRule.metadata).length==0){
	 			 deferred.reject({text:'',title:'Metadati non settati'})
	 		}
	 		
	 		//check if there is 1 measure
	 		var measurePresent=false;
	 		for(var key in $scope.currentRule.metadata){
	 			if($scope.currentRule.metadata[key].type.valueCd=="MEASURE"){
	 				measurePresent=true;
	 				break;
	 			}
	 		}
	 		if(!measurePresent){
	 			deferred.reject({text:'gestisci le misure dal tab METADATA',title:'Nessuna misura settata'})
	 		}
	 		
	 		deferred.resolve();
		},function(response){
			deferred.reject({text:response.data.errors[0].message,title:'errore nella query'})
		});
 		 
 		return deferred.promise; 
 	}
 	
 	$scope.cancelMeasureFunctionMeasureFunction=function(){
 		alert("cancelMeasureFunction")
 	} 
 	
 	$scope.loadAliasList=function(){
 		sbiModule_restServices.promiseGet("1.0/kpi","listAlias")
		.then(function(response){ 
			angular.copy(response.data,$scope.aliasList);
		},function(response){
			$scope.errorHandler(response.data.errors[0].message,'Errore nello scaricamento degli alias');
		});
 	};
	$scope.loadPlaceholderList=function(){
 		sbiModule_restServices.promiseGet("1.0/kpi","listPlaceholder")
		.then(function(response){ 
			angular.copy(response.data,$scope.placeholderList);
		},function(response){
			$scope.errorHandler(response.data.errors[0].message,'Errore nello scaricamento dei placeholder');
		});
 	};
 	$scope.loadAliasList();
 	$scope.loadPlaceholderList();
	
 	
 	$scope.columnToMetadata=function(columns){
		var tmpMeas=[];
		for(var index in  columns){
			tmpMeas.push( columns[index].label);
			if(!$scope.currentRule.metadata.hasOwnProperty( columns[index].label)){
				$scope.currentRule.metadata[columns[index].label]= columns[index]
				$scope.currentRule.metadata[columns[index].label].type= $scope.tipologiesType[1];
			}
		} 
		for(var index in $scope.currentRule.metadata){
			if(tmpMeas.indexOf($scope.currentRule.metadata[index].label)==-1){
				delete $scope.currentRule.metadata[index];
			}
		} 
	}
 	
 	$scope.aliasExtist=function(aliasName){
		 for(var i=0;i<$scope.aliasList.length;i++){
			if( $scope.aliasList[i].name==aliasName){
				return true;
			}
		}
		return false; 
	}
 	
 	$scope.loadPlaceholder=function(){
 		var placeh=$scope.currentRule.definition.match(/@\w*/g);
 		for(var i=0;i<placeh.length;i++){
 			var plcName=placeh[i].substring(1,placeh[i].length);
 			if(!$scope.currentRule.placeholder.hasOwnProperty(plcName))
 			$scope.currentRule.placeholder[plcName]="";
 		}
 	}
}

function DialogSaveController($scope, $mdDialog,currentRule,sbiModule_restServices,aliasExsist) {
	 
	$scope.presentAlias=[];
	$scope.newAlias=[];
	$scope.currentRule=currentRule;
	for(var key in currentRule.metadata){
		if(aliasExsist(currentRule.metadata[key].label)){
			$scope.presentAlias.push(currentRule.metadata[key].label);
		}else{
			$scope.newAlias.push(currentRule.metadata[key].label);
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
			  angular.copy(currentRule,tmpRoleObj);
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
			 
			 
			 sbiModule_restServices.promisePost("1.0/kpi","saveRule",tmpRoleObj)
				.then(function(response){ 
					alert("salvataggio riuscito")
					 $mdDialog.hide();
				},function(response){
					alert("errore salvataggio ruolo")
				});
			  
//		    $mdDialog.hide();
		  };
		}
 
function measureDetailControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	
	
	
	 
	$scope.loadMetadata=function(){
		$scope.loadPlaceholder();
		var postData={dataSourceId:$scope.currentRule.dataSourceId,
				query:$scope.currentRule.definition,
				maxItem:1
				} 
		sbiModule_restServices.promisePost("1.0/kpi","queryPreview",postData)
		.then(function(response){
			
			$scope.columnToMetadata(response.data.columns);
			 
		},function(response){
			$scope.errorHandler(response.data.errors[0].message,'Errore nella query');
			$scope.clearPreviewAndMetadata();
		});
	}
	
	$scope.loadPreview=function(){
		var postData={dataSourceId:$scope.currentRule.dataSourceId,
				query:$scope.currentRule.definition,
				maxItem:10
				}
		
		sbiModule_restServices.promisePost("1.0/kpi","queryPreview",postData)
		.then(function(response){
			 $scope.detailProperty.queryChanged=false;
			$scope.columnToMetadata(response.data.columns);
			angular.copy(response.data,$scope.detailProperty.previewData);
		},function(response){
			$scope.errorHandler(response.data.errors[0].message,'Errore nella query');
			$scope.clearPreviewAndMetadata();
			
		});
	}
	
	
}

function measureListControllerFunction($scope,sbiModule_translate,$mdDialog,sbiModule_restServices){
	$scope.translate=sbiModule_translate;
	$scope.measureRoleList=[];
	$scope.measureRoleColumnsList=[
	                           {"label":$scope.translate.load("sbi.kpi.measureName"),"name":"alias"},
	                           {"label":$scope.translate.load("sbi.kpi.rulesName"),"name":"rule"},
	                          {"label":$scope.translate.load("sbi.generic.author"),"name":"author"},
	                           ];
	
	$scope.loadMeasureRoleList=function(){
 		sbiModule_restServices.promiseGet("1.0/kpi","listMeasure")
		.then(function(response){
			$scope.measureRoleList=response.data;
			},function(response){
				$scope.errorHandler(response.data.errors[0].message,'Errore nel recuperare la lista delle misure');
		});
 	};
 	$scope.loadMeasureRoleList();
	
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