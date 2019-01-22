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
	.module('qbe_viewer', [ 'ngMaterial' ,'sbiModule', 'businessModelOpeningModule'])
	.service('$qbeViewer', function($mdDialog,sbiModule_config,sbiModule_restServices,sbiModule_translate,sbiModule_messaging,$log, $httpParamSerializer,$injector,urlBuilderService,windowCommunicationService,$mdSidenav) {
		var driversExecutionService = $injector.get('driversExecutionService');
		var driversDependencyService = $injector.get('driversDependencyService');

		var comunicator = windowCommunicationService;
		var consoleHandler = {}
		consoleHandler.name = "console"
		consoleHandler.handleMessage = function(message){
			console.log(message)
		}


		comunicator.addMessageHandler(consoleHandler);


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

			var saveHadler = {}
			saveHadler.name = "save"
			saveHadler.handleMessage = function(message){
				if(message.pars) {
					$scope.parameterItems = message.pars;
					$scope.selectedDataSet.qbeJSONQuery = message.qbeQuery;
				}
			}
			comunicator.addMessageHandler(saveHadler);
			$scope.$on("$destroy",function(){
				console.log("destroying controller")
				comunicator.removeMessageHandler(saveHadler);
			})
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
						comunicator.removeMessageHandler(saveHadler);
					});

			}
		};

		function openQbeInterfaceController($scope,url,driverableObject,$timeout) {

			$scope.showDrivers = false;
			$scope.driverableObject = {};
			$scope.driverableObject.executed = true;
			urlBuilderService.setBaseUrl(url);

			var queryParamObj = {};
			var queryDriverObj = {};

			if(driverableObject){

				driverableObject.currentView = {};
				driverableObject.currentView.status = 'BUSINESSMODEL';
				driverableObject.parameterView = {};
				driverableObject.parameterView.status='';

				$scope.currentView = driverableObject.currentView;
				$scope.parameterView = driverableObject.parameterView;

				driverableObject.executed = true;
				$scope.driverableObject = driverableObject;

				if(driverableObject.dsTypeCd ){
					    $scope.drivers = driverableObject.drivers
						queryParamObj.PARAMS = $scope.parameterItems ? $scope.parameterItems :  driverableObject.pars ;
				}else{
						$scope.drivers = $scope.bmOpen_urlViewPointService.listOfDrivers;
				}

				$scope.showDrivers = driversExecutionService.hasMandatoryDrivers($scope.drivers);

			}


			$scope.driverableObject.executed = !$scope.showDrivers;

			if($scope.driverableObject.pars && $scope.driverableObject.pars.length > 0){
					$scope.showDrivers = true
					$scope.driverableObject.executed = false;
			}


			var drivers = driversExecutionService.additionalUrlDrivers;

			var driversObject =  driversExecutionService.prepareDriversForSending(drivers);
			queryDriverObj.DRIVERS = driversObject;


		 urlBuilderService.addQueryParams(queryDriverObj);
			urlBuilderService.addQueryParams(queryParamObj);
			$scope.documentViewerUrl = urlBuilderService.build();

			$scope.toggleDrivers =function(){
				$scope.showDrivers = !$scope.showDrivers;
			}

			$scope.closeDocument = function() {

				$mdDialog.hide();

				if($scope.isFromDataSetCatalogue) {
					//$scope.selectedDataSet.qbeJSONQuery = document.getElementById("documentViewerIframe").contentWindow.qbe.getQueriesCatalogue();
					comunicator.sendMessage("close");
				} else {
					if ($scope.datasetSavedFromQbe==true) {

						console.info("[RELOAD]: Reload all necessary datasets (its different categories)");

						$scope.currentOptionMainMenu=="datasets" ? $scope.reloadMyDataFn() : $scope.reloadMyData = true;

						if($scope.currentOptionMainMenu=="models"){

							if ($scope.currentModelsTab=="federations") {
								// If the suboption of the Data option is Federations.
								$scope.getFederatedDatasets();
							}

						}

						$scope.datasetSavedFromQbe = false;
					}
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

				/**
				 * Take the frame that keeps the QBE ExtJS page (inside the 'qbe' property - defined inside the qbe.jsp), so we can access functions
				 * inside the QbePanel.js (the page). We need 'openSaveDataSetWizard' function in order to save the dataset from the QBE.
				 */
				/**
				 * These two lines are commented, since the IE has a problem with taking the 'contentWindow' property of the current frame.
				 * At the same time, extraction of the 'contentWindow' through the 'document' object works fine (and represents almost the
				 * same solution) in all three browsers: IE, Mozilla, Chrome.
				 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				// OLD IMPLEMENTATION
//				var frame = window.frames['documentViewerIframe'];
//				frame.contentWindow.qbe.openSaveDataSetWizard('TRUE');

				// NEW IMPLEMENTATION



				if(!$scope.editQbeDset){
					window.openPanelForSavingQbeDataset();
				} else {

					$scope.selectedDataSet.qbeJSONQuery = document.getElementById("documentViewerIframe").contentWindow.qbe.getQueriesCatalogue();
					sbiModule_restServices.promisePost('1.0/datasets','', angular.toJson($scope.selectedDataSet))
					.then(
							function(response) {

								sbiModule_restServices.promiseGet('1.0/datasets/dataset/id',response.data.id)
									.then(
											function(responseDS) {

												$log.info("Dataset saved successfully");
												$scope.datasetSavedFromQbe=true;
												$scope.closeDocument();


											})})
				}


				/**
				 * Catch the 'save' event that is fired when the DS is persisted (saved) after confirming the dataset wizard inside the QBE (as a
				 * result of calling the 'openSaveDataSetWizard' function.
				 */
				//document.getElementById("documentViewerIframe").contentWindow.qbe.on("save", function() {$scope.datasetSavedFromQbe = true;})

			}

		}


});