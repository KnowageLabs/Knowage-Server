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
        .module('DocumentDetails')
        .controller('DocumentDetailsDriversController',['$scope','DocumentService','$location','resourceService','$httpParamSerializer', '$mdDialog','sbiModule_translate','sbiModule_messaging',
        										function($scope,DocumentService,$location,resourceService,$httpParamSerializer, $mdDialog,sbiModule_translate,sbiModule_messaging){

        	 var self = this;
        	 var documentService = DocumentService;
        	 self.translate = sbiModule_translate;
             self.document = documentService.document;
             self.service = DocumentService;
           //  self.document.driversPosition = self.document.driversPosition ? self.document.driversPosition : 'right';
           //  self.availableAligns = ['right', 'left', 'top'];
             self.confirmDelete = documentService.confirmDelete;
             var crudService = resourceService;
             var requiredPath = documentService.requiredPath;
             var basePath = id + "/" + 'drivers' ;
             var id = documentService.documentId;
             self.driverParuses = [];
             self.drivers=documentAndInfo.drivers;
             documentService.lovColumns=[];
             self.setAlign = function(align) {
                 self.document.driversPosition = align;

             }
             self.selectedDataCondition = documentService.selectedDataCondition;
             self.selectedVisualCondition = documentService.selectedVisualCondition;
             self.visibilityConditions = documentService.visusalDependencyObjects;
             self.dataConditions = documentService.dataDependencyObjects;
             self.driversNum = documentService.driversNum;
             self.required =  true;
             var getDriverNames = function(drivers){
            	 var driverNames=[];
		            if(self.document.id){
		            	 for(var i = 0; i<drivers.length;i++){
		            		 driverNames.push(drivers[i].name);
		            	 }
		            	 return driverNames;
		            	 }
             }
             self.analyticalDrivers = getDriverNames(documentAndInfo.analyticalDrivers);
             var paruses = documentService.paruses;
             self.addDriver = function() {

                 if (self.drivers) {
                     self.drivers.push({ 'label': '', 'priority': self.drivers.length == 0 ? 1: self.drivers.length +1,'newDriver':'true','biObjectID':self.document.id,'visible':false,'required':false,'multivalue':false });
                   //  documentService.drivers.push({ 'label': 'New analytical driver', 'priority': self.drivers.length,'newDriver':'true','biObjectID':self.document.id,'visible':false,'required':false,'multivalue':false });
                 } else {
                     self.drivers = [{ 'label': '', 'priority': 1,'newDriver':'true','biObjectID':self.document.id ,'visible':false,'required':false,'multivalue':false}];
                  //   documentService.drivers.push({ 'label': 'New analytical driver', 'priority': self.drivers.length,'newDriver':'true','biObjectID':self.document.id,'visible':false,'required':false,'multivalue':false });
                     self.selectDriver(0);
                 }
             }
             self.openMenu=function(menu,event){
            	 menu(event);
             }

             self.addToChangedDrivers = function(driver){
            	 self.setParameterInfo(self.selectedDriver);
            	 if(documentService.changedDrivers.indexOf(driver) == -1)
            	 documentService.changedDrivers.push(driver);
             }


             var addParId = function(driver){
            	 driver.parID = driver.parameter.id;
             }

             var getVisualDependenciesByDriverId = function(requiredPath,basePath,driver){
            	 crudService.get(requiredPath,basePath).then(function(response){
               	  	 documentService.visusalDependencyObjects = response.data;
            		 self.visibilityConditions = documentService.visusalDependencyObjects;
               	});
             }

             var getDataDependenciesByDriverId = function(requiredPath,basePath,driver){
            	 crudService.get(requiredPath,basePath).then(function(response){
               	  	 documentService.dataDependencyObjects = response.data;
            		 self.dataConditions = documentService.dataDependencyObjects;

               	});
             }

             var getLovsByAnalyticalDriverId = function(driverId){
            	 var requiredPath = "2.0/analyticalDrivers";
            	 var basePath = driverId + "/lovs";
            	 crudService.get(requiredPath,basePath).then(function(response){
            		for(var i = 0;i<response.data.length;i++){
            			documentService.lovIdAndColumns.push( setLovColumns(response.data[i]));
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
            	 if(self.drivers.length==1 && documentService.drivers.length == 0){self.selectedDriver = self.drivers[0];}
            	 self.drivers = documentService.drivers;
            	  var querryParams = "";
            	  var basePath = documentService.visualDependencies;
            	  var baseDataPath = documentService.dataDependenciesName;
	                 for (var i in self.drivers) {
	                     if (self.drivers[i].priority == priority) {
	                    	 self.setParameterInfo(self.drivers[i]);
	                         self.selectedDriver = self.drivers[i];
	                         self.setParameterInfo(self.selectedDriver);
	                         getLovsByAnalyticalDriverId(self.selectedDriver.parID);
	                         if(self.selectedDriver.id){
	                         querryParams = setQuerryParameters(self.selectedDriver.id);
	                         basePath =self.document.id+"/" + basePath + querryParams;
	                         baseDataPath = self.document.id+"/" + baseDataPath + querryParams;
	                         getVisualDependenciesByDriverId(documentService.requiredPath,basePath, self.selectedDriver);
	                         getDataDependenciesByDriverId(documentService.requiredPath,baseDataPath, self.selectedDriver);
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
                        	 delete self.selectedDriver;
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
                     if (documentService.visusalDependencyObjects) {
                         documentService.visusalDependencyObjects.push({'newDependency':'true'})
                         self.visibilityConditions = documentService.visusalDependencyObjects;
                     } else {
                    	 documentService.visusalDependencyObjects = [];
                    	 documentService.visusalDependencyObjects.push({})

                     }
                     if(documentService.visusalDependencyObjects.length >0){
                         selectedCondition = documentService.visusalDependencyObjects.length - 1;
                         }else{
                        	 selectedCondition = 0;
                         }
                 }
             $mdDialog.show({
                         controller: CorrelationDialogController,
                         templateUrl: '../js/documentdetails/html/documentDetails.correlationDialog.tpl.html',
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
                     if (documentService.dataDependencyObjects) {
                         documentService.dataDependencyObjects.push({'newDependency':'true'})
                         self.dataConditions = documentService.dataDependencyObjects;

                     } else {
                    	 documentService.dataDependencyObjects = [];
                    	 documentService.dataDependencyObjects.push({})

                     }
                     if(documentService.dataDependencyObjects.length >0){
                     selectedDataCondition = documentService.dataDependencyObjects.length - 1;
                     }else{
                    	 selectedDataCondition = 0;
                     }
                 }

             $mdDialog.show({
                         controller: CorrelationDataDialogController,
                         templateUrl: '../js/documentdetails/html/documentDetails.correlationDataDialog.tpl.html',
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

             function CorrelationDialogController($scope, DocumentService, selectedDriver, selectedCondition, $mdDialog) {
            	 $scope.translate = sbiModule_translate;
            	 if(selectedCondition == undefined){
            		 selectedCondition = documentService.visusalDependencyObjects.length - 1;
            	 }
            	 var selectedConditionIndex = selectedCondition;
            	 $scope.documentService = DocumentService;
                 $scope.document = documentService.document;
                 $scope.drivers = documentService.drivers;
                 $scope.selectedDriver = selectedDriver;
                 $scope.selectedCondition = documentService.visusalDependencyObjects[selectedCondition];
                 $scope.availableOperators = ['>', '<', '=', 'contains','notcontains'];
                 documentService.selectedVisualCondition = documentService.visusalDependencyObjects[selectedCondition];
                 $scope.close = function(selectedCondition) {
                	 for(var i = 0; i < documentService.visusalDependencyObjects.length;i++){
	                	 if( $scope.selectedCondition.newDependency && selectedConditionIndex == i)
	                		 documentService.visusalDependencyObjects.splice(i, 1);
                	 }
                	 $mdDialog.cancel(); }
                 $scope.hide = function() { setVisualDependencyProperties(documentService.selectedVisualCondition); $mdDialog.hide(); }

                 $scope.addToChangedVisualDepedencies = function(visualDependency){
                	 setVisualDependencyProperties(visualDependency);
                	 if(documentService.changedVisualDependencies.indexOf(visualDependency) == -1)
                		 documentService.changedVisualDependencies.push(visualDependency);
                 }
              console.log(   window.parent.angular.element(window.frameElement))
              console.log(document)
                 var setVisualDependencyProperties = function(visualDependency){
                	 var driverIndex = $scope.documentService.drivers.findIndex(i => i.priority ==selectedDriver.priority);
 					if($scope.documentService.drivers.length > 1){
 						selectedDriver = $scope.documentService.drivers[driverIndex];
 					}else selectedDriver = $scope.documentService.drivers[0] ;
                	 var visualProgram;
                	 var visualObjects = documentService.visusalDependencyObjects;
                	 for(var i = 0; i<visualObjects.length;i++){
                		 if(visualDependency== visualObjects[i])
                			 visualProgram = i+1;
                	 }
                	 visualDependency.prog = visualProgram;
                	 visualDependency.objParFatherId = selectedDriver.id;
                	 visualDependency.objParFatherUrlName = selectedDriver.parameterUrlName;
                 }
                 $scope.getDriverNameById = function(visualDependency){
	            	  for(var i = 0; i< drivers.length;i++){
	            	  if(visualDependency.objParId == drivers[i].id)
	            		  return drivers[i].label;
	            	  }
	               }
             }
             function CorrelationDataDialogController($scope, DocumentService, selectedDriver, selectedDataCondition, $mdDialog) {

            	 $scope.translate = sbiModule_translate;
            	 var selectedConditionIndex = selectedDataCondition;
            	 $scope.documentService = DocumentService;
                 $scope.document = documentService.document;
                 $scope.drivers = [];
                 $scope.paruseColumns = {};
                 angular.copy(documentService.drivers,  $scope.drivers);
                 $scope.selectedDriver = selectedDriver;
                 $scope.driverName = $scope.selectedDriver.label;
                 $scope.analyticalDrivers = documentService.analyticalDrivers;
                 $scope.lovIdAndColumns = documentService.lovIdAndColumns;
                 $scope.paruses = documentService.driverParuses;
                 $scope.dataDependencyModel = {};
                 $scope.selectedDataCondition = documentService.dataDependencyObjects[selectedDataCondition];
                 $scope.selectedDataCondition.persist ={};
                 $scope.selectedDataCondition.persist[(documentService.driverParuses.filter(par => par.useID == $scope.selectedDriver.parID))[0].useID] = true;

                 $scope.paruseColumns[$scope.selectedDataCondition.paruseId] = $scope.selectedDataCondition.filterColumn;
                 $scope.documentService.paruseColumns = $scope.paruseColumns;


                 $scope.availableOperators = ['>','>=','<','<=','=', 'contains','notcontains','starts with','ends with'];
                 documentService.selectedDataCondition = documentService.dataDependencyObjects[selectedDataCondition];
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
                	 for(var i = 0; i < documentService.dataDependencyObjects.length;i++){
	                	 if( $scope.selectedDataCondition.newDependency && selectedConditionIndex == i)
	                		 documentService.dataDependencyObjects.splice(i, 1);

                	 }
                	 $mdDialog.cancel();
                }
                 $scope.hide = function() { setDataDependencyProperties($scope.selectedDataCondition); $mdDialog.hide(); }
                 $scope.addToChangedDataDepedencies = function(dataDependency,driver){
                	 setDataDependencyProperties(dataDependency)
                	 if(documentService.changedDataDependencies.indexOf(dataDependency) == -1)
                		 documentService.changedDataDependencies.push(dataDependency);
                 		}
                 $scope.getLovColumnsForParuse = function(paruse){
                	 for(var i = 0; i < documentService.lovIdAndColumns.length;i++){
                		 if(paruse.idLov == documentService.lovIdAndColumns[i].id)
                			 return documentService.lovIdAndColumns[i].columns;
                	 }

                 }
                 $scope.getFilterColumns = function(obj){
                 for (var property in obj) {
                	    if (obj.hasOwnProperty(property)) {

                	    }
                	}
                 }
                 var setDataDependencyProperties = function(dataDependency){
                	 var driverIndex = $scope.documentService.drivers.findIndex(i => i.priority ==selectedDriver.priority);
  					if($scope.documentService.drivers.length > 1){
  						selectedDriver = $scope.documentService.drivers[driverIndex];
  					}else selectedDriver = $scope.documentService.drivers[0] ;
                	 var dataProgram;
                	 var dataObjects = documentService.dataDependencyObjects;
                	 for(var i = 0; i<dataObjects.length;i++){
                		 if(dataDependency== dataObjects[i])
                			 dataProgram = i+1;
                	 }
                	 dataDependency.prog = dataProgram;
                	 dataDependency.objParId = selectedDriver.id;
                	 dataDependency.objParFatherUrlName = selectedDriver.parameterUrlName;
                	 dataDependency.paruseId=selectedDriver.parID;
                 }
                 $scope.getDriverNameById = function(dataDependency){
                  	  for(var i = 0; i< $scope.drivers.length;i++){
                  	  if(dataDependency.objParId == $scope.drivers[i].id)
                  		  return  $scope.drivers[i].name;
                  	  }
                  }

                }
             self.setParameterInfo = function(driver){
            	 if(documentAndInfo.analyticalDrivers){
               	 for(var i = 0 ; i<documentAndInfo.analyticalDrivers.length; i++){
               		 if(documentAndInfo.analyticalDrivers[i].id==driver.parID){
               			 driver.parameter = documentAndInfo.analyticalDrivers[i];
               		 	 driver.parID = documentAndInfo.analyticalDrivers[i].id;
               		 	driver.parameter.name = documentAndInfo.analyticalDrivers[i].name}
               	 }
                }
             }

        }]);
})();