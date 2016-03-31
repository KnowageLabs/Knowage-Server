var measureRuleApp = angular.module('measureRuleManager', [ 'ngMaterial',  'angular_table' ,'angular_list','sbiModule', 'angular-list-detail','ui.codemirror','angularUtils.directives.dirPagination']);
measureRuleApp.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

measureRuleApp.controller('measureRuleMasterController', [ '$scope','sbiModule_translate','$mdDialog','sbiModule_config','sbiModule_restServices','$q','$angularListDetail','$timeout',measureRuleMasterControllerFunction ]);
measureRuleApp.controller('measureListController', [ '$scope','sbiModule_translate','$mdDialog','sbiModule_restServices','$angularListDetail','$timeout','$mdToast' ,measureListControllerFunction ]);
measureRuleApp.controller('measureDetailController', [ '$scope','sbiModule_translate' ,'$mdDialog' ,'sbiModule_restServices','sbiModule_config','$q','$angularListDetail',measureDetailControllerFunction ]); 

function measureRuleMasterControllerFunction($scope,sbiModule_translate,$mdDialog,sbiModule_config,sbiModule_restServices,$q,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.currentRule={};
	$scope.detailProperty={};
	$scope.originalRule={};
	$scope.selectedTab={'tab':0};
	
	$scope.updateListRule=function(){
		$scope.$broadcast("updateListRule");
	}
	
	$scope.loadBroadcastRuleById=function(ruleId,ruleVersion){
		$scope.$broadcast("loadRuleById",{ruleId:ruleId,ruleVersion:ruleVersion});
	}
	
	$scope.broadcastAlterDatasource=function(dataSourceId){
		
		$scope.$broadcast("alterDatasource",{datasourceId:dataSourceId});
	}
	
	$scope.broadcastLoadAliasList=function(ruleId){
		var deferred = $q.defer();
		$scope.$broadcast("loadAliasList",{ruleId:ruleId,deferred:deferred});
		return deferred.promise;
	}
	
	$scope.emptyRule={
			dataSourceId:{},
			definition:"SELECT\n\nFROM\n\nWHERE",
			ruleOutputs:[],
			placeholders:[]
	};
	
	$scope.emptyProperty={
			dataSourcesIsSelected:false,
			queryChanged:true,
			previewData:{rows:[],metaData:{fields:[]}},
	};
	
	$scope.errorHandler=function(text,title){
		var titleFin=title || "";
		var textFin=text;
		if(angular.isObject(text)){
			if(text.hasOwnProperty("errors")){
				textFin="";
				for(var i=0;i<text.errors.length;i++){
					textFin+=text.errors[i].message+" <br> ";
				}
			}else{
				textFin=JSON.stringify(text)
			}
		}
		
		var confirm = $mdDialog.confirm()
		.title(titleFin)
		.content(textFin)
		.ariaLabel('error') 
		.ok('OK') 
		return $mdDialog.show(confirm);
	}
	
}

function DialogSaveController($scope, $mdDialog,$mdToast,currentRule,originalRule,sbiModule_restServices,aliasExsist,sbiModule_translate,updateListRule,getPlaceholder,loadBroadcastRuleById,loadPlaceholderListFunction) {
	$scope.translate=sbiModule_translate;
	$scope.reusedAlias=[];
	$scope.newAlias=[];
	$scope.reusedPlaceholder=[];
	$scope.newPlaceholder=[];
	$scope.currentRule=currentRule;
	for(var key in currentRule.ruleOutputs){
		if(aliasExsist(currentRule.ruleOutputs[key].alias)){
			$scope.reusedAlias.push(currentRule.ruleOutputs[key].alias);
		}else{
			$scope.newAlias.push(currentRule.ruleOutputs[key].alias);
		}
	}
	
	for(var key in currentRule.placeholders){
		if(getPlaceholder(currentRule.placeholders[key].name)!=undefined){
			$scope.reusedPlaceholder.push(currentRule.placeholders[key].name);
		}else{
			$scope.newPlaceholder.push(currentRule.placeholders[key].name);
		}
	}
	
	$scope.reusedAlias.sort();
	$scope.newAlias.sort();
	$scope.reusedPlaceholder.sort();
	$scope.newPlaceholder.sort(); 
	
	  $scope.hide = function() {
		    $mdDialog.hide();
		  };
		  $scope.cancel = function() {
		    $mdDialog.cancel();
		  };
		  $scope.save = function() {
			  sbiModule_restServices.promisePost("1.0/kpi","saveRule",currentRule)
				.then(function(response){ 
					$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kpi.rule.save.success")).position('top').action($scope.translate.load("sbi.general.yes")).highlightAction(false).hideDelay(2000))
					.then(function(){
						$mdDialog.hide();
						updateListRule();
						if($scope.newPlaceholder.length>0){
							loadPlaceholderListFunction();
						}
						loadBroadcastRuleById(response.data.id,response.data.version);
						})
					
				},function(response){
					$scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.save.success"));
				});
		  };
		}
function measureDetailControllerFunction($scope,sbiModule_translate ,$mdDialog ,sbiModule_restServices,sbiModule_config,$q,$angularListDetail){
	
	$scope.loadMetadata=function(){
		var deferred = $q.defer();
		$scope.loadPlaceholder();
		var postData={
				rule:$scope.currentRule
				} 
		sbiModule_restServices.promisePost("1.0/kpi","queryPreview",postData)
		.then(function(response){
			 $scope.detailProperty.queryChanged=false;
			$scope.columnToRuleOutputs(response.data.columns);
			deferred.resolve();
		},function(response){
			$scope.errorHandler(response.data,""+sbiModule_translate.load("sbi.kpi.rule.load.metadata.error")+""+sbiModule_translate.load("sbi.kpi.rule.query.wrong"));
			$scope.clearPreviewAndMetadata(true,true);
			deferred.reject();
		});
		
		return deferred.promise; 
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
				$scope.errorHandler(response.data,""+sbiModule_translate.load("sbi.kpi.rule.load.preview.error")+""+sbiModule_translate.load("sbi.kpi.rule.query.wrong"));
				$scope.clearPreviewAndMetadata(true,false);
				
			});
		}else{
			$scope.clearPreviewAndMetadata(true,false);
			}
	
	}
	
	$scope.havePlaceholder=function(){
		if($scope.currentRule.placeholders){
			return $scope.currentRule.placeholders.length>0;
		}
		return false;
	}
	
	$scope.aliasList=[];
	$scope.placeholderList=[];
	$scope.tipologiesType=[]; 
	
	
	$scope.clearPreviewAndMetadata=function(prev,metdt){
		if(prev==true){
			$scope.detailProperty.previewData={rows:[],metaData:{fields:[]}};
		}
		if(metdt==true){
			$scope.currentRule.ruleOutputs=[];
			
		}
	}
	
 	$scope.cancelMeasureFunction=function(){
 		if(!angular.equals($scope.originalRule,$scope.currentRule)){
	 		var confirm = $mdDialog.confirm()
	        .title(sbiModule_translate.load("sbi.layer.modify.progress"))
	        .content(sbiModule_translate.load("sbi.layer.modify.progress.message.modify"))
	        .ariaLabel('cancel metadata') 
			.ok(sbiModule_translate.load("sbi.general.yes"))
			.cancel(sbiModule_translate.load("sbi.general.No"));
			  $mdDialog.show(confirm).then(function() {
				  $angularListDetail.goToList();
			  }, function() {
			   return;
			  });
 		}else{
 			  $angularListDetail.goToList();
 		} 
 	};
 	
 	
 	$scope.saveMeasureFunction=function(){ 
 		$scope.checkValiditymeasureRule().then(function(){
 			 $mdDialog.show({
 	 		      controller: DialogSaveController,
 	 		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/kpi/measureRuleSubController/saveDialogTemplate.jsp',  
 	 		      clickOutsideToClose:true,
 	 		      fullscreen: true,
 	 		      locals:{
 	 		    	 currentRule:$scope.currentRule,
 	 		    	 aliasExsist:$scope.aliasExtist,
 	 		    	updateListRule:$scope.updateListRule,
 	 		    	originalRule:$scope.originalRule,
 	 		    	getPlaceholder:$scope.getPlaceholder,
 	 		    	loadPlaceholderListFunction:$scope.loadPlaceholderList,
 	 		    	loadBroadcastRuleById:$scope.loadBroadcastRuleById
 	 		    	 }
 	 		    })
 	 		    .then(function(answer) {
 	 		      }, function() {
 	 		       });
 		},function(response){
 			if(response.hasOwnProperty("data")){
				$scope.errorHandler(response.data); 
 			}else if(response.hasOwnProperty("text") || response.hasOwnProperty("title")){
 				$scope.errorHandler(response.text,response.title); 
 			}
 			return;
 		})
 	} 
 	
 	$scope.preSaveControl=function(deferred){
 		sbiModule_restServices.promisePost("1.0/kpi","preSaveRule",$scope.currentRule)
		.then(function(response){
			
			//check if metadata are presents
	 			if($scope.currentRule.ruleOutputs.length==0){
	 			 deferred.reject({text:sbiModule_translate.load("sbi.kpi.rule.presave.metadata.missing.text"),title:sbiModule_translate.load("sbi.kpi.rule.presave.metadata.missing")})
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
	 			deferred.reject({text:sbiModule_translate.load("sbi.kpi.rule.presave.metadata.missing.text"),title:sbiModule_translate.load("sbi.kpi.rule.presave.metadata.no.measure.set")})
	 		}
	 		
	 		deferred.resolve();
		},function(response){
			deferred.reject(response)
		});
 	};
 	$scope.checkValiditymeasureRule=function(){
 		var deferred = $q.defer();
 		if($scope.detailProperty.queryChanged==true){
 			$scope.loadMetadata().then(
 					function(){
 						$scope.preSaveControl(deferred);
		 			},function(){
		 				deferred.reject()
		 			});
 		}else{
 			$scope.preSaveControl(deferred);
 		}
 		return deferred.promise; 
 	}
 	 
 	$scope.loadAliasList=function(ruleId,deferred){
 		
 		var currentRuleId=""
 		if(ruleId){
 			currentRuleId="/"+ruleId;
 		}
 		sbiModule_restServices.promiseGet("1.0/kpi","listAvailableAlias"+currentRuleId)
		.then(function(response){ 
			angular.copy(response.data,$scope.aliasList);
			if(deferred){
				deferred.resolve();
			}
		},function(response){
			$scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.alias.error"));
			if(deferred){
				deferred.reject();
			}
		});
 		
 	};
 	
 	$scope.$on('loadAliasList', function(event, args) {
 		$scope.loadAliasList(args.ruleId,args.deferred);
 	});
 	
	$scope.loadPlaceholderList=function(){
 		sbiModule_restServices.promiseGet("1.0/kpi","listPlaceholder")
		.then(function(response){ 
			angular.copy(response.data,$scope.placeholderList);
		},function(response){
			$scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.placeholder.error"));
		});
 	};
// 	$scope.loadAliasList();
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
 				var isBlocked=false;
 				var type=$scope.tipologiesType[1];
 				if(columns[index].type=='int' || columns[index].type=="float"){
 					 type=$scope.tipologiesType[0];
 				}
 				
 				$scope.currentRule.ruleOutputs.push({
 					alias:columns[index].label,
 					type:type });
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
			if(angular.equals($scope.aliasList[i].name.toUpperCase(),aliasName.toUpperCase())){
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

function measureListControllerFunction($scope,sbiModule_translate,$mdDialog,sbiModule_restServices,$angularListDetail,$timeout,$mdToast ){
	$scope.translate=sbiModule_translate;
	
	$scope.newMeasureFunction=function(){
		angular.copy($scope.emptyRule,$scope.currentRule);
		angular.copy($scope.emptyRule,$scope.originalRule);
		angular.copy($scope.emptyProperty,$scope.detailProperty);
		$scope.broadcastLoadAliasList();
		$angularListDetail.goToDetail();
	};
	
	$scope.$on('loadRuleById', function(event, args) {
 		$scope.loadRuleById(args.ruleId,args.ruleVersion);
 	});
	
	$scope.loadRuleById=function(ruleId,ruleVersion,clone){ 
		var rid;
		if(clone!=true){
			rid=ruleId;
		}
		$scope.broadcastLoadAliasList(rid).then(
				function(){
					sbiModule_restServices.promiseGet("1.0/kpi",ruleId+"/"+ruleVersion+"/loadRule")
					.then(function(response){ 
						if(clone==true){
							response.data.id=undefined;
							response.data.name= sbiModule_translate.load("sbi.generic.copyof")+" "+response.data.name; 
						}
						angular.copy(response.data,$scope.currentRule);
						angular.copy(response.data,$scope.originalRule);
						angular.copy($scope.emptyProperty,$scope.detailProperty);
						$scope.detailProperty.dataSourcesIsSelected=true;
						$scope.detailProperty.queryChanged=false;  
						$scope.broadcastAlterDatasource(response.data.dataSourceId); 
						$angularListDetail.goToDetail();
						$timeout(function(){
							$scope.selectedTab.tab=0;
							angular.element(document.getElementsByClassName("CodeMirror")[0])[0].CodeMirror.refresh();
						},0)
					},function(response){
						$scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.rule.error"));
					});  
				});
	};
	
	$scope.measureClickFunction=function(item){
		$scope.loadRuleById(item.ruleId,item.ruleVersion,false);
	};
	
	$scope.measureRuleList=[];
	$scope.measureRuleColumnsList=[
	                           {"label":$scope.translate.load("sbi.kpi.measureName"),"name":"alias"},
	                           {"label":$scope.translate.load("sbi.kpi.rulesName"),"name":"rule"},
	                           {"label":$scope.translate.load("sbi.generic.category"),"name":"categoryName"},                           
//	                           {"label":$scope.translate.load("sbi.generic.category"),"name":"category.translatedValueName"},                           
	                          {"label":$scope.translate.load("sbi.generic.author"),"name":"author"},
	                           ];
	
	$scope.loadmeasureRuleList=function(){
		sbiModule_restServices.promiseGet("1.0/kpi","listMeasure")
		.then(function(response){
			for(var i=0;i<response.data.length;i++){
				if(response.data[i].category!=null){
					response.data[i].categoryName=response.data[i].category.translatedValueName;
				}
				
			}
			$scope.measureRuleList=response.data;
			},function(response){
				$scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.rule.list.error"));
		});
 	};
 	$scope.loadmeasureRuleList();
	
 	
 	$scope.$on('updateListRule', function(event, args) {
 		$scope.loadmeasureRuleList();
 	});
	
	
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
						   $mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.catalogues.toast.deleted")).position('top').action(
							'OK').highlightAction(false).hideDelay(2000))
						   $scope.loadmeasureRuleList();
					   },
					   function(response){
						   $scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.remove.ko")); 
					   }); }, function() {
		    console.log("annulla")
		   });
	};
	
	$scope.cloneMeasure=function(item,event){
		 var confirm = $mdDialog.confirm()
        .title($scope.translate.load("sbi.generic.confirmClone"))
        .ariaLabel('clone measure') 
        .ok($scope.translate.load("sbi.general.yes"))
        .cancel($scope.translate.load("sbi.general.No"));
		   $mdDialog.show(confirm).then(function() {
			   $scope.loadRuleById(item.ruleId,item.ruleVersion,true);    
		   }, function() {
		    console.log("annulla")
		   });
	};
	
	$scope.measureMenuOption= [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,
		backgroundColor:'transparent',
		action : function(item,event) {
			$scope.deleteMeasure(item,event);
			}
	
		},
		{
			label : sbiModule_translate.load('sbi.generic.clone'),
			icon:'fa fa-copy' ,	 
			backgroundColor:'transparent',
			action : function(item,event) {
				$scope.cloneMeasure(item,event);
				}
		
			}];
}