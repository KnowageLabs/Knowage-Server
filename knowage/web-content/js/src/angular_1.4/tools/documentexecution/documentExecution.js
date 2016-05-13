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
			 'docExecute_dependencyService','$timeout','docExecute_exportService','$filter','sbiModule_dateServices',documentExecutionControllerFn]);

	function documentExecutionControllerFn(
			$scope, $http, $mdSidenav,$mdDialog,$mdToast, sbiModule_translate, sbiModule_restServices,sbiModule_user, sbiModule_config,
			sbiModule_messaging, execProperties, documentExecuteFactories, sbiModule_helpOnLine,documentExecuteServices
			,docExecute_urlViewPointService,docExecute_paramRolePanelService,infoMetadataService,sbiModule_download,$crossNavigationScope,docExecute_dependencyService,$timeout,docExecute_exportService,$filter,sbiModule_dateServices) {

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
		$scope.sidenavToShow = 'east';
		$scope.sidenavCenter = null;
		$scope.filterDropping = null; 
		
		if ($scope.executionInstance.SidenavOri === 'north'){
			$scope.sidenavCenter = "center left";
			$scope.filterDropping = "row"; 
		}
			
		else{
			$scope.sidenavCenter = "center center";
			$scope.filterDropping = "column";
		}

		$scope.hideProgressCircular = execProperties.hideProgressCircular;
		
		$scope.getSidenavType = function(){
			var xx = execProperties;
			return $scope.sidenavToShow;
		}
		
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
					docExecute_urlViewPointService.getParametersForExecution(execProperties.selectedRole.name, $scope.buildCorrelation,docExecute_urlViewPointService.buildParameterForFirstExecution(execProperties.executionInstance.CROSS_PARAMETER,execProperties.executionInstance.MENU_PARAMETER));
				}else{ 
					docExecute_paramRolePanelService.toggleParametersPanel(true);
				}
				docExecute_urlViewPointService.frameLoaded=false;
				docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name,docExecute_urlViewPointService.buildParameterForFirstExecution(execProperties.executionInstance.CROSS_PARAMETER,execProperties.executionInstance.MENU_PARAMETER));
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
			//INIT VISUAL CORRELATION PARAMS
			for(var i=0; i<parameters.length; i++){
				docExecute_dependencyService.visualCorrelationWatch(parameters[i]);
			}
		};
				
//		
//		  $scope.$watch(function () {
//        	  var elem = angular.element(document.querySelector('#sidenavContent'))[0];
//        	  return elem == undefined ? null : elem.offsetHeight;
//        }, function (newValue, oldValue) {
//        	if(newValue > 50){
//        		var elem2 = angular.element(document.querySelector('#sidenavOri'))[0];
//        		var newHeight = newValue  + 50;
//        		var str = newHeight.toString() + 'px';
//        		var class1 = angular.element(document.querySelector('#sidenavOri'))[0].classList[2];
//        		elem2.scrollHeight = 0 ;
//        		elem2.offsetHeight = 2 * newValue  + 50 ;
//        		elem2.clientHeight = 2 * newValue  + 50 ;
//	       		// angular.element(document.querySelector('#sidenavOri'))[0].offsetHeight = newValue  + 50 ;
//        		elem2.style.height = str ;
//        		angular.element(document.querySelector('#sidenavOri'))[0].scrollHeight = 0 ;
//        		 angular.element(document.querySelector('#sidenavOri'))[0].offsetHeight = 2 * newValue  + 50 ;
//        		 angular.element(document.querySelector('#sidenavOri'))[0].clientHeight = 2 * newValue  + 50 ;
//        		// angular.element(document.querySelector('#sidenavOri'))[0].offsetHeight = newValue  + 50 ;
//        		 angular.element(document.querySelector('#sidenavOri'))[0].style.height = str ;
//        	}
//        }, true);
//		
		
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

		//mail
		$scope.sendMail = function(){
			$mdDialog.show({
				scope:$scope,
				preserveScope: true,
				clickOutsideToClose:true,
				controllerAs : 'sendMailCtrl',
				controller : function($mdDialog) {
					var sendmailctl = this;
					sendmailctl.mail = {};
					sendmailctl.mail.label = $scope.executionInstance.OBJECT_LABEL;
					sendmailctl.mail.docId = $scope.executionInstance.OBJECT_ID;
					sendmailctl.mail.userId = sbiModule_user.userId;
					sendmailctl.mail.MESSAGE = "";
					params = documentExecuteServices.buildStringParameters(execProperties.parametersData.documentParameters);
					params= typeof params === 'undefined' ? {} : params;
					sendmailctl.mail.parameters = params;
					sendmailctl.submit = function() {
						sbiModule_restServices.promisePost(
								"1.0/documentexecutionmail",
								"sendMail", sendmailctl.mail)
						.then(
							function(response) {
									$mdDialog.hide();
									documentExecuteServices.showToast(sbiModule_translate.load("sbi.execution.sendmail.success"), 3000);
							},
						function(response){
								documentExecuteServices.showToast(response.data.errors);
							}	
						);
					};
					
					sendmailctl.annulla = function($event) {
						$mdDialog.hide();
					};
				},

				templateUrl : sbiModule_config.contextName 
					+ '/js/src/angular_1.4/tools/documentexecution/templates/documentSendMail.html'
			});
		}
		
		
		
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
				docExecute_urlViewPointService.getParametersForExecution(role,$scope.buildCorrelation,docExecute_urlViewPointService.buildParameterForFirstExecution(execProperties.executionInstance.CROSS_PARAMETER,execProperties.executionInstance.MENU_PARAMETER));
				docExecute_urlViewPointService.frameLoaded=false;
				if($scope.firstExecutionProcessRestV1){
					docExecute_urlViewPointService.executionProcesRestV1(role,docExecute_urlViewPointService.buildParameterForFirstExecution(execProperties.executionInstance.CROSS_PARAMETER,execProperties.executionInstance.MENU_PARAMETER));
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
					//INIT VISUAL CORRELATION PARAMS
					docExecute_dependencyService.visualCorrelationWatch(parameter);
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
		$scope.isCloseDocumentButtonVisible=function(){
			return $crossNavigationScope.isCloseDocumentButtonVisible();  
		}

		$scope.iframeOnload = function(){
			docExecute_urlViewPointService.frameLoaded = true;
			if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase != '$digest') {
				$scope.$apply();
			}
		};
		
		$scope.navigateTo= function(outputParameters,inputParameters){
			$crossNavigationScope.crossNavigationHelper.navigateTo(outputParameters,inputParameters);
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

var execExternalCrossNavigation=function(outputParameters,inputParameters){ 
	angular.element(frameElement).scope().$parent.navigateTo(outputParameters,inputParameters);
}