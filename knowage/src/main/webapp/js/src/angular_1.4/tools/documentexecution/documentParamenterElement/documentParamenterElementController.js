/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
	var documentExecutionModule = angular.module('driversExecutionModule');

	documentExecutionModule.directive('documentParamenterElement',
			['sbiModule_config',
			 function(sbiModule_config) {
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.dynamicResourcesBasePath
				+ '/angular_1.4/tools/documentexecution/documentParamenterElement/documentParamenterElementTemplate.jsp',
			controller: documentParamenterElementCtrl,
			scope: {
				parameter: '=',
				execproperties: '=',
				datasetSettings: '=?',
			}
		};
	}]);

	var documentParamenterElementCtrl = function(
			$scope, sbiModule_config, sbiModule_restServices, sbiModule_translate,
			 $mdDialog, $mdMedia,$filter,sbiModule_dateServices, sbiModule_user,
			sbiModule_messaging, sbiModule_i18n,driversExecutionService) {

		$scope.translate = sbiModule_translate;
		//$scope.parameter.showMapDriver = sbiModule_user.functionalities.indexOf("MapDriverManagement")>-1;
		$scope.sbiModule_translate = sbiModule_translate;
		$scope.sbiModule_messaging = sbiModule_messaging;
		$scope.i18n = sbiModule_i18n;
		$scope.execProperties = $scope.execproperties
		$scope.driversExecutionService = driversExecutionService;
		var initalize = function(){
			if($scope.execProperties.currentView && $scope.execProperties.currentView.status == 'DOCUMENT'){
				$scope.executionPath = "1.0/documentexecution";
				$scope.valuesPath = "parametervalues";
				$scope.executionParameters = "1.0/documentExeParameters";
				$scope.parametersPath = "getParameters";
			}else{
				adaptExecutionProperties();

				$scope.executionPath = "1.0/businessModelOpening";
				$scope.valuesPath = "parametervalues";
				$scope.executionParameters = "1.0/businessModelOpening";
				$scope.parametersPath = "getParameters";
			}

		}

		$scope.isEmpty = function(obj) {
			for(var prop in obj) {
				if(obj.hasOwnProperty(prop))
					return false;
			}
			return JSON.stringify(obj) === JSON.stringify({});
		}

		$scope.getTreeParameterValue = function(innerNode) {
			if (typeof innerNode === 'undefined'){
				$scope.execProperties.hideProgressCircular.status=false;
			}

			var treeLovNode = (innerNode != undefined && innerNode != null)? innerNode.id : 'lovroot';
			var templateUrl = sbiModule_config.dynamicResourcesBasePath
				+ '/angular_1.4/tools/documentexecution/templates/popupTreeParameterDialogTemplate.jsp';

			var params = {};
			if($scope.execProperties.currentView && $scope.execProperties.currentView.status == 'DOCUMENT'){
				params.label = $scope.execProperties.executionInstance.OBJECT_LABEL;
			} else {
				params.name = $scope.execProperties.executionInstance.OBJECT_NAME;
			}
			params.role=$scope.execProperties.selectedRole.name;
			params.parameterId=$scope.parameter.urlName;
			params.mode='complete';
			params.treeLovNode=treeLovNode;
			params.PARAMETERS=driversExecutionService.buildStringParameters($scope.execProperties.parametersData.documentParameters);

			if($scope.parameter.children && !innerNode) {
				$scope.parameter.children = [];
			}

			if(!$scope.parameter.children || $scope.parameter.children.length == 0) {
				$scope.parameter.children = $scope.parameter.children || [];
				$scope.parameter.innerValuesMap = {};

//				treeLovNode = 'lovroot';

				if(sbiModule_config.contextName == "/knowagecockpitengine") {
					sbiModule_restServices.restToRootProject();
				}
				sbiModule_restServices.post($scope.executionPath,$scope.valuesPath , params)
				.success(function(response, status, headers, config) {
					console.log('parametervalues response OK -> ', response);

					angular.copy(response.filterValues, $scope.parameter.children);
					//check parameters selected field
					if($scope.parameter.parameterValue && $scope.parameter.parameterValue.length>0){
						for(var z=0;z<$scope.parameter.children.length;z++){
							if($scope.parameter.parameterValue.indexOf($scope.parameter.children[z].value)!==-1){
								$scope.parameter.children[z].checked= true;
							}
						}
					}

					$scope.popupParameterDialog($scope.parameter, templateUrl);

				})
				.error(function(response, status, headers, config) {
					console.log('parametervalues response ERROR -> ', response);
					sbiModule_messaging.showErrorMessage(response.errors[0].message, 'Error');
				});

			} else if(innerNode != undefined && innerNode != null) {

				if(!innerNode.children || innerNode.children.length == 0) {
					innerNode.children = innerNode.children || [];

					sbiModule_restServices.post($scope.executionPath,$scope.valuesPath, params)
					.success(function(response, status, headers, config) {
						console.log('parametervalues response OK -> ', response);
						angular.copy(response.filterValues, innerNode.children);
						//check parameters selected field
						for(var z=0;z<innerNode.children.length;z++){
							if(innerNode.checked==true || ($scope.parameter.parameterValue!=undefined && $scope.parameter.parameterValue.indexOf(innerNode.children[z].value)!==-1 )){
								innerNode.children[z].checked= true;
							}
						}

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




		var addParameterValueDescription = function(parameter) {

			// if parameter description not present get it from default value
			if(parameter.parameterValue != undefined){
				var parDescription = null;

				var valuesList = null;
				// if parameterValue is string instead of list create one element list
				if(typeof parameter.parameterValue === 'string'){
					valuesList=[];
					valuesList.push(parameter.parameterValue);
				}
				else{
					valuesList = parameter.parameterValue;
				}

				for(var z = 0; z < valuesList.length; z++) {
					var parVal =valuesList[z];
					//check if parval
					var found = false;
					for(var z2 = 0; parameter.defaultValues && z2 < parameter.defaultValues.length && !found; z2++) {
						var defvalue =parameter.defaultValues[z2];
						if(parVal == defvalue.value){
							if(parDescription != null){
								parDescription += ";";
							}
							else{
								parDescription="";
							}
							parDescription += defvalue.label;
							found = true;

						}
					}

				}
				if(parDescription != null){
					parameter.parameterDescription = parDescription;
				}
			}
		};


		$scope.toggleCheckboxParameter = function(parVal ,parDesc, parameter) {
			if (typeof parameter.parameterValue == 'undefined'){
				parameter.parameterValue= [];
			}
			var idx = parameter.parameterValue.indexOf(parVal);
	        if (idx > -1) {
	        	// in case the element is removed recalculate description
	        	parameter.parameterValue.splice(idx, 1);
	        	//recalculate descriptions
	        	if(parameter.parameterValue.length == 0) {
	        		parameter.parameterDescription = '';
	        	} else {
	        		addParameterValueDescription(parameter);
	        	}

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

		$scope.toggleRadioParameter = function(parVal ,parDesc, parameter) {
			if(parameter.parameterDescription == undefined ) parameter.parameterDescription = "";
			parameter.parameterDescription = parDesc;
			$scope.resetCorrelatedParameters(parameter, false);
		};

		$scope.toggleComboParameter = function(parameter) {
			if (typeof parameter.parameterValue == 'undefined'){
				parameter.parameterValue= [];
			}
			if(parameter.parameterDescription == undefined ) {
				parameter.parameterDescription ="";
			}
			addParameterValueDescription(parameter);
			$scope.resetCorrelatedParameters(parameter, false);
		}


		$scope.popupLookupParameterDialog = function(parameter) {

			$scope.execProperties.hideProgressCircular.status=false;
			var params = $scope.execProperties.drivers ? $scope.execProperties.drivers : $scope.execProperties.parametersData.documentParameters;
			parameter.PARAMETERS=driversExecutionService.buildStringParameters(params);
			var templateUrl = sbiModule_config.dynamicResourcesBasePath
				+ '/angular_1.4/tools/documentexecution/templates/popupLookupParameterDialogTemplate.htm';

			$scope.popupParameterDialog(parameter, templateUrl);
		};


		$scope.endDateRange = function(defaultValue, parameter){
			var dateStart = parameter.parameterValue;
			if(defaultValue && defaultValue!='' && dateStart!=null && dateStart!=''){
				var defaultValueObj = {};
				for(var i=0; i<parameter.defaultValues.length; i++){
					if(parameter.defaultValues[i].value==defaultValue){
						defaultValueObj = parameter.defaultValues[i];
						break;
					}
				}
				var dateS = new Date(dateStart);
				if(defaultValueObj.type=='days'){
					var ys = parseInt(dateS.getDate());
					var ye =  parseInt(defaultValueObj.quantity);
					dateS.setDate(ys + ye);
				}else if(defaultValueObj.type=='years'){
					var ys = parseInt(dateS.getFullYear());
					var ye =  parseInt(defaultValueObj.quantity);
					dateS.setFullYear(ys + ye);
				}else if(defaultValueObj.type=='months'){
					var ys = parseInt(dateS.getMonth());
					var ye =  parseInt(defaultValueObj.quantity);
					dateS.setMonth(ys + ye);
				}else if(defaultValueObj.type=='weeks'){
					var ys = parseInt(dateS.getDate());
					var ye =  parseInt(defaultValueObj.quantity * 7);
					dateS.setDate(ys + ye);
				}
				var dateToSubmit = $filter('date')(dateS, $scope.parseDateTemp(sbiModule_config.localizedDateFormat));
				if(typeof parameter.datarange == 'undefined' ){
					parameter.datarange = {};
				}
				return dateToSubmit;
			}else{
				return '';
			}
		};


		$scope.parseDateTemp = function(date){
			result = "";
			if(date == "d/m/Y"){
				result = "dd/MM/yyyy";
			}
			if(date =="m/d/Y"){
				result = "MM/dd/yyyy"
			}
			return result;
		}

		$scope.showRequiredFieldMessage = function(parameter) {
			var currParamValue = parameter.parameterValue;
			return parameter.mandatory
					&& (
						typeof currParamValue === 'undefined'
						|| (typeof currParamValue === 'string' && currParamValue.length == 0)
						|| (typeof currParamValue === 'number' && isNaN(currParamValue) && currParamValue !== 0)
						|| (Array.isArray(currParamValue) && currParamValue.length == 0)
						|| currParamValue == null
					);
		};


		$scope.showRequiredFieldMessageDateRange = function(parameter) {
			if(parameter.mandatory){
				if( parameter.parameterValue!='' && parameter.datarange && parameter.datarange.opt!=''){
					return false;
				}else{
					return true;
				}
			}else{
				return false;
			}


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
				skipHide: true,
				$type: "confirm",
				clickOutsideToClose: false,
				theme: "knowage",
				openFrom: '#' + parameter.urlName,
				closeTo: '#' + parameter.urlName,
				templateUrl : templateUrl,
				onComplete : function() {
					$scope.execProperties.hideProgressCircular.status=true;
								},
				locals : {
					parameter: parameter,
					toggleCheckboxParameter: $scope.toggleCheckboxParameter,
					checkboxParameterExists: $scope.checkboxParameterExists,
					sbiModule_translate: $scope.sbiModule_translate,
					sbiModule_messaging: $scope.sbiModule_messaging
				},

				controllerAs: "paramDialogCtrl",

				controller : function($mdDialog, parameter, toggleCheckboxParameter, checkboxParameterExists,sbiModule_translate,sbiModule_messaging) {
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
						if(!paramDialogCtrl.tempParameter.multivalue && node.selectableNodes) {
//							paramDialogCtrl.tempParameter.parameterValue = node;
							paramDialogCtrl.tempParameter.parameterValue = node.value;
							paramDialogCtrl.tempParameter.parameterDescription = {};
							paramDialogCtrl.tempParameter.parameterDescription[node.value] = node.description;
							paramDialogCtrl.tempParameter.parameterDescription.id = node.id;
						}
						if(paramDialogCtrl.tempParameter.treePath) {
							if(paramDialogCtrl.tempParameter.treePath[node.id.slice(-1)] && paramDialogCtrl.tempParameter.treePath[node.id.slice(-1)] != node.label){
								paramDialogCtrl.tempParameter.treePath = [node.label];
							}
							paramDialogCtrl.tempParameter.treePath[node.id.slice(-1)] = node.label;
						}
						else paramDialogCtrl.tempParameter.treePath = [node.label];

						// in case the node is not a leaf the rest service is invoked in order
						// to retrieve sub node items
						if(!node.leaf && (!node.children || node.children.length == 0)) {
							$scope.getTreeParameterValue(node);
						}
					};

					paramDialogCtrl.abort = function() {
						$mdDialog.hide();
					};

					paramDialogCtrl.disableButton = function() {
						if(!paramDialogCtrl.tempParameter.multivalue && (!paramDialogCtrl.tempParameter.parameterValue || paramDialogCtrl.tempParameter.parameterValue && Array.isArray(paramDialogCtrl.tempParameter.parameterValue) && paramDialogCtrl.tempParameter.parameterValue.length==0)) {
							return true;
						} else return false;
					}

					paramDialogCtrl.save = function() {
						// Lov parameters NON tree
						if(paramDialogCtrl.tempParameter.defaultValues && paramDialogCtrl.tempParameter.defaultValuesMeta) {
							var parameterValueArray = [];
							paramDialogCtrl.tempParameter.parameterUniqueValue = [];
							if(paramDialogCtrl.lookoutGridOptions.api.getSelectedRows()) paramDialogCtrl.selectedTableItems = paramDialogCtrl.lookoutGridOptions.api.getSelectedRows();
							if(paramDialogCtrl.tempParameter.multivalue) {

								var parameterValueArrayToShow = [];

								for(var i = 0; i < paramDialogCtrl.selectedTableItems.length; i++) {
									var selectedTableItem = paramDialogCtrl.selectedTableItems[i];

									parameterValueArrayToShow.push(selectedTableItem[paramDialogCtrl.tempParameter.descriptionColumnNameMetadata.toUpperCase()]);
									parameterValueArray.push(selectedTableItem[paramDialogCtrl.tempParameter.valueColumnNameMetadata.toUpperCase()]);
									paramDialogCtrl.tempParameter.parameterUniqueValue.push(selectedTableItem);
								}

								if(paramDialogCtrl.tempParameter.selectionType == 'LOOKUP'){
									paramDialogCtrl.tempParameter.parameterDescription = {};
									for(var w=0; w<parameterValueArray.length; w++){
										paramDialogCtrl.tempParameter.parameterDescription[parameterValueArray[w]] =parameterValueArrayToShow[w];
									}

								}
								paramDialogCtrl.tempParameter.parameterValue = parameterValueArray;
							} else {
								if(paramDialogCtrl.tempParameter.selectionType == 'LOOKUP'){
									parameterValueArray.push(paramDialogCtrl.selectedTableItems[0][paramDialogCtrl.tempParameter.valueColumnNameMetadata.toUpperCase()]);
									paramDialogCtrl.tempParameter.parameterValue = parameterValueArray;
									paramDialogCtrl.tempParameter.parameterDescription = {};
									paramDialogCtrl.tempParameter.parameterDescription[paramDialogCtrl.tempParameter.parameterValue[0]] =paramDialogCtrl.selectedTableItems[0][paramDialogCtrl.tempParameter.descriptionColumnNameMetadata.toUpperCase()];
								}else{
									paramDialogCtrl.tempParameter.parameterDescription = paramDialogCtrl.selectedTableItems[paramDialogCtrl.tempParameter.descriptionColumnNameMetadata.toUpperCase()];
									paramDialogCtrl.tempParameter.parameterValue = paramDialogCtrl.selectedTableItems[paramDialogCtrl.tempParameter.valueColumnNameMetadata.toUpperCase()];
//									paramDialogCtrl.tempParameter.parameterValue = paramDialogCtrl.selectedTableItems.value;
								}
							}
						}

						angular.copy(paramDialogCtrl.tempParameter, paramDialogCtrl.initialParameterState);

						if(paramDialogCtrl.initialParameterState.selectionType == 'TREE'){
							driversExecutionService.setParameterValueResult(paramDialogCtrl.initialParameterState);
						}

						$scope.resetCorrelatedParameters(paramDialogCtrl.initialParameterState, false);

						$mdDialog.hide();
					};

					paramDialogCtrl.isFolderFn = function(node) {
						var isFolder = !!(
								!node.expanded
								&& (!node.leaf || node.leaf == false)
//								&& node.children !== undefined
//								&& (node.children.length > 0)
						);

						return isFolder;
					};

					paramDialogCtrl.isOpenFolderFn = function(node) {
						var isOpenFolder = !!(
								node.expanded
								&& (!node.leaf || node.leaf == false)
						);

						return isOpenFolder;
					};

					paramDialogCtrl.isDocumentFn = function(node) {
						var isDocument = !!(
								node.leaf
								&& !node.children
						);

						return isDocument;
					};

					paramDialogCtrl.allowInternalNodeSelection = paramDialogCtrl.initialParameterState.allowInternalNodeSelection;

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

					paramDialogCtrl.getFolderIconClass = function(node) {
						return 'fa fa-folder';
					};

					paramDialogCtrl.getOpenFolderIconClass = function(node) {
						return 'fa fa-folder-open';
					};

					// Lov parameters NON tree
					if(paramDialogCtrl.tempParameter.defaultValues && paramDialogCtrl.tempParameter.defaultValuesMeta) {
						paramDialogCtrl.tableColumns = [];
						for(var i = 0 ; i < paramDialogCtrl.tempParameter.defaultValuesMeta.length; i++) {
							var columnName = paramDialogCtrl.tempParameter.defaultValuesMeta[i].toUpperCase();
							var column = {label: columnName.replace(/_/g, ' '), name: columnName};
							paramDialogCtrl.tableColumns.push(column);
						};
						// BACKEND FILTERING
						var objPost = {};
						if($scope.execProperties.currentView && $scope.execProperties.currentView.status == 'DOCUMENT'){
							objPost.OBJECT_LABEL = $scope.execProperties.executionInstance.OBJECT_LABEL;
						} else {
							objPost.OBJECT_NAME = $scope.execProperties.executionInstance.OBJECT_NAME;
						}
						objPost.ROLE = $scope.execProperties.selectedRole.name;
						objPost.PARAMETER_ID = paramDialogCtrl.tempParameter.urlName;
						objPost.MODE = 'extra';
						objPost.PARAMETERS = paramDialogCtrl.tempParameter.PARAMETERS;

						paramDialogCtrl.columns = [
							{"headerName":"Label",
								"field":"label",
								headerCheckboxSelection: paramDialogCtrl.tempParameter.multivalue,
								headerCheckboxSelectionFilteredOnly: true,
								checkboxSelection: paramDialogCtrl.tempParameter.multivalue},
							{"headerName":"Description","field":"description"}];

						paramDialogCtrl.lookoutGridOptions = {
				            enableColResize: false,
				            enableSorting: true,
				            pagination: true,
				            paginationAutoPageSize: true,
				            onGridSizeChanged: resizeColumns,
				            onGridReady: resizeColumns,
				            rowSelection: paramDialogCtrl.tempParameter.multivalue ? 'multiple' : 'single',
				            rowMultiSelectWithClick: paramDialogCtrl.tempParameter.multivalue,
				            defaultColDef: {
				            	suppressMovable: true,
				            	tooltip: function (params) {
				            		return params.value;
				                },
				            },
				            columnDefs: paramDialogCtrl.columns,
				            postSort: postSort
						}

						function postSort(nodes){

						    function move(toIndex, fromIndex) {
						    	nodes.splice(toIndex, 0, nodes.splice(fromIndex, 1)[0]);
						    }

						    var nextInsertPos = 0;
						    for (var i = 0; i < nodes.length; i++) {
						        if (nodes[i].selected) {
						            move(nextInsertPos, i)
						            nextInsertPos++;
						        }
						    }
						}


						paramDialogCtrl.filterDataset = function(){
							paramDialogCtrl.lookoutGridOptions.api.setQuickFilter(paramDialogCtrl.paramSearchText);
						}

						function resizeColumns(grid){
							grid.api.sizeColumnsToFit();
						}

						function getColumnsDefs(fields){
							var temp = [];
							var first=false;
							for(var k in fields){
								if(fields[k]!='recNo' && fields[k]!='recCk' && !fields[k].hidden){
									var tempCol = {'headerName': fields[k].header, 'field':fields[k].name }
									if(!first) {
										tempCol.headerCheckboxSelection = paramDialogCtrl.tempParameter.multivalue;
										tempCol.headerCheckboxSelectionFilteredOnly = true,
										tempCol.checkboxSelection = paramDialogCtrl.tempParameter.multivalue;
										first = true;
									}
									temp.push(tempCol);
								}
							}
							return temp;
						}
						if(sbiModule_config.contextName != "/knowage") {
							sbiModule_restServices.restToRootProject();
						}
						sbiModule_restServices.post($scope.executionParameters,$scope.parametersPath, objPost)
						  .then(function(response) {
								if(response.data.errors && response.data.errors[0]){
									sbiModule_messaging.showWarningMessage(response.data.errors[0].message, 'Warning');
								}
								else if(response.data.status=="OK"){

									paramDialogCtrl.lookoutGridOptions.api.setColumnDefs(getColumnsDefs(response.data.result.metaData.fields));

									paramDialogCtrl.tableData = response.data.result.root;
									paramDialogCtrl.lookoutGridOptions.api.setRowData(response.data.result.root);
									if(parameter.parameterValue && parameter.parameterValue.length>0){
										var initSelectedItems = paramDialogCtrl.initSelectedTableItems();
										paramDialogCtrl.lookoutGridOptions.api.forEachNode( function(rowNode, index) {
											for(var i in initSelectedItems){
												if(initSelectedItems[i] == rowNode.data) {
													rowNode.setSelected(true);
												}
											}
										});
									}
									paramDialogCtrl.lookoutGridOptions.api.refreshClientSideRowModel('sort');
									paramDialogCtrl.lookoutGridOptions.api.sizeColumnsToFit();
									paramDialogCtrl.selectedTableItems = paramDialogCtrl.initSelectedTableItems();
								}
						  });

						paramDialogCtrl.initSelectedTableItems = function() {
							var isMultivalue = paramDialogCtrl.tempParameter.multivalue;
							var defaultValues = paramDialogCtrl.tableData;

							if(paramDialogCtrl.tempParameter.parameterValue && paramDialogCtrl.tempParameter.parameterValue != null) {

								var parameterValue = paramDialogCtrl.tempParameter.parameterValue;
								var parameterUniqueValue = paramDialogCtrl.tempParameter.parameterUniqueValue;
								var selectedTableItemsArray = [];

								for (var i = 0; i < defaultValues.length; i++) {
									var defaultValue = defaultValues[i];

									if(isMultivalue) {
										for (var j = 0; j < parameterUniqueValue.length; j++) {
											var parameterUniqueValueItem = parameterUniqueValue[j];

											if(angular.equals(parameterUniqueValueItem, defaultValue)) {
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

						//paramDialogCtrl.selectedTableItems = paramDialogCtrl.initSelectedTableItems();
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
					+ 'geoMapFilter?'
					+ 'SELECTEDLAYER=' + parameter.selectedLayer
					+ '&SELECTEDLAYERPROP=' + parameter.selectedLayerProp
					+ '&MULTIVALUE=' + parameter.multivalue
					+ valueData;

			$mdDialog.show({
				skipHide: true,
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

				controller : function($scope, $mdDialog, sbiModule_translate, sbiModule_messaging) {
					$scope.sbiModule_translate = sbiModule_translate;
					$scope.sbiModule_messaging = sbiModule_messaging;

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

		$scope.resetParameter = function(parameter, mainReset, endRecursion) {
			// reset selected parameter
			$scope.driversExecutionService.resetParameter(parameter, mainReset);
			// reset also all correlated parameters
			if (!endRecursion) {
				$scope.resetCorrelatedParameters(parameter, mainReset);
			}
		}

		$scope.resetCorrelatedParameters = function(parameter, mainReset) {
			var allCorrelatedParams = getCorrelatedParameters(parameter);
			for (var i=0; i<allCorrelatedParams.length; i++) {
				var correlatedParam = allCorrelatedParams[i];
				if (correlatedParam.defaultValues) correlatedParam.parameterValue = "";
				$scope.resetParameter(correlatedParam, mainReset, true);
			}
		}

		$scope.isArray = function(obj) {
			return Array.isArray(obj);
		}

		$scope.isBlank = function(value) {
			return (!value || value == '');
		}

		$scope.descriptionOf = function(value) {
			var matches = $scope.parameter.driverDefaultValue ? $scope.parameter.driverDefaultValue.filter(function(e) { return e.value == value; }) : [];
			if (matches.length > 0) {
				return matches[0].description;
			} else if(typeof value == "object" && value.hasOwnProperty("description")) {
				return value.description;
			} else {
				return $scope.parameter.parameterDescription[value];
			}
		}

		var getCorrelatedParameters = function(fatherParam) {
			var toReturn = [];
			var allParameters = $scope.execProperties.parametersData.documentParameters;
			for (var i=0; i<allParameters.length; i++) {
				var dependencies = Object.keys(allParameters[i].dependsOn);
				for (var j=0; j<dependencies.length; j++) {
					if (dependencies[j] == fatherParam.urlName) {
						toReturn.push(allParameters[i]);
						break;
					}
				}
			}
			return toReturn;
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

		var adaptExecutionProperties = function(){
			$scope.execProperties.selectedRole = {}
			$scope.execProperties.executionInstance = {}
			$scope.execProperties.executionInstance.OBJECT_ID = $scope.execProperties.id;
			$scope.execProperties.executionInstance.OBJECT_LABEL = $scope.execProperties.name;

			$scope.execProperties.initResetFunctionVisualDependency = {};
			$scope.execProperties.initResetFunctionVisualDependency.status=false;
			$scope.execProperties.initResetFunctionDataDependency = {};
			$scope.execProperties.initResetFunctionDataDependency.status=false;
			$scope.execProperties.initResetFunctionLovDependency = {};
			$scope.execProperties.initResetFunctionLovDependency.status=false;
			$scope.execProperties.returnFromDataDepenViewpoint = {};
			$scope.execProperties.returnFromDataDepenViewpoint.status = false;
			$scope.execProperties.returnFromLovDepenViewpoint = {};
			$scope.execProperties.returnFromLovDepenViewpoint.status = false;
			$scope.execProperties.isParameterRolePanelDisabled = {};
			$scope.execProperties.isParameterRolePanelDisabled.status = false;

			$scope.execProperties.returnFromVisualViewpoint = {};
			$scope.execProperties.returnFromVisualViewpoint.status = false;

			if($scope.execProperties.dsTypeCd){
				$scope.execProperties.executionInstance.OBJECT_NAME = $scope.execProperties.qbeDatamarts;
			} else if ($scope.execProperties.type == "SbiQbeDataSet") {
				$scope.execProperties.executionInstance.OBJECT_NAME = $scope.execProperties.configuration.qbeDatamarts;
			} else {
				$scope.execProperties.executionInstance.OBJECT_NAME = $scope.execProperties.name;
			}

			$scope.execProperties.selectedRole.name = sbiModule_user.roles[0];

			if($scope.execProperties.parametersData == null){
				$scope.execProperties.parametersData = {};
				$scope.execProperties.parametersData.documentParameters = $scope.execProperties.drivers;
			}

			if(!$scope.execProperties.hideProgressCircular){
				$scope.execProperties.hideProgressCircular={};
			    $scope.execProperties.hideProgressCircular.status=false;
			}
		}

		initalize();

	}
})();
