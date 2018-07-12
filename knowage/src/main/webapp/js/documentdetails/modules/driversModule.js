(function() {
    angular.module('DriversModule',[])
    		.service('DriversService',['sbiModule_translate','resourceService',
    			function(sbiModule_translate,resourceService){

	    		  var driversResource = {};
	      		  var translate = sbiModule_translate;
	      		  var crudService = resourceService;
	      		  driversResource.changedDrivers = [];
	      		  driversResource.driverParuses = [];
	      		  driversResource.driversForDeleting = [];
	      		  driversResource.lovIdAndColumns = [];
	      		  driversResource.paruseColumns = {};
	      		  driversResource.driverRelatedObject;
	      		  driversResource.analyticalDrivers = [];

	      		  //*****Dependencies*****

	      		  driversResource.visualDependencies = "visualdependencies";
	      		  driversResource.dataDependenciesName = "datadependencies";
	      		  driversResource.selectedVisualCondition = {};
	      		  driversResource.selectedDataCondition = {};
	      		  driversResource.visusalDependencyObjects = [];
	      		  driversResource.dataDependencyObjects = [];
	      		  driversResource.changedVisualDependencies = [];
	      		  driversResource.changedDataDependencies = [];
	      		  driversResource.dataDependenciesForDeleting = [];
	      		  driversResource.visualDependenciesForDeleting = [];
	      		  driversResource.driverRelatedObject = {};
	    		  driversResource.driversOnObject = [];
	    		  driversResource.driversNum =0;
	      		driversResource.getParusesByAnaliticalDriverId = function (driverId){
	    			   var base = "2.0/analyticalDrivers";
	                   var path = driverId + "/modes";
	                   crudService.get(base,path).then(function(response){
	                		for(var i = 0; i < response.data.length; i++) {
	                			var existingDriverList = driversResource.driverParuses.filter(paruse => (paruse.useID == response.data[i].useID))
	                			if(existingDriverList.length != 0)
	                				continue;
	                			driversResource.driverParuses.push(response.data[i]);
	                		}
	                   });
	    		   }
	      		driversResource.getAllAnalyticalDrivers = function (){
	    			   var base = "2.0/analyticalDrivers";
	                   var path = "";
	                   crudService.get(base,path).then(function(response){
	                			driversResource.analyticalDrivers=response.data;
	                			 for(var i = 0;i< driversResource.analyticalDrivers.length;i++){
	             	      			driversResource.getParusesByAnaliticalDriverId(driversResource.analyticalDrivers[i].id);
	             	    		 }
	                   });
	    		   }

	      		driversResource.getDriverRelatedObject = function(basePath,endPath){
	      			crudService.get(basePath,endPath).then(function(response){
	      				driversResource.driverRelatedObject = response.data;
	      			});
	      		}

	      		driversResource.setDriverRelatedObject = function(driverRelatedObject){
	      				driversResource.driverRelatedObject = driverRelatedObject;
	      		}

	      		driversResource.getDriversOnRelatedObject = function(basePath,endPath){
	      			crudService.get(basePath,endPath).then(function(response){
	      				driversResource.driversOnObject = response.data;
	      				driversResource.driversNum =driversResource.driversOnObject.length > 1;
	      			});
	      		}

	      		driversResource.setDriversOnRelatedObject = function(driversOnRelatedObject){
	      			driversResource.driversOnObject = driversOnRelatedObject;
	      			driversResource.driversNum =driversResource.driversOnObject.length > 1;
	      		}

				driversResource.getAllAnalyticalDrivers();
	      	return driversResource;
    		}]);
})();