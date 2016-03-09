app.controller('kpiDefinitionThresholdController', ['$scope','sbiModule_translate','sbiModule_restServices','$mdSidenav', kpiDefinitionThresholdControllerFunction ]);

function kpiDefinitionThresholdControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdSidenav){
	$scope.thresholdList=[];
	
	$scope.loadPlaceholderList=function(){
 		sbiModule_restServices.promiseGet("1.0/kpi","listThreshold")
		.then(function(response){ 
			angular.copy(response.data,$scope.thresholdList);
		},function(response){
			$scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.threshold.error"));
		});
 	};
 	$scope.loadPlaceholderList();
	
	$scope.addNewThreshold=function(){
		var emptyThreshold={"position":$scope.kpi.threshold.thresholdValues.length+1,"label":"","color":"#00FFFF","includeMin":false,"includeMax":false,"minValue":"","maxValue":""}
		$scope.kpi.threshold.thresholdValues.push(emptyThreshold);
		$scope.loadThreshold();
	}
	
	$scope.thresholdTableActionButton=[
	                              	 {icon:'fa fa-trash' ,   
	                             		action : function(item,event) {
	                             				alert("canciella")
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
	                        	label:" position ",
	                        	name:"position",
	                        },
	                        {
	                        	label:"Label",
	                        	name:"inputLable",
	                        },
	                        {
	                        	label:"Min",
	                        	name:"includeNumericInputMin",
	                        	size: "60px"
	                        },
	                        {
	                        	label:"Include Min",
	                        	name:"includeMinCheck"
	                        },
	                        {
	                        	label:"Max",
	                        	name:"includeNumericInputMax",
	                        	size: "60px"
	                        },
	                        {
	                        	label:"Include Max",
	                        	name:"includeMaxCheck"
	                        },
	                        {
	                        	label:"Severity",
	                        	name:"comboSeverity"
	                        },
	                        {
	                        	label:"Color",
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
			toggleMin: function(row,column,evt){
				evt.stopPropagation();
				console.log("Download .....");
			},
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
				
			}
	}

	
}