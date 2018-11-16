(function() {
	
	angular.module('metaManager')
	.controller('businessModelSqlFilterController', ['$scope', 'sbiModule_translate', 'sbiModule_restServices', 'businessViewFilterService', 
		function($scope, sbiModule_translate, sbiModule_restServices, businessViewFilterService){
		
			$scope.translate = sbiModule_translate;
			$scope.BWFilterService = businessViewFilterService;
		
			for(var i=0; i<$scope.selectedBusinessModel.properties.length; i++) {
				if($scope.selectedBusinessModel.properties[i]['structural.sqlFilter']) {
					$scope.BWFilterService.sqlExpression = $scope.selectedBusinessModel.properties[i];
				}
			}
		
		}
	
])})();