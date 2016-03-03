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
			//$scope.startExecutionProcess($scope.selectedRole);
			//$scope.startExecutionProcessRest($scope.selectedRole);
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
	
	
	
	/*
	 * START EXECUTION PROCESS
	 * Return the SBI_EXECUTION_ID code 
	 */
	$scope.startExecutionProcess = function(role) {
		var params = 
			//Servlet case put a "&"
			"&OBJECT_ID=" + execProperties.executionInstance.OBJECT_ID
			+ "&OBJECT_LABEL=" + execProperties.executionInstance.OBJECT_LABEL
			+ "&isFromCross=" + execProperties.executionInstance.isFromCross
			+ "&isPossibleToComeBackToRolePage=" + execProperties.executionInstance.isPossibleToComeBackToRolePage 
			+ "&ROLE=" + role;		
		$http.post(
				sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME=" +
				"START_EXECUTION_PROCESS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE" + params
		).success(function(data, status, headers, config){
			$scope.getParametersForExecution(role, data.execContextId);
		})
		.error(function(data, status, headers, config) {
			console.log("Error " + status + ": ", data);
		});		
	};
	

	
	/*
	 * GET PARAMETERS 
	 * Check if parameters exist.
	 * exist - open parameters panel
	 * no exist - get iframe url  
	 */
	$scope.getParametersForExecution = function(role, execContextId) {
		var params = 
			"&OBJECT_ID=" + execProperties.executionInstance.OBJECT_ID 
			+ "&OBJECT_LABEL=" + execProperties.executionInstance.OBJECT_LABEL 
			+ "&isFromCross=" + execProperties.executionInstance.isFromCross
			+ "&isPossibleToComeBackToRolePage=" + execProperties.executionInstance.isPossibleToComeBackToRolePage 
			+ "&ROLE=" + role + 
			"&SBI_EXECUTION_ID=" + execContextId;
		$http.post(
				sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME=" +
				"GET_PARAMETERS_FOR_EXECUTION_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE" +  params
		).success(function(data){

			//check if document has paramiters 
			if(data.length>0){
				//check default param control TODO
				$scope.showParametersPanel=true;
				
				if(!($mdSidenav('right').isOpen())) {
//					$mdSidenav('right').toggle();
					$mdSidenav('right').open();
				}
				$scope.documentParameters = data;
				$scope.getViewPoints(role, execContextId); 
			}else{
				$scope.getURLForExecution(role ,execContextId,data);
			}
			
		});
	};
	
	/*
	 * GET VIEWPOINTS
	 * return saved params
	 */
	$scope.getViewPoints = function(role, execContextId) {
		var params = 
			"&OBJECT_ID=" + execProperties.executionInstance.OBJECT_I
			+ "&OBJECT_LABEL=" + execProperties.executionInstance.OBJECT_LABEL 
			+ "&isFromCross=" + execProperties.executionInstance.isFromCross 
			+ "&isPossibleToComeBackToRolePage=" + execProperties.executionInstance.isPossibleToComeBackToRolePage 
			+ "&ROLE=" + role 
			+ "&SBI_EXECUTION_ID=" + execContextId;
		
		$http.post(
				sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME=" +
				"GET_VIEWPOINTS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE" +  params
		).success(function(data){
			$scope.getSubObject(role ,execContextId); //??
			$scope.execContextId = execContextId;
			console.log('GET VIEW POINTS return data ->', data);
			//Handler data save
		})
		.error(function(data, status, headers, config) {
			console.log("Error " + status + ": ", data);
		});
	};
	
	/*
	 * GET SUBJECT OBJECT 
	 */
	$scope.getSubObject = function(role,execContextId) {
		var params =
			"&OBJECT_ID=" + execProperties.executionInstance.OBJECT_ID
			+ "&OBJECT_LABEL=" + execProperties.executionInstance.OBJECT_LABEL 
			+ "&isFromCross=" + execProperties.executionInstance.isFromCross 
			+ "&isPossibleToComeBackToRolePage=" + execProperties.executionInstance.isPossibleToComeBackToRolePage 
			+ "&ROLE=" + role + "&SBI_EXECUTION_ID=" 
			+ execContextId;
		
		$http.post(
				sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME="  + 
				"GET_SUBOBJECTS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE" +  params
		).success(function(data){
			$scope.getSnapShots(role ,execContextId);
		})
		.error(function(data, status, headers, config) {
			console.log("Error " + status + ": ", data);
		});
	};
	
	/*
	 * GET SNAP SHOTS
	 */
	$scope.getSnapShots = function(role, execContextId) {
		var params = 
			"&OBJECT_ID=" + execProperties.executionInstance.OBJECT_ID
			+ "&OBJECT_LABEL=" + execProperties.executionInstance.OBJECT_LABEL 
			+ "&isFromCross=" + execProperties.executionInstance.isFromCross
			+ "&isPossibleToComeBackToRolePage=" + execProperties.executionInstance.isPossibleToComeBackToRolePage 
			+ "&ROLE=" + role 
			+ "&SBI_EXECUTION_ID=" + execContextId;
		
		$http.post(
				sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME=" +
				"GET_SNAPSHOTS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE"+ params
		).success(function(data){

		})
		.error(function(data, status, headers, config) {
			console.log("Error " + status + ": ", data);
		});
	};
	
	
	
	/*
	 * GET URL DOCUMENT 
	 * Load iframe URL
	 */
	$scope.getURLForExecution = function(role, execContextId, parameters) {
		var paramStr = (Object.keys(parameters).length > 0) ? 
				JSON.stringify(parameters) : '{}'; 
		var obj = new Object();
		
		obj.OBJECT_ID = execProperties.executionInstance.OBJECT_ID;
		obj.OBJECT_LABEL = execProperties.executionInstance.OBJECT_LABEL;
		obj.isFromCross = execProperties.executionInstance.isFromCross;
		obj.isPossibleToComeBackToRolePage = execProperties.executionInstance.isPossibleToComeBackToRolePage;
		obj.SBI_EXECUTION_ID = execContextId;
		obj.PARAMETERS = paramStr;
		obj.ROLE = role;
		
		$http({
			method: 'POST',
			url: sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME=" +
				"GET_URL_FOR_EXECUTION_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE",
			
			transformRequest: function(obj) {
				var str = [];
				for(var p in obj){
					str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
				}
				return str.join("&");
			},
			data: obj,
			headers: {'Content-Type': 'application/x-www-form-urlencoded'},
		})
		.success(function(data){
			console.log(data);
			if ('errors' in data){
				if(data['errors'].length > 0 ){
					var strErros='';
					for(var i=0; i<=data['errors'].length-1;i++){
						strErros=strErros + data['errors'][i] + ' \n';
					}
					alert(strErros);
				}
			}else{
				$scope.documentUrl=data.url;
			}
		})
		.error(function(data, status, headers, config) {
			console.log("Error " + status + ": ", data);
		});
	};
	
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
	
	
	
	
	
	
	/*
	 * OLD TEST
	 */
	
	/*
	 * START EXECUTION PROCESS REST
	 * Return the SBI_EXECUTION_ID code 
	 */
	$scope.startExecutionProcessRest = function(role) {
		var params = 
			//Servlet case put a "&"
			"OBJECT_ID=" + execProperties.executionInstance.OBJECT_ID
			+ "&OBJECT_LABEL=" + execProperties.executionInstance.OBJECT_LABEL
			+ "&isFromCross=" + execProperties.executionInstance.isFromCross
			+ "&isPossibleToComeBackToRolePage=" + execProperties.executionInstance.isPossibleToComeBackToRolePage 
			+ "&ROLE=" + role;				
		sbiModule_restServices.alterContextPath( sbiModule_config.contextName);
		sbiModule_restServices.get("executeDocumentV2", 'documentParams',params).success(
				function(data, status, headers, config) {					
					console.log(data);
					//$scope.getParametersForExecutionRest(role, data.execContextId);
				}).error(function(data, status, headers, config) {
					console.log("TargetLayer non Ottenuto " + status);
				});
	};
	
	
	
	/*
	 * GET PARAMETERS 
	 * Check if parameters exist.
	 * exist - open parameters panel
	 * no exist - get iframe url  
	 */
	$scope.getParametersForExecutionRest = function(role, execContextId) {
		var params = 
			"OBJECT_ID=" + execProperties.executionInstance.OBJECT_ID 
			+ "&OBJECT_LABEL=" + execProperties.executionInstance.OBJECT_LABEL 
			+ "&isFromCross=" + execProperties.executionInstance.isFromCross
			+ "&isPossibleToComeBackToRolePage=" + execProperties.executionInstance.isPossibleToComeBackToRolePage 
			+ "&ROLE=" + role + 
			"&SBI_EXECUTION_ID=" + execContextId;		
		sbiModule_restServices.alterContextPath( sbiModule_config.contextName);
		sbiModule_restServices.get("executeDocumentGetParameters", 'getParameters',params).success(
				function(data, status, headers, config) {
					console.log(data);
					//check if document has paramiters 
					if(data.length>0){
						//check default param control TODO
						$scope.showParametersPanel=true;
						if(!($mdSidenav('right').isOpen())) {
							$mdSidenav('right').open();
						}
						$scope.documentParameters = data;
						$scope.getViewPointsRest(role, execContextId); 
					}else{
						$scope.getURLForExecutionRest(role ,execContextId,data);
					}					
				}).error(function(data, status, headers, config) {
					console.log("TargetLayer non Ottenuto " + status);
				});
		
		
	};
	
	
	
	
	
	
	
	
	
	
	
};