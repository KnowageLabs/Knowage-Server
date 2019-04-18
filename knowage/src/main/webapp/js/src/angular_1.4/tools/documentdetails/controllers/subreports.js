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
        .controller('SubreportsController',[ 'DocumentService', 'resourceService', '$location', 'sbiModule_restServices', 'sbiModule_messaging', 'sbiModule_translate',
        										function(DocumentService, resourceService, $location, sbiModule_restServices, sbiModule_messaging, sbiModule_translate){

        	   var self = this;
        	   var documentService = DocumentService;
        	   self.documentService = DocumentService;
         	   self.translate = sbiModule_translate;
         	   self.document = DocumentService.document;
         	   self.documentInfoObject = $location.search();
         	   var id = self.document.id;
         	   var documentBasePath = "";
        	   var resourceName = DocumentService.requiredPath; 
        	   var basePath = id + "/" + 'subreports';
         	   self.listOfSubreports = [];
         	   self.showSelected = true;         	 
         	   
              self.getSubreportsForMasterDocument = function(id) {
            	  resourceService.get(resourceName, basePath)
            	  .then(function(response) {
            		  self.documentService.documentsList = response.data;            		  
            		  documentService.savedSubreports = response.data;
            		  console.log(response);
            	  }, function(response) {
      					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');        				
      			  });
              }
              
              self.getSubreportsForMasterDocument(id);
              
              
       }])
          
})();
