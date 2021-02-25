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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
	agGrid.initialiseAgGridWithAngular1(angular);
    'use strict';

    angular
        .module('DocumentDetails', ['ngMaterial', 'jsonFormatter','sbiModule', 'componentTreeModule', 'file_upload','DriversModule','TemplateModule', 'OutputParametersModule', 'DataLineageModule', 'SubreportsModule', 'agGrid', 'ngMessages', 'angularXRegExp'])
        .config(['$mdThemingProvider','$locationProvider','$httpProvider', function($mdThemingProvider,$locationProvider,$httpProvider) {
            $mdThemingProvider.theme('knowage')
            $mdThemingProvider.setDefaultTheme('knowage');
            $httpProvider.interceptors.push('httpInterceptor');
            $locationProvider.html5Mode({
            	  enabled: true,
            	  requireBase: false
            	});
        }])
        .controller('DocumentDetailsController',['$scope','$filter','DriversService','DocumentService','templateService','outputParametersService','dataLineageService','subreportsService','closingIFrame','$location','resourceService','multipartForm','$mdDialog', 'sbiModule_restServices', 'sbiModule_translate', 'sbiModule_messaging', DocumentDetailsController])

    function DocumentDetailsController($scope,$filter,DriversService,DocumentService,templateService,outputParametersService,dataLineageService,subreportsService,closingIFrame,$location,resourceService,multipartForm,$mdDialog,sbiModule_restServices,sbiModule_translate,sbiModule_messaging) {
        var self = this;
        var documentService = DocumentService;
        self.translate = sbiModule_translate;
        var requiredPath = documentService.requiredPath;
        var paruses = documentService.driverParuses;
        self.title = "Document Details";
        var template = templateService.template;
        var document = documentService.document;
        DriversService.setDriverRelatedObject(document);
        if(document.id != undefined){
		DriversService.getDriversOnRelatedObject(requiredPath,document.id + "/drivers");
        }
		documentService.drivers = DriversService.driversOnObject
        self.analyticalDrivers = DriversService.analyticalDrivers;
        self.lovIdAndColumns = DriversService.lovIdAndColumns;
        var documentBasePath =""+ document.id;
        var driverPostBasePath = document.id + '/drivers';
		self.typeCode = documentService.document.typeCode;
		self.engine = documentService.document.engine;
		self.docId = documentService.documentId;
		$scope.$broadcast('setDocumentPath', requiredPath);
        self.cancelFunction = function() {
        	window.parent.angular.element(window.frameElement).scope().closeDialogFromExt(true);
        };

		self.savingFunction = function(){

			if(documentService.document.id != undefined){
				DriversService.persistDrivers(documentService.document.id,requiredPath);
				DriversService.deleteDrivers(documentService.document.id,requiredPath);
	
//				DriversService.persistDataDependency(documentService.document.id,requiredPath);
//				DriversService.deleteDataDependencies(documentService.document.id,requiredPath);
	
				DriversService.persistVisualDependency(documentService.document.id,requiredPath);
				DriversService.deleteVisualDependencies(documentService.document.id,requiredPath);
			}

			if(DocumentService.document.outputParameters) {
				outputParametersService.persistOutputParameters();
				outputParametersService.deleteOutputParameters();
			}

			dataLineageService.persistTables();
			dataLineageService.deleteTables();

			persistDocument().then(function() {
				templateService.uploadTemplate();
				templateService.setActiveTemplate();
				templateService.deleteTemplates();
	
				subreportsService.deleteSubreports();
				subreportsService.persistSubreports();
			}).then(function() {
				$scope.$root.$broadcast("RefreshTemplates", "");
			});

		};


        var uploadImage = function() {
        	if(documentService.previewFile.file) {
        		var templateUploadBasePath = document.id + '/image';
        		multipartForm.post(documentService.requiredPath +"/"+ templateUploadBasePath, documentService.previewFile).then(function(response){
  				  getImage();
  			});
        	}
        };
        var getImage = function(){
        	resourceService.get(documentService.requiredPath,document.id + '/image').then(function(response){
        		 documentService.documentImage = response.data;
           	});
        }

        var prepareDocumentForPersisting = function(document){
        	delete document.dataSetLabel;
    		delete document.creationDate;
    		document.refreshSeconds = parseInt(document.refreshSeconds);
        };


		var persistDocument = function(){
			prepareDocumentForPersisting(documentService.document);
			setFoldersPath();
			if(documentService.document.id || document.id){
				return resourceService.put(documentService.requiredPath,documentService.document.id,documentService.document).then(function(response){
					if(response.data.errors){
						sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
					} else {
						sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.documentupdated"), 'Success!');
					}
					documentService.document = response.data;
					self.typeCode = response.data.typeCode;
					self.engine = response.data.engine;
					uploadImage();
				});
			} else {
				return resourceService.post(documentService.requiredPath,"",document).then(function(response){
					if(response.data.errors){
						sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
					} else {
						sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.documentcreated"), 'Success!');
					}
					documentService.document = response.data;
					DriversService.setDriverRelatedObject(response.data);
					self.docId = response.data.id;
					self.typeCode = response.data.typeCode;
					self.engine = response.data.engine;
					uploadImage();
				});
			}
		};

         var setFoldersPath = function(){
      	   document.functionalities=[];
    		 for(var i = 0; i<documentService.folders.length; i++ ){
    			 if(documentService.folders[i].checked){
    				 document.functionalities.push(documentService.folders[i].path);
    		 }
    	}
     };

     self.getAllDocuments = function() {
    	 var docBasePath = "";
    	  resourceService.get(requiredPath, docBasePath)
    	  .then(function(response) {
    		  documentService.documentsList = response.data;
    		  self.setChecked();
    		  console.log(response);
    	  }, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			 });
      }

	self.setChecked = function() {
		for(var i = 0; i < documentService.documentsList.length; i++) {
			for(var j = 0; j < documentService.savedSubreports.length; j++) {
				if(documentService.documentsList[i].id == documentService.savedSubreports[j].sub_rpt_id) {
					documentService.documentsList[i].wanted = true;
				}
			}
		}
		return documentService.documentsList;
	}

	self.noSelectedFunctionalities = function() {
		return documentService.folders
			.filter(function(e) { return e.checked; })
			.length == 0;
	}
	
	closingIFrame.close();

	};

})();