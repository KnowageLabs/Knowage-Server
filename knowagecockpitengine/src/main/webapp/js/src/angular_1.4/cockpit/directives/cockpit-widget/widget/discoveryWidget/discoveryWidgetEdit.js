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
		finishEdit,
		model,
		sbiModule_translate,
		cockpitModule_datasetServices,
		cockpitModule_generalServices,
		cockpitModule_generalOptions,
		mdPanelRef,
		$mdDialog
		){
	$scope.translate=sbiModule_translate;
	$scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
	$scope.newModel = angular.copy(model);
	$scope.textAlignments = [{text:'left',align:'flex-start'},{text:'center',align:'center'},{text:'right',align:'flex-end'}];
	
	$scope.columnsGrid = {
		angularCompileRows: true,
		enableColResize: false,
        enableSorting: true,
        enableFilter: false,
        onGridReady: resizeColumns,
        onGridSizeChanged: resizeColumns,
        columnDefs: [{"headerName":"Column","field":"name"},
    		{"headerName":"Alias","field":"alias","editable":true,cellRenderer:editableCell, cellClass: 'editableCell'},
    		{"headerName":"Type","field":"fieldType","editable":true,cellRenderer:editableCell, cellClass: 'editableCell',cellEditor:"agSelectCellEditor",cellEditorParams: {values: ['ATTRIBUTE','MEASURE']}  },
    		{"headerName":"Style","field":"style", cellStyle: {border:"none"},cellRenderer:styleRenderer,width: 50},
    		{"headerName":"Aggregation","field":"aggregationSelected", cellRenderer:aggregationRenderer,width: 80},
    		{"headerName":"Show Column","field":"visible",cellRenderer:checkboxRenderer,width: 100},
    		{"headerName":"Show Facet","field":"facet",cellRenderer:checkboxRenderer,width: 100},
    		{"headerName":"Enable text search","field":"fullTextSearch",cellRenderer:checkboxRenderer,width: 100}],
    	rowData : $scope.newModel.content.columnSelectedOfDataset
	};
	
	function resizeColumns(){
		$scope.columnsGrid.api.sizeColumnsToFit();
	}
	
	function editableCell(params){
		return '<i>'+params.value+'</i>';
	}
	
	function checkboxRenderer(params){
		return '<div style="display:inline-flex;justify-content:center;width:100%;"><input type="checkbox" ng-model="newModel.content.columnSelectedOfDataset['+params.rowIndex+'][\''+params.column.colId+'\']"/></div>'
	}
	
	function styleRenderer(params){
		return 	'<div style="display:inline-flex;justify-content:center;width:100%;"><md-button class="md-icon-button" ng-click="showSettingsDialog($event,'+params.rowIndex+')" ng-style="{\'background-color\':newModel.content.columnSelectedOfDataset['+params.rowIndex+'].style[\'background-color\']}">'+
			  	'	<md-tooltip md-delay="500">Column Settings</md-tooltip>'+
			  	'	<md-icon md-font-icon="fa fa-paint-brush" ng-style="{\'color\':newModel.content.columnSelectedOfDataset['+params.rowIndex+'].style.color}"></md-icon>'+
			  	'</md-button></div>';
	}
	
	function aggregationRenderer(params){
		return '<div style="display:inline-flex;justify-content:center;width:100%;"><md-button class="md-icon-button" ng-click="showAggregationDialog($event,'+params.rowIndex+')" >'+
			  	'	<md-tooltip md-delay="500">Column Settings</md-tooltip>'+
			  	'	<md-icon md-font-icon="fa fa-pencil"></md-icon>'+
			  	'</md-button></div>';
	}
	
	if($scope.newModel.dataset && $scope.newModel.dataset.dsId){
		$scope.local = cockpitModule_datasetServices.getDatasetById($scope.newModel.dataset.dsId);
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
  		$scope.selectedColumn = angular.copy(column);
  		$scope.textAlignments = [{text:'left',align:'flex-start'},{text:'center',align:'center'},{text:'right',align:'flex-end'}];
  		$scope.colorPickerProperty = {format:'rgb', placeholder:sbiModule_translate.load('sbi.cockpit.color.select')};
  		
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
  			{name:'SUM',available:['MEASURE']},
  			{name:'AVG',available:['MEASURE']},
  			{name:'MIN',available:['MEASURE','ATTRIBUTE']},
  			{name:'MAX',available:['MEASURE','ATTRIBUTE']}];
  		
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