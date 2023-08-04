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
	angular.module('TemplateModule', ['sbiModule'])
			.service('templateService', ['resourceService', 'DocumentService', 'multipartForm', 'sbiModule_translate', 'sbiModule_messaging', function(resourceService, DocumentService, multipartForm, sbiModule_translate, sbiModule_messaging) {
				var templateResource = {};
				var documentService = DocumentService;
				var requiredPath = "2.0/documentdetails";
				templateResource.listOfTemplates = [];
				templateResource.changedTemplates = [];
				templateResource.changedTemplate = {};
				templateResource.file = {};
				templateResource.templatesForDeleting = [];
				var templateUploadBasePath = document.id + '/templates';

				templateResource.uploadTemplate = function() {
					var ret = Promise.resolve();
					if(templateResource.file.file) {
						var templateUploadBasePath = documentService.document.id + '/templates';
						ret = multipartForm.post(requiredPath +"/"+ templateUploadBasePath, templateResource.file).then(function(response){
							templateResource.getAllTemplates();
						});
					}
					return ret;
				};

				templateResource.getAllTemplates = function() {
					var ret = Promise.resolve();
					var templateBasePath = documentService.document.id + '/templates';
					ret = resourceService.get(requiredPath, templateBasePath).then(function(response) {
						templateResource.listOfTemplates = response.data;
					});
					return ret;
				}

				templateResource.getSelectedTemplate = function(template) {
					var ret = Promise.resolve();
					var basePath = documentService.document.id + '/templates';
					var basePathWithId = basePath + "/selected/" + template.id;
					ret = resourceService.get(requiredPath, basePathWithId).then(function(response) {
						templateResource.selectedTemplateContent = response.data;
					});
					return ret;
				};

				templateResource.setActiveTemplate = function() {
					var ret = Promise.resolve();
					if(templateResource.changedTemplate.id) {
						var templateModifyBasePath = documentService.document.id + "/templates/" + templateResource.changedTemplate.id;
						ret = resourceService.put(requiredPath, templateModifyBasePath);
					}
					return ret;
				};

				var deleteTemplateById = function(template) {
					var ret = Promise.resolve();
					var basePath = documentService.document.id + "/" + 'templates';
					var basePathWithId = basePath + "/" + template.id;
					ret = resourceService.delete(requiredPath, basePathWithId).then(function(response) {
						 sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.documentdetails.toast.deleted"), 'Success!');
					});
					return ret;
				};

				templateResource.deleteTemplates = function() {
					var ret = [];
					for(var i = 0; i < templateResource.templatesForDeleting.length; i++) {
						ret.push(deleteTemplateById(templateResource.templatesForDeleting[i]));
					}
					return Promise.all(ret);
				};

			return templateResource;
		}]);

})();
