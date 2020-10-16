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
			template:   '<button class="md-button md-knowage-theme" ng-click="addNewCatalogFunction()" ng-class="{\'md-icon-button\':selectedItem && !insideMenu}">'+
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

	function catalogFunctionController($scope,sbiModule_translate,sbiModule_restServices,$q,$mdDialog,cockpitModule_datasetServices,$mdToast){
		$scope.translate = sbiModule_translate;

		// PYTHON ENVIRONMENTS CONFIG
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promiseGet('2.0/configs/category', 'PYTHON_CONFIGURATION')
		.then(function(response){
			$scope.ngModel.pythonEnvironments = $scope.buildEnvironments(response.data);
		});

		// R ENVIRONMENTS CONFIG
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promiseGet('2.0/configs/category', 'R_CONFIGURATION')
		.then(function(response){
			$scope.ngModel.rEnvironments = $scope.buildEnvironments(response.data);
		});

		$scope.buildEnvironments = function (data) {
			toReturn = []
			for (i=0; i<data.length; i++) {
				key = data[i].label;
				val = data[i].valueCheck;
				toReturn[i] = {"label": key, "value": val};
			}
			return toReturn;
		}

		$scope.loadAllCatalogFunctions = function(){
			sbiModule_restServices.restToRootProject();
			sbiModule_restServices.get("2.0/functions-catalog", "").then(
					function(result) {
						$scope.ngModel.allCatalogFunctions = result.data.functions;
					}, function () {
						$scope.ngModel.allCatalogFunctions = {};
					}
			)
		}

		$scope.removeFunctionOutputColumns = function(){
			if ($scope.ngModel.content == undefined) {
				var columns = $scope.ngModel.columnSelectedOfDatasetAggregations;
			} else {
				var columns = $scope.ngModel.content.columnSelectedOfDataset;
			}
			var colsToMantain = [];
			for (var i=0; i<columns.length; i++) {
				if (columns[i].boundFunction && $scope.currentRow && columns[i].boundFunction.id == $scope.currentRow.boundFunction.id)
					continue;
				else
					colsToMantain.push(columns[i]);
			}
			if ($scope.ngModel.content == undefined) {
				$scope.ngModel.columnSelectedOfDatasetAggregations = colsToMantain;
			} else {
				$scope.ngModel.content.columnSelectedOfDataset = colsToMantain;
			}
		}

		$scope.loadAllCatalogFunctions();

		if($scope.selectedItem){
			if ($scope.measuresListFunc != undefined) {
//				var tmpList = $scope.measuresListFunc();
//				$scope.currentRow = tmpList[$scope.selectedItem];
				$scope.currentRow = $scope.selectedItem;
			} else if ( $scope.ngModel.content == undefined) {  // case when coming from chart widget
				$scope.currentRow = $scope.ngModel.columnSelectedOfDatasetAggregations[$scope.selectedItem];
			} else {
				$scope.currentRow = $scope.ngModel.content.columnSelectedOfDataset[$scope.selectedItem]
			}
		}

		$scope.addNewCatalogFunction = function(){
			var deferred = $q.defer();
			var promise ;
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
					callbackAddTo: $scope.callbackAddTo
				},
				fullscreen: true,
				controller: catalogFunctionDialogController
			}).then(function() {
				deferred.promise.then(function(result){
					$scope.removeFunctionOutputColumns();
					if ($scope.ngModel.content == undefined) {
						for (var i=0; i<result.length; i++) {
							$scope.ngModel.columnSelectedOfDatasetAggregations.push(result[i]);
						}
					} else {
						for (var i=0; i<result.length; i++) {
							$scope.ngModel.content.columnSelectedOfDataset.push(result[i]);
						}
					}
					$scope.ngModel.selectedFunction = undefined;
					$scope.loadAllCatalogFunctions();
				});
			}, function() {
			});
			promise =  deferred.promise;
		}
	}

	function catalogFunctionDialogController($scope,sbiModule_translate,cockpitModule_template,sbiModule_restServices,$mdDialog,$q,promise,model,actualItem,callbackUpdateGrid,callbackUpdateAlias,additionalInfo,measuresListFunc,callbackAddTo,cockpitModule_datasetServices,cockpitModule_generalOptions,$timeout, cockpitModule_properties){
		$scope.translate=sbiModule_translate;
		$scope.model = model;
		$scope.model.selectedFunction = actualItem ? angular.copy(actualItem) : {};
		var style = {'display': 'inline-flex', 'justify-content':'center', 'align-items':'center'};

		$scope.model.functionsGrid = {
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
		        rowData: $scope.model.allCatalogFunctions
		}

		function resizeFunctions(){
			$scope.model.functionsGrid.api.sizeColumnsToFit();
		}

		function selectFunction(props){
			$scope.selectFunction(props.api.getSelectedRows()[0]);
		}

		function languageRenderer(props){
			var language = props.value;
			if (language == 'Python')
				return language + "<i class='fab fa-python'></i>";
			else
				return language + "<i class='fab fa-r-project'></i>";
		}

		$scope.model.columnsGrid = {
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
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.inputColumn.type'), field:'type'},
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.inputColumn.datasetColumn'), field:'dsColumn', editable:true, cellRenderer:editableCell, cellEditor:"agSelectCellEditor", cellEditorParams: {values: getDatasetColumns()}}],
	        rowData: $scope.model.selectedFunction.inputColumns
		}

		function refreshRowForColumns(cell){
			$scope.model.columnsGrid.api.redrawRows({rowNodes: [$scope.model.columnsGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
		}

		function resizeColumns(){
			$scope.model.columnsGrid.api.sizeColumnsToFit();
		}

		function getDatasetColumns(){
			var toReturn = [];
			for (var i=0; i<$scope.model.content.columnSelectedOfDataset.length; i++) {
				var column = $scope.model.content.columnSelectedOfDataset[i];
				if (!column.isFunction)
					toReturn.push(column.name);
			}
			return toReturn;
		}

		$scope.model.variablesGrid = {
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
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.inputVariable.type'), field:'type'},
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.catalogFunctions.inputVariable.value'), field:'value', editable: true, cellRenderer: editableCell}],
	        rowData: $scope.model.selectedFunction.inputVariables
		}

		function editableCell(params){
			var editButton = '<i class="fa fa-edit"></i> <i>';
			if (typeof(params.value) !== 'undefined')
				return editButton + params.value;
			else return editButton;
		}

		function refreshRowForVariables(cell){
			$scope.model.variablesGrid.api.redrawRows({rowNodes: [$scope.model.variablesGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
		}

		function resizeVariables(){
			$scope.model.variablesGrid.api.sizeColumnsToFit();
		}

		$scope.selectFunction=function(func){
			$scope.model.selectedFunction = func;
			$scope.model.columnsGrid.api.setRowData(func.inputColumns);
			$scope.model.variablesGrid.api.setRowData(func.inputVariables);
		}

		$scope.model.librariesGrid = {
	        enableColResize: false,
	        enableFilter: true,
	        enableSorting: true,
	        onGridReady: resizeLibraries,
	        onGridSizeChanged: resizeLibraries,
	        pagination: true,
	        paginationAutoPageSize: true,
	        columnDefs: [
	        	{headerName: "Library", field:'name'},
	        	{headerName: "Version", field:'version'}],
			rowData: $scope.model.libraries
		}

		function resizeLibraries(){
			$scope.model.librariesGrid.api.sizeColumnsToFit();
		}

		function refreshRowForLibraries(cell){
			$scope.model.librariesGrid.api.redrawRows({rowNodes: [$scope.model.librariesGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
		}

		$scope.setLibraries=function() {
			sbiModule_restServices.restToRootProject();
			var endpoint = $scope.model.selectedFunction.language == "Python" ? "python" : "RWidget";
			sbiModule_restServices.promiseGet('2.0/backendservices/widgets/'+ endpoint +'/libraries', JSON.parse($scope.model.selectedFunction.environment).label)
			.then(function(response){
				$scope.model.selectedFunction.libraries = [];
				var librariesArray = JSON.parse((response.data.result));
				for (idx in librariesArray) {
					lib = librariesArray[idx];
					$scope.model.selectedFunction.libraries.push({"name": lib.name, "version": lib.version})
				}
				$scope.model.librariesGrid.api.setRowData($scope.model.selectedFunction.libraries);
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
			if (!checkColumnsConfiguration($scope.model.selectedFunction.inputColumns))
				$scope.toastifyMsg('warning',$scope.translate.load("sbi.cockpit.widgets.table.catalogFunctions.function.error.datasetColumns"));
			else if (!checkVariablesConfiguration($scope.model.selectedFunction.inputVariables))
				$scope.toastifyMsg('warning',$scope.translate.load("sbi.cockpit.widgets.table.catalogFunctions.function.error.inputVariables"));
			else if (!checkEnvironmentConfiguration($scope.model.selectedFunction.environment))
				$scope.toastifyMsg('warning',$scope.translate.load("sbi.cockpit.widgets.table.catalogFunctions.function.error.environment"));
			else {
				$scope.result = [];
				for (var i=0; i<$scope.model.selectedFunction.outputColumns.length; i++) {
					$scope.result[i] = {};
					$scope.result[i].boundFunction = angular.copy($scope.model.selectedFunction);
					if(!$scope.result[i].alias) $scope.result[i].alias = $scope.model.selectedFunction.outputColumns[i].name;
					$scope.result[i].aliasToShow = $scope.result[i].alias;
					$scope.result[i].name = $scope.result[i].alias;
					$scope.result[i].fieldType = $scope.model.selectedFunction.outputColumns[i].fieldType;
					$scope.result[i].isFunction = true;
					$scope.result[i].type = getResultType($scope.model.selectedFunction.outputColumns[i].type);
					$scope.result[i].environment = $scope.model.selectedFunction.environment;
				}
				promise.resolve($scope.result);
				$mdDialog.hide();
			}
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
			$scope.model.selectedFunction = undefined;
			$mdDialog.cancel();
		}

		$scope.isSolrDataset = function() {
			if($scope.model.dataset.dsId != undefined) {
				if (cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId).type == "SbiSolrDataSet") {
					return true;
				}

			}
			return false;
		}
	}

})();