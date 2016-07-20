
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
	$scope.selectedSchema= {};
	
	
	
	
	$scope.templateTypeList = [{
	   	 "value": "xmla",
		  "name": "XMLA Server"	 
	}, 
	{
		 "value": "mondrian",
		  "name": "Mondrian"	 
	}]
	
	angular.element(document).ready(function () { // on page load function
		$scope.getMondrianSchemas();
    });
	
	$scope.getMondrianSchemas = function(){
		
		sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
		sbiModule_restServices.promiseGet("2.0/mondrianSchemasResource","")
		.then(function(response) {
			console.log(response.data);
			$scope.schemasList = [];
			$scope.schemasList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		
	};
	

};

