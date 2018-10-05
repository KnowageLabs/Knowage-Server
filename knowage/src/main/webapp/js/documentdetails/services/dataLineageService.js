/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function () {
    angular.module('DataLineageModule', ['sbiModule'])
    	   .service('dataLineageService', ['$filter','DocumentService','sbiModule_translate','sbiModule_restServices','sbiModule_messaging', function($filter,DocumentService,sbiModule_translate,sbiModule_restServices,sbiModule_messaging) {
    		   var dataLineageResource = {};
    		   var documentService = DocumentService;
    		   dataLineageResource.requiredPathForRelations = "2.0/metaDocumetRelationResource"; 
   		  
    		   dataLineageResource.persistTables = function() {
    	        	var checkedTables = $filter('filter')(documentService.tablesList, {wanted: true});
    	        	var savedTables = documentService.savedTables;
    	        	if(checkedTables) {
    	        		for(var i = 0; i < checkedTables.length; i++) {
    	            		if(!containsTable(checkedTables[i], savedTables)) {
    	            			var table = angular.copy(checkedTables[i]);
    	        				delete table.wanted;
    	        				sbiModule_restServices.promisePost(dataLineageResource.requiredPathForRelations + "/" + documentService.document.id, "", table);
    	            		}
    	            	}
    	        	}
    	        };
    	        
    	        var containsTable = function(obj, list) {
    	        	for(var i = 0; i < list.length; i++) {
    	        		if(list[i].tableId === obj.tableId) {
    	        			return true;
    	        		}
    	        	}
    	        	return false;
    	        }
    	        
    	        dataLineageResource.deleteTables = function() {
    	        	var tablesForRemoving = $filter('filter')(documentService.tablesList, {wanted: false});
    	        	if(tablesForRemoving) {
    	        		for(var i = 0; i < tablesForRemoving.length; i++) {
    	            		deleteTableById(tablesForRemoving[i]);
    	            	}
    	        	}
    	        }

    	        var deleteTableById = function(table) {
    	        	var tableBasePath = documentService.document.id +  "/" + table.tableId;
    	        	sbiModule_restServices.promiseDelete(dataLineageResource.requiredPathForRelations, tableBasePath).then(function(response) {
    	        		sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.documentdetails.toast.deleted"), 'Success!');
    	        	});
    	        };
    		   
    		   return dataLineageResource;
    	   }]);

})();
