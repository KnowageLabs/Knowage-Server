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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Controller for Analysis view of the Workspace.
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */
(function() {

	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

	angular
	.module('analysis_view_workspace', [])
	/**
	 * The HTML content of the Analysis view (analysis documents).
	 */
	.directive('analysisViewWorkspace', function () {
		return {
			restrict: 'E',
			replace: 'true',
			templateUrl: currentScriptPath + '../../../templates/analysisViewWorkspace.html',
			controller: analysisController
		};
	})
	.service('multipartForm',['$http',function($http){

		this.post = function(uploadUrl,data){

			var formData = new FormData();

			for(var key in data){


				formData.append(key,data[key]);
			}

			return $http.post(uploadUrl,formData,{
				transformRequest:angular.identity,
				headers:{'Content-Type': undefined}
			})
		}
	}])
	.filter("asDate", function () {

		return function (input) {
			return new Date(input);
		}

	});

	function analysisController($scope, sbiModule_restServices, sbiModule_translate, sbiModule_config, sbiModule_user,
			$mdDialog, $mdSidenav, $documentViewer, $qbeViewer, toastr, $httpParamSerializer, multipartForm, sbiModule_i18n, sbiModule_messaging) {

		$scope.cockpitAnalysisDocsInitial = [];
		$scope.activeTabAnalysis = null;
		$scope.translate = sbiModule_translate;
		$scope.selectedItems = [];
		$scope.previewFile = {};

		$scope.i18n = sbiModule_i18n;
		//if(!$scope.i18n.isLoaded()){
		//	$scope.i18n.loadI18nMap();
		//}
		$scope.getSelectedDocumentDate =function(string){
			return moment(string).locale(sbiModule_config.curr_language).format('LLL');
		}

		$scope.loadAllMyAnalysisDocuments = function() {

			sbiModule_restServices
			.promiseGet("documents", "myAnalysisDocsList")
			.then(
					function(response) {

						/**
						 * TODO: Provide a comment
						 */
						angular.copy(response.data.root,$scope.allAnalysisDocs);

						$scope.i18n.loadI18nMap().then(function() {

							for (var i = 0 ; i < $scope.allAnalysisDocs.length; i ++ ){
								$scope.allAnalysisDocs[i].name = $scope.i18n.getI18n($scope.allAnalysisDocs[i].name);
							}

							$scope.cockpitAnalysisDocs = [];
							var tempDocumentType = "";

							/**
							 * TODO: Provide a comment
							 */
							for(var i=0; i<$scope.allAnalysisDocs.length; i++) {

								tempDocumentType = $scope.allAnalysisDocs[i].typeCode;

								switch(tempDocumentType.toUpperCase()) {

								case "DOCUMENT_COMPOSITE":
									$scope.cockpitAnalysisDocs.push($scope.allAnalysisDocs[i]);
									break;

								case "KPI":
								case "MAP":
								case "DOCUMENT_COMPOSITE":
									$scope.cockpitAnalysisDocs.push($scope.allAnalysisDocs[i]);
									break;
								}

							}

							angular.copy($scope.cockpitAnalysisDocs,$scope.cockpitAnalysisDocsInitial);

							console.info("[LOAD END]: Loading of Analysis Cockpit documents is finished.");

						}); // end of load I 18n
					},

					function(response) {

						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);

					}
			);
		}

		/**
		 * If we are coming to the Workspace interface (web page) from the interface for the creation of the new Cockpit document, reload all Analysis documents
		 * (Cockpit documents), so we can see the changes for the option from which we started a creation of a new documents (Analysis option).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if (whereAreWeComingFrom == "NewCockpit") {
			$scope.loadAllMyAnalysisDocuments();
			// Do not load Analysis documents again when clicking on its option after returning back to the Workspace.
			$scope.analysisDocumentsLoaded = true;
			whereAreWeComingFrom == null;
		}

		/**
		 * Clone a particular Analysis document.
		 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.cloneAnalysisDocument = function(document) {

			console.info("[CLONE START]: The cloning of a selected '" + document.label + "' has started.");

			var confirm = $mdDialog
			.confirm()
			.title($scope.translate.load("sbi.workspace.clone.confirm.title"))
			.content($scope.translate.load("sbi.workspace.analysis.clone.document.confirm.msg"))
			.ariaLabel('delete Document')
			.ok($scope.translate.load("sbi.general.yes"))
			.cancel($scope.translate.load("sbi.general.No"));

			$mdDialog
			.show(confirm)
			.then(
					function() {

						sbiModule_restServices
						.promisePost("documents","clone?docId="+document.id)
						.then(
								function(response) {

									if (document.typeCode == "DOCUMENT_COMPOSITE") {
										$scope.cockpitAnalysisDocsInitial.push(response.data);
										$scope.cockpitAnalysisDocs.push(response.data);
									}

									console.info("[CLONE END]: The cloning of a selected '" + document.label + "' went successfully.");

									// Take the toaster duration set inside the main controller of the Workspace. (danristo)
									toastr.success(sbiModule_translate.load('sbi.workspace.analysis.clone.document.success.msg'),
											sbiModule_translate.load('sbi.generic.success'), $scope.toasterConfig);

								},

								function(response) {

									// Take the toaster duration set inside the main controller of the Workspace. (danristo)
									toastr.error(response.data, sbiModule_translate.load('sbi.browser.document.clone.error'), $scope.toasterConfig);

								}
						);
					}
			);
		}
		
		/**
		 * Edit particular analysis document from the workspace */
		
		$scope.editAnalysisDocument = function(selectedDocument, ev) {
			
			console.info("[EDIT START]: Edit of Analysis Cockpit document with the label '", selectedDocument);
			
			$mdDialog.show({
				controller: editAnalysisDocumentController,
				templateUrl: sbiModule_config.dynamicResourcesBasePath + "/angular_1.4/tools/workspace/templates/editAnalysisDocumentTemp.html",
				locals: {
					document: selectedDocument		
				},
				parent: angular.element(document.body),
				targetEvent: ev,
				clickOutsideToClose: false
				
			});	
		}
		
		$scope.isVisible = function(document) {
			
			return ((document) && sbiModule_user.userId === document.creationUser) ? true : false;
		}
		
		function editAnalysisDocumentController($scope, $mdDialog, document) {
			
			$scope.document = document;
					
			$scope.cancel = function() {
				$mdDialog.cancel();
			}
			
			$scope.save = function(document) {
				console.log(document);
				
				$scope.document = {
					name: document.name,
					label: document.label,
					description: document.description,
					id: document.id
				}
				
				$scope.dataToSend = {
					document: $scope.document,
					updateFromWorkspace: true
				};
				
				sbiModule_restServices.promisePost("2.0/saveDocument", "", $scope.dataToSend);
				$mdDialog.hide();
				
			}
		}

		/**
		 * Delete particular Analysis document from the Workspace.
		 */
		$scope.deleteAnalysisDocument = function(document) {

			console.info("[DELETE START]: Delete of Analysis Cockpit document with the label '" + document.label + "' is started.");

			var confirm = $mdDialog
			.confirm()
			.title(sbiModule_translate.load("sbi.workspace.delete.confirm.title"))
			.content(sbiModule_translate.load("sbi.workspace.analysis.delete.document.confirm.msg"))
			.ariaLabel('delete Document')
			.ok(sbiModule_translate.load("sbi.general.yes"))
			.cancel(sbiModule_translate.load("sbi.general.No"));

			$mdDialog
			.show(confirm)
			.then(
					function() {

						sbiModule_restServices
						.promiseDelete("1.0/documents", document.label)
						.then(
								function(response) {

									/**
									 * Reload all Cockpits in Analysis after delete.
									 */
									$scope.loadAllMyAnalysisDocuments();

									$scope.selectedDocument = undefined;	// TODO: Create and define the role of this property

									/**
									 * If some dataset is removed from the filtered set of datasets, clear the search input, since all datasets are refreshed.
									 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
									 */
									$scope.searchInput = "";

									/**
									 * When deleting document from the Analysis interface, run the re-loading of documents in the Organizer, so they will be re-collected
									 * after deletion of one of the documents available in the Analysis interface, that could on the other side be inside the Organizer
									 * as well. E.g. physical removing of a document inside the Analysis, to which there is a (one or more) link inside the Organizer,
									 * should be followed by the removal of that link (links) inside the Organizer.
									 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
									 */
									$scope.loadAllFolders();

									/**
									 * The document that does not exist anymore (removed from Analysis documents) and previously appeared in the Recent documents (recently
									 * executed ones), should be removed from this option as well (from Recent). So, provide a reload of recently executed documents.
									 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
									 */
									$scope.loadRecentDocumentExecutionsForUser();

									console.info("[DELETE END]: Delete of Analysis Cockpit document with the label '" + document.label + "' is done successfully.");

									// Take the toaster duration set inside the main controller of the Workspace. (danristo)
									toastr.success(sbiModule_translate.load('sbi.workspace.analysis.delete.document.success.msg')
											,sbiModule_translate.load('sbi.generic.success'), $scope.toasterConfig);

								},

								function(response) {

									// Take the toaster duration set inside the main controller of the Workspace. (danristo)
									toastr.error(response.data, sbiModule_translate.load('sbi.browser.document.delete.error'), $scope.toasterConfig);

								}
						);
					}
			);

		}

		/**
		 * TODO:
		 * Create a new Cockpit document.
		 */

		$scope.addNewAnalysisDocument = function() {
			console.info("[NEW COCKPIT - START]: Open page for adding a new Cockpit document.");
			// cockpit service url from dataset parameters because sbiModule.config engineUrls not visible for user

			$mdDialog.show({
				scope:$scope,
				preserveScope: true,
				controller: CreateNewAnalysisController,
				templateUrl: sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',
				clickOutsideToClose:true,
				escapeToClose :true,
				fullscreen: true
			})
		}
		/**
		 * add new geo document
		 */
		$scope.addNewGeoMap = function() {
			console.info("[NEW GEO - START]: Open page for adding a new geo map document.");
			//console.log(datasetParameters);
			$mdDialog.show({
				scope:$scope,
				preserveScope: true,
				controller: CreateNewGeoMapController,
				templateUrl: sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',
				clickOutsideToClose:true,
				escapeToClose :true,
				fullscreen: true
			});


		}
		
		/**
		 * add new kpi document
		 */
		$scope.addNewKPI = function() {
			console.info("[NEW KPI - START]: Open page for adding a new kpi document.");
			//console.log(datasetParameters);
			$mdDialog.show({
				scope:$scope,
				preserveScope: true,
				controller: CreateNewKPIController,
				templateUrl: sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',
				clickOutsideToClose:true,
				escapeToClose :true,
				fullscreen: true
			});


		}
		
		
		/**
		 * Edit existing GEO document
		 */

		$scope.editGeoDocument= function(document){
			console.log(document);
			if(document.dataset){
				sbiModule_restServices.promiseGet("1.0/datasets/id", document.dataset)
				.then(function(response) {
					//console.log(response);
					$scope.openEditDialog(document.label,response.data,document.typeCode,document.id);
				},function(response){

					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.error(response.data, sbiModule_translate.load('sbi.workspace.dataset.load.error'), $scope.toasterConfig);

				});
			}else{

				$scope.openEditDialog(document.label,"",document.typeCode,document.id);
			}
		}

		$scope.uploadPreviewFile = function (selectedDocument) {
			$mdDialog.show({
				scope:$scope,
				preserveScope: true,
				controller: UploadPreviewFileController,
				templateUrl: sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/workspace/templates/analysisUploadPreviewFile.html',
				clickOutsideToClose: false,
				escapeToClose :true,
				//fullscreen: true,
				locals:{
					// previewDatasetModel:$scope.previewDatasetModel,
					// previewDatasetColumns:$scope.previewDatasetColumns
				}
			});
		}


		$scope.openEditDialog=function(doclabel,dsLabel,docType,docId){
			$mdDialog.show({
				scope:$scope,
				preserveScope: true,
				controller: $scope.editControllers[docType],
				templateUrl: sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',
				clickOutsideToClose:true,
				escapeToClose :true,
				fullscreen: true,
				locals:{
					datasetLabel:dsLabel,
					documentLabel:doclabel,
					documentId:docId
				}
			});
		}
		/**
		 * The immediate Run (preview) button functionality for the Analysis documents (for List view of documents).
		 */
		$scope.analysisSpeedMenu =
			[
				{
					label: sbiModule_translate.load('sbi.generic.run'),
					icon:'fa fa-play-circle' ,
					backgroundColor:'transparent',

					action: function(item,event) {
						$scope.executeDocument(item);
					}
				}
				];

		$scope.getFolders = function() {
			sbiModule_restServices.promiseGet("2.0/functionalities/forsharing", $scope.docForSharing).then(		
					function(response) {
						for(var i=0; i<response.data.length; i++){
							response.data[i].expanded=true;

						}
						$scope.folders = angular.copy(response.data);

						$mdDialog.show({
							scope:$scope,
							preserveScope: true,
							controller: ShareDocumentrController,
							templateUrl: sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/workspace/templates/folderTreeTemplateAnalysis.html',
							clickOutsideToClose:false,
							escapeToClose :false,
							locals:{
								doc:document
							}
						});
					},
					function(response) {
						sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
					});
		};

		$scope.shareDocument=function(document){
			$scope.docForSharing = document.id;
			if(document.functionalities.length>1){
				var json = {docId:$scope.docForSharing, isShare:false, functs:[]};

				sbiModule_restServices
				.promisePost("documents","share?"+$httpParamSerializer(json))
				.then(
						function(response) {

							$mdDialog.cancel();
							$scope.loadAllMyAnalysisDocuments();
							if(json.isShare==true) {
								toastr.success(sbiModule_translate.load('sbi.browser.document.share.success'),
										sbiModule_translate.load('sbi.generic.success'), $scope.toasterConfig);
							} else {
								toastr.success(sbiModule_translate.load('sbi.browser.document.unshare.success'),
										sbiModule_translate.load('sbi.generic.success'), $scope.toasterConfig);
							}


						},

						function(response) {

							toastr.error(response.data, sbiModule_translate.load('sbi.browser.document.clone.error'), $scope.toasterConfig);

						}
				);
			} else {
				$scope.getFolders();
			}


		}

		function UploadPreviewFileController($scope,$mdDialog) {
			$scope.closePreviewFileUploadDialog = function () {
				$mdDialog.cancel();
				$scope.previewFile = {};
			}

			$scope.uploadFile = function () {
				console.log($scope.previewFile)
				console.log($scope.selectedDocument)

				multipartForm.post("2.0/analysis/"+$scope.selectedDocument.id,$scope.previewFile)
				.then(function(response) {
					console.log("[POST]: SUCCESS!");
					$scope.loadAllMyAnalysisDocuments();
					$mdDialog.cancel();
					$scope.previewFile = {};
					toastr.success(sbiModule_translate.load("sbi.workspace.analysis.upload.preview.file.success")+" "+response.data.fileName,
							sbiModule_translate.load('sbi.generic.success'), $scope.toasterConfig);

				}, function(response) {
					toastr.error(sbiModule_translate.load(response.data.errors[0].message),
							sbiModule_translate.load('sbi.generic.error'), $scope.toasterConfig);

				});
			}
		}

		function  ShareDocumentrController($scope,$mdDialog,doc){

			$scope.closeDocumentTree = function () {
				$mdDialog.cancel();
			}

			$scope.shareDoc = function () {
				if($scope.selectedItems.length==0){
					toastr.info(sbiModule_translate.load('sbi.workspace.share.no.document.selected'),
							sbiModule_translate.load('sbi.federationdefinition.info'), $scope.toasterConfig);
				} else {
					$scope.foldersToShare = [];
					for (var item in $scope.selectedItems) {
						$scope.foldersToShare.push($scope.selectedItems[item].id);
					}

					var json = {docId:$scope.docForSharing, isShare:true, functs: $scope.foldersToShare};

					sbiModule_restServices
					.promisePost("documents","share?"+$httpParamSerializer(json))
					.then(
							function(response) {

								$mdDialog.cancel();
								$scope.loadAllMyAnalysisDocuments();
								toastr.success(sbiModule_translate.load('sbi.browser.document.share.success'),
										sbiModule_translate.load('sbi.generic.success'), $scope.toasterConfig);

							},

							function(response) {

								toastr.error(response.data, sbiModule_translate.load('sbi.browser.document.clone.error'), $scope.toasterConfig);

							}
					);
				}

			}

		}

		function CreateNewAnalysisController($scope,$mdDialog){
			$scope.iframeUrl = datasetParameters.cockpitServiceUrl + '&SBI_ENVIRONMENT=WORKSPACE&IS_TECHNICAL_USER=' + sbiModule_user.isTechnicalUser + "&documentMode=EDIT";
			$scope.cancelDialog = function() {
				$scope.loadAllMyAnalysisDocuments();
				$mdDialog.cancel();
			}
		}

		function CreateNewGeoMapController($scope,$mdDialog){
			//console.log(sbiModule_user.isTechnicalUser);

			$scope.iframeUrl = datasetParameters.georeportServiceUrl+'&SBI_ENVIRONMENT=WORKSPACE&IS_TECHNICAL_USER='+ sbiModule_user.isTechnicalUser+"&DATASET_LABEL="+'';

			$scope.cancelMapDesignerDialog = function() {
				$scope.loadAllMyAnalysisDocuments();
				$mdDialog.cancel();
			}


		}
		
		function CreateNewKPIController($scope,$mdDialog){
			console.log("hello from CreateNewKPIController");

			$scope.iframeUrl = datasetParameters.kpiServiceUrl+'&SBI_ENVIRONMENT=WORKSPACE';

			$scope.cancelDialog = function() {
				
				$mdDialog.cancel();
			}


		}
		
		$scope.editControllers = {};
		
		$scope.editControllers.KPI = function EditNewKPIController($scope,$mdDialog,datasetLabel,documentLabel,documentId){
			console.log("hello from CreateNewKPIController");

			$scope.iframeUrl = datasetParameters.kpiServiceUrl+'&SBI_ENVIRONMENT=WORKSPACE&IS_TECHNICAL_USER='+ sbiModule_user.isTechnicalUser+'&document='+documentId+'&DOCUMENT_NAME='+documentLabel;

			$scope.cancelDialog = function() {
				
				$mdDialog.cancel();
			}


		}
		
		$scope.editControllers.MAP = function EditGeoMapController($scope,$mdDialog,datasetLabel,documentLabel){
			//console.log(sbiModule_user.isTechnicalUser);

			$scope.iframeUrl = datasetParameters.georeportServiceUrl+'&SBI_ENVIRONMENT=WORKSPACE&IS_TECHNICAL_USER='+ sbiModule_user.isTechnicalUser+'&DOCUMENT_LABEL='+documentLabel+'&DATASET_LABEL='+datasetLabel;

			$scope.cancelMapDesignerDialog = function() {
				$scope.loadAllMyAnalysisDocuments();
				$mdDialog.cancel();
			}


		}
		

		

		if(initialOptionMainMenu){
			if(initialOptionMainMenu.toLowerCase() == 'analysis'){
				var selectedMenu = $scope.getMenuFromName('analysis');
				$scope.leftMenuItemPicked(selectedMenu,true);
			}
		}

	}
})();