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
    angular.module('OutputParametersModule', ['sbiModule'])
    	   .service('outputParametersService', ['resourceService', 'DocumentService', 'sbiModule_translate', 'sbiModule_messaging', function(resourceService,DocumentService,sbiModule_translate,sbiModule_messaging) {
    		   var outputResource = {};
    		   var documentService = DocumentService;
    		   var requiredPath = "2.0/documentdetails";
    		   outputResource.outputParametersForDeleting = [];

    		   outputResource.persistOutputParameters = function() {
    	        	for(var i = 0; i < DocumentService.document.outputParameters.length; i++) {
    	        		if(!DocumentService.document.outputParameters[i].id) {
    	        			delete DocumentService.document.outputParameters[i].$$hashKey;
    	        			var outputParametersPostBasePath = documentService.document.id + '/outputparameters';
    	        			
    	        			resourceService.post(requiredPath, outputParametersPostBasePath, DocumentService.document.outputParameters[i])
    	        			.then(
    	        					function(response) {
    	        			
	    	        					if (response.data.usedInCrossNavigations && response.data.usedInCrossNavigations == true) {
	    	        						var message = "Output parameter used in one or more cross navigations";
	    	    							sbiModule_messaging.showErrorMessage(message, 'Warning');
	    	        					}
    	        					}
    	        			);
    	        			
    	        		} else {
    	        			delete DocumentService.document.outputParameters[i].$$hashKey;
    	        			var outputParametersPutBasePath = documentService.document.id + "/outputparameters/" + DocumentService.document.outputParameters[i].id;
    	        			resourceService.put(requiredPath, outputParametersPutBasePath, DocumentService.document.outputParameters[i])
    	        			.then(
    	        					function(response) {
    	        			
	    	        					if (response.data.usedInCrossNavigations && response.data.usedInCrossNavigations == true) {
	    	        						var message = "Output parameter used in one or more cross navigations";
	    	    							sbiModule_messaging.showErrorMessage(message, 'Warning');
	    	        					}
    	        					}
    	        			);
    	        		}
    	        	}
    	        };
    	        
    	        var deleteOutputParameterById = function(outputParameter) {
    	        	 var basePath = documentService.document.id + "/" + 'outputparameters';
    	        	 var basePathWithId = basePath + "/" + outputParameter.id;
    	        	 resourceService.delete(requiredPath, basePathWithId).then(function(response) {
    	        		 sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.documentdetails.toast.deleted"), 'Success!');
    	        	 });
    	         };

    	         outputResource.deleteOutputParameters = function() {
    	        	 for(var i = 0; i < outputResource.outputParametersForDeleting.length; i++) {
    	        		 deleteOutputParameterById(outputResource.outputParametersForDeleting[i]);
    	        	 }
    	         };
    		   
    		   return outputResource;
    	   }]);

})();
