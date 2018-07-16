(function () {
    angular.module('DocumentDetails')
    	   .service('DocumentService', ['$location','$mdDialog','sbiModule_translate','resourceService','DriversService', function($location,$mdDialog,sbiModule_translate,resourceService,DriversService) {
    		   var documentData = {}
    		   var translate = sbiModule_translate;
    		   var crudService = resourceService;
    		   documentData.documentInfoObject =$location.search();
    		   documentData.documentId = documentData.documentInfoObject.OBJECT_ID;
    		   documentData.templates = "templates";
    		   documentData.requiredPath = "2.0/documents1";
    		   documentData.template= documentAndInfo.template;
    		   
//    		   documentData.visualDependencies = "visualdependencies";
//    		   documentData.dataDependenciesName = "datadependencies";
//    		   documentData.selectedVisualCondition = {};
//    		   documentData.selectedDataCondition = {};
//    		   documentData.analyticalDrivers = documentAndInfo.analyticalDrivers;
//    		   documentData.visusalDependencyObjects = [];
//    		   documentData.dataDependencyObjects = [];
//    		   documentData.changedDrivers = [];
//    		   documentData.changedVisualDependencies = [];
//    		   documentData.changedDataDependencies = [];
//    		   documentData.driverParuses = [];
//    		   documentData.driversForDeleting = [];
//    		   documentData.dataDependenciesForDeleting = [];
//    		   documentData.visualDependenciesForDeleting = [];
//    		   documentData.lovIdAndColumns = [];
    		   documentData.previewFile = {};
    		   documentData.documentImage;
//    		   documentData.paruseColumns = {};
			   documentData.engines = documentAndInfo.engines;
    		   documentData.resourcePath= documentAndInfo.resourcePath;
    		   documentData.drivers = [];
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
//    		   documentData.getAllParuses = function (driverId){
//    			   var base = "2.0/analyticalDrivers";
//                   var path = driverId + "/modes";
//                   crudService.get(base,path).then(function(response){
//                		for(var i = 0; i < response.data.length; i++) {
//                			var obj = documentData.driverParuses.filter(paruse => (paruse.useID == response.data[i].useID))
//                			if(obj.length != 0)
//                				continue;
//                			documentData.driverParuses.push(response.data[i]);
//                		}
//                   });
//    		   }
//    		   documentData.getAllAnalyticalDrivers = function (){
//    			   var base = "2.0/analyticalDrivers";
//                   var path = "";
//                   crudService.get(base,path).then(function(response){
//                		for(var i = 0; i < response.data.length; i++) {
//                			documentData.analyticalDrivers=response.data;
//                		}
//                   });
//    		   }
//
//    		   for(var i = 0;i< documentData.analyticalDrivers.length;i++){
//    			   documentData.getAllParuses(documentData.analyticalDrivers[i].id);
//    		   }
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
        		    			 for(var i = 0; i< documentData.visusalDependencyObjects.length;i++){
        		            		 if(i == index)
        		            			 DriversService.visualDependenciesForDeleting.push(DriversService.visusalDependencyObjects[index]);
        		            		 }
        		    			 DriversService.visusalDependencyObjects.splice(index, 1);
        		    		 break;
        		    		case "data":
        		    			for(var i = 0; i< DriversService.dataDependencyObjects.length;i++){
        		            		 if(i == index)
        		            			 documentData.dataDependenciesForDeleting.push(DriversService.dataDependencyObjects[index]);
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
