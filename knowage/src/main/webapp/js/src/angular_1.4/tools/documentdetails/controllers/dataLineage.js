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
angular
        .module('DocumentDetails')
        .controller('DataLineageController',[ 'DocumentService', 'resourceService', '$location',  '$filter', 'sbiModule_restServices', 'sbiModule_messaging', 'sbiModule_translate',
        										function(DocumentService, resourceService, $location, $filter, sbiModule_restServices, sbiModule_messaging, sbiModule_translate){

        	   var self = this;
        	   var documentService = DocumentService;
         	   self.translate = sbiModule_translate;
         	   self.document = DocumentService.document;
         	   self.documentInfoObject = $location.search();
         	   var id = self.document.id;         	   
        	   self.sourceList = [];
        	   
        	   self.getDataSources = function() {
        		   sbiModule_restServices.promiseGet("2.0/metaSourceResource", "")
        		   .then(function(response) {
        			   self.sourceList = response.data;
        		   }, function(response) {
        			   sbiModule_messaging.showErrorMessage(response.data.errors[0].message, "Error");
        		   })
        	   };
        	   
        	   self.getDataSources();
        	   
        	   self.getTablesBySourceID = function(id){	
        			sbiModule_restServices.promiseGet("2.0/metaSourceResource/"+id+"/metatables", "")
        			.then(function(response) {
        				self.tablesList = response.data;
        				documentService.tablesList = response.data;
        				self.setChecked();        				
        			}, function(response) {
        				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');        				
        			});	
        		}        	   
        	   
        	   self.getTablesByDocumentID = function(id){	
        			sbiModule_restServices.promiseGet("2.0/metaDocumetRelationResource/document/"+id, "")
        			.then(function(response) {        				
        				self.savedTables = response.data;
        				documentService.savedTables = response.data;
        			}, function(response) {
        				sbiModule_messaging.showErrorMessage('error getting saved', 'Error');        				
        			});	
        		}
        	   
        	   self.getTablesByDocumentID(id);        	   
        	   
        	   self.setChecked = function() {
        		   for(var i = 0; i < self.tablesList.length; i++) {
        			   for(var j = 0; j < self.savedTables.length; j++) {
        				   if(self.tablesList[i].tableId == self.savedTables[j].tableId) {
        					   self.tablesList[i].wanted = true;
        				   }
        			   }        			   
        		   }        		   
        		   return self.tablesList;
        	   }
       }])
 
})();
