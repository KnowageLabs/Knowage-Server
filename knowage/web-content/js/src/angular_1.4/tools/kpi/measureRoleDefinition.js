var measureRoleApp = angular.module('measureRoleManager', [ 'ngMaterial',  'angular_table' ,'angular_list','sbiModule', 'angular-list-detail','ui.codemirror','angularUtils.directives.dirPagination']);
measureRoleApp.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

measureRoleApp.controller('measureRoleMasterController', [ '$scope','sbiModule_translate','$mdDialog','sbiModule_config','sbiModule_restServices','$q','$angularListDetail','$timeout',measureRoleMasterControllerFunction ]);
measureRoleApp.controller('measureListController', [ '$scope','sbiModule_translate','$mdDialog','sbiModule_restServices' ,measureListControllerFunction ]);
measureRoleApp.controller('measureDetailController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',measureDetailControllerFunction ]); 

function measureRoleMasterControllerFunction($scope,sbiModule_translate,$mdDialog,sbiModule_config,sbiModule_restServices,$q,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.aliasList=[];
	$scope.placeholderList=[];
	$scope.tipologiesType=[]; 
	$scope.currentRule={};
	$scope.originalRule={};
	$scope.detailProperty={};
	
	$scope.emptyRule={
			dataSourceId:{},
			definition:"SELECT FROM WHERE",
			ruleOutputs:[],
			placeholders:[]
				};
	
	$scope.emptyProperty={
			dataSourcesIsSelected:false,
			queryChanged:true,
			previewData:{rows:[],metaData:{fields:[]}},
			};
	
	$scope.clearPreviewAndMetadata=function(prev,metdt){
		if(prev==true){
			$scope.detailProperty.previewData={rows:[],metaData:{fields:[]}};
		}
		if(metdt==true){
			$scope.currentRule.ruleOutputs=[];
			
		}
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
	 	angular.copy($scope.emptyRule,$scope.currentRule);
	 	angular.copy($scope.emptyRule,$scope.originalRule);
	 	angular.copy($scope.emptyProperty,$scope.detailProperty);
	 	$angularListDetail.goToDetail();
	};
 	
 	$scope.cancelMeasureFunction=function(){
 		if(!angular.equals($scope.originalRule,$scope.currentRule)){
	 		var confirm = $mdDialog.confirm()
	        .title('Modifica in corso?')
	        .content('sei sicuro di voler annullare l\'operazione?.')
	        .ariaLabel('cancel metadata') 
	        .ok('OK')
	        .cancel('CANCEL');
			  $mdDialog.show(confirm).then(function() {
				  $angularListDetail.goToList();
			  }, function() {
			   return;
			  });
 		}else{
 			  $angularListDetail.goToList();
 		}
 		
 		
 	};
 	
 	$scope.measureClickFunction=function(item){
		console.log("click",item);
		
		sbiModule_restServices.promiseGet("1.0/kpi",item.ruleId+"/loadRule")
		.then(function(response){ 
			angular.copy(response.data,$scope.currentRule);
			angular.copy(response.data,$scope.originalRule);
			angular.copy($scope.emptyProperty,$scope.detailProperty);
			$scope.detailProperty.dataSourcesIsSelected=true;
			$scope.detailProperty.queryChanged=false;
			$angularListDetail.goToDetail();
			$timeout(function(){
			 angular.element(document.getElementsByClassName("CodeMirror")[0])[0].CodeMirror.refresh();
			},0)
		},function(response){
			console.log("errore")
		});
		
		 
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
 	 		    	 aliasExsist:$scope.aliasExtist
 	 		    	 }
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
 		var postData={
 				rule:$scope.currentRule
				} 
		sbiModule_restServices.promisePost("1.0/kpi","queryPreview",postData)
		.then(function(response){
			$scope.columnToRuleOutputs(response.data.columns);
			
			//check if metadata are presents
	 			if($scope.currentRule.ruleOutputs.length==0){
	 			 deferred.reject({text:'',title:'Metadati non settati'})
	 		}
	 		
	 		//check if there is 1 measure
	 		var measurePresent=false;
	 		for(var key in $scope.currentRule.ruleOutputs){
	 			if($scope.currentRule.ruleOutputs[key].type.valueCd=="MEASURE"){
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
	
 	$scope.ruleOutputsIndexOfColumName=function(cname){
 		for(var i=0;i<$scope.currentRule.ruleOutputs.length;i++){
 			if($scope.currentRule.ruleOutputs[i].alias==cname){
 				return i;
 			}
 		}
 		return -1;
 	};
 	
 	$scope.placeholderIndexOfValue=function(cname){
 		for(var i=0;i<$scope.currentRule.placeholders.length;i++){
 			if($scope.currentRule.placeholders[i].name==cname){
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	$scope.columnToRuleOutputs=function(columns){
 		var tmpMeas=[];
 		
 		//add new Metadata
 		for(var index in  columns){
 			tmpMeas.push( columns[index].label);
 			if($scope.ruleOutputsIndexOfColumName(columns[index].label)==-1){
 				$scope.currentRule.ruleOutputs.push({
 					alias:columns[index].label,
 					type:$scope.tipologiesType[1]});
 			}
 		}
 		
 		//remove unused metadata
 		 
 		for(var index=0; index<$scope.currentRule.ruleOutputs.length;index++){ 
			if(tmpMeas.indexOf($scope.currentRule.ruleOutputs[index].alias)==-1){
				$scope.currentRule.ruleOutputs.splice(index,1);
				index--;
			}
		} 
 	};
 	 	
 	$scope.aliasExtist=function(aliasName){
		 for(var i=0;i<$scope.aliasList.length;i++){
			if( $scope.aliasList[i].name==aliasName){
				return true;
			}
		}
		return false; 
	}
 	
 	$scope.getPlaceholder=function(plcName){
 		for(var i=0;i<$scope.placeholderList.length;i++){
 			if($scope.placeholderList[i].name==plcName){
 				return $scope.placeholderList[i];
 			}
 		}
 		return undefined;
 	};
 	
 	$scope.loadPlaceholder=function(){
 		var placeh=$scope.currentRule.definition.match(/@\w*/g);
 		if(placeh!=null){
 			//add new placeholder
 			for(var i=0;i<placeh.length;i++){
 				var plcName=placeh[i].substring(1,placeh[i].length);
 					if($scope.placeholderIndexOfValue(plcName)==-1){
 						
 						var plcObject=$scope.getPlaceholder(plcName);
 						if(plcObject==undefined){
 							var tmpPlcNew={
 									name:plcName,
 			 	 					value:""
 							};
 							$scope.currentRule.placeholders.push(tmpPlcNew)
 						}else{
 							$scope.currentRule.placeholders.push(plcObject)
 						} 
 					}
 			}
 			
 			//remove unused placeholder
 			for(var index=0;index<$scope.currentRule.placeholders.length;index++){
 				if(placeh.indexOf("@"+$scope.currentRule.placeholders[index].name)==-1){
 					$scope.currentRule.placeholders.splice(index,1);
 					index--;
 				}
 			}
 		}else{
 			angular.copy([], $scope.currentRule.placeholders);
 		}
 		
 	}
 	
}

function DialogSaveController($scope, $mdDialog,currentRule,sbiModule_restServices,aliasExsist) {
	 
	$scope.presentAlias=[];
	$scope.newAlias=[];
	$scope.currentRule=currentRule;
	for(var key in currentRule.ruleOutputs){
		if(aliasExsist(currentRule.ruleOutputs[key].alias)){
			$scope.presentAlias.push(currentRule.ruleOutputs[key].alias);
		}else{
			$scope.newAlias.push(currentRule.ruleOutputs[key].alias);
		}
	}

	
	  $scope.hide = function() {
		    $mdDialog.hide();
		  };
		  $scope.cancel = function() {
		    $mdDialog.cancel();
		  };
		  $scope.save = function() {
			  sbiModule_restServices.promisePost("1.0/kpi","saveRule",currentRule)
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
	
	$scope.havePlaceholder=function(){
 		return !angular.equals($scope.currentRule.placeholders, {});
 	}
	
	$scope.loadMetadata=function(){
		$scope.loadPlaceholder();
		var postData={
				rule:$scope.currentRule
				} 
		sbiModule_restServices.promisePost("1.0/kpi","queryPreview",postData)
		.then(function(response){
			
			$scope.columnToRuleOutputs(response.data.columns);
			 
		},function(response){
			$scope.errorHandler(response.data.errors[0].message,'Errore nella query');
			$scope.clearPreviewAndMetadata(true,true);
		});
	}
	
	$scope.loadPreview=function(checkPlaceholder){
		$scope.loadPlaceholder();
		
		if(checkPlaceholder!=true ||  (checkPlaceholder==true && !$scope.havePlaceholder())){
				
			var postData={
					rule:$scope.currentRule,
					maxItem:10
					}
			
			sbiModule_restServices.promisePost("1.0/kpi","queryPreview",postData)
			.then(function(response){
				 $scope.detailProperty.queryChanged=false;
				$scope.columnToRuleOutputs(response.data.columns);
				angular.copy(response.data,$scope.detailProperty.previewData);
			},function(response){
				$scope.errorHandler(response.data.errors[0].message,'Errore nella query');
				$scope.clearPreviewAndMetadata(true,false);
				
			});
		}else{
			$scope.clearPreviewAndMetadata(true,false);
			}
	
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
	
	
	
	$scope.deleteMeasure=function(item,event){
		 var confirm = $mdDialog.confirm()
         .title($scope.translate.load("sbi.kpi.measure.delete.title"))
         .content($scope.translate.load("sbi.kpi.measure.delete.content"))
         .ariaLabel('delete measure') 
         .ok($scope.translate.load("sbi.general.yes"))
         .cancel($scope.translate.load("sbi.general.No"));
		   $mdDialog.show(confirm).then(function() {
		     
			   
			   sbiModule_restServices.promiseDelete("1.0/kpi",item.ruleId+"/deleteRule").then(
					   function(response){
						   alert("cancellato")
					   },
					   function(response){
						   $scope.errorHandler(response.data.errors[0].message,""); 
					   });
			   
			   
			   
			   
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