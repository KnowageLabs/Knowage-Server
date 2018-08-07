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
	  		return cockpitModule_generalServices.getTemplateUrl('selectorWidget',template);
	  	}
		
		$scope.isActive = function(p){
			return $scope.ngModel.activeValues && $scope.ngModel.activeValues.indexOf(p) == -1;
		}
		
		$scope.isSelected = function(p){
			return $scope.parameter == p;
		}
		
		$scope.selectElement = function(e,radio){
			if(e.target.attributes.disabled || e.target.parentNode.attributes.disabled) return;
			var tempValue;
			if(e.target.attributes.value && e.target.attributes.value.value){
				tempValue = e.target.attributes.value.value;
			}else {
				if(e.target.parentNode.attributes.value && e.target.parentNode.attributes.value.value){
					tempValue = e.target.parentNode.attributes.value.value;
				}
			}
			$scope.toggleComboParameter(tempValue);
		}
	
		$scope.accessibilityModeEnabled = accessibility_preferences.accessibilityModeEnabled;
		
		if ($scope.ngModel && $scope.ngModel.dataset && $scope.ngModel.dataset.dsId){
			var dataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
			$scope.ngModel.dataset.isRealtime = dataset.isRealtime;
			$scope.ngModel.dataset.label = dataset.label;
		}
		$scope.ngModel.activeValues = null;
		$scope.multiCombo = {};
		$scope.multiCombo.selected = [];
		$scope.multiValue = [];
		$scope.searchParamText = "";
		$scope.selectedTab = {'tab' : 0};
		$scope.widgetIsInit=false;
		$scope.totalCount = 0;
		$scope.translate = sbiModule_translate;
		$scope.datasetRecords = {};
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
					if(newValue != oldValue && newValue.length > 0){
						scope.itemList = scope.filterDataset(scope.itemList,scope.reformatSelections(newValue));
					}else{
						angular.copy(scope.savedRows, scope.itemList);
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
					templateUrl: $scope.getTemplateUrl('selectorWidgetEditPropertyTemplate'),
					position: $mdPanel.newPanelPosition().absolute().center(),
					fullscreen :true,
					hasBackdrop: true,
					clickOutsideToClose: false,
					escapeToClose: false,
					focusOnOpen: true,
					preserveScope: true,
					autoWrap:false,
					locals: {finishEdit: finishEdit, originalModel: $scope.ngModel, getMetadata: $scope.getMetadata, scopeFather: $scope},

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
			$scope.ngModel.activeValues = null;
			
			if(!$scope.ngModel.dataset.label){
				$scope.ngModel.dataset.label = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId).label;
			}

			$scope.aggregated = true;
			if(Object.keys($scope.cockpitModule_widgetSelection.getCurrentSelections($scope.ngModel.dataset.label)).length == 0){
				$scope.aggregated = false;
			}

			if(nature == 'gridster-resized' || nature == 'fullExpand' || nature == 'resize'){
				return;
			}

			$scope.datasetRecords = datasetRecords;
			
			checkForSavedSelections(nature);
			
			updateModel();
			$scope.showSelection = false;
			if(datasetRecords.activeValues){
				datasetRecords.activeValues.then(function(activeValues){
					
					var tempActs = [];
					for(var k in activeValues.rows){
						tempActs.push(activeValues.rows[k].column_1);
					}
					
					updateModel(tempActs);
					
					$scope.showSelection = true;
				},function(error){})
			}else{
				$scope.showSelection = true;
			}
			$scope.hideWidgetSpinner();
			
			
		}


		var checkForSavedSelections = function (nature){
			var datasetLabel = $scope.ngModel.dataset.label;
			var columnName = $scope.ngModel.content.selectedColumn.name;
			var selections = cockpitModule_widgetSelection.getSelectionValues(datasetLabel,columnName);

			$scope.hasDefaultValue = !selections || selections.length==0;

			if( $scope.ngModel.settings.modalityValue=="multiValue"){
				$scope.defaultValue = [];
			} else {
				$scope.defaultValue = "";
			}

			if($scope.hasDefaultValue && (nature == "init" || nature == "refresh" || nature=='selections' || nature == "filters")){
				var applyDefaultValues = false;

				switch($scope.ngModel.settings.defaultValue.toUpperCase()){
				case 'FIRST':
					if(Array.isArray($scope.defaultValue)){
						$scope.defaultValue.push( $scope.datasetRecords.rows[0].column_1)
					} else {
						$scope.defaultValue =  $scope.datasetRecords.rows[0].column_1;
					}
					applyDefaultValues = true;
					break;
				case 'LAST':
					if(Array.isArray($scope.defaultValue)){
						$scope.defaultValue.push($scope.datasetRecords.rows[$scope.datasetRecords.rows.length-1].column_1);
					} else {
						$scope.defaultValue =  $scope.datasetRecords.rows[$scope.datasetRecords.rows.length-1].column_1;
					}
					applyDefaultValues = true;
					break;
				case 'STATIC':
					if(Array.isArray($scope.defaultValue)){
						$scope.defaultValue.push($scope.ngModel.settings.staticValue)
					} else {
						$scope.defaultValue =  $scope.ngModel.settings.staticValue;
					}
					applyDefaultValues = true;
					break;
				}

				if(applyDefaultValues){
					var item = {};
					item.aggregated=$scope.aggregated;
					item.columnName=$scope.ngModel.content.selectedColumn.aliasToShow;
					item.columnAlias=$scope.ngModel.content.selectedColumn.aliasToShow;
					item.ds=$scope.ngModel.dataset.label;
					$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,$scope.defaultValue);
				}else{
					$scope.defaultValue = angular.copy(selections);
					checkRefreshSettings();
				}
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

		var updateModel = function(activeVals){
			var datasetLabel = $scope.ngModel.dataset.label;
			var columnName = $scope.ngModel.content.selectedColumn.name;

			if(activeVals){
				$scope.ngModel.activeValues = activeVals;
			}
			var values = cockpitModule_widgetSelection.getSelectionValues(datasetLabel,columnName);
			if(values){
				updateValues(values);
			}
		}

		var updateValues = function(values){
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

//		$scope.toggleCheckboxParameter = function(parVal) {
//			cockpitModule_widgetSelection.setWidgetOfType("selector");
//			cockpitModule_widgetSelection.setWidgetID($scope.ngModel.id);
//			$scope.hasDefaultValue = false;
//			var index = $scope.multiValue.indexOf(parVal);
//
//			if (index > -1) {
//				$scope.multiValue.splice(index, 1);
//			} else {
//				$scope.multiValue.push(parVal);
//
//			}
//
//			if($scope.multiValue.length>0){
//				$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,$scope.multiValue);
//			} else {
//				var item = {};
//				item.aggregated=$scope.aggregated;
//				item.columnName=$scope.ngModel.content.selectedColumn.aliasToShow;
//				item.columnAlias=$scope.ngModel.content.selectedColumn.aliasToShow;
//				item.ds=$scope.ngModel.dataset.label;
//				item.value=angular.copy($scope.multiValue);
//				$rootScope.$broadcast('DELETE_SELECTION',item);
//				$scope.deleteSelections(item);
//			}
//		};

//		$scope.toggleRadioParameter = function(parVal ) {
//			cockpitModule_widgetSelection.setWidgetOfType("selector");
//			cockpitModule_widgetSelection.setWidgetID($scope.ngModel.id);
//			$scope.hasDefaultValue = false;
//			var item = {};
//			item.aggregated=$scope.aggregated;
//			item.columnName=$scope.ngModel.content.selectedColumn.aliasToShow;
//			item.columnAlias=$scope.ngModel.content.selectedColumn.aliasToShow;
//			item.ds=$scope.ngModel.dataset.label;
//
//			if($scope.parameter != parVal){
//				$scope.parameter = parVal;
//				$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,parVal);
//			} else {
//				item.value=angular.copy($scope.parameter);
//				$rootScope.$broadcast('DELETE_SELECTION',item);
//				$scope.deleteSelections(item);
//			}
//		}

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
			item.ds=$scope.ngModel.dataset.label;

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