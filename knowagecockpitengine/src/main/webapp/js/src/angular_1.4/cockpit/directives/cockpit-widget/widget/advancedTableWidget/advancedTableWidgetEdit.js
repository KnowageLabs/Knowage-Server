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
	.controller('advancedTableWidgetEditControllerFunction',advancedTableWidgetEditControllerFunction)

function advancedTableWidgetEditControllerFunction($scope,finishEdit,$q,model,sbiModule_translate,$mdDialog,mdPanelRef,$mdToast,cockpitModule_datasetServices,cockpitModule_generalOptions, cockpitModule_analyticalDrivers){
	$scope.translate=sbiModule_translate;
	$scope.newModel = angular.copy(model);
	$scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
	$scope.availableAggregations = ["NONE","SUM","AVG","MAX","MIN","COUNT","COUNT_DISTINCT"];
	
	$scope.changeDS = function(id){
		$scope.newModel.content.columnSelectedOfDataset = cockpitModule_datasetServices.getDatasetById(id).metadata.fieldsMeta;
		$scope.columnsGrid.api.setRowData($scope.newModel.content.columnSelectedOfDataset);
	}
	
	$scope.columnsDefition = [
    	{headerName: 'Name', field:'alias',"editable":isInputEditable,cellRenderer:editableCell, cellClass: 'editableCell',rowDrag: true},
    	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.alias'), field:'aliasToShow',"editable":true,cellRenderer:editableCell, cellClass: 'editableCell'},
    	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.type'), field: 'fieldType',"editable":true,cellRenderer:editableCell, cellClass: 'editableCell',cellEditor:"agSelectCellEditor",
    		cellEditorParams: {values: ['ATTRIBUTE','MEASURE']}},
    	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.aggregation'), field: 'aggregationSelected', cellRenderer: aggregationRenderer,"editable":isAggregationEditable, cellClass: 'editableCell',
    		cellEditor:"agSelectCellEditor",cellEditorParams: {values: $scope.availableAggregations}},
    	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.summaryfunction'), field: 'funcSummary', cellRenderer: aggregationRenderer,"editable":isAggregationEditable, cellClass: 'editableCell',
    		cellEditor:"agSelectCellEditor",cellEditorParams: {values: $scope.availableAggregations}},
    	{headerName:"",cellRenderer: buttonRenderer,"field":"valueId","cellStyle":{"border":"none !important","text-align": "right","display":"inline-flex","justify-content":"flex-end"},width: 150,suppressSizeToFit:true, tooltip: false}];
	
	$scope.columnsGrid = {
			angularCompileRows: true,
			domLayout: 'autoHeight',
	        enableColResize: false,
	        enableFilter: false,
	        enableSorting: false,
	        onRowDragMove: onRowDragMove,
	        onGridReady : resizeColumns,
	        onCellEditingStopped: refreshRow,
	        singleClickEdit: true,
	        stopEditingWhenGridLosesFocus: true,
	        columnDefs: $scope.columnsDefition,
			rowData: $scope.newModel.content.columnSelectedOfDataset
		}
	
	function onRowDragMove(event) {
	    if (event.node !== event.overNode) {
	    	var fromIndex = $scope.newModel.content.columnSelectedOfDataset.indexOf(event.node.data);
	    	var toIndex = $scope.newModel.content.columnSelectedOfDataset.indexOf(event.overNode.data);

	    	var newStore = $scope.newModel.content.columnSelectedOfDataset.slice();
	        moveInArray(newStore, fromIndex, toIndex);
	        $scope.newModel.content.columnSelectedOfDataset = newStore;
	        
	        $scope.columnsGrid.api.setRowData(newStore);
	        $scope.columnsGrid.api.clearFocusedCell();
	    }

	    function moveInArray(arr, fromIndex, toIndex) {
	        var element = arr[fromIndex];
	        arr.splice(fromIndex, 1);
	        arr.splice(toIndex, 0, element);
	    }
	}
	
	function resizeColumns(){
		$scope.columnsGrid.api.sizeColumnsToFit();
	}
	
	function editableCell(params){
		return typeof(params.value) !== 'undefined' ? '<i class="fa fa-edit"></i> <i>'+params.value+'<md-tooltip>'+params.value+'</md-tooltip></i>' : '';
	}
	function isInputEditable(params) {
		return typeof(params.data.name) !== 'undefined'; 
	}
	function isAggregationEditable(params) {
		return params.data.fieldType == "MEASURE" ? true : false;
	}
	
	function aggregationRenderer(params) {
		var aggregation = '<i class="fa fa-edit"></i> <i>'+params.value+'</i>';
		return params.data.fieldType == "MEASURE" ? aggregation : '';
	}
	
	function buttonRenderer(params){
		var calculator = '';
		if(params.data.isCalculated){
			calculator = '<calculated-field ng-model="newModel" selected-item="'+params.rowIndex+'"></calculated-field>';
		}
		return 	calculator +
				'<md-button class="md-icon-button noMargin" ng-click="draw(\''+params.data.alias+'\')" ng-style="{\'background-color\':newModel.content.columnSelectedOfDataset['+params.rowIndex+'].style[\'background-color\']}">'+
				'   <md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.columnstyle.icon")}}</md-tooltip>'+
				'	<md-icon ng-style="{\'color\':newModel.content.columnSelectedOfDataset['+params.rowIndex+'].style.color}" md-font-icon="fa fa-paint-brush" aria-label="Paint brush"></md-icon>'+
				'</md-button>'+
				'<md-button class="md-icon-button" ng-click="deleteColumn(\''+params.data.alias+'\',$event)"><md-icon md-font-icon="fa fa-trash"></md-icon><md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.column.delete")}}</md-tooltip></md-button>';
	}
	
	function refreshRow(cell){
		$scope.columnsGrid.api.redrawRows({rowNodes: [$scope.columnsGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
	}
	
	$scope.colorPickerPropertyTh = {
			format:'rgb', 
			placeholder:sbiModule_translate.load('sbi.cockpit.color.select'), 
			disabled:($scope.newModel.style.th && $scope.newModel.style.th.enabled === false)
	};
	
	$scope.toggleTh = function(){
		$scope.colorPickerPropertyTh.disabled = $scope.newModel.style.th.enabled;
	}
  	
  	$scope.initTh = function(){
  		return typeof($scope.newModel.style.th.enabled) != 'undefined' ? $scope.newModel.style.th.enabled : true;
  	}
	
	$scope.draw = function(rowName) {
		for(var k in $scope.newModel.content.columnSelectedOfDataset){
			if($scope.newModel.content.columnSelectedOfDataset[k].alias == rowName) $scope.selectedColumn = $scope.newModel.content.columnSelectedOfDataset[k];
		}
		
		$mdDialog.show({
			templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitColumnStyle.html',
			parent : angular.element(document.body),
			clickOutsideToClose:true,
			escapeToClose :true,
			preserveScope: false,
			autoWrap:false,
			fullscreen: true,
			locals:{model:$scope.newModel, selectedColumn : $scope.selectedColumn},
			controller: cockpitStyleColumnFunction
		}).then(function(answer) {
			console.log("Selected column:", $scope.selectedColumn);
		}, function() {
			console.log("Selected column:", $scope.selectedColumn);
		});
	},
	
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
	
	function controllerCockpitColumnsConfigurator($scope,sbiModule_translate,$mdDialog,model,cockpitModule_datasetServices,cockpitModule_generalOptions){
		$scope.translate=sbiModule_translate;

		$scope.cockpitModule_generalOptions=cockpitModule_generalOptions;
		$scope.model = model;
		$scope.columnSelected = [];
		$scope.localDataset = {};
		if($scope.model.dataset && $scope.model.dataset.dsId){
			angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.localDataset);
		} else{
			$scope.model.dataset= {};
			angular.copy([], $scope.model.dataset.metadata.fieldsMeta);
		}
		$scope.saveColumnConfiguration=function(){
			model = $scope.model;

			if(model.content.columnSelectedOfDataset == undefined){
				model.content.columnSelectedOfDataset = [];
			}

			for(var i=0;i<$scope.columnSelected.length;i++){
				var obj = $scope.columnSelected[i];
				obj.aggregationSelected = 'SUM';
				obj["funcSummary"] = "SUM";
				obj.typeSelected = $scope.columnSelected[i].type;
				obj.label = $scope.columnSelected[i].alias;
				obj.aliasToShow = $scope.columnSelected[i].alias;
				model.content.columnSelectedOfDataset.push(obj);
			}

			$mdDialog.hide();
		}

		$scope.cancelConfiguration=function(){
			$mdDialog.cancel();
		}
	}
	
	$scope.deleteColumn = function(rowName,event) {
		for(var k in $scope.newModel.content.columnSelectedOfDataset){
			if($scope.newModel.content.columnSelectedOfDataset[k].alia == rowName) var item = $scope.newModel.content.columnSelectedOfDataset[k];
		}
  		  var index=$scope.newModel.content.columnSelectedOfDataset.indexOf(item);
		  $scope.newModel.content.columnSelectedOfDataset.splice(index,1);
		  if($scope.newModel.settings.sortingColumn == item.aliasToShow){
			  $scope.newModel.settings.sortingColumn = null;
		  }
	  }

	$scope.$watchCollection('newModel.content.columnSelectedOfDataset',function(newValue,oldValue){
		if($scope.columnsGrid.api && newValue){
			$scope.columnsGrid.api.setRowData(newValue);
			$scope.columnsGrid.api.sizeColumnsToFit();
		}
	})
	
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
