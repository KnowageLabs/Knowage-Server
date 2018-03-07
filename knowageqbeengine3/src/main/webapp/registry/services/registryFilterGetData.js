(function () {
    angular.module('BlankApp')
    	   .factory('regFilterGetData', ['sbiModule_action_builder', 'registryConfigService', '$filter', '$q',
    			   function(sbiModule_action_builder, registryConfigService, $filter, $q) {
    		   
    		   var registryConfiguration = registryConfigService.getRegistryConfig();

    		   var serviceObj = {};
    		   serviceObj.action = sbiModule_action_builder;
    		   serviceObj.filterOptions = {};
    		   serviceObj.options = [];
    		   
    		   
    		   serviceObj.getData = function(filterField){    			       			   
    			   var deferred = $q.defer();
    			   
    			   if(this.filterOptions[filterField]) {    				    			   		 
     				  deferred.resolve(this.filterOptions[filterField]);    				   	       			 
     			   } else {
     				   var newPromise = getFiltersValues(filterField);   			       			   
     				   newPromise.then(function(result){
     					  serviceObj.filterOptions[filterField] = result.data.rows;
           				  deferred.resolve(serviceObj.filterOptions[filterField]);
          			   });  
     			   }		      			   
    			    return deferred.promise;
			   };
    		   
    		   
    		   var getColumn = function(filterField) {
    			   return $filter('filter')(registryConfiguration.columns,{field:filterField}, true)[0];
    		   };
    		   
    		   
    		   var createEntityId = function(entity, filterField) {
    			   var column = getColumn(filterField);
    			   var SubEntity = column.subEntity;
    			   var foreignKey = column.foreignKey;
    			   return entity + '::' + SubEntity + '(' + foreignKey + ')' + ':' + filterField;
    		   };
    		   
    		   
    		   var getFiltersValues = function(filterField) {
    			   var entity = registryConfiguration.entity;
    			   var ENTITY_ID = createEntityId(entity, filterField);
    			   var loadRegistryAction = serviceObj.action.getActionBuilder('POST');
    			   loadRegistryAction.actionName = 'GET_FILTER_VALUES_ACTION';
    			   loadRegistryAction.formParams.ENTITY_ID = ENTITY_ID;
    			   loadRegistryAction.formParams.ORDER_ENTITY = ENTITY_ID;
    			   
    			   var promise = loadRegistryAction.executeAction();

    			   return promise;
    		   };
    		       		   
    		   
    		   return serviceObj;
    	   }]);
})();