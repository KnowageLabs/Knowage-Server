/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

(function() {

app.controller('kpiDefinitionThresholdController', ['$scope','sbiModule_translate','sbiModule_restServices','$mdSidenav','$mdDialog','$timeout', kpiDefinitionThresholdControllerFunction ]);

function kpiDefinitionThresholdControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdSidenav,$mdDialog,$timeout){
	$scope.thresholdList=[];
	$scope.translate = sbiModule_translate;

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
		var emptyThreshold={"position":$scope.kpi.threshold.thresholdValues.length,"label":"","color":"#00FFFF","includeMin":false,"includeMax":false,"minValue":"","maxValue":""}
		$scope.kpi.threshold.thresholdValues.push(emptyThreshold);
		$scope.loadThreshold();
		$scope.checkIfIsUsedByAnotherKpi();
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
     				  var tempPos = item.position;
         				$scope.kpi.threshold.thresholdValues.splice(index,1);
         				var nextItem=$scope.kpi.threshold.thresholdValues[index];
         				if(nextItem!=undefined){
         					nextItem.position=tempPos;
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

	$scope.severityType = [];

	sbiModule_restServices.promiseGet("2.0/domains","listByCode/SEVERITY")
		.then(function(response){
			angular.copy(response.data, $scope.severityType);
		},function(response){
			$scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.generic.error")+" domains->SEVERITY");
		}
	);

	$scope.colorPickerProperty = {
	    placeholder: 'select color',
	    format: 'hex'
	};


	$scope.thresholdColumn = [
        {
        	label:sbiModule_translate.load("sbi.browser.searchpanel.attributes.label"),
        	name:"label",
        	type:"inputtext"
        },
        {
        	label:sbiModule_translate.load("sbi.thresholds.min"),
        	name:"minValue",
        	type:"inputnumber"
        },
        {
        	label:sbiModule_translate.load("sbi.thresholds.includemin"),
        	name:"includeMin",
        	type:"checkbox"
        },
        {
        	label:sbiModule_translate.load("sbi.thresholds.max"),
        	name:"maxValue",
        	type:"inputnumber"
        },
        {
        	label:sbiModule_translate.load("sbi.thresholds.includemax"),
        	name:"includeMax",
        	type:"checkbox"
        },
        {
        	label:sbiModule_translate.load("sbi.thresholds.severity"),
        	name:"comboSeverity",
        	type:"select",
        	values: $scope.severityType
        },
        {
        	label:sbiModule_translate.load("sbi.thresholds.color"),
        	name:"color",
        	type:"colorpicker"
        },
    ];

	$scope.move = function(e,row,direction){
		var lower, current, upper;
		angular.forEach($scope.kpi.threshold.thresholdValues,function(value,key){
			if(value.position == row.position){
				current = key;
			}else if(value.position == row.position-1){
				upper = key;
			}else if(value.position == row.position+1){
				lower = key;
			}
		})
		if(direction=='up'){
			$scope.kpi.threshold.thresholdValues[upper].position = row.position;
			$scope.kpi.threshold.thresholdValues[current].position = row.position-1;
		}else{
			$scope.kpi.threshold.thresholdValues[lower].position = row.position;
			$scope.kpi.threshold.thresholdValues[current].position = row.position+1;
		}

	};

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

	$scope.normalizeRows = function(){
		var indexes = [];
		angular.forEach($scope.kpi.threshold.thresholdValues,function(value,key){
			indexes.push(value.position);
		})
		var shift = Math.min(...indexes);
		angular.forEach($scope.kpi.threshold.thresholdValues,function(value,key){
			value.position = (value.position - shift);
		})
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
			    	    $scope.normalizeRows();
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
})();