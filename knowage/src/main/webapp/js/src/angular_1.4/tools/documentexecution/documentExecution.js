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
			['$scope', '$http', '$mdSidenav', '$mdDialog', '$mdToast', 'sbiModule_translate', 'sbiModule_restServices', 'sbiModule_user',
			 'sbiModule_config', 'sbiModule_messaging', 'execProperties', 'documentExecuteFactories', 'sbiModule_helpOnLine',
			 'documentExecuteServices', 'docExecute_urlViewPointService', 'docExecute_paramRolePanelService', 'infoMetadataService', 'sbiModule_download', '$crossNavigationScope',
			 
			 '$timeout', '$interval', 'docExecute_exportService', '$filter', 'sbiModule_dateServices', 'cockpitEditing', '$window', '$httpParamSerializer', '$mdMenu','sbiModule_i18n','sbiModule_device',
			 'driversExecutionService','driversDependencyService', 'datasetPreview_service' ,documentExecutionControllerFn]);

	function documentExecutionControllerFn(
			$scope, $http, $mdSidenav, $mdDialog,$mdToast, sbiModule_translate, sbiModule_restServices,sbiModule_user, sbiModule_config,
			sbiModule_messaging, execProperties, documentExecuteFactories, sbiModule_helpOnLine, documentExecuteServices,
			docExecute_urlViewPointService, docExecute_paramRolePanelService, infoMetadataService, sbiModule_download, $crossNavigationScope,
			$timeout, $interval, docExecute_exportService, $filter, sbiModule_dateServices,
			cockpitEditing, $window, $httpParamSerializer, $mdMenu,sbiModule_i18n, sbiModule_device,driversExecutionService,driversDependencyService, datasetPreview_service) {

		console.log("documentExecutionControllerFn IN ");

		$scope.sbiModule_restServices = sbiModule_restServices;
		$scope.sbiModule_messaging = sbiModule_messaging;

		$scope.showCollaborationMenu = sbiModule_user.functionalities.indexOf("Collaboration")>-1;
		$scope.browser = sbiModule_device.browser;

		$interval(function(){
			let el = angular.element( document.querySelector( '#menu md-menu-content' ) )
			if(Array.isArray(el)) $scope.menuElementLength = el[0].children.length;
			else $scope.menuElementLength = el.children.length;
		},1000)
		
		
		//NAVIGATOR WHEEL
		$scope.navigatorVisibility = false;
		$scope.toggleNavigator = function(e) {
			$scope.navigatorStyle = {
					"left" : (e.pageX-150)+'px'
			}
			$scope.navigatorVisibility = $scope.navigatorVisibility?false:true;
		}
		$scope.goBackHome = function(){
			$crossNavigationScope.crossNavigationHelper.crossNavigationSteps.stepControl.resetBreadCrumb();
			//$window.location.href = "http://localhost:8080/knowage/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ACTION&SBI_ENVIRONMENT=DOCBROWSER&OBJECT_LABEL=PADRE_CROSS&OBJECT_NAME=Home%20page&IS_SOURCE_DOCUMENT=true";
			$window.location.href = "http://161.27.39.83:8080/knowage/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ANGULAR_ACTION&SBI_ENVIRONMENT=DOCBROWSER&OBJECT_LABEL=HOME_PAGE&OBJECT_NAME=Home%20page&IS_SOURCE_DOCUMENT=true";
		}

		$scope.execProperties = execProperties;
		$scope.cockpitEditing = cockpitEditing;
		$scope.executionInstance = execProperties.executionInstance || {};
		$scope.roles = execProperties.roles;
		$scope.selectedRole = execProperties.selectedRole;
		$scope.execContextId = "";
		$scope.showSelectRoles = true;
		$scope.translate = sbiModule_translate;
		$scope.i18n = sbiModule_i18n;
		$scope.documentParameters = execProperties.parametersData.documentParameters;
		$scope.newViewpoint = JSON.parse(JSON.stringify(documentExecuteFactories.EmptyViewpoint));
		$scope.viewpoints = [];
		$scope.documentExecuteFactories = documentExecuteFactories;
		$scope.documentExecuteServices = documentExecuteServices;
		$scope.paramRolePanelService = docExecute_paramRolePanelService;
		$scope.urlViewPointService = docExecute_urlViewPointService;
		$scope.driversExecutionService = driversExecutionService;
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
		$scope.exportService = docExecute_exportService;
		$scope.crossNavigationScope=$crossNavigationScope;
		$scope.firstExecutionProcessRestV1=true;
		$scope.download=sbiModule_download;
		$scope.sidenavToShow = 'east';
		$scope.sidenavCenter = null;
		$scope.filterDropping = null;

		$scope.canRate = (sbiModule_user.functionalities.indexOf("EnableToRate") > 0) ? true : false;
		$scope.canPrintDocuments = (sbiModule_user.functionalities.indexOf("EnableToPrint") > 0) ? true : false;
		$scope.canCopyAndEmbedLink = (sbiModule_user.functionalities.indexOf("EnableToCopyAndEmbed") > 0) ? true : false;

		/**
		 * Add these 'documentExecutionNg.jsp' Javascript variables to the scope of the document execution controller and use them
		 * for managing the view part of the application (e.g. whether the "Add to my workspace" document execution menu option (or
		 * some other one) should be shown). They will be used for binding on this JSP page.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.executedFrom = executedFrom.toUpperCase();
		$scope.isAdmin = isAdmin;
		$scope.isSuperAdmin = isSuperAdmin;
		$scope.isAbleToExecuteAction = isAbleToExecuteAction;
		$scope.addToWorkspaceEnabled = (sbiModule_user.functionalities.indexOf("SaveIntoFolderFunctionality")>-1)? true:false;
		$scope.showScheduled = ((sbiModule_user.functionalities.indexOf("SeeSnapshotsFunctionality")>-1) || (sbiModule_user.functionalities.indexOf("SchedulerManagement")>-1)) && isNotOlapDoc ? true : false;

		//navigation default parameters
		$scope.navigatorEnabled 	= false;
		$scope.navigatorVisibility 	= false;

		//menu Toggle override
		$scope.closeMdMenu = function() { $mdMenu.hide(); };


		$scope.execute = function(role, params) {
			docExecute_urlViewPointService.executionProcesRestV1(role, params);
			docExecute_paramRolePanelService.toggleParametersPanel(false);
		}

		
		window.addEventListener("message", (event) => {
			if(event.data.type && event.data.type === 'htmlLink') {
				$scope.copyLinkHTML(event.data.embedHTML); 
			}
		})


		$scope.isOrganizerEnabled = function () {
			if(!$scope.addToWorkspaceEnabled){
				return false
			} else {
				return !($scope.executedFrom=='WORKSPACE_ORGANIZER'|| isAdmin || isSuperAdmin)
			}
		}

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
			var isRoleSelected =false;
			if(execProperties.roles && execProperties.roles.length > 0) {
				if(!angular.equals(execProperties.selectedRole.name,'')){
						for(role in execProperties.roles){
							if(angular.equals(execProperties.selectedRole.name,execProperties.roles[role])){
								isRoleSelected = true;
								break;
							}
						}
						if(!isRoleSelected){
							execProperties.selectedRole.name="";

						}
				}

				if(execProperties.roles.length==1 || (execProperties.roles.length>1 && isRoleSelected) ) {

					execProperties.selectedRole.name = isRoleSelected ? execProperties.selectedRole.name : execProperties.roles[0];
					$crossNavigationScope.changeNavigationRole(execProperties.selectedRole);
					$scope.showSelectRoles = false;
					//loads parameters if role is selected
					execProperties.isParameterRolePanelDisabled.status = true;
					docExecute_urlViewPointService.getParametersForExecution(execProperties.selectedRole.name, driversDependencyService.buildCorrelation,docExecute_urlViewPointService.buildParameterForFirstExecution(execProperties.executionInstance.CROSS_PARAMETER,execProperties.executionInstance.MENU_PARAMETER));
				}else{
					docExecute_paramRolePanelService.toggleParametersPanel(true);
				}
				docExecute_urlViewPointService.frameLoaded=false;
				// TODO controllare a cosa serve
				//docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name,docExecute_urlViewPointService.buildParameterForFirstExecution(execProperties.executionInstance.CROSS_PARAMETER,execProperties.executionInstance.MENU_PARAMETER));
				$scope.firstExecutionProcessRestV1=false;

			}

			console.log("initSelectedRole OUT ");
		};




		/*
		 * DEPENDENCIES
		 */
//		driversDependencyService.parametersWithVisualDependency = [];
//		driversDependencyService.parametersWithDataDependency = [];
//		driversDependencyService.visualCorrelationMap = {};
//		driversDependencyService.dataDependenciesMap = {};
//		driversDependencyService.parametersWithLovDependeny = [];
//		driversDependencyService.lovCorrelationMap = {};

		/*
		 * BUILD CORRELATION
		 * Callback function from service getParameter for visual dependencies
		 */
//		$scope.buildCorrelation = function(parameters){
//			driversDependencyService.buildVisualCorrelationMap(parameters,execProperties);
//			driversDependencyService.buildDataDependenciesMap(parameters,execProperties);
//			driversDependencyService.buildLovCorrelationMap(parameters,execProperties);
//			//INIT VISUAL CORRELATION PARAMS
//			for(var i=0; i<parameters.length; i++){
//				driversDependencyService.updateVisualDependency(parameters[i],execProperties);
//			}
//		};


		 /*
		  * WATCH ON LOV DEPENDENCIES PARAMETER OBJECT
		  */
		  $scope.$watch( function() {
			  return driversDependencyService.parametersWithLovDependeny;
			},
			function(newValue, oldValue) {
				if (!angular.equals(newValue, oldValue)) {
					for(var i=0; i<newValue.length; i++){
						if(oldValue[i] && (!angular.equals(newValue[i].parameterValue, oldValue[i].parameterValue)) ){
							driversDependencyService.updateLovValues(newValue[i],execProperties);
							break;
						}

					}
				}
			},true);




	 /*
	  * WATCH ON VISUAL DEPENDENCIES PARAMETER OBJECT
	  */
		$scope.$watch( function() {
			return driversDependencyService.parametersWithVisualDependency;
		},
		function(newValue, oldValue) {
			if (!angular.equals(newValue, oldValue)) {
				for(var i=0; i<newValue.length; i++){
					if(oldValue[i] && (!angular.equals(newValue[i].parameterValue, oldValue[i].parameterValue)) ){
						driversDependencyService.updateVisualDependency(newValue[i],execProperties);
						break;
					}

				}
			}
		},true);

	     /*
		  * WATCH ON DATA DEPENDENCIES PARAMETER OBJECT
		  */
		$scope.$watch( function() {
			return driversDependencyService.parametersWithDataDependency;
		},
		// new value and old Value are the whole parameters
		function(newValue, oldValue) {
			if (!angular.equals(newValue, oldValue)) {
				for(var i=0; i<newValue.length; i++){

					var oldValPar = oldValue[i];
					var newValPar = newValue[i];

					//only new value different old value
					if(oldValPar && (!angular.equals(newValPar, oldValPar)) ){

						var oldParValue = oldValPar.parameterValue;
						var newParValue = newValPar.parameterValue;

						if(oldParValue == undefined || oldParValue == "" ||
								(oldParValue && (!angular.equals(newParValue, oldParValue)))
								){
							driversDependencyService.updateDependencyValues(newValPar,execProperties);
						}
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
				templateUrl:sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/documentbrowser/template/documentRank.html',
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

		$scope.urlEncode = function(o){
		    if(!o){
		        return "";
		    }
		    var buf = [];
		    for(var key in o){
		        var ov = o[key], k = encodeURIComponent(key);
		        var type = typeof ov;
		        if(type == 'undefined'){
		            buf.push(k, "=&");
		        }else if(type != "function" && type != "object"){
		            buf.push(k, "=", encodeURIComponent(ov), "&");
		        }else if(ov instanceof Array){
		            if (ov.length) {
		                for(var i = 0, len = ov.length; i < len; i++) {
		                    buf.push(k, "=", encodeURIComponent(ov[i] === undefined ? '' : ov[i]), "&");
		                }
		            } else {
		                buf.push(k, "=&");
		            }
		        }
		    }
		    buf.pop();
		    return buf.join("");
		},

		//mail
		$scope.copyLinkHTML = function(embedHTML){
			var config = sbiModule_config;
			var host = sbiModule_config.host;
			var context = sbiModule_config.contextName;
			var adapter = sbiModule_config.adapterPathNoContext;
			var tenant = sbiModule_user.tenant;
			var label = $scope.executionInstance.OBJECT_LABEL;

			var parametersO = driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters);
			//var parameters = encodeURIComponent(JSON.stringify(parametersO)).replace(/'/g,"%27").replace(/"/g,"%22").replace(/%3D/g,"=").replace(/%26/g,"&");
			// var parameters = $httpParamSerializer(parametersO);
			var parameters = Object.keys(parametersO).map(e => "" + e + "=" + parametersO[e]).join("&");

			var passToService = {};
			passToService.label = label;

			sbiModule_restServices.promisePost("1.0/documentexecution", "canHavePublicExecutionUrl", passToService)
			.then(function(response, status, headers, config) {
				console.log('getParametersForExecution response OK -> ', response);

				var canExec = response.data.isPublic;

				var publicStr = canExec == true ? "/public" : "";

				if(host.endsWith("/")){
					host = host.substring(0, host.length - 1);
				}

				var url = host
				+ context
				+ publicStr
				+ adapter
				+ "?"
				+ "ACTION_NAME=EXECUTE_DOCUMENT_ACTION"
				+  "&OBJECT_LABEL="+label
				+ "&TOOLBAR_VISIBLE=true"
				+ "&ORGANIZATION="+tenant
				+ "&NEW_SESSION=true";


				if(parameters != undefined && parameters != ''){
					url += "&PARAMETERS=" + encodeURIComponent(parameters);
				}

				var urlToSend;

				if(embedHTML == true){
					urlToSend = "<iframe width=\"600\" height=\"600\" ";
					urlToSend += "\n";
					urlToSend += "     src="+url;
					urlToSend += "\n";
					urlToSend +="      frameborder=\"0\">";
					urlToSend += "\n";
					urlToSend += "</iframe>";
				}
				else{
					urlToSend = url;
				}

				$mdDialog.show({
					locals: {publicUrl: urlToSend, embedHTML: embedHTML, isPublic: canExec},
					//flex: 80,
					templateUrl: sbiModule_config.dynamicResourcesBasePath+"/angular_1.4/tools/documentexecution/templates/publicExecutionUrl.html",
					parent: angular.element(document.body),
					clickOutsideToClose:true,
					escapeToClose :true,
					preserveScope: true,
					fullscreen: true,
					controller: publicExecutionUrlControllerFunction
				});

//				else {
//					sbiModule_messaging.showWarningMessage(sbiModule_translate.load("sbi.execution.publicUrlExecutionEnable"), sbiModule_translate.load('sbi.generic.warning'));
//				}

			},function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"error while checking if public url can be delivered")
			});

		}



		//mail
		$scope.sendMail = function(){
			$mdDialog.show({
				scope:$scope,
				preserveScope: true,
				clickOutsideToClose:true,
				controllerAs : 'sendMailCtrl',
				controller : function($mdDialog) {
					var sendmailctl = this;
					sendmailctl.loaded = true;
					sendmailctl.mail = {};
					sendmailctl.mail.label = $scope.executionInstance.OBJECT_LABEL;
					sendmailctl.mail.docId = $scope.executionInstance.OBJECT_ID;
					sendmailctl.mail.userId = sbiModule_user.userId;
					sendmailctl.mail.MESSAGE = "";
					params = driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters);
					params= typeof params === 'undefined' ? {} : params;
					sendmailctl.mail.parameters = params;
					sendmailctl.submit = function() {
						sbiModule_restServices
						.promisePost("1.0/documentexecutionmail", "sendMail", sendmailctl.mail)
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

				templateUrl : sbiModule_config.dynamicResourcesBasePath
				+ '/angular_1.4/tools/documentexecution/templates/documentSendMail.html'
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
				templateUrl:sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/documentbrowser/template/documentNote.html',
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


		$scope.checkHelpOnline = function(){
			return sbiModule_user.isAbleTo("Glossary");
		}

		$scope.openHelpOnLine = function() {
			sbiModule_helpOnLine.showDocumentHelpOnLine($scope.executionInstance.OBJECT_LABEL);
		};

		$scope.execShowHelpOnLine = function(data) {
			sbiModule_helpOnLine.show(data);
		};

		//davverna - mcortella: toggle visibility of the navigator between documents
		$scope.openNavigator = function(){
			$scope.navigatorVisibility = $scope.navigatorVisibility ? false: true;
		}

		/*
		 * EXECUTE PARAMS
		 * Submit param form
		 */
		$scope.executeParameter = function(refresh) {
			console.log("executeParameter IN ");

			var action = function() {
				docExecute_urlViewPointService.frameLoaded=false;
				cockpitEditing.documentMode="VIEW";
				docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name, driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters));
				docExecute_paramRolePanelService.toggleParametersPanel(false);
			};

			if($scope.cockpitEditing.documentMode == "EDIT"){
				if(refresh) return;
				var confirm = $mdDialog.confirm()
						.title(sbiModule_translate.load('sbi.execution.executionpage.toolbar.editmode'))
						.content(sbiModule_translate.load('sbi.execution.executionpage.toolbar.editmode.quit'))
						.ariaLabel('Leave edit mode')
						.ok(sbiModule_translate.load("sbi.general.continue"))
						.cancel(sbiModule_translate.load("sbi.general.cancel"));

				$mdDialog.show(confirm).then(function(){action.call()});
			}else{
				action.call();
			}

			console.log("executeParameter OUT ");
		};

		$scope.exportCsv = function(){
			var body = {};

			body.documentId=$scope.executionInstance.OBJECT_ID;
			body.documentLabel=$scope.executionInstance.OBJECT_LABEL;
			body.exportType="CSV";

			body.parameters={};

			for(var i =0 ; i<execProperties.parametersData.documentParameters.length; i++){
				if(execProperties.parametersData.documentParameters[i].parameterValue==undefined){
					execProperties.parametersData.documentParameters[i].parameterValue = "";
				}

				var parValue = execProperties.parametersData.documentParameters[i].parameterValue.constructor == Array ? execProperties.parametersData.documentParameters[i].parameterValue.join(","): execProperties.parametersData.documentParameters[i].parameterValue;
				if(execProperties.parametersData.documentParameters[i].type == 'DATE'){
					var dateToSubmitFilter = $filter('date')(execProperties.parametersData.documentParameters[i].parameterValue, sbiModule_config.serverDateFormat);
					if( Object.prototype.toString.call( dateToSubmitFilter ) === '[object Array]' ) {
						execProperties.parametersData.documentParameters[i].parameterValue = dateToSubmitFilter[0];
					}else{
						execProperties.parametersData.documentParameters[i].parameterValue = dateToSubmitFilter;
					}

				}
				body.parameters[execProperties.parametersData.documentParameters[i].urlName] = execProperties.parametersData.documentParameters[i].parameterValue.constructor == Array ? execProperties.parametersData.documentParameters[i].parameterValue.join(","): execProperties.parametersData.documentParameters[i].parameterValue;;
			}

			$scope.sbiModule_messaging.showInfoMessage("The download has started in background. You will find the result file in your download page.");
			$scope.sbiModule_restServices.promisePost("2.0/export","cockpitData",body).then(function(response){

			},function(response){

			})
		}

		/* This will set the refresh rate for the current document, based on the refresh seconds field set by user */
		if($scope.executionInstance.REFRESH_SECONDS != undefined && $scope.executionInstance.REFRESH_SECONDS > 0)
		$interval(function(){
			console.log("reload");
			$scope.executeParameter(true);
			},$scope.executionInstance.REFRESH_SECONDS*1000);

		$scope.changeRole = function(role) {
			console.log("changeRole IN ");
			if(role != execProperties.selectedRole.name) {
				$crossNavigationScope.changeNavigationRole(execProperties.selectedRole);
				docExecute_urlViewPointService.getParametersForExecution(role,driversDependencyService.buildCorrelation,docExecute_urlViewPointService.buildParameterForFirstExecution(execProperties.executionInstance.CROSS_PARAMETER,execProperties.executionInstance.MENU_PARAMETER));
				docExecute_urlViewPointService.frameLoaded=false;
				if($scope.firstExecutionProcessRestV1){
					docExecute_urlViewPointService.executionProcesRestV1(role,docExecute_urlViewPointService.buildParameterForFirstExecution(execProperties.executionInstance.CROSS_PARAMETER,execProperties.executionInstance.MENU_PARAMETER));
					$scope.firstExecutionProcessRestV1=false;
				}else{
					docExecute_urlViewPointService.executionProcesRestV1(role, driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters));				}

			}
			console.log("changeRole OUT ");
		};
		$scope.getExecProperties = function(){
			return execProperties;
		}

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
					driversExecutionService.resetParameter(parameter, true);
					//INIT VISUAL CORRELATION PARAMS
					driversDependencyService.updateVisualDependency(parameter,execProperties);
				}
			}
		};

		$scope.printDocument = function() {
			var frame = window.frames["documentFrame"];
			if(frame.print) {
				frame.print();
			} else if(frame.contentWindow) {
				frame.contentWindow.print();
			}
		};

		$scope.closeDocument = function() {
			var action = function() {
				$crossNavigationScope.closeDocument($scope.executionInstance.OBJECT_ID);
			};

			if($scope.cockpitEditing.documentMode == "EDIT"){
				var confirm = $mdDialog.confirm()
						.title(sbiModule_translate.load('sbi.execution.executionpage.toolbar.editmode'))
						.content(sbiModule_translate.load('sbi.execution.executionpage.toolbar.editmode.quit'))
						.ariaLabel('Leave edit mode')
						.ok(sbiModule_translate.load("sbi.general.continue"))
						.cancel(sbiModule_translate.load("sbi.general.cancel"));

				$mdDialog.show(confirm).then(function(){action.call()});
			}else{
				action.call();
			}
		};

		$scope.isCloseDocumentButtonVisible=function(){
			return $crossNavigationScope.isCloseDocumentButtonVisible();
		};

		if($scope.browser.name == 'internet explorer'){
			document.getElementById('documentFrame').onload = function() {
				$scope.iframeOnload();
		    }
		}

		$scope.iframeOnload = function(){
			docExecute_urlViewPointService.frameLoaded = true;
			if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase != '$digest') {
				$scope.$apply();
			}
		};

		$scope.hasValidOutputTypeParameter = function() {
			if($scope.browser.name != 'internet explorer'){
				var url = new URL(location.origin + execProperties.documentUrl);
	            if(url.searchParams.has("outputType")) {
	            	var outputType = url.searchParams.get("outputType").toUpperCase();
	            	for(var i=0; i<docExecute_urlViewPointService.exportation.length; i++) {
	            		var exportType = docExecute_urlViewPointService.exportation[i];
	            		if(exportType.description.toUpperCase() === outputType) {
	            		    return true;
	            		}
	            	}
	            }
	            return false;
			}
		    return false;
		};

		$scope.previewDataset = function(datasetLabel, parameters, directDownload) {
			datasetPreview_service.previewDataset(datasetLabel, parameters, directDownload);
		}

		$scope.navigateTo= function(outputParameters,inputParameters,targetCrossNavigation,docLabel, otherOutputParameters){
			$crossNavigationScope.crossNavigationHelper.navigateTo(outputParameters,execProperties.parametersData.documentParameters,targetCrossNavigation,docLabel,otherOutputParameters);
//			$crossNavigationScope.crossNavigationHelper.navigateTo(outputParameters,inputParameters,targetCrossNavigation,docLabel);
		};

		$scope.internalNavigateTo= function(params,targetDocLabel){
			$crossNavigationScope.crossNavigationHelper.internalNavigateTo(params,targetDocLabel);
		};

		console.log("documentExecutionControllerFn OUT ");
	};

	documentExecutionApp.directive('iframeSetDimensionsOnload', ['docExecute_urlViewPointService','execProperties','sbiModule_device',function(docExecute_urlViewPointService, execProperties, sbiModule_device) {
		return {
			scope: {
				iframeOnload: '&?'
			},
			restrict: 'A',
			link: function(scope, element, attrs) {
				// check browser and output type, cas eof internet needs different behaviour
				var browser = sbiModule_device.browser;

				if(browser.name == 'internet explorer'){
					element.css('height', '100%');
					element.css('width', '100%');
					docExecute_urlViewPointService.frameLoaded = true;
					//scope.iframeOnload();
				}
				else{
					element.on('load', function() {
						// var iFrameHeight = element[0].parentElement.scrollHeight + 'px';
						// changed to height 100% because of phantomjs rendering errors
//						element.css('height', '100%');
//						element.css('width', '100%');
						if(scope.iframeOnload)
							scope.iframeOnload();
					});
				}
			}
		};
	}]);











})();

//from executed document, call this function to exec old cross navigation method
//from executed document, call this function to exec old cross navigation method
var execCrossNavigation=function(frameid, doclabel, params, subobjid, title, target){
	var jsonEncodedParams=params?JSON.parse('{"' + decodeURI(params).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}'):{};
	var parent = angular.element(frameElement).scope().$parent;
	while(parent != undefined){
		if(parent.internalNavigateTo != undefined){
			break;
		}
		parent = parent.$parent;
	}
	parent.internalNavigateTo(jsonEncodedParams,doclabel);
};

var execExternalCrossNavigation=function(outputParameters,inputParameters,targetCrossNavigation,docLabel,otherOutputParameters){
	
		var currentScope = angular.element(frameElement).scope();
		while(currentScope != undefined){
			if(currentScope.navigateTo != undefined){
				break;
			}
			currentScope = currentScope.$parent;
		}
		if(!currentScope){
			currentScope = angular.element(document.querySelector('#documentFrame')).scope();
		}
		currentScope.navigateTo(outputParameters,inputParameters,targetCrossNavigation,docLabel,otherOutputParameters);	
	
};

var execPreviewDataset = function(datasetLabel, parameters, directDownload) {
	var parent = angular.element(frameElement).scope().$parent;
	while(parent != undefined){
		if(parent.previewDataset != undefined){
			break;
		}
		parent = parent.$parent;
	}
	parent.previewDataset(datasetLabel, parameters, directDownload);
}

var execShowHelpOnLine=function(data){
	var parent = angular.element(frameElement).scope().$parent;
	while(parent != undefined){
		if(parent.execShowHelpOnLine != undefined){
			break;
		}
		parent = parent.$parent;
	}
	parent.execShowHelpOnLine(data);
}
