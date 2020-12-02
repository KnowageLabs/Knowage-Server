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
angular
	.module('cockpitModule')
	.controller('customChartWidgetEditControllerFunction',customChartWidgetEditControllerFunction)

function customChartWidgetEditControllerFunction(
	$scope,
	finishEdit,
	model,
	$filter,
	$mdDialog,
	mdPanelRef,
	sbiModule_translate,
	cockpitModule_template,
	cockpitModule_datasetServices,
	cockpitModule_generalOptions,
	datastore){

	$scope.translate = sbiModule_translate;
	$scope.newModel = angular.copy(model);
	$scope.typesMap = cockpitModule_generalOptions.typesMap;
	$scope.availableAggregations = ["NONE","SUM","AVG","MAX","MIN","COUNT","COUNT_DISTINCT"];

	if($scope.newModel.css.opened) $scope.newModel.css.opened = false;
	if($scope.newModel.html.opened) $scope.newModel.html.opened = false;
	if(!$scope.newModel.js.opened) $scope.newModel.js.opened = true;

	$scope.toggleLanguage = function(language){
		var languages = ['css','html','js'];
		for(var k in languages){
			if(languages[k] != language) $scope.newModel[languages[k]].opened = false;
			else $scope.newModel[language].opened = !$scope.newModel[language].opened;
		}
	}

	$scope.changeDS = function(id){
	    $scope.newModel.content.columnSelectedOfDataset = cockpitModule_datasetServices.getDatasetById(id).metadata.fieldsMeta;
		for(var c in $scope.newModel.content.columnSelectedOfDataset){
			if(!$scope.newModel.content.columnSelectedOfDataset[c].aliasToShow) $scope.newModel.content.columnSelectedOfDataset[c].aliasToShow = $scope.newModel.content.columnSelectedOfDataset[c].alias;
			if($scope.newModel.content.columnSelectedOfDataset[c].fieldType == 'MEASURE' && !$scope.newModel.content.columnSelectedOfDataset[c].aggregationSelected) $scope.newModel.content.columnSelectedOfDataset[c].aggregationSelected = 'SUM';
			if($scope.newModel.content.columnSelectedOfDataset[c].fieldType == 'MEASURE' && !$scope.newModel.content.columnSelectedOfDataset[c].funcSummary) $scope.newModel.content.columnSelectedOfDataset[c].funcSummary = $scope.newModel.content.columnSelectedOfDataset[c].aggregationSelected;
		}
		$scope.getDatasetAdditionalInfo(id);
		$scope.columnsGrid.api.setRowData($scope.newModel.content.columnSelectedOfDataset);
	}

	$scope.getDatasetAdditionalInfo = function(dsId){
        for(var k in cockpitModule_template.configuration.datasets){
        	if(cockpitModule_template.configuration.datasets[k].dsId == dsId) {
        		$scope.localDataset = cockpitModule_template.configuration.datasets[k];
        		break;
        	}
        }
	}
	if($scope.newModel.dataset && $scope.newModel.dataset.dsId) $scope.getDatasetAdditionalInfo($scope.newModel.dataset.dsId);

	function getColumnsDefinition() {
		$scope.availableGroups = [''];

		$scope.columnsDefinition = [
			{headerName: 'Name', field:'alias'},
	    	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.alias'), field:'aliasToShow',"editable":true,cellRenderer:editableCell, cellClass: 'editableCell'},
	    	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.type'), field: 'fieldType'},
	    	{headerName: 'Data Type', field: 'type',cellRenderer:typeCell},
	    	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.aggregation'), field: 'aggregationSelected', cellRenderer: aggregationRenderer,"editable":isAggregationEditable, cellClass: 'editableCell',
	    		cellEditor:"agSelectCellEditor",cellEditorParams: {values: $scope.availableAggregations}},
	    	{headerName:"",cellRenderer: buttonRenderer,"field":"valueId","cellStyle":{"border":"none !important","text-align": "right","display":"inline-flex","justify-content":"flex-end"},width: 100,suppressSizeToFit:true, tooltip: false,
	    			headerClass:'header-cell-buttons'}];

		if($scope.columnsGrid && $scope.columnsGrid.api) {
			$scope.columnsGrid.api.setColumnDefs($scope.columnsDefinition);
			$scope.columnsGrid.api.setRowData($scope.newModel.content.columnSelectedOfDataset);
			resizeColumns();
		}
	}
	getColumnsDefinition();

	$scope.columnsGrid = {
		angularCompileRows: true,
		domLayout:'autoHeight',
        enableColResize: false,
        enableFilter: false,
        enableSorting: false,
        onGridReady : resizeColumns,
        onViewportChanged: resizeColumns,
        onCellEditingStopped: refreshRow,
        singleClickEdit: true,
        columnDefs: $scope.columnsDefinition,
		rowData: $scope.newModel.content.columnSelectedOfDataset
	}

	function resizeColumns(){
		$scope.columnsGrid.api.sizeColumnsToFit();
	}

	function editableCell(params){
		return typeof(params.value) !== 'undefined' ? '<i class="fa fa-edit"></i> <i>'+params.value+'<md-tooltip>'+params.value+'</md-tooltip></i>' : '';
	}
	function typeCell(params){
		return "<i class='"+$scope.typesMap[params.value].icon+"'></i> "+$scope.typesMap[params.value].label;
	}
	function isInputEditable(params) {
		return typeof(params.data.name) !== 'undefined';
	}
	function isAggregationEditable(params) {
		if (params.data.isCalculated || params.data.isFunction) return false;
		return params.data.fieldType == "MEASURE" ? true : false;
	}

	function aggregationRenderer(params) {
		var aggregation = '<i class="fa fa-edit"></i> <i>'+params.value+'</i>';
		return (params.data.fieldType == "MEASURE" && !params.data.isCalculated && !params.data.isFunction) ? aggregation : '';
	}

	function groupRenderer(params) {
		return '<i class="fa fa-edit"></i> <i>'+ (typeof params.value != 'undefined' ? params.value : '') +'</i>';
	}

	function buttonRenderer(params){
		var calculator = '';
		if(params.data.isCalculated){
			calculator = '<calculated-field ng-model="newModel" selected-item="'+params.rowIndex+'" additional-info="datasetAdditionalInfos"></calculated-field>';
		}
		if(params.data.isFunction){
			calculator = '<catalog-function ng-model="newModel" selected-item="'+params.rowIndex+'" additional-info="datasetAdditionalInfos"></catalog-function>';
		}
		return 	calculator +
				'<md-button class="md-icon-button" ng-click="deleteColumn(\''+params.data.alias+'\',$event)"><md-icon md-font-icon="fa fa-trash"></md-icon><md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.column.delete")}}</md-tooltip></md-button>';
	}

	function refreshRow(cell){
		if(cell.data.fieldType == 'MEASURE' && !cell.data.aggregationSelected) cell.data.aggregationSelected = 'SUM';
		if(cell.data.fieldType == 'MEASURE' && cell.data.aggregationSelected) cell.data.funcSummary = cell.data.aggregationSelected == 'NONE' ? 'SUM' : cell.data.aggregationSelected;
		if(cell.data.isCalculated) cell.data.alias = cell.data.aliasToShow;
		$scope.columnsGrid.api.redrawRows({rowNodes: [$scope.columnsGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
	}

	$scope.deleteColumn = function(rowName,event) {
		for(var k in $scope.newModel.content.columnSelectedOfDataset){
			if($scope.newModel.content.columnSelectedOfDataset[k].alias == rowName) var item = $scope.newModel.content.columnSelectedOfDataset[k];
		}
		if (!item.isFunction) {
			var index=$scope.newModel.content.columnSelectedOfDataset.indexOf(item);
			$scope.newModel.content.columnSelectedOfDataset.splice(index,1);
			if($scope.newModel.settings.sortingColumn == item.aliasToShow){
				$scope.newModel.settings.sortingColumn = null;
			}
		} else {
			//if column to be deleted belongs to a function, we must delete all the other columns belonging to that function as well
			var id = item.boundFunction.id;
			colsToRemove = [];
			for (var i=0; i<$scope.newModel.content.columnSelectedOfDataset.length; i++) {
				var col = $scope.newModel.content.columnSelectedOfDataset[i];
				if (col.isFunction && col.boundFunction.id == id)
					colsToRemove.push(col);
			}
			for (var j=0; j<colsToRemove.length; j++) {
				var index=$scope.newModel.content.columnSelectedOfDataset.indexOf(colsToRemove[j]);
				$scope.newModel.content.columnSelectedOfDataset.splice(index,1);
				if($scope.newModel.settings && $scope.newModel.settings.sortingColumn == colsToRemove[j].aliasToShow){
					$scope.newModel.settings.sortingColumn = null;
				}
			}
		}
	  }

	$scope.openListColumn = function(){
		if($scope.newModel.dataset == undefined || $scope.newModel.dataset.dsId == undefined){
			$scope.showAction($scope.translate.load("sbi.cockpit.table.missingdataset"));
		}else{
			$mdDialog.show({
				templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitColumnsOfDataset.html',
				parent : angular.element(document.body),
				clickOutsideToClose:true,
				escapeToClose :true,
				preserveScope: true,
				autoWrap:false,
				locals: {model:$scope.newModel},
				fullscreen: true,
				controller: controllerCockpitColumnsConfigurator
			}).then(function(answer) {
			}, function() {
			});
		}
	}

	function controllerCockpitColumnsConfigurator($scope,sbiModule_translate,$mdDialog,model,cockpitModule_datasetServices,cockpitModule_generalOptions,$filter){
		$scope.translate=sbiModule_translate;

		$scope.cockpitModule_generalOptions=cockpitModule_generalOptions;
		$scope.model = model;
		$scope.localDataset = {};

		if($scope.model.dataset && $scope.model.dataset.dsId){
			angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.localDataset);
		} else{
			$scope.model.dataset= {};
			angular.copy([], $scope.model.dataset.metadata.fieldsMeta);
		}

		$scope.filterColumns = function(){
			var tempColumnsList = $filter('filter')($scope.localDataset.metadata.fieldsMeta,$scope.columnsSearchText);
			$scope.columnsGridOptions.api.setRowData(tempColumnsList);
		}

		$scope.columnsGridOptions = {
	            enableColResize: false,
	            enableFilter: true,
	            enableSorting: true,
	            pagination: true,
	            paginationAutoPageSize: true,
	            onGridSizeChanged: resizeColumns,
	            onViewportChanged: resizeColumns,
	            rowSelection: 'multiple',
				rowMultiSelectWithClick: true,
	            defaultColDef: {
	            	suppressMovable: true,
	            	tooltip: function (params) {
	                    return params.value;
	                },
	            },
	            columnDefs :[{"headerName":"Column","field":"alias",headerCheckboxSelection: true, checkboxSelection: true},
	        		{"headerName":"Field Type","field":"fieldType"},
	        		{"headerName":"Type","field":"type"}],
	        	rowData : $scope.localDataset.metadata.fieldsMeta
		};

		function resizeColumns(){
			$scope.columnsGridOptions.api.sizeColumnsToFit();
		}

		$scope.saveColumnConfiguration=function(){
			model = $scope.model;

			if(model.content.columnSelectedOfDataset == undefined){
				model.content.columnSelectedOfDataset = [];
			}
			for(var i in $scope.columnsGridOptions.api.getSelectedRows()){
				var obj = $scope.columnsGridOptions.api.getSelectedRows()[i];
				obj.aggregationSelected = 'SUM';
				obj.funcSummary = 'SUM';
				obj.typeSelected = obj.type;
				obj.label = obj.alias;
				obj.aliasToShow = obj.alias;
				model.content.columnSelectedOfDataset.push(obj);
			}

			$mdDialog.hide();
		}

		$scope.cancelConfiguration=function(){
			$mdDialog.cancel();
		}
	}

	$scope.$watch('newModel.content.columnSelectedOfDataset',function(newValue,oldValue){
		if($scope.columnsGrid.api && newValue){
			$scope.columnsGrid.api.setRowData(newValue);
			$scope.columnsGrid.api.sizeColumnsToFit();
		}
	},true)

	//Codemirror initializer
	$scope.codemirrorLoaded = function(_editor) {
        $scope._doc = _editor.getDoc();
        $scope._editor = _editor;
        _editor.focus();
        $scope._doc.markClean()
        _editor.on("beforeChange", function() {});
        _editor.on("change", function() {});
    };

    //codemirror options
    $scope.editorOptionsCss = {
        theme: 'eclipse',
        lineWrapping: true,
        lineNumbers: true,
        mode: {name:'css'},
        onLoad: $scope.codemirrorLoaded
    };
    $scope.editorOptionsHtml = {
        theme: 'eclipse',
        lineWrapping: true,
        lineNumbers: true,
        mode: {name: "xml", htmlMode: true},
        onLoad: $scope.codemirrorLoaded
    };
    $scope.editorOptionsJs = {
        theme: 'eclipse',
        lineWrapping: true,
        lineNumbers: true,
        mode: {name: "javascript"},
        onLoad: $scope.codemirrorLoaded
    };

	$scope.saveConfiguration=function(){
		 mdPanelRef.close();
		 angular.copy($scope.newModel,model);
		 $scope.$destroy();
		 finishEdit.resolve();
 	}
 	$scope.cancelConfiguration=function(){
 		mdPanelRef.close();
 		$scope.$destroy();
 		finishEdit.reject();
 	}



}
