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
        .controller('OutputParametersController',[ 'DocumentService', 'outputParametersService', 'resourceService', '$location',  'sbiModule_restServices', 'sbiModule_translate', '$scope', 'sbiModule_messaging', '$mdDialog',
        										function(DocumentService, outputParametersService, resourceService, $location, sbiModule_restServices, sbiModule_translate, $scope, sbiModule_messaging, $mdDialog){

        	  var self = this;
        	  var documentService = DocumentService;
        	  self.outputParametersService = outputParametersService;
        	  $scope.translate = sbiModule_translate;
        	  self.document = DocumentService.document;
        	  self.documentInfoObject = $location.search();
        	  var id = self.document.id;
        	  var basePath = id + "/" + 'outputparameters';
        	  var resourceName = DocumentService.requiredPath; 
        	  
        	  
        	  function getSortedArray() {
        		  self.document.outputParameters.sort(function(a, b) {
            		  return a.id - b.id;
            	  });
        		  return self.document.outputParameters;
        	  }
        	  
        	  getSortedArray();
        	  
              self.addParameter = function() {
            	  self.selectedParameter = {
            			  biObjectId: id,
            			  formatCode: "",
            			  formatValue: "",
            			  isUserDefined: true,
            			  type: self.typeList[0] ? self.typeList[0] : {} 
            	  }
                  if (self.document.outputParameters) {
                	  self.document.outputParameters.push(self.selectedParameter);
                  } else {
                	  self.document.outputParameters.push(self.selectedParameter);
                  }
              };               
              
              self.selectOutputParameter = function(index) {
                  self.selectedParameter = DocumentService.document.outputParameters[index];
              }
              
              self.removeOutputParameterFromList = function(index) {            	              	  
            	  self.outputParametersService.outputParametersForDeleting.push(DocumentService.document.outputParameters[index]);
            	  DocumentService.document.outputParameters.splice(index, 1);
              }
          	
          	self.loadTypeList = function(){
     			self.typeList = [];
     			sbiModule_restServices.promiseGet("2.0/domains","listByCode/PAR_TYPE").then(
     				function(response) {
     					self.typeList = response.data;
     				},function(response) {
     					console.log($scope.translate.load("sbi.documentdetails.load.error"));
     				});
          	};
     			   
     		self.loadTypeList();
     		  
     		self.loadDateTypes = function(){
				self.dateFormats = [];
				sbiModule_restServices.promiseGet("2.0/domains","listByCode/DATE_FORMAT").then(
					function(response) {
						self.dateFormats = response.data;
					},function(response) {
						console.log($scope.translate.load("sbi.documentdetails.load.error"));
					});
			};
			
			self.loadDateTypes();
			
         }]);
			
})();