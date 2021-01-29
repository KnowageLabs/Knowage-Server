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
    angular.module('SubreportsModule', ['sbiModule'])
    	   .service('subreportsService', ['$filter','DocumentService','sbiModule_translate','sbiModule_restServices','sbiModule_messaging', function($filter,DocumentService,sbiModule_translate,sbiModule_restServices,sbiModule_messaging) {
    		   var subreportsResource = {};
    		   var documentService = DocumentService;
    		   var requiredPath = "2.0/documentdetails";
    		   
    		   subreportsResource.persistSubreports = function() {
    	        	var checkedDocuments = $filter('filter')(documentService.documentsList, {wanted: true});
    	        	var savedSubreports = documentService.savedSubreports;
    	        	var subreportBasePath = documentService.document.id + '/subreports';
    	        	if(checkedDocuments) {
    	        		for(var i = 0; i < checkedDocuments.length; i++) {
    	            		if(!containsSubreport(checkedDocuments[i], savedSubreports)) {
    	            			var subreport = angular.copy(checkedDocuments[i]);
    	        				delete subreport.wanted;
    	        				prepareDocumentForPersisting(subreport);
    	        				sbiModule_restServices.promisePost(requiredPath, subreportBasePath, subreport);
    	            		}
    	            	}
    	        	}
    	        };

    	        var containsSubreport = function(obj, list) {
    	        	for(var i = 0; i < list.length; i++) {
    	        		if(list[i].sub_rpt_id === obj.id) {
    	        			return true;
    	        		}
    	        	}
    	        	return false;
    	        }
    	        
    	        var prepareDocumentForPersisting = function(document){
    	        	delete document.dataSetLabel;
    	    		delete document.creationDate;
    	    		document.refreshSeconds = parseInt(document.refreshSeconds);
    	        };
    	        
    	        subreportsResource.deleteSubreports = function() {
    	        	var subreportsForRemoving = $filter('filter')(documentService.documentsList, {wanted: false});
    	        	if(subreportsForRemoving) {
    	        		for(var i = 0; i < subreportsForRemoving.length; i++) {
    	            		deleteSubreportById(subreportsForRemoving[i]);
    	            	}
    	        	}
    	        }

    	        var deleteSubreportById = function(subreport) {
    	        	var subreportBasePath = documentService.document.id + '/subreports/' + subreport.id;
    	        	sbiModule_restServices.promiseDelete(requiredPath, subreportBasePath).then(function(response) {
    	        		sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.documentdetails.toast.deleted"), 'Success!');
    	        	});
    	        };

    		   
    		   return subreportsResource;
    	   }]);

})();
