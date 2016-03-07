(function() {

	var stringStartsWith = function (string, prefix) {
		return string.toLowerCase().slice(0, prefix.length) == prefix.toLowerCase();
	};

	var documentExecutionApp = angular.module('documentExecutionModule');

	documentExecutionApp.directive('iframeOnload', [function(){
		return {
		    scope: {
		        callBack: '&iframeOnload'
		    },
		    link: function(scope, element, attrs){
		        element.on('load', function(){
		            return scope.callBack();
		        })
		    }
		}}])
	
	
	documentExecutionApp.config(['$mdThemingProvider', function($mdThemingProvider) {
		$mdThemingProvider.theme('knowage')
		$mdThemingProvider.setDefaultTheme('knowage');
	}]);
	
	documentExecutionApp.controller( 'documentExecutionController', 
			['$scope', '$http', '$mdSidenav', 'sbiModule_translate', 'sbiModule_restServices', 
			 'sbiModule_config', 'sbiModule_messaging', 'execProperties',
			 documentExecutionControllerFn]);


	function documentExecutionControllerFn(
			$scope, $http, $mdSidenav, sbiModule_translate, sbiModule_restServices, sbiModule_config, sbiModule_messaging, execProperties) {

		console.log("documentExecutionControllerFn IN ");
		$scope.executionInstance = {};
		$scope.roles = execProperties.roles;
		$scope.selectedRole = "";
		$scope.execContextId = "";
		$scope.documentUrl="";
		$scope.showSelectRoles=true;
		$scope.translate = sbiModule_translate;
		$scope.documentParameters = [];
		$scope.showParametersPanel = true;

		$scope.initSelectedRole = function(){
			console.log("initSelectedRole IN ");
			if(execProperties.roles && execProperties.roles.length > 0) {
				if(execProperties.roles.length==1) {
					$scope.selectedRole = execProperties.roles[0];
					$scope.showSelectRoles=false;
					//load parameters if role is selected
					$scope.getParametersForExecution($scope.selectedRole);
				}
				$scope.executionProcesRestV1($scope.selectedRole);
			}
			console.log("initSelectedRole OUT ");
		};

		$scope.toggleParametersPanel = function(){
			console.log('toggle ');
			$mdSidenav('parametersPanelSideNav').toggle();
			$scope.showParametersPanel = $mdSidenav('parametersPanelSideNav').isOpen();
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
		//			if ('errorDocument' in data){
		//				alert(data.error);
		//			}else{
					if(data['errors'].length > 0 ){
						var strErros='';
						for(var i=0; i<=data['errors'].length-1;i++){
							strErros=strErros + data['errors'][i] + ' \n';
						}
						alert(strErros);
						//sbiModule_messaging.showErrorMessage(response.errors[0].message, 'Error'); todo
					}else{
						$scope.documentUrl=data.url;
					}
					//$scope.documentParameters = data.parameters; 
				//}						
				}).error(function(data, status, headers, config) {
					console.log("TargetLayer non Ottenuto " + status);
				});
		};
		



		/*
		 * EXECUTE PARAMS
		 * Submit param form
		 */
		$scope.executeParameter = function() {
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
			if($mdSidenav('parametersPanelSideNav').isOpen()) {
				$mdSidenav('parametersPanelSideNav').close();
				$scope.showParametersPanel = $mdSidenav('parametersPanelSideNav').isOpen();
			}
			console.log("initSelectedRole OUT ");
		};
		
		$scope.changeRole = function(role) {
			// $scope.selectedRole is overwritten by the ng-model attribute
			console.log("changeRole IN ");
			//If new selected role is different from the previous one
			if(role != $scope.selectedRole) { 
				$scope.executionProcesRestV1(role);
				$scope.getParametersForExecution(role);
//				if($mdSidenav('parametersPanelSideNav').isOpen()) {
//					$mdSidenav('parametersPanelSideNav').close();
//					$scope.showParametersPanel = $mdSidenav('parametersPanelSideNav').isOpen();
//				}
			}
			console.log("changeRole OUT ");
		};
	
		$scope.isExecuteParameterDisabled = function() {
			if($scope.documentParameters.length > 0) {
				for(var i = 0; i < $scope.documentParameters.length; i++ ) {
					if($scope.documentParameters[i].mandatory 
							&& (!$scope.documentParameters[i].parameterValue
									|| $scope.documentParameters[i].parameterValue == '' )) {
						return true;
					}
				}
			}
			return false
		};

		$scope.iframeOnload = function(){
			//alert('loaded');
//			if($mdSidenav('parametersPanelSideNav').isOpen()) {
//				$mdSidenav('parametersPanelSideNav').close();
//				$scope.showParametersPanel = $mdSidenav('parametersPanelSideNav').isOpen();
//			}
		}
		
		
		/*
		 * START EXECUTION PROCESS
		 * Return the SBI_EXECUTION_ID code 
		 */
//		$scope.startExecutionProcess = function(role) {
//			var params = 
//				"&OBJECT_ID=" + execProperties.executionInstance.OBJECT_ID
//				+ "&OBJECT_LABEL=" + execProperties.executionInstance.OBJECT_LABEL
//				+ "&isFromCross=" + execProperties.executionInstance.isFromCross
//				+ "&isPossibleToComeBackToRolePage=" + execProperties.executionInstance.isPossibleToComeBackToRolePage 
//				+ "&ROLE=" + role;		
//			$http.post(
//					sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME=" +
//					"START_EXECUTION_PROCESS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE" + params
//			).success(function(data, status, headers, config){
//				$scope.getParametersForExecution(role, data.execContextId);
//			})
//			.error(function(data, status, headers, config) {
//				console.log("Error " + status + ": ", data);
//			});
//		};

		/*
		 * GET PARAMETERS 
		 * Check if parameters exist.
		 * exist - open parameters panel
		 * no exist - get iframe url  
		 */
		$scope.getParametersForExecution = function(role) {		
			var params = 
				"label=" + execProperties.executionInstance.OBJECT_LABEL
				+ "&role=" + role;
			
			sbiModule_restServices.get("1.0/documentexecution", "filters", params)
			.success(function(response, status, headers, config){
				console.log('getParametersForExecution response OK -> ', response);
				//check if document has parameters 
				if(response && response.filterStatus && response.filterStatus.length>0){
					//check default parameter control TODO										
					$scope.showParametersPanel=true;
					if(!($mdSidenav('parametersPanelSideNav').isOpen())) {
						$mdSidenav('parametersPanelSideNav').open();
					}
					$scope.documentParameters = response.filterStatus;
					//$scope.getViewPoints(role, execContextId); 
					//$scope.getParameterValues();
				}else{
					$scope.showParametersPanel = false;
					if($mdSidenav('parametersPanelSideNav').isOpen()) {
						$mdSidenav('parametersPanelSideNav').close();
					}
					//$scope.getURLForExecution(role, execContextId, data);
				}
			})
			.error(function(response, status, headers, config) {
				console.log('getParametersForExecution response ERROR -> ', response);
				sbiModule_messaging.showErrorMessage(response.errors[0].message, 'Error');
			});
		};

		/*
		 * GET VIEWPOINTS
		 * return saved params
		 */
//		$scope.getViewPoints = function(role, execContextId) {
//			var params = 
//				"&OBJECT_ID=" + execProperties.executionInstance.OBJECT_I
//				+ "&OBJECT_LABEL=" + execProperties.executionInstance.OBJECT_LABEL 
//				+ "&isFromCross=" + execProperties.executionInstance.isFromCross 
//				+ "&isPossibleToComeBackToRolePage=" + execProperties.executionInstance.isPossibleToComeBackToRolePage 
//				+ "&ROLE=" + role 
//				+ "&SBI_EXECUTION_ID=" + execContextId;
//
//			$http.post(
//					sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME=" +
//					"GET_VIEWPOINTS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE" +  params
//			).success(function(data){
//				$scope.getSubObject(role ,execContextId); //??
//				$scope.execContextId = execContextId;
//				console.log('GET VIEW POINTS return data ->', data);
//				//Handler data save
//			})
//			.error(function(data, status, headers, config) {
//				console.log("Error " + status + ": ", data);
//			});
//		};
		
		/*
		 * GET SUBJECT OBJECT 
		 */
//		$scope.getSubObject = function(role,execContextId) {
//			var params =
//				"&OBJECT_ID=" + execProperties.executionInstance.OBJECT_ID
//				+ "&OBJECT_LABEL=" + execProperties.executionInstance.OBJECT_LABEL 
//				+ "&isFromCross=" + execProperties.executionInstance.isFromCross 
//				+ "&isPossibleToComeBackToRolePage=" + execProperties.executionInstance.isPossibleToComeBackToRolePage 
//				+ "&ROLE=" + role 
//				+ "&SBI_EXECUTION_ID=" + execContextId;
//
//			$http.post(
//					sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME="  + 
//					"GET_SUBOBJECTS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE" +  params
//			).success(function(data){
//				$scope.getSnapShots(role, execContextId);
//			})
//			.error(function(data, status, headers, config) {
//				console.log("Error " + status + ": ", data);
//			});
//		};	
		
		/*
		 * GET SNAP SHOTS
		 */
//		$scope.getSnapShots = function(role, execContextId) {
//			var params = 
//				"&OBJECT_ID=" + execProperties.executionInstance.OBJECT_ID
//				+ "&OBJECT_LABEL=" + execProperties.executionInstance.OBJECT_LABEL 
//				+ "&isFromCross=" + execProperties.executionInstance.isFromCross
//				+ "&isPossibleToComeBackToRolePage=" + execProperties.executionInstance.isPossibleToComeBackToRolePage 
//				+ "&ROLE=" + role 
//				+ "&SBI_EXECUTION_ID=" + execContextId;
//
//			$http.post(
//					sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME=" +
//					"GET_SNAPSHOTS_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE"+ params
//			).success(function(data){
//
//			})
//			.error(function(data, status, headers, config) {
//				console.log("Error " + status + ": ", data);
//			});
//		};
		
		
		/*
		 * GET URL DOCUMENT 
		 * Load iframe URL
		 */
//		$scope.getURLForExecution = function(role, execContextId, parameters) {
//			var paramStr = (Object.keys(parameters).length > 0) ? 
//					JSON.stringify(parameters) : '{}'; 
//					
//			var obj = new Object();
//
//			obj.OBJECT_ID = execProperties.executionInstance.OBJECT_ID;
//			obj.OBJECT_LABEL = execProperties.executionInstance.OBJECT_LABEL;
//			obj.isFromCross = execProperties.executionInstance.isFromCross;
//			obj.isPossibleToComeBackToRolePage = execProperties.executionInstance.isPossibleToComeBackToRolePage;
//			obj.SBI_EXECUTION_ID = execContextId;
//			obj.PARAMETERS = paramStr;
//			obj.ROLE = role;
//
//			$http({
//				method: 'POST',
//				url: sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME=" +
//				"GET_URL_FOR_EXECUTION_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE",
//
//				transformRequest: function(obj) {
//					var str = [];
//					for(var p in obj){
//						str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
//					}
//					return str.join("&");
//				},
//				data: obj,
//				headers: {'Content-Type': 'application/x-www-form-urlencoded'},
//			})
//			.success(function(data){
//				console.log(data);
//				if ('errors' in data){
//					if(data['errors'].length > 0 ){
//						var strErros='';
//						for(var i=0; i<=data['errors'].length-1;i++){
//							strErros=strErros + data['errors'][i] + ' \n';
//						}
//						alert(strErros);
//					}
//				}else{
//					$scope.documentUrl=data.url;
//				}
//			})
//			.error(function(data, status, headers, config) {
//				console.log("Error " + status + ": ", data);
//			});
//		};

		

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
	
	/*
	documentExecutionApp.directive('iframeSetDimensionsOnload', [function(){
		return {
			restrict: 'A',
			link: function(scope, element, attrs){
				element.on('load', function(){
					var iFrameHeight = element[0].parentElement.scrollHeight + 'px';
					element.css('height', iFrameHeight);
				})
			}
		}}]
	);
	 */
})();	