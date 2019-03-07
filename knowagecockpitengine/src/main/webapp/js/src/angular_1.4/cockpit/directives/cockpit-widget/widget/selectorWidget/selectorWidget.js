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

		$scope.isDisabled = function(p){
			if($scope.ngModel.settings.modalityPresent=="COMBOBOX" && $scope.ngModel.settings.modalityValue=="singleValue"){
				return $scope.ngModel.activeValues && $scope.ngModel.activeValues.indexOf(p) == -1 && $scope.selectedValues.indexOf(p) == -1;
			}else{
				return $scope.ngModel.activeValues && $scope.ngModel.activeValues.indexOf(p) == -1;
			}
		}

		$scope.isSelected = function(p){
			return $scope.selectedValues && $scope.selectedValues.indexOf(p) > -1;
		}

		$scope.mapToColumn = function(x){
			return x.column_1;
		}

		$scope.selectElement = function(e){
			if(e.target.attributes.disabled || e.target.parentNode.attributes.disabled) return;

			var tempValue;
			if(e.target.attributes.value && e.target.attributes.value.value){
				$scope.toggleParameter(getValueFromString(e.target.attributes.value.value));
			}else if(e.target.parentNode.attributes.value && e.target.parentNode.attributes.value.value){
                $scope.toggleParameter(getValueFromString(e.target.parentNode.attributes.value.value));
            }
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
		$scope.oldSelectedValues = null;

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

		if($scope.ngModel.settings.staticValue != undefined){
		    $scope.ngModel.settings.staticValues = $scope.ngModel.settings.staticValue;
		    delete $scope.ngModel.settings.staticValue;
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

		$scope.init=function(element,width,height){
			$scope.refreshWidget(null, 'init');
			$timeout(function(){
				$scope.widgetIsInit=true;
			},500);

		}

		$scope.refresh=function(element,width,height, datasetRecords,nature){
			$scope.showWidgetSpinner();
			$scope.ngModel.activeValues = null;

			if(!$scope.ngModel.dataset.label){
				$scope.ngModel.dataset.label = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId).label;
			}

			$scope.aggregated = true;
			if(Object.keys($scope.cockpitModule_widgetSelection.getCurrentSelections($scope.ngModel.dataset.label)).length == 0){
				$scope.aggregated = false;
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
			if(datasetRecords.activeValues){
				datasetRecords.activeValues.then(function(activeValues){
					var tempActs = [];
					for(var k in activeValues.rows){
						tempActs.push(activeValues.rows[k].column_1);
					}
					$scope.ngModel.activeValues = tempActs;
					$scope.hideWidgetSpinner();
					$scope.showSelection = true;
				},function(error){
				    console.error("Unable to load active values");
				    $scope.hideWidgetSpinner();
				    $scope.showSelection = true;
				})
			}else{
			    $scope.ngModel.activeValues = null;
				$timeout(function(){
					$scope.hideWidgetSpinner();
					$scope.showSelection = true;
				}, 0);
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
		      clickOutsideToClose:true,
		      locals: {selectables:$scope.ngModel.activeValues, itemsList:$scope.datasetRecords.rows, activeSelections: $scope.selectedValues, targetModel: $scope.ngModel.content, settings:$scope.ngModel.settings}
		    }).then(function(selectedFields) {
		    	$scope.toggleParameter(selectedFields);
		    },function(error){});
		}
		
		function MultiSelectDialogController(scope, $mdDialog, sbiModule_translate, targetModel, selectables, activeSelections, itemsList, settings) {
			scope.settings = settings;
			scope.translate = sbiModule_translate;
			scope.selectables = [];
			scope.allSelected = false;
			if(settings.hideDisabled) {
				for(var k in selectables){
					scope.selectables.push({name: selectables[k], selected: (activeSelections && activeSelections.indexOf(selectables[k].column_1) != -1) ? true : false });
				}
			}else {
				for(var j in itemsList){
					scope.selectables.push({name: itemsList[j].column_1, selected: (activeSelections && activeSelections.indexOf(itemsList[j].column_1) != -1) ? true : false });
				}
			}
			
			
			scope.targetColumn = targetModel.selectedColumn;
			scope.close = function() {
				scope.selectablesToSend = scope.selectables.reduce(function(result, element) {
					if(element.selected) result.push(element.name);
				  return result;
				}, []);
				$mdDialog.hide(scope.selectablesToSend);
		    };
		    scope.cancel = function(){
		    	$mdDialog.cancel();
		    }
		    
		    scope.isDisabled = function(p){
				return selectables && selectables.indexOf(p) == -1;
			}
		    
		    scope.selectAll = function(){
		    	scope.allSelected = !scope.allSelected;
		    	for(var s in scope.selectables){
		    		if(!scope.isDisabled(scope.selectables[s].name)) scope.selectables[s].selected = scope.allSelected;
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
					$scope.defaultValues = $scope.ngModel.settings.staticValues.split(",");
					applyDefaultValues = true;
					break;
				}

				if(applyDefaultValues){
					var item = {};
					item.aggregated=$scope.aggregated;
					item.columnName=$scope.ngModel.content.selectedColumn.aliasToShow;
					item.columnAlias=$scope.ngModel.content.selectedColumn.aliasToShow;
					item.ds=$scope.ngModel.dataset.label;
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
				}
			}else{
			    $scope.selectedValues = [];
			}
		}

	    $scope.clearParamSearch = function() {
			$scope.searchParamText = "";
		};

		$scope.toggleParameter = function(parVal) {
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
				var values;
				if($scope.ngModel.settings.modalityPresent=="LIST"){
					var index = $scope.selectedValues.indexOf(parVal);
					if (index > -1) {
						$scope.selectedValues.splice(index, 1);
					} else {
						$scope.selectedValues.push(parVal);
					}
					values = $scope.selectedValues;
				}else{
					values = parVal;
				}

				if(values.length>0){
					$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,angular.copy(values));
				} else {
					item.value=angular.copy(values);
					$rootScope.$broadcast('DELETE_SELECTION',item);
					$scope.deleteSelections(item);
				}
			} else { // singleValue
				if($scope.ngModel.settings.modalityPresent=="LIST"){
					if($scope.selectedValues[0] != parVal){
						$scope.selectedValues[0] = parVal;
						$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow, $scope.selectedValues[0]);
					} else {
						item.value=angular.copy($scope.selectedValues[0]);
						$rootScope.$broadcast('DELETE_SELECTION',item);
						$scope.deleteSelections(item);
					}
				}else{ // COMBOBOX
					if(parVal){
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
				$scope.cockpitModule_widgetSelection.getAssociations(true);
			}

			if(!reloadAss && reloadFilt.length!=0){
				$scope.cockpitModule_widgetSelection.refreshAllWidgetWhithSameDataset(reloadFilt);
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
		
//		$scope.modalityWatcher = $scope.$watch('model.settings.modalityValue',function(newValue,oldValue){
//			if(newValue == 'multiValue') $scope.model.settings.modalityPresent = 'LIST';
//		})

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

			$scope.model.content.columnSelectedOfDataset.length = 0;
			$scope.model.content.columnSelectedOfDataset.push($scope.model.content.selectedColumn);

			angular.copy($scope.model,originalModel);
			mdPanelRef.close();
			mdPanelRef.destroy();

			if(!scopeFather.ngModel.isNew){
				scopeFather.refreshWidget();
			}
			//$scope.modalityWatcher();

			$scope.$destroy();
			finishEdit.resolve();
		}

		$scope.cancelConfiguration=function(){
			$scope.modalityWatcher();
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