(function () {
    angular.module('TemplateModule', [])
    	   .service('templateService', ['$location','resourceService', function($location,resourceService) {
    		   var documentData = {};    		   
    		   var crudService = resourceService;
    		   documentData.documentInfoObject =$location.search();
    		   documentData.templates = "templates";
    		   documentData.requiredPath = "2.0/documents1";    		   
    		   documentData.template= documentAndInfo.template;    		   
			   documentData.listOfTemplates = [];
    		   documentData.changedTemplates = [];
    		   documentData.changedTemplate = {};
    		   documentData.file = {};    		  
    		   documentData.templatesForDeleting = [];

    		   if( documentAndInfo.document){
    			   documentData.document = documentAndInfo.document;
    			   documentData.drivers = documentAndInfo.drivers
    		   } else {
    			   documentData.document = {};
    			   documentData.drivers = [];
    		   }
    		   
    		   return documentData;
    	   }]);

})();
