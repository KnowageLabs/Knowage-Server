angular.module('scorecardManager').controller('scorecardDefinitionController', [ '$scope','$mdDialog', 'sbiModule_translate' ,'sbiModule_restServices',scorecardDefinitionControllerFunction ]);

function scorecardDefinitionControllerFunction($scope,$mdDialog, sbiModule_translate,sbiModule_restServices){
	$scope.addPerspective=function(){ 
		var firstFreeIndex = 0;
		var indexArray = []
		
		
		$scope.newPerspective = {
			'name':'New Perspective',
			'status':'GRAY',
			'criterion':{
	            "valueId": 214,
	            "valueCd": "MAJORITY",
	            "valueName": "sbidomains.nm.majoritycrit",
	            "valueDescription": "it.eng.spagobi.kpi.statusCriterion.Majority",
	            "domainCode": "KPI_SCORECARD_CRITE",
	            "domainName": "KPI SCORECARD CRITERIA",
	            "translatedValueName": "Policy \"Majority\"",
	            "translatedValueDescription": "it.eng.spagobi.kpi.statusCriterion.Majority"
	          },
			'options':{"criterionPriority":[]},
			'targets':[],
			'groupedKpis':[]
		};
		$scope.currentScorecard.perspectives.push($scope.newPerspective);

	};
	
	
	 
	
	$scope.deletePerspective = function(target, $index){
		var confirm = $mdDialog.confirm()
	    .title(sbiModule_translate.load("sbi.kpi.delete.progress"))
	    .content(sbiModule_translate.load("sbi.layer.delete.progress.message.delete"))
	    .ariaLabel('cancel perspective') 
	    .ok(sbiModule_translate.load("sbi.general.yes"))
	    .cancel(sbiModule_translate.load("sbi.general.No"));
	      $mdDialog.show(confirm).then(function() {
	    	  $scope.currentScorecard.perspectives.splice($index,1);
	      });
	}
	
}