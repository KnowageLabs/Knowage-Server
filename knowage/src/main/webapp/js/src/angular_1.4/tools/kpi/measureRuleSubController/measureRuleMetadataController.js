angular.module('measureRuleManager').controller('measureRuleMetadataController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices','sbiModule_messaging','$timeout',measureRuleMetadataControllerFunction ]);

function measureRuleMetadataControllerFunction($scope,sbiModule_translate,sbiModule_restServices,sbiModule_messaging,$timeout){
	$scope.hierarchicalLevelList=[];
	$scope.AttributeCategoryList=[];
	$scope.tmpSearchterm={}; 
	
	$scope.searchTextChange=function(mtdValue,searchTerm){
		if((mtdValue.category==undefined || mtdValue.category=="") && searchTerm!=""){
			$scope.tmpSearchterm[mtdValue.id]=searchTerm; 
			$timeout(function(){
				if($scope.tmpSearchterm[mtdValue.id]==searchTerm){
					mtdValue.category={valueCd:angular.uppercase(searchTerm)}; 
				}
			},500)
		}
	}
	
	sbiModule_restServices.promiseGet("2.0/domains","listByCode/KPI_RULEOUTPUT_TYPE")
	.then(function(response){ 
		angular.copy(response.data,$scope.tipologiesType); 
	},function(response){
		 $scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.generic.error")+" domains->KPI_RULEOUTPUT_TYPE"); 
	});
	
	sbiModule_restServices.promiseGet("2.0/domains","listByCode/TEMPORAL_LEVEL")
	.then(function(response){ 
		angular.copy(response.data,$scope.hierarchicalLevelList);
	},function(response){
		 $scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.generic.error")+" domains->TEMPORAL_LEVEL"); 
	});
	
	sbiModule_restServices.promiseGet("2.0/domains","listByCode/KPI_MEASURE_CATEGORY")
	.then(function(response){ 
		angular.copy(response.data,$scope.AttributeCategoryList);
	},function(response){
		 $scope.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.generic.error")+" domains->KPI_MEASURE_CATEGORY"); 
	});
	
	$scope.querySearchCategory=function(query){
		  var results = query ? $scope.AttributeCategoryList.filter( createFilterFor(query) ) : [];
		  results.push({valueCd:angular.uppercase(query)})
	      return results;
	}
	
	 function createFilterFor(query) {
	      var lowercaseQuery = angular.lowercase(query);
	      return function filterFn(state) {
	        return (angular.lowercase(state.valueCd).indexOf(lowercaseQuery) === 0);
	      };
	    }
	
	
}