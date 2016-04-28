angular.module('alertDefinitionManager').controller('alertKpiDefinitionController', ['$scope','sbiModule_translate', 'sbiModule_restServices','sbiModule_config','$mdDialog','$window','$timeout',alertKpiDefinitionControllerFunction ]);

function alertKpiDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices,sbiModule_config,$mdDialog,$window,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.kpiList=[];
	$scope.emptyKpiAlarm={action:[]};
	$scope.currentKpiAlarm=angular.extend({},$scope.emptyKpiAlarm);
	
	
	$scope.currentKpiAlarm.action=[{"type":"mail","jsonTemplate":{"mailTo":["gianlucaulivo@hotmail.it"],"subObject":"MinimoErrore","mailBody":"Questo è un test"},"threshold":[{"id":30,"position":1,"label":"Minimo","color":"#3DFF00","severityId":86,"severityCd":"LOW","minValue":0,"includeMin":true,"maxValue":10,"includeMax":false}]},{"type":"mail","jsonTemplate":{"mailTo":["gianlucaulivo@hotmail.it"],"subObject":"medio errore","mailBody":"test"},"threshold":[{"id":30,"position":1,"label":"Minimo","color":"#3DFF00","severityId":86,"severityCd":"LOW","minValue":0,"includeMin":true,"maxValue":10,"includeMax":false},{"id":31,"position":2,"label":"medio","color":"#FFEB00","severityId":85,"severityCd":"MEDIUM","minValue":10,"includeMin":true,"maxValue":100,"includeMax":false}]},{"type":"mail","jsonTemplate":{"mailTo":["gianlucaulivo@hotmail.it"],"subObject":"errore estremo","mailBody":"terst"},"threshold":[{"id":33,"position":4,"label":"allerta","color":"#FF0000","severityId":83,"severityCd":"URGENT","minValue":1000,"includeMin":true,"maxValue":null,"includeMax":false},{"id":32,"position":3,"label":"alto","color":"#FF8500","severityId":84,"severityCd":"HIGH","minValue":100,"includeMin":true,"maxValue":1000,"includeMax":false}]}];
	
	$scope.loadSelectedKpi=function(kpi){
		// load kpi only if arent already loaded
		if(kpi.threshold!=undefined && kpi.threshold!=null){
			return;
		}
		sbiModule_restServices.promiseGet('1.0/kpi',kpi.id+"/"+kpi.version+"/loadKpi")
		.then(
				function(response){
					angular.extend($scope.currentKpiAlarm.kpi,response.data);
					},
				function(response){
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("Errore nel caricare la lista di kpi **"))
						}
					);
	}
	
	$scope.loadKpiList=function(){
		sbiModule_restServices.promiseGet('1.0/kpi','listKpi')
		.then(
				function(response){
					$scope.kpiList=response.data
					},
				function(response){
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("Errore nel caricare la lista di kpi **"))
						}
					);
	}
	$scope.loadKpiList();
	
	$scope.addAction=function(){  
		 
		$mdDialog.show({ 
		      controller: addActionDialogController, 
		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/alert/listeners/kpiListener/templates/addKpiActionTemplate.html',  
		      clickOutsideToClose:false,
		      preserveScope:true, 
		      locals:{
		    	  translate: sbiModule_translate,
		    	  kpi:$scope.currentKpiAlarm.kpi
		    	  }
		    })
		    .then(function(act) {
		    	$scope.currentKpiAlarm.action.push(act); 
		    }, function() { 
		    });
		 
	}
	
	function addActionDialogController($scope,translate,kpi,$mdDialog){
	 	$scope.translate=translate;
		$scope.kpi=kpi;
		$scope.currentAction={};
		$scope.actionType=[{name:"mail",label:translate.load("mail**")}];
		
		$scope.cancel = function() {
		    $mdDialog.cancel();
		  };
	  $scope.save = function() {
	    $mdDialog.hide($scope.currentAction);
	  };
		  
	}
	
}