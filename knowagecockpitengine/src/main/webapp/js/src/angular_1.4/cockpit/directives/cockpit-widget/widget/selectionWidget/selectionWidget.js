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
.directive('cockpitSelectionWidget',function(cockpitModule_widgetServices,$mdDialog){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/selectionWidget/templates/selectionWidgetTemplate.html',
		   controller: cockpitSelectionWidgetControllerFunction,
		   compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    	element[0].classList.add("flex");
                    	element[0].classList.add("layout");
                    	element[0].style.overflow="auto"
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    	//init the widget
                    	element.ready(function () {
                    		scope.initWidget();
                        });



                    }
                };
		   	}
	   };
});

function cockpitSelectionWidgetControllerFunction($scope,$timeout,cockpitModule_widgetConfigurator,$mdPanel,cockpitModule_template,cockpitModule_datasetServices,$mdDialog,sbiModule_translate,$q,sbiModule_messaging,cockpitModule_documentServices,cockpitModule_widgetSelection,cockpitModule_properties,cockpitModule_templateServices){
	$scope.translate = sbiModule_translate;
	$scope.widgetIsInit=false;
	$scope.property={
		style:{}
	};

	$scope.selection = [];

	$scope.tmpSelection = [];
	angular.copy(cockpitModule_template.configuration.aggregations,$scope.tmpSelection);

	$scope.tmpFilters = {};
	angular.copy(cockpitModule_template.configuration.filters,$scope.tmpFilters);

	$scope.init=function(element,width,height){
		$scope.refreshWidget(null, 'init');
	};

	$scope.refresh=function(element,width,height,datasetRecords, nature){
		if(nature == 'init'){
			$timeout(function(){
				$scope.widgetIsInit=true;
				cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
			},500);
		}
	};

	$scope.filterForInitialSelection=function(obj){
		if(!cockpitModule_properties.EDIT_MODE){
			for(var i=0;i<cockpitModule_properties.STARTING_SELECTIONS.length;i++){
				if(angular.equals(cockpitModule_properties.STARTING_SELECTIONS[i],obj)){
					return true;
				}
			}
		}
		return false;
	}

	$scope.filterForInitialFilter=function(obj){
		if(!cockpitModule_properties.EDIT_MODE){
			for(var i=0;i<cockpitModule_properties.STARTING_FILTERS.length;i++){
				if(angular.equals(cockpitModule_properties.STARTING_FILTERS[i],obj)){
					return true;
				}
			}
		}
		return false;
	}
	
	if($scope.ngModel && $scope.ngModel.style && !$scope.ngModel.style.chips) $scope.ngModel.style.chips = {};
	
	$scope.getSelections=function(){
		$scope.selection = [];
		$scope.tmpSelection = [];
		angular.copy(cockpitModule_template.configuration.aggregations,$scope.tmpSelection);
		$scope.tmpFilters = {};
		angular.copy(cockpitModule_template.configuration.filters,$scope.tmpFilters);
		
		

		var dsIdsInSameSheet = cockpitModule_templateServices.getDatasetIdsInSameSheet($scope.ngModel.id);
		var dsLabelsInSameSheet = cockpitModule_datasetServices.getDatasetLabelsByIds(dsIdsInSameSheet);
		var associatedDsLabels = cockpitModule_templateServices.getAssociatedDatasetLabels(dsLabelsInSameSheet);

		if($scope.tmpSelection.length >0){
			for(var i=0;i<$scope.tmpSelection.length;i++){
				var selection = $scope.tmpSelection[i].selection;
				for(var key in selection){
					var string = key.split(".");
					var tmpValue;
					if(Object.prototype.toString.call( selection[key] ) === '[object Array]') {
						tmpValue = selection[key].join(', ');
					}else {
						tmpValue = selection[key];
					}
					var columnAlias = $scope.getColumnAlias(string[0], string[1]);
					var obj = {
						ds : string[0],
						columnName : string[1],
						columnAlias : columnAlias,
						value : tmpValue,
						aggregated:true
					};

					if((associatedDsLabels.indexOf(obj.ds) > -1 || dsLabelsInSameSheet.indexOf(obj.ds) > -1) && !$scope.filterForInitialSelection(obj)){
						$scope.selection.push(obj);
					}
				}
			}
		}

		for(var ds in $scope.tmpFilters){
			for(var col in $scope.tmpFilters[ds]){
				var tmpValue;
				if(Object.prototype.toString.call( $scope.tmpFilters[ds][col] ) === '[object Array]') {
					tmpValue = $scope.tmpFilters[ds][col].join(', ');
				}else {
					tmpValue = $scope.tmpFilters[ds][col];
				}
				var columnAlias = $scope.getColumnAlias(ds, col);
				var tmpObj={
					ds :ds,
					columnName : col,
					columnAlias : columnAlias,
					value : tmpValue,
					aggregated:false
				};

				if((associatedDsLabels.indexOf(tmpObj.ds) > -1 || dsLabelsInSameSheet.indexOf(tmpObj.ds) > -1) && !$scope.filterForInitialFilter(tmpObj)){
					$scope.selection.push(tmpObj);
				}
			}
		}
	}

	$scope.getRowStyle = function(even){
        var style = {};
        if($scope.ngModel.style && $scope.ngModel.style.row && $scope.ngModel.style.row.height) {
            style.height = $scope.ngModel.style.row.height;
            style['min-height'] = $scope.ngModel.style.row.height;
        }
        if($scope.ngModel.style && $scope.ngModel.style.alternateRows && $scope.ngModel.style.alternateRows.enabled){
            style['background-color'] = even ? $scope.ngModel.style.alternateRows.evenRowsColor : $scope.ngModel.style.alternateRows.oddRowsColor;
        }
        return style;
    };

	$scope.getColumnAlias = function(dsName, columnName){
		var columnAlias = columnName;
		for(var aliasIndex in cockpitModule_template.configuration.aliases){
			var alias = cockpitModule_template.configuration.aliases[aliasIndex]
			if(alias.dataset == dsName && alias.column == columnName){
				columnAlias = alias.alias;
			}
		}
		return columnAlias;
	}

	$scope.getSelections();

	if(!$scope.ngModel.style) $scope.ngModel.style = {};
	if(typeof($scope.ngModel.style.showColumn) == 'undefined') $scope.ngModel.style.showColumn = true;
	$scope.columnTableSelection = [
	{
		label: $scope.translate.load("sbi.cockpit.dataset"),
		name: "ds",
		hideTooltip: false,
		visible: $scope.ngModel.style.showDataset
	},{
		label: $scope.translate.load("sbi.cockpit.cross.column"),
		name: "columnAlias",
		hideTooltip: false,
		visible: $scope.ngModel.style.showColumn
  },{
	  label: $scope.translate.load("sbi.cockpit.core.selections.list.columnValues"),
	  name: "value",
	  hideTooltip: false,
	  visible: true
  }
    ];

	$scope.$watch('ngModel.style',function(newValue,oldValue){
		$scope.columnTableSelection[0].visible = newValue.showDataset;
		$scope.columnTableSelection[1].visible = newValue.showColumn;
	})

	$scope.actionsOfSelectionColumns = [
	    {
	    	icon:'fa fa-trash' ,
	    	action : function(item,event) {
	    		$scope.deleteSelection(item, true);
	    	}
	    }
    ];

	$scope.$on('DELETE_SELECTION',function(event, data){
		$scope.deleteSelection(data);
	});

	$scope.deleteSelection=function(item, saveConfiguration){
		cockpitModule_widgetSelection.setWidgetOfType("selection");
		cockpitModule_widgetSelection.setColumnName(item.columnName);

		if(item.aggregated){
			var key = item.ds + "." + item.columnName;

			for(var i=0;i<$scope.tmpSelection.length;i++){
				if($scope.tmpSelection[i].datasets.indexOf(item.ds) !=-1){
					var selection = $scope.tmpSelection[i].selection;
					if(selection){
						delete selection[key];
					}
				}
			}
		}else{
			if($scope.tmpFilters){
				if($scope.tmpFilters[item.ds]){
					delete $scope.tmpFilters[item.ds][item.columnName];

					if(Object.keys($scope.tmpFilters[item.ds]).length==0){
						delete $scope.tmpFilters[item.ds];
					}
				}
			}
		}

		var index=$scope.selection.indexOf(item);
		$scope.selection.splice(index,1);

		if(saveConfiguration){
			$scope.saveConfiguration();
		}
	}

	$scope.clearAllSelection = function(){
		cockpitModule_widgetSelection.setWidgetOfType("selection");
		if($scope.selection.length>0){
			while($scope.selection.length>0){
				$scope.deleteSelection($scope.selection[0], false);
			}
			$scope.saveConfiguration();
		}
	}

	$scope.cancelConfiguration=function(){
		$mdDialog.cancel();
	}

	$scope.saveConfiguration = function(){
	    cockpitModule_widgetSelection.updateSelections($scope.tmpSelection, $scope.tmpFilters);
	    $mdDialog.cancel();
	}

	// general widget event  'WIDGET_EVENT' without ID
	$scope.$on('WIDGET_EVENT',function(config,eventType,config){
		switch(eventType){
			case "UPDATE_FROM_DATASET_FILTER":
			case "UPDATE_FROM_SELECTION":
				$scope.getSelections();
				break;
			default:
		}
	});

	$scope.editWidget=function(index){
		var finishEdit=$q.defer();
		var config = {
			attachTo:  angular.element(document.body),
			locals: {finishEdit:finishEdit,model:$scope.ngModel},
			controller: function($scope,finishEdit,sbiModule_translate,model,mdPanelRef,$mdToast){
				$scope.translate=sbiModule_translate;
				
				$scope.localModel = {};
				angular.copy(model,$scope.localModel);
				$scope.colorPickerPropertyEvenOddRows={format:'rgb', placeholder:sbiModule_translate.load('sbi.cockpit.color.select'),disabled:!$scope.localModel.style || !$scope.localModel.style.alternateRows || !$scope.localModel.style.alternateRows.enabled};
				$scope.changeAlternatedRows = function(){
					$scope.colorPickerPropertyEvenOddRows.disabled = !$scope.localModel.style.alternateRows.enabled;
				}
				
				$scope.colorPickerPropertyChips = {format:'rgb', placeholder:sbiModule_translate.load('sbi.cockpit.color.select'),disabled:!$scope.localModel.style.chips || !$scope.localModel.style.chips.enabled};
				$scope.toggleChips = function(){
					$scope.colorPickerPropertyChips.disabled = $scope.localModel.style.chips.enabled ? false : true;
				}
				
				
				$scope.saveConfiguration=function(){
					angular.copy($scope.localModel,model);
					mdPanelRef.close();

					$scope.$destroy();
					finishEdit.resolve();

				}

				$scope.cancelConfiguration=function(){
					mdPanelRef.close();
					$scope.$destroy();
					finishEdit.reject();
				}
		    },
			disableParentScroll: true,
			templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/selectionWidget/templates/selectionWidgetEditPropertyTemplate.html',
			position: $mdPanel.newPanelPosition().absolute().center(),
			fullscreen :true,
			hasBackdrop: true,
			clickOutsideToClose: false,
			escapeToClose: false,
			focusOnOpen: true,
			preserveScope: true
		};

		$mdPanel.open(config);
		return finishEdit.promise;
	}
};


//this function register the widget in the cockpitModule_widgetConfigurator factory
addWidgetFunctionality("selection",{'initialDimension':{'width':5, 'height':5},'updateble':true,'cliccable':true});

})();