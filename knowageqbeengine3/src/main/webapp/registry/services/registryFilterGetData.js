/*
 * Knowage, Open Source Business Intelligence suite
	Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

	Knowage is free software: you can redistribute it and/or modify
	it under the terms of the GNU Affero General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	 (at your option) any later version.

	Knowage is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Affero General Public License for more details.

	You should have received a copy of the GNU Affero General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function () {
    angular.module('RegistryDocument')
    	   .factory('regFilterGetData', ['sbiModule_action_builder', 'registryConfigService', '$filter', '$q',
    			   function(sbiModule_action_builder, registryConfigService, $filter, $q) {
    		   
    		   var registryConfiguration = registryConfigService.getRegistryConfig();

    		   var serviceObj = {};
    		   serviceObj.action = sbiModule_action_builder;
    		   serviceObj.filterOptions = {};
    		       		   
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
    			   loadRegistryAction.formParams.QUERY_TYPE = 'standard';    			   
    			   loadRegistryAction.formParams.ORDER_ENTITY = ENTITY_ID;
    			   loadRegistryAction.formParams.ORDER_TYPE = 'asc';
    			   loadRegistryAction.formParams.QUERY_ROOT_ENTITY = true;
    			   loadRegistryAction.formParams.query = '';
    			   
    			   var promise = loadRegistryAction.executeAction();

    			   return promise;
    		   };
    		       		   
    		   
    		   return serviceObj;
    	   }]);
})();