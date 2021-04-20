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
						// init the widget
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
			$interval,
			$mdPanel,
			$q,
			$sce,
			$filter,
			sbiModule_translate,
			sbiModule_restServices,
			sbiModule_dateServices,
			sbiModule_config,
			cockpitModule_analyticalDrivers,
			cockpitModule_datasetServices,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetServices,
			cockpitModule_widgetSelection,
			cockpitModule_properties,
			cockpitModule_template,
			accessibility_preferences,
			cockpitModule_generalServices,
			$rootScope){

		$scope.getTemplateUrl = function(template){
			return $sce.trustAsResourceUrl(cockpitModule_generalServices.getTemplateUrl('selectorWidget',template));
		}

		if(!$scope.ngModel.settings) $scope.ngModel.settings = {};
		if($scope.ngModel.settings.modalityPresent == 'COMBOBOX' && !$scope.ngModel.settings.modalityValue) $scope.ngModel.settings.modalityValue = "dropdown";

		$scope.isDisabled = function(p){
			if ($scope.ngModel.settings.enableAll) {
				return false;
			}
			if($scope.ngModel.settings.modalityValue=="dropdown"){
				return $scope.ngModel.activeValues && $scope.ngModel.activeValues.indexOf(p) == -1 && $scope.selectedValues.indexOf(p) == -1;
			}else{
				return $scope.ngModel.activeValues && $scope.ngModel.activeValues.indexOf(p) == -1;
			}
		}

		$scope.isSelected = function(p){
			return $scope.selectedValues && $scope.selectedValues.indexOf(p) > -1;
		}
		
		$scope.isSelectedColumnTemporal = function(){
			if($scope.ngModel.content.selectedColumn){
				var datesFormat = ['java.sql.Date','java.util.Date','java.sql.Timestamp','oracle.sql.TIMESTAMP'];
				return (datesFormat.indexOf($scope.ngModel.content.selectedColumn.type) != -1);
			}else return false;
		}

		$scope.mapToColumn = function(x){
			return x.column_1;
		}

		$scope.selectElement = function(e,isBulk){
			if(e.target.attributes.disabled || e.target.parentNode.attributes.disabled) return;

			if(e.target.attributes.value && e.target.attributes.value.value){
				if(!isBulk) $scope.toggleParameter(getValueFromString(e.target.attributes.value.value));
				else $scope.prepareParameter(getValueFromString(e.target.attributes.value.value));
			}else if(e.target.querySelector("input") && e.target.querySelector("input").value){
				if(!isBulk) $scope.toggleParameter(getValueFromString(e.target.querySelector("input").value));
				else $scope.prepareParameter(getValueFromString(e.target.querySelector("input").value));
			}else if(e.target.parentNode.attributes.value && e.target.parentNode.attributes.value.value){
				if(!isBulk) $scope.toggleParameter(getValueFromString(e.target.parentNode.attributes.value.value));
				else $scope.prepareParameter(getValueFromString(e.target.parentNode.attributes.value.value));
			}
			else if(e.target.parentNode.querySelectorAll("input")[0].value){
				 $scope.toggleParameter(getValueFromString(e.target.parentNode.querySelectorAll("input")[0].value)); 
			}
			
		}
		
//		$scope.$watch("selectedDate.startDate",function(newValue,oldValue){
//			if (newValue){
//				$scope.selectDate();
//			}
//		})
//		
//		$scope.$watch("selectedDate.endDate",function(newValue,oldValue){
//			if (newValue){
//				$scope.selectDate();
//			}
//		})
		
		$scope.selectDate = function(){
			var tempDates = [];
			if($scope.ngModel.settings.modalityValue=='multiValue'){
				if(!$scope.selectedDate.startDate || !$scope.selectedDate.endDate) return;
				var from = $scope.selectedDate.startDate.getTime();
				var to = $scope.selectedDate.endDate.getTime();
				var values = $scope.ngModel.activeValues || $scope.datasetRecords.rows;
				for(var k in values){
					var value = values[k].column_1 || values[k];
					var dateToCheck = moment(value,'DD/MM/YYYY HH:mm:ss.SSS').valueOf();
					if(dateToCheck >= from && dateToCheck <= to) tempDates.push(value);
				}
			}else tempDates.push($filter('date')($scope.selectedDate.startDate,'dd/MM/yyyy HH:mm:ss.sss'));
			$scope.toggleParameter(tempDates);
		}

		var getValueFromString = function(s){
			for(i in $scope.datasetRecords.rows){
				var value = $scope.datasetRecords.rows[i].column_1;
				if("" + value == s){
					return value;
				}
			}
			return null;
		}

		$scope.gridWidth = function() {
			var tempStyle = $scope.ngModel.style ? $scope.ngModel.style : {};
			if($scope.ngModel.settings.modalityView == 'grid' && $scope.ngModel.settings.gridColumnsWidth){
				tempStyle.width = $scope.ngModel.settings.gridColumnsWidth;
			}else{
				if(tempStyle.width) delete tempStyle.width;
			}
			return tempStyle;
		}

		$scope.accessibilityModeEnabled = accessibility_preferences.accessibilityModeEnabled;

		if ($scope.ngModel && $scope.ngModel.dataset && $scope.ngModel.dataset.dsId){
			var dataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
			$scope.ngModel.dataset.isRealtime = dataset.isRealtime;
			$scope.ngModel.dataset.label = dataset.label;
		}

		$scope.ngModel.activeValues = null;

		$scope.selectedValues = [];
		$scope.tempSelectedValues = [];
		$scope.oldSelectedValues = null;
		$scope.showInfoBar = false;

		$scope.searchParamText = "";
		$scope.selectedTab = {'tab' : 0};
		$scope.widgetIsInit=false;
		$scope.totalCount = 0;

		$scope.translate = sbiModule_translate;
		$scope.datasetRecords = {};
		$scope.cockpitModule_widgetSelection = cockpitModule_widgetSelection;
		$scope.realTimeSelections = cockpitModule_widgetServices.realtimeSelections;
		

		// set a watcher on a variable that can contains the associative selections for realtime dataset
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

		if($scope.ngModel.settings.staticValue != undefined){
			$scope.ngModel.settings.staticValues = $scope.ngModel.settings.staticValue;
			delete $scope.ngModel.settings.staticValue;
		}

		if(!$scope.ngModel.style){
			$scope.ngModel.style={};
		}

		$scope.init=function(element,width,height){
			$scope.refreshWidget(null, 'init');

		}

		$scope.refresh=function(element,width,height, datasetRecords,nature){
			$scope.showUnlock = false;
			$scope.showWidgetSpinner();
			$scope.ngModel.activeValues = null;
			
			if($scope.ngModel.settings.defaultStartDate) $scope.ngModel.settings.defaultStartDate = new Date($scope.ngModel.settings.defaultStartDate);
			if($scope.ngModel.settings.defaultEndDate) $scope.ngModel.settings.defaultEndDate = new Date($scope.ngModel.settings.defaultEndDate);

			if(!$scope.ngModel.dataset.label){
				$scope.ngModel.dataset.label = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId).label;
			}

			$scope.aggregated = true;
			if(Object.keys($scope.cockpitModule_widgetSelection.getCurrentSelections($scope.ngModel.dataset.label)).length == 0){
				$scope.aggregated = false;
				$scope.oldSelectedValues = null;
			}

			if(nature == 'gridster-resized' || nature == 'fullExpand' || nature == 'resize'){
				$scope.hideWidgetSpinner();
				return;
			}

			$scope.datasetRecords.activeValues = datasetRecords.activeValues;
			if(datasetRecords.rows){
				$scope.datasetRecords.metaData = datasetRecords.metaData;
				if(!angular.equals($scope.datasetRecords.rows, datasetRecords.rows)){
					$scope.datasetRecords.rows = datasetRecords.rows;
				}
			}

			checkForSavedSelections(nature);

			updateModel();

			$scope.showSelection = false;
			if(datasetRecords.activeValues  && !Array.isArray(datasetRecords.activeValues) ){
				datasetRecords.activeValues.then(function(activeValues){
					var tempActs = [];
					for(var k in activeValues.rows){
						tempActs.push(activeValues.rows[k].column_1);
					}
					$scope.ngModel.activeValues = tempActs;
					$scope.hideWidgetSpinner();
					$scope.showSelection = true;
					$scope.waitingForSelection = false;
					$scope.tempSelectedValues = angular.copy($scope.selectedValues);
					if($scope.selectedValues && $scope.selectedValues.length > 0 && !$scope.ngModel.settings.enableAll) $scope.showUnlock = true;
					if($scope.savedParameters && $scope.savedParameters.length > 0){
						$scope.selectedValues = angular.copy($scope.savedParameters);
						$scope.tempSelectedValues = angular.copy($scope.savedParameters);
						$scope.showUnlock = false;
						$scope.showInfoBar = true;
					}
					if($scope.datasetRecords.rows && $scope.ngModel.settings.modalityValue != 'singleValue' && $scope.ngModel.settings.modalityValue != 'multiValue'){
						$scope.datasetRecords.rows = $filter('orderBy')($scope.datasetRecords.rows, function(item){
							if($scope.isSelected(item.column_1)) return 1;
							if(!$scope.isDisabled(item.column_1)) return 2;
							return 3;
						})
					}
				},function(error){
					console.error("Unable to load active values");
					$scope.selectedValues = [];
					$scope.tempSelectedValues = [];
					$scope.showUnlock = false;
					$scope.showInfoBar = false;
					$scope.hideWidgetSpinner();
					$scope.showSelection = true;
					$scope.waitingForSelection = false;
				})
			}else{
				if(!cockpitModule_widgetSelection.isLastTimestampedSelection($scope.ngModel.dataset.label, $scope.ngModel.content.selectedColumn.name))$scope.ngModel.activeValues = null;
				else $scope.ngModel.activeValues = datasetRecords.activeValues;
				$timeout(function(){
					$scope.hideWidgetSpinner();
					$scope.showSelection = true;
					$scope.waitingForSelection = false;
				}, 0);

				$scope.tempSelectedValues = $scope.selectedValues ? angular.copy($scope.selectedValues) : [];
				if($scope.selectedValues && $scope.selectedValues.length > 0 && !$scope.ngModel.settings.enableAll) $scope.showUnlock = true;
				if($scope.savedParameters && $scope.savedParameters.length > 0){
					$scope.selectedValues = angular.copy($scope.savedParameters);
					$scope.tempSelectedValues = angular.copy($scope.savedParameters);
					$scope.showUnlock = false;
					$scope.showInfoBar = true;
				}
				if($scope.datasetRecords.rows){
					$scope.datasetRecords.rows = $filter('orderBy')($scope.datasetRecords.rows, function(item){
						if($scope.isSelected(item.column_1)) return 1;
						if(!$scope.isDisabled(item.column_1)) return 2;
						return 3;
					})
				}
			}



			if(nature == 'init'){
				$timeout(function(){
					$scope.widgetIsInit=true;
					cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
				},500);
			}
		}

		$scope.mobilecheck = function() {
			var check = false;
			(function(a){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))) check = true;})(navigator.userAgent||navigator.vendor||window.opera);
			return check;
		};

		$scope.openSelectDialog = function(ev, column){
			$mdDialog.show({
				controller: MultiSelectDialogController,
				fullscreen: $scope.mobilecheck,
				templateUrl: $scope.getTemplateUrl('selectorWidgetMultiSelectDialogTemplate'),
				parent: angular.element(document.body),
				targetEvent: ev,
				clickOutsideToClose:false,
				bindToController: true,
				locals: {
					itemsList:$scope.datasetRecords.rows,
					selectables:$scope.ngModel.activeValues,
					activeSelections: $scope.selectedValues,
					targetModel: $scope.ngModel.content,
					settings:$scope.ngModel.settings,
					callback:$scope.toggleParameter,
					updateSelectables: $scope.updateSelectables,
					ds: $scope.ngModel.dataset.label,
					title:($scope.ngModel.style.title && $scope.ngModel.style.title.label) ? $scope.ngModel.style.title.label : $scope.ngModel.content.name
				}
			}).then(function(selectedFields) {
				$scope.toggleParameter(selectedFields);
			},function(pendingSelection){
				if (pendingSelection) {
					$scope.toggleParameter(pendingSelection);
				}
			});
		}

		function MultiSelectDialogController($rootScope, scope, $mdDialog, $filter, sbiModule_translate, targetModel, activeSelections, itemsList, selectables, settings, title, ds, callback,updateSelectables) {
			scope.activeSelections = activeSelections;
			scope.settings = settings;
			scope.title = $filter('i18n')(title);
			scope.translate = sbiModule_translate;
			scope.allSelected = false;

			scope.isDisabled = function(p){
				if ($scope.ngModel.settings.enableAll) {
					return false;
				}
				return selectables && selectables.indexOf(p.name || p) == -1 && (activeSelections.indexOf(p) == -1);
			}

			scope.checkActiveSelections = function() {
				scope.selectables = [];
				if(settings.hideDisabled){

					if(selectables){
						for(var k in selectables){
							scope.selectables.push({name: selectables[k], selected: (activeSelections && activeSelections.indexOf(selectables[k]) != -1) ? true : false});
						}
					}else{
						for(var j in itemsList){
							if(activeSelections.length > 0){
								if(activeSelections.indexOf(itemsList[j].column_1) != -1){
									scope.selectables.push({name: itemsList[j].column_1, selected: true});
								}else {
									scope.selectables.push({name: itemsList[j].column_1, selected: false});
								}
							}else {
								scope.selectables.push({name: itemsList[j].column_1, selected: false});
							}

						}
					}
				}else{
					for(var j in itemsList){
						scope.selectables.push({name: itemsList[j].column_1, selected: (activeSelections && activeSelections.indexOf(itemsList[j].column_1) != -1) ? true : false});
					}
				}
				if(targetModel.sortingOrder) {
					var direction = targetModel.sortingOrder == 'DESC' ? true : false;
					scope.selectables = $filter('orderBy')(scope.selectables,"name", direction);
				}

				scope.selectables = $filter('orderBy')(scope.selectables, function(item){
					if(item.selected) return 1;
					if(!scope.isDisabled(item)) return 2;
					return 3;
				})
				scope.loading = false;
			}
			scope.checkActiveSelections();
			scope.targetColumn = targetModel.selectedColumn;
			scope.close = function() {
				scope.selectablesToSend = scope.selectables.reduce(function(result, element) {
					if(element.selected) result.push(element.name);
					return result;
				}, []);
				$mdDialog.hide(scope.selectablesToSend);
			};
			scope.cancel = function(){
				$mdDialog.cancel(scope.tempActiveSelections);
			}

			scope.editSelection = function() {
				scope.loading = true;
				scope.hideUnlock = true;
				scope.tempSelectables = selectables;
				scope.tempActiveSelections = activeSelections;
				for(var s in scope.availableItems){
					scope.availableItems[s].selected = false;
				}
				callback([],true);
				updateSelectables().then(function(newSelectables){
					selectables = newSelectables;
					scope.checkActiveSelections();
				});
			}

			scope.selectAll = function(){
				scope.allSelected = !scope.allSelected;
				for(var s in scope.availableItems){
					if(!scope.isDisabled(scope.availableItems[s].name)) scope.availableItems[s].selected = scope.allSelected;
				}
			}

		}



		var checkForSavedSelections = function(nature){
			var datasetLabel = $scope.ngModel.dataset.label;
			var columnName = $scope.ngModel.content.selectedColumn.aliasToShow;
			var selections = $scope.cockpitModule_widgetSelection.getSelectionValues(datasetLabel,columnName);

			$scope.hasDefaultValues = !selections || selections.length==0;

			$scope.defaultValues = [];

			if($scope.hasDefaultValues && (nature == "init" || nature == "refresh")){
				var applyDefaultValues = false;

				if($scope.ngModel.settings.defaultValue && $scope.datasetRecords && $scope.datasetRecords.rows && ($scope.datasetRecords.rows.length > 0)){
					switch($scope.ngModel.settings.defaultValue.toUpperCase()){
					case 'FIRST':
						$scope.defaultValues.push($scope.datasetRecords.rows[0].column_1);
						applyDefaultValues = true;
						break;
					case 'LAST':
						$scope.defaultValues.push($scope.datasetRecords.rows[$scope.datasetRecords.rows.length-1].column_1);
						applyDefaultValues = true;
						break;
					case 'STATIC':
						if($scope.ngModel.settings.staticValues) {
							$scope.defaultValues = angular.copy($scope.ngModel.settings.staticValues);
							$scope.defaultValues = $scope.defaultValues.replace(/\$V\{([a-zA-Z0-9]+)\}/g,function(match,p1){
								if(!cockpitModule_properties.VARIABLES[p1]) return null;
								else return cockpitModule_properties.VARIABLES[p1] || null;
							})
							$scope.defaultValues = $scope.defaultValues.replace(/\$P\{([a-zA-Z0-9]+)\}/g,function(match,p1){
								p1 = cockpitModule_analyticalDrivers[p1] || null;
								return p1;
							})
							$scope.defaultValues = $scope.defaultValues.split(",");
						}
						else $scope.defaultValues.push('');
						applyDefaultValues = true;
						break;
					}
				}

				if(applyDefaultValues){
					var item = {};
					item.aggregated=$scope.aggregated;
					item.columnName=$scope.ngModel.content.selectedColumn.aliasToShow;
					item.columnAlias=$scope.ngModel.content.selectedColumn.aliasToShow;
					item.ds=$scope.ngModel.dataset.label;

					if (nature == "init")
						$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow, $scope.defaultValues);
				}else{
					if(selections && !angular.equals($scope.defaultValues, selections)){
						$scope.defaultValues = angular.copy(selections);
					}
					if(!angular.equals($scope.selectedValues, $scope.defaultValues)){
						$scope.selectedValues = angular.copy($scope.defaultValues);
					}
				}
			}
		}

		var updateModel = function(activeVals){
			var datasetLabel = $scope.ngModel.dataset.label;
			var columnName = $scope.ngModel.content.selectedColumn.aliasToShow;
			var values = $scope.cockpitModule_widgetSelection.getSelectionValues(datasetLabel,columnName);
			updateValues(values);
		}

		var updateValues = function(values){
			if(values){
				if(!angular.equals($scope.selectedValues, values)){
					$scope.selectedValues = angular.copy(values);
					$scope.tempSelectedValues = angular.copy($scope.selectedValues);
				}
			}else{
				$scope.selectedValues = [];
			}
		}

		$scope.clearParamSearch = function() {
			$scope.searchParamText = "";
		};

		$scope.updateSelectables = function(){
			return $q(function(resolve, reject) {
				var pollingInterval = $interval(function(){
					if(!$scope.waitingForSelection) {
						$interval.cancel(pollingInterval);
						resolve($scope.ngModel.activeValues);
					}
				},300)
			})
		}

		$scope.cancelBulkSelection = function(){
			$scope.tempSelectedValues = [];
			$scope.showInfoBar = false;
		}

		function arraysEqual(a, b) {
			  if (a === b) return true;
			  if (a == null || b == null) return false;
			  if (a.length != b.length) return false;

			  for (var i = 0; i < a.length; ++i) {
			    if (a[i] !== b[i]) return false;
			  }
			  return true;
			}

		$scope.prepareParameter = function(parVal){
			if ($scope.tempSelectedValues.indexOf(parVal) > -1) {
				$scope.tempSelectedValues.splice($scope.tempSelectedValues.indexOf(parVal), 1);
			} else {
				$scope.tempSelectedValues.push(parVal);
			}
			if($scope.tempSelectedValues.length == 0){
				if($scope.selectedValues && $scope.selectedValues.length != $scope.tempSelectedValues.length){
					$scope.showInfoBar = true;
				}else $scope.showInfoBar = false;
			}else if(arraysEqual($scope.selectedValues, $scope.tempSelectedValues)){
				$scope.showInfoBar = false;
			}else $scope.showInfoBar = true;
		}



		$scope.bulkSelect = function(){
			$scope.savedParameters = [];
			$scope.toggleParameter($scope.tempSelectedValues);
			$scope.cancelBulkSelection();
		}

		$scope.unlock = function(){
			if($scope.ngModel.settings.modalityValue == 'multiValue'){
				$scope.savedParameters = $scope.tempSelectedValues.length > 0 ? angular.copy($scope.tempSelectedValues) : angular.copy($scope.selectedValues);
				$scope.tempSelectedValues = [];
			}
			$scope.toggleParameter([]);
		}

		$scope.toggleParameter = function(parVal,setLoader) {

			if(setLoader) $scope.waitingForSelection = setLoader;
			if($scope.ngModel.settings.modalityPresent=="COMBOBOX" && $scope.ngModel.settings.modalityValue!='multiValue'){
				if(angular.equals(parVal, $scope.oldSelectedValues)){
					return;
				}
				$scope.oldSelectedValues = angular.copy(parVal);
			}
			$scope.hasDefaultValues = false;

			var item = {};
			item.aggregated=$scope.aggregated;
			item.columnName=$scope.ngModel.content.selectedColumn.aliasToShow;
			item.columnAlias=$scope.ngModel.content.selectedColumn.aliasToShow;
			item.ds=$scope.ngModel.dataset.label;

			if($scope.ngModel.settings.modalityValue=="multiValue"){
				if(parVal.length>0){
					$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,angular.copy(parVal));
				} else {
					item.value=angular.copy(parVal);
					$rootScope.$broadcast('DELETE_SELECTION',item);
					$scope.deleteSelections(item);
				}
			} else { // singleValue
				if($scope.ngModel.settings.modalityValue!="dropdown"){
					if($scope.selectedValues[0] != parVal){
						if (!parVal || parVal.length == 0) {
							item.value=angular.copy($scope.selectedValues[0]);
							$rootScope.$broadcast('DELETE_SELECTION',item);
							$scope.deleteSelections(item);
						}
						else {
							$scope.selectedValues[0] = parVal;
							$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow, $scope.selectedValues[0]);
						}
					} else {
						if ($scope.selectedValues[0]) {
							$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow, $scope.selectedValues[0]);
						}
						else {
							item.value=angular.copy($scope.selectedValues[0]);
							$rootScope.$broadcast('DELETE_SELECTION',item);
							$scope.deleteSelections(item);
						}

					}
				}else{ // COMBOBOX
					if(parVal && (parVal.length>0 || (!isNaN(parVal) && !Array.isArray(parVal)))){
						$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow, angular.copy(parVal));
					}else{
						item.value=angular.copy(parVal);
						$rootScope.$broadcast('DELETE_SELECTION',item);
						$scope.deleteSelections(item);
					}
				}
			}
		}

		$scope.getOptions = function(){
			var isSortinEnabled = false;
			if($scope.ngModel.content.sortingOrder){
				if($scope.ngModel.content.sortingOrder === '') isSortinEnabled = false;
				else isSortinEnabled = true;
			} 

			var obj = {};
			obj["page"] = -1;
			obj["itemPerPage"] = -1;
			obj["columnOrdering"] = isSortinEnabled ? { name: $scope.ngModel.content.selectedColumn.name } : undefined;
			obj["reverseOrdering"] = isSortinEnabled ? $scope.ngModel.content.sortingOrder == 'ASC' : undefined;
			obj["type"] = $scope.ngModel.type;

			return obj;
		}

		$scope.clearAllSelection = function(){
			var tempItem = {
					"aggregated" : $scope.aggregated,
					"ds" : $scope.ngModel.dataset.label,
					"columnName" : $scope.ngModel.content.selectedColumn.name
			}
			$scope.deleteSelections(tempItem);
		}
		
		$scope.clearStartDate = function(){
			$scope.selectedDate = {};
		}
		
		$scope.clearStartDate();
		
		$scope.deleteSelections = function(item){
			var reloadAss=false;
			var associatedDatasets = [];
			var reloadFilt=[];

			if(item.aggregated){
				var key = item.ds + "." + item.columnName;

				for(var i=0; i<cockpitModule_template.configuration.aggregations.length; i++){
					if(cockpitModule_template.configuration.aggregations[i].datasets.indexOf(item.ds) !=-1){
						var selection = cockpitModule_template.configuration.aggregations[i].selection;
						if(selection){
							delete selection[key];
							reloadAss=true;
							associatedDatasets.push(item.ds);
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
						if(reloadFilt.indexOf(item.ds) == -1){
							reloadFilt.push(item.ds);
						}
					}
				}
			}

			if(reloadAss){
				$scope.cockpitModule_widgetSelection.getAssociations(true,undefined,undefined,associatedDatasets);
			}

			cockpitModule_widgetSelection.removeTimestampedSelection(item.ds, item.columnName);

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

			setTimeout(function() {
				for(var i in reloadFilt){
					cockpitModule_widgetSelection.refreshAllWidgetWhithSameDataset(reloadFilt[i]);
				}
			}, 0);
		}

		$scope.editWidget=function(index){
			var finishEdit=$q.defer();
			var config = {
					attachTo:  angular.element(document.body),
					controller: selectorWidgetEditControllerFunction,
					disableParentScroll: true,
					templateUrl: $scope.getTemplateUrl('selectorWidgetEditPropertyTemplate'),
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
			$mdToast,
			cockpitModule_generalOptions){
		$scope.translate=sbiModule_translate;
		$scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
		$scope.getMetadata = getMetadata;

		$scope.model = {};
		angular.copy(originalModel,$scope.model);
		$scope.tempSelectedColumn = $scope.model.content.selectedColumn && $scope.model.content.selectedColumn.alias;
		
		$scope.changeColumn = function(){
			for(var k in $scope.model.content.columnSelectedOfDataset){
				if($scope.model.content.columnSelectedOfDataset[k].alias == $scope.tempSelectedColumn){
					$scope.model.content.selectedColumn = $scope.model.content.columnSelectedOfDataset[k];
					break;
				}
			}
		}
		
		$scope.$watch('model.content.selectedColumn',function(newValue,oldValue){
			if(newValue){
				$scope.model.settings.sortingColumn = newValue.name;
			}
		})
		
		$scope.$watch('model.content.sortingOrder',function(newValue,oldValue){
			if(newValue){
				$scope.model.settings.sortingOrder = newValue;
			}
		})

		$scope.saveConfiguration=function(){
			if($scope.model.dataset == undefined || $scope.model.dataset.dsId == undefined ){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.missingdataset'));
				return;
			}

			if($scope.model.content.selectedColumn == undefined || $scope.model.content.selectedColumn.length==0){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.nocolumns'));
				return;
			}
			
			if($scope.model.settings.defaultValue=='STATIC' && !$scope.model.settings.staticValues){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.nodefault'));
				return;
			}

			if($scope.model.content.columnSelectedOfDataset == undefined || $scope.model.content.columnSelectedOfDataset.length==0){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.nocolumns'));
				return;
			}

			if(typeof $scope.model.settings.modalityValue == 'undefined'){
				$scope.showAction($scope.translate.load('kn.table.nomodality'));
				return;
			}

			$scope.model.content.columnSelectedOfDataset.length = 0;
			$scope.model.content.columnSelectedOfDataset.push($scope.model.content.selectedColumn);

			angular.copy($scope.model,originalModel);
			mdPanelRef.close();
			mdPanelRef.destroy();

			if(!scopeFather.ngModel.isNew){
				scopeFather.refreshWidget(null,'init');
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