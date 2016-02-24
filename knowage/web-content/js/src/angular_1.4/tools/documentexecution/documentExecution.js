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
	$scope.result="";
	$scope.showSelectRoles=true;
	$scope.translate = sbiModule_translate;
	$scope.documentParameters = [];
    $scope.showParametersPanel = false;
   
	$scope.initSelectedRole = function(){
		console.log("initSelectedRole IN ");
		if(execProperties.roles && execProperties.roles.length > 0) {
			if(execProperties.roles.length==1){$scope.showSelectRoles=false;}			
			$scope.selectedRole = execProperties.roles[0]
			$scope.startExecutionProcess(execProperties.roles[0]);
		}
		console.log("initSelectedRole OUT ");
	};
	
	$scope.toggleParametersPanel = function(){
		console.log('toggle ');
		$mdSidenav('right').toggle();
	};
	
	/*
	 * START EXECUTION PROCESS
	 * Return the SBI_EXECUTION_ID code 
	 */
	$scope.startExecutionProcess = function(role) {
		var params = "&OBJECT_ID="+execProperties.executionInstance.OBJECT_ID+"&OBJECT_LABEL="+execProperties.executionInstance.OBJECT_LABEL+
			"&isFromCross="+execProperties.executionInstance.isFromCross+"&isPossibleToComeBackToRolePage="+execProperties.executionInstance.isPossibleToComeBackToRolePage+"&ROLE="+role;		
		$http.post(
				sbiModule_config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=" +
				"START_EXECUTION_PROCESS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE"+ params
				).success(function(data){
					$scope.getParametersForExecutioin(role ,data.execContextId);
				});
	};
	
	/*
	 * GET PARAMETERS 
	 * Check if parameters exist.
	 * exist - open parameters panel
	 * no exist - get iframe url  
	 */
	$scope.getParametersForExecutioin = function(role, execContextId) {
		var params = "&OBJECT_ID="+execProperties.executionInstance.OBJECT_ID+"&OBJECT_LABEL="+execProperties.executionInstance.OBJECT_LABEL+
			"&isFromCross="+execProperties.executionInstance.isFromCross+"&isPossibleToComeBackToRolePage="+
			execProperties.executionInstance.isPossibleToComeBackToRolePage+"&ROLE="+role+"&SBI_EXECUTION_ID="+execContextId;
		$http.post(
				sbiModule_config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=" +
				"GET_PARAMETERS_FOR_EXECUTION_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE"+ params
				).success(function(data){
					//check paramitesr 
					if(data.length>0){
						//check default param control TODO
						$scope.showParametersPanel=true;
						$mdSidenav('right').toggle();
						$scope.documentParameters = data;
						$scope.getViewPoints(role ,execContextId); 
					}else{
						$scope.getURLForExecutioin(role ,execContextId,data);
					}
				});
	};
	
	/*
	 * GET VIEWPOINTS
	 * return params saved
	 */
	$scope.getViewPoints = function(role, execContextId) {
		var params = "&OBJECT_ID="+execProperties.executionInstance.OBJECT_ID+"&OBJECT_LABEL="+execProperties.executionInstance.OBJECT_LABEL+
			"&isFromCross="+execProperties.executionInstance.isFromCross+"&isPossibleToComeBackToRolePage="+
			execProperties.executionInstance.isPossibleToComeBackToRolePage+"&ROLE="+role+"&SBI_EXECUTION_ID="+execContextId;
		$http.post(
				sbiModule_config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=" +
				"GET_VIEWPOINTS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE"+ params
				).success(function(data){
					$scope.getSubObject(role ,execContextId); //??
					$scope.execContextId = execContextId;
					console.log('GET VIEW POINTS return data ->', data);
					//Handler data save
				});
	};
	
	/*
	 * GET SUBJECT OBJECT 
	 */
	$scope.getSubObject = function(role,execContextId) {
		var params = "&OBJECT_ID="+execProperties.executionInstance.OBJECT_ID+"&OBJECT_LABEL="+execProperties.executionInstance.OBJECT_LABEL+
			"&isFromCross="+execProperties.executionInstance.isFromCross+"&isPossibleToComeBackToRolePage="+
			execProperties.executionInstance.isPossibleToComeBackToRolePage+"&ROLE="+role+"&SBI_EXECUTION_ID="+execContextId;		
		$http.post(
				sbiModule_config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=" +
				"GET_SUBOBJECTS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE"+ params
				).success(function(data){
					$scope.getSnapShots(role ,execContextId);
				});
	};
	
	/*
	 * GET SNAP SHOTS
	 */
	$scope.getSnapShots = function(role,execContextId) {
		var params = "&OBJECT_ID="+execProperties.executionInstance.OBJECT_ID+"&OBJECT_LABEL="+execProperties.executionInstance.OBJECT_LABEL+
			"&isFromCross="+execProperties.executionInstance.isFromCross+"&isPossibleToComeBackToRolePage="
			+execProperties.executionInstance.isPossibleToComeBackToRolePage+"&ROLE="+role+"&SBI_EXECUTION_ID="+execContextId;	
		$http.post(
				sbiModule_config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=" +
				"GET_SNAPSHOTS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE"+ params
				).success(function(data){
					
				});
	};
	
	/*
	 * EXECUTE PARAMS
	 * Submit param form
	 */
	$scope.executeParameter = function(){
		var jsonData =  {};
		console.log("$scope.documentParameters -> ", $scope.documentParameters);
		if($scope.documentParameters.length>0){
			for(var i =0;i<=$scope.documentParameters.length-1;i++ ){
				jsonData[$scope.documentParameters[i].id] = $scope.documentParameters[i].parameterToExecute;
				jsonData[$scope.documentParameters[i].id+"_field_visible_description"] = $scope.documentParameters[i].parameterToExecute;
			}
		}
		$scope.getURLForExecutioin($scope.selectedRole,  $scope.execContextId, jsonData)
		$mdSidenav('right').toggle();
	}
	
	//chiedere a Benedetto
	$scope.executeParameterDisabled = function(){
		var disabled = false;
		if($scope.documentParameters.length>0){
			for(var i =0;i<=$scope.documentParameters.length-1;i++ ){
				if($scope.documentParameters[i].mandatory && (typeof $scope.documentParameters[i].parameterToExecute === 'undefined') ){
					disabled=true;
				}
			}
		}
		return disabled;
	}
	
	/*
	 * GET URL DOCUMENT 
	 * Load iframe URL
	 */
	$scope.getURLForExecutioin = function(role, execContextId, parameters) {
		    var paramStr = (Object.keys(parameters).length >0) ? JSON.stringify(parameters) : '{}'; 
		    var obj = new Object();
		    obj.OBJECT_ID=execProperties.executionInstance.OBJECT_ID;
			obj.OBJECT_LABEL=execProperties.executionInstance.OBJECT_LABEL;
			obj.isFromCross=execProperties.executionInstance.isFromCross;
			obj.isPossibleToComeBackToRolePage=execProperties.executionInstance.isPossibleToComeBackToRolePage;
			obj.SBI_EXECUTION_ID=execContextId;
			obj.PARAMETERS=paramStr;
			obj.ROLE=role;			
			$http({
			    method: 'POST',
			    url: sbiModule_config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=" +
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
			}).success(function(data){
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
					$scope.result=data.url;
				}
			});
	};	
	$scope.changeRole = function(role){
		$scope.startExecutionProcess(role);
	};
	console.log("documentExecutionControllerFn OUT ");
};