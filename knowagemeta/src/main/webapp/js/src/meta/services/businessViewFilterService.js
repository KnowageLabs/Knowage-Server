(function() {
	
	angular.module('metaManager').service('businessViewFilterService', ['sbiModule_restServices', 'sbiModule_config', 'sbiModule_translate',
		function(sbiModule_restServices, sbiModule_config, sbiModule_translate) {
			
			return{
				
				sqlExpression:"",
				
				getBusinessModelDrivers : function(businessModelId) {
					var reqPath = businessModelId + "/drivers";
					var endPath = "2.0/businessmodels";
					var bmDrivers = [];
					
					sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
					var promise = sbiModule_restServices.promiseGet(endPath, reqPath);
						promise.then(function(response) {
							bmDrivers = response.data;
						
						}, function(response) {
							
							sbiModule_restServices.errorHandler(response.data, sbiModule_translate.load("sbi.meta.model.business.filter.failureMessage"));
						});
					
						return promise;
				},
				
				areDriversFromExpressionValid : function() {
					
					var expressionDrivers = this.extractDriversFromExpression(this.sqlExpression);
					return this.isBusinessModelContainsDrivers(bmId, expressionDrivers);
					
				},
				
				
				contains : function(sourceArray, destinationArray) {
					return false;
				},
				
				isBusinessModelContainsDrivers: function(bmId, drivers) {
					
					
					var promise = this.getBusinessModelDrivers(bmId);
					promise.then(function(response) {
							businessModelDrivers = response.data;
							return this.contains(drivers, businessModelDrivers)
						})
					return promise;
				},
				
				extractDriversFromExpression : function(expression) {
//					var pattern = /\$p{(.+)}/g;
//					var result = expression.match(pattern);
//					var res = pattern.exec(expression);
//					result = res[1];
					var drivers = [];
					drivers.push("dragan");
					drivers.push("marko");
				}
				
			}	
	}]);
	
}());