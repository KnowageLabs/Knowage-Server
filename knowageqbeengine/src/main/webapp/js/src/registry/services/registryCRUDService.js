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
    	   .factory('registryCRUDService', ['sbiModule_action_builder', function(sbiModule_action_builder) {

    		   var crud = {};
    		   crud.action = sbiModule_action_builder;
    		   crud.read = function(params){
    			   var loadRegistryAction = this.action.getActionBuilder('POST');
    			   loadRegistryAction.actionName = 'LOAD_REGISTRY_ACTION';
    			   loadRegistryAction.formParams = params;
    			   var promise = loadRegistryAction.executeAction();
    			   return promise;
    		   };

    		   crud.update = function(records) {
    			   //delete record.id;
    			   for (var i = 0; i < records.length; i++) {
    				   delete records[i].selected;

    				}
    			   var loadRegistryAction = this.action.getActionBuilder('POST');
    			   loadRegistryAction.actionName = 'UPDATE_RECORDS_ACTION';
    			   loadRegistryAction.formParams.records = records;
    			   var promise = loadRegistryAction.executeAction();
    			   return promise;
    		   };

    		   crud.delete = function(record) {
    			   var records = [];
    			   var loadRegistryAction = this.action.getActionBuilder('POST');
    			   loadRegistryAction.actionName = 'DELETE_RECORDS_ACTION';
    			   loadRegistryAction.formParams.records = records;
    			   loadRegistryAction.formParams.records.push(record);
    			   var promise = loadRegistryAction.executeAction();
    			   return promise;
    		   };

    		   return crud;
    	   }]);
})();