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
(function () {
angular
        .module('DocumentDetails')
        .controller('DocumentDetailsInformationsController',['$scope','$location','resourceService','DocumentService','sbiModule_translate','templateService',
        											 function($scope,$location, resourceService, DocumentService,sbiModule_translate,templateService){

        	 var self = this;
        	 self.documentService = DocumentService;
        	 self.translate = sbiModule_translate;
             self.documentInfoObject =$location.search();
             var basePath = self.documentInfoObject.OBJECT_ID;
             var resourceName = self.documentService.requiredPath;
             var crudService = resourceService;
             var functionalitiesPath = "2.0";
             var functionalitiesBasePath = "folders";
             var functionalitiesQuerryParam = "includeDocs=false";
             var translate = sbiModule_translate;
             self.functionalities;
             self.folders = [];
             self.types = documentAndInfo.types;
             self.engines = documentAndInfo.engines;
             self.datasources = documentAndInfo.datasources;
             self.datasets1 = documentAndInfo.datasets;
             self.datasets =[];
             self.document = self.documentService.document;
             console.log(self.document)
             if(self.document.visible != false )self.document.visible = true;

             if(self.document.refreshSeconds == null)
            	 self.document.refreshSeconds = 0;

             self.availableStatuses = documentAndInfo.states;
             self.template = self.documentService.template;
             self.required = true;
             self.folderObject=[];
             self.previewImage = {};
             self.document.lockedByUser = (self.document.lockedByUser == "true");
             self.file = templateService.file;
             var pathForFolderLocation = [];

             var createStringPath = function(folderArray){
            	 var stringPath = "";
            	 for(var i = folderArray.length-1 ; i >= 0; i-- ){
            		 stringPath = stringPath + "/" + folderArray[i];
            	 }
            	 self.document.functionalities=[]
            	 self.document.functionalities.push(stringPath);
             }

             var setFoldersPath = function(folders,folderId){
            	 if(folderId != null){
            	 var folderNum = folderId;
            	 }else var folderNum = 1;
            	 for(var i = 0; i < folders.length; i++){
            		 if(folderNum == folders[i].id){
            			 pathForFolderLocation.push(folders[i].name);
            			 if(folders[i].parentId)
            			 setFoldersPath(folders,folders[i].parentId)
            		 }
            	}
             }
             self.isDatasourceVisible = function(){
            	 switch(self.document.engine){
            	 case "knowageofficeengine":
            	 case "knowagecompositedoce":
            	 case "knowageprocessengine":
            	 case "knowagechartengine":
            	 case "knowagenetworkengine":
            	 case "knowagecockpitengine":
            	 case "knowagedossierengine":
            	 case "knowagekpiengine":
            	 case "knowagesvgviewerengine":
            		 return false;
            		 break;
            	 default : return true;
            	 }
             }
             self.isDatasetVisible = function(){
            	 switch(self.document.engine){
            	 case "knowagegisengine":
            	 case "knowagechartengine":
            	 case "knowagenetworkengine":
            		 return true;
            		 break;
            	 default : return false;
            	 }
             }
             var setCheckedFolder = function(folders){
            	 for(var i = 0; i < folders.length; i++){
            		 if(!self.document.functionalities && documentAndInfo.folderId == folders[i].id){
            			 folders[i].checked = true;
            		 }else if (self.document.functionalities && self.document.functionalities.indexOf(folders[i].path) != -1)
            			 folders[i].checked = true;
            	 }
            	 return folders;
             }

             self.addRestriction = function(){
            	 if(self.document.profiledVisibility){
            		 self.document.profiledVisibility = self.document.profiledVisibility + " AND " + self.visibilityAttribute + " = " + self.restrictionValue;
            	 }else {self.document.profiledVisibility =self.visibilityAttribute + " = " + self.restrictionValue;
            	 }
             }
             self.clearRestrictions = function(){
            	 self.document.profiledVisibility = "";
                 self.visibilityAttribute = "";
            	 self.restrictionValue = "";
             }
             self.getFunctionalities = function(functionalitiesPath){

	              	crudService.get(functionalitiesPath,functionalitiesBasePath,functionalitiesQuerryParam).then(function(response){
	              		self.folders = response.data;
	              		setCheckedFolder(self.folders);
	              		setFoldersPath(self.folders,documentAndInfo.folderId);
	              		createStringPath(pathForFolderLocation);
	              		self.documentService.folders = response.data;
	              	});

              };

              self.getDatasets = function(){
              	 crudService.get("2.0/datasets","").then(function(response){
              		self.datasets = response.data;
                 	});
              }
              self.getDatasets();
             self.getImage = function(){
             	 crudService.get(self.documentService.requiredPath,basePath + '/image').then(function(response){
             		self.documentService.documentImage = response.data;
                	});
             }

             self.getProfileAttributes = function(){
            	 crudService.get("2.0/attributes","").then(function(response){
            		 self.attributeValues = response.data;
            		 console.log(response.data)
            	 })
             }
             self.getProfileAttributes();

             if(self.document.previewFile){
            	 self.getImage();
             }

             self.deleteImage = function(){
            	 resourceService.delete(self.documentService.requiredPath,self.document.id + '/image').then(function(response){
            		 self.documentService.documentImage = undefined;
               	});
             }
             self.getFunctionalities(functionalitiesPath);


        }]);
})();


