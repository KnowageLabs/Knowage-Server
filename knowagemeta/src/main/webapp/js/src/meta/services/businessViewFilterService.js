(function() {
	
	angular.module('metaManager').service('businessViewFilterService', ['sbiModule_restServices', 'sbiModule_config', 'sbiModule_translate', '$filter',
		function(sbiModule_restServices, sbiModule_config, sbiModule_translate, $filter) {
		
			var drivers = [];
			
			var getBusinessModelDrivers = function(businessModelId) {
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
			};
		
			getBusinessModelDrivers(bmId).then(function(response){
				drivers = response.data;
			})				 
		 
			return{
						
				sqlExpression:"",
				
				areDriversFromExpressionValid: function() {
					if(this.sqlExpression.length == 0)
						return true;
					var expressionDrivers = this.extractDriversFromExpression(this.sqlExpression);
					return this.contains(expressionDrivers);
					
				},
				
				contains: function(expressionValueArray) {
					
					for(i=0; i<expressionValueArray.length; i++) {
						var filterRes = $filter('filter')(drivers, {parameterUrlName: expressionValueArray[i]});
						if(filterRes.length == 0) {
							return false;
						}
					}
					
					return true;
				},
				
				extractDriversFromExpression: function(expression) {
					
					var drivers = [];
					var pattern = /\$P{(.*?)}/g;
					var result = (expression['structural.sqlFilter'].value).match(pattern);
					var res; 
					
					while(res != null || res == undefined) {
						res = pattern.exec(expression['structural.sqlFilter'].value);
						if(res == null)
							break;
						var r = res[1];
						
						drivers.push(r);
					}
					
					return drivers;
				}
				
			}	
	}]);
	
}());