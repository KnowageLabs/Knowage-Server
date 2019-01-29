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
             var crudService = resourceService;
             var requiredPath = "";
             var id = self.driverRelatedObject.id;
             var basePath = id + "/" + 'drivers';
             self.driverParuses = [];
             var requiredPath = "2.0/documents1";
             $scope.paruseColumns = {}
             self.analyticalDrivers = [];
             self.hasParuse = true;
             self.driversPerModel = driversService.driversPerModel;
             driversService.lovColumns=[];
             self.selectedDataCondition = driversService.selectedDataCondition;
             self.selectedVisualCondition = driversService.selectedVisualCondition;
             self.visibilityConditions = driversService.visusalDependencyObjects;
             self.dataConditions = driversService.dataDependencyObjects;

             if(self.driverRelatedObject == {} && driversService.driversOnObject.length == 0){

            	 if(self.drivers.length == 0 ){

            crudService.get(requiredPath,basePath).then(function(response){
            	 driversService.driversOnObject = response.data;
            	 self.drivers=driversService.driversOnObject;
            	 self.driversNum = self.drivers.length > 1;
             });

            	 }
             }else {

            	 if(self.driverRelatedObject.engine){self.drivers = driversService.driversOnObject; self.driversNum = self.drivers.length > 1;}
            	 else{
            		  requiredPath = "2.0/businessmodels";
            		  self.drivers =$filter('filter')(driversService.driversPerModel, {biMetaModelID: self.driverRelatedObject.id},true);self.driversNum = self.drivers.length > 1}}
             			driversService.rederedDrivers = self.drivers;
             self.required = true;



             var setSelectedDriver = function(driver){
            	 self.selectedDriver = driver;
             }
             driversService.setSelectedDriver = setSelectedDriver;
             var getDriverNames = function(driversOnObject){
            	 var driverNames=[];
		            if(driversService.driverRelatedObject.id){
		            	 for(var i = 0; i< driversOnObject.length;i++){
		            		 driverNames.push(driversOnObject[i].name);
		            	 }
		            return driverNames;
		            	 }
             }
             self.clearSearch = function(){
            	 self.searchTerm = "";
             }
             self.addDriver = function() {
            	 if(driversService.driverRelatedObject.hasOwnProperty('modelLocked')){

         	 		if(driversService.driverRelatedObject.id){
    					 if (self.drivers) {
    	                     self.drivers.push({ 'label': '', 'priority': self.drivers.length == 0 ? 1: self.drivers.length + 1 ,'newDriver':'true',  'biMetaModelID' :driversService.driverRelatedObject.id,'visible':true,'required':true,'multivalue':false });
    	                     var index = self.drivers.length;
    	                  self.driversNum = self.drivers.length > 1;
    	                     self.selectDriver( index );
    	                 } else {
    	                     self.drivers = [{ 'label': '', 'priority': 1,'newDriver':'true','biMetaModelID' : driversService.driverRelatedObject.id ,'visible':true,'required':true,'multivalue':false}];
    	                  self.driversNum = self.drivers.length > 1;
    	                     self.selectDriver(1);

    	                 }
    				 }
          }else{
				 if(driversService.driverRelatedObject.id){
					 if (self.drivers) {
	                     self.drivers.push({ 'label': '', 'priority': self.drivers.length == 0 ? 1: self.drivers.length +1 ,'newDriver':'true',  'biObjectID' :driversService.driverRelatedObject.id,'visible':true,'required':true,'multivalue':false });
	                     var index = self.drivers.length;
	                     self.driversNum = self.drivers.length > 1;
	                     self.selectDriver( index );
	                 } else {
	                     self.drivers = [{ 'label': '', 'priority': 1,'newDriver':'true', 'biObjectID' : driversService.driverRelatedObject.id ,'visible':true,'required':true,'multivalue':false}];
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
            	 self.setParameterInfo(driver);
            	 if(driversService.changedDrivers.indexOf(driver) == -1)
            		 driversService.changedDrivers.push(driver);
             }

             $scope.$on('changedModel', function(event, data) {
            	   self.driverRelatedObject = data;
            	   driversService.renderedDrivers =  $filter('filter')(driversService.driversPerModel, {biMetaModelID: data.id},true);
            	   driversService.driverRelatedObject =  self.driverRelatedObject;
            	   self.selectedDriver = undefined;
            	   self.drivers = driversService.renderedDrivers;
            	   requiredPath = "2.0/businessmodels";
            	   self.driversNum = self.drivers.length > 1;
             });

             $scope.$on('setDocumentPath', function(event, data) {
            	 requiredPath = data;
             });

             var addParId = function(driver){
            	 driver.parID = driver.parameter.id;
             }

             var getVisualDependenciesByDriverId = function(requiredPath,basePath,driver){
            	 crudService.get(requiredPath,basePath).then(function(response){
            		 driversService.visusalDependencyObjects[driver.id] = response.data;
            		 self.visibilityConditions = driversService.visusalDependencyObjects;
               	});
             }

             var getDataDependenciesByDriverId = function(requiredPath,basePath,driver){
            	 crudService.get(requiredPath,basePath).then(function(response){
            		 driversService.dataDependencyObjects[driver.id] = response.data;
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
            	 	if(lovObject != [] && lovObject.QUERY){
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
	                     	 driversService.getParusesByAnaliticalDriverId(self.selectedDriver.parID);
	                         self.setParameterInfo(self.selectedDriver);
							 if(self.selectedDriver.parID)
	                         getLovsByAnalyticalDriverId(self.selectedDriver.parID);
	                         	if(self.selectedDriver.id ){
			                         querryParams = setQuerryParameters(self.selectedDriver.id);
			                         basePath = self.driverRelatedObject.id + "/" + basePath + querryParams;
			                         baseDataPath = self.driverRelatedObject.id + "/" + baseDataPath + querryParams;

			                         if (!(driversService.visusalDependencyObjects[self.selectedDriver.id] && driversService.visusalDependencyObjects[self.selectedDriver.id].length == 0))
			                         getVisualDependenciesByDriverId(requiredPath,basePath, self.selectedDriver);

			                         if(!(driversService.dataDependencyObjects[self.selectedDriver.id]  && driversService.dataDependencyObjects[self.selectedDriver.id].length == 0))
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
                    	 driversService.driversForDeleting.push(driver);
                    	 self.drivers.splice(i, 1);
                     }
                 }
                 for (var i = 0; i< driversService.driversPerModel.length;i++) {
                	 if (driversService.driversPerModel[i].id == driver.id)
                		 driversService.driversPerModel.splice(i,1);
                 }
                 if(self.drivers.length > 0){
	                 self.priorityOfDeletedDriver = driversService.driversForDeleting[driversService.driversForDeleting.length-1].priority;
	                 for (var d in self.drivers) {
	                     if (self.drivers[d].priority > self.priorityOfDeletedDriver) {
	                         self.drivers[d].priority--;
	                     }
	                 }
                 }
             }
             self.movePriority = function(priority, direction) {
                 var cur, next, prev;
                 for (var p in self.drivers) {
                     if (self.drivers[p].priority == priority) cur = p;
                     if (direction == 'up' && self.drivers[p].priority == priority - 1) prev = p;
                     self.addToChangedDrivers(self.drivers[p])
                     if (direction == 'down' && self.drivers[p].priority == priority + 1) next = p;
                     self.addToChangedDrivers(self.drivers[p])
                 }
                 if (direction == 'up') {
                     self.drivers[cur].priority--;
                     self.drivers[prev].priority++;
                     self.addToChangedDrivers(self.drivers[cur]);
                     self.addToChangedDrivers(self.drivers[prev]);

                 }
                 if (direction == 'down') {
                     self.drivers[cur].priority++;
                     self.drivers[next].priority--;
                     self.addToChangedDrivers(self.drivers[cur]);
                     self.addToChangedDrivers(self.drivers[prev]);
                 }
             }

             self.editCondition = function(ev, selectedDriver, selectedCondition) {
            	 var labelSelected = selectedDriver.label
                 $scope.selectedDriver =$filter('filter')(driversService.rederedDrivers,{label:labelSelected})[0] ;
                 self.selectedDriver = $scope.selectedDriver
                 if (!selectedCondition && selectedCondition != 0) {
                     if (driversService.visusalDependencyObjects[ $scope.selectedDriver.id]) {
                    	 driversService.visusalDependencyObjects[ $scope.selectedDriver.id].push({'newDependency':'true'})
                         self.visibilityConditions = driversService.visusalDependencyObjects;
                     } else {
                    	 driversService.visusalDependencyObjects[ $scope.selectedDriver.id] = [];
                    	 driversService.visusalDependencyObjects[ $scope.selectedDriver.id].push({'newDependency':'true'})
                     }
                     if(driversService.visusalDependencyObjects[ $scope.selectedDriver.id].length >0){
                         selectedCondition = driversService.visusalDependencyObjects[ $scope.selectedDriver.id].length - 1;
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
                             selectedDriver:  $scope.selectedDriver,
                             selectedCondition: selectedCondition
                         }
                     })
                     .then(
                         function(answer) {},
                         function() {});
             };

             self.editDataCondition = function(ev, selectedDriver, selectedDataCondition) {
            	 var labelSelected = selectedDriver.label
                 $scope.selectedDriver =$filter('filter')(driversService.driversOnObject,{label:labelSelected})[0] ;
                 self.selectedDriver = $scope.selectedDriver
                 if (!(angular.isNumber(selectedDataCondition))) {
                     if (!driversService.dataDependencyObjects[$scope.selectedDriver.id] || driversService.dataDependencyObjects[$scope.selectedDriver.id].length == 0) {
                    	 driversService.dataDependencyObjects[$scope.selectedDriver.id] = [];
                    	 driversService.dataDependencyObjects[$scope.selectedDriver.id].push({'newDependency':'true'})
                         self.dataConditions = driversService.dataDependencyObjects;
                    	 selectedDataCondition = 0;
                     }else if(driversService.dataDependencyObjects.length >0){
                    	 driversService.dataDependencyObjects[$scope.selectedDriver.id].push({'newDependency':'true'})
                     selectedDataCondition = driversService.dataDependencyObjects[$scope.selectedDriver.id].length - 1;
                     }
                 }else{
                	 selectedDataCondition = selectedDataCondition;
                 }

             $mdDialog.show({
                         controller: CorrelationDataDialogController,
                         templateUrl: '../js/documentdetails/templates/correlationDataDialog.html',
                         targetEvent: ev,
                         clickOutsideToClose: true,
                         locals: {
                        	 columns:$scope.paruseColumns,
                             selectedDriver: $scope.selectedDriver,
                             selectedDataCondition: selectedDataCondition,
                             getLovColumns:$scope.getLovColumnsForParuse
                         }
                     })
                     .then(
                         function(answer) {},
                         function() {});
             };

             self.deleteVisualCondition = function(index,name,driver) {
//            	 self.confirmDelete(index,name);

            	 driversService.visualDependenciesForDeleting.push(self.visibilityConditions[driver.id][index]);
            	 self.visibilityConditions[driver.id].splice(index, 1);
             }

             self.deleteDataCondition = function(index,name,driver) {
            //	 self.confirmDelete(index,name);

            	driversService.dataDependenciesForDeleting.push(self.dataConditions[driver.id][index]);
           	  	self.dataConditions[driver.id].splice(index, 1);

             }

             var setQuerryParameters = function(driverID){
            	 return "?driverId="+driverID;
             }

             function CorrelationDialogController($scope, DriversService, selectedDriver, selectedCondition, $mdDialog) {
            	 $scope.translate = sbiModule_translate;
            	 if(selectedCondition == undefined){
            		 selectedCondition = driversService.visusalDependencyObjects[selectedDriver.id].length - 1;
            	 }
            	 var selectedConditionIndex = selectedCondition;
            	 $scope.driversService = DriversService;
                 $scope.document = driversService.driverRelatedObject;
                 $scope.drivers = $scope.driversService.driversOnObject;
                 var selectedDriverName = selectedDriver.label;
                 $scope.selectedDriver = $filter('filter')(driversService.rederedDrivers,{label:selectedDriverName})[0];
                 $scope.selectedCondition = driversService.visusalDependencyObjects[selectedDriver.id][selectedCondition];
                 $scope.availableOperators = ['equal','greater', 'less', 'contains','notcontains'];
                 driversService.selectedVisualCondition = driversService.visusalDependencyObjects[selectedDriver.id][selectedCondition];
                 $scope.close = function(selectedCondition) {
                	 for(var i = 0; i < driversService.visusalDependencyObjects.length;i++){
	                	 if( $scope.selectedCondition.newDependency && selectedConditionIndex == i)
	                		 driversService.visusalDependencyObjects[selectedDriver.id].splice(i, 1);
                	 }
                	 $mdDialog.cancel(); }
                 $scope.hide = function() { setVisualDependencyProperties(driversService.selectedVisualCondition); $mdDialog.hide(); }

                 $scope.addToChangedVisualDepedencies = function(visualDependency){
                	 setVisualDependencyProperties(visualDependency);
                	 if(driversService.changedVisualDependencies.indexOf(visualDependency) == -1)
                		 driversService.changedVisualDependencies.push(visualDependency);
                 }
                 var setVisualDependencyProperties = function(visualDependency){
                	// var driverIndex = $scope.driversService.driversOnObject.findIndex(i => i.priority ==selectedDriver.priority);
                	 var driverIndex;
                	 for(var i = 0; i < $scope.driversService.driversOnObject.length;i++){
                		 if($scope.driversService.driversOnObject[i].priority == selectedDriver.priority)
                			 driverIndex = i;
                	 }

 					if($scope.driversService.driversOnObject.length > 1){
 						selectedDriver = $scope.driversService.driversOnObject[driverIndex];
 					}else selectedDriver = $scope.driversService.driversOnObject[0] ;
                	 var visualProgram;
                	 var visualObjects = driversService.visusalDependencyObjects;
                	 for(var i = 0; i<visualObjects[selectedDriver.id].length;i++){
                		 if(visualDependency == visualObjects[selectedDriver.id][i])
                			 visualProgram = i+1;
                	 }
                	 visualDependency.prog = visualProgram;
	                 visualDependency.parId = selectedDriver.id;
	                 visualDependency.parFatherUrlName = $filter('filter')($scope.driversService.driversOnObject,{id:visualDependency.parFatherId})[0].parameterUrlName;
                 }
             }

             $scope.getLovColumnsForParuse = function(paruse){
            	 for(var i = 0; i < driversService.lovIdAndColumns.length;i++){
            		 if(paruse.idLov == driversService.lovIdAndColumns[i].id)
            			 return driversService.lovIdAndColumns[i].columns;
            	 }

             }
             function CorrelationDataDialogController($scope,columns,DriversService, selectedDriver, selectedDataCondition,getLovColumns, $mdDialog) {

            	 $scope.translate = sbiModule_translate;
            	 var selectedConditionIndex = selectedDataCondition;
            	 $scope.conditionIndex = selectedDataCondition;
            	 $scope.driversService = DriversService;
                 $scope.document = driversService.driverRelatedObject;// /****************************
                 $scope.drivers = [];
                 angular.copy(driversService.driversOnObject,  $scope.drivers);
                 var labelSelected = selectedDriver.label
                 $scope.selectedDriver =$filter('filter')(driversService.driversOnObject,{label:labelSelected})[0] ;
                 $scope.driverName = $scope.selectedDriver.label;
                 $scope.analyticalDrivers = driversService.analyticalDrivers;
                 $scope.lovIdAndColumns = driversService.lovIdAndColumns;
                 $scope.paruses = driversService.driverParuses;
                 $scope.dataDependencyModel = {};
                 $scope.selectedDataCondition = driversService.dataDependencyObjects[$scope.selectedDriver.id][selectedDataCondition];

                 if(columns.length > 0){
                	 $scope.paruseColumns = columns;
                }else
                	$scope.paruseColumns = {};
                	$scope.paruseColumns[ $scope.selectedDriver.id] = {};

                 var selectedParuse = [];
     			for(var j = 0; j < $scope.driversService.driverParuses.length;j++){
     				if($scope.driversService.driverParuses[j].id == $scope.selectedDriver.parID)
     					selectedParuse.push($scope.driversService.driverParuses[j]);
     			}
     			$scope.dependencyParuses = selectedParuse;
                // var selectedParuse = driversService.driverParuses.filter(par => par.useID == $scope.selectedDriver.parID);

     			if($scope.selectedDataCondition.useModeId){
     				$scope.paruseColumns[ $scope.selectedDriver.id][$scope.selectedDataCondition.useModeId] = $scope.selectedDataCondition.filterColumn;
     				$scope.driversService.paruseColumns = $scope.paruseColumns;
     				$scope.selectedDataCondition.persist = {};
     				$scope.selectedDataCondition.persist[$scope.selectedDataCondition.useModeId] = true;
     			}else{
     				$scope.selectedDataCondition.useModeId = selectedParuse[0].useID;
     				var paruseColumnsTmp =  getLovColumns(selectedParuse[0])
     				if(paruseColumnsTmp != undefined)
     				$scope.paruseColumns[ $scope.selectedDriver.id][$scope.selectedDataCondition.useModeId] =  getLovColumns(selectedParuse[0])[0];
     				$scope.driversService.paruseColumns = $scope.paruseColumns;
     				$scope.selectedDataCondition.persist = {};
     				$scope.selectedDataCondition.persist[$scope.selectedDataCondition.useModeId] = true;
     			}

     			$scope.availableOperators = ['equal','greater','greaterequal','less','lessequal', 'contains','notcontains','starts with','ends with'];
     			driversService.selectedDataCondition = driversService.dataDependencyObjects[ $scope.selectedDriver.id][selectedDataCondition];
                 $scope.dataModes = {};
                 $scope.countParuses = function(){
                	 var counter = 0;
                	 for(var i = 0; i < $scope.paruses.length; i++){
                		 if( $scope.selectedDriver.parID == $scope.paruses[i].id)
                			 counter++;
                	 }
                	 return counter;
                 }

                 for( var j = 0; j< $scope.drivers.length; j++){
                	 if($scope.drivers[j].label ==  $scope.selectedDriver.label){
                		 $scope.drivers.splice(j,1);
                	 }
                 }
                 $scope.close = function(selectedCondition) {
                	 for(var i = 0; i < driversService.dataDependencyObjects[ $scope.selectedDriver.id].length;i++){
	                	 if( $scope.selectedDataCondition.newDependency && selectedConditionIndex == i)
	                		 driversService.dataDependencyObjects[ $scope.selectedDriver.id].splice(i, 1);

                	 }
                	 $mdDialog.cancel();
                }
                 $scope.hide = function() {

                	 setDataDependencyProperties($scope.selectedDataCondition);
                	 $mdDialog.hide();
                	 }
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

                 var setDataDependencyProperties = function(dataDependency){
                	// var driverIndex = $scope.driversService.driversOnObject.findIndex(i => i.priority ==selectedDriver.priority);

                	 var dataProgram;
                	 var dataObjects = driversService.dataDependencyObjects[$scope.selectedDriver.id];
                	 dataProgram = dataObjects.length > 0 ? dataObjects.length:1;
                	 dataDependency.prog = dataProgram;
	                 dataDependency.parId = $scope.selectedDriver.id;
	                 dataDependency.parFatherUrlName = $filter('filter')($scope.drivers,{id:dataDependency.parFatherId})[0].parameterUrlName
	                 dataDependency.filterColumn
                 }

                 $scope.isItChecked = function(index){
                 var itIsChecked = false;
                 if(index == selectedParuse.indexOf(($filter('filter')(selectedParuse,{useID:$scope.selectedDataCondition.useModeId}))[0])){
                		return !$scope.selectedDataCondition.persist[$scope.selectedDataCondition.useModeId];
                	 }
                 }


                 $scope.hasParuseColumns = function(){
                	 var hasColumns = false;
                	 for(var i=0; i< selectedParuse.length;i++){
                		 if($scope.getLovColumnsForParuse(selectedParuse[i])){
                			 return true;
                		 }
                	 }
                	 return false;
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
             self.hasDependencies = function(driverName){

            	 var driver = {}
		            for(var i = 0; i< driversService.analyticalDrivers.length;i++){
		            	if(driversService.analyticalDrivers[i].name == driverName){
		            		driver = driversService.analyticalDrivers[i]
		            	}
		            }
            	 var paruses = $filter('filter')(driversService.driverParuses, {id:driver.id},true);
            	 for(var i = 0; i < paruses.length; i++){
            		 if( $scope.getLovColumnsForParuse(paruses[i]))
            			 return true;
            	 }

             }
        }]);
})();