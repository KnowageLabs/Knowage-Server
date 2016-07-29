
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


function olapDesignerController($scope, $timeout, $window, $mdDialog, $http, $sce,
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
		sbiModule_restServices.promiseGet("2.0/mondrianSchemasResource","")
		.then(function(response) {
			$scope.schemasList = [];
			$scope.schemasList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		
	};
	
$scope.getCube = function(item){
		$scope.selectedSchema = item;
		sbiModule_restServices.promiseGet("/1.0/designer/cubes/"+$scope.selectedSchema.currentContentId,"")
		.then(function(response) {
			$scope.cubeList = response.data;
			$scope.showCubes = true;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		
	};
	
	$scope.setMDX = function(){
		
		sbiModule_restServices.promiseGet("/1.0/designer/cubes/getMDX/"+$scope.selectedSchema.currentContentId+"/"+$scope.selectedCube.name,"")
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
		
      	sbiModule_restServices.promisePost("1.0/designer/cubes","",angular.toJson($scope.template))
    	.then(function(response) {
    		
    		var url = sbiModule_config.contextName + "/restful-services/1.0/designer/cubes/start";
    		console.log(url);
    		$window.location = url;
		
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});			
		};

};

