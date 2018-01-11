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
	.directive('cockpitSelectorWidget',function(cockpitModule_widgetServices,$mdDialog){
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
			accessibility_preferences){

		$scope.accessibilityModeEnabled = accessibility_preferences.accessibilityModeEnabled;
		if ($scope.ngModel && $scope.ngModel.dataset && $scope.ngModel.dataset.dsId){
			$scope.ngModel.dataset.isRealtime = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId).isRealtime;
		}
		$scope.parameter = "";
		$scope.selectedTab = {'tab' : 0};
		$scope.widgetIsInit=false;
		$scope.totalCount = 0;
		$scope.translate = sbiModule_translate;
		$scope.datasetRecords = {};
	
		var scope = $scope;
	
		$scope.realTimeSelections = cockpitModule_widgetServices.realtimeSelections;
		//set a watcher on a variable that can contains the associative selections for realtime dataset
		var realtimeSelectionsWatcher = $scope.$watchCollection('realTimeSelections',function(newValue,oldValue,scope){
			if(scope.ngModel && scope.ngModel.dataset && scope.ngModel.dataset.dsId){
				var dataset = cockpitModule_datasetServices.getDatasetById(scope.ngModel.dataset.dsId);
				if(cockpitModule_properties.DS_IN_CACHE.indexOf(dataset.label)==-1 ){
	                cockpitModule_properties.DS_IN_CACHE.push(dataset.label);
	            }
				if(newValue != oldValue && newValue.length > 0){
					scope.itemList = scope.filterDataset(scope.itemList,scope.reformatSelections(newValue));
				}else{
					angular.copy(scope.savedRows, scope.itemList);
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
					locals: {finishEdit: finishEdit, originalModel: $scope.ngModel, getMetadata: $scope.getMetadata, scopeFather: $scope},
	
			};
			$mdPanel.open(config);
			return finishEdit.promise;
		}
		
		$scope.init=function(element,width,height){
			$scope.refreshWidget();
			$timeout(function(){
				$scope.widgetIsInit=true;
			},500);
	
		}
		
		$scope.refresh=function(element,width,height, datasetRecords,nature){
			if(nature == 'gridster-resized' || nature == 'fullExpand' || nature == 'resize'){
				return;
			}
			$scope.columnsToShow = [];
			$scope.columnToshowinIndex = [];
			$scope.datasetRecords = datasetRecords;
		}
		
		$scope.toggleCheckboxParameter = function(parVal , parameter) {
			if (typeof parameter.parameterValue == 'undefined'){
				parameter.parameterValue= [];
			}
			var idx = parameter.parameterValue.indexOf(parVal);
	        if (idx > -1) {
	        	// in case the element is removed recalculate description 
	        	parameter.parameterValue.splice(idx, 1);
	        	//recalculate descriptions
				addParameterValueDescription(parameter);

	        }
	        else {
	        	// in case the elemnt is addedd can add description
	        	parameter.parameterValue.push(parVal);
	        	if(parameter.parameterDescription == undefined) parameter.parameterDescription = ""; 
	        	if(parameter.parameterDescription==""){
	        		parameter.parameterDescription = parDesc;	        		
	        	}
	        	else{
	        		parameter.parameterDescription += ";"+parDesc;	
	        	}
	        } 
		};
		
		$scope.toggleRadioParameter = function(parVal ) {		
			$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,parVal);
		};
	
		$scope.toggleComboParameter = function(parameter) {
			$scope.doSelection($scope.ngModel.content.selectedColumn.aliasToShow,parameter);		
		}
		
		$scope.checkboxParameterExists = function (parVal,parameter) {
			if( parameter.parameterValue==undefined ||  parameter.parameterValue==null){
				return false;
			}
	        return parameter.parameterValue.indexOf(parVal) > -1;
	      };
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
			if($scope.model.content.columnSelectedOfDataset == undefined || $scope.model.content.columnSelectedOfDataset.length==0){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.nocolumns'));
			}
			finishEdit.resolve();
		}
		
		$scope.cancelConfiguration=function(){
			mdPanelRef.close();
			mdPanelRef.destroy();
			$scope.$destroy();
			finishEdit.reject();
		}
	};
	
	addWidgetFunctionality("selector",{'initialDimension':{'width':20, 'height':20},'updateble':true,'cliccable':true});

})();