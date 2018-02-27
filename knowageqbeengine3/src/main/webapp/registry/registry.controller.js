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

							} ]).controller('RegistryController',
					RegistryController)

	function RegistryController(registryConfigService, registryCRUDService,
			regFilterGetData, sbiModule_messaging) {
		var self = this;
		var registryConfigurationService = registryConfigService;
		var registryCRUD = registryCRUDService;
		var registryConfiguration = registryConfigurationService
				.getRegistryConfig();
		var regGetData = regFilterGetData;
		var columnsInfo = registryConfiguration.columns;
		var sbiMessaging = sbiModule_messaging;
		var configButtons =
		self.data = [];
		self.filterOptions = [];
		self.columns = [];
		self.options = [];
		self.pagesInfo = {};

		self.selectedColumn = [];
		self.setSelected = function(selectedColumn) {
			self.selectedColumn.push(selectedColumn);
			console.log(self.selectedColumn);
		};
		self.loadData = function(param) {
			registryCRUD.read(param).then(function(response) {
				self.data = response.data.rows;

			});
		};

		self.updateColumn = function() {
			for (var i = 0; i < self.selectedColumn.length; i++) {
				registryCRUD.update(self.selectedColumn[i]).then(function(response) {});
				if (i == (self.selectedColumn.length - 1)) {
					sbiMessaging.showInfoMessage('You have succesufly updated '
							+ (i + 1) + ' field/s ', 'Success!!!');
				}
			}
			self.selectedColumn = [];
		};

		self.deleteColumn = function(row) {
				registryCRUD.delete(row).then(function(response) {
						sbiMessaging.showInfoMessage('You have succesufly deleted column!', 'Success!!!');
						self.deleteRow(row.$$hashKey);
				});
		};
		self.loadData();
		self.filters = {};
		self.page = 1;

		// Filling columns
		columnsInfo.forEach(function(column) {
			column.position = columnsInfo.indexOf(column);
			self.columns.push(column);
		});

		self.getOptions = function(column) {
			regGetData.getData(column).then(function(response) {
				self.options = response.data.rows;
				return addColumnOptions(column);
			});
		};

		var addColumnOptions = function(field) {
			self.columns.forEach(function(column) {
				if (column.field == field) {
					self.columns.options = self.options;
				}
			});
		};

		self.getFilters = function(filterField) {
			regGetData.getData(filterField).then(function(response) {
				self.filterOptions = response.data.rows;
				return addOptions(filterField);
			});
		};

		var addOptions = function(filterField) {
			for (var i = 0; i < registryConfiguration.filters.length; i++) {
				if (registryConfiguration.filters[i].field == filterField) {
					registryConfiguration.filters[i].options = self.filterOptions;
				}
			}
			return registryConfiguration.filters;
		};

		// array object to define the registry configuration
		self.configuration = {
			title : "Registry Document",
			itemsPerPage : 15,
			enableButtons : registryConfiguration.configurations[0].value == "true",
			filters : registryConfiguration.filters
		};
console.log(registryConfiguration);
		self.isArray = angular.isArray;

		self.deleteRow = function(hash) {
			angular.forEach(self.data, function(value, key) {
				if (value.$$hashKey == hash) {
					self.data.splice(key, 1);
					return;
				}
			})
		}

		self.addRow = function() {
			var tmpRow = angular.copy(self.data[0], {});
			for ( var i in tmpRow) {
				tmpRow[i] = "";
			}
			;
			self.data.unshift(tmpRow);
		}

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
			return new Array(Math.ceil(self.data.length
					/ self.configuration.itemsPerPage));
		};

		self.hasNext = function() {
			return self.page * self.configuration.itemsPerPage < self.data.length;
		};

		self.hasPrevious = function() {
			return self.page > 1;
		};

		self.min = function() {
			return self.data.length > 0 ? (self.page - 1)
					* self.configuration.itemsPerPage + 1 : 0;
		};

		self.max = function() {
			return self.hasNext() ? (self.page * self.configuration.itemsPerPage)
					: self.data.length;
		};

		self.next = function(params) {
			// self.hasNext() && self.page++;
			self.page++;
			params = {
				start : self.page * self.configuration.itemsPerPage,
				limit : self.configuration.itemsPerPage
			};
			self.pagesInfo = params;
			console.log(self.pagesInfo);
			self.loadData(params);
			self.hasNest();
		}

		self.previous = function() {
			self.hasPrevious() && self.page--;
		}

	}
})();