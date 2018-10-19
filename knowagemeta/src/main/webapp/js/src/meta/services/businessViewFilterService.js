(function() {
	
	angular.module('metaManager').service('businessViewFilterService', ['sbiModule_restServices', 'sbiModule_config', 'sbiModule_translate', '$q',
		function(sbiModule_restServices, sbiModule_config, sbiModule_translate, $q) {
			
			var objToReturn = {};
			
			objToReturn.bmDrivers = [];
		
			objToReturn.obj = {
				input: ""
			}
			
			objToReturn.getBusinessModelDrivers = function() {
				var reqPath = bmId + "/drivers";
				var endPath = "2.0/businessmodels";
				var deferred = $q.defer();
				
				sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
				sbiModule_restServices.promiseGet(endPath, reqPath)
					.then(function(response) {
						objToReturn.bmDrivers = response.data;
						deferred.resolve(objToReturn.bmDrivers);
						
					}, function(response) {
						deferred.reject(response);
						//sbiModule_restServices.errorHandler(response.data, sbiModule_translate.load("sbi.meta.model.business.filter.failureMessage"));
					});
				
					return deferred.promise;
			}
			
			objToReturn.checkIfDriversExist = function(sqlExpression) {
				
				var pattern = /\$p{(.+)}/g;
				var result = sqlExpression.match(pattern);
				var res = pattern.exec(sqlExpression);
				result = res[1];
				
				var listDrivers = bmDrivers;
				
			}
			
			return objToReturn;
	}]);
	
}());