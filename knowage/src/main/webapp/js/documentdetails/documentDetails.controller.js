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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
    'use strict';

    angular
        .module('DocumentDetails', ['ngMaterial', 'jsonFormatter','sbiModule', 'componentTreeModule', 'file_upload','DriversModule','TemplateModule', 'OutputParametersModule', 'DataLineageModule', 'SubreportsModule'])
        .config(['$mdThemingProvider','$locationProvider','$httpProvider', function($mdThemingProvider,$locationProvider,$httpProvider) {
            $mdThemingProvider.theme('knowage')
            $mdThemingProvider.setDefaultTheme('knowage');
            $httpProvider.interceptors.push('httpInterceptor');
            $locationProvider.html5Mode({
            	  enabled: true,
            	  requireBase: false
            	});
        }])
        .controller('DocumentDetailsController',['$scope','$filter','DriversService','DocumentService','templateService','outputParametersService','dataLineageService','subreportsService','closingIFrame','$location','resourceService','multipartForm','$mdDialog', 'sbiModule_restServices', 'sbiModule_translate', 'sbiModule_messaging', DocumentDetailsController])

    function DocumentDetailsController($scope,$filter,DriversService,DocumentService,templateService,outputParametersService,dataLineageService,subreportsService,closingIFrame,$location,resourceService,multipartForm,$mdDialog,sbiModule_restServices,sbiModule_translate,sbiModule_messaging) {
        var self = this;
        var documentService = DocumentService;
        self.translate = sbiModule_translate;
        var requiredPath = documentService.requiredPath;
        var paruses = documentService.driverParuses;
        self.title = "Document Details";
        var template = templateService.template;
        var document = documentService.document;
        DriversService.setDriverRelatedObject(document);
		DriversService.getDriversOnRelatedObject(requiredPath,document.id + "/drivers");
        self.analyticalDrivers = DriversService.analyticalDrivers;
        self.lovIdAndColumns = DriversService.lovIdAndColumns;
        var documentBasePath =""+ document.id;
        var driverPostBasePath = document.id + '/drivers';
		self.typeCode = documentService.document.typeCode;
		self.docId = documentService.documentId;

        self.cancelFunction = function() {
        	window.parent.angular.element(window.frameElement).scope().closeDialogFromExt();
        };

        self.savingFunction = function(){

        	persistDocument();
        	
        	persistDrivers();
        	deleteDrivers();
        	
        	persistDataDependency();
        	deleteDataDependencies();
        	
        	persistVisualDependency();
        	deleteVisualDependencies();
        	
        	outputParametersService.persistOutputParameters();
        	outputParametersService.deleteOutputParameters();
        	
        	dataLineageService.persistTables();
			dataLineageService.deleteTables();
        	
        	templateService.uploadTemplate();
        	templateService.setActiveTemplate();
        	templateService.deleteTemplates();

        	subreportsService.deleteSubreports();
			subreportsService.persistSubreports();			

        };


        var uploadImage = function() {
        	if(documentService.previewFile.file) {
        		var templateUploadBasePath = document.id + '/image';
        		multipartForm.post(documentService.requiredPath +"/"+ templateUploadBasePath, documentService.previewFile).then(function(response){
  				  getImage();
  			});
        	}
        };
        var getImage = function(){
        	resourceService.get(documentService.requiredPath,document.id + '/image').then(function(response){
        		 documentService.documentImage = response.data;
           	});
        }

        var prepareDocumentForPersisting = function(document){
        	delete document.dataSetLabel;
    		delete document.creationDate;
    		document.refreshSeconds = parseInt(document.refreshSeconds);
        };

        var prepareDependencyForPersisting = function(dependency){
        	delete dependency.newDependency;
        };

        var prepareDriverForPersisting = function(driver){
        	self.setParameterInfo(driver);
        	delete driver.newDriver;
			delete driver.$$hashKey;
			delete driver.parameter.checks;
			delete driver.parameter.$$hashKey;
			delete driver.parameter.$$mdSelectId;
			driver.modifiable = 0;
        };

        self.deleteDriverVisualDependency = function(visualDependency){
      	  var visualDependencyBasePath = document.id + "/" + DriversService.visualDependencies + "/delete";
      	  resourceService.post(requiredPath,visualDependencyBasePath,visualDependency).then(function(response){
      		  if(response.data.errors){
       				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
       			}else
	      		sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.deleted"), 'Success!');
	      	  });
        };

        self.deleteDriverDataDependency = function(dataDependency){
      	  var dataDependencyBasePath = document.id + "/" + DriversService.dataDependenciesName + "/delete";
      	  resourceService.post(requiredPath,dataDependencyBasePath,dataDependency).then(function(response){
      		if(response.data.errors){
   				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
   			}else
      		sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.deleted"), 'Success!');
      	  });
        };

	    self.deleteDriverById = function(driver){
	     	  var requiredPath = documentService.requiredPath;
	        var basePath = document.id + "/" + 'drivers' ;
	     	  var basePathWithId = basePath + "/" + driver.id;
	     	  resourceService.delete(requiredPath,basePathWithId).then(function(response){
	     		if(response.data.errors){
	 				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
	 			}else
	         		sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.deleted"), 'Success!');
	         });
	   };

        self.setParameterInfo = function(driver){
          	 for(var i = 0 ; i<self.analyticalDrivers.length; i++){
          		 if(self.analyticalDrivers[i].label==driver.parameter.name){
          			 driver.parameter = self.analyticalDrivers[i];
          		 	 driver.parID = self.analyticalDrivers[i].id;}
          	 }
        };
    
        var deleteDrivers = function(){
        	for(var i = 0; i < DriversService.driversForDeleting.length; i++){
        		self.deleteDriverById(DriversService.driversForDeleting[i]);
        	}
        };

        var deleteDataDependencies = function(){
        	for(var i = 0; i < DriversService.dataDependenciesForDeleting.length; i++){
        		self.deleteDriverDataDependency(DriversService.dataDependenciesForDeleting[i])
        	}
        	DriversService.dataDependenciesForDeleting=[];
        };

        var deleteVisualDependencies = function(){
        	for(var i = 0; i < DriversService.visualDependenciesForDeleting.length; i++){
        		self.deleteDriverVisualDependency(DriversService.visualDependenciesForDeleting[i])
        	}
        	DriversService.visualDependenciesForDeleting=[];
        };

        var persistDrivers = function(){
          	 var basePath = DriversService.visualDependencies;
         	 var baseDataPath = DriversService.dataDependenciesName;
          	 var querryParams = "";
          	driverPostBasePath = document.id + "/drivers" ;
          	for(var i = 0; i < DriversService.changedDrivers.length; i++){
          		if(DriversService.changedDrivers[i].newDriver){
          			 prepareDriverForPersisting(DriversService.changedDrivers[i]);
          			resourceService.post(documentService.requiredPath,driverPostBasePath,DriversService.changedDrivers[i]).then(function(response){
          				if(response.data.errors){
              				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
              			}else
       				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.drivercreated"), 'Success!');

          					var driverIndex = DriversService.drivers.findIndex(i => i.priority ==response.data.priority);
          					if(driverIndex == -1){
          						DriversService.drivers.push(response.data);
          					}else{documentService.drivers[driverIndex].id = response.data.id}
          					DriversService.getAllAnalyticalDrivers();
          					DriversService.driversNum = (DriversService.drivers.length > 1);
   	        				 querryParams = setQuerryParameters(response.data.id);
   	        				 basePath =document.id+"/" + basePath + querryParams;
   	        	             baseDataPath = document.id+"/" + baseDataPath + querryParams;
   	        				getLovsByAnalyticalDriverId(response.data.parID);
          			});
          		}else{
          			prepareDriverForPersisting(DriversService.changedDrivers[i]);
          			var driverPutBasePath = document.id + "/drivers/" +  DriversService.changedDrivers[i].id;
          			resourceService.put(documentService.requiredPath,driverPutBasePath,DriversService.changedDrivers[i]).then(function(response){
          				if(response.data.errors){
              				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
              			}else
       				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.driverupdated"), 'Success!');
          			});
          		}

          		}
          	DriversService.changedDrivers = [];
           }

       var setQuerryParameters = function(driverID){
      	 return "?driverId="+driverID;
       }
       var persistVisualDependency = function(){
       	for(var i = 0; i < DriversService.changedVisualDependencies.length; i++){
       		var visualDependency = DriversService.changedVisualDependencies[i];
       		var visualPath = document.id+ '/visualdependencies';
       		if(visualDependency.newDependency){
       			prepareDependencyForPersisting(visualDependency)
       			resourceService.post(documentService.requiredPath,visualPath,visualDependency).then(function(response){
       				if(response.data.errors){
              				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
              			}else
       				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.visualdependecycreated"), 'Success!');
       			});;
       		}else{
       			resourceService.put(documentService.requiredPath,visualPath,visualDependency).then(function(response){
       				if(response.data.errors){
              				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
              			}else
       				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.visualdependecyupdated"), 'Success!');
       			});;
       		}
       	}
       	documentService.changedVisualDependencies = [];
       };

       var persistDataDependency = function(documentBasePath){
       	for(var i = 0; i < DriversService.changedDataDependencies.length; i++){
       		var persistances =  Object.keys(DriversService.paruseColumns);
       		var filterColumns =  Object.values(DriversService.paruseColumns);
       		var dataDependency = DriversService.changedDataDependencies[i];
       		var isNew = dataDependency.newDependency;
       		var prog = dataDependency.prog;
       		var dataPath = document.id+ '/datadependencies';
       		var parusesForDataDependency={};
       		var filterColumnsForDataDependency=[];
       		parusesForDataDependency = dataDependency.persist;
       		for(var j = 0 ; j < persistances.length;j++){
       			if(persistances[j] != "undefined" && parusesForDataDependency[persistances[j]]){
       			var newDataDependency = {};
       			if(prog == dataDependency.prog){
       				newDataDependency = dataDependency;
       			}else{
       				newDataDependency = angular.copy(dataDependency);
       			}
       				newDataDependency.filterColumn =  filterColumns[j]
       				var paruse = paruses.filter(par => par.useID==persistances[j])
       				newDataDependency.paruseId= paruse[0].useID;
		        		if(isNew){
		        			prepareDependencyForPersisting(newDataDependency);
		        			delete newDataDependency.persist;
		        			resourceService.post(documentService.requiredPath,dataPath,newDataDependency).then(function(response){
		        				if(response.data.errors){
		               				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
		               			}else
		        				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.datadependecycreated"), 'Success!');
		        			});
		        			newDataDependency.prog++;
		        		}else{
		        			delete newDataDependency.persist;
		        			resourceService.put(documentService.requiredPath,dataPath,newDataDependency).then(function(response){
		        				if(response.data.errors){
		               				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
		               			}else
		        				sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.datadependecyupdated"), 'Success!');
		        			});
		        			newDataDependency.prog++;
       			}
       		}
       	}

       };
       DriversService.changedDataDependencies = [];
       };
        var getLovColumnsForParuse = function(paruse){
       	 for(var i = 0; i < DriversService.lovIdAndColumns.length;i++){
       		 if(paruse.idLov == DriversService.lovIdAndColumns[i].id)
       			 return DriversService.lovIdAndColumns[i].columns;
       	 }

       };
       var getLovsByAnalyticalDriverId = function(driverId){
        	 var requiredPath = "2.0/analyticalDrivers";
        	 var basePath = driverId + "/lovs";
        	resourceService.get(requiredPath,basePath).then(function(response){
        		for(var i = 0;i<response.data.length;i++){
        			DriversService.lovIdAndColumns.push( setLovColumns(response.data[i]));
        		 }
        	 });
         }
         var setLovColumns = function(lov){
        	 var lovIdAndColumns = {}
        	 var lovColumns = [];
        	 var lovObject = JSON.parse(lov.lovProviderJSON);
        	 	if(lovObject != []){
        	 	var stringColumns = lovObject.QUERY['VISIBLE-COLUMNS'];
  	            	 if(stringColumns.includes(",")){
  	            		  lovColumns = stringColumns.split(',')
  	            		  lovIdAndColumns.id = lov.id;
  	            		  lovIdAndColumns.columns = lovColumns;
  	            	 }else{
  	            		  lovColumns.push(stringColumns);
  	            		  lovIdAndColumns.id = lov.id;
  	            		  lovIdAndColumns.columns = lovColumns;
  	            	 }
        	 }

        	 	return lovIdAndColumns;
         }
         var persistDocument = function(){
      	   prepareDocumentForPersisting(document);
      	   setFoldersPath();
         	if(document.id){
         		resourceService.put(documentService.requiredPath,document.id,document).then(function(response){
         			if(response.data.errors){
         				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
         			}else
         			sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.documentupdated"), 'Success!');
         			documentService.document = response.data;
         			document = response.data;
         			uploadImage();
         		});
         	} else{
         		resourceService.post(documentService.requiredPath,"",document).then(function(response){
         			if(response.data.errors){
         				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
         			}else
         			sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.documentcreated"), 'Success!');
         			documentService.document = response.data;
         			document = response.data;
         			self.docId = response.data.id;
         			uploadImage();
         		});
         	}
         };

         var setFoldersPath = function(){
      	   document.functionalities=[];
    		 for(var i = 0; i<documentService.folders.length; i++ ){
    			 if(documentService.folders[i].checked){
    				 document.functionalities.push(documentService.folders[i].path);
    		 }
    	}
     };

       closingIFrame.close();

   };

})();