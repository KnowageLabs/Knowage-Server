(function() {
	var documentExecutionModule = angular.module('documentExecutionModule');

	documentExecutionModule.directive('documentParamenterElement', 
			['sbiModule_config',
			 function(sbiModule_config){
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.contextName 
				+ '/js/src/angular_1.4/tools/documentexecution/documentParamenterElement/documentParamenterElementTemplate.jsp',
			controller: documentParamenterElementCtrl,
			scope: {
				parameter: '=',
			}
		};
	}]);
	
	var documentParamenterElementCtrl = function($scope, sbiModule_config, sbiModule_translate, documentExecuteUtils, $mdDialog) {
		$scope.documentExecuteUtils = documentExecuteUtils;
		$scope.sbiModule_translate = sbiModule_translate;
		
		$scope.getTreeParameterValue = function(parameter) {
			
		};

		$scope.toggleCheckboxParameter = function(parameter, defaultParameter) {
			var tempNewParameterValue = [];
			for(var i = 0; i < parameter.defaultValues.length; i++) {
				var defaultValue = parameter.defaultValues[i];
				if(defaultValue.isSelected == true) {
					tempNewParameterValue.push(defaultValue.value);
				}
			}
			parameter.parameterValue = tempNewParameterValue;
		};
		
		$scope.popupLookupParameterDialog = function(parameter) {
			$mdDialog.show({
				$type: "confirm",
				clickOutsideToClose: false,
				theme: "knowage",
				openFrom: '#' + parameter.urlName,
				closeTo: '#' + parameter.urlName,
				templateUrl : sbiModule_config.contextName
					+ '/js/src/angular_1.4/tools/documentexecution/templates/popupLookupParameterDialogTemplate.htm',
				
				locals : {
					parameter: parameter,
					toggleCheckboxParameter: $scope.toggleCheckboxParameter,
					sbiModule_translate: $scope.sbiModule_translate,
				},
				controllerAs: "lookupParamCtrl",
				
				controller : function($mdDialog, parameter, toggleCheckboxParameter, sbiModule_translate) {
					var lookupParamCtrl = this;
					
					lookupParamCtrl.toggleCheckboxParameter = toggleCheckboxParameter;
					
					lookupParamCtrl.initialParameterState = {};
					
					angular.copy(parameter, lookupParamCtrl.initialParameterState);
					lookupParamCtrl.parameter = parameter;
					
					lookupParamCtrl.dialogTitle = sbiModule_translate.load("sbi.kpis.parameter") + ': ' + parameter.label;
					lookupParamCtrl.dialogCancelLabel = sbiModule_translate.load("sbi.browser.defaultRole.cancel");
					lookupParamCtrl.dialogSaveLabel = sbiModule_translate.load("sbi.browser.defaultRole.save");
					
					lookupParamCtrl.abort = function(){
						angular.copy(lookupParamCtrl.initialParameterState, lookupParamCtrl.parameter);
						$mdDialog.hide();
					};
					
					lookupParamCtrl.save = function(){
						$mdDialog.hide();
					};
				}
			});
		};
	};
	
})();