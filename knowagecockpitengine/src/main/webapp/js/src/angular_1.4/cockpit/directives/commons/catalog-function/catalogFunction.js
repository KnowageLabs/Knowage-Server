/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function(){

	angular.module('cockpitModule').directive('catalogFunction',function(){
		return{
			template:   '<button class="md-button md-knowage-theme" ng-click="addNewCatalogFunction()" ng-disabled="isFunctionInUse() && !selectedItem" ng-class="{\'md-icon-button\':selectedItem && !insideMenu}">'+
						'	<md-icon md-font-icon="fas fa-square-root-alt" ng-if="selectedItem"></md-icon>'+
						'	<span ng-if="!selectedItem">{{::translate.load("sbi.cockpit.widgets.table.catalogFunctions.add")}}</span>'+
						'	<span ng-if="selectedItem && insideMenu">{{::translate.load("sbi.cockpit.widgets.table.catalogFunctions.edit")}}</span>'+
						'	<md-tooltip ng-if="selectedItem && !insideMenu" md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.catalogFunctions.edit")}}</md-tooltip>'+
						'</button>',
			replace: true,
			scope:{
				ngModel: "=",
				selectedItem : "=?",
				callbackUpdateGrid : "&?",
				callbackUpdateAlias : "&?",
				insideMenu : "=?",
				additionalInfo: "=?",
				// A function with no params that return the list
				// of available features
				measuresListFunc : "&?",
				// A function with the new CF to add to the list
				// of fields
				callbackAddTo : "&?"
			},
			controller: catalogFunctionController,
		}
	});

	function catalogFunctionController($scope,sbiModule_translate,sbiModule_restServices,cockpitModule_catalogFunctionService,$q,$mdDialog,cockpitModule_datasetServices,$mdToast){
		$scope.translate = sbiModule_translate;

		function buildCrossTabColumns(crosstabDefinition){
			var columnsArray = [];
			for (var c in crosstabDefinition.columns) {
				columnsArray.push(crosstabDefinition.columns[c]);
			}
			for (var r in crosstabDefinition.rows) {
				columnsArray.push(crosstabDefinition.rows[r]);
			}
			for (var m in crosstabDefinition.measures) {
				columnsArray.push(crosstabDefinition.measures[m]);
			}
			return columnsArray;
		}

		$scope.removeFunctionOutputColumns = function(){
			if ($scope.ngModel.content == undefined) { // chart widget
				var columns = $scope.ngModel.columnSelectedOfDatasetAggregations;
			} else if ($scope.ngModel.content.crosstabDefinition) { // cross tab widget
				return;
			} else {	// other widgets
				var columns = $scope.ngModel.content.columnSelectedOfDataset;
			}
			var colsToMantain = [];
			for (var i=0; i<columns.length; i++) {
				if (columns[i].boundFunction && $scope.currentRow && columns[i].boundFunction.id == $scope.currentRow.boundFunction.id)
					continue;
				else
					colsToMantain.push(columns[i]);
			}
			if ($scope.ngModel.content == undefined) { // chart widget
				$scope.ngModel.columnSelectedOfDatasetAggregations = colsToMantain;
			} else {
				$scope.ngModel.content.columnSelectedOfDataset = colsToMantain;
			}
		}

		$scope.isFunctionInUse = function(){
			if ($scope.ngModel.content == undefined) { // chart widget
				var columns = $scope.ngModel.columnSelectedOfDatasetAggregations;
			} else if ($scope.ngModel.content.crosstabDefinition) { // cross tab widget
				var columns = buildCrossTabColumns($scope.ngModel.content.crosstabDefinition);
			} else {	// other widgets
				var columns = $scope.ngModel.content.columnSelectedOfDataset;
			}
			if (columns) {
				for (var i=0; i<columns.length; i++) {
					if (columns[i].isFunction) return true;
				}
			}
			return false;
		}

		if($scope.selectedItem){
			if ($scope.measuresListFunc != undefined) {
				$scope.currentRow = $scope.selectedItem;
			} else if ( $scope.ngModel.content == undefined) {  // chart widget
				$scope.currentRow = $scope.ngModel.columnSelectedOfDatasetAggregations[$scope.selectedItem];
			} else { // other widgets
				$scope.currentRow = $scope.ngModel.content.columnSelectedOfDataset[$scope.selectedItem]
			}
		}

		$scope.addNewCatalogFunction = function(){
			var deferred = $q.defer();
			var promise;
			if ($scope.currentRow) {
				$scope.currentFunction = $scope.currentRow.boundFunction
			}

			$mdDialog.show({
				templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitCatalogFunctionTemplate.html',
				parent : angular.element(document.body),
				clickOutsideToClose:true,
				escapeToClose :true,
				preserveScope: true,
				autoWrap:false,
				locals: {
					promise: deferred,
					model:$scope.ngModel,
					actualItem : $scope.currentFunction,
					callbackUpdateGrid: $scope.callbackUpdateGrid,
					callbackUpdateAlias: $scope.callbackUpdateAlias,
					additionalInfo: $scope.additionalInfo,
					measuresListFunc: $scope.measuresListFunc,
					callbackAddTo: $scope.callbackAddTo,
					buildCrossTabColumns: buildCrossTabColumns
				},
				fullscreen: true,
				controller: catalogFunctionDialogController
			}).then(function() {
				deferred.promise.then(function(result){
					$scope.removeFunctionOutputColumns();
					if ($scope.ngModel.content == undefined) { // chart widget
						for (var i=0; i<result.length; i++) {
							$scope.ngModel.columnSelectedOfDatasetAggregations.push(result[i]);
						}
					} else if ($scope.ngModel.content.crosstabDefinition) { // cross tab widget
						for (var i=0; i<result.length; i++) {
							result[i].id = result[i].name;
							result[i].nature = "catalog_function";
							if (result[i].fieldType == "ATTRIBUTE") {
								$scope.ngModel.content.crosstabDefinition.rows.push(result[i]);
							} else {
								result[i].containerType = "MEASURE-PT";
								result[i].funct = "NONE";
								result[i].iconCls = "measure";
								result[i].values = "[]";
								$scope.ngModel.content.crosstabDefinition.measures.push(result[i]);
							}
						}
					} else { // other widgets
						for (var i=0; i<result.length; i++) {
							$scope.ngModel.content.columnSelectedOfDataset.push(result[i]);
						}
					}
					$scope.ngModel.selectedFunction = undefined;
				});
			}, function() {
			});
			promise = deferred.promise;
		}
	}

	function catalogFunctionDialogController($scope,sbiModule_translate,cockpitModule_template,cockpitModule_catalogFunctionService,sbiModule_restServices,$mdDialog,$q,promise,model,actualItem,callbackUpdateGrid,callbackUpdateAlias,additionalInfo,measuresListFunc,callbackAddTo,buildCrossTabColumns,cockpitModule_datasetServices,cockpitModule_generalOptions,$timeout, cockpitModule_properties){
		$scope.translate=sbiModule_translate;
		$scope.model = model;
		$scope.selectedFunction = actualItem ? angular.copy(actualItem) : {};
		var style = {'display': 'inline-flex', 'justify-content':'center', 'align-items':'center'};
		var typesMap = {'STRING': "fa fa-quote-right", 'NUMBER': "fa fa-hashtag", 'DATE': 'fa fa-calendar'};
		$scope.rEnvironments = cockpitModule_catalogFunctionService.rEnvironments;
		$scope.pythonEnvironments = cockpitModule_catalogFunctionService.pythonEnvironments;

		$scope.functionsGrid = {
		        enableColResize: false,
		        enableFilter: true,
		        enableSorting: true,
		        onGridReady: resizeFunctions,
		        onGridSizeChanged: resizeFunctions,
		        rowSelection: "single",
		        onRowSelected: selectFunction,
		        pagination: true,
		        paginationAutoPageSize: true,
		        columnDefs: [
		        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.function.name'), field:'name', headerTooltip:'description'},
		        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.function.language'), field:'language', cellRenderer: languageRenderer, cellStyle: style}],
		        rowData: cockpitModule_catalogFunctionService.allCatalogFunctions
		}

		function resizeFunctions(){
			$scope.functionsGrid.api.sizeColumnsToFit();
		}

		function selectFunction(props){
			$scope.selectFunction(props.api.getSelectedRows()[0]);
		}

		function languageRenderer(props){
			var language = props.value;
			if (language == 'Python')
				return language + "<i class='fab fa-python layout-padding'></i>";
			else
				return language + "<i class='fab fa-r-project layout-padding'></i>";
		}

		$scope.columnsGrid = {
			angularCompileRows: true,
			domLayout :'autoHeight',
	        enableColResize: false,
	        enableFilter: false,
	        enableSorting: false,
	        onGridReady : resizeColumns,
	        onGridSizeChanged: resizeColumns,
	        onCellEditingStopped: refreshRowForColumns,
	        singleClickEdit: true,
	        columnDefs: [
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.inputColumn.name'), field:'name'},
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.inputColumn.type'), field:'type', cellRenderer:typeRenderer},
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.inputColumn.datasetColumn'), field:'dsColumn', editable:true, cellRenderer:editableCell, cellEditor:"agSelectCellEditor", cellEditorParams: {values: getDatasetColumns()}}],
	        rowData: $scope.selectedFunction.inputColumns
		}

		function refreshRowForColumns(cell){
			$scope.columnsGrid.api.redrawRows({rowNodes: [$scope.columnsGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
		}

		function resizeColumns(){
			$scope.columnsGrid.api.sizeColumnsToFit();
		}

		function getDatasetColumns(){
			var toReturn = [];
			if ($scope.model.content == undefined) { // chart widget
				var columns = $scope.model.columnSelectedOfDatasetAggregations;
			} else if ($scope.model.content.crosstabDefinition) { // cross tab widget
				var columns = buildCrossTabColumns($scope.model.content.crosstabDefinition);
			} else { // other widgets
				var columns = $scope.model.content.columnSelectedOfDataset;
			}
			for (var i=0; i<columns.length; i++) {
				var column = columns[i];
				if (!column.isFunction) {
					if (column.name) toReturn.push(column.name);
					else toReturn.push(column.id);
				}
			}
			return toReturn;
		}

		$scope.variablesGrid = {
			angularCompileRows: true,
			domLayout :'autoHeight',
	        enableColResize: false,
	        enableFilter: false,
	        enableSorting: false,
	        onGridReady : resizeVariables,
	        onGridSizeChanged: resizeVariables,
	        onCellEditingStopped: refreshRowForVariables,
	        singleClickEdit: true,
	        columnDefs: [
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.inputVariable.name'), field:'name'},
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.inputVariable.type'), field:'type', cellRenderer: typeRenderer},
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.inputVariable.value'), field:'value', editable: true, cellRenderer: editableCell}],
	        rowData: $scope.selectedFunction.inputVariables
		}

		function editableCell(params){
			var editButton = '<i class="fa fa-edit"></i> <i>';
			if (typeof(params.value) !== 'undefined')
				return editButton + params.value;
			else return editButton;
		}

		function typeRenderer(params){
			var typeIcon = '<i class="' + typesMap[params.value] + '"></i> <i>'
			return typeIcon + params.value;
		}

		function refreshRowForVariables(cell){
			$scope.variablesGrid.api.redrawRows({rowNodes: [$scope.variablesGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
		}

		function resizeVariables(){
			$scope.variablesGrid.api.sizeColumnsToFit();
		}

		$scope.selectFunction=function(func){
			$scope.selectedFunction = func;
			$scope.columnsGrid.api.setRowData(func.inputColumns);
			$scope.variablesGrid.api.setRowData(func.inputVariables);
			delete $scope.selectedFunction.environment;
		}

		$scope.librariesGrid = {
	        enableColResize: false,
	        enableFilter: true,
	        enableSorting: true,
	        onGridReady: resizeLibraries,
	        onGridSizeChanged: resizeLibraries,
	        pagination: true,
	        paginationAutoPageSize: true,
	        columnDefs: [
	        	{headerName: "Library", field:'name'},
	        	{headerName: "Version", field:'version'}]
		}

		function resizeLibraries(){
			$scope.librariesGrid.api.sizeColumnsToFit();
		}

		function refreshRowForLibraries(cell){
			$scope.librariesGrid.api.redrawRows({rowNodes: [$scope.librariesGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
		}

		$scope.setLibraries=function() {
			sbiModule_restServices.restToRootProject();
			var endpoint = $scope.selectedFunction.language == "Python" ? "python" : "RWidget";
			sbiModule_restServices.promiseGet('2.0/backendservices/widgets/'+ endpoint +'/libraries', JSON.parse($scope.selectedFunction.environment).label)
			.then(function(response){
				$scope.selectedFunction.libraries = [];
				var librariesArray = JSON.parse((response.data.result));
				for (idx in librariesArray) {
					lib = librariesArray[idx];
					$scope.selectedFunction.libraries.push({"name": lib.name, "version": lib.version})
				}
				$scope.librariesGrid.api.setRowData($scope.selectedFunction.libraries);
			}, function(error){
			});
		}

		checkColumnsConfiguration=function(columns){
			for (var i=0; i<columns.length; i++) {
				if (!columns[i].dsColumn)
					return false;
			}
			return true;
		}

		checkVariablesConfiguration=function(variables){
			for (var i=0; i<variables.length; i++) {
				if (!variables[i].value || variables[i].value == '')
					return false;
			}
			return true;
		}

		checkEnvironmentConfiguration=function(environment){
			if (!environment)
				return false;
			return true;
		}

		$scope.toastifyMsg = function(type,msg){
			Toastify({
				text: msg,
				duration: 10000,
				close: true,
				className: 'kn-' + type + 'Toast',
				stopOnFocus: true
			}).showToast();
		}

		$scope.saveColumnConfiguration=function(){
			if (!checkColumnsConfiguration($scope.selectedFunction.inputColumns))
				$scope.toastifyMsg('warning',$scope.translate.load("sbi.cockpit.widgets.table.catalogFunctions.function.error.datasetColumns"));
			else if (!checkVariablesConfiguration($scope.selectedFunction.inputVariables))
				$scope.toastifyMsg('warning',$scope.translate.load("sbi.cockpit.widgets.table.catalogFunctions.function.error.inputVariables"));
			else if (!checkEnvironmentConfiguration($scope.selectedFunction.environment))
				$scope.toastifyMsg('warning',$scope.translate.load("sbi.cockpit.widgets.table.catalogFunctions.function.error.environment"));
			else {
				$scope.result = [];
				for (var i=0; i<$scope.selectedFunction.outputColumns.length; i++) {
					$scope.result[i] = {};
					$scope.result[i].boundFunction = copyFunctionForModel($scope.selectedFunction);
					if(!$scope.result[i].alias) $scope.result[i].alias = $scope.selectedFunction.outputColumns[i].name;
					$scope.result[i].aliasToShow = $scope.result[i].alias;
					$scope.result[i].name = $scope.result[i].alias;
					$scope.result[i].fieldType = $scope.selectedFunction.outputColumns[i].fieldType;
					$scope.result[i].isFunction = true;
					$scope.result[i].type = getResultType($scope.selectedFunction.outputColumns[i].type);
				}
				promise.resolve($scope.result);
				$mdDialog.hide();
			}
		}

		copyFunctionForModel=function(func){
			var funcForExecution = {};
			funcForExecution.id = func.id;
			funcForExecution.name = func.name;
			funcForExecution.label = func.label;
			funcForExecution.inputVariables = func.inputVariables;
			funcForExecution.inputColumns = func.inputColumns;
			funcForExecution.outputColumns = func.outputColumns;
			funcForExecution.environment = func.environment;
			return funcForExecution;
		}

		getResultType=function(type){
			if (type == "NUMBER")
				return "java.lang.Double";
			else if (type == "DATE")
				return "java.sql.Date";
			else
				return "java.lang.String";
		}

		$scope.cancelConfiguration=function(){
			$scope.selectedFunction = undefined;
			$mdDialog.cancel();
		}

	}

})();