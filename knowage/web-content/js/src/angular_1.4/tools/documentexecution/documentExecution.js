'use strict';
var stringStartsWith = function (string, prefix) {
	return string.toLowerCase().slice(0, prefix.length) == prefix.toLowerCase();
};

//var documentExecutionApp = angular.module('documentExecutionModule', ['ngMaterial']);
var documentExecutionApp = angular.module('documentExecutionModule');

documentExecutionApp.config(['$mdThemingProvider', function($mdThemingProvider) {
	   $mdThemingProvider.theme('knowage')
	   $mdThemingProvider.setDefaultTheme('knowage');
	}]);
documentExecutionApp.controller( 'documentExecutionController', 
		['$scope', '$http', '$mdSidenav', 'sbiModule_translate', 'sbiModule_restServices', 'sbiModule_config', 'execProperties',
		 documentExecutionControllerFn]);

function documentExecutionControllerFn(
		$scope, $http, $mdSidenav, sbiModule_translate, sbiModule_restServices, sbiModule_config, execProperties) {
	
	console.log("documentExecutionControllerFn IN ");
	$scope.executionInstance = {};
	$scope.roles = execProperties.roles;
	$scope.selectedRole = "";
	$scope.execContextId = "";
	$scope.documentUrl="";
	$scope.showSelectRoles=true;
	$scope.translate = sbiModule_translate;
	$scope.documentParameters = [];
    $scope.showParametersPanel = false;
   
	$scope.initSelectedRole = function(){
		console.log("initSelectedRole IN ");
		if(execProperties.roles && execProperties.roles.length > 0) {
			if(execProperties.roles.length==1) {
				$scope.showSelectRoles=false;
			}
			
			$scope.selectedRole = execProperties.roles[0];
			$scope.executionProcesRestV1($scope.selectedRole);
		}
		console.log("initSelectedRole OUT ");
	};
	
	$scope.toggleParametersPanel = function(){
		console.log('toggle ');
		$mdSidenav('right').toggle();
		$scope.showParametersPanel = $mdSidenav('right').isOpen();
	};
	
	
	
	/*
	 * START EXECUTION PROCESS REST
	 * Return the SBI_EXECUTION_ID code 
	 */
	$scope.executionProcesRestV1 = function(role, paramsStr) {
		
		//var paramTest = '{"valoreDriver":"12","valoreDriver_field_visible_description":"12","valore_9_url":"21","valore_9_url_field_visible_description":"21"}';
		if(typeof paramsStr === 'undefined'){
			paramsStr='{}';
		}
		var params = 
			"label=" + execProperties.executionInstance.OBJECT_LABEL
			+ "&role=" + role
			+ "&parameters=" + paramsStr;				
		sbiModule_restServices.alterContextPath( sbiModule_config.contextName);
		sbiModule_restServices.get("1.0/documentexecution", 'url',params).success(
				function(data, status, headers, config) {					
					console.log(data);
//					if ('errorDocument' in data){
//						alert(data.error);
//					}else{
						if(data['errors'].length > 0 ){
							var strErros='';
							for(var i=0; i<=data['errors'].length-1;i++){
								strErros=strErros + data['errors'][i] + ' \n';
							}
							alert(strErros);
						}
						$scope.documentParameters = data.parameters;
						$scope.documentUrl=data.url;
					//}						
				}).error(function(data, status, headers, config) {
					console.log("TargetLayer non Ottenuto " + status);
				});
	};
	
	
	/*
	 * EXECUTE PARAMS
	 * Submit param form
	 */
	$scope.executeParameter = function(){
		var jsonData =  {};
		console.log("$scope.documentParameters -> ", $scope.documentParameters);
		if($scope.documentParameters.length > 0){
			for(var i = 0; i < $scope.documentParameters.length; i++ ){
				jsonData[$scope.documentParameters[i].id] = $scope.documentParameters[i].parameterValue;
				jsonData[$scope.documentParameters[i].id + "_field_visible_description"] = 
					$scope.documentParameters[i].parameterValue;
			}
		}
		$scope.executionProcesRestV1($scope.selectedRole, JSON.stringify(jsonData));
		//$scope.getURLForExecution($scope.selectedRole, $scope.execContextId, jsonData);
		if($mdSidenav('right').isOpen()) {
			$mdSidenav('right').close();
			$scope.showParametersPanel = $mdSidenav('right').isOpen();
		}
//		$mdSidenav('right').toggle();
	}
	
	
	$scope.isExecuteParameterHidden = function(){
		if($scope.documentParameters.length > 0){
			for(var i = 0; i < $scope.documentParameters.length; i++ ){
				if($scope.documentParameters[i].mandatory 
						&& (typeof $scope.documentParameters[i].parameterValue === 'undefined') ){
					return true;
				}
			}
		}
		return false;
	}
	
	
	
	
	$scope.changeRole = function(role){
		// $scope.selectedRole is overwritten by the ng-model attribute
		
		console.log("changeRole IN ");
		//If new selected role is different from the previous one
		if(role != $scope.selectedRole) { 
			$scope.startExecutionProcess(role);
		}
		console.log("changeRole OUT ");
	};
	
	$scope.canExecuteDocument = function(){
	};
	
	$scope.isParameterPanelDisabled = function(){
		return (!$scope.documentParameters || $scope.documentParameters.length == 0);
	};
	
	
	
	$scope.executeDocument = function(){
		console.log('Executing document -> ', execProperties);
	};
	
	$scope.editDocument = function(){
		alert('Editing document');
		console.log('Editing document -> ', execProperties);
	};
	
	$scope.deleteDocument = function(){
		alert('Deleting document');
		console.log('Deleting document -> ', execProperties);
	};
	
	console.log("documentExecutionControllerFn OUT ");
	
	
	
	
	
	
	
	
	
	
	
	
	
};