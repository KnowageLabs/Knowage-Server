app.controller('kpiDefinitionThresholdController', ['$scope','sbiModule_translate','sbiModule_restServices','$mdSidenav','$mdDialog','$timeout', kpiDefinitionThresholdControllerFunction ]);

function kpiDefinitionThresholdControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdSidenav,$mdDialog,$timeout){
	$scope.thresholdList=[];
	
	
	$scope.loadThresholdList=function(){
 		sbiModule_restServices.promiseGet("1.0/kpi","listThreshold")
		.then(function(response){ 
			angular.copy(response.data,$scope.thresholdList);
		},function(response){
			$scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.threshold.error"));
		});
 	};
 	$scope.loadThresholdList();
 	
 	$scope.loadThresholdTypeList=function(){
 		sbiModule_restServices.promiseGet("2.0/domains","listByCode/THRESHOLD_TYPE")
 		.then(function(response){ 
 			angular.copy(response.data,$scope.thresholdTypeList); 
 		},function(response){
 			 $scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.generic.error")+" domains->THRESHOLD_TYPE"); 
 		});
 		};
 	$scope.loadThresholdTypeList();
 	
 	
	
	$scope.addNewThreshold=function(){
		var emptyThreshold={"position":$scope.kpi.threshold.thresholdValues.length+1,"label":"","color":"#00FFFF","includeMin":false,"includeMax":false,"minValue":"","maxValue":""}
		$scope.kpi.threshold.thresholdValues.push(emptyThreshold);
		$scope.loadThreshold();
		$scope.checkIfIsUsedByAnotherKpi();
		$timeout(function() {
		var tbox=angular.element(document.querySelector('#angularTableTemplate.kpiListTableThresholdItemBox #angularTableContentBox'))[0];
		tbox.scrollTop = tbox.scrollHeight;
		},0);
	};
	

	
	$scope.thresholdTableActionButton=[
	                              	 {icon:'fa fa-trash' ,   
	                             		action : function(item,event) {	                             			
	                             			 var confirm = $mdDialog.confirm()
	                             	         .title($scope.translate.load("sbi.kpi.measure.delete.title"))
	                             			.content($scope.translate.load("sbi.kpi.measure.delete.content"))
	                             	         .ariaLabel('delete threshold item')
	                             	         .ok($scope.translate.load("sbi.general.yes"))
	                             	         .cancel($scope.translate.load("sbi.general.No"));
	                             			   $mdDialog.show(confirm).then(function() {
	                             				  var index=$scope.kpi.threshold.thresholdValues.indexOf(item);
		                             				$scope.kpi.threshold.thresholdValues.splice(index,1);
		                             				var nextItem=$scope.kpi.threshold.thresholdValues[index];
		                             				if(nextItem!=undefined){
		                             					nextItem.position=index+1;
		                             				}
		                             				$scope.checkIfIsUsedByAnotherKpi();
	                             			   }, function() {
	                             			  //do nothing
	                             			   });
	                             		 }
	                             	} 
	                             ];

	
		
	$scope.openThresholdSidenav=function(){
		 $mdSidenav("thresholdTab").toggle();
	}
	
	$scope.thresholdColumn=[
	                        {
	                        	label:"  ",
	                        	name:"move",
	                        	size:"70px"
	                        },
	                        {
	                        	label:sbiModule_translate.load("sbi.thresholds.position"),
	                        	name:"position",
	                        },
	                        {
	                        	label:sbiModule_translate.load("sbi.browser.searchpanel.attributes.label"),
	                        	name:"inputLable",
	                        },
	                        {
	                        	label:sbiModule_translate.load("sbi.thresholds.min"),
	                        	name:"includeNumericInputMin",
	                        	size: "60px"
	                        },
	                        {
	                        	label:sbiModule_translate.load("sbi.thresholds.includemin"),
	                        	name:"includeMinCheck"
	                        },
	                        {
	                        	label:sbiModule_translate.load("sbi.thresholds.max"),
	                        	name:"includeNumericInputMax",
	                        	size: "60px"
	                        },
	                        {
	                        	label:sbiModule_translate.load("sbi.thresholds.includemax"),
	                        	name:"includeMaxCheck"
	                        },
	                        {
	                        	label:sbiModule_translate.load("sbi.thresholds.severity"),
	                        	name:"comboSeverity"
	                        },
	                        {
	                        	label:sbiModule_translate.load("sbi.thresholds.color"),
	                        	name:"selectColor",
	                        	size:"90px"
	                        },
	                        
	                        ];
	
	sbiModule_restServices.promiseGet("2.0/domains","listByCode/SEVERITY")
	.then(function(response){ 
		angular.copy(response.data, $scope.thresholdFunction.severityType);
	},function(response){
		 $scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.generic.error")+" domains->SEVERITY"); 
	});
	
	
	
	
	$scope.thresholdFunction={ 
			severityType:[],
			moveUp: function(evt,index){
				evt.stopPropagation();
				if(index==0)return;
				var tmp={};
				angular.copy($scope.kpi.threshold.thresholdValues[index-1],tmp);
				angular.copy($scope.kpi.threshold.thresholdValues[index],$scope.kpi.threshold.thresholdValues[index-1]);
				angular.copy(tmp,$scope.kpi.threshold.thresholdValues[index]);
				//change the index
				$scope.kpi.threshold.thresholdValues[index].position=$scope.kpi.threshold.thresholdValues[index-1].position;
				$scope.kpi.threshold.thresholdValues[index-1].position=tmp.position;
				
			},
			moveDown: function(evt,index){
				evt.stopPropagation();
				if(index==$scope.kpi.threshold.thresholdValues.length-1)return;
				var tmp={};
				angular.copy($scope.kpi.threshold.thresholdValues[index+1],tmp);
				angular.copy($scope.kpi.threshold.thresholdValues[index],$scope.kpi.threshold.thresholdValues[index+1]);
				angular.copy(tmp,$scope.kpi.threshold.thresholdValues[index]);
				
				//change the index
				$scope.kpi.threshold.thresholdValues[index].position=$scope.kpi.threshold.thresholdValues[index+1].position;
				$scope.kpi.threshold.thresholdValues[index+1].position=tmp.position;
				
			},
			addNewThreshold: function(){
				$scope.addNewThreshold();
			}
	}

	$scope.loadSelectedThreshold=function(item,listId){ 
		
		if($scope.kpi.threshold.thresholdValues.length==0 ||  angular.equals($scope.emptyKpi.threshold,$scope.kpi.threshold)){
			loadSelThresh(item)
		}else{
		
		 var confirm = $mdDialog.confirm()
         .title($scope.translate.load("sbi.thresholds.already.present"))
         .content($scope.translate.load("sbi.thresholds.confirm.override"))
         .ariaLabel('Load threshold')
         .ok($scope.translate.load("sbi.general.yes"))
         .cancel($scope.translate.load("sbi.general.No"));
		   $mdDialog.show(confirm).then(function() {
			   loadSelThresh(item)
		   }, function() {
		  //do nothing
		   });
		}
		
		
	}
	
	$scope.cloneThreshold=function(){
		$scope.kpi.threshold.name+= " ("+$scope.translate.load("sbi.generic.clone")+")"
		$scope.kpi.threshold.id=undefined;	
		$scope.isUsedByAnotherKpi.value=false;
	}
	 
	
	function loadSelThresh(item){
	 var currKpiId="";
			if($scope.kpi.id!=undefined){
				currKpiId="?kpiId="+$scope.kpi.id;
			}
		sbiModule_restServices.promiseGet("1.0/kpi",item.id+"/loadThreshold"+currKpiId)
		.then(function(response){  
			
			if(response.data.usedByKpi==true){
				
				$mdDialog.show({
			          clickOutsideToClose: false, 
			          preserveScope: true,  
			          locals: {translate:sbiModule_translate},
			          template: '<md-dialog>' +
			          			'<md-toolbar>  <div class="md-toolbar-tools"> <h2>{{ translate.load("sbi.kpi.threshold.load.reused.title")}}</h2></div></md-toolbar>'+
			                    '  <md-dialog-content layout-margin layout="column"> ' + 
			                    '  <p> {{ translate.load("sbi.kpi.threshold.load.reused.message")}} </p> ' +
				                 '   <div layout="row"> ' +
							      '  <md-button class="md-raised" ng-click="cancelDialog()" flex> {{translate.load("sbi.generic.cancel")}} </md-button>' + 
				                  '  <md-button class="md-raised" ng-click="successDialog()" flex>  {{translate.load("sbi.generic.use.it")}} </md-button> '+
				                 '   <md-button class="md-raised"  ng-click="successDialog(true)"  flex>  {{translate.load("sbi.generic.clone")}} </md-button> '+
				                 ' </div>'+
				                ' </md-dialog-content>' +
			                    '</md-dialog>',
			          controller: function DialogController($scope, $mdDialog,translate) { 
			        	  $scope.translate=translate;
			            $scope.cancelDialog = function() {
			            	$mdDialog.cancel();
			            }
			            $scope.successDialog = function(answer) {
			            	$mdDialog.hide(answer);
			            }
			          }
			       }).then(function(clone) {
			    	    if(clone){
			    	    	 response.data.name+= " ("+$scope.translate.load("sbi.generic.clone")+")"
							 response.data.id=undefined;	
			    	    } 
			    	    angular.copy(response.data,$scope.kpi.threshold);
						$scope.loadThreshold(); 
						 $mdSidenav('thresholdTab').close()
			       }) ;
				 
			}else{
				angular.copy(response.data,$scope.kpi.threshold);
				$scope.loadThreshold();
				 $mdSidenav('thresholdTab').close()
			}
			
			
			
			
			 
		},function(response){
			$scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.threshold.error"));
		});
	}
}