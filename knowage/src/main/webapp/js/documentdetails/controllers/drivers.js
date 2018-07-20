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
angular
        .module('DriversModule')
        .controller('DocumentDetailsDriversController',['$scope','$location','DriversService','resourceService','$httpParamSerializer', '$mdDialog','sbiModule_translate','sbiModule_messaging','$filter',
        										function($scope,$location,DriversService,resourceService,$httpParamSerializer, $mdDialog,sbiModule_translate,sbiModule_messaging,$filter){

        	 var self = this;
        	 var driversService = DriversService;
        	 self.translate = sbiModule_translate;
             self.driverRelatedObject = driversService.driverRelatedObject;
          //   self.confirmDelete = DocumentService.confirmDelete;   // ******************
																	// TODO: ...
             var crudService = resourceService;
             var requiredPath = "";
             var id = self.driverRelatedObject.id;
             var basePath = id + "/" + 'drivers';
             self.driverParuses = [];

             self.analyticalDrivers = [];

             self.driversPerModel = driversService.driversPerModel;
             driversService.lovColumns=[];
             self.selectedDataCondition = driversService.selectedDataCondition;
             self.selectedVisualCondition = driversService.selectedVisualCondition;
             self.visibilityConditions = driversService.visusalDependencyObjects;
             self.dataConditions = driversService.dataDependencyObjects;
             self.drivers=DriversService.driversOnObject;
             self.driversNum = self.drivers.length > 1;
             self.required = true;
             var requiredPath = "2.0/documents1";

             var getDriverNames = function(driversOnObject){
            	 var driverNames=[];
		            if(driversService.driverRelatedObject.id){
		            	 for(var i = 0; i< driversOnObject.length;i++){
		            		 driverNames.push(driversOnObject[i].name);
		            	 }
		            return driverNames;
		            	 }
             }

             self.addDriver = function() {
              if(driversService.driverRelatedObject.hasOwnProperty('modelLocked')){

            	 		if(driversService.driverRelatedObject.id){
       					 if (self.drivers) {
       	                     self.drivers.push({ 'label': '', 'priority': self.drivers.length == 0 ? 1: self.drivers.length ,'newDriver':'true',  'biMetaModelID' :driversService.driverRelatedObject.id,'visible':false,'required':false,'multivalue':false });
       	                     var index = self.drivers.length;
       	                  self.driversNum = self.drivers.length > 1;
       	                     self.selectDriver( index );
       	                 } else {
       	                     self.drivers = [{ 'label': '', 'priority': 1,'newDriver':'true','biMetaModelID' : driversService.driverRelatedObject.id ,'visible':false,'required':false,'multivalue':false}];
       	                  self.driversNum = self.drivers.length > 1;
       	                     self.selectDriver(1);

       	                 }
       				 }
             }else{
				 if(driversService.driverRelatedObject.id){
					 if (self.drivers) {
	                     self.drivers.push({ 'label': '', 'priority': self.drivers.length == 0 ? 1: self.drivers.length ,'newDriver':'true',  'biObjectID' :driversService.driverRelatedObject.id,'visible':false,'required':false,'multivalue':false });
	                     var index = self.drivers.length;
	                     self.driversNum = self.drivers.length > 1;
	                     self.selectDriver( index );
	                 } else {
	                     self.drivers = [{ 'label': '', 'priority': 1,'newDriver':'true', 'biObjectID' : driversService.driverRelatedObject.id ,'visible':false,'required':false,'multivalue':false}];
	                     self.driversNum = self.drivers.length > 1;
	                     self.selectDriver(1);
	                 }
				 }
             }
             }

             self.openMenu=function(menu,event){
            	 menu(event);
             }

             self.addToChangedDrivers = function(driver){
            	 self.setParameterInfo(self.selectedDriver);
            	 if(driversService.changedDrivers.indexOf(driver) == -1)
            		 driversService.changedDrivers.push(driver);
             }

             $scope.$on('changedModel', function(event, data) {
            	   self.driverRelatedObject = data;
            	   self.selectedDriver = undefined;
            	   self.drivers = $filter('filter')(driversService.driversPerModel, {biMetaModelID: data.id});
            	   requiredPath = "2.0/businessmodels";
            	   self.driversNum = self.drivers.length > 1
             });

             $scope.$on('setDocumentPath', function(event, data) {
            	 requiredPath = data;
             });

             var addParId = function(driver){
            	 driver.parID = driver.parameter.id;
             }

             var getVisualDependenciesByDriverId = function(requiredPath,basePath,driver){
            	 crudService.get(requiredPath,basePath).then(function(response){
            		 driversService.visusalDependencyObjects = response.data;
            		 self.visibilityConditions = driversService.visusalDependencyObjects;
               	});
             }

             var getDataDependenciesByDriverId = function(requiredPath,basePath,driver){
            	 crudService.get(requiredPath,basePath).then(function(response){
            		 driversService.dataDependencyObjects = response.data;
            		 self.dataConditions = driversService.dataDependencyObjects;

               	});
             }

             var getLovsByAnalyticalDriverId = function(driverId){
            	 var requiredPath = "2.0/analyticalDrivers";
            	 var basePath = driverId + "/lovs";
            	 crudService.get(requiredPath,basePath).then(function(response){
            		for(var i = 0;i<response.data.length;i++){
            			driversService.lovIdAndColumns.push( setLovColumns(response.data[i]));
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

             self.selectDriver = function(priority) {
            	 if( self.analyticalDrivers.length == 0)
            		 self.analyticalDrivers = getDriverNames(driversService.analyticalDrivers);
            	 if(self.drivers.length==1 && self.drivers.length == 0){self.selectedDriver = self.drivers[0];}
            	  var querryParams = "";
            	  var basePath = driversService.visualDependencies;
            	  var baseDataPath = driversService.dataDependenciesName;
	                 for (var i in self.drivers) {
	                     if (self.drivers[i].priority == priority) {
	                    	 self.setParameterInfo(self.drivers[i]);
	                         self.selectedDriver = self.drivers[i];
	                         self.setParameterInfo(self.selectedDriver);
							 if(self.selectedDriver.parID)
	                         getLovsByAnalyticalDriverId(self.selectedDriver.parID);
	                         	if(self.selectedDriver.id){
			                         querryParams = setQuerryParameters(self.selectedDriver.id);
			                         basePath = self.driverRelatedObject.id + "/" + basePath + querryParams;
			                         baseDataPath = self.driverRelatedObject.id + "/" + baseDataPath + querryParams;
			                         getVisualDependenciesByDriverId(requiredPath,basePath, self.selectedDriver);
			                         getDataDependenciesByDriverId(requiredPath,baseDataPath, self.selectedDriver);
			                         break;
	                            }
	                        return;
	                     }
	                     self.selectedDriver = self.drivers[self.drivers.length - 1];
	                 }

             }

             self.deleteDriver = function(driver,name) {
                 var index;
                 for (var i = 0; i< self.drivers.length;i++) {
                     if (self.drivers[i].id == driver.id) {
                        	// delete self.selectedDriver;
                        	 index = i;
                         }
                     }
                 for (var d in self.drivers) {
                     if (self.drivers[d].priority > self.drivers[index].priority) {
                         self.drivers[d].priority--;
                     }
                 }
                 self.confirmDelete(index,name);
             }

             self.movePriority = function(priority, direction) {
                 var cur, next, prev;
                 for (var p in self.drivers) {
                     if (self.drivers[p].priority == priority) cur = p;
                     if (direction == 'up' && self.drivers[p].priority == priority - 1) prev = p;
                     if (direction == 'down' && self.drivers[p].priority == priority + 1) next = p;
                 }
                 if (direction == 'up') {
                     self.drivers[cur].priority--;
                     self.drivers[prev].priority++;
                 }
                 if (direction == 'down') {
                     self.drivers[cur].priority++;
                     self.drivers[next].priority--;
                 }
             }

             self.editCondition = function(ev, selectedDriver, selectedCondition) {
                 if (!selectedCondition && selectedCondition != 0) {
                     if (driversService.visusalDependencyObjects) {
                    	 driversService.visusalDependencyObjects.push({'newDependency':'true'})
                         self.visibilityConditions = driversService.visusalDependencyObjects;
                     } else {
                    	 driversService.visusalDependencyObjects = [];
                    	 driversService.visusalDependencyObjects.push({})
                     }
                     if(driversService.visusalDependencyObjects.length >0){
                         selectedCondition = driversService.visusalDependencyObjects.length - 1;
                         }else{
                        	 selectedCondition = 0;
                         }
                 }
             $mdDialog.show({
                         controller: CorrelationDialogController,
                         templateUrl: '../js/documentdetails/templates/correlationDialog.html',
                         targetEvent: ev,
                         clickOutsideToClose: true,
                         locals: {
                             selectedDriver: selectedDriver,
                             selectedCondition: selectedCondition
                         }
                     })
                     .then(
                         function(answer) {},
                         function() {});
             };

             self.editDataCondition = function(ev, selectedDriver, selectedDataCondition) {
                 if (!selectedDataCondition && selectedDataCondition != 0) {
                     if (driversService.dataDependencyObjects) {
                    	 driversService.dataDependencyObjects.push({'newDependency':'true'})
                         self.dataConditions = driversService.dataDependencyObjects;

                     } else {
                    	 driversService.dataDependencyObjects = [];
                    	 driversService.dataDependencyObjects.push({})

                     }
                     if(driversService.dataDependencyObjects.length >0){
                     selectedDataCondition = driversService.dataDependencyObjects.length - 1;
                     }else{
                    	 selectedDataCondition = 0;
                     }
                 }

             $mdDialog.show({
                         controller: CorrelationDataDialogController,
                         templateUrl: '../js/documentdetails/templates/correlationDataDialog.html',
                         targetEvent: ev,
                         clickOutsideToClose: true,
                         locals: {
                             selectedDriver: selectedDriver,
                             selectedDataCondition: selectedDataCondition
                         }
                     })
                     .then(
                         function(answer) {},
                         function() {});
             };

             self.deleteCondition = function(index,name) {
            	 self.confirmDelete(index,name);
             }

             self.deleteDataCondition = function(index,name) {
            	 self.confirmDelete(index,name);
             }

             var setQuerryParameters = function(driverID){
            	 return "?driverId="+driverID;
             }

             function CorrelationDialogController($scope, DriversService, selectedDriver, selectedCondition, $mdDialog) {
            	 $scope.translate = sbiModule_translate;
            	 if(selectedCondition == undefined){
            		 selectedCondition = driversService.visusalDependencyObjects.length - 1;
            	 }
            	 var selectedConditionIndex = selectedCondition;
            	 $scope.driversService = DriversService;
                 $scope.document = driversService.driverRelatedObject;
                 $scope.drivers = driversService.driversOnObject;
                 $scope.selectedDriver = selectedDriver;
                 $scope.selectedCondition = driversService.visusalDependencyObjects[selectedCondition];
                 $scope.availableOperators = ['>', '<', '=', 'contains','notcontains'];
                 driversService.selectedVisualCondition = driversService.visusalDependencyObjects[selectedCondition];
                 $scope.close = function(selectedCondition) {
                	 for(var i = 0; i < driversService.visusalDependencyObjects.length;i++){
	                	 if( $scope.selectedCondition.newDependency && selectedConditionIndex == i)
	                		 driversService.visusalDependencyObjects.splice(i, 1);
                	 }
                	 $mdDialog.cancel(); }
                 $scope.hide = function() { setVisualDependencyProperties(driversService.selectedVisualCondition); $mdDialog.hide(); }

                 $scope.addToChangedVisualDepedencies = function(visualDependency){
                	 setVisualDependencyProperties(visualDependency);
                	 if(driversService.changedVisualDependencies.indexOf(visualDependency) == -1)
                		 driversService.changedVisualDependencies.push(visualDependency);
                 }
                 var setVisualDependencyProperties = function(visualDependency){
                	 var driverIndex = $scope.driversService.driversOnObject.findIndex(i => i.priority ==selectedDriver.priority);
 					if($scope.driversService.driversOnObject.length > 1){
 						selectedDriver = $scope.driversService.driversOnObject[driverIndex];
 					}else selectedDriver = $scope.driversService.driversOnObject[0] ;
                	 var visualProgram;
                	 var visualObjects = driversService.visusalDependencyObjects;
                	 for(var i = 0; i<visualObjects.length;i++){
                		 if(visualDependency== visualObjects[i])
                			 visualProgram = i+1;
                	 }
                	 visualDependency.prog = visualProgram;
                	 if( !$scope.document.hasOwnProperty('modelLocked')){
	                	 visualDependency.objParFatherId = selectedDriver.id;
	                	 visualDependency.objParFatherUrlName = selectedDriver.parameterUrlName;
                	 }else{
                		 visualDependency.metaModelParFatherId = selectedDriver.id;
                    	 visualDependency.metaModelParFatherUrlName = selectedDriver.parameterUrlName;
                	 }
                 }
                 $scope.getDriverNameById = function(visualDependency){
	            	  for(var i = 0; i< drivers.length;i++){
	            	  if(visualDependency.objParId == drivers[i].id)
	            		  return drivers[i].label;
	            	  }
	               }
             }
             function CorrelationDataDialogController($scope, DriversService, selectedDriver, selectedDataCondition, $mdDialog) {

            	 $scope.translate = sbiModule_translate;
            	 var selectedConditionIndex = selectedDataCondition;
            	 $scope.driversService = DriversService;
                 $scope.document = driversService.driverRelatedObject;// /****************************
                 $scope.drivers = [];
                 $scope.paruseColumns = {};
                 angular.copy(driversService.driversOnObject,  $scope.drivers);
                 $scope.selectedDriver = selectedDriver;
                 $scope.driverName = $scope.selectedDriver.label;
                 $scope.analyticalDrivers = driversService.analyticalDrivers;
                 $scope.lovIdAndColumns = driversService.lovIdAndColumns;
                 $scope.paruses = driversService.driverParuses;
                 $scope.dataDependencyModel = {};
                 $scope.selectedDataCondition = driversService.dataDependencyObjects[selectedDataCondition];
                 $scope.selectedDataCondition.persist ={};
                 $scope.selectedDataCondition.persist[(driversService.driverParuses.filter(par => par.useID == $scope.selectedDriver.parID))[0].useID] = true;

                 $scope.paruseColumns[$scope.selectedDataCondition.paruseId] = $scope.selectedDataCondition.filterColumn;
                 $scope.driversService.paruseColumns = $scope.paruseColumns;


                 $scope.availableOperators = ['>','>=','<','<=','=', 'contains','notcontains','starts with','ends with'];
                 driversService.selectedDataCondition = driversService.dataDependencyObjects[selectedDataCondition];
                 $scope.dataModes = {};
                 $scope.countParuses = function(){
                	 var counter = 0;
                	 for(var i = 0; i < $scope.paruses.length; i++){
                		 if(selectedDriver.parID == $scope.paruses[i].id)
                			 counter++;

                	 }
                	 return counter;
                 }

                 for( var j = 0; j< $scope.drivers.length; j++){
                	 if($scope.drivers[j].label == selectedDriver.label){
                		 $scope.drivers.splice(j,1);
                	 }
                 }
                 $scope.close = function(selectedCondition) {
                	 for(var i = 0; i < driversService.dataDependencyObjects.length;i++){
	                	 if( $scope.selectedDataCondition.newDependency && selectedConditionIndex == i)
	                		 driversService.dataDependencyObjects.splice(i, 1);

                	 }
                	 $mdDialog.cancel();
                }
                 $scope.hide = function() { setDataDependencyProperties($scope.selectedDataCondition); $mdDialog.hide(); }
                 $scope.addToChangedDataDepedencies = function(dataDependency,driver){
                	 setDataDependencyProperties(dataDependency)
                	 if(driversService.changedDataDependencies.indexOf(dataDependency) == -1)
                		 driversService.changedDataDependencies.push(dataDependency);
                 		}
                 $scope.getLovColumnsForParuse = function(paruse){
                	 for(var i = 0; i < driversService.lovIdAndColumns.length;i++){
                		 if(paruse.idLov == driversService.lovIdAndColumns[i].id)
                			 return driversService.lovIdAndColumns[i].columns;
                	 }

                 }
                 $scope.getFilterColumns = function(obj){
                 for (var property in obj) {
                	    if (obj.hasOwnProperty(property)) {

                	    }
                	}
                 }
                 var setDataDependencyProperties = function(dataDependency){
                	 var driverIndex = $scope.driversService.driversOnObject.findIndex(i => i.priority ==selectedDriver.priority);
  					if($scope.driversService.driversOnObject.length > 1){
  						selectedDriver = $scope.driversService.driversOnObject[driverIndex];
  					}else selectedDriver = $scope.driversService.driversOnObject[0] ;
                	 var dataProgram;
                	 var dataObjects = driversService.dataDependencyObjects;
                	 for(var i = 0; i<dataObjects.length;i++){
                		 if(dataDependency== dataObjects[i])
                			 dataProgram = i+1;
                	 }
                	 dataDependency.prog = dataProgram;
                	 dataDependency.paruseId=selectedDriver.parID;
                	 if( !$scope.document.hasOwnProperty('modelLocked')){
	                	 dataDependency.objParId = selectedDriver.id;
	                	 dataDependency.objParFatherUrlName = selectedDriver.parameterUrlName;
                	 }else{
                		 dataDependency.metamodelParId = selectedDriver.id;
                    	 dataDependency.metaModelParFatherUrlName = selectedDriver.parameterUrlName;
                	 }
                 }
                 $scope.getDriverNameById = function(dataDependency){
                  	  for(var i = 0; i< $scope.drivers.length;i++){
                  	  if(dataDependency.objParId == $scope.drivers[i].id)
                  		  return  $scope.drivers[i].name;
                  	  }
                  }

                }
             self.setParameterInfo = function(driver){
            	 if(driversService.analyticalDrivers){
               	 for(var i = 0 ; i<driversService.analyticalDrivers.length; i++){
               		 if((driver.parameter && driversService.analyticalDrivers[i].id==driver.parID) || (driver.parameter && driversService.analyticalDrivers[i].name==driver.parameter.name) ){
               			 driver.parameter = driversService.analyticalDrivers[i];
               		 	 driver.parID = driversService.analyticalDrivers[i].id;
               		 	driver.parameter.name = driversService.analyticalDrivers[i].name}
               	 }
                }
             }

        }]);
})();