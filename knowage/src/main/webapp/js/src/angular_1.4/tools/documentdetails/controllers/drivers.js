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
        .directive('uniqueUrlName', function() {
			return {
				require: 'ngModel',
				link: function(scope, element, attr, mCtrl) {

					function validate(value) {

						var driverUrlNames = new Set();

						for (var i in scope.dd.drivers) {

							var currDriver = scope.dd.drivers[i];
							if (currDriver.parameterUrlName) {
								var currUrl = currDriver.parameterUrlName;
								if (!driverUrlNames.has(currUrl)) {
									driverUrlNames.add(currUrl);
								}
							}

						}

						if (driverUrlNames.has(value)) {
							mCtrl.$setValidity('notUnique', false);
						} else {
							mCtrl.$setValidity('notUnique', true);
							return value;
						}

					}

					mCtrl.$parsers.push(validate);
				}
			};
        })
        .controller('DocumentDetailsDriversController',['$scope','$location','DriversService','resourceService','$httpParamSerializer', '$mdDialog','sbiModule_translate','sbiModule_messaging','$filter', 'sbiModule_config', 'kn_regex',
        										function($scope,$location,DriversService,resourceService,$httpParamSerializer, $mdDialog,sbiModule_translate,sbiModule_messaging,$filter, sbiModule_config, kn_regex){

        	 var self = this;
        	 self.regex = kn_regex;
        	 var driversService = DriversService;
        	 self.translate = sbiModule_translate;
             self.driverRelatedObject = driversService.driverRelatedObject;
             var crudService = resourceService;
             var requiredPath = "";
             var id = self.driverRelatedObject.id;
             var basePath = id + "/" + 'drivers';
             self.driverParuses = [];
             var requiredPath = "2.0/documentdetails";
             $scope.paruseColumns = {}
             self.analyticalDrivers = [];
             self.hasParuse = true;
             driversService.lovColumns=[];
             self.selectedDataCondition = driversService.selectedDataCondition;
             self.selectedVisualCondition = driversService.selectedVisualCondition;
             self.visibilityConditions = driversService.visusalDependencyObjects;
             self.dataConditions = driversService.dataDependencyObjects;
             self.drivers = [];
             self.transformedObj = {};

             driversService.transformingCorrelations = function(correlations, transformKey, fromPost) {
            	 if(self.transformedObj.hasOwnProperty(transformKey) && !fromPost) {
            		 delete self.transformedObj[transformKey];
            	 }
            	 for (var i = 0; i < correlations.length; i++){
        			var fatherIdfilterOperation = correlations[i].parFatherId+correlations[i].filterOperation;

        			if(self.transformedObj[fatherIdfilterOperation]==undefined){
        				self.transformedObj[fatherIdfilterOperation] = [];
        			}
        			if(correlations[i].id && correlations[i].deleteItem==undefined){
            			self.transformedObj[fatherIdfilterOperation].push (correlations[i]);
        			}
            	 }
            	 if(self.transformedObj[fatherIdfilterOperation]!=undefined && self.transformedObj[fatherIdfilterOperation].length==0)
            		 delete self.transformedObj[fatherIdfilterOperation]

            	 return self.transformedObj;
             }

             driversService.removeFromTransformedObj = function(transformKey) {
            	 if(self.transformedObj.hasOwnProperty(transformKey)) {
        			 delete self.transformedObj[transformKey];
        		 }
             }

             self.isEmpty = function(obj) {
            	  for(var prop in obj) {
            	    if(obj.hasOwnProperty(prop)) {
            	      return false;
            	    }
            	  }
            	  return JSON.stringify(obj) === JSON.stringify({});
             }

             if(self.driverRelatedObject == {} && driversService.driversOnObject.length == 0) {
            	 if(self.drivers.length == 0) {
		            crudService.get(requiredPath,basePath).then(function(response) {
		            	driversService.driversOnObject = response.data;
		            	self.drivers = driversService.driversOnObject;
		            	self.driversNum = self.drivers.length > 1;
		             });
            	 }
             } else {
            	 if(self.driverRelatedObject.engine) {
            		 self.drivers = self.driverRelatedObject.drivers;
            		 self.driversNum = self.drivers != null && self.drivers.length > 1;
            	 } else {
            		 requiredPath = "2.0/businessmodels";
            	 }
             }

             driversService.renderedDrivers = self.drivers;
             self.required = true;

             var setSelectedDriver = function(driver){
            	 self.selectedDriver = driver;
             }

             driversService.setSelectedDriver = setSelectedDriver;

             var getDriverNames = function(analyticalDrivers){
            	 var driverNames=[];
		         if(driversService.driverRelatedObject.id){
		        	 for(var i = 0; i< analyticalDrivers.length;i++){
		        		 driverNames.push(analyticalDrivers[i].label);
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
		        	 self.transformedObj = {};
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
            	 self.setInfoForChangedDriver(driver);
            	 if(driversService.changedDrivers.indexOf(driver) == -1)
            		 driversService.changedDrivers.push(driver);
             }

			driversService.loadingDriversOnBM = function(selectedBusinessModel) {
				self.driverRelatedObject = selectedBusinessModel;
				requiredPath = "2.0/businessmodels";
				driversService.fillAllDriversPerModel(requiredPath, selectedBusinessModel);
				driversService.driverRelatedObject = self.driverRelatedObject;
				self.selectedDriver = undefined;
				self.drivers = driversService.renderedDrivers;
			}

			if(!self.driverRelatedObject.engine) {
				driversService.loadingDriversOnBM(driversService.driverRelatedObject);
			}

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

             driversService.getDataDependenciesByDriverId = function(requiredPath,basePath,driver){
            	 crudService.get(requiredPath,basePath).then(function(response){
            		 driversService.dataDependencyObjects[driver.id] = response.data;
            		 self.dataConditions = driversService.dataDependencyObjects;
            		 self.transformedObj = {};
            		 driversService.transformingCorrelations(response.data);
               	});
             }

             self.selectDriver = function(priority) {
            	 if( self.analyticalDrivers.length == 0)
            		 self.analyticalDrivers = getDriverNames(driversService.analyticalDrivers);
            	 var querryParams = "";
            	 var basePath = driversService.visualDependencies;
            	 var baseDataPath = driversService.dataDependenciesName;
                 for (var i in self.drivers) {
                	 if (self.drivers[i].priority == priority) {
                    	self.setParameterInfo(self.drivers[i]);
                        self.selectedDriver = self.drivers[i];
						if(self.selectedDriver.parID) {
							driversService.getParusesByAnaliticalDriverId(self.selectedDriver.parID);
							driversService.getLovsByAnalyticalDriverId(self.selectedDriver.parID);
						}
                     	if(self.selectedDriver.id ){
	                        querryParams = setQuerryParameters(self.selectedDriver.id);
	                        basePath = self.driverRelatedObject.id + "/" + basePath + querryParams;
	                        baseDataPath = self.driverRelatedObject.id + "/" + baseDataPath + querryParams;
//			                if (!(driversService.visusalDependencyObjects[self.selectedDriver.id] && driversService.visusalDependencyObjects[self.selectedDriver.id].length == 0))
	                        getVisualDependenciesByDriverId(requiredPath,basePath, self.selectedDriver);
//			                if(!(driversService.dataDependencyObjects[self.selectedDriver.id]  && driversService.dataDependencyObjects[self.selectedDriver.id].length == 0))
	                        driversService.getDataDependenciesByDriverId(requiredPath,baseDataPath, self.selectedDriver);
	                        break;
                        }
                        return;
                     }
                     self.selectedDriver = self.drivers[self.drivers.length - 1];
                 }
                 self.driversNum = self.drivers.length > 1;
             }

             self.deleteDriver = function(driver,name) {
                 var index;
                 for (var i = 0; i< self.drivers.length;i++) {
                     if (self.drivers[i].id == driver.id) {
                        	// delete self.selectedDriver;
                    	 index = i;
                    	 if(!driver.newDriver) {
                    		 driversService.driversForDeleting.push(driver);
                    	 }
                    	 self.drivers.splice(i, 1);
                     }
                 }
//                 for (var i = 0; i< driversService.renderedDrivers.length;i++) {
//                	 if (driversService.renderedDrivers[i].id == driver.id)
//                		 driversService.renderedDrivers.splice(i,1);
//                 }
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
            	 var tempParameter = selectedDriver.parameter;
            	 var labelSelected = selectedDriver.label
                 $scope.selectedDriver =$filter('filter')(driversService.driversOnObject,{label:labelSelected})[0];
            	 $scope.selectedDriver.parameter = tempParameter;
                 self.selectedDriver = $scope.selectedDriver
                 if (!selectedCondition && selectedCondition != 0) {
                     if (driversService.visusalDependencyObjects[ $scope.selectedDriver.id]) {
                    	 driversService.visusalDependencyObjects[ $scope.selectedDriver.id].push({'newDependency':'true'})
                         self.visibilityConditions = driversService.visusalDependencyObjects;
                     }else{
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
                     templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/documentdetails/templates/correlationDialog.html',
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

             self.editDataCondition = function(ev, selectedDriver, transformKey) {
            	 $scope.selectedDriver = selectedDriver;
                 selectedDataCondition = angular.copy(self.transformedObj[transformKey]);
                 $mdDialog.show({
                     controller: CorrelationDataDialogController,
                     templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/documentdetails/templates/correlationDataDialog.html',
                     targetEvent: ev,
                     clickOutsideToClose: true,
                     locals: {
                    	 columns: $scope.paruseColumns,
                         selectedDriver: $scope.selectedDriver,
                         selectedDataCondition: selectedDataCondition,
                         getLovColumns: $scope.getLovColumnsForParuse,
                         transformKey: transformKey
                     }
                 })
                 .then(
                     function(answer) {},
                     function() {});
             };

             self.deleteVisualCondition = function(index,name,driver) {
            	 driversService.visualDependenciesForDeleting.push(self.visibilityConditions[driver.id][index]);
            	 self.visibilityConditions[driver.id].splice(index, 1);
             }

             self.deleteDataCondition = function(transformKey) {
            	angular.copy(self.transformedObj[transformKey], driversService.dataDependenciesForDeleting);
            	driversService.deleteDataDependencies(id, requiredPath);
            	driversService.removeFromTransformedObj(transformKey);
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
                 $scope.drivers = $scope.driversService.driversOnObject;
                 var selectedDriverName = selectedDriver.label;
                 $scope.selectedDriver = $filter('filter')(driversService.renderedDrivers,{label:selectedDriverName})[0];
                 $scope.selectedCondition = driversService.visusalDependencyObjects[selectedDriver.id][selectedCondition];
                 $scope.availableOperators = ['contains','not contains'];
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
            		 if(paruse.idLov == driversService.lovIdAndColumns[i].id){
            			 if(driversService.lovIdAndColumns[i].columns != undefined){
            				 return driversService.lovIdAndColumns[i].columns;
            			 }else
            				 return ["VALUE","DESCRIPTION"]
            		 }
            	 }
             }

             function CorrelationDataDialogController($scope,columns,DriversService, selectedDriver, selectedDataCondition, getLovColumns, transformKey, $mdDialog) {

            	 $scope.translate = sbiModule_translate;
            	 var selectedConditionIndex = selectedDataCondition;
            	 $scope.driversService = DriversService;
                 $scope.drivers = [];
                 angular.copy(driversService.renderedDrivers,  $scope.drivers);
                 $scope.selectedDriver = selectedDriver;
                 $scope.driverName = $scope.selectedDriver.label;
                 $scope.analyticalDrivers = driversService.analyticalDrivers;
                 $scope.lovIdAndColumns = driversService.lovIdAndColumns;
                 $scope.paruses = driversService.driverParuses;
                 $scope.dataDependencyModel = {};
                 $scope.selectedDataCondition = selectedDataCondition;
                 $scope.useModeIds = {};
                 $scope.availableOperators = ['equal','greater','greaterequal','less','lessequal', 'contains','not contains','starts with','ends with'];
      			 $scope.connectingOperators = ['AND','OR'];

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

     			$scope.driversService.paruseColumns = $scope.paruseColumns;

     			if(!selectedDataCondition){
     				$scope.selectedDataCondition = [{}]
     				$scope.selectedDataCondition[0].parFatherId = $scope.drivers[0].id;
     				$scope.selectedDataCondition[0].filterOperation = $scope.availableOperators[0];
     				$scope.selectedDataCondition[0].logicOperator = $scope.connectingOperators[0];
     				$scope.useModeIds[$scope.dependencyParuses[0].useID]=true;
     				for(var j = 0; j < $scope.dependencyParuses.length;j++){
     				    $scope.paruseColumns[$scope.selectedDriver.id][$scope.dependencyParuses[j].useID] = getLovColumns($scope.dependencyParuses[j])[0];
     				}
     			}else {
         			for(var j = 0; j < $scope.selectedDataCondition.length;j++){
         				$scope.useModeIds[$scope.selectedDataCondition[j].useModeId]=true;
         				$scope.paruseColumns[ $scope.selectedDriver.id][$scope.selectedDataCondition[j].useModeId] = $scope.selectedDataCondition[j].filterColumn;
         			}
     			}

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
                		if(paruse.idLov && paruse.idLov == driversService.lovIdAndColumns[i].id)
                			return driversService.lovIdAndColumns[i].columns;
                	}
                	return ["VALUE","DESCRIPTION"]
                 }

                 var setDataDependencyProperties = function(){
                	 var selectedDataConditionUseModeIds = $scope.selectedDataCondition.map(function(item) { return item.useModeId; });
                	 //adding new correlation on button and checkbox
                	 var counter = 0;
                	 for(var propt in $scope.useModeIds){
                		 if($scope.useModeIds[propt] && (selectedDataConditionUseModeIds.indexOf(parseInt(propt))==-1 || !$scope.selectedDataCondition[0].hasOwnProperty("useModeId"))){
                			 var obj = {};
                    		 angular.copy($scope.selectedDataCondition[0], obj);
                    		 obj.useModeId = parseInt(propt)
                    		 obj.filterColumn =  $scope.paruseColumns[$scope.selectedDriver.id][parseInt(propt)];
                    		 delete obj.id;

            				 obj.prog = counter;
            				 counter++;
            				 obj.parId = $scope.selectedDriver.id;
                    		 obj.newDependency = true;
                    		 if(!$scope.selectedDataCondition[0].hasOwnProperty("useModeId")) {
                    			 $scope.selectedDataCondition = [];
                    		 }
                    		 $scope.selectedDataCondition.push(obj);
                		 }
                	 }

                	 var dataObjects = driversService.dataDependencyObjects[$scope.selectedDriver.id];

                	 for(var i = 0; i < $scope.selectedDataCondition.length; i++) {
                		if($scope.useModeIds.hasOwnProperty($scope.selectedDataCondition[i].useModeId) && !$scope.useModeIds[$scope.selectedDataCondition[i].useModeId]) {
                			driversService.dataDependenciesForDeleting.push($scope.selectedDataCondition[i])
                			$scope.selectedDataCondition[i].deleteItem = true
                    	}

            			$scope.selectedDataCondition[i].filterOperation = $scope.selectedDataCondition[0].filterOperation;
            			$scope.selectedDataCondition[i].parFatherId = $scope.selectedDataCondition[0].parFatherId;
            			$scope.selectedDataCondition[i].logicOperator = $scope.selectedDataCondition[0].logicOperator;

            			$scope.selectedDataCondition[i].filterColumn = $scope.paruseColumns[$scope.selectedDriver.id][$scope.selectedDataCondition[i].useModeId];
            			$scope.selectedDataCondition[i].parFatherUrlName = $filter('filter')($scope.drivers,{id:$scope.selectedDataCondition[0].parFatherId})[0].parameterUrlName;
            			if($scope.selectedDataCondition[i].deleteItem!=true){
                			driversService.changedDataDependencies.push($scope.selectedDataCondition[i]);
            			}

            		 }
                	 if(!transformKey && driversService.changedDataDependencies[0]) {
                		 transformKey = driversService.changedDataDependencies[0].parFatherId+driversService.changedDataDependencies[0].filterOperation;
                	 }
                	 driversService.persistDataDependency(id, requiredPath);

                	 driversService.transformingCorrelations($scope.selectedDataCondition, transformKey, false);
                	 if(driversService.dataDependenciesForDeleting.length > 0) {
                		 driversService.deleteDataDependencies(id, requiredPath);
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
		           			 driver.parameter =angular.copy( driversService.analyticalDrivers[i]);
		           		 	 driver.parID = driversService.analyticalDrivers[i].id;
		           		 }
		           	}
	            }
            }

             self.setInfoForChangedDriver = function(driver){
            	 if(driversService.analyticalDrivers){
	               	 for(var i = 0 ; i < driversService.analyticalDrivers.length; i++){
	               		 if(driver.parameter && driversService.analyticalDrivers[i].label==driver.parameter.label){
	               			 driver.parameter = angular.copy(driversService.analyticalDrivers[i]);
	               		 	 driver.parID = driversService.analyticalDrivers[i].id;
	               		 }
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