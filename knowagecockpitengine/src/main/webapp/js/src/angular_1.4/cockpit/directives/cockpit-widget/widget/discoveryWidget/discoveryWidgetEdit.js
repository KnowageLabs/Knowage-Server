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
	.controller('discoveryWidgetEditControllerFunction',discoveryWidgetEditControllerFunction)

function discoveryWidgetEditControllerFunction(
		$scope,
		$compile,
		finishEdit,
		model,
		sbiModule_translate,
		cockpitModule_datasetServices,
		cockpitModule_generalServices,
		cockpitModule_generalOptions,
		mdPanelRef,
		cockpitModule_analyticalDrivers,
		$mdDialog
		){
	$scope.translate=sbiModule_translate;
	$scope.cockpitModule_analyticalDrivers = cockpitModule_analyticalDrivers;
	$scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
	$scope.newModel = angular.copy(model);
	$scope.textAlignments = [{text:'left',align:'flex-start'},{text:'center',align:'center'},{text:'right',align:'flex-end'}];
	$scope.defaultTextSearchType = [{label: $scope.translate.load('kn.cockpit.discovery.defaultsearchstatic'),value:'static'},
		{label:$scope.translate.load('kn.cockpit.discovery.defaultsearchanalytic'),value:'driver'}];

	$scope.checkSelectedColumnsConsistency = function(){
		if($scope.newModel.dataset && $scope.newModel.dataset.dsId){
			$scope.local = cockpitModule_datasetServices.getDatasetById($scope.newModel.dataset.dsId);
			if($scope.newModel.content.columnSelectedOfDataset){
				for(var k in $scope.newModel.content.columnSelectedOfDataset){
					for(var j in $scope.local.metadata.fieldsMeta){
						if($scope.local.metadata.fieldsMeta[j].name == $scope.newModel.content.columnSelectedOfDataset[k].name){
							$scope.newModel.content.columnSelectedOfDataset[k].type = $scope.local.metadata.fieldsMeta[j].type;
							$scope.newModel.content.columnSelectedOfDataset[k].properties = $scope.local.metadata.fieldsMeta[j].properties;
							break;
						}
					}
				}
			}
		}
	}
	$scope.checkSelectedColumnsConsistency();

	$scope.columnsGrid = {
		angularCompileRows: true,
		enableColResize: false,
        enableSorting: false,
        enableFilter: false,
        rowDragManaged: true,
        onRowDragEnter: rowDragEnter,
        onRowDragEnd: onRowDragEnd,
        onGridReady: resizeColumns,
        onGridSizeChanged: resizeColumns,
        onCellEditingStopped: refreshRow,
        columnDefs: [{"headerName":"Column","field":"name",rowDrag: true},
    		{"headerName":"Alias","field":"alias","editable":true,cellRenderer:editableCell, cellClass: 'editableCell'},
    		{"headerName":"Type","field":"fieldType","editable":true,cellRenderer:editableCell, cellClass: 'editableCell',cellEditor:"agSelectCellEditor",cellEditorParams: {values: ['ATTRIBUTE','MEASURE']}  },
    		{"headerName":"Style","field":"style", cellStyle: {border:"none"},cellRenderer:styleRenderer,width: 50, headerClass:'header-cell-center'},
    		{"headerName":"Show Column","field":"visible",cellRenderer:checkboxRenderer,width: 100,headerComponent: CustomHeader, headerClass:'header-cell-center'},
    		{"headerName":"Show Facet","field":"facet",cellRenderer:checkboxRendererAggregated,width: 100,headerComponent: CustomHeader, headerClass:'header-cell-center'},
    		{"headerName":"Enable text search","field":"fullTextSearch",cellRenderer:checkboxRenderer,width: 100,headerComponent: CustomHeader,headerClass:'header-cell-center'}],
    	rowData : $scope.newModel.content.columnSelectedOfDataset
	};

	function moveInArray(arr, fromIndex, toIndex) {
        var element = arr[fromIndex];
        arr.splice(fromIndex, 1);
        arr.splice(toIndex, 0, element);
    }

	function rowDragEnter(event){
		$scope.startingDragRow = event.overIndex;
	}
	function onRowDragEnd(event){
		moveInArray($scope.newModel.content.columnSelectedOfDataset, $scope.startingDragRow, event.overIndex);
		$scope.columnsGrid.api.redrawRows();
	}

	function CustomHeader() {}

	CustomHeader.prototype.init = function (agParams) {
	    this.agParams = agParams;
	    this.eGui = document.createElement('div');
	    this.eGui.innerHTML = '<span>'+agParams.displayName+'</span><input type="checkbox" ng-model="selectAll.'+agParams.column.colId+'" ng-change="changeSelectAll(\''+agParams.column.colId+'\')"/>';

        this.$scope = $scope;
        $compile(this.eGui)(this.$scope);
        this.$scope.params = agParams;
        window.setTimeout(this.$scope.$apply.bind(this.$scope), 0);
	};

	CustomHeader.prototype.getGui = function () {return this.eGui;};

	function resizeColumns(){
		$scope.columnsGrid.api.sizeColumnsToFit();
	}

	$scope.selectAll = {
			visible: true,
			fullTextSearch: false,
			facet: false
	};

	for(var k in $scope.newModel.content.columnSelectedOfDataset){
		if($scope.newModel.content.columnSelectedOfDataset[k].visible) $scope.selectAll.visible = true;
		if($scope.newModel.content.columnSelectedOfDataset[k].fullTextSearch) $scope.selectAll.fullTextSearch = true;
		if($scope.newModel.content.columnSelectedOfDataset[k].facet) $scope.selectAll.facet = true;
	}

	$scope.changeSelectAll = function(columnId){
		for(var k in $scope.newModel.content.columnSelectedOfDataset){
			if((columnId != 'facet') || (columnId == 'facet' && $scope.newModel.content.columnSelectedOfDataset[k].fieldType != 'MEASURE')){
				$scope.newModel.content.columnSelectedOfDataset[k][columnId] = $scope.selectAll[columnId];
			}
		}
	}

	function editableCell(params){
		return '<i class="fa fa-edit"></i> <i>'+params.value+'</i>';
	}

	function checkboxRenderer(params){
		return '<div style="display:inline-flex;justify-content:center;width:100%;height:100%;align-items:center;"><input type="checkbox" ng-model="newModel.content.columnSelectedOfDataset['+params.rowIndex+'][\''+params.column.colId+'\']"/></div>';
	}

	function checkboxRendererAggregated(params){
		var input = '<input type="checkbox" ng-model="newModel.content.columnSelectedOfDataset['+params.rowIndex+'][\''+params.column.colId+'\']"/>';
		var button = '<md-button class="md-icon-button" ng-if="newModel.content.columnSelectedOfDataset['+params.rowIndex+'][\''+params.column.colId+'\']" ng-click="showAggregationDialog($event,'+params.rowIndex+')" >'+
				  	'	<md-tooltip md-delay="500">Select aggregation</md-tooltip>'+
				  	'	<md-icon md-font-icon="fa fa-pencil"></md-icon>'+
				  	'</md-button>';
		if(params.node.data.fieldType == 'ATTRIBUTE' && !cockpitModule_generalServices.isNumericColumn(params.node.data)){
			return '<div style="display:inline-flex;justify-content:center;width:100%;height:100%;align-items:center;">'+input+button+'</div>';
		}else{
		    params.node.data.facet = false;
		    return '<span></span>';
		}
	}

	function styleRenderer(params){
		return 	'<div style="display:inline-flex;justify-content:center;width:100%;"><md-button class="md-icon-button" ng-click="showSettingsDialog($event,'+params.rowIndex+')" ng-style="{\'background-color\':newModel.content.columnSelectedOfDataset['+params.rowIndex+'].style[\'background-color\']}">'+
			  	'	<md-tooltip md-delay="500">Column Style</md-tooltip>'+
			  	'	<md-icon md-font-icon="fa fa-paint-brush" ng-style="{\'color\':newModel.content.columnSelectedOfDataset['+params.rowIndex+'].style.color}"></md-icon>'+
			  	'</md-button></div>';
	}

	function aggregationRenderer(params){
		return '<div style="display:inline-flex;justify-content:center;width:100%;"><md-button class="md-icon-button" ng-click="showAggregationDialog($event,'+params.rowIndex+')" >'+
			  	'	<md-tooltip md-delay="500">Column Settings</md-tooltip>'+
			  	'	<md-icon md-font-icon="fa fa-pencil"></md-icon>'+
			  	'</md-button></div>';
	}

	function refreshRow(cell){
		if(cell.node.data.fieldType == 'MEASURE') $scope.newModel.content.columnSelectedOfDataset[cell.rowIndex].facet = false;
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

  	$scope.getTemplateUrl = function(template){
  		return cockpitModule_generalServices.getTemplateUrl('discoveryWidget',template);
  	}

  	$scope.initProperty = function(property){
  		if(typeof property == 'undefined') return true
  		return property;
  	}

  	$scope.initTh = function(){
  		return typeof($scope.newModel.style.th.enabled) != 'undefined' ? $scope.newModel.style.th.enabled : true;
  	}

  	$scope.initAggregation = function(col){
  		if(typeof col.aggregationSelected == 'undefined') return 'COUNT';
  		return col.aggregationSelected;
  	}

  	$scope.resetAggregations = function(col){
  		if(col.fieldType == 'ATTRIBUTE') col.aggregationSelected = 'COUNT';
  	}

  	$scope.handleEvent = function(event ,dsId){
  		if($scope.newModel.dataset) $scope.newModel.dataset.dsId = dsId;
  		else $scope.newModel.dataset = {"dsId":dsId};
  		$scope.local = cockpitModule_datasetServices.getDatasetById($scope.newModel.dataset.dsId);
  		$scope.newModel.dataset.label = $scope.local.label;
  		$scope.newModel.content.columnSelectedOfDataset = $scope.local.metadata.fieldsMeta;
  		for(var c in $scope.newModel.content.columnSelectedOfDataset){
  			$scope.newModel.content.columnSelectedOfDataset[c].visible = true;
  			$scope.newModel.content.columnSelectedOfDataset[c].facet = true;
  			$scope.newModel.content.columnSelectedOfDataset[c].fullTextSearch = true;
  			$scope.newModel.content.columnSelectedOfDataset[c].aggregationSelected = 'COUNT';
  			$scope.newModel.content.columnSelectedOfDataset[c].aggregationColumn = $scope.newModel.content.columnSelectedOfDataset[c].name;
  		}
  		$scope.columnsGrid.api.setRowData($scope.newModel.content.columnSelectedOfDataset);
  	}

  	$scope.addColumn = function(){
  		$scope.newModel.content.columnSelectedOfDataset.push({});
  	}

  	$scope.deleteColumn = function(col){
  		for(var k in $scope.newModel.content.columnSelectedOfDataset){
  			if($scope.newModel.content.columnSelectedOfDataset[k].$$hashKey == col.$$hashKey) {
  				$scope.newModel.content.columnSelectedOfDataset.splice(k,1);
  				return;
  			}
  		}
  	}

  	$scope.showSettingsDialog = function(ev,index){
	    $mdDialog.show({
	      controller: settingsDialogContent,
	      templateUrl: $scope.getTemplateUrl('discoveryWidgetColumnStyleTemplate'),
	      parent: angular.element(document.body),
	      targetEvent: ev,
	      clickOutsideToClose:true,
	      locals: {column:$scope.newModel.content.columnSelectedOfDataset[index],index:index}
	    })
        .then(function(column) {
        	$scope.newModel.content.columnSelectedOfDataset[index] = column;
        }, function() {
        });
  	}

  	function settingsDialogContent($scope, $mdDialog, column, index){
  		$scope.translate=sbiModule_translate;
  		$scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
  		$scope.selectedColumn = angular.copy(column);
  		$scope.textAlignments = [{text:'left',align:'flex-start'},{text:'center',align:'center'},{text:'right',align:'flex-end'}];
  		$scope.colorPickerProperty = {format:'rgb', placeholder:sbiModule_translate.load('sbi.cockpit.color.select')};

  		$scope.isDateColumn = function(type){
  			if(type == 'oracle.sql.TIMESTAMP' || type == 'java.sql.Timestamp' || type == 'java.util.Date' || type == 'java.sql.Date' || type == 'java.sql.Time'){
  				return true;
  			}
  			return false;
  		}

  		$scope.cancel = function(){
  			$mdDialog.cancel();
  		}

  		$scope.save = function(){
  			$mdDialog.hide($scope.selectedColumn);
  		}
  	}

  	$scope.showAggregationDialog = function(ev,index){
  		 $mdDialog.show({
  		      controller: aggregationDialogContent,
  		      templateUrl: $scope.getTemplateUrl('discoveryWidgetColumnAggregationTemplate'),
  		      parent: angular.element(document.body),
  		      targetEvent: ev,
  		      clickOutsideToClose:true,
  		      locals: {columns:$scope.newModel.content.columnSelectedOfDataset,index:index}
  		    })
  	        .then(function(column) {
  	        	$scope.newModel.content.columnSelectedOfDataset[index] = column;
  	        }, function() {
  	        });
  	}

  	function aggregationDialogContent($scope, $mdDialog, columns, index){
  		$scope.translate=sbiModule_translate;
  		$scope.columns = columns;
  		$scope.selectedColumn = angular.copy(columns[index]);
  		if(!$scope.selectedColumn.aggregationColumn) $scope.selectedColumn.aggregationColumn = $scope.selectedColumn.name;
  		$scope.availableAggregations = [
  			{name:'COUNT',available:['MEASURE','ATTRIBUTE']},
  			{name:'SUM',available:['MEASURE','ATTRIBUTE']},
  			{name:'AVG',available:['MEASURE','ATTRIBUTE']}
  		];

  		$scope.cancel = function(){
  			$mdDialog.cancel();
  		}

  		$scope.save = function(){
  			$mdDialog.hide($scope.selectedColumn);
  		}
  	}

  	//MAIN DIALOG BUTTONS
	$scope.saveConfiguration=function(){
		mdPanelRef.close();
		angular.copy($scope.newModel,model);
		finishEdit.resolve();
		$scope.$destroy();
  	}

	$scope.cancelConfiguration=function(){
  		mdPanelRef.close();
  		finishEdit.reject();
  		$scope.$destroy();
  	}
}