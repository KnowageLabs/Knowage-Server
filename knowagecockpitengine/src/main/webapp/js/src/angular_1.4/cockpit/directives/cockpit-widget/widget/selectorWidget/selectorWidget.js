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

(function() {
angular.module('cockpitModule')
	.directive('cockpitSelectorWidget',function(cockpitModule_widgetServices,$mdDialog,$rootScope){
		return{
			templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/selectorWidget/templates/selectorWidgetTemplate.html',
			controller: cockpitSelectorWidgetControllerFunction,
			compile: function (tElement, tAttrs, transclude) {
				return {
					pre: function preLink(scope, element, attrs, ctrl, transclud) {
						element[0].classList.add("flex");
						element[0].classList.add("layout-column");
						element[0].classList.add("layout-fill");
					},
					post: function postLink(scope, element, attrs, ctrl, transclud) {
						//init the widget
						element.ready(function () {
							scope.initWidget();
						});
					}
				};
			}

		}
	})

	function cockpitSelectorWidgetControllerFunction(
			$scope,
			$mdDialog,
			$mdToast,
			$timeout,
			$mdPanel,
			$q,
			$filter,
			sbiModule_translate,
			sbiModule_restServices,
			sbiModule_dateServices,
			sbiModule_config,
			cockpitModule_datasetServices,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetServices,
			cockpitModule_widgetSelection,
			cockpitModule_properties,
			cockpitModule_template,
			accessibility_preferences,
			$rootScope){

		$scope.accessibilityModeEnabled = accessibility_preferences.accessibilityModeEnabled;
		if ($scope.ngModel && $scope.ngModel.dataset && $scope.ngModel.dataset.dsId){
			$scope.ngModel.dataset.isRealtime = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId).isRealtime;
			$scope.ngModel.dataset.name = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId).name;
		}
		$scope.ngModel.activeValues = null;
		$scope.multiCombo = {};
		$scope.multiCombo.selected = [];
		$scope.multiValue = [];
		$scope.selections = [];
		$scope.searchParamText = "";
		$scope.selectedTab = {'tab' : 0};
		$scope.widgetIsInit=false;
		$scope.totalCount = 0;
		$scope.translate = sbiModule_translate;
		$scope.datasetRecords = {};

		$scope.$watch("startDate",function(newValue,oldValue){
			if($scope.ngModel.settings.modalityValue=='singleValue'){
				$scope.endDate = $scope.startDate;
			}

			if($scope.startDate != undefined
					&& $scope.endDate != undefined
					&& newValue.getTime() > $scope.endDate.getTime()){
				$scope.endDate = newValue;
			}

			if(newValue != oldValue){
				$scope.applyDateSelection(newValue,oldValue,$scope.endDate,$scope.endDate);
			}
		});

		$scope.clearStartDate = function(){
			$scope.startDate = undefined;
		}

		$scope.$watch("endDate",function(newValue,oldValue){
			if($scope.ngModel.settings.modalityValue=='multiValue' && newValue != oldValue){
				$scope.applyDateSelection($scope.startDate,$scope.startDate,newValue,oldValue);
			}
		});

		$scope.clearEndDate = function(){
			$scope.endDate = undefined;
		}

		$scope.applyDateSelection = function(newStartDate,oldStartDate,newEndDate,oldEndDate){
			if(((newStartDate && !oldStartDate || newStartDate && newStartDate != oldStartDate) && newEndDate)
					|| (newStartDate && (newEndDate && !oldEndDate || newEndDate && newEndDate != oldEndDate))){
				var dates = $scope.getDatesBetween(newStartDate, newEndDate);
				$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,dates);
			}else if((!newStartDate && oldStartDate) || (!newEndDate && oldEndDate)){
				var item = {};
				item.aggregated = $scope.aggregated;
				item.columnName = $scope.ngModel.content.selectedColumn.aliasToShow;
				item.columnAlias = $scope.ngModel.content.selectedColumn.aliasToShow;
				item.ds = $scope.ngModel.dataset.name;

				$rootScope.$broadcast('DELETE_SELECTION',item);
				$scope.deleteSelections(item);
			}
		}

		$scope.getDatesBetween = function(startDate, endDate){
			var startMillis = startDate.getTime();
			var endMillis = endDate.getTime() + 24 * 3600 * 1000;
			var dates = [];
			var dateFormat = sbiModule_config.clientServerTimestampFormat.replace("Y", "yyyy").replace("m", "MM").replace("d", "dd").replace("H", "HH").replace("i", "mm").replace("s", "ss");

			var column;
			for(var i=1; i<$scope.datasetRecords.metaData.fields.length; i++){
				var field = $scope.datasetRecords.metaData.fields[i];
				if(field.header == $scope.ngModel.content.selectedColumn.name){
					column = field.name;
					break;
				}
			}

			if(column){
				for(var i=0; i<$scope.datasetRecords.rows.length; i++){
					var dateString = $scope.datasetRecords.rows[i][column];
					var dateMillis = sbiModule_dateServices.getDateFromFormat(dateString.split('.')[0], dateFormat).getTime();
					if(startMillis <= dateMillis && dateMillis < endMillis){
						dates.push(dateString);
					}
				}
			}

			return dates;
		}

		$scope.isSelectedColumnTemporal = function(){
			if($scope.ngModel.content.selectedColumn && $scope.ngModel.content.selectedColumn && $scope.ngModel.content.selectedColumn.type){
				var type = $scope.ngModel.content.selectedColumn.type.toLowerCase();
				var isTemporal = type.indexOf('date') > -1 || type.indexOf('timestamp') > -1;
				return isTemporal;
			}
			return false;
		}

		$scope.cockpitModule_widgetSelection = cockpitModule_widgetSelection;
		$scope.realTimeSelections = cockpitModule_widgetServices.realtimeSelections;
		cockpitModule_widgetSelection.setWidgetOfType("selector");

		//set a watcher on a variable that can contains the associative selections for realtime dataset
		var realtimeSelectionsWatcher = $scope.$watchCollection('realTimeSelections',function(newValue,oldValue,scope){
			if(scope.ngModel && scope.ngModel.dataset && scope.ngModel.dataset.dsId){
				var dataset = cockpitModule_datasetServices.getDatasetById(scope.ngModel.dataset.dsId);
				if(dataset.isRealtime && dataset.useCache){
					if(cockpitModule_properties.DS_IN_CACHE.indexOf(dataset.label)==-1 ){
						cockpitModule_properties.DS_IN_CACHE.push(dataset.label);
					}
					if($scope.isSelectedColumnTemporal()){
						if(!newValue || newValue.length == 0){
							$scope.clearStartDate();
							$scope.clearEndDate();
						}
					}else{
						if(newValue != oldValue && newValue.length > 0){
							scope.itemList = scope.filterDataset(scope.datasetRecords,scope.reformatSelections(newValue));
						}else{
							angular.copy(scope.savedRows, scope.itemList);
						}
					}
				}
			}
		});

		if(!$scope.ngModel.settings){
			$scope.ngModel.settings = {};
		}

		if(!$scope.ngModel.style){
			$scope.ngModel.style={};
		}
		if(!$scope.ngModel.settings.summary){
			$scope.ngModel.settings.summary={
					'enabled': false,
					'forceDisabled': false,
					'style': {}
			};
		}

		$scope.editWidget=function(index){
			var finishEdit=$q.defer();
			var config = {
					attachTo:  angular.element(document.body),
					controller: selectorWidgetEditControllerFunction,
					disableParentScroll: true,
					templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/selectorWidget/templates/selectorWidgetEditPropertyTemplate.html',
					position: $mdPanel.newPanelPosition().absolute().center(),
					fullscreen :true,
					hasBackdrop: true,
					clickOutsideToClose: false,
					escapeToClose: false,
					focusOnOpen: true,
					preserveScope: true,
					autoWrap:false,
					locals: {finishEdit: finishEdit, originalModel: $scope.ngModel, getMetadata: $scope.getMetadata, scopeFather: $scope}
			};
			$mdPanel.open(config);
			return finishEdit.promise;
		}

		$scope.init=function(element,width,height){
			$scope.refreshWidget(null, 'init');
			$timeout(function(){
				$scope.widgetIsInit=true;
			},500);

		}

		$scope.refresh=function(element,width,height, datasetRecords,nature){
			if(!$scope.ngModel.dataset.name){
				$scope.ngModel.dataset.name = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId).name;
			}

			$scope.aggregated = true;
			$scope.filtersParams = angular.copy($scope.cockpitModule_widgetSelection.getCurrentSelections($scope.ngModel.dataset.name));
			if(Object.keys($scope.filtersParams).length == 0){
				$scope.aggregated = false;
				$scope.filtersParams = $scope.cockpitModule_widgetSelection.getCurrentFilters($scope.ngModel.dataset.name);
			}

			if(nature == 'gridster-resized' || nature == 'fullExpand' || nature == 'resize'){
				return;
			}

			$scope.datasetRecords = datasetRecords;

			checkForSavedSelections($scope.filtersParams,nature);
			checkRefreshSettings();
			updateModel();
		}

		// reformatting the filter object to have an easier access on it
		$scope.reformatFilters = function(){
			var filters = {};
			for(var f in $scope.ngModel.filters){
				if($scope.ngModel.filters[f].filterVals.length > 0){
					var columnObject = $scope.getColumnObjectFromName($scope.ngModel.content.columnSelectedOfDataset,$scope.ngModel.filters[f].colName);
					var aliasToShow = columnObject.aliasToShow;
					filters[aliasToShow] = {
						"type":columnObject.fieldType,
						"values":$scope.ngModel.filters[f].filterVals,
						"operator":$scope.ngModel.filters[f].filterOperator
					};
				}
			}
			return filters;
		};

		// filtering the table for realtime dataset
		$scope.filterDataset = function(dataset,selection){
			if(dataset != undefined){
				//using the reformatted filters
				var filters = selection ? selection : $scope.reformatFilters();
				for(var f in filters){
					for(var d = dataset.length - 1; d >= 0; d--){
						//if the column is an attribute check in filter
						if (filters[f].type == 'ATTRIBUTE'){
							var value = dataset[d][f];
							if(typeof value == "number"){
								value = String(value);
							}
							if (filters[f].values.indexOf(value)==-1){
								dataset.splice(d,1);
							}
						//if the column is a measure cast it to number and check in filter
						} else if (filters[f].type == 'MEASURE'){
							var columnValue = Number(dataset[d][f]);
							var filterValue = filters[f].values.map(function (x) {
							    return Number(x);
							});
							//check operator
							var operator = String(filters[f].operator);
							if (operator == "="){
								operator = "==";
							}
							var leftOperand = String(columnValue);
							var rightOperand = String(filterValue[0]);
							var expression =  leftOperand + operator + rightOperand;


							//if (filterValue.indexOf(columnValue)==-1){
							if (eval(expression) == false){
								dataset.splice(d,1);
							}
						}
					}
				}
			}
			return dataset;
		}

		//reformatting the selections to have the same model of the filters
		$scope.reformatSelections = function(realTimeSelections){
			if ($scope.ngModel && $scope.ngModel.dataset && $scope.ngModel.dataset.dsId){
				var widgetDatasetId = $scope.ngModel.dataset.dsId;
				var widgetDataset = cockpitModule_datasetServices.getDatasetById(widgetDatasetId)

				for (var i=0; i< realTimeSelections.length; i++){
					//search if there are selection on the widget's dataset
					if (realTimeSelections[i].datasetId == widgetDatasetId){
						var selections = realTimeSelections[i].selections;
						var formattedSelection = {};
						var datasetSelection = selections[widgetDataset.label];
						for(var s in datasetSelection){
							var columnObject = scope.getColumnObjectFromName(scope.ngModel.content.columnSelectedOfDataset,s);
							if (!columnObject){
								columnObject = scope.getColumnObjectFromName(widgetDataset.metadata.fieldsMeta,s);
							}

							formattedSelection[columnObject.aliasToShow || columnObject.alias] = {"values":[], "type": columnObject.fieldType};
							for(var k in datasetSelection[s]){
								// clean the value from the parenthesis ( )
								var x = datasetSelection[s][k].replace(/[()]/g, '').replace(/['']/g, '').split(/[,]/g);
								for(var i=0; i<x.length; i++){
									formattedSelection[columnObject.aliasToShow || columnObject.alias].values.push(x[i]);
								}
							}
						}
					}
				}
				return formattedSelection;
			}
		}

		$scope.getColumnObjectFromName = function(columnSelectedOfDataset, originalName){
			for (i = 0; i < columnSelectedOfDataset.length; i++){
				if (columnSelectedOfDataset[i].name === originalName){
					return columnSelectedOfDataset[i];
				}
			}
		}

		var checkForSavedSelections = function (filtersParams,nature){
			$scope.selections.length = 0;
			if(filtersParams.hasOwnProperty($scope.ngModel.dataset.name) && filtersParams[$scope.ngModel.dataset.name].hasOwnProperty($scope.ngModel.content.selectedColumn.aliasToShow) ){
				var fp = filtersParams[$scope.ngModel.dataset.name][$scope.ngModel.content.selectedColumn.aliasToShow];

				if(fp.length == 0){
					$scope.selections = [];
				}else if(fp.length == 1){
					$scope.selections = fp[0].split(",");
				}else{
					$scope.selections = fp;
				}

				for (var i = 0; i < $scope.selections.length; i++) {
					$scope.selections[i] = $scope.selections[i].replace("')", "").replace("('", "").replace(/'/g,"")
				}
			}

			if($scope.hasDefaultValue == true){
				$scope.hasDefaultValue = false;
			}else if($scope.selections.length==0){
				$scope.hasDefaultValue = true;
			}

			if( $scope.ngModel.settings.modalityValue=="multiValue"){
				$scope.defaultValue = [];
			} else {
				$scope.defaultValue = "";
			}

			if($scope.hasDefaultValue && (nature == "refresh" || nature == "filters")){
				switch($scope.ngModel.settings.defaultValue.toUpperCase()){
				case 'FIRST':
					if(Array.isArray($scope.defaultValue)){
						$scope.defaultValue.push( $scope.datasetRecords.rows[0].column_1)
					} else {
						$scope.defaultValue =  $scope.datasetRecords.rows[0].column_1;
					}

					break;
				case 'LAST':
					if(Array.isArray($scope.defaultValue)){
						$scope.defaultValue.push($scope.datasetRecords.rows[$scope.datasetRecords.rows.length-1].column_1);
					} else {
						$scope.defaultValue =  $scope.datasetRecords.rows[$scope.datasetRecords.rows.length-1].column_1;
					}
					break;
				case 'STATIC':
					if(Array.isArray($scope.defaultValue)){
						$scope.defaultValue.push($scope.ngModel.settings.staticValue)
					} else {
						$scope.defaultValue =  $scope.ngModel.settings.staticValue;
					}
					break;
				default:
					if(!Array.isArray($scope.defaultValue)){
						$scope.defaultValue = "";
					}
					break;
				}
				var item = {};
				item.aggregated=$scope.aggregated;
				item.columnName=$scope.ngModel.content.selectedColumn.aliasToShow;
				item.columnAlias=$scope.ngModel.content.selectedColumn.aliasToShow;
				item.ds=$scope.ngModel.dataset.name;
				if($scope.ngModel.settings.defaultValue!=""){
					$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,$scope.defaultValue);
				}
			}else{
				$scope.defaultValue =  angular.copy($scope.selections);
			}
		}

		var checkRefreshSettings = function () {
			if($scope.ngModel.settings.modalityValue=="multiValue"){

				if($scope.ngModel.settings.modalityPresent=='COMBOBOX') {
					$scope.multiCombo.selected.length=0;
					if(Array.isArray($scope.defaultValue)){
						Array.prototype.push.apply($scope.multiCombo.selected, $scope.defaultValue);
					}
				} else {
					//multivalue list of checkboxes
					$scope.multiValue.length=0;
					if(Array.isArray($scope.defaultValue)){
						Array.prototype.push.apply($scope.multiValue, $scope.defaultValue);
						//case from other widget, but not delete from selection || case when all are checked from selector widget
					}
				}
			} else {
				if(Array.isArray($scope.defaultValue)){
					$scope.parameter = $scope.defaultValue[0] ? $scope.defaultValue[0]: "";
				} else {
					$scope.parameter = $scope.defaultValue;
				}
			}
		}

		var updateModel = function(){
			var datasetName = $scope.ngModel.dataset.name;
			var columnName = $scope.ngModel.content.selectedColumn.name;

			var selections = cockpitModule_widgetSelection.getCurrentSelections(datasetName);
			if(selections && selections[datasetName] && selections[datasetName][columnName]){
				updateValues(selections[datasetName][columnName]);
			}else{
				selections = cockpitModule_widgetSelection.getCurrentFilters(datasetName);
				if(selections && selections[datasetName] && selections[datasetName][columnName]){
					updateValues(selections[datasetName][columnName]);
				}else{
					if($scope.startDate && $scope.endDate){
						$scope.startDate = undefined;
						$scope.endDate = undefined;
					}
				}
			}
		}

		var updateValues = function(structuredValues){
			var values = [];
			for(var i in structuredValues){
				values = values.concat(structuredValues[i].slice(2,structuredValues[i].length-2).split("','"));
			}
			if($scope.ngModel.settings.modalityValue != 'multiValue'){
				$scope.parameter = (values.length == 1) ? values[0] : "";
			} else {
				if($scope.ngModel.settings.modalityPresent == 'LIST'){
					$scope.multiValue = values;
				} else {
					$scope.multiCombo.selected = values;
				}
			}
		}

		$scope.toggleCheckboxParameter = function(parVal) {
			cockpitModule_widgetSelection.setWidgetOfType("selector");
			cockpitModule_widgetSelection.setWidgetID($scope.ngModel.id);
			$scope.hasDefaultValue = false;
			var index = $scope.multiValue.indexOf(parVal);

			if (index > -1) {
				$scope.multiValue.splice(index, 1);
			} else {
				$scope.multiValue.push(parVal);

			}

			if($scope.multiValue.length>0){
				$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,$scope.multiValue);
			} else {
				var item = {};
				item.aggregated=$scope.aggregated;
				item.columnName=$scope.ngModel.content.selectedColumn.aliasToShow;
				item.columnAlias=$scope.ngModel.content.selectedColumn.aliasToShow;
				item.ds=$scope.ngModel.dataset.name;
				item.value=angular.copy($scope.multiValue);
				$rootScope.$broadcast('DELETE_SELECTION',item);
				$scope.deleteSelections(item);
			}
		};

		$scope.toggleRadioParameter = function(parVal ) {
			cockpitModule_widgetSelection.setWidgetOfType("selector");
			cockpitModule_widgetSelection.setWidgetID($scope.ngModel.id);
			$scope.hasDefaultValue = false;
			var item = {};
			item.aggregated=$scope.aggregated;
			item.columnName=$scope.ngModel.content.selectedColumn.aliasToShow;
			item.columnAlias=$scope.ngModel.content.selectedColumn.aliasToShow;
			item.ds=$scope.ngModel.dataset.name;

			if($scope.parameter != parVal){
				$scope.parameter = parVal;
				$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,parVal);
			} else {
				item.value=angular.copy($scope.parameter);
				$rootScope.$broadcast('DELETE_SELECTION',item);
				$scope.deleteSelections(item);
			}
		}

	    $scope.clearParamSearch = function() {
			$scope.searchParamText = "";
		};

		$scope.toggleComboParameter = function(parVal, single) {
			cockpitModule_widgetSelection.setWidgetOfType("selector");
			cockpitModule_widgetSelection.setWidgetID($scope.ngModel.id);
			$scope.hasDefaultValue = false;
			var item = {};
			item.aggregated=$scope.aggregated;
			item.columnName=$scope.ngModel.content.selectedColumn.aliasToShow;
			item.columnAlias=$scope.ngModel.content.selectedColumn.aliasToShow;
			item.ds=$scope.ngModel.dataset.name;

			if($scope.ngModel.settings.modalityValue=="multiValue"){
				var index = $scope.multiCombo.selected.indexOf(parVal);

				if (index > -1) {
					$scope.multiCombo.selected.splice(index, 1);
				} else {
					$scope.multiCombo.selected.push(parVal);

				}

				if($scope.multiCombo.selected.length>0){
					$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,angular.copy($scope.multiCombo.selected));
				} else {
					item.value=angular.copy($scope.multiCombo.selected);
					$rootScope.$broadcast('DELETE_SELECTION',item);
					$scope.deleteSelections(item);
				}
			} else {
				//signle
				if($scope.parameter != parVal){
					$scope.parameter = parVal;
					$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,$scope.parameter);
				} else {
					item.value=angular.copy($scope.parameter);
					$rootScope.$broadcast('DELETE_SELECTION',item);
					$scope.deleteSelections(item);
				}
			}
		}

		$scope.comboParameterExists = function (record) {
			for (var i = 0; i < $scope.parameter.length; i++) {
				return $scope.parameter.indexOf(record) > -1;
			}
		}

		$scope.checkboxParameterExists = function (parVal) {
			for (var i = 0; i < $scope.multiValue.length; i++) {
				return $scope.multiValue.indexOf(parVal) > -1;
			}
	    };

	    $scope.getOptions =function(){
	    	var isSortinEnabled = $scope.ngModel.content.sortingOrder && $scope.ngModel.content.sortingOrder!='';

			var obj = {};
			obj["page"] = -1;
			obj["itemPerPage"] = -1;
			obj["columnOrdering"] = isSortinEnabled ? { name: $scope.ngModel.content.selectedColumn.name } : undefined;
			obj["reverseOrdering"] = isSortinEnabled ? $scope.ngModel.content.sortingOrder == 'ASC' : undefined;
			obj["type"] = $scope.ngModel.type;

			return obj;
		}

	    $scope.deleteSelections = function(item){
	    	var reloadAss=false;
	    	var reloadFilt=[];

	    	if(item.aggregated){
				var key = item.ds + "." + item.columnName;

				for(var i=0; i<cockpitModule_template.configuration.aggregations.length; i++){
					if(cockpitModule_template.configuration.aggregations[i].datasets.indexOf(item.ds) !=-1){
						var selection = cockpitModule_template.configuration.aggregations[i].selection;
						if(selection){
							delete selection[key];
							reloadAss=true;
						}
					}
				}
			}else{
				if(cockpitModule_template.configuration.filters){
					if(cockpitModule_template.configuration.filters[item.ds]){
						delete cockpitModule_template.configuration.filters[item.ds][item.columnName];

						if(Object.keys(cockpitModule_template.configuration.filters[item.ds]).length==0){
							delete cockpitModule_template.configuration.filters[item.ds];
						}

						reloadFilt.push(item.ds);
					}
				}
			}

			if(reloadAss){
				cockpitModule_widgetSelection.getAssociations(true);
			}

			if(!reloadAss && reloadFilt.length!=0){
				cockpitModule_widgetSelection.refreshAllWidgetWhithSameDataset(reloadFilt);
			}

			var hs=false;
			for(var i=0; i<cockpitModule_template.configuration.aggregations.length; i++){
				if(Object.keys(cockpitModule_template.configuration.aggregations[i].selection).length>0){
					hs= true;
					break;
				}
			}

			if(hs==false && Object.keys(cockpitModule_template.configuration.filters).length==0){
				cockpitModule_properties.HAVE_SELECTIONS_OR_FILTERS=false;
			}
	    }
	};

	function selectorWidgetEditControllerFunction(
			$scope,
			finishEdit,
			sbiModule_translate,
			$mdDialog,
			originalModel,
			mdPanelRef,
			getMetadata,
			scopeFather,
			$mdToast){
		$scope.translate=sbiModule_translate;

		$scope.getMetadata = getMetadata;

		$scope.model = {};
		angular.copy(originalModel,$scope.model);

		$scope.saveConfiguration=function(){
			if($scope.model.dataset == undefined || $scope.model.dataset.dsId == undefined ){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.missingdataset'));
				return;
			}

			if($scope.model.content.selectedColumn == undefined || $scope.model.content.selectedColumn.length==0){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.nocolumns'));
				return;
			}

			if($scope.model.content.columnSelectedOfDataset == undefined || $scope.model.content.columnSelectedOfDataset.length==0){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.nocolumns'));
				return;
			}

			angular.copy($scope.model,originalModel);
			mdPanelRef.close();
			mdPanelRef.destroy();

			if(!scopeFather.ngModel.isNew){
				scopeFather.refreshWidget();
			}

			$scope.$destroy();
			finishEdit.resolve();
		}

		$scope.cancelConfiguration=function(){
			mdPanelRef.close();
			mdPanelRef.destroy();
			$scope.$destroy();
			finishEdit.reject();
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
	};

	addWidgetFunctionality("selector",{'initialDimension':{'width':5, 'height':5},'updateble':true,'cliccable':true});

})();