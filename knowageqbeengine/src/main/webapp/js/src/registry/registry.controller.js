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

	angular.module('RegistryDocument', [ 'ngMaterial', 'registryConfig', 'sbiModule', 'registry_date_time_picker' ])
			.config(
					['$mdThemingProvider',
					'$httpProvider',
					function($mdThemingProvider, $httpProvider) {
						$mdThemingProvider.theme('knowage');
						$mdThemingProvider.setDefaultTheme('knowage');
						$httpProvider.interceptors
								.push('httpInterceptor');

					}])
			.filter('momentDate', function() {
				return function(input, currLanguage) {
					return input ? moment(input).locale(currLanguage).format("L") : '';
				};
			})
			.filter('momentDateAndTime', function() {
				return function(input, currLanguage) {
					return input ? moment(input).locale(currLanguage).format("L") + ' ' +
					moment(input).locale(currLanguage).format("HH:mm:ss") : '';
				};
			})
			.controller('RegistryController', ['$scope','registryConfigService', 'registryCRUDService',
								'regFilterGetData', 'sbiModule_messaging','sbiModule_translate', 'sbiModule_config', '$mdDialog', '$filter', 'orderByFilter','registryPaginationService',
					RegistryController])

	function RegistryController($scope,registryConfigService, registryCRUDService,
			regFilterGetData, sbiModule_messaging, sbiModule_translate, sbiModule_config, $mdDialog, $filter, orderBy, registryPaginationService) {
		var registryConfigurationService = registryConfigService;
		var registryCRUD = registryCRUDService;
		var registryConfiguration = registryConfigurationService
				.getRegistryConfig();
		var columnsInfo = registryConfiguration.columns;
		var sbiMessaging = sbiModule_messaging;
		var pagination = registryPaginationService;
		var dateColumns=[];
		$scope.sbiTranslate = sbiModule_translate;
		$scope.data = [];
		$scope.resultsNumber = 0;
		$scope.comboColumnOptions = {};
		$scope.columns = [];
		$scope.columnSizeInfo = [];
		$scope.columnFieldTypes = [];
		$scope.selectedRow = [];
		$scope.formParams = {};
		$scope.filters = {};
		$scope.page = 1;
		$scope.propertyName = '';
		$scope.reverse = false;
		$scope.dateFormat='MM/dd/yyyy';

		// array object to define the registry configuration

		$scope.configuration = {
			title: "Registry Document",
			itemsPerPage: 15,
			//enableButtons: registryConfiguration.configurations[0].value == "true",
			filters: registryConfiguration.filters,
			pagination: registryConfiguration.pagination,
			pivot: false
		};

		for(var i = 0 ; i < registryConfiguration.configurations.length;i++){
			if(registryConfiguration.configurations[i].name=="enableButtons"){
				$scope.configuration.enableButtons = registryConfiguration.configurations[i].value == "true"
			} else {
				if(registryConfiguration.configurations[i].name=="enableDeleteRecords"){
					$scope.configuration.enableDeleteRecords = registryConfiguration.configurations[i].value == "true"
				}
				if(registryConfiguration.configurations[i].name=="enableAddRecords"){
					$scope.configuration.enableAddRecords = registryConfiguration.configurations[i].value == "true"
				}
			}
		}

		//Getting initial data from server
		var loadInitialData = function(){
			if($scope.configuration.pagination == 'true') {
				$scope.formParams.start = 0;
				$scope.formParams.limit = $scope.configuration.itemsPerPage;
			} else {
				$scope.formParams.start = 0;
			}
			$scope.currLanguage = sbiModule_config.curr_language;
			readData($scope.formParams);
		};

		var readData = function(formParameters) {
			$scope.formatNumber= 0;
			registryCRUD.read(formParameters).then(function(response) {

				$scope.data= $scope.transformDataStore(response.data).rows
				if($scope.configuration.pagination != 'true'){
					$scope.data = orderBy($scope.data,$scope.propertyName,$scope.reverse);
				}
				dateColumns = dateColumnsFilter(response.data.metaData.fields);
				$scope.data = dateRowsFilter(dateColumns,$scope.data);
				$scope.resultsNumber = response.data.results;
				$scope.initalizePagination();
				if($scope.columnSizeInfo.length == 0) {
					$scope.columnSizeInfo = response.data.metaData.columnsInfos;
					$scope.columnFieldTypes = response.data.metaData.fields;
					$scope.columns.length=0
					addColumnsInfo();
				}


			});
		};
		$scope.transformDataStore = function (datastore){
			var newDataStore = {};
			newDataStore.metaData = datastore.metaData;
			newDataStore.results = datastore.results;
			newDataStore.rows = [];

			for(var i=0; i<datastore.rows.length; i++){
				var obj = {};
				for(var j=1; j<datastore.metaData.fields.length; j++){
					if(datastore.rows[i][datastore.metaData.fields[j].name]!=undefined){
						obj[datastore.metaData.fields[j].header] = datastore.rows[i][datastore.metaData.fields[j].name];
					}
				}
				newDataStore.rows.push(obj);
			}
			return newDataStore;
		}
		loadInitialData();

		// Filling columns
		var addColumnsInfo = function() {
			columnsInfo.forEach(function(column) {
				$scope.columnSizeInfo.forEach(function(columnSize) {
					if(columnSize.sizeColumn === column.field) {
						column.size = columnSize.size;
					}
				});

				$scope.columnFieldTypes.forEach(function(columnType) {
					if(columnType.header === column.field) {
						column.dataType = columnType.type;
					}
				});

				if(column.type && column.type === 'merge') {
					$scope.configuration.pivot = true;
				}
				if(column.visible == true) {
					column.position = columnsInfo.indexOf(column);
					$scope.columns.push(column);
				}
			});
		};

		//Sorting Columns
		$scope.sortBy = function(propertyName){
			if($scope.configuration.pagination != 'true'){
			$scope.reverse = (propertyName !== null && $scope.propertyName === propertyName) ? !$scope.reverse : false;
			$scope.propertyName = propertyName;
			$scope.data = orderBy($scope.data,$scope.propertyName,$scope.reverse);
			}
		};

		$scope.setDataType = function(columnDataType) {
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

		$scope.getDecimalPlaces = function(colName){
			var decimalPlaces;
			var floatColumns = $filter('filter')($scope.columnFieldTypes, {type: 'float'}, true);
			floatColumns.forEach(function(col){
				if(col.header == colName) {
					var format = col.format.split(',');
					decimalPlaces = format.length;
				}
				return decimalPlaces;
			});
		};

		$scope.getStep = function(dataType){
			if(dataType == 'float'){
				return '.01';
			} else if(dataType == 'int') {
				return '1';
			} else {
				return 'any';
			}
		};

		/* Pivot Table */
		$scope.setRowspan = function(rows,rowIndex,columnIndex,columns){

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

		$scope.compareRowsForRowspanPrint = function(rows,rowIndex,columnIndex,columns){

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

		$scope.setSummaryRowColor = function(rows,index,columns){
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

		$scope.isItSummaryRow = function(rows,indexF,index,columns){
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
		$scope.dependentColumns = [];

		$scope.addColumnOptions = function(column, row, $mdOpenMenu) {
			$mdOpenMenu();
			row.selected = true;

			//regular independent combo columns
			if(column.editor === 'COMBO' && !$scope.isDependentColumn(column)) {
				if(!$scope.comboColumnOptions[column.field]) {
					$scope.comboColumnOptions[column.field] = {};
					var promise = regFilterGetData.getData(column.field);
					promise.then(function(response) {
						$scope.comboColumnOptions[column.field] = response;
					});
					return promise;
				}
			}

			//dependent combo columns
			if(column.editor === 'COMBO' && $scope.isDependentColumn(column)) {
				if(!$scope.comboColumnOptions[column.field]) {
					$scope.comboColumnOptions[column.field] = {};

					var dependencesPromise = regFilterGetData.getDependeceOptions(column, row)
					dependencesPromise.then(function(response) {
						$scope.comboColumnOptions[column.field][row[column.dependsFrom]] = response.data.rows;
					});
					return dependencesPromise;
				} else {
					if(!$scope.comboColumnOptions[column.field].hasOwnProperty(row[column.dependsFrom])) {
						var dependencesPromise = regFilterGetData.getDependeceOptions(column, row)
						dependencesPromise.then(function(response) {
							$scope.comboColumnOptions[column.field][row[column.dependsFrom]] = response.data.rows;
						});
						return dependencesPromise;
					}
				}
			}
		};

		$scope.stopShow = false;

		$scope.notifyAboutDependency = function(column, event) {
			clicked++;
			if(clicked == 1) {
				fillDependencyColumns(column);
				createDialog($scope.dependentColumns);
			}

			if($scope.dependentColumns.length != 0 && !$scope.stopShow) {

				$mdDialog.show($scope.confirm)
						.then(function(result){
						$scope.stopShow = result;
					}, function(result){
						$scope.stopShow = result;
					});
				$scope.emptyDependentColumns($scope.dependentColumns);
			}
		};

		var fillDependencyColumns = function(column) {
			for(var i = 0; i < $scope.columns.length; i++) {
				var col = $scope.columns[i];
				if(col.dependsFrom === column.field) {
					var dependent = [];
					dependent.title = col.title;
					dependent.field = col.field;
					$scope.dependentColumns.push(dependent);
					fillDependencyColumns(col);
				}
			}

		};

		var createDialog = function(dependentColumns) {
			$scope.joinedFields = "";
			var i = 0;
			for (var k in $scope.dependentColumns) {
				if (i > 0)
					$scope.joinedFields += ", ";

				$scope.joinedFields += $scope.dependentColumns[k].title
				i++;
			}

			$scope.confirm = $mdDialog.prompt(
					{
						controller: DialogController,
						parent: angular.element(document.body),
						templateUrl: sbiModule_config.dynamicResourcesEnginePath + '/registry/dependentColumnsDialog.tpl.html',
						locals: {
							dontShowAgain: $scope.stopShow,
							columns: $scope.joinedFields
						},
						targetEvent: event,
						clickOutsideToClose: false,
						preserveScope: true,
						fullscreen: true
					}
			);

		};

		$scope.emptyDependentColumns = function (dependentColumns) {

			for (var i = 0; i < $scope.selectedRow.length; i++) {

				for(var property in $scope.selectedRow[i]) {

					for (var k in dependentColumns) {
						if (angular.equals(dependentColumns[k].field, property)) {
							$scope.selectedRow[i][property] = '';
						}
					}
				}
			}
		}

		function DialogController($scope, $mdDialog, dontShowAgain, columns, sbiModule_translate) {
			$scope.dontShowAgain = dontShowAgain;
			$scope.dependentColumns = columns;
			$scope.translate = sbiModule_translate;

			$scope.closeDialog = function() {
				dontShowAgain = $scope.dontShowAgain;
				$mdDialog.hide(dontShowAgain);
			};

		};

		$scope.isDependentColumn = function(column) {
			if(column.dependsFrom && column.dependsFrom != null) {
				return true;
			} else {
				return false;
			}
		};

		//Filters handling
		$scope.addFilterOptions = function(filterField){
			var promise = regFilterGetData.getData(filterField);
			promise.then(function(response) {
				addOptions(filterField,response);
			});
		};

		$scope.checkIfFiltersHaveValues = function() {
			for (var i = 0; i < registryConfiguration.filters.length; i++) {
				var filter = registryConfiguration.filters[i];
				for (var j = 0; j < registryConfiguration.columns.length; j++) {
					var column = registryConfiguration.columns[j];
					if(filter.presentation != 'DRIVER' && filter.field == column.field){
						if(filter.value) return true;
					}
				}
			}
			return false;
		}

		$scope.checkIfFilterColumnExists = function(){
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


		$scope.getFilteredData = function(params) {
			$scope.formParams = Object.assign({}, params);
			if($scope.configuration.pagination == 'true') {
				$scope.formParams.start = 0;
				$scope.formParams.limit = $scope.configuration.itemsPerPage;
				$scope.page = 1;
			} else {
				$scope.formParams.start = 0;
			}
			readData($scope.formParams);
		};

		$scope.deleteFilterValues = function(){
			$scope.filters = {};
			$scope.formParams = {};
			$scope.page = 1;
			for(var i = 0 ; i<($scope.configuration.filters).length; i++){
				$scope.configuration.filters[i].value = null;
			}
			loadInitialData($scope.formParams);
		};

		$scope.resetDateField = function (e, row, col) {
			row[col.field] = null;
			e.preventDefault();
			e.stopPropagation();
		}

		$scope.allADFilters = function() {
			var found = true;
			for(var i = 0 ; i<($scope.configuration.filters).length; i++){
					if ($scope.configuration.filters[i].presentation != 'DRIVER') {
						return false;
					}
				}

			return found;
		}

		// Update
		$scope.setSelected = function(selectedRow) {

			if(($scope.selectedRow).indexOf(selectedRow) === -1){
				$scope.selectedRow.push(selectedRow);
			}

		};

		$scope.allADFilters = function() {
			var found = true;
			for(var i = 0 ; i<($scope.configuration.filters).length; i++){
					if ($scope.configuration.filters[i].presentation != 'DRIVER') {
						return false;
					}
				}

			return found;
		}

		$scope.updateRow = function() {
			for (var i = 0; i < $scope.selectedRow.length; i++) {
				for(var property in $scope.selectedRow[i]) {
					if(!$scope.selectedRow[i].id && $scope.selectedRow[i][property] && typeof $scope.selectedRow[i][property].getMonth === 'function') {
						//var time = $scope.selectedRow[i][property].getTime();
						//$scope.selectedRow[i][property].setTime(time + new Date().getTimezoneOffset()*60*1000);
					}
					if($scope.selectedRow[i].$newRow) delete $scope.selectedRow[i].$newRow;

				}
			}
			registryCRUD.update($scope.selectedRow).then(function(response) {
				readData($scope.formParams);
				sbiMessaging.showInfoMessage( $scope.sbiTranslate.load("kn.registry.registryDocument.update.success")
						+' '+ ($scope.selectedRow.length) + ' ' + $scope.sbiTranslate.load("kn.registry.registryDocument.row"), $scope.sbiTranslate.load("kn.registry.registryDocument.success"));
				$scope.selectedRow.length = 0;
			});

		};

		// Delete
		$scope.deleteRowFromDB = function(row, event) {

			var confirm = $mdDialog.confirm()
			.title(sbiModule_translate.load('kn.registry.document.delete.row'))
			.textContent(sbiModule_translate.load('kn.registry.document.delete.confirm.message'))
			.targetEvent(event)
			.ok(sbiModule_translate.load('kn.qbe.general.yes'))
			.cancel(sbiModule_translate.load('kn.qbe.general.no'));

			if(row.$newRow){
				$scope.deleteRow(row.$$hashKey)
				return;
			}

			$mdDialog.show(confirm).then(function() {
				registryCRUD.delete(row).then(function(response) {
					sbiMessaging.showInfoMessage($scope.sbiTranslate.load("kn.registry.registryDocument.delete.success"), $scope.sbiTranslate.load("kn.registry.registryDocument.success"));
					$scope.deleteRow(row.$$hashKey);
				});
			});
		};

		$scope.isArray = angular.isArray;

		$scope.deleteRow = function(hash) {
			angular.forEach($scope.data, function(value, key) {
				if (value.$$hashKey == hash) {
					$scope.data.splice(key, 1);
					return;
				}
			});
		};

		$scope.addRow = function() {
			var tmpRow = angular.copy($scope.data[0], {});
			for ( var i in tmpRow) {
				tmpRow[i] = $scope.getDefaultValue(i);
			};
			tmpRow.$newRow = true;
			$scope.data.unshift(tmpRow);
		};

		$scope.getDefaultValue = function(columnTitle) {
			var defaultValue = "";
			angular.forEach($scope.columnFieldTypes, function(value, key) {
				if (angular.equals(columnTitle, value.header) && value.defaultValue) {
					defaultValue =  value.defaultValue;
					return;
				}

				if (angular.equals(columnTitle, value.header) && value.type == 'timestamp') {
					defaultValue = new Date();
				}
			});

			return defaultValue;
		}

		// reordering columns function
		$scope.move = function(position, direction) {
			var prev, cur, next;
			if (direction == 'left') {
				angular.forEach($scope.columns, function(value, key) {
					if (value.position == (position - 1))
						prev = key;
					if (value.position == (position))
						cur = key;
				})
				$scope.columns[cur].position--;
				$scope.columns[prev].position++;
			} else {
				angular.forEach($scope.columns, function(value, key) {
					if (value.position == (position + 1))
						next = key;
					if (value.position == (position))
						cur = key;
				})
				$scope.columns[cur].position++;
				$scope.columns[next].position--;
			}

		}

		$scope.addToFilters = function(filter) {
			$scope.filters[filter.field] = filter.value;
		}

		//Pagination
		$scope.initalizePagination=function(){

			$scope.getTotalPages = pagination.getTotalPages($scope.resultsNumber,$scope.configuration.itemsPerPage);
			$scope.hasNext = pagination.hasNext($scope.page,$scope.configuration.itemsPerPage,$scope.resultsNumber);
			$scope.hasPrevious = pagination.hasPrevious($scope.page);
			$scope.min = pagination.min($scope.resultsNumber,$scope.page,$scope.configuration.itemsPerPage);
			$scope.max = pagination.max($scope.page,$scope.configuration.itemsPerPage,$scope.resultsNumber);
			$scope.next = function() {
				$scope.page++;
				$scope.formParams = pagination.next($scope.page,$scope.formParams,$scope.configuration.itemsPerPage,$scope.filters);
				readData($scope.formParams);
			};
			$scope.previous = function() {
				$scope.page--;
				$scope.formParams = pagination.previous($scope.page,$scope.formParams,$scope.configuration.itemsPerPage,$scope.filters);
				readData($scope.formParams);
			};
			$scope.last = function() {
				$scope.page= $scope.getTotalPages.length;
				$scope.formParams = pagination.previous($scope.page,$scope.formParams,$scope.configuration.itemsPerPage,$scope.filters);
				readData($scope.formParams);
			};
			$scope.first = function() {
				$scope.page= 1;
				$scope.formParams = pagination.previous($scope.page,$scope.formParams,$scope.configuration.itemsPerPage,$scope.filters);
				readData($scope.formParams);
			}
			$scope.goToPage = function() {
				$scope.formParams = pagination.goToPage($scope.page,$scope.formParams,$scope.configuration.itemsPerPage,$scope.filters);
				readData($scope.formParams);
			}
		};


		$scope.checkIfSelected = function(row) {

			if(($scope.selectedRow).indexOf(row) !== -1 ){
				return 'blue';
			}
		}
		var dateColumnsFilter = function(columns){
			var namesOfDateColumns =[];
			for(var i = 0 ; i <columns.length ; i++){
				if(columns[i].type === 'date'){
					namesOfDateColumns.push({header: columns[i].header});
				}
			}
			return namesOfDateColumns;
		}

		var dateRowsFilter= function(columnNames,rows){
			for(var i = 0 ; i<columnNames.length ;  i++){
				for(var j = 0 ; j < rows.length; j++){
					var value  = rows[j][columnNames[i].header]
					if(value == '') value = null;

					if(value !=null) {
						var rowDate = new Date(value);
						if(!isNaN(rowDate)){
							rows[j][columnNames[i].header]= rowDate;//.replace(/ /g,'T')
							//rows[j][columnNames[i].header].setTime(rowDate.getTime() - new Date().getTimezoneOffset()*60*1000);
						}
					}
				}
			}
			return rows;
		}
	}
})();