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
	.service('$qbeViewer', function($mdDialog,sbiModule_config,sbiModule_restServices,sbiModule_messaging,$log, $httpParamSerializer,$injector,qbeUrlBuilderService) {
		var driversExecutionService = $injector.get('driversExecutionService');

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

			$scope.showDrivers = false;
			$scope.driverableObject = {};
			$scope.driverableObject.executed = true;
			qbeUrlBuilderService.setBaseUrl(url);

			var queryParamObj = {};
			queryParamObj.PARAMS = $scope.parameterItems ? $scope.parameterItems :  driverableObject.pars ;

			if(driverableObject){

				driverableObject.executed = true;
				$scope.driverableObject = driverableObject;
				driverableObject.dsTypeCd ? $scope.drivers = driverableObject.drivers : $scope.drivers = $scope.bmOpen_urlViewPointService.listOfDrivers;
				$scope.showDrivers = driversExecutionService.hasMandatoryDrivers($scope.drivers);

			}


			$scope.driverableObject.executed = !$scope.showDrivers;

			if($scope.driverableObject.pars && $scope.driverableObject.pars.length > 0){
					$scope.showDrivers = true
					$scope.driverableObject.executed = false;
			}


			var drivers = driversExecutionService.additionalUrlDrivers;

			var driversObject = driversExecutionService.createObjectFromArray(drivers);

			qbeUrlBuilderService.addQueryParams(driversObject);
			qbeUrlBuilderService.addQueryParams(queryParamObj);
			$scope.documentViewerUrl = qbeUrlBuilderService.build();

			$scope.toggleDrivers =function(){
				$scope.showDrivers = !$scope.showDrivers;
				$scope.driverableObject.executed = true;
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

				if($scope.drivers){
				var drivers = driversExecutionService.buildStringParameters($scope.drivers);
				}else {
					var drivers = {};
				}

				qbeUrlBuilderService.addQueryParams(drivers);
				$scope.documentViewerUrl = qbeUrlBuilderService.build();
				$scope.showDrivers = false
				$scope.driverableObject.executed = true;
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