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

		
		if(!$scope.model.settings.modalityValue){
			$scope.model.settings.modalityValue="singleValue";
		}
		if(!$scope.model.settings.modalityView){
			$scope.model.settings.modalityView="vertical";
		}
		if(!$scope.model.settings.modalityPresent){
			$scope.model.settings.modalityPresent="LIST";
		}
		if(!$scope.model.settings.defaultValue){
			$scope.model.settings.defaultValue="";
		}
		
		$scope.lastId = -1;

		if($scope.model.dataset && $scope.model.dataset.dsId){
			$scope.local = cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId);
		}

		$scope.$watch('model.content.selectedColumn',function(newValue,oldValue){
			if($scope.model.content.columnSelectedOfDataset){
				$scope.model.content.columnSelectedOfDataset.length = 0;
				$scope.model.content.columnSelectedOfDataset.push(newValue);
			}
		},true)
		
		$scope.showCircularcolumns = {value :false};
		$scope.modalityValue = [{value: "singleValue",name: $scope.translate.load('sbi.cockpit.widgets.selector.single.value')},{value :"multiValue",name: $scope.translate.load('sbi.cockpit.widgets.selector.multivalue')}];
		$scope.modalityView = [{value: "vertical",name: $scope.translate.load('sbi.cockpit.widgets.selector.vertical')},{value :"horizontal",name: $scope.translate.load('sbi.cockpit.widgets.selector.horizontal')}];
		$scope.modalityPresent = [{value: "LIST",name: $scope.translate.load('sbi.cockpit.widgets.selector.list')},{value :"COMBOBOX",name: $scope.translate.load('sbi.cockpit.widgets.selector.combobox')}];
		$scope.defaultValues = [{value: "FIRST",name: "Main column's first item"},{value: "LAST",name: "Main columns's last item"}]

		$scope.resetValue = function(dsId){
			if($scope.model.dataset && $scope.model.dataset.dsId){
				$scope.lastId = $scope.model.dataset.dsId;
			}else{
				$scope.model.dataset = {};
			}

			if($scope.lastId==-1 || $scope.lastId!=dsId){
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
					$scope.showCircularcolumns ={value : false};
					$scope.safeApply();
				}else{
					$scope.model.content.columnSelectedOfDataset = [];
				}
			}
			$scope.copyColumnSelectedOfDataset =angular.copy($scope.model.content.columnSelectedOfDataset);
			$scope.model.content.copyColumnSelectedOfDataset = $scope.copyColumnSelectedOfDataset;
		}

		$scope.safeApply=function(){
			if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase !='$digest') {
				$scope.$apply();
			}
		}

	}
})();

