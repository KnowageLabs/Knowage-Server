(function() {
	var documentExecutionModule = angular.module('documentExecutionModule');

	documentExecutionModule.directive('documentParamenterElement', 
			['sbiModule_config',
			 function(sbiModule_config) {
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
	
	var documentParamenterElementCtrl = function(
	$scope, sbiModule_config,sbiModule_restServices,
	 sbiModule_translate, execProperties, documentExecuteServices,
	 $mdDialog) {
		
		
		$scope.documentExecuteServices = documentExecuteServices;
		$scope.sbiModule_translate = sbiModule_translate;
		
		$scope.getTreeParameterValue = function(innerNode) {
			var treeLovNode = (innerNode != undefined && innerNode != null)? innerNode.id : 'lovroot';
			
			var templateUrl = sbiModule_config.contextName
				+ '/js/src/angular_1.4/tools/documentexecution/templates/popupTreeParameterDialogTemplate.jsp';
			
//			var params = 
//				'label=' + execProperties.executionInstance.OBJECT_LABEL
//				+ '&role=' + execProperties.selectedRole.name
//				+ '&biparameterId=' + $scope.parameter.urlName
//				+ '&mode=' + 'COMPLETE'
//				+ '&treeLovNode=' + treeLovNode
//			;
			
			
			var params = {};
			params.label = execProperties.executionInstance.OBJECT_LABEL;
			params.role=execProperties.selectedRole.name;
			params.biparameterId=$scope.parameter.urlName;
			params.mode='complete';
			params.treeLovNode=treeLovNode;
			params.PARAMETERS=documentExecuteServices.buildStringParameters(execProperties.parametersData.documentParameters);
						
			
			if(!$scope.parameter.children || $scope.parameter.children.length == 0) {
				$scope.parameter.children = $scope.parameter.children || [];
				$scope.parameter.innerValuesMap = {};
				
//				treeLovNode = 'lovroot';
					
				sbiModule_restServices.post("1.0/documentexecution", "parametervalues", params)
				.success(function(response, status, headers, config) {
					console.log('parametervalues response OK -> ', response);
					
					angular.copy(response.filterValues, $scope.parameter.children);
//					$scope.updateAddToParameterInnerValuesMap($scope.parameter, $scope.parameter.children);
					
					$scope.popupParameterDialog($scope.parameter, templateUrl);
				})
				.error(function(response, status, headers, config) {
					console.log('parametervalues response ERROR -> ', response);
					sbiModule_messaging.showErrorMessage(response.errors[0].message, 'Error');
				});
				
			} else if(innerNode != undefined && innerNode != null) {
	
				if(!innerNode.children || innerNode.children.length == 0) {
					innerNode.children = innerNode.children || [];
					
					sbiModule_restServices.post("1.0/documentexecution", "parametervalues", params)
					.success(function(response, status, headers, config) {
						console.log('parametervalues response OK -> ', response);
						
						angular.copy(response.filterValues, innerNode.children);
//						$scope.updateAddToParameterInnerValuesMap($scope.parameter, innerNode.children);
					})
					.error(function(response, status, headers, config) {
						console.log('parametervalues response ERROR -> ', response);
						sbiModule_messaging.showErrorMessage(response.errors[0].message, 'Error');
					});
				}
			} else {
				$scope.popupParameterDialog($scope.parameter, templateUrl);
			}
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
			var templateUrl = sbiModule_config.contextName
				+ '/js/src/angular_1.4/tools/documentexecution/templates/popupLookupParameterDialogTemplate.htm';
			
			$scope.popupParameterDialog(parameter, templateUrl);
		};
		
		$scope.showRequiredFieldMessage = function(parameter) {
			return (
				parameter.mandatory 
				&& (
						!parameter.parameterValue
						|| (Array.isArray(parameter.parameterValue) && parameter.parameterValue.length == 0) 
						|| parameter.parameterValue == '')
				) == true;
		};	
		
		$scope.popupParameterDialog = function(parameter, templateUrl) {
			$mdDialog.show({
				$type: "confirm",
				clickOutsideToClose: false,
				theme: "knowage",
				openFrom: '#' + parameter.urlName,
				closeTo: '#' + parameter.urlName,
				templateUrl : templateUrl,
				
				locals : {
					parameter: parameter,
					toggleCheckboxParameter: $scope.toggleCheckboxParameter,
					sbiModule_translate: $scope.sbiModule_translate,
				},
				controllerAs: "paramDialogCtrl",
				
				controller : function($mdDialog, parameter, toggleCheckboxParameter, sbiModule_translate) {
					var paramDialogCtrl = this;
					
					paramDialogCtrl.toggleCheckboxParameter = toggleCheckboxParameter;
					
					paramDialogCtrl.initialParameterState = parameter;
					
					paramDialogCtrl.tempParameter = {};
					angular.copy(parameter, paramDialogCtrl.tempParameter);
					
					paramDialogCtrl.dialogTitle = sbiModule_translate.load("sbi.kpis.parameter") + ': ' + parameter.label;
					paramDialogCtrl.dialogCancelLabel = sbiModule_translate.load("sbi.browser.defaultRole.cancel");
					paramDialogCtrl.dialogSaveLabel = sbiModule_translate.load("sbi.browser.defaultRole.save");
					
					paramDialogCtrl.setTreeParameterValue = function(node) {			
						if(!paramDialogCtrl.tempParameter.multivalue) {
							paramDialogCtrl.tempParameter.parameterValue = node;
//							angular.copy(node, paramDialogCtrl.tempParameter.parameterValue);
						}
						
						// in case the node is not a leaf the rest service is invoked in order
						// to retrieve sub node items
						if(!node.leaf && (!node.children || node.children.length == 0)) {
							$scope.getTreeParameterValue(node);
						}
					};
					
					paramDialogCtrl.abort = function() {
						$mdDialog.hide();
					};
					
					paramDialogCtrl.save = function() {
						angular.copy(paramDialogCtrl.tempParameter, paramDialogCtrl.initialParameterState);
//						paramDialogCtrl.initialParameterState = paramDialogCtrl.tempParameter;
						$mdDialog.hide();
					};
					
					paramDialogCtrl.isFolderFn = function(node) {
						return(
								!node.expanded
								&& (!node.leaf || node.leaf == false) 
//								&& node.children !== undefined 
//								&& (node.children.length > 0)
						);
					};
					
					paramDialogCtrl.isOpenFolderFn = function(node) {
						return(
								node.expanded
								&& (!node.leaf || node.leaf == false) 
//								&& node.children !== undefined 
//								&& (node.children.length > 0)
						);
						
					};
					
					paramDialogCtrl.isDocumentFn = function(node) {
						return(
								node.leaf
//								node.expanded
//								&& (!node.leaf || node.leaf == false) 
								&& !node.children 
//								&& (node.children.length > 0)
						);
					};
					
					
				}
			});
		};
		
		$scope.updateAddToParameterInnerValuesMap = function(parameter, parameterValues) {
			if(!parameter.innerValuesMap) {
				parameter.innerValuesMap = {};
			}
			
			for(var i = 0; i < parameterValues.length; i++) {
				var parameterValue = parameterValues[i];
				var parameterValueId = parameterValue.id;
				
				if(!parameter.innerValuesMap[parameterValueId]) {
					parameter.innerValuesMap[parameterValueId] = parameterValue;
				}
			}
		}
	};
})();