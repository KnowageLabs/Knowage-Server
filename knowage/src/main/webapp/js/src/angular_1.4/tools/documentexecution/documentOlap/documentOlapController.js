/*
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
	var documentExecutionModule = angular.module('documentExecutionModule');
	
	documentExecutionModule.directive('documentOlap', ['sbiModule_config', function(sbiModule_config){
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.dynamicResourcesBasePath 
				+ '/angular_1.4/tools/documentexecution/documentOlap/documentOlapTemplate.jsp',
			controller: documentOlapCtrl
		};
	}]);
	
	
	var documentOlapCtrl = function($scope, sbiModule_config, sbiModule_translate, documentExecuteServices, $mdDialog,
			sbiModule_restServices,	docExecute_urlViewPointService,execProperties,docExecute_paramRolePanelService,$filter,
			sbiModule_download) {

		$scope.column = [
                        {label:sbiModule_translate.load("sbi.generic.name"),name:"name"},
                        {label:sbiModule_translate.load("sbi.generic.descr"),name:"description"},
                        {label:sbiModule_translate.load("sbi.generic.scope.public"),name:"isPublic"}];

		$scope.closeFilter = function(){
			$mdDialog.cancel();
		}

		
		$scope.gvpCtrlOlapMenuOpt = [ 			 		               	
			 { // Fill Form
				 label: sbiModule_translate.load("sbi.generic.apply"),
				 icon:"fa fa-file-text-o",
				 color:'#222222',
				 action : function(item) { 
					 var para = {
						 "subobj_id":item.id,
						 "subobj_name":item.name,
						 "subobj_description":item.description,
						 "subobj_visibility":item.isPublic,
					 }
					 docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name, para);
					 docExecute_paramRolePanelService.returnToDocument();
					 
				}
			 }
			 ,{   //Delete Action
				 label: sbiModule_translate.load("sbi.generic.delete"),
				 icon:"fa fa-trash-o",
				 //backgroundColor:'red',
				 color:'#222222',
				 action : function(item, $index) {
					 var index =docExecute_urlViewPointService.olapList.indexOf(item);
					 sbiModule_restServices.promiseDelete("1.0/olapsubobjects", 'removeOlapSubObject', 
								"idObj=" + item.id)
						.then(function(response){  
							docExecute_urlViewPointService.olapList.splice(index,1);
						},function(response){
							sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.alert.load.error"));
						});
				 }
			 } 	
		 ];

	};
	
	
})();