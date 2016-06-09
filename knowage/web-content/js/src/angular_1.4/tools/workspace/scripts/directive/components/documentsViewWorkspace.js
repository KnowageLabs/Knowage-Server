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

angular
	.module('documents_view_workspace', [])

	.directive('documentsViewWorkspace', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/documentsViewWorkspace.html',
		      controller: documentsController
		  };
	})

function documentsController($scope,sbiModule_restServices,sbiModule_translate,$window,$mdSidenav,$mdDialog,sbiModule_messaging,sbiModule_config, $documentViewer ){

	$scope.selectedDocument = undefined;
	$scope.showDocumentInfo = false;
	$scope.folders = [];
	$scope.foldersToShow=[];
	$scope.breadModel=[];
	$scope.breadCrumbControl;
	$scope.documentsOfSelectedFolder=[];
	
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

	$scope.selectOrganizerDocument= function ( document ) {
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
	
	$scope.deleteFolder = function(folder) {
		var confirm = $mdDialog.confirm()
		.title(sbiModule_translate.load("sbi.workspace.folder.delete.confirm.dialog"))
		.content(sbiModule_translate.load("sbi.workspace.folder.delete.confirm"))
		.ariaLabel('delete folder') 
		.ok(sbiModule_translate.load("sbi.general.yes"))
		.cancel(sbiModule_translate.load("sbi.general.No"));
			$mdDialog.show(confirm).then(function() {
				sbiModule_restServices.promiseDelete("2.0/organizer/folders",folder.functId)
				.then(function(response) {
					$scope.loadAllFolders();
				},function(response) {
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
				});
		});
	}
	
	$scope.loadAllFolders = function() {
		sbiModule_restServices.promiseGet("2.0/organizer/folders","")
		.then(function(response) {
			angular.copy(response.data,$scope.folders); // all folders
			$scope.loadFolderContent();
			console.info("[LOAD END]: Loading of users folders is finished.");
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	

	
	$scope.openFolder = function(folder){
		//console.log(folder);
		$scope.selectOrganizerDocument(undefined);
		$scope.breadModel.push(folder);
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
		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/workspace/templates/createFolderDialog.html',  
		      clickOutsideToClose:false,
		      escapeToClose :false
		 });
	}
	
	function AddNewFolderController($scope,$mdDialog,$http){
		
		$scope.createFolder = function() {
			
			$scope.folder = {
				    "parentFunct": $scope.selectedFolder.functId,
				    "code": $scope.folder.code,
				    "name": $scope.folder.name,
				    "descr": $scope.folder.descr,
				    "path": $scope.selectedFolder.path+"/"+$scope.folder.code,
				    "prog": 1
				  }
			if($scope.folder.descr==null){
				$scope.folder.descr="";
			}
			sbiModule_restServices.promisePost('2.0/organizer/folders','',angular.toJson($scope.folder))
			.then(function(response) {
				console.log("[POST]: SUCCESS!");
				$mdDialog.cancel();
				$scope.clearForm();
		        $scope.loadAllFolders();
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});
			
		}
		
		$scope.closeDialog = function() {
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
		
		//search for the root
		for ( i = 0; i < $scope.folders.length; i++) {
			if($scope.folders[i].parentFunct==null){
			
				$scope.openFolder($scope.folders[i]);
				$scope.loadDocumentsForFolder($scope.folders[i]);
			}
		}
			
	}
	
	$scope.moveBreadCrumbToFolder=function(item,index){
	      
		$scope.openFolder(item);
		
	}
	
	$scope.loadDocumentsForFolder=function(folder){
		$scope.documentsOfSelectedFolder=[];
		if(folder != undefined){
		sbiModule_restServices.promiseGet("2.0/organizer/documents",folder.functId)
		.then(function(response) {
			angular.copy(response.data,$scope.documentsOfSelectedFolder); // all folders
			console.info("[LOAD END]: Loading of documents.");
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
		}
	}
	
	$scope.executeDocumentFromOrganizer=function(document){
		console.log(document);
		console.info("[EXECUTION]: Execution of document with the label '" + document.label + "' is started.");		
		$documentViewer.openDocument(document.biObjId, document.documentLabel, document.documentName);
	}
	
	$scope.deleteDocumentFromOrganizer = function(document) {
		var confirm = $mdDialog.confirm()
		.title(sbiModule_translate.load("sbi.workspace.folder.delete.confirm.dialog"))
		.content(sbiModule_translate.load("sbi.workspace.organizer.document.delete.confirm"))
		.ariaLabel('delete documentOrganizer') 
		.ok(sbiModule_translate.load("sbi.general.yes"))
		.cancel(sbiModule_translate.load("sbi.general.No"));
			$mdDialog.show(confirm).then(function() {
				sbiModule_restServices.promiseDelete("2.0/organizer/documents/"+document.functId,document.biObjId)
				.then(function(response) {
					$scope.loadDocumentsForFolder($scope.selectedFolder);
					$scope.selectOrganizerDocument(undefined);
				},function(response) {
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
				});
		});
	}
	
	$scope.moveDocumentToFolder=function(document){
		console.log("moveee it move it");
	}
}
