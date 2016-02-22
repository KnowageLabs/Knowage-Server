var stringStartsWith = function (string, prefix) {
	return string.toLowerCase().slice(0, prefix.length) == prefix.toLowerCase();
};

//var documentExecutionApp = angular.module('documentExecutionModule', ['md.data.table', 'ngMaterial', 'ui.tree', 'sbiModule', 'document_tree']);
var documentExecutionApp = angular.module('documentExecutionModule');

documentExecutionApp.controller( 'documentExecutionController', 
		['$scope', '$http', '$mdSidenav', 'sbiModule_translate', 'sbiModule_restServices', 'sbiModule_config', 'execProperties',
		 documentExecutionControllerFn]);

function documentExecutionControllerFn(
		$scope, $http, $mdSidenav, sbiModule_translate, sbiModule_restServices, sbiModule_config, execProperties) {
	
	console.log("documentExecutionControllerFn IN ");
	
	$scope.executionInstance = {};
	
	$scope.roles = execProperties.roles;
	$scope.selectedRole = "";
	
	$scope.initSelectedRole = function(){
		console.log("initSelectedRole IN ");
		
		if(execProperties.roles && execProperties.roles.length > 0) {
			$scope.selectedRole = execProperties.roles[0]
		}
		
		console.log("initSelectedRole OUT ");
	};
	
	console.log("documentExecutionControllerFn OUT ");
};