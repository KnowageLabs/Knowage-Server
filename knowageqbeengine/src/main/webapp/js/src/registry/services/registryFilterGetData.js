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

			   serviceObj.getDependeceOptions = function(column, row) {
				   var columnField = column.field;
    			   var DEPENDENCES = createDependences(registryConfiguration.entity, column, row);
    			   var getFilterValuesAction = getBasicParamsForAction(columnField);
    			   if(DEPENDENCES!=''){
        			   getFilterValuesAction.formParams.DEPENDENCES = DEPENDENCES;
    			   }
    			   var promise = getFilterValuesAction.executeAction();
    			   return promise;
			   };

    		   var getColumn = function(filterField) {
    			   return $filter('filter')(registryConfiguration.columns,{field:filterField}, true)[0];
    		   };

    		   var getColumnConfiguration = function (field) {
    				for (var i = 0; i < registryConfiguration.columns.length; i++) {
    					if (registryConfiguration.columns[i].field == field) {
    						return registryConfiguration.columns[i];
    					}
    				}
    				return null;
    			}

    		   var createEntityId = function(entity, filterField) {
    			   var column = getColumn(filterField);
    			   var index = entity.indexOf('::');
    			   var temp = entity;
    			   if (index != -1) {
    					temp = entity.substring(0 , index);
    				}
    			   var subEntity = column.subEntity;
    			   if (subEntity) {
    			   	   var foreignKey = column.foreignKey;
    			   	   return temp + '::' + subEntity + '(' + foreignKey + ')' + ':' + filterField;
					} else {
						return temp + ':' + filterField;
					}
    		   };

    		   var createOrderEntity = function(entity, filterField) {
    			   var temp = entity;
    			   var entityId = createEntityId(entity, filterField);
    			   var orderBy = entityId;
    			   var index = entity.indexOf('::');
    			   if (index != -1) {
    					temp = entity.substring(0 , index);
    				}
    			   var column = getColumn(filterField);
    			   if(column.orderBy != null && column.orderBy != undefined){
    					if (column.subEntity) {
    						orderBy =   temp + "::" + column.subEntity + "(" + column.foreignKey + ")" + ":" + column.orderBy;
    							}
    					else{
    						orderBy =  temp + ':' + column.orderBy;
    					}
    				}
    			   return orderBy;
    		   };

    		   var getFiltersValues = function(filterField) {
    			   var loadRegistryAction = getBasicParamsForAction(filterField);
    			   var promise = loadRegistryAction.executeAction();
    			   return promise;
    		   };

    		   var createDependences = function(entity, columnObject, row) {
				   var temp = registryConfiguration.entity;
					var index = registryConfiguration.entity.indexOf('::');
					if (index != -1) {
						temp = registryConfiguration.entity.substring(0 , index);
					}
				   lstDependsFromRef = [];
				   var dependences = ""
					if (columnObject.dependsFrom){
						var lstDependsFrom = columnObject.dependsFrom.split(",");
						var lstDependsFromEntity = (columnObject.dependsFromEntity)?columnObject.dependsFromEntity.split(","):[];
						for (var i=0; i<lstDependsFrom.length; i++){
							var name = (lstDependsFromEntity && lstDependsFromEntity[i] != null && lstDependsFromEntity[i] !== '') ? lstDependsFromEntity[i] : temp;
							var columnDepends = getColumn(lstDependsFrom[i].trim());
							if (columnDepends && columnDepends.subEntity) {
								name +=  "::" + columnDepends.subEntity + "(" + columnDepends.foreignKey + ")" + ":" + lstDependsFrom[i].trim();
							}
							else{
								 name += ':' + lstDependsFrom[i].trim();
							}
							var tmpFieldRef = {};
							tmpFieldRef.field = lstDependsFrom[i].trim();
							tmpFieldRef.entity = name;
							tmpFieldRef.title = columnObject.title;
							lstDependsFromRef.push(tmpFieldRef);
						}

						for (var i=0; i<lstDependsFromRef.length; i++){
							var name = '';
							var comma = (i < lstDependsFrom.length-1)?',':'';
							var filterValue;
							for (var j=0; j<lstDependsFromRef.length; j++){
								var tmpRef = lstDependsFromRef[j];
								if (tmpRef.field == lstDependsFrom[i].trim()){
									name = tmpRef.entity;
									for(var k=0; k < registryConfiguration.filters.length; k++){
										if (registryConfiguration.filters[k].name == tmpRef.field){
											filterValue = registryConfiguration.filters[k].value;
											break;
										}
									}
									break;
								}
							}
							dependences += (row[lstDependsFrom[i].trim()] != "")? name + '=' + row[lstDependsFrom[i].trim()] + comma : "";
						}
					}
					return dependences;
			   };

    		   var getBasicParamsForAction = function (filterField){
    			   var entity = registryConfiguration.entity;
    			   var ENTITY_ID = createEntityId(entity, filterField);
    			   var ORDER_ENTITY = createOrderEntity(entity, filterField);
    			   var loadRegistryAction = serviceObj.action.getActionBuilder('POST');
    			   loadRegistryAction.actionName = 'GET_FILTER_VALUES_ACTION';
    			   loadRegistryAction.formParams.ENTITY_ID = ENTITY_ID;
    			   loadRegistryAction.formParams.QUERY_TYPE = 'standard';
    			   loadRegistryAction.formParams.ORDER_ENTITY = ORDER_ENTITY;
    			   loadRegistryAction.formParams.ORDER_TYPE = 'asc';
    			   loadRegistryAction.formParams.QUERY_ROOT_ENTITY = true;
    			   loadRegistryAction.formParams.query = '';
    			   return loadRegistryAction;
    		   }

    		   return serviceObj;
    	   }]);
})();