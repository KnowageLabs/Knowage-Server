(function () {
	angular.module('cockpitModule')
	.directive('cockpitSelectorConfigurator',function(cockpitModule_widgetServices,$mdDialog,$mdSidenav){

		return {
			templateUrl: baseScriptPath+ '/directives/cockpit-selector-configurator/templates/cockpitSelectorConfiguratorTemplate.jsp',
			controller: cockpitSelectorConfiguratorControllerFunction,
			compile: function (tElement, tAttrs, transclude) {
				return {
					pre: function preLink(scope, element, attrs, ctrl, transclud) {
					},
					post: function postLink(scope, element, attrs, ctrl, transclud) {
					}
				};
			}
		};
	});

	function cockpitSelectorConfiguratorControllerFunction($scope,$mdDialog,cockpitModule_datasetServices,$mdToast,cockpitModule_widgetConfigurator,sbiModule_restServices,sbiModule_translate,sbiModule_config,$mdSidenav,$q,cockpitModule_generalOptions){


		$scope.translate=sbiModule_translate;
		$scope.cockpitModule_generalOptions=cockpitModule_generalOptions;
		$scope.availableDatasets=cockpitModule_datasetServices.getAvaiableDatasets();

		$scope.lastId = -1;


		$scope.isSelectedColumnTemporal = function(){
			if($scope.model.content.selectedColumn && $scope.model.content.selectedColumn && $scope.model.content.selectedColumn.type){
				var type = $scope.model.content.selectedColumn.type.toLowerCase();
				var isTemporal = type.indexOf('date') > -1 || type.indexOf('timestamp') > -1;
				return isTemporal;
			}
			return false;
		}

		$scope.setSelectorType = function(type){
			$scope.model.settings.modalityValue = type;
		}
		
		if($scope.model.settings.defaultStartDate) $scope.model.settings.defaultStartDate = new Date($scope.model.settings.defaultStartDate);
		if($scope.model.settings.defaultEndDate) $scope.model.settings.defaultEndDate = new Date($scope.model.settings.defaultEndDate);
		$scope.setToDate = function(type){
			if(type == 'start') $scope.model.settings.defaultStartDate = new Date($scope.model.settings.defaultStartDate);
			if(type == 'end') $scope.model.settings.defaultEndDate = new Date($scope.model.settings.defaultEndDate);
		}

		$scope.showCircularcolumns = {value :false};
		$scope.modalityValue = [
			{value: "singleValue",name: $scope.translate.load('sbi.cockpit.widgets.selector.single.value'),temporalAvailable:true},
			{value :"multiValue",name: $scope.translate.load('sbi.cockpit.widgets.selector.multivalue'),temporalAvailable:true},
			{value :"dropdown",name: $scope.translate.load('sbi.cockpit.widgets.selector.selectinput'),temporalAvailable:false},
			{value :"multiDropdown",name: "Multiselection Dropdown",temporalAvailable:false}];
		$scope.modalityView = [
			{value: "vertical",name: $scope.translate.load('sbi.cockpit.widgets.selector.vertical'),icon:"fa fa-ellipsis-v"},
			{value :"horizontal",name: $scope.translate.load('sbi.cockpit.widgets.selector.horizontal'),icon:"fa fa-ellipsis-h"},
			{value :"grid",name: $scope.translate.load('sbi.cockpit.widgets.selector.grid'),icon:"fa fa-th"}
			];
		$scope.modalityPresent = [{value: "LIST",name: $scope.translate.load('sbi.cockpit.widgets.selector.list')},{value :"COMBOBOX",name: $scope.translate.load('sbi.cockpit.widgets.selector.combobox')}];
		$scope.defaultValues = [{value: "FIRST",name: "Main column's first item"},{value: "LAST",name: "Main columns's last item"},{value: "STATIC",name: "Static"}]

		$scope.resetValue = function(dsId, forceFlag){
			if($scope.model.dataset && $scope.model.dataset.dsId){
				$scope.lastId = $scope.model.dataset.dsId;
			}else{
				$scope.model.dataset = {};
			}

			if($scope.lastId==-1 || $scope.lastId!=dsId || forceFlag){
				$scope.showCircularcolumns = {value : true};
				$scope.safeApply();

				$scope.model.dataset.dsId = dsId;
				$scope.local = {};
				if($scope.model.dataset.dsId !=-1){
					angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.local);
					$scope.model.content.columnSelectedOfDataset  = [];
					for(var i=0;i<$scope.local.metadata.fieldsMeta.length;i++){
						var obj = $scope.local.metadata.fieldsMeta[i];
						obj["aggregationSelected"] = "SUM";
						obj["funcSummary"] = "SUM";
						obj["aliasToShow"] = obj.alias;
						$scope.model.content.columnSelectedOfDataset.push(obj);
					}
					$scope.lastId=$scope.model.dataset.dsId;
				}else{
					$scope.model.content.columnSelectedOfDataset = [];
				}

				for(i in $scope.model.content.columnSelectedOfDataset){
				    if($scope.model.content.columnSelectedOfDataset[i] && $scope.model.content.columnSelectedOfDataset[i].alias == $scope.model.content.selectedColumn && $scope.model.content.selectedColumn.alias){
				        $scope.model.content.columnSelectedOfDataset[i] = $scope.model.content.selectedColumn;
				        break;
				    }
				}

				$scope.showCircularcolumns ={value : false};
                $scope.safeApply();
			}
		}

		$scope.safeApply=function(){
			if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase !='$digest') {
				$scope.$apply();
			}
		}

		if($scope.model.dataset && $scope.model.dataset.dsId && $scope.model.dataset.dsId != -1){
            $scope.local = cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId);
            $scope.resetValue($scope.model.dataset.dsId, true);
        }

		$scope.$watch("model.settings.hideDisabled", function(newValue,oldValue){
			if(newValue){
				$scope.model.settings.enableAll = false;
			}
		})
	}
})();

