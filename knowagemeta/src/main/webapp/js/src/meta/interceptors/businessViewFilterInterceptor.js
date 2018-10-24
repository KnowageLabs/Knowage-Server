(function() {

	angular.module('metaManager')
		.factory('businessViewFilterInterceptor', ['$q', '$injector', 'sbiModule_translate', function($q, $injector, sbiModule_translate) {
			
			var containsUrl = function(url, urlPart) {
				return url.indexOf(urlPart) != -1	
			};
			
			var isCheckRelationshipsService = function(url) {
				var myUrl = "1.0/metaWeb/checkRelationships";
				return containsUrl(url, myUrl);
			};
			
			var shouldCancelRequest = function(config) {
				var BVFilterService = $injector.get('businessViewFilterService');
				return isCheckRelationshipsService(config.url) && !BVFilterService.areDriversFromExpressionValid();
			};
			
			var createErrorResponseObject = function(errorMessage) {
				var response = {};
				response.data = errorMessage;
				return response;
			};
			
			return {
				request: function(config) {
					if(shouldCancelRequest(config)) {
						return $q.reject(createErrorResponseObject(sbiModule_translate.load("sbi.meta.model.business.filter.errorMessage")));
					}
					
					return config; 
				}
			}
						  
		}]).config(function($httpProvider) {  
			$httpProvider.interceptors.push('businessViewFilterInterceptor');
		})
	
})();
