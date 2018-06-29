(function () {
    angular.module('DocumentDetails')
    	   .service('DocumentService', ['$location','$mdDialog','sbiModule_translate','resourceService', function($location,$mdDialog,sbiModule_translate,resourceService) {
    		   var documentData = {}
    		   var translate = sbiModule_translate;
    		   var crudService = resourceService;
    		   documentData.documentInfoObject =$location.search();
    		   documentData.documentId = documentData.documentInfoObject.OBJECT_ID;
    		   documentData.templates = "templates";
    		   documentData.requiredPath = "2.0/documents1";
    		   documentData.requiredPathForRelations = "2.0/metaDocumetRelationResource";
    		   documentData.template= documentAndInfo.template;
    		   documentData.visualDependencies = "visualdependencies";
    		   documentData.dataDependenciesName = "datadependencies";
    		   documentData.selectedVisualCondition = {};
    		   documentData.selectedDataCondition = {};
    		   documentData.analyticalDrivers = documentAndInfo.analyticalDrivers;
    		   documentData.visusalDependencyObjects = [];
    		   documentData.dataDependencyObjects = [];
    		   documentData.changedDrivers = [];
			   documentData.changedOutputParameters = [];
			   documentData.listOfTemplates = [];
    		   documentData.changedTemplates = [];
    		   documentData.changedTemplate = {};
    		   documentData.file = {};
    		   documentData.changedVisualDependencies = [];
    		   documentData.changedDataDependencies = [];
    		   documentData.driverParuses = [];
    		   documentData.driversForDeleting = [];
    		   documentData.outputParametersForDeleting = [];
    		   documentData.templatesForDeleting = [];
    		   documentData.dataDependenciesForDeleting = [];
    		   documentData.visualDependenciesForDeleting = [];
    		   documentData.lovIdAndColumns = [];
    		   documentData.previewFile = {};
    		   documentData.documentImage;
    		   documentData.paruseColumns = {};
			   documentData.engines = documentAndInfo.engines;
    		   documentData.resourcePath= documentAndInfo.resourcePath;

    		   if( documentAndInfo.document){
    			   documentData.document = documentAndInfo.document;
    			   documentData.drivers = documentAndInfo.drivers
    		   } else {
    			   documentData.document = {};
    			   documentData.drivers = [];
    		   }
    		   documentData.driversNum =documentData.drivers.length > 1;
    		   documentData.folders = [];
    		   documentData.hasTemplate = function(){
    			   if(document.template)
    				   return false;
    		   }
    		   documentData.getAllParuses = function (driverId){
    			   var base = "2.0/analyticalDrivers";
                   var path = driverId + "/modes";
                   crudService.get(base,path).then(function(response){
                		for(var i = 0; i < response.data.length; i++) {
                			var obj = documentData.driverParuses.filter(paruse => (paruse.useID == response.data[i].useID))
                			if(obj.length != 0)
                				continue;
                			documentData.driverParuses.push(response.data[i]);
                		}
                   });
    		   }
    		   documentData.getAllAnalyticalDrivers = function (){
    			   var base = "2.0/analyticalDrivers";
                   var path = "";
                   crudService.get(base,path).then(function(response){
                		for(var i = 0; i < response.data.length; i++) {
                			documentData.analyticalDrivers=response.data;
                		}
                   });
    		   }

    		   for(var i = 0;i< documentData.analyticalDrivers.length;i++){
    			   documentData.getAllParuses(documentData.analyticalDrivers[i].id);
    		   }
    		   documentData.confirmDelete = function(index,name) {
        		    var confirm = $mdDialog.confirm()
        		          .title(translate.load("sbi.documentdetails.toast.confirm.title"))
        		          .content(translate.load("sbi.documentdetails.toast.confirm.content"))
        		          .ariaLabel("confirm_delete")
        		          .ok(translate.load("sbi.general.continue"))
        		          .cancel(translate.load("sbi.general.cancel"));
        		    $mdDialog.show(confirm).then(function() {
        		    	switch(name){
        		    		case "driver":
		        		    	 documentAndInfo.drivers.splice(index, 1);
		        		    	 documentData.driversForDeleting.push(documentAndInfo.drivers[index]);
		        		    	 break;
        		    		case "visual":
        		    			 for(var i = 0; i< self.visibilityConditions.length;i++){
        		            		 if(i == index)
        		            			 documentData.visualDependenciesForDeleting.push(documentData.visusalDependencyObjects[index]);
        		            		 }
        		    			documentData.visusalDependencyObjects.splice(index, 1);
        		    		 break;
        		    		case "data":
        		    			for(var i = 0; i< self.dataConditions.length;i++){
        		            		 if(i == index)
        		            			 documentData.dataDependenciesForDeleting.push(documentData.dataDependencyObjects[index]);
        		            	 }
        		    		 documentData.dataDependencyObjects.splice(index, 1);
           		    		 break;
        		    	}
        		    }, function() {

        		    });
        	};
    		   return documentData;
    	   }]);

})();
