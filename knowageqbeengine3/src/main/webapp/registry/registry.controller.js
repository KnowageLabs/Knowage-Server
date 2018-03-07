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

	angular.module('BlankApp', [ 'ngMaterial', 'registryConfig', 'sbiModule' ])
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
								'regFilterGetData', 'sbiModule_messaging', '$mdDialog',
					RegistryController])

	function RegistryController(registryConfigService, registryCRUDService,
			regFilterGetData, sbiModule_messaging, $mdDialog) {
		var self = this;
		var registryConfigurationService = registryConfigService;
		var registryCRUD = registryCRUDService;
		var registryConfiguration = registryConfigurationService
				.getRegistryConfig();
		var regGetData = regFilterGetData;
		var columnsInfo = registryConfiguration.columns;
		var sbiMessaging = sbiModule_messaging;
		self.data = [];
		self.results = 0;
//		self.filterOptions = {};
		self.columns = [];
		self.selectedColumn = [];
		self.formParams = {};
		self.filters = {};
		self.page = 1;

		// array object to define the registry configuration
		self.configuration = {
			title: "Registry Document",
			itemsPerPage: 15,
			enableButtons: registryConfiguration.configurations[0].value == "true",
			filters: registryConfiguration.filters,
			pagination: registryConfiguration.pagination
		};

		//Getting initial data from server
        var loadInitialData = function(param){
        	if(self.configuration.pagination == 'true') {
        		self.formParams.start = 0;
        		self.formParams.limit = self.configuration.itemsPerPage;
        	} else {
        		self.formParams.start = 0;
        	}
        	readData(param);
        };

        var readData = function(data) {
        	registryCRUD.read(data).then(function(response) {
	           	 self.data = response.data.rows;
	           	 self.results = response.data.results;
	         });
        };

        loadInitialData(self.formParams);
        
		self.setSelected = function(selectedColumn) {
			if((self.selectedColumn).indexOf(selectedColumn) === -1){
				self.selectedColumn.push(selectedColumn);
				}
		};

		// Filling columns
		columnsInfo.forEach(function(column) {
			column.position = columnsInfo.indexOf(column);
			self.columns.push(column);
		});

		self.filterOptions = {};
//		self.columnOptions = {};
		
		//Filters handling
		self.getFilters = function(filterField) {					
			var promise = regGetData.getData(filterField);
			promise.then(function(response) {
				self.filterOptions[filterField] = response;
				addOptions(filterField);
			});
			return promise;  								
		};

		var addOptions = function(filterField) {
			for (var i = 0; i < registryConfiguration.filters.length; i++) {
				if (registryConfiguration.filters[i].field == filterField) {
					registryConfiguration.filters[i].options = self.filterOptions[filterField];
				}
			}			
			return registryConfiguration.filters;
		};

//		var addColumnOptions = function(field) {
//			self.columns.forEach(function(column) {
//				if (column.field == field) {
//					self.columns.options = self.filterOptions[field];
//				}
//			});
//		};

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
			for (var i = 0; i < self.selectedColumn.length; i++) {
				registryCRUD.update(self.selectedColumn[i]).then(function(response) {});
				if (i == (self.selectedColumn.length - 1)) {
					sbiMessaging.showInfoMessage('You have succesufly updated '
							+ (i + 1) + ' field/s ', 'Success!!!');
				}
			}
			self.selectedColumn = [];
		};

		self.deleteRowFromDB = function(row, event) {
			var confirm = $mdDialog.confirm()
				.title('Delete Row!')
				.textContent('Are you sure you want to delete row?')
				.targetEvent(event)
				.ok('Yes')
				.cancel('No');

			$mdDialog.show(confirm).then(function() {
					registryCRUD.delete(row).then(function(response) {
						sbiMessaging.showInfoMessage('You have succesufly deleted row!', 'Success!!!');
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

		self.getTotalPages = function() {
            return new Array(Math.ceil(self.results / self.configuration.itemsPerPage));
        };

        self.hasNext = function() {
            return self.page * self.configuration.itemsPerPage < self.results;
        };

        self.hasPrevious = function() {
            return self.page > 1;
        };

        self.min = function() {
            return self.results > 0 ? (self.page - 1) * self.configuration.itemsPerPage + 1 : 0;
        };

        self.max = function() {
            return self.hasNext() ? (self.page * self.configuration.itemsPerPage) : self.results;
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

	}
})();