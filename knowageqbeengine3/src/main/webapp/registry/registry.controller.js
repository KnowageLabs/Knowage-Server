/**
 * Knowage, Open Source Business Intelligence suite Copyright (C) 2016
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Knowage is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
	'use strict';

	angular.module('RegistryDocument', [ 'ngMaterial', 'registryConfig', 'sbiModule' ])
			.config(
					[
							'$mdThemingProvider',
							'$httpProvider',
							function($mdThemingProvider, $httpProvider) {
								$mdThemingProvider.theme('knowage');
								$mdThemingProvider.setDefaultTheme('knowage');
								$httpProvider.interceptors
										.push('httpInterceptor');

							} ]).controller('RegistryController', ['registryConfigService', 'registryCRUDService',
								'regFilterGetData', 'sbiModule_messaging','sbiModule_translate', 'sbiModule_config', '$mdDialog', '$filter', 'orderByFilter','registryPaginationService',
					RegistryController])

	function RegistryController(registryConfigService, registryCRUDService,
			regFilterGetData, sbiModule_messaging, sbiModule_translate, sbiModule_config, $mdDialog, $filter, orderBy, registryPaginationService) {
		var self = this;
		var registryConfigurationService = registryConfigService;
		var registryCRUD = registryCRUDService;
		var registryConfiguration = registryConfigurationService
				.getRegistryConfig();
		var columnsInfo = registryConfiguration.columns;
		var sbiMessaging = sbiModule_messaging;
		var pagination = registryPaginationService;
		var dateColumns=[];
		self.sbiTranslate = sbiModule_translate;
		self.data = [];
		self.resultsNumber = 0;
		self.comboColumnOptions = {};
		self.columns = [];
		self.columnSizeInfo = [];
		self.columnFieldTypes = [];
		self.selectedRow = [];
		self.formParams = {};
		self.filters = {};
		self.page = 1;
		self.propertyName = '';
		self.reverse = false;
		self.dateFormat='MM/dd/yyyy';
		
		// array object to define the registry configuration
		self.configuration = {
			title: "Registry Document",
			itemsPerPage: 15,
			enableButtons: registryConfiguration.configurations[0].value == "true",
			filters: registryConfiguration.filters,
			pagination: registryConfiguration.pagination,
			pivot: false
		};

		//Getting initial data from server
        var loadInitialData = function(){
        	if(self.configuration.pagination == 'true') {
        		self.formParams.start = 0;
        		self.formParams.limit = self.configuration.itemsPerPage;
        	} else {
        		self.formParams.start = 0;
        	}
        	readData(self.formParams);
        };

        var readData = function(formParameters) {
        	 self.formatNumber= 0;
        	registryCRUD.read(formParameters).then(function(response) {        	
	           	 self.data = response.data.rows;
	           	 if(self.configuration.pagination != 'true'){
	           	 self.data = orderBy(self.data,self.propertyName,self.reverse);
	           	 }
	           	dateColumns = dateColumnsFilter(response.data.metaData.fields);
	           	self.data = dateRowsFilter(dateColumns,response.data.rows);

	           	 self.resultsNumber = response.data.results;
	           	self.initalizePagination();
	           	 if(self.columnSizeInfo.length == 0) {
	           		self.columnSizeInfo = response.data.metaData.columnsInfos;
	           		self.columnFieldTypes = response.data.metaData.fields;
	           		addColumnsInfo();
	           	 }


	         });
        };

        loadInitialData();

		// Filling columns
		var addColumnsInfo = function() {
			columnsInfo.forEach(function(column) {
				self.columnSizeInfo.forEach(function(columnSize) {
					if(columnSize.sizeColumn === column.field) {
						column.size = columnSize.size;
					}
				});

				self.columnFieldTypes.forEach(function(columnType) {
					if(columnType.name === column.field) {
						column.dataType = columnType.type;
					}
				});

				if(column.type && column.type === 'merge') {
					self.configuration.pivot = true;
				}
				if(column.visible == true) {
					column.position = columnsInfo.indexOf(column);
					self.columns.push(column);
				}
			});
		};
		
		//Sorting Columns
		self.sortBy = function(propertyName){
			if(self.configuration.pagination != 'true'){
			self.reverse = (propertyName !== null && self.propertyName === propertyName) ? !self.reverse : false;
			self.propertyName = propertyName;
		 	self.data = orderBy(self.data,self.propertyName,self.reverse);
			}
		};

		self.setDataType = function(columnDataType) {
			switch(columnDataType) {
			   case 'string':
				   return 'text';
				   break;
			   case 'int':
			   case 'float':
			   case 'decimal':
			   case 'long':
				   return 'number';
				   break;
			   case 'date':
				   return 'date';
				   break;
			   default:
				   return 'text';
			   	   break;
			}
		};

		self.getDecimalPlaces = function(colName){
            var decimalPlaces;
            var floatColumns = $filter('filter')(self.columnFieldTypes, {type: 'float'}, true);
            floatColumns.forEach(function(col){
                if(col.name == colName) {
                    var format = col.format.split(',');
                    decimalPlaces = format.length;
                }
                return decimalPlaces;
            });
        };

        self.getStep = function(dataType){
            if(dataType == 'float'){
                return '.01';
            } else if(dataType == 'int') {
                return '1';
            } else {
                return 'any';
            }
        };
        
		/* Pivot Table */
		self.setRowspan = function(rows,rowIndex,columnIndex,columns){

            // count columns to be merged
            var rowsToMergeCounter = 0;
            if( columns[columnIndex].type !== 'merge'){
            	return;
            }else if(columnIndex>0 && columns[columnIndex-1].type !== 'merge'){
            	return;
            }
            for(var j = rowIndex; j<rows.length; j++){
                    // Defining variables
	                var columnField=columns[columnIndex].field;
	                var row = rows[j];
	                var previousColumnField = columns[0].field;
	             if(rows.length >j+1){
	                  var nextRow = rows[j+1];
	             }else{
	            	 rowsToMergeCounter++;
	                  return rowsToMergeCounter;
	             }
	              	var field = row[columnField];
	                var previousField = row[previousColumnField];
	                var NextRowField = nextRow[columnField];
	                var NextRowPreviousField = nextRow[previousColumnField];
	                var mergedCounter = 0 ;
	               //Counting how many column pairs are same in comparing rows
	             for(var i = 0 ; i <= columnIndex;i++){
	                   if(row[columns[i].field] == nextRow[columns[i].field]){
	                       mergedCounter++;
	                   }
	             }
                  //Checking are all column pairs same in compared rows if yes compare next row , else return counter as a rowspan
	             if(mergedCounter == columnIndex+1 ){
	            	 rowsToMergeCounter++;
	             } else{
	            	 rowsToMergeCounter++;
	                 return rowsToMergeCounter;
	             }
             }
        };

	    self.compareRowsForRowspanPrint = function(rows,rowIndex,columnIndex,columns){

	    	 if( columns[columnIndex].type !== 'merge'){
	            	return true;
	            }else if(columnIndex>0 && columns[columnIndex-1].type !== 'merge'){
	            	return true;
	            }

	        for(var j = rowIndex; j<rows.length; j++){

	            // Defining variables
	            var row = rows[j];
	            var previousRow ;
	            var columnField=columns[columnIndex].field;
	            var previousColumnField = columns[0].field;

	            if(0 < j){
	                 previousRow = rows[j-1];
	            }else{
	                return true;
	            }

	            var fieldValue = row[columnField];
	            var previousFieldValue = row[previousColumnField];
	            var previousRowFieldValue = previousRow[columnField];
	            var previousRowPreviousFieldValue = previousRow[previousColumnField];
	            var mergedCounter = 0;
	            //Counting how many column pairs are same in comparing rows
	            for(var i = 0 ; i <= columnIndex;i++){
	                if(row[columns[i].field] == previousRow[columns[i].field]){
	                    mergedCounter++;
	                }
	            }
	            //if it is first column and values are same with comparing field do not print because it is already merged

	            if(columnIndex==0 && fieldValue == previousRowFieldValue || columnIndex>0  && mergedCounter == columnIndex +1 ){
	                return false;
	            }else{
	                return true;
	            }

	        }
	    };

	    self.setSummaryRowColor = function(rows,index,columns){
			//counter to check is there summaryFunction and type='measure' atributes
			var counter = 0;
			var summaryFunctionIndex = 0;
			for(var i = 0;i<columns.length; i++){
				if(columns[i].summaryFunction){
					counter++;
					summaryFunctionIndex = i;
				}else if( columns[i].type=='measure')
					counter++;
			}
			//if there are at least one summaryFunction and one type='measure' than do the rest of the logic
			if(counter >= 2){
                    // Defining variables
                var row = rows[index];
                    //if it is not last row set it as the nextRow else color the row because it is last
                if(rows.length >index+1){
                    var nextRow = rows[index+1];
                }else{
                    return 'blue';
                }
                 var field = row[columns[summaryFunctionIndex].field];
                 var NextRowField = nextRow[columns[summaryFunctionIndex].field];
                 	//if next field value is not equal to selected one it means that is the summary row and it could be colored
                 if(field != NextRowField ){
                    return 'blue';
                 }
			}
                return;
        };
        
        self.isItSummaryRow = function(rows,indexF,index,columns){
        		var row = rows[indexF];
        		var columnField = columns[index].field;
        		if(index > 0){
        		var previousColumnField=columns[index-1].field;
        		}else{
        			var previousColumnField = null;
        		}
        		var previousFieldValue = row[previousColumnField];
        		var fieldValue = row[columnField];

        		return  (previousFieldValue === '      ' && fieldValue !== '      ' );
        };

              
	  //Adding options to combo columns
        var clicked = 0;
        self.dependentColumns = [];
        
        self.addColumnOptions = function(column, row, $mdOpenMenu) {
            $mdOpenMenu();
            row.selected = true;
            
            //regular independent combo columns
            if(column.editor === 'COMBO' && !self.isDependentColumn(column)) {           	
            	if(!self.comboColumnOptions[column.field]) {
            		self.comboColumnOptions[column.field] = {};
            		var promise = regFilterGetData.getData(column.field);
                    promise.then(function(response) {
                        self.comboColumnOptions[column.field] = response;
                    });
                    return promise;
            	}            
            } 
            
            //dependent combo columns
            if(column.editor === 'COMBO' && self.isDependentColumn(column)) {            	            	
            	if(!self.comboColumnOptions[column.field]) {
            		self.comboColumnOptions[column.field] = {};
            		            		
            		var dependencesPromise = regFilterGetData.getDependeceOptions(column.field, column.dependsFrom, row[column.dependsFrom])
                	dependencesPromise.then(function(response) {	                		
                		self.comboColumnOptions[column.field][row[column.dependsFrom]] = response.data.rows;                		
                	});
                	return dependencesPromise;            		               	
            	} else {
            		if(!self.comboColumnOptions[column.field].hasOwnProperty(row[column.dependsFrom])) {
            			var dependencesPromise = regFilterGetData.getDependeceOptions(column.field, column.dependsFrom, row[column.dependsFrom])
                    	dependencesPromise.then(function(response) {	                		
                    		self.comboColumnOptions[column.field][row[column.dependsFrom]] = response.data.rows;                    		
                    	});
                    	return dependencesPromise;
            		}
            	}            	            	
            }
        };
                
        self.stopShow = false;
                
        self.notifyAboutDependency = function(column, event) {
        	clicked++;
        	if(clicked == 1) {
        		fillDependencyColumns(column);
        		createDialog(self.dependentColumns);
        	}        	        	
          	        	
        	if(self.dependentColumns.length != 0 && !self.stopShow) {
        		
        		$mdDialog.show(self.confirm)
        				.then(function(result){
						 self.stopShow = result;
					 }, function(result){
						 self.stopShow = result;
					 }); 
        	}        	
        };
                 
        var fillDependencyColumns = function(column) {
        	for(var i = 0; i < self.columns.length; i++) {
        		var col = self.columns[i];
        		if(col.dependsFrom === column.field) {        			
        			var dependent = col.title;
        			self.dependentColumns.push(dependent);            			
        		}
        	}
        };
        
        var createDialog = function(dependentColumns) {
        	self.confirm = $mdDialog.prompt(
        			{
    					controller: DialogController,
    					parent: angular.element(document.body),
    					templateUrl: sbiModule_config.contextName + '/registry/dependentColumnsDialog.tpl.html',
    					locals: {    						
    						dontShowAgain: self.stopShow,
    						columns: self.dependentColumns
    					},
    					targetEvent: event,
    				    clickOutsideToClose: false,
    				    preserveScope: true,
    				    fullscreen: true
        			}
        	);
        };
        
        function DialogController($scope, $mdDialog, dontShowAgain, columns) {
        	 $scope.dontShowAgain = dontShowAgain;
        	 $scope.dependentColumns = columns;
        	 
        	 $scope.closeDialog = function() {
         		 dontShowAgain = $scope.dontShowAgain;
        		 $mdDialog.hide(dontShowAgain);        		 
             }; 
             
        };
                      
        self.isDependentColumn = function(column) {
        	if(column.hasOwnProperty('dependsFrom') && column.hasOwnProperty('dependsFromEntity')) {
        		return true;
        	} else {
        		return false;
        	}
        };

		//Filters handling
		self.addFilterOptions = function(filterField){
			var promise = regFilterGetData.getData(filterField);
			promise.then(function(response) {
				addOptions(filterField,response);
			});
		};

		self.checkIfFilterColumnExists = function(){
			var filters = [];
			for (var i = 0; i < registryConfiguration.filters.length; i++) {
				var filter = registryConfiguration.filters[i];
				for (var j = 0; j < registryConfiguration.columns.length; j++) {
					var column = registryConfiguration.columns[j];
					if(filter.presentation != 'DRIVER' && filter.field == column.field){
						filters.push(filter);
					}
				}
			}
			return filters;
		};

		var addOptions = function(filterField,options) {
			var filter =  $filter('filter')(registryConfiguration.filters,{field:filterField}, true)[0];
			filter.options = options;
		};


		self.getFilteredData = function(params) {
        	self.formParams = Object.assign({}, params);
        	if(self.configuration.pagination == 'true') {
        		self.formParams.start = 0;
        		self.formParams.limit = self.configuration.itemsPerPage;
            	self.page = 1;
        	} else {
        		self.formParams.start = 0;
        	}
     	   	readData(self.formParams);
        };

        self.deleteFilterValues = function(){
			self.filters = {};
			self.formParams = {};
			self.page = 1;
			for(var i = 0 ; i<(self.configuration.filters).length; i++){
            	self.configuration.filters[i].value = null;
			}
			loadInitialData(self.formParams);
		};

		// Update
		self.setSelected = function(selectedRow) {

			if((self.selectedRow).indexOf(selectedRow) === -1){
				self.selectedRow.push(selectedRow);
			}

		};

        self.updateRow = function() {
			for (var i = 0; i < self.selectedRow.length; i++) {
				registryCRUD.update(self.selectedRow[i]).then(function(response) {});
				if (i == (self.selectedRow.length - 1)) {
					sbiMessaging.showInfoMessage( self.sbiTranslate.load("kn.registry.registryDocument.update.success")
							+' '+ (i + 1) + ' ' + self.sbiTranslate.load("kn.registry.registryDocument.row"), self.sbiTranslate.load("kn.registry.registryDocument.success"));
				}
			}
			self.selectedRow = [];
		};

		// Delete
		self.deleteRowFromDB = function(row, event) {
			var confirm = $mdDialog.confirm()
            .title(sbiModule_translate.load('kn.registry.document.delete.row'))
            .textContent(sbiModule_translate.load('kn.registry.document.delete.confirm.message'))
            .targetEvent(event)
            .ok(sbiModule_translate.load('kn.qbe.general.yes'))
            .cancel(sbiModule_translate.load('kn.qbe.general.no'));

			$mdDialog.show(confirm).then(function() {
					registryCRUD.delete(row).then(function(response) {
						sbiMessaging.showInfoMessage(self.sbiTranslate.load("kn.registry.registryDocument.delete.success"), self.sbiTranslate.load("kn.registry.registryDocument.success"));
						self.deleteRow(row.$$hashKey);
					});
				});
		};

		self.isArray = angular.isArray;

		self.deleteRow = function(hash) {
			angular.forEach(self.data, function(value, key) {
				if (value.$$hashKey == hash) {
					self.data.splice(key, 1);
					return;
				}
			});
		};

		self.addRow = function() {
			var tmpRow = angular.copy(self.data[0], {});
			for ( var i in tmpRow) {
				tmpRow[i] = "";
			};
			self.data.unshift(tmpRow);
		};

		// reordering columns function
		self.move = function(position, direction) {
			var prev, cur, next;
			if (direction == 'left') {
				angular.forEach(self.columns, function(value, key) {
					if (value.position == (position - 1))
						prev = key;
					if (value.position == (position))
						cur = key;
				})
				self.columns[cur].position--;
				self.columns[prev].position++;
			} else {
				angular.forEach(self.columns, function(value, key) {
					if (value.position == (position + 1))
						next = key;
					if (value.position == (position))
						cur = key;
				})
				self.columns[cur].position++;
				self.columns[next].position--;
			}

		}

		self.addToFilters = function(filter) {
			self.filters[filter.field] = filter.value;
		}

		//Pagination
		self.initalizePagination=function(){

			self.getTotalPages = pagination.getTotalPages(self.resultsNumber,self.configuration.itemsPerPage);
			self.hasNext = pagination.hasNext(self.page,self.configuration.itemsPerPage,self.resultsNumber);
			self.hasPrevious = pagination.hasPrevious(self.page);
			self.min = pagination.min(self.resultsNumber,self.page,self.configuration.itemsPerPage);
			self.max = pagination.max(self.page,self.configuration.itemsPerPage,self.resultsNumber);
			self.next = function() {
				 self.page++;
				 self.formParams = pagination.next(self.page,self.formParams,self.configuration.itemsPerPage,self.filters);
				 readData(self.formParams);
			};
			self.previous = function() {
				 self.page--;
				 self.formParams = pagination.previous(self.page,self.formParams,self.configuration.itemsPerPage,self.filters);
				 readData(self.formParams);
			};
			self.goToPage = function() {
				self.formParams = pagination.goToPage(self.page,self.formParams,self.configuration.itemsPerPage,self.filters);
				 readData(self.formParams);
			}
		};


        self.checkIfSelected = function(row) {

        	if((self.selectedRow).indexOf(row) !== -1 ){
        		return 'blue';
        	}
        }
        var dateColumnsFilter = function(columns){
        	var namesOfDateColumns =[];
        	for(var i = 0 ; i <columns.length ; i++){
        		if(columns[i].type === 'date'){
        			namesOfDateColumns.push(columns[i].name);
        		}
        	}
        	return namesOfDateColumns;
        }

        var dateRowsFilter= function(columnNames,rows){
        	for(var i = 0 ; i<columnNames.length ;  i++){
        		for(var j = 0 ; j < rows.length; j++){
        			rows[j][columnNames[i]]= new Date((rows[j][columnNames[i]]));//.replace(/ /g,'T')
        		}
        	}
        	return rows;
        }

	}
})();