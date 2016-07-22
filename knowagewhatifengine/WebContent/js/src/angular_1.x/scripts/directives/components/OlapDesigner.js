
angular.module('olap_designer', [])
		.directive(
				'olapDesigner',
				function() {
					return {
						restrict : "E",
						replace : 'true',
						templateUrl : '/knowagewhatifengine/html/template/main/olap/olapDesigner.html',
						controller : olapDesignerController
					}
				});


function olapDesignerController($scope, $timeout, $window, $mdDialog, $http, $sce,
		sbiModule_messaging, sbiModule_restServices, sbiModule_translate,
		toastr, $cookies, sbiModule_docInfo, sbiModule_config) {
	
	$scope.selectedType = null;
	$scope.schemasList = [];
	$scope.cubeList = [];
	$scope.selectedSchema= {
	"name": "",		
	"currentContentId": null		
	};
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
	
$scope.getCube = function(){
		
		sbiModule_restServices.promiseGet("/1.0/designer/cubes/"+$scope.selectedSchema.currentContentId,"")
		.then(function(response) {
			$scope.cubeList = response.data;
			$scope.showCubes = true;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		
	};
	
	$scope.setMDX = function(){
		console.log($scope.selectedCube.name);
		sbiModule_restServices.promiseGet("/1.0/designer/cubes/getMDX/"+$scope.selectedSchema.currentContentId+"/"+$scope.selectedCube.name,"")
		.then(function(response) {
			mdx = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		};
		$scope.saveMDX = function(){
			sbiModule_restServices.promiseGet("/1.0/designer/cubes/getMDX/"+$scope.selectedSchema.currentContentId+"/"+$scope.selectedCube.name,"")
			.then(function(response) {
				mdx = response.data;
				console.log(mdx);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});	
			};

};

