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

function advancedTableWidgetEditControllerFunction($scope,$compile,finishEdit,$q,model,sbiModule_translate,$mdDialog,mdPanelRef,$mdToast,cockpitModule_datasetServices,cockpitModule_generalOptions, cockpitModule_analyticalDrivers, sbiModule_restServices,cockpitModule_template){
	$scope.translate=sbiModule_translate;
	$scope.newModel = angular.copy(model);
	$scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
	$scope.availableAggregations = ["NONE","SUM","AVG","MAX","MIN","COUNT","COUNT_DISTINCT"];
	$scope.availableSummaryAggregations = ["SUM","AVG","COUNT","COUNT_DISTINCT", "MAX", "MIN"];
	$scope.typesMap = cockpitModule_generalOptions.typesMap;
	$scope.allHidden = false;
	for(var k in $scope.newModel.content.columnSelectedOfDataset){
		if($scope.newModel.content.columnSelectedOfDataset[k].style && $scope.newModel.content.columnSelectedOfDataset[k].style.hiddenColumn) $scope.allHidden = true;
		else{
			$scope.allHidden = false;
			break;
		}
	}

	function uuidv4() {
	  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
	    return v.toString(16);
	  });
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
        sbiModule_restServices.restToRootProject();
        sbiModule_restServices.promiseGet('2.0/datasets', 'availableFunctions/' + dsId, "useCache=" + $scope.localDataset.useCache).then(function(response){
        	$scope.datasetAdditionalInfos = response.data;
        }, function(response) {
        	if(response.data && response.data.errors && response.data.errors[0]) $scope.showAction(response.data.errors[0].message);
        	else $scope.showAction($scope.translate.load('sbi.generic.error'));
        });
	}
	if($scope.newModel.dataset && $scope.newModel.dataset.dsId) $scope.getDatasetAdditionalInfo($scope.newModel.dataset.dsId);

	function moveInArray(arr, fromIndex, toIndex) {
        var element = arr[fromIndex];
        arr.splice(fromIndex, 1);
        arr.splice(toIndex, 0, element);
    }

	function getColumnsDefinition() {
		$scope.availableGroups = {0:''};
		for(var g in $scope.newModel.groups){
			if(!$scope.newModel.groups[g].id) $scope.newModel.groups[g].id = uuidv4();
			$scope.availableGroups[$scope.newModel.groups[g].id] = $scope.newModel.groups[g].name;
		}

		$scope.columnsDefinition = [
			{headerName: 'Name', field:'alias',"editable":isInputEditable,cellRenderer:editableCell, cellClass: 'editableCell',rowDrag: true},
	    	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.alias'), field:'aliasToShow',"editable":true,cellRenderer:editableCell, cellClass: 'editableCell'},
	    	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.type'), field: 'fieldType',"editable":true,cellRenderer:editableCell, cellClass: 'editableCell',cellEditor:"agSelectCellEditor",
	    		cellEditorParams: {values: ['ATTRIBUTE','MEASURE']}},{headerName: 'Data Type', field: 'type',cellRenderer:typeCell},
	    	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.aggregation'), field: 'aggregationSelected', cellRenderer: aggregationRenderer,"editable":isAggregationEditable, cellClass: 'editableCell',
	    		cellEditor:"agSelectCellEditor",cellEditorParams: {values: $scope.availableAggregations}},
	    	{headerName:"",cellRenderer: buttonRenderer,"field":"valueId","cellStyle":{"border":"none !important","text-align": "right","display":"inline-flex","justify-content":"flex-end"},width: 150,suppressSizeToFit:true, tooltip: false,
	    			headerComponent: CustomHeader,headerClass:'header-cell-buttons'}];
		if($scope.newModel.groups && $scope.newModel.groups.length > 0){
			delete $scope.columnsDefinition[0].rowDrag;
			$scope.columnsDefinition.unshift({headerName: 'Group', field:'group',"editable":isInputEditable,cellRenderer:groupRenderer, cellClass: 'editableCell',rowDrag: true,cellEditor:"agSelectCellEditor",
				cellEditorParams: {values: Object.keys($scope.availableGroups)},valueFormatter: groupSelectFormatter, valueParser: groupSelectParser});
		}
		if($scope.columnsGrid && $scope.columnsGrid.api) {
			$scope.columnsGrid.api.setColumnDefs($scope.columnsDefinition);
			$scope.columnsGrid.api.setRowData($scope.newModel.content.columnSelectedOfDataset);
			resizeColumns();
		}
	}
	getColumnsDefinition();

	$scope.columnsGrid = {
		angularCompileRows: true,
		animateRows: true,
		domLayout:'autoHeight',
        enableColResize: false,
        enableFilter: false,
        enableSorting: false,
        rowDragManaged: true,
        onRowDragEnter: rowDragEnter,
        onRowDragEnd: onRowDragEnd,
        onGridReady : resizeColumns,
        onCellEditingStopped: refreshRow,
        singleClickEdit: true,
        columnDefs: $scope.columnsDefinition,
		rowData: $scope.newModel.content.columnSelectedOfDataset
	}

	function rowDragEnter(event){
		if(!$scope.startingDragRow) $scope.startingDragRow = event.overIndex;
	}
	function onRowDragEnd(event){
		moveInArray($scope.newModel.content.columnSelectedOfDataset, $scope.startingDragRow, event.overIndex);
		delete $scope.startingDragRow;
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
		var value = '';
		if(typeof params.valueFormatted != 'undefined') value = params.valueFormatted;
		return '<i class="fa fa-edit"></i> <i>'+ value +'</i>';
	}

	function groupSelectFormatter(params) {
		return $scope.availableGroups[params.value];
	}

	function groupSelectParser(params) {
		for (var key in $scope.availableGroups) {
		    if ($scope.availableGroups.hasOwnProperty(key)) {
		      if (params.newValue === $scope.availableGroups[key]) {
		        return key;
		      }
		    }
		}
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
				'<md-button class="md-icon-button noMargin" ng-click="draw('+params.rowIndex+')" ng-style="{\'background-color\':newModel.content.columnSelectedOfDataset['+params.rowIndex+'].style[\'background-color\']}">'+
				'   <md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.columnstyle.icon")}}</md-tooltip>'+
				'	<md-icon ng-style="{\'color\':newModel.content.columnSelectedOfDataset['+params.rowIndex+'].style.color}" md-font-icon="fa fa-paint-brush" aria-label="Paint brush"></md-icon>'+
				'</md-button>'+
				'<md-button class="md-icon-button" ng-click="toggleColumnVisibility(\''+params.rowIndex+'\')">'+
				'   <md-tooltip md-delay="500">Hide/show Column</md-tooltip>'+
				'	<md-icon ng-if="newModel.content.columnSelectedOfDataset['+params.rowIndex+'].style.hiddenColumn" md-font-icon="fa fa-ban fa-stack-2x text-danger"></md-icon>'+
				'	<md-icon md-font-icon="fa fa-eye"></md-icon>'+
				'</md-button>'+
				'<md-button class="md-icon-button" ng-click="deleteColumn(\''+params.data.alias+'\',$event)"><md-icon md-font-icon="fa fa-trash"></md-icon><md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.column.delete")}}</md-tooltip></md-button>';
	}

	function CustomHeader() {
	}

	CustomHeader.prototype.init = function (agParams) {
	    this.agParams = agParams;
	    this.eGui = document.createElement('div');
	    this.eGui.innerHTML = '<md-button class="md-icon-button" ng-click="toggleAllColumnsVisibility()">'+
					'   <md-tooltip ng-if="allHidden" md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.column.showall")}}</md-tooltip>'+
					'   <md-tooltip ng-if="!allHidden" md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.column.hideall")}}</md-tooltip>'+
					'	<md-icon ng-if="!allHidden" md-font-icon="fa fa-ban fa-stack-2x text-danger"></md-icon>'+
					'	<md-icon md-font-icon="fa fa-eye"></md-icon>'+
					'</md-button>'+
					'<md-button class="md-icon-button" ng-click="deleteAllColumns($event)"><md-icon md-font-icon="fa fa-trash"></md-icon>'+
					'<md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.column.deleteall")}}</md-tooltip></md-button>';

        this.$scope = $scope;
        $compile(this.eGui)(this.$scope);
        this.$scope.params = agParams;
        window.setTimeout(this.$scope.$apply.bind(this.$scope), 0);
	};

	CustomHeader.prototype.getGui = function () {
	    return this.eGui;
	};

	$scope.toggleAllColumnsVisibility = function(){
		for(var k in $scope.newModel.content.columnSelectedOfDataset){
			if($scope.newModel.content.columnSelectedOfDataset[k].style) $scope.newModel.content.columnSelectedOfDataset[k].style.hiddenColumn = !$scope.allHidden;
	  		else $scope.newModel.content.columnSelectedOfDataset[k].style = {'hiddenColumn': !$scope.allHidden};
		}
		$scope.allHidden = !$scope.allHidden;
	}

	$scope.deleteAllColumns = function($event){
		var confirm = $mdDialog.confirm()
	        .title($scope.translate.load("sbi.cockpit.widgets.table.confirm.delete"))
	        .textContent($scope.translate.load("sbi.cockpit.widgets.table.confirm.delete.text"))
	        .targetEvent($event)
	        .ok($scope.translate.load("sbi.general.continue"))
	        .cancel($scope.translate.load("sbi.generic.cancel"));

		  $mdDialog.show(confirm).then(function() {
			  $scope.newModel.content.columnSelectedOfDataset = [];
		  }, function() {});
	}

	function headerButtonRenderer() {
		return 	'<div>'+
				'    <div ref="eLabel" class="ag-header-cell-label" role="presentation" style="justify-content:flex-end"></div>'+
				'	 <md-button class="md-icon-button" ng-click="deleteAllColumns($event)"><md-icon md-font-icon="fa fa-trash"></md-icon>'+
				'	 <md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.column.delete")}}</md-tooltip></md-button>'+
				'</div>';
	}

	function refreshRow(cell){
		if(cell.data.fieldType == 'MEASURE' && !cell.data.aggregationSelected) cell.data.aggregationSelected = 'SUM';
		if(cell.data.fieldType == 'MEASURE' && cell.data.aggregationSelected) cell.data.funcSummary = cell.data.aggregationSelected == 'NONE' ? 'SUM' : cell.data.aggregationSelected;
		if(cell.data.isCalculated) cell.data.alias = cell.data.aliasToShow;
		$scope.columnsGrid.api.redrawRows({rowNodes: [$scope.columnsGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
	}

	$scope.colorPickerPropertyTh = {
			format:'rgb',
			placeholder:sbiModule_translate.load('sbi.cockpit.color.select'),
			disabled:($scope.newModel.style.th && $scope.newModel.style.th.enabled === false)
	};

	$scope.colorPickerMultiselection = {
			format:'rgb',
			placeholder:sbiModule_translate.load('sbi.cockpit.color.select'),
			disabled: !$scope.newModel.settings.multiselectable
	}
	$scope.enableMultiselect = function(){
		$scope.colorPickerMultiselection.disabled = $scope.newModel.settings.multiselectable;
	}

	$scope.toggleTh = function(){
		$scope.colorPickerPropertyTh.disabled = $scope.newModel.style.th.enabled;
	}

  	$scope.initTh = function(){
  		return typeof($scope.newModel.style.th.enabled) != 'undefined' ? $scope.newModel.style.th.enabled : true;
  	}

  	$scope.toggleColumnVisibility = function(rowIndex){
  		if($scope.newModel.content.columnSelectedOfDataset[rowIndex].style) $scope.newModel.content.columnSelectedOfDataset[rowIndex].style.hiddenColumn = !$scope.newModel.content.columnSelectedOfDataset[rowIndex].style.hiddenColumn;
  		else $scope.newModel.content.columnSelectedOfDataset[rowIndex].style = {'hiddenColumn': true};
  	}

	$scope.draw = function(rowIndex) {
		$scope.selectedColumn = $scope.newModel.content.columnSelectedOfDataset[rowIndex];

		$mdDialog.show({
			templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitColumnStyle.html',
			parent : angular.element(document.body),
			clickOutsideToClose:true,
			escapeToClose :true,
			preserveScope: false,
			autoWrap:false,
			fullscreen: true,
			locals:{model:$scope.newModel, selectedColumn : $scope.selectedColumn, dialogOptions: {}},
			controller: cockpitStyleColumnFunction
		}).then(function(answer) {
			console.log("Selected column:", $scope.selectedColumn);
		}, function() {
			console.log("Selected column:", $scope.selectedColumn);
		});
	}

	$scope.openColumnGroups = function(){
		$mdDialog.show({
			templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitColumnsGroup.html',
			parent : angular.element(document.body),
			clickOutsideToClose:true,
			escapeToClose :true,
			locals: {model:$scope.newModel},
			fullscreen: true,
			controller: columnsGroupController
		}).then(function(model) {
			$scope.newModel = model;
			getColumnsDefinition();
			}, function() {
			});
	}

	function columnsGroupController(scope,sbiModule_translate,cockpitModule_generalOptions,$mdDialog,model){
		scope.translate=sbiModule_translate;
		scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
		scope.model = angular.copy(model);

		scope.addGroup = function(){
			var id = uuidv4();
			if(scope.model.groups) scope.model.groups.push({id:id});
			else scope.model.groups = [{id:id}];
		}

		scope.deleteGroup = function(i){
			scope.model.groups.splice(i,1);
			if(scope.model.groups.length == 0) delete scope.model.groups;
		}

		scope.cancel = function(){
			$mdDialog.cancel();
		}

		scope.saveGroups = function(){
			for(var k in scope.model.groups){
				if(!scope.model.groups[k].id) scope.model.groups[k].id = uuidv4();
			}
			$mdDialog.hide(scope.model);
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

	$scope.showAction = function(text) {
		var toast = $mdToast.simple()
				.content(text)
				.action('OK')
				.highlightAction(false)
				.hideDelay(3000)
				.position('top')

		$mdToast.show(toast).then(function(response) {
			if ( response == 'ok' ) {
			}
		});
	}
	$scope.checkAggregation = function(){
		var isAggregated;
		for(var i in $scope.newModel.content.columnSelectedOfDataset){
			var column = $scope.newModel.content.columnSelectedOfDataset[i];
			if(column.fieldType == 'MEASURE'){
				if(!column.isCalculated || column.datasetOrTableFlag == true){
					if(column.aggregationSelected != 'NONE'){
						if(isAggregated == false) return false;
						else isAggregated = true;
					}else{
						if(isAggregated == true) return false;
						else isAggregated = false;
					}
				}
			}
		}
		return true;
	}

	$scope.checkAliases = function(){
		var columns = $scope.newModel.content.columnSelectedOfDataset;
		for(var i = 0; i < columns.length - 1; i++){
			for(var j = i + 1; j < columns.length; j++){
				if(columns[i].aliasToShow == columns[j].aliasToShow){
					return false;
				}
			}
		}
		return true;
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
				if($scope.newModel.settings.sortingColumn == colsToRemove[j].aliasToShow){
					$scope.newModel.settings.sortingColumn = null;
				}
			}
		}
	}

	$scope.addSummaryRow = function(){
		$scope.newModel.settings.summary.list.push({});
	}

	$scope.removeSummaryRow = function(i){
		$scope.newModel.settings.summary.list.splice(i,1);
	}

	$scope.checkForPinnedColumns = function(columns){
		for(var k in columns){
			if(columns[k].pinned) return false;
		}
		return true;
	}

	$scope.$watch('newModel.settings.summary.enabled',function(newValue,oldValue){
		if(newValue){
			if($scope.newModel.settings.summary.style && $scope.newModel.settings.summary.style.pinnedOnly){
				$scope.showNoPinnedColumnWarning = $scope.checkForPinnedColumns($scope.newModel.content.columnSelectedOfDataset);
			}
			if(!$scope.newModel.settings.summary.list) $scope.newModel.settings.summary.list = [{}];
		}
	})

	$scope.$watch('newModel.content.columnSelectedOfDataset',function(newValue,oldValue){
		if($scope.newModel.settings.summary && $scope.newModel.settings.summary.style && $scope.newModel.settings.summary.style.pinnedOnly){
			$scope.showNoPinnedColumnWarning = $scope.checkForPinnedColumns(newValue);
		}
		if($scope.columnsGrid.api && newValue){
			$scope.columnsGrid.api.setRowData(newValue);
			$scope.columnsGrid.api.sizeColumnsToFit();
		}
	},true)

	$scope.saveConfiguration=function(){
	    if($scope.newModel.dataset == undefined || $scope.newModel.dataset.dsId == undefined ){
            $scope.showAction($scope.translate.load('sbi.cockpit.table.missingdataset'));
            return;
        }
        if($scope.newModel.content.columnSelectedOfDataset == undefined || $scope.newModel.content.columnSelectedOfDataset.length==0){
            $scope.showAction($scope.translate.load('sbi.cockpit.table.nocolumns'));
            return;
        }
        if(!$scope.checkAliases()){
            $scope.showAction($scope.translate.load('sbi.cockpit.table.erroraliases'));
            return;
        }
        if(!$scope.checkSolrRequirements()){
            $scope.showAction($scope.translate.load('sbi.cockpit.table.errorsolr'));
            return;
        }

        if($scope.newModel.settings.summary && $scope.newModel.settings.summary && $scope.newModel.settings.summary.enabled){
			for(var k in $scope.newModel.content.columnSelectedOfDataset){
				if(!$scope.newModel.content.columnSelectedOfDataset[k].datasetOrTableFlag){
					$scope.newModel.content.columnSelectedOfDataset[k].funcSummary = $scope.newModel.content.columnSelectedOfDataset[k].aggregationSelected == 'NONE' ? 'SUM' : $scope.newModel.content.columnSelectedOfDataset[k].aggregationSelected;
				}
			}
		}

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


    $scope.checkAliases = function(){
        var columns = $scope.newModel.content.columnSelectedOfDataset;
        for(var i = 0; i < columns.length - 1; i++){
            for(var j = i + 1; j < columns.length; j++){
                if(columns[i].aliasToShow.toLowerCase() == columns[j].aliasToShow.toLowerCase()){
                    return false;
                }
            }
        }
        return true;
    }

    $scope.checkSolrRequirements = function(){
        if(cockpitModule_datasetServices.getDatasetById($scope.newModel.dataset.dsId).type == 'SbiSolrDataSet'){
            return !$scope.newModel.settings.pagination.enabled || $scope.newModel.settings.pagination.frontEnd;
        }
        return true;
    }

    $scope.showAction = function(text) {
        var toast = $mdToast.simple()
                .content(text)
                .action('OK')
                .highlightAction(false)
                .hideDelay(3000)
                .position('top')

        $mdToast.show(toast);
    }

}