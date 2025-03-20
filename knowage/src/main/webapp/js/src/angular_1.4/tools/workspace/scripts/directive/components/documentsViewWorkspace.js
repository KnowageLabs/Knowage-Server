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

(function() {

	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

angular
	.module('documents_view_workspace', [])

	.directive('documentsViewWorkspace', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: currentScriptPath + '../../../templates/documentsViewWorkspace.html',
		      controller: documentsController
		  };
	})

function documentsController($scope, sbiModule_restServices, sbiModule_translate, $window, $mdSidenav, $mdDialog,
			sbiModule_config, $documentViewer, toastr, sbiModule_i18n){

	$scope.selectedDocument = undefined;
	$scope.showDocumentInfo = false;
	$scope.folders = [];
	$scope.foldersForTree=[];
	$scope.foldersToShow=[];
	$scope.breadModel=[];
	$scope.breadCrumbControl;
	$scope.documentsOfSelectedFolder=[];
	$scope.documentsOfSelectedFolderInitial=[];

	//if(!$scope.i18n.isLoaded()){
	//	$scope.i18n.loadI18nMap();
	//}

	// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	$scope.documentsFromAllFolders=[];

	$scope.destFolder=undefined;
	$scope.selectedFolder=undefined;

	$scope.showDocumentDetails = function() {
		return $scope.showDocumentInfo && $scope.isSelectedDocumentValid();
	};

	$scope.isSelectedDocumentValid = function() {
		return $scope.selectedDocument !== undefined;
	};

	$scope.setDocumentDetailOpen = function(isOpen) {
		if (isOpen && !$mdSidenav('rightDoc').isLockedOpen() && !$mdSidenav('rightDoc').isOpen()) {
			$scope.toggleDocumentDetail();
		}

		$scope.showDocumentInfo = isOpen;
	};

	$scope.toggleDocumentDetail = function() {
		$mdSidenav('rightDoc').toggle();
	};

	$scope.selectDocument= function ( document ) {
		if (document !== undefined) {
			$scope.lastDocumentSelected = document;
		}
		var alreadySelected = (document !== undefined && $scope.selectedDocument === document);
		$scope.selectedDocument = document;
		if (alreadySelected) {
			$scope.selectedDocument=undefined;
			$scope.setDocumentDetailOpen(!$scope.showDocumentDetail);
		} else {
			$scope.setDocumentDetailOpen(document !== undefined);
		}
	};


	$scope.showOrganizerDocumentDetails = function() {
		return $scope.showOrganizerDocumentInfo && $scope.isSelectedOrganizerDocumentValid();
	};


	$scope.isSelectedOrganizerDocumentValid = function() {
		return $scope.selectedOrganizerDocument !== undefined;
	};

	$scope.setOrganizerDocumentDetailOpen = function(isOpen) {
		if (isOpen && !$mdSidenav('rightOrganizer').isLockedOpen() && !$mdSidenav('rightOrganizer').isOpen()) {
			$scope.toggleOrganizerDocumentDetail();
		}

		$scope.showOrganizerDocumentInfo = isOpen;
	};

	$scope.toggleOrganizerDocumentDetail = function() {
		$mdSidenav('rightOrganizer').toggle();
	};

	$scope.selectOrganizerDocument = function ( document ) {
		if (document !== undefined) {
			$scope.lastDocumentSelected = document;
		}
		var alreadySelected = (document !== undefined && $scope.selectedOrganizerDocument === document);
		$scope.selectedOrganizerDocument = document;
		if (alreadySelected) {
			$scope.selectedOrganizerDocument=undefined;
			$scope.setOrganizerDocumentDetailOpen(!$scope.showOrganizerDocumentDetail);
		} else {
			$scope.setOrganizerDocumentDetailOpen(document !== undefined);
		}
	};



	$scope.tableSpeedMenuOption = [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,
		backgroundColor:'transparent',
		action : function(item,event) {
			$scope.deleteFolder(item);
		}
	} ];

	$scope.organizerSpeedMenu=[{
		label : sbiModule_translate.load('sbi.generic.run'),
		icon:'fa fa-play-circle' ,
		backgroundColor:'transparent',
		action : function(item,event) {
			$scope.executeDocumentFromOrganizer(item);
		}
	} ];

	$scope.deleteFolder = function(folder) {
		var confirm = $mdDialog.confirm()
						.title(sbiModule_translate.load("sbi.workspace.delete.confirm.title"))
						.content(sbiModule_translate.load("sbi.workspace.folder.delete.confirm"))
						.ariaLabel('delete folder')
						.ok(sbiModule_translate.load("sbi.general.yes"))
						.cancel(sbiModule_translate.load("sbi.general.No"));
							$mdDialog.show(confirm).then(function() {
								sbiModule_restServices.promiseDelete("2.0/organizer/foldersee",folder.functId)
								.then(function(response) {

									$scope.loadAllFolders();

									// Take the toaster duration set inside the main controller of the Workspace. (danristo)
									toastr.success(sbiModule_translate.load('sbi.workspace.folder.delete.success'),
											sbiModule_translate.load('sbi.generic.success'), $scope.toasterConfig);

								},function(response) {

									/**
									 * Provide a toast with an error message that informs user that he cannot delete a folder. The reason for that could be
									 * at least one subfolder or at least one document that the folder contains.
									 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
									 */
									// Take the toaster duration set inside the main controller of the Workspace. (danristo)
									toastr.error(sbiModule_translate.load('sbi.workspace.organizer.folder.error.delete'),
											sbiModule_translate.load("sbi.generic.error"), $scope.toasterConfig);

								});
						});
	}

	$scope.loadAllFolders = function() {
		sbiModule_restServices.promiseGet("2.0/organizer/folders","")
		.then(function(response) {
			angular.copy(response.data,$scope.folders); // all folders

			$scope.i18n.loadI18nMap().then(function() {

				for (var i = 0 ; i < $scope.folders.length; i ++ ){
					$scope.folders[i].name = $scope.i18n.getI18n($scope.folders[i].name);
				}

				//angular.copy(response.data,$scope.foldersForTree);
				$scope.convertTimestampToDateFolders();
				$scope.loadFolderContent();
				$scope.loadDocumentsForFolder($scope.selectedFolder);
				console.info("[LOAD END]: Loading of users folders is finished.");
			}); // end of load I 18n
		},function(response){

			// Take the toaster duration set inside the main controller of the Workspace. (danristo)
			toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);

		});
	}

	$scope.convertTimestampToDateFolders = function(){
		for (var i = 0; i < $scope.folders.length; i++) {
			var timestamp = $scope.folders[i].timeIn;
			var date = new Date(timestamp);
			var dateString = date.toLocaleString();
			$scope.folders[i].timeIn = dateString;
		}
	}

	$scope.openFolder = function(folder){
		//console.log(folder);
		$scope.selectOrganizerDocument(undefined);

		/**
		 * Only for the need of the breadcrumb in the Organizer, rename the root folder that is originally name "root"
		 * with the value of "Home". This will not change the DB value (the persisted value) of the root folder name.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if (folder.name=="root") {
			folder.name = "Home";
		}

		//$scope.breadModel.push(folder);
		$scope.breadCrumbControl.insertBread(folder);
		$scope.selectedFolder=folder;

		$scope.foldersToShow=[];
		if($scope.selectedFolder!= undefined){
			for ( i = 0; i < $scope.folders.length; i++) {
				if($scope.folders[i].parentFunct==$scope.selectedFolder.functId){
					$scope.foldersToShow.push($scope.folders[i]);
				}
			}
		}
		$scope.loadDocumentsForFolder(folder);
	}

	$scope.addNewFolder  = function(){
		$mdDialog.show({
			  scope:$scope,
			  preserveScope: true,
		      controller: AddNewFolderController,
		      templateUrl: sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/workspace/templates/createFolderDialog.html',
		      clickOutsideToClose:false,
		      escapeToClose :false
		 });
	}

	function AddNewFolderController($scope,$mdDialog,$http){

		$scope.createFolder = function() {

			/**
			 * The Code value is encoded for the 'path' parameter, since it can contain special characters (such as \,/,?,%,@ etc.), as well as Name can.
			 * The reason we do this is because the path parameter on the server side could be read right, EVEN THOUGH this path parameter is not ever
			 * used in the current implementation (not for getting existing folders and files, neither for posting new ones, nor for deleting existing
			 * ones). All operations are performed with the ID of the folder and document, so Code/Name are not interfering ever in this process. Hence,
			 * the encoding is done for preventing problems in some future implementation when this parameter could be used when performing such operations.
			 *
			 * NOTE: Another approach, which bypasses the encoding of the path, is a preventing the user of specifying such special characters when creating
			 * a new folder. This functionality is performed in the old interface - Functionalities management.
			 *
			 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			$scope.folder = {
				    "parentFunct": $scope.selectedFolder.functId,
				    "code": $scope.folder.code,
				    "name": $scope.folder.name,
				    "descr": $scope.folder.descr,
				    "path": $scope.selectedFolder.path+"/"+encodeURIComponent($scope.folder.code),
				    "prog": 1
				  }
			if($scope.folder.descr==null){
				$scope.folder.descr="";
			}
			sbiModule_restServices.promisePost('2.0/organizer/foldersee','',angular.toJson($scope.folder))
			.then(function(response) {
				console.log("[POST]: SUCCESS!");
				$mdDialog.cancel();
				$scope.clearForm();
		        $scope.loadAllFolders();

		        // Take the toaster duration set inside the main controller of the Workspace. (danristo)
		        toastr.success(sbiModule_translate.load("sbi.workspace.folder.add.success.msg"),
		        		sbiModule_translate.load('sbi.generic.success'), $scope.toasterConfig);

			}, function(response) {

				/**
				 * Added the part of the code for translation of the coded error message that comes from the server side when the creation of
				 * new folder is not possible.
				 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				// Take the toaster duration set inside the main controller of the Workspace. (danristo)
				toastr.error(sbiModule_translate.load(response.data.errors[0].message),
						sbiModule_translate.load('sbi.generic.error'), $scope.toasterConfig);

			});

		}

		$scope.closeDialog = function() {

			/**
			 * When closing a form, clear its content. This should be necessary, because the user could leave the form filled with data and just close it
			 * without saving it.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			$scope.clearForm();

			$mdDialog.cancel();

		}

		$scope.clearForm = function() {
			$scope.folder = {
				    "parentFunct": "",
				    "code": "",
				    "name": "",
				    "descr": "",
				    "path": "",
				    "prog": 1
	}
		}

	}

	$scope.loadFolderContent=function(){
		if($scope.selectedFolder==undefined){
			$scope.showRoot();
		}else{
			//$scope.openFolder($scope.selectedFolder);
			$scope.foldersToShow=[];
			if($scope.selectedFolder!= undefined){

				for ( i = 0; i < $scope.folders.length; i++) {
					if($scope.folders[i].parentFunct==$scope.selectedFolder.functId){
						$scope.foldersToShow.push($scope.folders[i]);
					}
				}
			}
		}


	}

	$scope.showRoot=function(){
		//$scope.breadModel=[];

		//search for the root
		for ( i = 0; i < $scope.folders.length; i++) {
			if($scope.folders[i].parentFunct==null){
				$scope.openFolder($scope.folders[i]);
				$scope.loadDocumentsForFolder($scope.folders[i]);
			}
		}

	}

	$scope.moveBreadCrumbToFolder=function(item,index){
	     //console.log($scope.breadModel);
		//$scope.openFolder(item);
		$scope.selectedFolder=item;
		$scope.loadFolderContent();
		$scope.loadDocumentsForFolder($scope.selectedFolder);

		/**
		 * When moving through the folder structure via the breadcrumb in the Organizer, hide the document detail if shown. For example, if the user is
		 * inside the root/Folder1 and it selects the document inside of it, the right-side navigation panel will appear with the document details. If
		 * the user then moves to the 'root' folder, the detail panel should be hidden.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.showOrganizerDocumentInfo = false;
	}

	$scope.loadDocumentsForFolder=function(folder){

		$scope.documentsOfSelectedFolder=[];

		if(folder != undefined) {

			sbiModule_restServices.promiseGet("2.0/organizer/documents",folder.functId)
				.then(function(response) {

					angular.copy(response.data,$scope.documentsOfSelectedFolder);
					angular.copy(response.data,$scope.documentsOfSelectedFolderInitial);

					/**
					 * Only the 'root' folder has the parent folder that is NULL. Only when having this one, we can provide
					 * collecting of all documents that are inside the Organizer (in the root or in some folder).
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					if (folder.parentFunct == null) {
						$scope.loadAllOrganizerDocuments();
					}

					console.info("[LOAD END]: Loading of documents.");

				},function(response){

					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);

				});
		}
	}

	// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	$scope.loadAllOrganizerDocuments = function() {

		sbiModule_restServices.promiseGet("2.0/organizer/documents","")
			.then(
					function(response) {
						angular.copy(response.data,$scope.documentsFromAllFolders);
						console.info("[LOAD END]: Loading of all documents from all Organizer folders.");
					},

					function(response){

						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);

					}
				);

	}

	$scope.executeDocumentFromOrganizer=function(document){

		console.info("[EXECUTION]: Execution of document with the label '" + document.label + "' is started.");

		/**
		 * When opening (executing) a document from the Organizer of the Workspace, notify the document executing JSP page that
		 * we are coming from the Organizer, so the page can hide the "Add to my workspace" option on the menu that is available
		 * on the page where document is executed. This option, when picked, creates a label to the executed document inside the
		 * Organizer of the Workspace and since we (in this case) execute it from the Organizer, there is no need for this menu
		 * option. For that reason, we send parameter with the "WORKSPACE_ORGANIZER" value to the 'openDocument' function. The
		 * $scope variable is sent to the remote function, since we need it there to rise an "documentClosed" event that will cause
		 * the right-side detail panel to close, if previously opened. This operation is necessary, since user can potentially
		 * modify the document (e.g. a cockpit document) that is executed from the Organizer (e.g. document's label that is shown
		 * in the detail panel could be changed). For the same reason, reload all folders and their documents.
		 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$documentViewer.openDocument(document.biObjId,document.documentLabel,document.documentName,$scope,"WORKSPACE_ORGANIZER");

		$scope.$on("documentClosed", function() { $scope.hideRightSidePanel(); $scope.loadAllFolders(); })

	}

	$scope.deleteDocumentFromOrganizer = function(document) {
		var confirm = $mdDialog.confirm()
		.title(sbiModule_translate.load("sbi.workspace.remove.confirm.title"))
		.content(sbiModule_translate.load("sbi.workspace.organizer.document.remove.confirm"))
		.ariaLabel('delete documentOrganizer')
		.ok(sbiModule_translate.load("sbi.general.yes"))
		.cancel(sbiModule_translate.load("sbi.general.No"));
			$mdDialog.show(confirm).then(function() {
				sbiModule_restServices.promiseDelete("2.0/organizer/documents/"+document.functId,document.biObjId)
				.then(function(response) {

					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.success(sbiModule_translate.load('sbi.workspace.organizer.document.remove.success'),
							sbiModule_translate.load('sbi.generic.success'), $scope.toasterConfig);

					$scope.selectOrganizerDocument(undefined);

					if ($scope.searchInput!="") {
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
						 * Set the indicator of state of searching (we are not searching in the Organizer anymore) so the breadcrumb could be showed again
						 * on the top of the Organizer (Documents) interface.
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						$scope.organizerSearch = false;

					}
					else {
						$scope.loadDocumentsForFolder($scope.selectedFolder);
					}


				},function(response) {

					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);

				});
		});
	}

	$scope.moveDocumentToFolder=function(document){

		/**
		 * Only for the need of the tree structure of folders in the Organizer (when moving a document into some folder),
		 * rename the root folder that is originally name "root" with the value of "Home". This will not change the DB value
		 * (the persisted value) of the root folder name.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var folders = $scope.folders;

		for (i=0; i<$scope.folders.length; i++) {
			if ($scope.folders[i].name=="root") {
				$scope.folders[i].name = "Home";
				break;
			}
		}

		angular.copy($scope.folders,$scope.foldersForTree);
		$mdDialog.show({
			  scope:$scope,
			  preserveScope: true,
		      controller: MoveDocumentToFolderController,
		      templateUrl: sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/workspace/templates/folderTreeTemplate.html',
		      clickOutsideToClose:false,
		      escapeToClose :false,
		      locals:{
		    	 doc:document
		      }
		    });

	}

	function  MoveDocumentToFolderController($scope,$mdDialog,doc){
//		console.log($scope.foldersForTree);
		$scope.closeFolderTree=function(){
			$scope.destFolder=undefined;
    		$mdDialog.cancel();
    	}


		$scope.keys={
				'id':'functId',
				'parentId':'parentFunct'
		};

//		$scope.setDestinationFolder=function(item){
//
//			$scope.destFolder=item;
//		}

		$scope.executeMovingDocument=function(){
			if($scope.destFolder!=undefined){
				if($scope.selectedFolder.functId==$scope.destFolder.functId){

					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.success(sbiModule_translate.load('sbi.workspace.organizer.move.to.same.destination.folder'),
							sbiModule_translate.load('sbi.generic.info'), $scope.toasterConfig);

				}else{
					sbiModule_restServices.promisePut("2.0/organizer/documentsee/"+doc.biObjId+"/"+$scope.selectedFolder.functId,$scope.destFolder.functId)
					.then(function(response) {

						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						toastr.success(sbiModule_translate.load('sbi.workspace.organizer.folder.move.success'),
								sbiModule_translate.load('sbi.generic.success'), $scope.toasterConfig);

						$scope.loadDocumentsForFolder($scope.selectedFolder);
						$scope.selectOrganizerDocument(undefined);
						$scope.closeFolderTree();
					},function(response) {

						/**
						 * Provide a toast with an error message that informs user that he cannot delete a folder. The reason for that could be
						 * at least one subfolder or at least one document that the folder contains.
						 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						toastr.error(sbiModule_translate.load(response.data.errors[0].message),
								sbiModule_translate.load("sbi.generic.error"), $scope.toasterConfig);

					});
				}
			}

		}

	}

	if(initialOptionMainMenu){
		if(initialOptionMainMenu.toLowerCase() == 'documents'){
			var selectedMenu = $scope.getMenuFromName('documents');
			$scope.leftMenuItemPicked(selectedMenu,true);
		}
	}
}

})();