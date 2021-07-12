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

/**
 * @author Marco Balestri <marco.balestri@eng.it>
 */

angular
	.module('cockpitModule')
	.controller('RWidgetEditControllerFunction', RWidgetEditControllerFunction)

function RWidgetEditControllerFunction(
		$scope,
		$http,
		$mdToast,
		finishEdit,
		model,
		sbiModule_translate,
		$mdDialog,
		mdPanelRef,
		cockpitModule_datasetServices,
		cockpitModule_analyticalDrivers,
		cockpitModule_helperDescriptors,
		cockpitModule_generalOptions,
		sbiModule_restServices) {

	$scope.translate = sbiModule_translate;
	$scope.newModel = angular.copy(model);
	$scope.formattedAnalyticalDrivers = [];

	$scope.setLibraries = function () {
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promiseGet('2.0/backendservices/widgets/RWidget/libraries', $scope.newModel.RAddress)
		.then(function(response){
			$scope.newModel.libraries = [];
			var librariesArray = JSON.parse((response.data.result));
			for (idx in librariesArray) {
				lib = librariesArray[idx];
				name = lib[0];
				version = lib[1];
				$scope.newModel.libraries.push({"name": name, "version": version})
			}
		}, function(error){
		});
  	}

	sbiModule_restServices.restToRootProject();
	sbiModule_restServices.promiseGet('2.0/configs/category', 'R_CONFIGURATION')
	.then(function(response){
		$scope.newModel.REnvs = $scope.buildEnvironments(response.data);
		$scope.newModel.REnvsKeys = Object.keys($scope.newModel.REnvs);
	}, function(error){
	});

	for(var a in cockpitModule_analyticalDrivers){
		$scope.formattedAnalyticalDrivers.push({'name':a});
	}

	$scope.buildEnvironments = function (data) {
		toReturn = {}
		for (i=0; i<data.length; i++) {
			key = data[i].label;
			val = data[i].valueCheck;
			toReturn[key] = val;
		}
		return toReturn;
	}

	$scope.newModel.outputTypes = {
        "Image":"img",
        "HTML":"html",
	};

	$scope.newModel.outputTypesKeys = Object.keys($scope.newModel.outputTypes);

	$scope.toggleTag = function(tag){
		tag.opened = !tag.opened;
	}

	$scope.$watch('newModel.dataset.dsId',function(newValue,oldValue){
		if(newValue){
			$scope.availableDatasets=cockpitModule_datasetServices.getAvaiableDatasets();
			var dsIndex;
			for(var d in $scope.availableDatasets){
				if($scope.availableDatasets[d].id.dsId == newValue) dsIndex = d;
			}
			if(typeof dsIndex != 'undefined'){
				$scope.dataset = $scope.availableDatasets[dsIndex];
				if (newValue != oldValue) $scope.newModel.content.columnSelectedOfDataset = $scope.dataset.metadata.fieldsMeta;
				for (i in $scope.newModel.content.columnSelectedOfDataset) {
					obj = $scope.newModel.content.columnSelectedOfDataset[i];
					if(obj.fieldType == "MEASURE" && !obj.value) obj.aggregationSelected = "SUM";
				}
				if ($scope.columnsGrid && $scope.columnsGrid.api) {
					$scope.columnsGrid.api.setRowData($scope.newModel.content.columnSelectedOfDataset);
					resizeColumns();
				}
			}
		}else{
			if($scope.newModel.content && $scope.newModel.content.columnSelectedOfDataset) $scope.newModel.content.columnSelectedOfDataset = [];
		}

		var ds_label;
		if (newValue) {
			ds_label = cockpitModule_datasetServices.getDatasetById(newValue).label;
		}
		$scope.helper = cockpitModule_helperDescriptors.rHelperJSON(newValue, ds_label, $scope.dataset ? $scope.dataset.metadata.fieldsMeta : null, $scope.formattedAnalyticalDrivers, $scope.aggregations, $scope.newModel.cross, $scope.availableDatasets);

	})

	$scope.insertCode = function(tag){
		var tempString = tag.tag;
		for(var i in tag.inputs){
			if($scope.helper[tag.name] && (typeof $scope.helper[tag.name][tag.inputs[i].name] != 'undefined')) {
				tempString = tempString.replace('%%'+tag.inputs[i].name+'%%', function(match){
					if(tag.inputs[i].replacer){
						return tag.inputs[i].replacer.replace('***', $scope.helper[tag.name][tag.inputs[i].name]);
					}else return $scope.helper[tag.name][tag.inputs[i].name];
				});
			}else tempString = tempString.replace('%%'+tag.inputs[i].name+'%%','');
		}
		if($scope.newModel.RCode) $scope.newModel.RCode += tempString;
		else  $scope.newModel.RCode = tempString;
	}

	$scope.editorOptionsR = {
        theme: 'eclipse',
        lineWrapping: true,
        lineNumbers: true,
        mode: {name: "r"},
        onLoad: $scope.codemirrorLoaded
	};

	//codemirror initializer
	$scope.codemirrorLoaded = function (_editor) {
		$scope._doc = _editor.getDoc();
		$scope._editor = _editor;
		_editor.focus();
		$scope._doc.markClean()
		_editor.on("beforeChange", function () {});
		_editor.on("change", function () {});
	};

	$scope.saveConfiguration = function () {
		if(!$scope.checkAliases()){
            $scope.showAction($scope.translate.load('sbi.cockpit.table.erroraliases'));
            return;
        }
		if(!$scope.checkEnvironment()){
            $scope.showAction($scope.translate.load('kn.cockpit.R.errorenvironment'));
            return;
        }
		mdPanelRef.close();
		angular.copy($scope.newModel,model);
		$scope.$destroy();
		finishEdit.resolve();
	};

	$scope.cancelConfiguration = function () {
		mdPanelRef.close();
		$scope.$destroy();
		finishEdit.reject();
	};

	// aggregations on dataset

	if (!$scope.newModel.settings) $scope.newModel.settings = {};
	if (!$scope.newModel.content) $scope.newModel.content = {name: $scope.newModel.type + '_' + $scope.newModel.id};
	$scope.availableAggregations = ["NONE","SUM","AVG","MAX","MIN","COUNT","COUNT_DISTINCT"];

	if($scope.newModel.dataset && $scope.newModel.dataset.dsId){
		$scope.local = cockpitModule_datasetServices.getDatasetById($scope.newModel.dataset.dsId);
	}

	$scope.showCircularcolumns = {value :false};

	$scope.colorPickerProperty={format:'rgb'}

	$scope.columnsGrid = {
		angularCompileRows: true,
		domLayout :'autoHeight',
        enableColResize: false,
        enableFilter: false,
        enableSorting: false,
        onGridReady : resizeColumns,
        onCellEditingStopped: refreshRow,
        singleClickEdit: true,
        columnDefs: [
        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.name'), field:'name'},
        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.alias'), field:'alias',"editable":true,cellRenderer:editableCell, cellClass: 'editableCell'},
        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.type'), field: 'fieldType'},
        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.aggregation'), field: 'aggregationSelected', cellRenderer: aggregationRenderer,"editable":isAggregationEditable, cellClass: 'editableCell',
        		cellEditor:"agSelectCellEditor",cellEditorParams: {values: $scope.availableAggregations}},
        	{headerName:"",cellRenderer: buttonRenderer,"field":"valueId","cellStyle":{"border":"none !important","text-align": "right","display":"inline-flex","justify-content":"flex-end"},width: 150,suppressSizeToFit:true, tooltip: false}],
        rowData: $scope.newModel.content.columnSelectedOfDataset
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
		if (params.data.isCalculated) return false;
		return params.data.fieldType == "MEASURE" ? true : false;
	}

	function aggregationRenderer(params) {
		var aggregation = '<i class="fa fa-edit"></i> <i>'+params.value+'</i>';
		if (!params.data.isCalculated && params.data.fieldType == "MEASURE") {
			return aggregation;
		} else return "";
	}

	function buttonRenderer(params){
		var calculator = '';
		if(params.data.isCalculated){
			calculator = '<calculated-field ng-model="newModel" selected-item="'+params.rowIndex+'"></calculated-field>';
		}
		return 	calculator + '<md-button class="md-icon-button" ng-click="deleteColumn(\''+params.data.name+'\',$event)"><md-icon md-font-icon="fa fa-trash"></md-icon><md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.column.delete")}}</md-tooltip></md-button>';
	}

	function refreshRow(cell){
		$scope.columnsGrid.api.redrawRows({rowNodes: [$scope.columnsGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
	}

	$scope.deleteColumn = function(rowName,event) {
		for(var k in $scope.newModel.content.columnSelectedOfDataset){
			if($scope.newModel.content.columnSelectedOfDataset[k].name == rowName) var item = $scope.newModel.content.columnSelectedOfDataset[k];
		}
  		  var index=$scope.newModel.content.columnSelectedOfDataset.indexOf(item);
		  $scope.newModel.content.columnSelectedOfDataset.splice(index,1);
	  }

	$scope.checkEnvironment = function(){
        if (!$scope.newModel.RAddress) return false;
        else return true;
    }

	$scope.checkAliases = function(){
        var columns = $scope.newModel.content.columnSelectedOfDataset;
        if (columns) {
	        for(var i = 0; i < columns.length - 1; i++){
	            for(var j = i + 1; j < columns.length; j++){
	                if(columns[i].alias == columns[j].alias){
	                    return false;
	                }
	            }
	        }
        }
        return true;
    }

	$scope.$watchCollection('newModel.content.columnSelectedOfDataset',function(newValue,oldValue){
		if($scope.columnsGrid.api && newValue){
			$scope.columnsGrid.api.setRowData(newValue);
			$scope.columnsGrid.api.sizeColumnsToFit();
		}
	})

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
				locals: {model:$scope.newModel, getMetadata : $scope.getMetadata},
				fullscreen: true,
				controller: addColumnController
			}).then(function(returnModel) {
				$scope.newModel.content.columnSelectedOfDataset = returnModel;
			}, function() {
			});
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

}


function addColumnController($scope,sbiModule_translate,$mdDialog,model,getMetadata,cockpitModule_datasetServices,cockpitModule_generalOptions){
	$scope.translate=sbiModule_translate;
	$scope.model = angular.copy(model);
	$scope.columnSelected = [];
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
		if($scope.model.content.columnSelectedOfDataset == undefined){
			$scope.model.content.columnSelectedOfDataset = [];
		}

		for(var i in $scope.columnsGridOptions.api.getSelectedRows()){
			var obj = $scope.columnsGridOptions.api.getSelectedRows()[i];
			obj.aggregationSelected = 'SUM';
			obj.typeSelected = obj.type;
			$scope.model.content.columnSelectedOfDataset.push(obj);
		}
		$mdDialog.hide($scope.model.content.columnSelectedOfDataset);
	}

	$scope.cancelConfiguration=function(){
		$mdDialog.cancel();
	}

}

