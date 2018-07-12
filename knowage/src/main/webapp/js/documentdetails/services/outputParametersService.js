(function () {
    angular.module('OutputParametersModule', [])
    	   .service('outputParametersService', ['$location','resourceService', function($location,resourceService) {
    		   var documentData = {}
    		   var crudService = resourceService;
    		   documentData.documentInfoObject =$location.search();
    		   documentData.requiredPath = "2.0/documents1";    		   
			   documentData.changedOutputParameters = [];			   
    		   documentData.outputParametersForDeleting = [];

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
