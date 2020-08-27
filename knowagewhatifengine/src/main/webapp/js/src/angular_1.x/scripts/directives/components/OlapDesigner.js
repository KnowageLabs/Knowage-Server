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
angular.module('olap_designer', ['sbiModule'])
		.directive(
				'olapDesigner',
				function(sbiModule_config) {
					return {
						restrict : "E",
						replace : 'true',
						templateUrl : function() {
						return sbiModule_config.contextName + '/html/template/main/olap/olapDesigner.html'	
						}, 
						controller : olapDesignerController
					}
				});


function olapDesignerController($scope, channelMessaging, $timeout, $window, $location, $mdDialog, $http, $sce,
		sbiModule_messaging, sbiModule_restServices, sbiModule_translate,
		toastr, $cookies, sbiModule_docInfo, sbiModule_config) {
	
	$scope.template = {};
	$scope.selectedType = null;
	$scope.schemasList = [];
	$scope.cubeList = [];
	$scope.selectedSchema= {};
	$scope.showCubes = false;
	$scope.selectedCube = {
	"name": ""				
	};
	$scope.templateTypeList = [{
	   	 "value": "xmla",
		  "name": "XMLA Server"	 
	}, 
	{
		 "value": "mondrian",
		  "name": "Mondrian"	 
	}];
	var mdx = null;
	$scope.xmlaObj={
			"address": "",
			"parameters": []		
	}
	
	angular.element(document).ready(function () { // on page load function
		$scope.getMondrianSchemas();
    });
	
	$scope.addXmlaParameter=function(){ 
	
		
		var parameter={};
		$scope.xmlaObj.parameters.push(parameter);
		console.log($scope.xmlaObj.parameters);
		return parameter;
		
	}
	
	$scope.removeXmlaParameter=function(inputVariable){
	
		var index=$scope.xmlaObj.parameters.indexOf(inputVariable);		
		$scope.xmlaObj.parameters.splice(index, 1);
		console.log($scope.xmlaObj.parameters);
	}
	
	$scope.getMondrianSchemas = function(){
		
		sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
		sbiModule_restServices.promiseGet("2.0/mondrianSchemasResource","?SBI_EXECUTION_ID=" + JSsbiExecutionID)
		.then(function(response) {
			$scope.schemasList = [];
			$scope.schemasList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		
	};
	
$scope.getCube = function(item){
		$scope.selectedSchema = item;
		sbiModule_restServices.promiseGet("1.0/designer/allcubes/"+$scope.selectedSchema.currentContentId,"?SBI_EXECUTION_ID=" + JSsbiExecutionID)
		.then(function(response) {
			$scope.cubeList = response.data;
			$scope.showCubes = true;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		
	};
	
	$scope.setMDX = function(){
		
		sbiModule_restServices.promiseGet("1.0/designer/cubes/getMDX/"+$scope.selectedSchema.currentContentId+"/"+$scope.selectedCube.name,"?SBI_EXECUTION_ID=" + JSsbiExecutionID)
		.then(function(response) {
			mdx = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		};
		
		 var prepareTemplate = function() {
			console.log($scope.selectedSchema);
			$scope.template.mondrianSchema = $scope.selectedSchema.name;
			$scope.template.mondrianSchemaId = $scope.selectedSchema.currentContentId;
			$scope.template.id = $scope.selectedSchema.id;
			$scope.template.mdxQuery = mdx; 
			$scope.template.mondrianMdxQuery = mdx;
			
		} 
		
	
	
$scope.saveMDX = function(){
			
			prepareTemplate();
			console.log(engineName);
      	sbiModule_restServices.promisePost("1.0/designer/cubes?SBI_EXECUTION_ID=" + JSsbiExecutionID,"",angular.toJson($scope.template))
    	.then(function(response) {
    		
    		var url = sbiModule_config.contextName + "/restful-services/pages/execute?SBI_EXECUTION_ID="
    		+ JSsbiExecutionID+"&mode="+mode+"&schemaID="+$scope.selectedSchema.id+"&cubeName="+$scope.selectedCube.name+"&schemaName="
    		+$scope.selectedSchema.name+"&ENGINE="+engineName+"&currentContentId="+$scope.selectedSchema.currentContentId
    		;
   
    		$window.location = url;
		
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});			
		};
		
		/**
		  * Close the wizard
		  */
	$scope.closeOlapTemplate = function(){
		 
		channelMessaging.sendMessage();

	}

};


