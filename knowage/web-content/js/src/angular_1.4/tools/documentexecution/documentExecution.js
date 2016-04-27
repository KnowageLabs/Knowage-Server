(function() {

	

	var stringStartsWith = function (string, prefix) {
		return string.toLowerCase().slice(0, prefix.length) == prefix.toLowerCase();
	};

	var documentExecutionApp = angular.module('documentExecutionModule');
	
	documentExecutionApp.config(['$mdThemingProvider', function($mdThemingProvider) {
		$mdThemingProvider.theme('knowage')
		$mdThemingProvider.setDefaultTheme('knowage');
	}]);

	documentExecutionApp.controller( 'documentExecutionController', 
			['$scope', '$http', '$mdSidenav', '$mdDialog','$mdToast', 'sbiModule_translate', 'sbiModule_restServices','sbiModule_user', 
			 'sbiModule_config', 'sbiModule_messaging', 'execProperties', 'documentExecuteFactories', 'sbiModule_helpOnLine',
			 'documentExecuteServices','docExecute_urlViewPointService','docExecute_paramRolePanelService','infoMetadataService','sbiModule_download','$crossNavigationScope',
			 'docExecute_dependencyService','$timeout','docExecute_exportService',documentExecutionControllerFn]);

	function documentExecutionControllerFn(
			$scope, $http, $mdSidenav,$mdDialog,$mdToast, sbiModule_translate, sbiModule_restServices,sbiModule_user, sbiModule_config,
			sbiModule_messaging, execProperties, documentExecuteFactories, sbiModule_helpOnLine,documentExecuteServices
			,docExecute_urlViewPointService,docExecute_paramRolePanelService,infoMetadataService,sbiModule_download,$crossNavigationScope,docExecute_dependencyService,$timeout,docExecute_exportService) {

		console.log("documentExecutionControllerFn IN ");
		$scope.executionInstance = execProperties.executionInstance || {};
		$scope.roles = execProperties.roles;
		$scope.selectedRole = execProperties.selectedRole;
		$scope.execContextId = "";
		//$scope.documentUrl="";
		$scope.showSelectRoles = true;
		$scope.translate = sbiModule_translate;
		$scope.documentParameters = execProperties.parametersData.documentParameters;
		$scope.newViewpoint = JSON.parse(JSON.stringify(documentExecuteFactories.EmptyViewpoint));
		$scope.viewpoints = [];
		$scope.documentExecuteFactories = documentExecuteFactories;
		$scope.documentExecuteServices = documentExecuteServices;
		$scope.paramRolePanelService = docExecute_paramRolePanelService;
		$scope.urlViewPointService = docExecute_urlViewPointService;		
		$scope.currentView = execProperties.currentView;
		$scope.parameterView = execProperties.parameterView;
		$scope.isParameterRolePanelDisabled = execProperties.isParameterRolePanelDisabled;
		$scope.showParametersPanel = execProperties.showParametersPanel;
		//rank
		$scope.rankDocumentSaved = 0;
		$scope.requestToRating={};		
		$scope.isClick = false;
		$scope.setRank = false;
		//note
		$scope.noteLoaded = {};
		$scope.typeNote='Private';
		$scope.notesList = [];
		$scope.profile="";
		$scope.selectedTab={'tab':0};
		$scope.contentNotes = "";
		$scope.dependenciesService = docExecute_dependencyService;
		$scope.crossNavigationScope=$crossNavigationScope;
		$scope.firstExecutionProcessRestV1=true;
		$scope.download=sbiModule_download;
		
		$scope.openInfoMetadata = function() {
			infoMetadataService.openInfoMetadata();
		};

		$scope.initSelectedRole = function() {
			console.log("initSelectedRole IN "); 
			if(execProperties.roles && execProperties.roles.length > 0) {
				 if(execProperties.roles.length==1 || (execProperties.roles.length>1 && !angular.equals(execProperties.selectedRole.name,'')) ) {
					execProperties.selectedRole.name = execProperties.roles[0];
					$crossNavigationScope.changeNavigationRole(execProperties.selectedRole);
					$scope.showSelectRoles = false;					
					//loads parameters if role is selected
					execProperties.isParameterRolePanelDisabled.status = true;
					docExecute_urlViewPointService.getParametersForExecution(execProperties.selectedRole.name, $scope.buildCorrelation,execProperties.executionInstance.CROSS_PARAMETER);
				}else{ 
					docExecute_paramRolePanelService.toggleParametersPanel(true);
				}
				docExecute_urlViewPointService.frameLoaded=false;
				docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name,execProperties.executionInstance.CROSS_PARAMETER);
				$scope.firstExecutionProcessRestV1=false;
				 
			}
			
			console.log("initSelectedRole OUT ");
		};
				
		
		
		
		/*
		 * DEPENDENCIES
		 */
		$scope.dependenciesService.observableVisualParameterArray = [];
		$scope.dependenciesService.observableDataDependenciesArray = [];
		$scope.dependenciesService.visualCorrelationMap = {};
		$scope.dependenciesService.dataDependenciesMap = {};
		
		/*
		 * BUILD CORRELATION
		 * Callback function from service getParameter for visual dependencies
		 */
		$scope.buildCorrelation = function(parameters){			
			docExecute_dependencyService.buildVisualCorrelationMap(parameters);
			docExecute_dependencyService.buildDataDependenciesMap(parameters);
		};
				
		
		
		
		
	 /*
	  * WATCH ON VISUAL DEPENDENCIES PARAMETER OBJECT
	  */
	  $scope.$watch( function() {
		  return $scope.dependenciesService.observableVisualParameterArray;
		},
		function(newValue, oldValue) {
			if (!angular.equals(newValue, oldValue)) {
				for(var i=0; i<newValue.length; i++){
					if(oldValue[i] && (!angular.equals(newValue[i].parameterValue, oldValue[i].parameterValue)) ){
						docExecute_dependencyService.visualCorrelationWatch(newValue[i]);
						break;
					}
					
				}
			}
		},true);
		 
	     /*
		  * WATCH ON DATA DEPENDENCIES PARAMETER OBJECT
		  */
	  $scope.$watch( function() {
		  return $scope.dependenciesService.observableDataDependenciesArray;
		},
		function(newValue, oldValue) {
			if (!angular.equals(newValue, oldValue)) {
				for(var i=0; i<newValue.length; i++){
					//only new value different old value
					if(oldValue[i] && (!angular.equals(newValue[i].parameterValue, oldValue[i].parameterValue)) ){
						docExecute_dependencyService.dataDependenciesCorrelationWatch(newValue[i]);		
						break;
					}
					
				}
			}
		},true);
	  
	  
		
		
		
		//ranking document
		$scope.rankDocument = function() {
			var obj = {
					'obj':$scope.executionInstance.OBJECT_ID
					};
			sbiModule_restServices.promisePost("documentrating", "getvote",obj).then(function(response) { 
				//angular.copy(response.data,$scope.rankDocumentSaved);
				$scope.rankDocumentSaved = response.data;
			},function(response) {
				$mdDialog.cancel();
				$scope.isClick = false;
			});

			$mdDialog.show({
				controller: rankControllerFunction,
				templateUrl:sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentRank.html',
				scope:$scope,
				preserveScope: true,
				clickOutsideToClose:true
			})
			.then(function(answer) {
				$scope.status = 'You said the information was "' + answer + '".';
				$scope.isClick = false;
			}, function() {
				$scope.status = 'You cancelled the dialog.';
				$scope.isClick = false;
			});
		};

		//note document
		$scope.noteDocument = function() {
			var obj = {'id' : $scope.executionInstance.OBJECT_ID};
			sbiModule_restServices
			.promisePost("documentnotes", 'getNote',obj)
			.then(
				function(response) {
					if (response.data.hasOwnProperty("errors")) {
						$scope.showAction(response.data);
					} else {
						console.log(response);
						angular.copy(response.data,$scope.noteLoaded);
						$scope.contentNotes = $scope.noteLoaded.nota;
						$scope.profile = response.data.profile;
					}
				},
				function(response) {
					$scope.errorHandler(response.data,"");
				});

			$mdDialog.show({
				controller: noteControllerFunction,
				templateUrl:sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentNote.html',
				scope:$scope,
				preserveScope: true,
				clickOutsideToClose:true
			})
			.then(function(answer) {
				$scope.status = 'You said the information was "' + answer + '".';
			}, function() {
				$scope.status = 'You cancelled the dialog.';
			});
		};


		$scope.openHelpOnLine = function() {	
			sbiModule_helpOnLine.showDocumentHelpOnLine($scope.executionInstance.OBJECT_LABEL);
		};
					
		/*
		 * EXECUTE PARAMS
		 * Submit param form
		 */
		$scope.executeParameter = function() {
			console.log("executeParameter IN ");
			
			docExecute_urlViewPointService.frameLoaded=false;
			docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name, 
					 documentExecuteServices.buildStringParameters(execProperties.parametersData.documentParameters));			
//			if($mdSidenav('parametersPanelSideNav').isOpen()) {
//				$mdSidenav('parametersPanelSideNav').close();
//				execProperties.showParametersPanel.status = $mdSidenav('parametersPanelSideNav').isOpen();
//			}
//			execProperties.showParametersPanel.status=false;
			docExecute_paramRolePanelService.toggleParametersPanel(false);
			console.log("executeParameter OUT ");
		};
		
		$scope.changeRole = function(role) {
			console.log("changeRole IN ");
			if(role != execProperties.selectedRole.name) {  
				$crossNavigationScope.changeNavigationRole(execProperties.selectedRole);
				docExecute_urlViewPointService.getParametersForExecution(role,$scope.buildCorrelation,execProperties.executionInstance.CROSS_PARAMETER)
				docExecute_urlViewPointService.frameLoaded=false;
				if($scope.firstExecutionProcessRestV1){
					docExecute_urlViewPointService.executionProcesRestV1(role, execProperties.executionInstance.CROSS_PARAMETER);
					$scope.firstExecutionProcessRestV1=false;
				}else{
					docExecute_urlViewPointService.executionProcesRestV1(role, documentExecuteServices.buildStringParameters(execProperties.parametersData.documentParameters));
				}
					 
			}
			console.log("changeRole OUT ");
		};

		$scope.isParameterPanelDisabled = function() {
			return (!execProperties.parametersData.documentParameters || execProperties.parametersData.documentParameters.length == 0);
		};

		$scope.executeDocument = function() {
			console.log('Executing document -> ', execProperties);
		};

		$scope.editDocument = function() {
			alert('Editing document');
			console.log('Editing document -> ', execProperties);
		};

		$scope.deleteDocument = function() {
			alert('Deleting document');
			console.log('Deleting document -> ', execProperties);
		};

		$scope.clearListParametersForm = function() {
			if(execProperties.parametersData.documentParameters.length > 0) {
				for(var i = 0; i < execProperties.parametersData.documentParameters.length; i++) {
					var parameter = execProperties.parametersData.documentParameters[i];
					documentExecuteServices.resetParameter(parameter);
				}
			}
		};

		$scope.printDocument = function() {
			var frame = window.frames["documentFrame"];
			if(frame.print) {
				frame.print();
			}else if(frame.contentWindow) {
				frame.contentWindow.print();
			}
		};
				
		$scope.closeDocument = function() {
			$crossNavigationScope.closeDocument($scope.executionInstance.OBJECT_ID);  
		};

		$scope.iframeOnload = function(){
			docExecute_urlViewPointService.frameLoaded = true;
			if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase != '$digest') {
				$scope.$apply();
			}
		};
		
		$scope.navigateTo= function(item){
			$crossNavigationScope.crossNavigationHelper.navigateTo(item);
		}
		
		$scope.internalNavigateTo= function(params,targetDocLabel){
			$crossNavigationScope.crossNavigationHelper.internalNavigateTo(params,targetDocLabel);
		}
		 
		console.log("documentExecutionControllerFn OUT ");
	};

	documentExecutionApp.directive('iframeSetDimensionsOnload', ['docExecute_urlViewPointService',function(docExecute_urlViewPointService) {
		return {
			scope: {
		        iframeOnload: '&?'
		    },
			restrict: 'A',
			link: function(scope, element, attrs) {
				element.on('load', function() {
					var iFrameHeight = element[0].parentElement.scrollHeight + 'px';
					element.css('height', iFrameHeight);				
					element.css('width', '100%');
					if(scope.iframeOnload)
						 scope.iframeOnload();
				});
			}
		};
	}]);
})();

//from executed document, call this function to exec old cross navigation method
var execCrossNavigation=function(frameid, doclabel, params, subobjid, title, target){
	var jsonEncodedParams=JSON.parse('{"' + decodeURI(params).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"').replace(/\s/g,'') + '"}');
	angular.element(frameElement).scope().$parent.internalNavigateTo(jsonEncodedParams,doclabel);
}

var execExternalCrossNavigation=function(parameters){ 
	angular.element(frameElement).scope().$parent.navigateTo(parameters);
}