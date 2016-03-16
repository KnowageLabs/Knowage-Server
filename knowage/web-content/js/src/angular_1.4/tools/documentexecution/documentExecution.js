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
			['$scope', '$http', '$mdSidenav', '$mdDialog','$mdToast', 'sbiModule_translate', 'sbiModule_restServices', 
			 'sbiModule_config', 'sbiModule_messaging', 'execProperties', 'documentExecuteFactories', 'sbiModule_helpOnLine',
			 'documentExecuteServices','docExecute_urlViewPointService','docExecute_paramRolePanelService','infoMetadataService',
			 documentExecutionControllerFn]);

	function documentExecutionControllerFn(
			$scope, $http, $mdSidenav,$mdDialog,$mdToast, sbiModule_translate, sbiModule_restServices, sbiModule_config,
			sbiModule_messaging, execProperties, documentExecuteFactories, sbiModule_helpOnLine,documentExecuteServices
			,docExecute_urlViewPointService,docExecute_paramRolePanelService,infoMetadataService) {

		console.log("documentExecutionControllerFn IN ");
		$scope.executionInstance = execProperties.executionInstance || {};
		$scope.roles = execProperties.roles;
		$scope.selectedRole = execProperties.selectedRole;
		$scope.execContextId = "";
		//$scope.documentUrl="";
		$scope.showSelectRoles=true;
		$scope.translate = sbiModule_translate;
		$scope.documentParameters = execProperties.parametersData.documentParameters;
		$scope.newViewpoint = JSON.parse(JSON.stringify(documentExecuteFactories.EmptyViewpoint));
		$scope.viewpoints = [];
		$scope.documentExecuteFactories = documentExecuteFactories;
		$scope.documentExecuteServices = documentExecuteServices;
		$scope.paramRolePanelService = docExecute_paramRolePanelService;
		$scope.urlViewPointService = docExecute_urlViewPointService;		
		$scope.currentView = execProperties.currentView;
		$scope.parameterView=execProperties.parameterView;
		$scope.isParameterRolePanelDisabled = execProperties.isParameterRolePanelDisabled;
		$scope.showParametersPanel = execProperties.showParametersPanel;
		$scope.documentSelectedToRanking={};
		$scope.rankDocumentSaved = 0;
		$scope.requestToRating={};		
		$scope.isClick=false;
		
		$scope.openInfoMetadata = function(){
			infoMetadataService.openInfoMetadata();
		}
		
		$scope.initSelectedRole = function(){
			console.log("initSelectedRole IN ");
			if(execProperties.roles && execProperties.roles.length > 0) {
				if(execProperties.roles.length==1) {
					execProperties.selectedRole.name = execProperties.roles[0];
					$scope.showSelectRoles=false;
					//loads parameters if role is selected
					docExecute_urlViewPointService.getParametersForExecution(execProperties.selectedRole.name);
					execProperties.isParameterRolePanelDisabled.status = true;
				}
				docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name);
			}
			
			console.log("initSelectedRole OUT ");
		};
		
		//ranking document
		$scope.rankDocument = function(document){
			$scope.documentSelectedToRanking = document;
			var obj = {
					'obj':$scope.executionInstance.OBJECT_ID
					};
			sbiModule_restServices.promisePost("documentrating","getvote",obj).then(function(response){ 
				//angular.copy(response.data,$scope.rankDocumentSaved);
				$scope.rankDocumentSaved = response.data;
			},function(response){
				$mdDialog.cancel();
				$scope.isClick=false;
			});
			
			$mdDialog.show({
				templateUrl:sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentRank.html',
				scope:$scope,
				preserveScope: true,
				clickOutsideToClose:true
			})
			.then(function(answer) {
				$scope.status = 'You said the information was "' + answer + '".';
				$scope.isClick=false;
			}, function() {
				$scope.status = 'You cancelled the dialog.';
				$scope.isClick=false;
			});
		};
		
		$scope.rateScore=function(value){
			$scope.requestToRating = {
					'rating':value,
					'obj':$scope.executionInstance.OBJECT_ID,
			};
			$scope.isClick=true;
		};
		
		$scope.saveRank = function(){
			sbiModule_restServices.promisePost("documentrating", 'vote',$scope.requestToRating)
			.then(function(response) {
					if (response.data.hasOwnProperty("errors")) {
						$scope.showAction(response.data);
					} else {
						$mdDialog.cancel();
						$scope.showAction(sbiModule_translate.load('sbi.execution.executionpage.toolbar.rating.saved'));
						$scope.isClick=false;
					}
	
				},
				function(response) {
					$scope.isClick=false;
					$scope.errorHandler(response.data,"");
				}
			);
		};
		
		$scope.hoverStar = function(value){
			$scope.isClick=false;
			for(var i=1;i<=value;i++){
				var string= "star"+i;
				angular.element(document.getElementById(string).firstChild).removeClass('fa-star-o');
				angular.element(document.getElementById(string).firstChild).addClass('fa-star');
				
			}
		};
		
		$scope.leaveStar = function(value){
			if(!$scope.isClick){
				for(var i=1;i<=value;i++){
					var string= "star"+i;
					angular.element(document.getElementById(string).firstChild).removeClass('fa-star');
					angular.element(document.getElementById(string).firstChild).addClass('fa-star-o');
				}
			}
		};
		
		$scope.close=function(){
			$mdDialog.cancel();
			$scope.isClick=false;
		};
		
		$scope.showAction = function(text) {
			$scope.isClick=false;
			var toast = $mdToast.simple()
			.content(text)
			.action('OK')
			.highlightAction(false)
			.hideDelay(3000)
			.position('top');

			$mdToast.show(toast).then(function(response) {
				if ( response == 'ok' ) {

				}
			});
		};
		
		$scope.openHelpOnLine=function(){	
			sbiModule_helpOnLine.showDocumentHelpOnLine($scope.executionInstance.OBJECT_LABEL);
		};
					
		/*
		 * EXECUTE PARAMS
		 * Submit param form
		 */
		$scope.executeParameter = function() {
			console.log("executeParameter IN ");
			docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name, JSON.stringify(documentExecuteServices.buildStringParameters(execProperties.parametersData.documentParameters)));			
			if($mdSidenav('parametersPanelSideNav').isOpen()) {
				$mdSidenav('parametersPanelSideNav').close();
				execProperties.showParametersPanel.status = $mdSidenav('parametersPanelSideNav').isOpen();
			}
			console.log("executeParameter OUT ");
		};
		
		$scope.changeRole = function(role) {
			console.log("changeRole IN ");
			if(role != execProperties.selectedRole.name) {  
				docExecute_urlViewPointService.executionProcesRestV1(role);
				docExecute_urlViewPointService.getParametersForExecution(role);
			}
			console.log("changeRole OUT ");
		};
	
		
		$scope.showRequiredFieldMessage = function(parameter) {
			return (
				parameter.mandatory 
				&& (
						!parameter.parameterValue
						|| (Array.isArray(parameter.parameterValue) && parameter.parameterValue.length == 0) 
						|| parameter.parameterValue == '')
				) == true;
		};		

		$scope.isParameterPanelDisabled = function(){
			return (!execProperties.parametersData.documentParameters || execProperties.parametersData.documentParameters.length == 0);
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
		
		$scope.clearListParametersForm = function(){
			if(execProperties.parametersData.documentParameters.length > 0){
				for(var i = 0; i < execProperties.parametersData.documentParameters.length; i++){
					var parameter = execProperties.parametersData.documentParameters[i];
					documentExecuteServices.resetParameter(parameter);
				}
			}
		};
		
		/*
		 * Create new viewpoint document execution
		 */
		$scope.createNewViewpoint = function(){
			$mdDialog.show({
				scope : $scope,
				preserveScope : true,
				
				templateUrl : sbiModule_config.contextName + '/js/src/angular_1.4/tools/glossary/commons/templates/dialog-new-parameters-document-execution.html',
					
				controllerAs : 'vpCtrl',
				controller : function($mdDialog) {
					var vpctl = this;
					vpctl.headerTitle = sbiModule_translate.load("sbi.execution.executionpage.toolbar.saveas");
					vpctl.submit = function() {
						vpctl.newViewpoint.OBJECT_LABEL = execProperties.executionInstance.OBJECT_LABEL;
						vpctl.newViewpoint.ROLE = execProperties.selectedRole.name;
						vpctl.newViewpoint.VIEWPOINT = documentExecuteServices.buildStringParameters(execProperties.parametersData.documentParameters);
						sbiModule_restServices.post(
								"1.0/documentviewpoint",
								"addViewpoint", vpctl.newViewpoint)
						   .success(function(data, status, headers, config) {
							if(data.errors && data.errors.length > 0 ){
								documentExecuteServices.showToast(data.errors[0].message);
							}else{
								$mdDialog.hide();
								documentExecuteServices.showToast(sbiModule_translate.load("sbi.execution.viewpoints.msg.saved"), 3000);
							}							
						})
						.error(function(data, status, headers, config) {
							documentExecuteServices.showToast(sbiModule_translate.load("sbi.execution.viewpoints.msg.error.save"),3000);	
						});
					};
					
					vpctl.annulla = function($event) {
						$mdDialog.hide();
						$scope.newViewpoint = JSON.parse(JSON.stringify(documentExecuteFactories.EmptyViewpoint));
					};
				},

				templateUrl : sbiModule_config.contextName + '/js/src/angular_1.4/tools/documentexecution/templates/dialog-new-parameters-document-execution.html'
			});
		};
				
		console.log("documentExecutionControllerFn OUT ");
	};
	
	documentExecutionApp.directive('iframeSetDimensionsOnload', [function(){
		return {
			restrict: 'A',
			link: function(scope, element, attrs){
				element.on('load', function(){
					var iFrameHeight = element[0].parentElement.scrollHeight + 'px';
					element.css('height', iFrameHeight);				
					element.css('width', '100%');
				});
			}
		};
	}]);
})();	