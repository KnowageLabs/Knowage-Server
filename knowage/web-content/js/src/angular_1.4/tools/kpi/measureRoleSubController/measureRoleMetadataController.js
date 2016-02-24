angular.module('measureRoleManager').controller('measureRoleMetadataController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',measureRoleMetadataControllerFunction ]);

function measureRoleMetadataControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.hierarchicalLevelList=[];
	$scope.AttributeCategoryList=[];
	
	$scope.aliasExtist=function(aliasName){
	return $scope.aliasList.indexOf(aliasName)!=-1;
	}

	$scope.tipologiesType=[
                       {label:sbiModule_translate.load("sbi.ds.attribute"),value:"Attribute"},
                       {label:sbiModule_translate.load("sbi.ds.measure"),value:"Measure"},
                       {label:sbiModule_translate.load("sbi.kpi.temporalAttribute"),value:"TemporalAttribute"},
                       ];

	sbiModule_restServices.promiseGet("2.0/domains","listByCode/TEMPORAL_LEVEL")
	.then(function(response){ 
		angular.copy(response.data,$scope.hierarchicalLevelList);
	},function(response){
		console.log("errore")
	});
	
	sbiModule_restServices.promiseGet("2.0/domains","listByCode/KPI_MEASURE_CATEGORY")
	.then(function(response){ 
		angular.copy(response.data,$scope.AttributeCategoryList);
	},function(response){
		console.log("errore")
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