(function() {
	angular.module('DriversModule',['ResourceModule', 'ngMessages', 'angularXRegExp'])
    		.service('DriversService',['sbiModule_translate','resourceService','sbiModule_messaging',
    			function(sbiModule_translate,resourceService,sbiModule_messaging){

	    		  var driversResource = {};
	      		  var crudService = resourceService;
	      		  self.translate = sbiModule_translate;
	      		  driversResource.changedDrivers = [];
	      		  driversResource.driverParuses = [];
	      		  driversResource.driversForDeleting = [];
	      		  driversResource.lovIdAndColumns = [];
	      		  driversResource.paruseColumns = {};

	      		  driversResource.analyticalDrivers = [];

	      		  driversResource.visualDependencies = "visualdependencies";
	      		  driversResource.dataDependenciesName = "datadependencies";
	      		  driversResource.selectedVisualCondition = {};
	      		  driversResource.selectedDataCondition = {};
	      		  driversResource.visusalDependencyObjects = [];
	      		  driversResource.dataDependencyObjects = {};
	      		  driversResource.changedVisualDependencies = [];
	      		  driversResource.changedDataDependencies = [];
	      		  driversResource.dataDependenciesForDeleting = [];
	      		  driversResource.visualDependenciesForDeleting = [];
	      		  driversResource.driverRelatedObject = {};
	    		  driversResource.driversOnObject = [];
	    		  driversResource.driversNum = 0;

	      		driversResource.getParusesByAnaliticalDriverId = function (driverId){
	    			   var base = "2.0/analyticalDrivers";
	                   var path = driverId + "/modes";
	                   crudService.get(base,path).then(function(response){
	                		for(var i = 0; i < response.data.length; i++) {
	                			var existingDriverList = [];
	                			for(var j = 0; j < driversResource.driverParuses.length;j++){
	                				if(driversResource.driverParuses[j].useID == response.data[i].useID)
	                					existingDriverList.push(driversResource.driverParuses[j]);
	                			}
	                			//var existingDriverList = driversResource.driverParuses.filter(paruse => (paruse.useID == response.data[i].useID))
	                			if(existingDriverList.length != 0)
	                				continue;
	                			driversResource.driverParuses.push(response.data[i]);
	                		}
	                   });
	    		   }

	      		driversResource.fillAllDriversPerModel = function(basePath,businessModel){
	      			driversResource.renderedDrivers.length = 0;
      				var endPath = businessModel.id + '/drivers';
	      			crudService.get(basePath,endPath).then(function(response){
	      				for(var i = 0; i < response.data.length; i++){
	      					var isNotContained = true;
	      					if(driversResource.renderedDrivers.length == 0){
	      						driversResource.renderedDrivers.push(response.data[i])
	      						continue;
	      					}
	      					for(var j = 0; j < driversResource.renderedDrivers.length;j++){
	      						if(driversResource.renderedDrivers[j].id == response.data[i].id ){
	      							isNotContained = false;
	      							break;
	      						}
	      					}
	      					if(isNotContained) driversResource.renderedDrivers.push(response.data[i]);
	      				}
	      			});
	      		}

	      		driversResource.getAllAnalyticalDrivers = function (){
	      			var base = "2.0/analyticalDrivers";
	      			var path = "";
	      			crudService.get(base,path).then(function(response){
		    			driversResource.analyticalDrivers=response.data;
		    			console.log('response.data.drivers');
		    			console.log(response.data);
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
	      			var promise = crudService.get(basePath,endPath).then(function(response){
	      				driversResource.driversOnObject = response.data;
	      			});
	      			return promise;
	      		}

	      		driversResource.getDriversforBM = function(basePath,endPath){
	      			var promise = crudService.get(basePath,endPath).then(function(response){
	      				driversResource.driversOnObject = response.data;
	      			});
	      			return promise;
	      		}

	      		driversResource.setDriversOnRelatedObject = function(driversOnRelatedObject){
	      			driversResource.driversOnObject = driversOnRelatedObject;
	      		}

	      	  driversResource.persistDrivers = function(driverableObjectId,requiredPath){
	           	 var basePath = driversResource.visualDependencies;
	          	 var baseDataPath = driversResource.dataDependenciesName;
	           	 var querryParams = "";
	           	driverPostBasePath = driverableObjectId + "/drivers" ;
	           	for(var i = 0; i < driversResource.changedDrivers.length; i++){
	           		if(driversResource.changedDrivers[i].newDriver){
	           			 prepareDriverForPersisting(driversResource.changedDrivers[i]);
	           			crudService.post(requiredPath,driverPostBasePath,driversResource.changedDrivers[i]).then(function(response){
	           				if(response.data.errors){
	               				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
	               			}else
	               				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.drivercreated"), 'Success!');
		           				for(var i = 0;i < driversResource.renderedDrivers.length; i++){
		           					if(response.data.label == driversResource.renderedDrivers[i].label)
		           						driversResource.renderedDrivers[i] = response.data;
		           				}
		           				driversResource.setSelectedDriver(response.data);
		           				driversResource.getParusesByAnaliticalDriverId(response.data.parID);
		           				driversResource.getDataDependenciesByDriverId(requiredPath, driversResource.dataDependenciesName, response.data);
	           					//var driverIndex = driversResource.driversOnObject.findIndex(i => i.priority ==response.data.priority);
	           					var driverIndex = -1;
		                       	 for(var i = 0; i < driversResource.driversOnObject.length;i++){
		                       		 if(driversResource.driversOnObject[i].priority == response.data.priority)
		                       			 driverIndex = i;
		                       	 }
	           					if(driverIndex == -1){
	           						driversResource.driversOnObject.push(response.data);
	           					}else{
	           						driversResource.driversOnObject[driverIndex].id = response.data.id
	           					}
	           					 driversResource.driversNum = (driversResource.driversOnObject.length > 1);
		        				 querryParams = setQuerryParameters(response.data.id);
		        				 basePath =driverableObjectId +"/" + basePath + querryParams;
		        	             baseDataPath = driverableObjectId +"/" + baseDataPath + querryParams;
		        	             driversResource.getLovsByAnalyticalDriverId(response.data.parID);
	           			});
	           		}else{
	           			prepareDriverForPersisting(driversResource.changedDrivers[i]);
	           			var driverPutBasePath = driverableObjectId + "/drivers/" +  driversResource.changedDrivers[i].id;
	           			crudService.put(requiredPath,driverPutBasePath,driversResource.changedDrivers[i]).then(function(response){
	           				if(response.data.errors){
	               				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
	               			}else
	               				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.driverupdated"), 'Success!');
	           			});
	           		}
	           	}
	           	driversResource.changedDrivers = [];
	            }

	      	  var setQuerryParameters = function(driverID){
	           	 return "?driverId="+driverID;
	            }

	      	 var prepareDriverForPersisting = function(driver){
	         	setParameterInfo(driver);
	         	delete driver.newDriver;
	 			delete driver.$$hashKey;
	 			delete driver.parameter.checks;
	 			delete driver.parameter.$$hashKey;
	 			delete driver.parameter.$$mdSelectId;
	 			driver.modifiable = 0;
	         };

	         var setParameterInfo = function(driver){
	          	 for(var i = 0 ; i<driversResource.analyticalDrivers.length; i++){
	          		 if(driversResource.analyticalDrivers[i].label==driver.parameter.name){
	          			 driver.parameter = driversResource.analyticalDrivers[i];
	          		 	 driver.parID = driversResource.analyticalDrivers[i].id;}
	          	 }
	           };

	      		driversResource.persistVisualDependency = function(driverableObjectId,requiredPath){
	      	       	for(var i = 0; i < driversResource.changedVisualDependencies.length; i++){
	      	       		var visualDependency = driversResource.changedVisualDependencies[i];
	      	       		var visualPath = driverableObjectId +  '/visualdependencies';
	      	       		if(visualDependency.newDependency){
	      	       			prepareDependencyForPersisting(visualDependency)
	      	       			crudService.post(requiredPath,visualPath,visualDependency).then(function(response){
	      	       				if(response.data.errors){
	      	              				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
	      	              			}else
	      	       				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.visualdependecycreated"), 'Success!');
	      	       				if(!driversResource.visusalDependencyObjects){ driversResource.visusalDependencyObjects = []; driversResource.visusalDependencyObjects[visualDependency.parId] =[]}
	      	       				for(var i = 0;i<driversResource.visusalDependencyObjects[visualDependency.parId].length;i++)
	      	       			if (driversResource.visusalDependencyObjects[visualDependency.parId][i].prog == visualDependency.prog)
        						driversResource.visusalDependencyObjects[visualDependency.parId][i] = response.data

	      	       			});;
	      	       		}else{
	      	       			crudService.put(requiredPath,visualPath,visualDependency).then(function(response){
	      	       				if(response.data.errors){
	      	              				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
	      	              			}else
	      	       				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.visualdependecyupdated"), 'Success!');

	      	       			});;
	      	       		}
	      	       	}
	      	      driversResource.changedVisualDependencies = [];
	      	       };

	      	     driversResource.persistDataDependency = function(driverableObjectId,requiredPath){
		      	   	var parusesForDataDependency={};
	      	       	for(var i = 0; i < driversResource.changedDataDependencies.length; i++){
	      	       		var dataDependency = driversResource.changedDataDependencies[i];
	      	       		var prog = dataDependency.prog;
	      	       		var dataPath = driverableObjectId + '/datadependencies';
	      	       		var filterColumnsForDataDependency=[];

      	       			if(driversResource.changedDataDependencies[i].useModeId != undefined){
	      	       			var newDataDependency = {};
	      	       			if(prog == dataDependency.prog){
	      	       				newDataDependency = dataDependency;
	      	       			}else{
	      	       				newDataDependency = angular.copy(dataDependency);
	      	       			}
	      	       			var paruse = [];
	            			for(var k = 0; k < driversResource.driverParuses.length;k++){
	            				if(driversResource.driverParuses[k].useID == driversResource.changedDataDependencies[i].useModeId)
	            					paruse.push(driversResource.driverParuses[k]);
	            			}
	            			if(paruse.length == 0)
	            				continue;
		      	       			//var paruse = driversResource.driverParuses.filter(par => par.useID==driversResource.changedDataDependencies[i].useModeId)
		      	       			newDataDependency.useModeId= paruse[0].useID;
      			        		if(driversResource.changedDataDependencies[i].id==undefined){
      			        			prepareDependencyForPersisting(newDataDependency);
      			        			parusesForDataDependency[newDataDependency.useModeId] = false;
      			        			delete newDataDependency.persist;
      			        			crudService.post(requiredPath,dataPath,newDataDependency).then(function(response){
      			        				if(response.data.errors){
      			               				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
      			               			}else{
      			               			driversResource.transformingCorrelations([response.data], response.data.parFatherId+response.data.filterOperation,true);
      			        				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.datadependecycreated"), 'Success!');
      			        				for(var i = 0; i < driversResource.dataDependencyObjects[newDataDependency.parId].length; i++){
      			        					if (driversResource.dataDependencyObjects[newDataDependency.parId][i].prog == newDataDependency.prog)
      			        						driversResource.dataDependencyObjects[newDataDependency.parId][i] = response.data
      			        					}
      			               			}
      			        			});
      			        			newDataDependency.prog++;
      			        		}else{
      			        			parusesForDataDependency[newDataDependency.useModeId] = false;
      			        			delete newDataDependency.persist;
      			        			crudService.put(requiredPath,dataPath,newDataDependency).then(function(response){
      			        				if(response.data.errors){
      			               				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
      			               			}else{
      			               				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.datadependecyupdated"), 'Success!');
      			               			}
      			        			});
      			        			newDataDependency.prog++;
      			        		}
      	       			}
	      	       }
		      	      driversResource.changedDataDependencies = [];
	      	     }

	      	     var prepareDependencyForPersisting = function(dependency){
	             	delete dependency.newDependency;
	             };

	             driversResource.deleteDriverVisualDependency = function(visualDependency,driverableObjectId,requiredPath){
	             	  var visualDependencyBasePath = driverableObjectId + "/" + driversResource.visualDependencies + "/delete";
	             	  crudService.post(requiredPath,visualDependencyBasePath,visualDependency).then(function(response){
	             		  if(response.data.errors){
	              				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
	              			}else
	       	      		sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.deleted"), 'Success!');
	       	      	  });
	             };

	             driversResource.deleteDriverDataDependency = function(dataDependency,driverableObjectId,requiredPath){
	             	  var dataDependencyBasePath = driverableObjectId + "/" + driversResource.dataDependenciesName + "/delete";
	             	  crudService.post(requiredPath,dataDependencyBasePath,dataDependency).then(function(response){
	             		if(response.data.errors){
	          				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
	          			}else
	             		sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.deleted"), 'Success!');
	             	  });
	           };

	           driversResource.getLovsByAnalyticalDriverId = function(driverId){
	          	 var requiredPath = "2.0/analyticalDrivers";
	          	 var basePath = driverId + "/lovs";
	          	resourceService.get(requiredPath,basePath).then(function(response){
	          		for(var i = 0;i<response.data.length;i++){
	          			driversResource.lovIdAndColumns.push(setLovColumns(response.data[i]));
	          		 }
	          	 });
	           }

				var setLovColumns = function(lov){
					var lovIdAndColumns = {}
					var lovColumns = [];
					var lovObject = JSON.parse(lov.lovProviderJSON);
					if(lovObject != [] && lovObject.QUERY){
						var visibleColumns = lovObject.QUERY['VISIBLE-COLUMNS'];
						var invisibleColumns = lovObject.QUERY['INVISIBLE-COLUMNS'];

						var visibleColumnsArr = visibleColumns ? visibleColumns.split(',') : [];
						var invisibleColumnsArr = invisibleColumns ? invisibleColumns.split(',') : [];
						for (var i of visibleColumnsArr) {
							lovColumns.push(i);
						}
						for (var i of invisibleColumnsArr) {
							lovColumns.push(i);
						}

						lovIdAndColumns.id = lov.id;
						lovIdAndColumns.columns = lovColumns.filter(function(el) { return "" !== el; }).sort();
					}
					return lovIdAndColumns;
				}

				driversResource.deleteDrivers = function(driverableObjectId,requiredPath){
					for(var i = 0; i < driversResource.driversForDeleting.length; i++){
						driversResource.deleteDriverById(driversResource.driversForDeleting[i],driverableObjectId,requiredPath);
					}
					driversResource.driversForDeleting = [];
				};

	           driversResource.deleteDriverById = function(driver,driverableObjectId,requiredPath){
	              var basePath = driverableObjectId + "/" + 'drivers' ;
	          	  var basePathWithId = basePath + "/" + driver.id;
	          	  crudService.delete(requiredPath,basePathWithId).then(function(response){
	          		if(response.data.errors){
	      				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
	      			}else
	              		sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.deleted"), 'Success!');
	              });
	           };

	             driversResource.deleteVisualDependencies = function(driverableObjectId,requiredPath){
	             	for(var i = 0; i < driversResource.visualDependenciesForDeleting.length; i++){
	             		driversResource.deleteDriverVisualDependency(driversResource.visualDependenciesForDeleting[i],driverableObjectId,requiredPath)
	             	}
	             	driversResource.visualDependenciesForDeleting=[];
	             };

	             driversResource.deleteDataDependencies = function(driverableObjectId,requiredPath){
	             	for(var i = 0; i < driversResource.dataDependenciesForDeleting.length; i++){
	             		delete driversResource.dataDependenciesForDeleting[i].deleteItem;
	             		driversResource.deleteDriverDataDependency(driversResource.dataDependenciesForDeleting[i],driverableObjectId,requiredPath)
	             	}
	             	driversResource.dataDependenciesForDeleting=[];
	             };

				driversResource.getAllAnalyticalDrivers();
	      	return driversResource;
    		}]);
})();