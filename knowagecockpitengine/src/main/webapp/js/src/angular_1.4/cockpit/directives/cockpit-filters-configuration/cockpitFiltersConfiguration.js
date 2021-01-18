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
 * @authors Alessandro Piovani (alessandro.piovani@eng.it)
 * v0.0.1
 *
 */
(function() {

	angular.module('cockpitModule')
	.directive('cockpitFiltersConfiguration',function(){
		   return{
			   templateUrl: baseScriptPath+ '/directives/cockpit-filters-configuration/templates/cockpitFiltersConfiguration.html',
			   controller: cockpitFiltersControllerFunction,
			   scope: {
				   ngModelShared: '='
			   	},
			   	compile: function (tElement, tAttrs, transclude) {
	                return {
	                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
	                    },
	                    post: function postLink(scope, element, attrs, ctrl, transclud) {
	                    }
	                };
			   	}
		   }
	});

	//filter operators if dataset is realtime or not
	angular.module('cockpitModule').filter('filterDatasetOperator', function() {
		  return function (items, datasetIsRealTime) {
			    var filtered = [];
			    for (var i = 0; i < items.length; i++) {
		    		var item = items[i]
			    	if (datasetIsRealTime != undefined && datasetIsRealTime == true){
			    		if (item != "=" && item != "<" && item != ">" && item != "<=" && item != ">=" && item != "!="){
			    			continue;
			    		} else {
			    			filtered.push(item);
			    		}
					} else  {
						filtered.push(item)
					}
			    }
			    return filtered;
			  };

	});

	function cockpitFiltersControllerFunction($scope,cockpitModule_widgetServices,
			cockpitModule_properties,cockpitModule_template,$mdDialog,sbiModule_translate,sbiModule_restServices,
			sbiModule_messaging,
			cockpitModule_gridsterOptions,$mdPanel,cockpitModule_widgetConfigurator,$mdToast,
			cockpitModule_generalServices,cockpitModule_widgetSelection,cockpitModule_datasetServices,$rootScope){

		$scope.cockpitModule_properties=cockpitModule_properties;
		$scope.cockpitModule_template=cockpitModule_template;
		$scope.cockpitModule_widgetServices=cockpitModule_widgetServices;
		$scope.cockpitModule_datasetServices=cockpitModule_datasetServices;
		$scope.translate=sbiModule_translate;

		$scope.openGeneralConfigurationDialog=function(){
			cockpitModule_generalServices.openGeneralConfiguration();
		}

		$scope.openDataConfigurationDialog=function(){
			cockpitModule_generalServices.openDataConfiguration();
		}

		$scope.fabSpeed = {
				isOpen : false
		}

		$scope.showFilters=false;

		$scope.localDS={};

		// filter currently modified
		$scope.newFilter={};
		$scope.newFilterAddMode=false;
		$scope.newFilterEditMode=false;

		$scope.filtersTableColumns=[
			{"label": $scope.translate.load("sbi.cockpit.widgets.filtersConfiguration.at.dataset"),"name":"dataset.label"},
			{"label": $scope.translate.load("sbi.cockpit.widgets.filtersConfiguration.at.column"),"name":"colName"},
			{"label": $scope.translate.load("sbi.cockpit.widgets.filtersConfiguration.at.operator"),"name":"filterOperator"},
			{"label":$scope.translate.load("sbi.cockpit.widgets.filtersConfiguration.at.val")+'1',"name":"filterVal1"},
			{"label":$scope.translate.load("sbi.cockpit.widgets.filtersConfiguration.at.val")+'2',"name":"filterVal2"}
			];

		$scope.isObjectEmpty = function(obj){
		   return Object.keys(obj).length === 0;
		}

		// current selected Widget datasets
		$scope.newFilterAllWidgetDS=[];

		// current selected Widget dataset columns
		$scope.newFilterColumnDS=[];
		$scope.newFilterColumnDS.push("");
		$scope.newFilterCurrenteSelectedDS = {};

		$scope.operatorsTypeString = ['=','!=','IN','like','is null','is not null','min','max','not IN'];
		$scope.operatorsTypeNumber = ['=','<','>','<=','>=','IN','is null','is not null','!=','min','max','range','not IN'];
		$scope.operatorsTypeDate = ['=','!=','IN','is null','is not null','min','max','range','not IN'];

		$scope.zeroOperandOperators = ['is null', 'is not null', 'min', 'max'];
		$scope.oneOperandOperators = ['=', '!=', 'like', '<','>','<=','>=','IN'];
		$scope.twoOperandsOperators = ['range'];


		$scope.updateFiltersDatasetNames = function(){
			for(var k in $scope.ngModelShared.filters){
				if ($scope.ngModelShared.filters[k].dataset == 'string') {
					$scope.ngModelShared.filters[k].dataset = {"label": $scope.ngModelShared.filters[k].dataset}
					if(cockpitModule_datasetServices.getDatasetByLabel($scope.ngModelShared.filters[k].dataset)) $scope.ngModelShared.filters[k].dataset.dsId=cockpitModule_datasetServices.getDatasetByLabel($scope.ngModelShared.filters[k].dataset).id.dsId;
					else $scope.ngModelShared.filters[k].dataset.dsId = $scope.ngModelShared.dataset.dsId;
				}
				if(!cockpitModule_datasetServices.getDatasetByLabel($scope.ngModelShared.filters[k].dataset.label)){
					$scope.ngModelShared.filters[k].dataset.label = cockpitModule_datasetServices.getDatasetLabelById($scope.ngModelShared.filters[k].dataset.dsId)
				}
			}
		}
		$scope.updateFiltersDatasetNames();


		/*
		 *  if dataset changes some filters referring to old dataset could be no longer valid
		 */
		$scope.cleanFilters=function(dsIdArray){
			if($scope.ngModelShared.filters != undefined){
				var currentFilters = $scope.ngModelShared.filters
				var indexToDelete = [];
				for(var i = 0; i< currentFilters.length; i++){
					if(dsIdArray.indexOf(currentFilters[i].dataset.dsId) == -1){
						indexToDelete.push(i);
					}
				}
				for(var i = indexToDelete.length-1; i>= 0; i--){
					var indDel = indexToDelete[i];
					currentFilters.splice(indDel,1);
				}
			}
		}

		/*
		 *  previous filter ending put a filter for each column, even if not defined, erase not defined ones
		 */
		$scope.cleanOldCockpitFilters=function(ds){
			var indexToDelete = [];
			if ($scope.ngModelShared.filters == undefined) { //no filters to delete
				return;
			}
			for(var i = 0; i<$scope.ngModelShared.filters.length;i++){
				var f = $scope.ngModelShared.filters[i];
				if(f.filterOperator == undefined || f.filterOperator == ''){
					indexToDelete.push(i);
				}
				else{
					if(f.dataset == undefined){
						f.dataset = ds.label;
					}
					if(f.filterVal1 == undefined && f.filterVals != undefined && f.filterVals.length>0){
						f.filterVal1 = f.filterVals[0];
						f.filterVal2 = f.filterVals[1];
					}
					if(f.column != undefined){
						f.colName = f.column;
						f.colAlias = f.column;
					}else {
						f.colName = f.colName || f.columnName;
						f.colAlias = f.colAlias || f.columnName;
					}
				}
			}
			for(var i = indexToDelete.length-1; i>=0 ;i--){
				var ind = indexToDelete[i];
				$scope.ngModelShared.filters.splice(ind,1);
			}

		}



		/*
		 *  functions with button add, erase, save
		 */

		$scope.addNewFilter=function(){
			$scope.newFilter = {};
			$scope.newFilterEditMode = false;
			$scope.newFilterAddMode = true;

//			// set by default a dataset, user can then change
//			if($scope.newFilterAllWidgetDS.length>0){
//				var ds = $scope.newFilterAllWidgetDS[$scope.newFilterAllWidgetDS.length-1];
//				$scope.cdataset = ds.label;
//				$scope.newFilterChangeDatasetUpdateColumns();
//			}

		}

		$scope.eraseNewFilter=function(){
			$scope.newFilter = undefined;
			$scope.newFilterEditMode = false;
			$scope.newFilterAddMode = false;
		}

/*
* Check if filter is unique
*/
$scope.checkIfFilterIsNotUnique=function(colName, dsId) {
	
	for (var k in $scope.ngModelShared.filters){
		if ($scope.ngModelShared.filters[k].dataset.dsId === dsId) {
			if ($scope.ngModelShared.filters[k].colName === colName) {				
				return true;
			}			
		}			
	}
	return false;	
}

/*
 * Check if new filter is valid before inserting it into filter list
 */
		$scope.checkNewFilterValidity=function(){
			var message = '';
			var valid = true;
			if($scope.newFilter.dataset == undefined
					|| $scope.newFilter.colName == undefined
					|| $scope.newFilter.filterOperator == undefined){
				valid = false;
				message = $scope.translate.load("kn.cockpit.filters.warning.missingfields");
			}
			if( $scope.twoOperandsOperators.indexOf($scope.newFilter.filterOperator)>-1
					&& ($scope.newFilter.filterVal1 == undefined || $scope.newFilter.filterVal2 == undefined
							|| $scope.newFilter.filterVal1 == '' || $scope.newFilter.filterVal2 == ''
					)){
				valid = false;
				message = $scope.translate.load("kn.cockpit.filters.warning.missingfields");
			}
			else if( $scope.oneOperandOperators.indexOf($scope.newFilter.filterOperator)>-1
					&& ($scope.newFilter.filterVal1 == undefined || $scope.newFilter.filterVal1 == '')){
				valid = false;
				message = $scope.translate.load("kn.cockpit.filters.warning.missingfields");
			}
			if ($scope.checkIfFilterIsNotUnique($scope.newFilter.colName,$scope.newFilter.dataset.dsId)){
				valid = false;
				message = $scope.translate.load("kn.cockpit.filters.warning.duplicatedfilter");
			}
			valid || sbiModule_messaging.showWarningMessage(message,$scope.translate.load("kn.cockpit.filters.warning.title"),);
			return valid;
		}

		/*
		 * Save new filter into filter list
		 */

		$scope.saveNewFilter=function(){

			// check if it is valid
			var valid = $scope.checkNewFilterValidity();

			if(valid) {

				// depending on oeprator type could be necessary to delete some previous values
				if($scope.zeroOperandOperators.indexOf($scope.newFilter.filterOperator) >=0 ){
					$scope.newFilter.filterVal1 = '';
					$scope.newFilter.filterVal2 = '';
				}
				else if($scope.oneOperandOperators.indexOf($scope.newFilter.filterOperator) >=0 ){
					$scope.newFilter.filterVal2 = '';
				}


				// fill filterVals array
				$scope.newFilter.filterVals = [];
				if($scope.newFilter.filterVal1 != undefined && $scope.newFilter.filterVal1 != ''){
					$scope.newFilter.filterVals[0]=$scope.newFilter.filterVal1;
				}
				if($scope.newFilter.filterVal2 != undefined && $scope.newFilter.filterVal2 != ''){
					$scope.newFilter.filterVals[1]=$scope.newFilter.filterVal2;
				}

				// editing an existing field
				if($scope.newFilterEditMode == true){
					// filter is identified by dataset and column
					for(var i = 0; i<$scope.ngModelShared.filters.length; i++){
						var fil = $scope.ngModelShared.filters[i];

						//fil.colName = $scope.cleanSingleQbeColumn(fil.colName);

						if(fil.dataset == $scope.newFilter.dataset
								&& fil.colName == $scope.newFilter.colName
						){
							$scope.ngModelShared.filters[i] = $scope.newFilter;
						}
					}

				}
				else{

					// if insert mode check filter is noty already present
					var found = false;
					for(var i = 0; i<$scope.ngModelShared.filters.length; i++){
						var fil = $scope.ngModelShared.filters[i];
						if(fil.dataset == $scope.newFilter.dataset && fil.colName == $scope.newFilter.colName){
							found = true;
						}
					}
					if(found == true){
						sbiModule_messaging.showWarningMessage(sbiModule_translate.load("sbi.cockpit.widgets.filtersConfiguration.filterAlreadyPresent"));
						return;
					}
					else {
						$scope.ngModelShared.filters.push($scope.newFilter);
					}
				}

				$scope.newFilterColumnDS = [];
				$scope.newFilter = {};
				$scope.newFilterEditMode = false;
				$scope.newFilterAddMode = false;

			}
		}


		$scope.eraseSelectedFilter=function(selectedFilter){
			var indexToDelete = -1;
			for(var j=0;j<$scope.ngModelShared.filters.length && indexToDelete == -1;j++){
				var cycleFilter = $scope.ngModelShared.filters[j];
				if( selectedFilter.colName == cycleFilter.colName &&
						selectedFilter.dataset == cycleFilter.dataset ){
					indexToDelete = j;
				}
			}
			if(indexToDelete != -1){
				$scope.ngModelShared.filters.splice(indexToDelete,1);
			}

			$scope.newFilter = undefined;
			$scope.newFilterEditMode = false;
			$scope.newFilterAddMode = false;
		}


		$scope.selectRowFilter=function(row){
			var dataset = row.dataset;
			var colName = row.colName;
			var colAlias = row.colAlias;
			var filterOperator = row.filterOperator;
			var filterVal1 = row.filterVal1;
			var filterVal2 = row.filterVal2;
			var type = row.type;

			// if clicked on already selected don't do anything
			if($scope.newFilter && $scope.newFilter.dataset == row.dataset &&  $scope.newFilter.colName == row.colName ){
				return;
			}

			$scope.newFilter = {};
			$scope.newFilter['dataset'] = dataset;

			// fill the combo of columns with current dataset
			$scope.newFilterColumnDS = [{"name":colName, "alias":colAlias}];
			//$scope.newFilterColumnDS.push({"name":colName, "alias":colAlias});


			$scope.newFilter['colName'] = colName;
			$scope.newFilter['colAlias'] = colAlias;
			$scope.newFilter['filterOperator'] = filterOperator;
			$scope.newFilter['type'] = type;
			$scope.newFilter['filterVal1'] = filterVal1;
			$scope.newFilter['filterVal2'] = filterVal2;
			$scope.newFilter['filterVals'] = [filterVal1,filterVal2];


			$scope.newFilterEditMode = true;
			$scope.newFilterAddMode = false;

		}



		// CHART AND TEXT CASE: newValue in text is an array, in other case is single datset id
		$scope.$watch("ngModelShared.datasetId", function(newValue, oldValue) {

			if(newValue != undefined){
				if($scope.ngModelShared.type == 'text' || $scope.ngModelShared.type == 'map'){
					// new value is array containing all datasets currently included in widget
					$scope.refreshMultiDatasetCase(newValue, 'text');

				}
				else{
					$scope.refreshSingleDatasetCase(newValue, oldValue, 'chart');
				}
				//$scope.cleanQbeColumns();
			}
		});

		// TABLE CASE
		$scope.$watch("ngModelShared.dataset.dsId", function(newValue, oldValue) {
			if(newValue != undefined){
				if($scope.ngModelShared.type == 'text' || $scope.ngModelShared.type == 'map'){
					// new value is array containing all datasets currently included in widget
					$scope.refreshMultiDatasetCase(newValue, 'text');
				}else {
					$scope.refreshSingleDatasetCase(newValue, oldValue, 'table');
				    $scope.cleanQbeColumns();
				}
			}
		});


/*
 * refresh filters value ins single dataset widget case
 */

		$scope.refreshSingleDatasetCase=function(newValue, oldValue, type){

			// get DS
			var ds = cockpitModule_datasetServices.getDatasetById(newValue);

			// if dataset has changed
			if(newValue != oldValue){
				$scope.ngModelShared.filters = [];
			}
			else{
				/* back compatibility check:
				 * - erase filters that have no operator, considering coming from old interface
				 * - in case of filterVals put filterVal1 and filterVal2
				 */
				$scope.cleanOldCockpitFilters(ds);
			}


			$scope.refreshDatasetField(ds);
			$scope.eraseNewFilter();
			if($scope.ngModelShared.filters  == undefined ){
				$scope.ngModelShared.filters=[];
			}
			$scope.selectedDsId=newValue;
			angular.copy(cockpitModule_datasetServices.getDatasetById($scope.selectedDsId), $scope.localDS);
		}

		/*
		 * refresh filters value ins multi dataset widget case
		 */

		$scope.refreshMultiDatasetCase=function(newValue, type){
			// erase filter linked to no more existing dataset
			$scope.cleanFilters($scope.ngModelShared.datasetId);
			$scope.refreshDatasetCombo(newValue);
			// if at least one ds is selected then enable filters
			if(newValue.length>0 && $scope.ngModelShared.filters == undefined){
				$scope.ngModelShared.filters = [];
			}
			$scope.eraseNewFilter();
			$scope.selectedDsId=newValue;
		}


/*
 * functions that fill dataset combo field
 */

		// case only one dataset
		$scope.refreshDatasetField=function(ds){
			$scope.newFilterAllWidgetDS = [];
			$scope.newFilterAllWidgetDS.push({"dsId": ds.id.dsId, "label": ds.label});
		}

		// case multi dataset
		$scope.refreshDatasetCombo=function(newValues){
			$scope.newFilterAllWidgetDS = [];

			for(var i =0; i<newValues.length; i++){
				var ds = cockpitModule_datasetServices.getDatasetById(newValues[i]);
				$scope.newFilterAllWidgetDS.push({"dsId": ds.id.dsId, "label": ds.label});
			}

		}



		/*
		 * change columns on column combo after changing dataset
		 */
		$scope.newFilterChangeDatasetUpdateColumns=function(){
			$scope.newFilterColumnDS = [];

			var ds = cockpitModule_datasetServices.getDatasetById($scope.newFilter.dataset.dsId);
			$scope.datasetIsRealTime = ds.isRealtime;
			$scope.newFilterCurrenteSelectedDS = ds;
			// now dataset is only one localDSforFilters
			for(var i=0;i<ds.metadata.fieldsMeta.length;i++){
				var obj = ds.metadata.fieldsMeta[i];
				$scope.newFilterColumnDS.push(obj);
			}
		}

		/*
		 * change type depending on column selected in coumnbs combo, this affects filter operators available
		 */
		$scope.newFilterChangeColumnUpdateType=function(){

			// if column changed erase filterOperator, filterVal1, filterVal2
			$scope.newFilter.filterOperator = undefined;
			$scope.newFilter.filterVal1 = undefined;
			$scope.newFilter.filterVal2 = undefined;
			$scope.newFilter.filterVals = undefined;

			var colLabel = $scope.newFilter.colName;
			var ds = $scope.newFilterCurrenteSelectedDS;
			for(var i=0;i<ds.metadata.fieldsMeta.length;i++){
				var col = ds.metadata.fieldsMeta[i];
				if(col.name == colLabel){
					var type = ds.metadata.fieldsMeta[i].type;
					$scope.newFilter.colName = col.name;
					$scope.newFilter.colAlias = col.alias;

					$scope.newFilter.type = type;
				}
			}
		}



		$scope.actionsOnSelectionFilters = [
			{
				icon:'fa fa-trash iconAlignFix' ,
				action : function(item,event) {
					$scope.eraseSelectedFilter(item);
				}
			}
			];



		$scope.cleanSingleQbeColumn = function(columnName){
			var colonIndex = columnName.indexOf(":");
			if(colonIndex > -1){
				columnName = columnName.substr(colonIndex + 1);
			}
			return columnName;
		}

		// clean column name in case of QBE dataset
		$scope.cleanQbeColumns=function(){
			for(var i=0; i<$scope.ngModelShared.filters.length; i++){
				var columnName = $scope.ngModelShared.filters[i].colName;
				var colonIndex = columnName.indexOf(":");
				if(colonIndex > -1){
					columnName = columnName.substr(colonIndex + 1);
				}
				$scope.ngModelShared.filters[i].columnName = columnName;
			}
		}

		if($scope.ngModelShared && !$scope.ngModelShared.limitRows){
			$scope.ngModelShared.limitRows = {enable: false, rows: 10};
		}

		$scope.canUseSolrDataset = function(dataset, widgetType) {
		    if(dataset && dataset.dsId) {
		        var datasetConfiguration = cockpitModule_datasetServices.getDatasetById(dataset.dsId);
		        if(datasetConfiguration.type == 'SbiSolrDataSet' && widgetType == 'discovery') {
		            return true;
		        }
		    }
		    return false;
		}
	};
})();