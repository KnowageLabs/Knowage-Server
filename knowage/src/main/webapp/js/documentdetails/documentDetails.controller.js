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
    'use strict';

    angular
        .module('DocumentDetails', ['ngMaterial', 'jsonFormatter','sbiModule', 'componentTreeModule', 'file_upload','DriversModule','TemplateModule', 'OutputParametersModule', 'DataLineageModule', 'SubreportsModule'])
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
		self.docId = documentService.documentId;
		$scope.$broadcast('setDocumentPath', requiredPath);
        self.cancelFunction = function() {
        	window.parent.angular.element(window.frameElement).scope().closeDialogFromExt();
        };

        self.savingFunction = function(){

        	persistDocument();

        	if(document.id != undefined){
        	DriversService.persistDrivers(document.id,requiredPath);
        	DriversService.deleteDrivers(document.id,requiredPath);

        	DriversService.persistDataDependency(document.id,requiredPath);
        	DriversService.deleteDataDependencies(document.id,requiredPath);

        	DriversService.persistVisualDependency(document.id,requiredPath);
        	DriversService.deleteVisualDependencies(document.id,requiredPath);
        	}
        	outputParametersService.persistOutputParameters();
        	outputParametersService.deleteOutputParameters();

        	dataLineageService.persistTables();
			dataLineageService.deleteTables();

        	templateService.uploadTemplate();
        	templateService.setActiveTemplate();
        	templateService.deleteTemplates();

        	subreportsService.deleteSubreports();
			subreportsService.persistSubreports();

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
      	   prepareDocumentForPersisting(document);
      	   setFoldersPath();
         	if(document.id){
         		resourceService.put(documentService.requiredPath,document.id,document).then(function(response){
         			if(response.data.errors){
         				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
         			}else
         			sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.documentupdated"), 'Success!');
         			documentService.document = response.data;
         			document = response.data;
         			uploadImage();
         		});
         	} else{
         		resourceService.post(documentService.requiredPath,"",document).then(function(response){
         			if(response.data.errors){
         				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Failure!!!');
         			}else
         			sbiModule_messaging.showInfoMessage(self.translate.load("sbi.documentdetails.toast.documentcreated"), 'Success!');
         			documentService.document = response.data;
         			DriversService.setDriverRelatedObject(response.data);
         			document = response.data;
         			self.docId = response.data.id;
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

       closingIFrame.close();

   };

})();