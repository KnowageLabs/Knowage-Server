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
	.service('$qbeViewer', function($mdDialog,sbiModule_config,sbiModule_restServices,sbiModule_messaging,$log, $httpParamSerializer) {

		this.openQbeInterfaceFromModel = function($scope,url,execProperties,drivers,driversExecutionService) {

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
							locals:{url:url,
									execProperties:execProperties,
									drivers:drivers,
									driversExecutionService:driversExecutionService
							}
						}
					)
					.then(function() {

					});
			}

		};

		this.openQbeInterfaceDSet = function($scope, editDSet, url, isDerived,execProperties) {

			if(execProperties){
				$scope.dataset = execProperties;
				$scope.drivers = $scope.dataset.drivers;
				$scope.showDrivers = true;

				if($scope.drivers){
					$scope.businessModel.executed = false;
				}else{
					$scope.showDrivers = false;
					$scope.businessModel.executed = true;
				}
			}else{
				$scope.showDrivers = false;
				$scope.businessModel = {};
				$scope.businessModel.executed = true;
			}

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
							locals:{url:url,
								   execProperties:execProperties,
								   drivers:$scope.dataset.drivers,
								   }
						}
					)
					.then(function() {

					});

			}
			$scope.executeParameter = function(){
				$scope.documentViewerUrl = url //+ driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters);
				$scope.showQbe = true;
				$scope.businessModel.executed = true;
			}

		};

		function openQbeInterfaceController($scope,url,execProperties,drivers,$timeout,driversExecutionService, bmOpen_urlViewPointService) {
			if(execProperties){
				$scope.businessModel = execProperties;
				if(execProperties.dsTypeCd){
					$scope.drivers = execProperties.drivers
				}else{
					$scope.drivers = bmOpen_urlViewPointService.listOfDrivers;
				}
				for(var i = 0; i < $scope.drivers.length;i++){
					$scope.businessModel.executed = true;
					if($scope.drivers[i].mandatory){
						if($scope.drivers[i].defaultValues.length == 1 && $scope.drivers[i].defaultValues[0].isEnabled){
							var drivers = driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters);
							var driverName = Object.keys(drivers)[0];
							var driverValue = drivers[Object.keys(drivers)[0]][0].value;
							 var driverObject = {};
							 driverObject[driverName] = driverValue;
							$scope.documentViewerUrl = url + '&' +  $httpParamSerializer(driverObject)  ;
							$scope.businessModel.executed = true;
							break;
						}else{
							$scope.businessModel.executed = false;
							break;
						}
					}
				}

				$scope.showDrivers = true;
				if($scope.drivers.length == 0){
					$scope.showDrivers = false;
					$scope.businessModel.executed = true;
				}
			}else{
				$scope.showDrivers = false;
				$scope.businessModel = {};
				$scope.businessModel.executed = true;
				$scope.documentViewerUrl = url;
			}


			$scope.hideDrivers =function(){
				$scope.showDrivers = true;
				$scope.businessModel.executed = !$scope.businessModel.executed;
			}
			$scope.closeDocument = function() {

				$mdDialog.hide();

				if($scope.isFromDataSetCatalogue) {
					$scope.selectedDataSet.qbeJSONQuery = document.getElementById("documentViewerIframe").contentWindow.qbe.getQueriesCatalogue();

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
			$scope.executeParameter = function(){
				var drivers = driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters);
				$scope.documentViewerUrl = url + '&' + $httpParamSerializer(drivers);
				$scope.showQbe = true;
				$scope.businessModel.executed = true;
			}
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