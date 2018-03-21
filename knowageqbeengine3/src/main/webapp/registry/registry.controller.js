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
								'regFilterGetData', 'sbiModule_messaging','sbiModule_translate', '$mdDialog', '$filter',
					RegistryController])

	function RegistryController(registryConfigService, registryCRUDService,
			regFilterGetData, sbiModule_messaging,sbiModule_translate, $mdDialog, $filter) {
		var self = this;
		var registryConfigurationService = registryConfigService;
		var registryCRUD = registryCRUDService;
		var registryConfiguration = registryConfigurationService
				.getRegistryConfig();
		var columnsInfo = registryConfiguration.columns;
		var sbiMessaging = sbiModule_messaging;
		 self.sbiTranslate =sbiModule_translate;
		self.data = [];
		self.resultsNumber = 0;
		self.comboColumnOptions = {};
		self.columns = [];
		self.columnSizeInfo = [];
		self.selectedRow = [];
		self.formParams = {};
		self.filters = {};
		self.page = 1;

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
        	registryCRUD.read(formParameters).then(function(response) {
	           	 self.data = response.data.rows;
	           	 self.resultsNumber = response.data.results;

	           	 if(self.columnSizeInfo.length == 0) {
	           		self.columnSizeInfo = response.data.metaData.columnsInfos;
	           		addColumnsInfo();
	           	 }
	         });
        };

        loadInitialData();

		self.setSelected = function(selectedRow) {
			if((self.selectedRow).indexOf(selectedRow) === -1){
				self.selectedRow.push(selectedRow);
			}

		};

		// Filling columns
		var addColumnsInfo = function() {
			columnsInfo.forEach(function(column) {
				self.columnSizeInfo.forEach(function(columnSize) {
					if(columnSize.sizeColumn === column.field) {
						column.size = columnSize.size;
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

		/* Pivot Table */
		self.setRowspan = function(rows,index,colIndex,columns){

            // count columns to be merged
            var counter = 0;
            if(columns[colIndex].type && columns[colIndex].type != 'merge'){
            	return counter;
            }else if(colIndex>0 && columns[colIndex-1].type != 'merge'){
            	return counter++;
            }
                for(var j = index; j<rows.length; j++){

                    // Defining variables
                        var columnField=columns[colIndex].field;
                        var row = rows[j];
                        var previousColumnField = columns[0].field;

                        if(rows.length >j+1){
                            var nextRow = rows[j+1];
                        }else{
                            counter++;
                            return counter;
                        }
                        var field = row[columnField];
                        var previousField = row[previousColumnField];
                        var NextRowField = nextRow[columnField];
                        var NextRowPreviousField = nextRow[previousColumnField];
                        var mergeCounter = 0 ;

                        //Counting how many column pairs are same in comparing rows
                            for(var i = 0 ; i <= colIndex;i++){
                                if(row[columns[i].field] == nextRow[columns[i].field]){
                                    mergeCounter++;
                                }
                            }

                            //Checking are all column pairs same in compared rows if yes compare next row , else return counter as a rowspan
                             if(mergeCounter == colIndex+1 ){
                                counter++;
                             } else{
                                counter++;
                                return counter;
                             }
                }
        };

	    self.checkColumnBefore = function(rows,index,colIndex,columns){

	    	 if(columns[colIndex].type && columns[colIndex].type != 'merge'){
	            	return true;
	            }else if(colIndex>0 && columns[colIndex-1].type != 'merge'){
	            	return true;
	            }

	        for(var j = index; j<rows.length; j++){

	            // Defining variables
	            var row = rows[j];
	            var previousRow ;
	            var columnField=columns[colIndex].field;
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
	            var mergeCounter = 0;

	            //Counting how many column pairs are same in comparing rows
	            for(var i = 0 ; i <= colIndex;i++){
	                if(row[columns[i].field] == previousRow[columns[i].field]){
	                    mergeCounter++;
	                }
	            }

	            //Checking are the conditions for printing column value fulfilled

	            //if it is first column and values are same with comparing field do not print because it is already merged
	            if(colIndex==0 && fieldValue == previousRowFieldValue){
	                return false;

	                //For columns that are not first
	            }else if (colIndex>0){
	            	//mergeCounter represent number of column pairs of two comparing rows that have the same value
	            	//if all column pairs of comparing values are same it means that row before has rowspan and we should not show field value
	                if(mergeCounter == colIndex +1 ){
	                    return false;

	                    //if previous column is not merged ,also this one shoudn't be , so we show the value of the field
	                }else if(fieldValue != previousRowFieldValue && previousFieldValue == previousRowPreviousFieldValue ){
	                    return true;
	                    //if some previous column is not merged ,and this one have the same value with comparing column,also this one shoudn't be , so we show the value of the field
	                }else if(fieldValue == previousRowFieldValue && mergeCounter != colIndex +1 ){
	                    return true;
	                }else if(fieldValue != previousRowFieldValue){
	                    return true;
	                }
	            }else{
	                return true;
	            }

	        }
	    };

	  //Adding options to combo columns
        self.addColumnOptions = function(columnField, columnEditor, row, $mdOpenMenu) {
            $mdOpenMenu();
            row.selected = true;

            if(columnEditor === 'COMBO' && !self.comboColumnOptions[columnField]) {
                var promise = regFilterGetData.getData(columnField);
                promise.then(function(response) {
                    self.comboColumnOptions[columnField] = response;
                });
                return promise;
            }
        };
        
		//Filters handling
		self.addFilterOptions = function(filterField){
			var promise = regFilterGetData.getData(filterField);
			promise.then(function(response) {
				addOptions(filterField,response);
			});
		}

		var addOptions = function(filterField,options) {
			var filter =  $filter('filter')(registryConfiguration.filters,{field:filterField}, true)[0];
			filter.options = options;
		};


		self.loadFilteredData = function(params) {
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

        self.clearFilterValues = function(){
			self.filters = {};
			self.formParams = {};
			self.page = 1;
			for(var i = 0 ; i<(self.configuration.filters).length; i++){
            	self.configuration.filters[i].value = null;
			}
			loadInitialData(self.formParams);
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
		self.getTotalPages = function() {
            return new Array(Math.ceil(self.resultsNumber / self.configuration.itemsPerPage));
        };

        self.hasNext = function() {
            return self.page * self.configuration.itemsPerPage < self.resultsNumber;
        };

        self.hasPrevious = function() {
            return self.page > 1;
        };

        self.min = function() {
            return self.resultsNumber > 0 ? (self.page - 1) * self.configuration.itemsPerPage + 1 : 0;
        };

        self.max = function() {
            return self.hasNext() ? (self.page * self.configuration.itemsPerPage) : self.resultsNumber;
        };


        self.next = function() {
        	self.page++;
        	self.formParams.start = (self.page - 1) * self.configuration.itemsPerPage;
        	self.formParams.limit = self.configuration.itemsPerPage;
        	var filterFields = Object.keys(self.filters);
        	var filterValues = Object.values(self.filters);

        	for(var i = 0; i < filterFields.length; i++) {
        		self.formParams[filterFields[i]] = filterValues[i];
        	}
            readData(self.formParams);
        };

        self.previous = function() {
        	self.page--;
        	self.formParams.start = (self.page - 1) * self.configuration.itemsPerPage;
        	self.formParams.limit = self.configuration.itemsPerPage;
        	var filterFields = Object.keys(self.filters);
        	var filterValues = Object.values(self.filters);

        	for(var i = 0; i < filterFields.length; i++) {
        		self.formParams[filterFields[i]] = filterValues[i];
        	}
            readData(self.formParams);
        };

        self.goToPage = function() {
        	self.formParams.start = (self.page - 1) * self.configuration.itemsPerPage;
        	self.formParams.limit = self.configuration.itemsPerPage;
        	var filterFields = Object.keys(self.filters);
        	var filterValues = Object.values(self.filters);

        	for(var i = 0; i < filterFields.length; i++) {
        		self.formParams[filterFields[i]] = filterValues[i];
        	}
            readData(self.formParams);
        };
        
        self.checkIfSelected = function(row) {

        	if((self.selectedRow).indexOf(row) !== -1 ){
        		return 'blue';
        	}

        }
	}
})();