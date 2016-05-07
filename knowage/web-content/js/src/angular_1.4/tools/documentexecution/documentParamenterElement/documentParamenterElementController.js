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
			$scope, sbiModule_config, sbiModule_restServices, sbiModule_translate, 
			execProperties, documentExecuteServices, $mdDialog, $mdMedia,execProperties) {
		
		$scope.documentExecuteServices = documentExecuteServices;
		$scope.sbiModule_translate = sbiModule_translate;
		
		$scope.getTreeParameterValue = function(innerNode) {
			if (typeof innerNode === 'undefined'){
				execProperties.hideProgressCircular.status=false;
			}
			
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
		
		
		$scope.checkboxParameterExists = function (parVal,parameter) {
			if( parameter.parameterValue==undefined ||  parameter.parameterValue==null){
				return false;
			}
	        return parameter.parameterValue.indexOf(parVal) > -1;
	      };
	      
		$scope.toggleCheckboxParameter = function(parVal,parameter) {
			if (typeof parameter.parameterValue == 'undefined'){
				parameter.parameterValue= [];
			}
			var idx = parameter.parameterValue.indexOf(parVal);
	        if (idx > -1) {
	        	parameter.parameterValue.splice(idx, 1);
	        }
	        else {
	        	parameter.parameterValue.push(parVal);
	        } 
		};
		
		$scope.popupLookupParameterDialog = function(parameter) {
			
			execProperties.hideProgressCircular.status=false;
			parameter.PARAMETERS=documentExecuteServices.buildStringParameters(execProperties.parametersData.documentParameters);
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
		
		
		$scope.showDefaultValueAreValid = function(parameter) {
			if(parameter.defaultValues && parameter.defaultValues.length>0 && parameter.defaultValues[0].error){
					return false;
				}else{
					return true;
				}
		};
		
		
		$scope.popupParameterDialog = function(parameter, templateUrl) {
			$mdDialog.show({
				$type: "confirm",
				clickOutsideToClose: false,
				theme: "knowage",
				openFrom: '#' + parameter.urlName,
				closeTo: '#' + parameter.urlName,
				templateUrl : templateUrl,
				onComplete : function() {
								execProperties.hideProgressCircular.status=true;
								},
				locals : {
					parameter: parameter,
					toggleCheckboxParameter: $scope.toggleCheckboxParameter,
					checkboxParameterExists: $scope.checkboxParameterExists,
					sbiModule_translate: $scope.sbiModule_translate,
				},
				
				controllerAs: "paramDialogCtrl",
				
				controller : function($mdDialog, parameter, toggleCheckboxParameter, checkboxParameterExists,sbiModule_translate) {
					var paramDialogCtrl = this;
					
					paramDialogCtrl.toggleCheckboxParameter = toggleCheckboxParameter;
					paramDialogCtrl.checkboxParameterExists = checkboxParameterExists;
					
					paramDialogCtrl.initialParameterState = parameter;
					
					paramDialogCtrl.tempParameter = {};
					angular.copy(paramDialogCtrl.initialParameterState, paramDialogCtrl.tempParameter);
					
					paramDialogCtrl.dialogTitle = sbiModule_translate.load("sbi.kpis.parameter") + ': ' + parameter.label;
					paramDialogCtrl.dialogCancelLabel = sbiModule_translate.load("sbi.browser.defaultRole.cancel");
					paramDialogCtrl.dialogSaveLabel = sbiModule_translate.load("sbi.browser.defaultRole.save");
					
					paramDialogCtrl.setTreeParameterValue = function(node) {			
						if(!paramDialogCtrl.tempParameter.multivalue) {
//							paramDialogCtrl.tempParameter.parameterValue = node;
							paramDialogCtrl.tempParameter.parameterValue = node.value;
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
						
						// Lov parameters NON tree
						if(paramDialogCtrl.tempParameter.defaultValues && paramDialogCtrl.tempParameter.defaultValuesMeta) {
							if(paramDialogCtrl.tempParameter.multivalue) {
								var parameterValueArray = [];
	
								for(var i = 0; i < paramDialogCtrl.selectedTableItems.length; i++) {
									var selectedTableItem = paramDialogCtrl.selectedTableItems[i];
	
									parameterValueArray.push(selectedTableItem.value);
								}
	
								paramDialogCtrl.tempParameter.parameterValue = parameterValueArray;
							} else {
								paramDialogCtrl.tempParameter.parameterValue = paramDialogCtrl.selectedTableItems.value;
							}
						}
						
						angular.copy(paramDialogCtrl.tempParameter, paramDialogCtrl.initialParameterState);
						
						if(paramDialogCtrl.initialParameterState.selectionType == 'TREE'){
							documentExecuteServices.setParameterValueResult(paramDialogCtrl.initialParameterState);
						}
						
						$mdDialog.hide();
					};
					
					paramDialogCtrl.isFolderFn = function(node) {
						return !!(
								!node.expanded
								&& (!node.leaf || node.leaf == false) 
//								&& node.children !== undefined 
//								&& (node.children.length > 0)
						);
					};
					
					paramDialogCtrl.isOpenFolderFn = function(node) {
						return !!(
								node.expanded
								&& (!node.leaf || node.leaf == false) 
						);
					};
					
					paramDialogCtrl.isDocumentFn = function(node) {
						return !!(
								node.leaf
								&& !node.children 
						);
					};
					
					paramDialogCtrl.showNodeCheckBoxFn = function(node) {
						var param = paramDialogCtrl.initialParameterState;
						return !!(
							param.multivalue == true
								&&(param.allowInternalNodeSelection
									|| (!param.allowInternalNodeSelection 
										&& node.leaf == true && !node.children)
								)
							);
					};
					
					// Lov parameters NON tree
					if(paramDialogCtrl.tempParameter.defaultValues && paramDialogCtrl.tempParameter.defaultValuesMeta) {
						paramDialogCtrl.tableColumns = [];
						for(var i = 0 ; i < paramDialogCtrl.tempParameter.defaultValuesMeta.length; i++) {
							var columnName = paramDialogCtrl.tempParameter.defaultValuesMeta[i];
							
							paramDialogCtrl.tableColumns.push(columnName.toUpperCase());
						};
						// defaultValues are filtered occording to dataDependencies
						var _dataDependencies = paramDialogCtrl.tempParameter.dataDependencies;
						paramDialogCtrl.tableData = [];
						if(_dataDependencies){
							var _defaultValues = paramDialogCtrl.tempParameter.defaultValues;
							var _urlName = paramDialogCtrl.tempParameter.urlName;
							for(var rowIndex in _defaultValues){
								var item = _defaultValues[rowIndex];
								var toKeep = true;
								for(var depIndex in _dataDependencies){
									var currDep = _dataDependencies[depIndex];
									if(currDep.parameterToChangeUrlName == _urlName){
										var filterParam = paramDialogCtrl.tempParameter.PARAMETERS[currDep.objParFatherUrlName];
										if(Array.isArray(filterParam)){
											var testMultiCond = false;
											for(var parIndex in filterParam){
												testMultiCond = testMultiCond || testCondition(item[currDep.filterColumn.toUpperCase()], currDep.filterOperation, filterParam[parIndex]);
											}
											toKeep = testMultiCond;
										}else if(!testCondition(item[currDep.filterColumn.toUpperCase()], currDep.filterOperation, filterParam)){
											toKeep = false;
											break;
										}
									}
								}
								if(toKeep){
									paramDialogCtrl.tableData.push(item);
								}
							}
						}else{
							paramDialogCtrl.tableData = paramDialogCtrl.tempParameter.defaultValues;
						}
						
						
						paramDialogCtrl.initSelectedTableItems = function() {
							var isMultivalue = paramDialogCtrl.tempParameter.multivalue;
							var defaultValues = paramDialogCtrl.tempParameter.defaultValues;
							
							if(paramDialogCtrl.tempParameter.parameterValue 
									&& paramDialogCtrl.tempParameter.parameterValue != null) {
								
								var parameterValue = paramDialogCtrl.tempParameter.parameterValue;
								
								var selectedTableItemsArray = [];
								
								for (var i = 0; i < defaultValues.length; i++) {
									var defaultValue = defaultValues[i];
									
									if(isMultivalue) {
										for (var j = 0; j < parameterValue.length; j++) {
											var parameterValueItem = parameterValue[j];
											
											if(parameterValueItem == defaultValue.value) {
												selectedTableItemsArray.push(defaultValue);
												break;
											}
										}
									} else {
										if(parameterValue == defaultValue.value) {
											return defaultValue;
										}
									}
								}
								
								if(isMultivalue) {
									return selectedTableItemsArray;
								}
							} else {
								return isMultivalue? [] : {};
							}
						};
						
						paramDialogCtrl.selectedTableItems = paramDialogCtrl.initSelectedTableItems();
					}
				}
			});
		};
		
		$scope.popupMapParameterDialog = function(parameter) {
			var valueData = '';
			
			if(parameter.parameterValue && parameter.parameterValue.length > 0) {
				var parameterValue;
				if(parameter.multivalue) {
					parameterValue = parameter.parameterValue.join("','");
				} else {
					parameterValue = parameter.parameterValue;
				}
				valueData = '&SELECTEDPROPDATA=' + "'" + parameterValue + "'";
			}
			
			var mapFilterSrc = 
				sbiModule_config.contextName + '/restful-services/publish?PUBLISHER=' 
					+ '/WEB-INF/jsp/behaviouralmodel/analyticaldriver/mapFilter/geoMapFilter.jsp?'
					+ 'SELECTEDLAYER=' + parameter.selectedLayer
					+ '&SELECTEDLAYERPROP=' + parameter.selectedLayerProp
					+ '&MULTIVALUE=' + parameter.multivalue
					+ valueData;
			
			$mdDialog.show({
				clickOutsideToClose: false,
				theme: "knowage",
				openFrom: '#' + parameter.urlName,
				closeTo: '#' + parameter.urlName,
				template:
					'<md-dialog aria-label="Map parameter" style="height:95%; width:95%; max-width: 100%; max-height: 100%;" ng-cloak>'
						+ '<md-toolbar layout="row">'
							+ '<div class="md-toolbar-tools" flex layout-align="center center">'
						      	+ '<h2 class="md-flex">{{parameter.label}}</h2>'
						     	+ '<span flex></span>'
						      	+ '<md-button title="Close" aria-label="Close" class="toolbar-button-custom" ng-click="close()">'
						      		+ '{{sbiModule_translate.load("sbi.general.close")}}' 
								+ '</md-button>'
							+ '</div>'
						+ '</md-toolbar>'
						+ '<md-dialog-content flex layout="column" class="dialogFrameContent" >'
							+ '<iframe flex class="noBorder" ng-src="{{iframeUrl}}"></iframe>'
						+ '</md-dialog-content>' 
					+'</md-dialog>',
					
				clickOutsideToClose: false,
				
				controller : function($scope, $mdDialog, sbiModule_translate) {
					$scope.sbiModule_translate = sbiModule_translate;
					
					$scope.parameter = parameter;
					$scope.selectedFeatures = [];

					$scope.iframeUrl = mapFilterSrc;
					
					$scope.close = function() {
						$mdDialog.hide();
						
						$scope.parameter.parameterValue = ($scope.parameter.multivalue)?
								$scope.selectedFeatures : $scope.selectedFeatures[0] ;
					};
					
					$scope.updateSelectedFeatures = function(dataToReturn) {
						$scope.selectedFeatures = dataToReturn;
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
		
		var testCondition = function(fieldA, condition, fieldB){
			var ret = false;
			try{
				switch (condition){
					case 'start':
						ret = fieldA.startsWith(fieldB);
						break;
					case 'end':
						ret = fieldA.endssWith(fieldB);
						break;
					case 'contains':
						ret = fieldA.indexOf(fieldB)>-1;
						break;
					case 'equal':
						ret = fieldA == fieldB;
						break;
					case 'less':
						ret = fieldA < fieldB;
						break;
					case 'lessequal':
						ret = fieldA <= fieldB;
						break;
					case 'greater':
						ret = fieldA > fieldB;
						break;
					case 'greaterequal':
						ret = fieldA >= fieldB;
						break;
				}
			}catch(e){
				console.error(e.message, e);
			}
			return ret;
		}
	};
})();