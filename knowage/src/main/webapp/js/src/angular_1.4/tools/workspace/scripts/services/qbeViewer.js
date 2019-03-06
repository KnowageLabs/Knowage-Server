/**
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 * @author Ana Tomic (atomic, ana.tomic@mht.net)
 */

angular
	.module('qbe_viewer', ['ngMaterial' ,'sbiModule', 'businessModelOpeningModule', 'datasetSaveModule', 'datasetSchedulerModule'])
	.service('$qbeViewer', function($mdDialog,sbiModule_config,sbiModule_restServices,sbiModule_translate,sbiModule_messaging,$log, $httpParamSerializer,$injector,sbiModule_urlBuilderService,windowCommunicationService,$mdSidenav,qbeViewerMessagingHandler, $mdPanel, $q, datasetSave_service, datasetScheduler_service) {
		var driversExecutionService = $injector.get('driversExecutionService');
		var driversDependencyService = $injector.get('driversDependencyService');
		var comunicator = windowCommunicationService;
		var urlBuilderService = sbiModule_urlBuilderService;
//		var comunicator = windowCommunicationService;
//		var consoleHandler = {}
//		consoleHandler.name = "console"
//		consoleHandler.handleMessage = function(message){
//			console.log(message)
//		}


	//	comunicator.addMessageHandler(consoleHandler);


		this.openQbeInterfaceFromModel = function($scope,url,driverableObject) {

			$scope.editQbeDset = false;
			if(datasetParameters.error){
				sbiModule_messaging.showErrorMessage(datasetParameters.error, 'Error');
			}else{

				$mdDialog
					.show
					(
						{
							skipHide: true,
							scope:$scope,
							preserveScope: true,
							controller: openQbeInterfaceController,
	//						templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/scripts/services/qbeViewerTemplate.html',
							templateUrl: sbiModule_config.contextName + '/js/src/angular_1.4/tools/workspace/scripts/services/qbeViewerTemplate.html',
							fullscreen: true,
							locals:{
									url:url,
									driverableObject:$scope.selectedModel
							}
						}
					)
					.then(function() {

					});
			}

		};

		this.openQbeInterfaceDSet = function($scope, editDSet, url, isDerived) {



			if(datasetParameters.error){
				sbiModule_messaging.showErrorMessage(datasetParameters.error, 'Error');
			}else{

				$scope.editQbeDset = editDSet;
				if( $scope.selectedDataSet && !isDerived){
					globalQbeJson = $scope.selectedDataSet.qbeJSONQuery;
				}

				$mdDialog
					.show
					(
						{
							scope:$scope,
							preserveScope: true,
							controller: openQbeInterfaceController,
							templateUrl: sbiModule_config.contextName + '/js/src/angular_1.4/tools/workspace/scripts/services/qbeViewerTemplate.html',
							fullscreen: true,
							locals:{
								url:url,
								driverableObject:$scope.selectedDataSet,
								   }
						}
					)
					.then(function() {

					});

			}
		};

		function openQbeInterfaceController($scope,url,driverableObject,$timeout) {



//			$scope.$on("$destroy",function(){
//				console.log("destroying controller")
//				comunicator.removeMessageHandler(saveHadler);
//			})

			$scope.showDrivers = false;
			$scope.driverableObject = {};
			$scope.driverableObject.executed = true;
			urlBuilderService.setBaseUrl(url);
			var savingPanelConfig;
//			driverableObject.isParameterRolePanelDisabled = {};
//			driverableObject.isParameterRolePanelDisabled.status = true;

			var initNewDataSet = function() {
				if ($scope.selectedDataSet == undefined || angular.equals($scope.selectedDataSet, {})) {
					$scope.selectedDataSet = {
							dsTypeCd: "Qbe",
							qbeDatamarts: driverableObject.name,
							qbeDataSource: driverableObject.dataSourceLabel,
							currentView: driverableObject.currentView,
							parameterView: driverableObject.parameterView,
							executed: driverableObject.executed
					};
				}
			}

			initNewDataSet();

			var openConfirmationPanel = function(okFunction,cancelFunction){
				var config = {
						attachTo:  angular.element(document.body),
						templateUrl: sbiModule_config.contextName +'/js/src/angular_1.4/tools/workspace/templates/closingConfirmationPanel.html',
						position: $mdPanel.newPanelPosition().absolute().center(),
						fullscreen :false,
						controller: function($scope,mdPanelRef,sbiModule_translate){
							$scope.translate = sbiModule_translate;
							var isFunction=function(func){
								return func && typeof func === "function";
							}
							$scope.ok = function(){
								if(isFunction(okFunction)){
									okFunction();
								}
								okFunction();
								mdPanelRef.close();
							}

							$scope.cancel = function(){
								if(isFunction(cancelFunction)){
									cancelFunction();
								}
								mdPanelRef.close();
							}
						},

						hasBackdrop: true,
						clickOutsideToClose: true,
						escapeToClose: true,
						focusOnOpen: true,
						preserveScope: true,
				};

				$mdPanel.open(config);
			}

			var openPanelForSavingQbeDataset = function() {
				savingPanelConfig = {
						attachTo:  angular.element(document.body),
						templateUrl: sbiModule_config.contextName +'/js/src/angular_1.4/tools/workspace/templates/saveQbeDatasetTemplate.html',
						position: $mdPanel.newPanelPosition().absolute().center(),
						fullscreen: true,
						controller: function($scope, selectedDataSet, mdPanelRef, closeDocumentFn, savedFromQbe, sbiModule_messaging, sbiModule_translate, datasetSave_service, datasetScheduler_service){
							$scope.model = {selectedDataSet: selectedDataSet, "mdPanelRef": mdPanelRef};

							$scope.closePanel = function(){
								mdPanelRef.close();
							}

							$scope.saveDataSet = function() {
								if ($scope.model.selectedDataSet.isPersisted && !$scope.model.selectedDataSet.hasOwnProperty('pars'))
									$scope.model.selectedDataSet.pars = [];

								datasetSave_service.persistDataSet($scope.model.selectedDataSet)
												.then(function(response){
													var dsId = response.data.id;
													if (savedFromQbe)
														$scope.model.selectedDataSet.id = dsId;

													if ($scope.model.selectedDataSet.isScheduled) {
														$scope.model.selectedDataSet.schedulingCronLine = datasetScheduler_service.createSchedulingCroneLine();
														datasetScheduler_service.schedulDataset($scope.model.selectedDataSet)
															.then(function(response){
																sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.generic.success'), 'SUCCESS');
																$scope.closePanel();
																if (!savedFromQbe) {
																	clearModel();
																	closeQbe();
																}
															}, function(response){
																sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
															});
													} else {
														sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.generic.success'), 'SUCCESS');
														$scope.closePanel();
														if (!savedFromQbe) {
															clearModel();
															closeQbe();
														}
													}
												}, function(error){
													sbiModule_messaging.showErrorMessage(error.data.errors[0].message, 'Error');
												});
							}

							var closeQbe = function() {
								return closeDocumentFn();
							}

							var clearModel = function() {
								$scope.model.selectedDataSet = {};
							}
						},
						locals: {selectedDataSet: $scope.selectedDataSet, closeDocumentFn: $scope.closeDocument, savedFromQbe: $scope.datasetSavedFromQbe},
						hasBackdrop: true,
						clickOutsideToClose: true,
						escapeToClose: true,
						focusOnOpen: true,
						preserveScope: true,
				};

				$mdPanel.open(savingPanelConfig);
			}

			var queryParamObj = {};
			var queryDriverObj = {};

			if(driverableObject){
				driverableObject.currentView = {};
				driverableObject.currentView.status = 'BUSINESSMODEL';
				driverableObject.parameterView = {};
				driverableObject.parameterView.status='';
				driverableObject.executed = true;
				$scope.driverableObject = driverableObject;

				$scope.currentView = driverableObject.currentView;
				$scope.parameterView = driverableObject.parameterView;
				if(driverableObject.dsTypeCd ){
					    $scope.drivers = driverableObject.drivers
						queryParamObj.PARAMS = $scope.parameterItems ? $scope.parameterItems :  driverableObject.pars ;
				}else{
						$scope.drivers = $scope.bmOpen_urlViewPointService.listOfDrivers;
				}

				$scope.showDrivers = driversExecutionService.hasMandatoryDrivers($scope.drivers);
				$scope.showFilterIcon = driversExecutionService.showFilterIcon;
			}


			$scope.driverableObject.executed = !$scope.showDrivers;

			if($scope.driverableObject.pars && $scope.driverableObject.pars.length > 0){
					$scope.showDrivers = true
					$scope.driverableObject.executed = false;
			}

			var driversObject =  driversExecutionService.prepareDriversForSending($scope.drivers);
			queryDriverObj.DRIVERS = driversObject;

			urlBuilderService.addQueryParams(queryDriverObj);
			urlBuilderService.addQueryParams(queryParamObj);
			$scope.documentViewerUrl = urlBuilderService.build();

			$scope.toggleDrivers =function(){
				$scope.showDrivers = !$scope.showDrivers;
			}

			var onClosing = function(){
				console.info("[RELOAD]: Reload all necessary datasets (its different categories)");
				$scope.selectedDataSet = {};

				$scope.currentOptionMainMenu=="datasets" ? $scope.reloadMyDataFn() : $scope.reloadMyData = true;

				if($scope.currentOptionMainMenu=="models"){

					if ($scope.currentModelsTab=="federations") {
						// If the suboption of the Data option is Federations.
						$scope.getFederatedDatasets();
					}

				}

				$mdDialog.hide();
			}

			$scope.closeDocument = function(confirm) {



				if($scope.isFromDataSetCatalogue) {
					//$scope.selectedDataSet.qbeJSONQuery = document.getElementById("documentViewerIframe").contentWindow.qbe.getQueriesCatalogue();
					$mdDialog.hide();
					comunicator.sendMessage("close");
				}else if(confirm){
					openConfirmationPanel(function(){
						onClosing();
					});

				} else {
						onClosing();

				}
			}


			$scope.execute = function() {
				var drivers = {};
				if($scope.drivers){
					drivers = driversExecutionService.prepareDriversForSending($scope.drivers);
				}

				queryDriverObj.DRIVERS = drivers;
				urlBuilderService.addQueryParams(queryParamObj);
				urlBuilderService.addQueryParams(queryDriverObj);

				$scope.documentViewerUrl = urlBuilderService.build();
				$scope.showDrivers = false
				$scope.driverableObject.executed = true;
			}

			$scope.clearListParametersForm = function() {
				if($scope.drivers.length > 0) {
					for(var i = 0; i < $scope.drivers.length; i++) {
						driversExecutionService.resetParameter($scope.drivers[i]);
						//INIT VISUAL CORRELATION PARAMS
//						driversDependencyService.updateVisualDependency($scope.drivers[i], $scope.drivers);
					}
				}
			};

			$scope.createNewViewpoint = function() {
				$mdDialog.show({
					autoWrap: false,
					skipHide: true,
					preserveScope : true,
					templateUrl : sbiModule_config.contextName + '/js/src/angular_1.4/tools/glossary/commons/templates/dialog-new-parameters-document-execution.html',
					controllerAs : 'vpCtrl',
					controller : function($mdDialog) {
						var vpctl = this;
						vpctl.headerTitle = sbiModule_translate.load("sbi.execution.executionpage.toolbar.saveas");
						vpctl.name = sbiModule_translate.load("sbi.execution.viewpoints.name");
						vpctl.description = sbiModule_translate.load("sbi.execution.viewpoints.description");
						vpctl.visibility = sbiModule_translate.load("sbi.execution.subobjects.visibility");
						vpctl.publicOpt = sbiModule_translate.load("sbi.execution.subobjects.visibility.public");
						vpctl.privateOpt = sbiModule_translate.load("sbi.execution.subobjects.visibility.private");
						vpctl.cancelOpt = sbiModule_translate.load("sbi.ds.wizard.cancel");
						vpctl.submitOpt = sbiModule_translate.load("sbi.generic.update");
						vpctl.submit = function() {
							vpctl.newViewpoint.OBJECT_LABEL = driverableObject.executionInstance.OBJECT_LABEL;
							vpctl.newViewpoint.ROLE = driverableObject.selectedRole.name;
							vpctl.newViewpoint.VIEWPOINT = driversExecutionService.buildStringParameters($scope.drivers);
							sbiModule_restServices.post("1.0/metamodelviewpoint", "addViewpoint", vpctl.newViewpoint)
									.success(function(data, status, headers, config) {
										if(data.errors && data.errors.length > 0 ) {
											sbiModule_restServices.errorHandler(data.errors[0].message,"sbi.generic.toastr.title.error");
										}else{
											$mdDialog.hide();
//											documentExecuteServices.showToast(sbiModule_translate.load("sbi.execution.viewpoints.msg.saved"), 3000);
										}
									})
									.error(function(data, status, headers, config) {
										sbiModule_restServices.errorHandler("Errors : " + status,"sbi.execution.viewpoints.msg.error.save");
									});
						};

						vpctl.annulla = function($event) {
							$mdDialog.hide();
							$scope.newViewpoint = JSON.parse(JSON.stringify(driversExecutionService.emptyViewpoint));
						};
					},

					templateUrl : sbiModule_config.contextName + '/js/src/angular_1.4/tools/documentexecution/templates/dialog-new-parameters-document-execution.html'
				});
			};

//			$scope.frameLoaded = true;

			$scope.getViewpoints = function() {
				$scope.driverableObject.currentView.status = 'PARAMETERS';
				$scope.driverableObject.parameterView.status='FILTER_SAVED';

				sbiModule_restServices.get("1.0/metamodelviewpoint", "getViewpoints",
						"label=" + driverableObject.executionInstance.OBJECT_LABEL + "&role="+ driverableObject.selectedRole.name)
						.success(function(data, status, headers, config) {
							console.log('data viewpoints '  ,  data.viewpoints);
							driversExecutionService.gvpCtrlViewpoints = data.viewpoints;
//							if($mdSidenav('parametersPanelSideNav').isOpen()) {
//								$scope.toggleParametersPanel(false);
//							}
						})
						.error(function(data, status, headers, config) {});
			};


			$scope.saveQbeDocument = function() {

				if ($scope.editQbeDset)
					$scope.datasetSavedFromQbe=false;
				else
					$scope.datasetSavedFromQbe=true;

				/**
				 * COMMUNICATOR LOGIC
				 * Step 1: Send message to QBE, so QBE Engine is going to update JsonQBEQuery and Meta
				 */
				comunicator.sendMessage("saveDS");
				// Step 2: After QBE finish, open Panel for Save - openPanelForSavingQbeDataset()
			}


			$scope.$on("$destroy",function(){
				console.log("destroying controller")
				comunicator.removeMessageHandler(messagingHandler);
			})

			var persistDataSet = function(){
				sbiModule_restServices.promisePost('1.0/datasets','', angular.toJson($scope.selectedDataSet))
				.then(
						function(response) {

							sbiModule_restServices.promiseGet('1.0/datasets/dataset/id',response.data.id)
								.then(
										function(responseDS) {

											$log.info("Dataset saved successfully");
											$scope.datasetSavedFromQbe=true;
											$scope.closeDocument();
							//				comunicator.removeMessageHandler(saveHadler);

										})})
			}

			var messagingHandler = qbeViewerMessagingHandler.initalizeHandler($scope.selectedDataSet,$scope.parameterItems, openPanelForSavingQbeDataset);
			qbeViewerMessagingHandler.registerHandler(messagingHandler);

		}


});