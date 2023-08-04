(function () {
    angular.module('DocumentDetails')
    	   .service('DocumentService', ['$location','$mdDialog','sbiModule_translate','resourceService','DriversService', function($location,$mdDialog,sbiModule_translate,resourceService,DriversService) {
    		   var documentData = {}
    		   var translate = sbiModule_translate;
    		   var crudService = resourceService;
    		   documentData.documentInfoObject =$location.search();
    		   documentData.documentId = documentData.documentInfoObject.OBJECT_ID;
    		   documentData.templates = "templates";
    		   documentData.requiredPath = "2.0/documentdetails";
    		   documentData.drivers = [];
    		   documentData.previewFile = {};
    		   documentData.documentImage;


    		   if(documentAndInfo){
    		   documentData.template= documentAndInfo.template;
			   documentData.engines = documentAndInfo.engines;
    		   documentData.resourcePath= documentAndInfo.resourcePath;
    		   if( documentAndInfo.document){
    			   documentData.document = documentAndInfo.document;
    			   documentData.drivers = documentAndInfo.drivers
    		   } else {
    			   documentData.document = {};
    			   documentData.drivers = [];
    		   }
    		   }
    		   documentData.driversNum =documentData.drivers.length > 1;
    		   documentData.folders = [];
    		   documentData.hasTemplate = function(){
    			   if(document.template)
    				   return false;
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

        		    			DriversService.driversForDeleting.push(DriversService.driversOnObject[index]);
        		    			DriversService.driversOnObject.splice(index, 1);
		        		    	 break;
        		    		case "visual":
        		    			 for(var i = 0; i< DriversService.visusalDependencyObjects.length;i++){
        		            		 if(i == index)
        		            			 DriversService.visualDependenciesForDeleting.push(DriversService.visusalDependencyObjects[index]);
        		            		 }
        		    			 DriversService.visusalDependencyObjects.splice(index, 1);
        		    		 break;
        		    		case "data":
        		    			for(var i = 0; i< DriversService.dataDependencyObjects.length;i++){
        		            		 if(i == index)
        		            			 DriversService.dataDependenciesForDeleting.push(DriversService.dataDependencyObjects[index]);
        		            	 }
        		    			DriversService.dataDependencyObjects.splice(index, 1);
           		    		 break;
        		    	}
        		    }, function() {

        		    });
        	};
    		   return documentData;
    	   }]);

})();
