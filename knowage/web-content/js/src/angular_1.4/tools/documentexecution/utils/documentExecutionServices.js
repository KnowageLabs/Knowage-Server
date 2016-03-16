(function() {
	angular.module('documentExecutionModule')
	.service('documentExecuteServices', function($mdToast) {
		var obj = {
			decodeRequestStringToJson: function (str) {
				var hash;
				var parametersJson = {};
				var hashes = str.slice(str.indexOf('?') + 1).split('&');
				for (var i = 0; i < hashes.length; i++) {
					hash = hashes[i].split('=');
					parametersJson[hash[0]] = (/^\[.*\]$/).test(hash[1])?
						JSON.parse(hash[1]) : hash[1] ;
				}
				return parametersJson;
			},


			showToast: function(text, time) {
				var timer = time == undefined ? 6000 : time;
				$mdToast.show($mdToast.simple().content(text).position('top').action(
				'OK').highlightAction(false).hideDelay(timer));
			},

			buildStringParameters : function (documentParameters){
				var jsonDatum =  {};
				if(documentParameters.length > 0){
					for(var i = 0; i < documentParameters.length; i++ ){
						var parameter = documentParameters[i];
						var valueKey = parameter.urlName;
						var descriptionKey = parameter.urlName + "_field_visible_description";					
						var jsonDatumValue = null;
						if(parameter.valueSelection.toLowerCase() == 'lov') {
							parameter.parameterValue = parameter.parameterValue || [];
							if(Array.isArray(parameter.parameterValue) && parameter.multivalue) {
								parameter.parameterValue = parameter.parameterValue || [];
								
								jsonDatumValue = parameter.parameterValue;
							} else {
								jsonDatumValue = (typeof parameter.parameterValue === 'undefined')? '' : parameter.parameterValue;
							}
						} else {
							jsonDatumValue = (typeof parameter.parameterValue === 'undefined')? '' : parameter.parameterValue;
						}
						jsonDatum[valueKey] = jsonDatumValue;
						jsonDatum[descriptionKey] = jsonDatumValue;
					}
				}			
				return jsonDatum;
			},
			
			resetParameter: function(parameter) {
				if(parameter.valueSelection.toLowerCase() == 'lov') {
					if(parameter.multivalue) {
						parameter.parameterValue = [];
						
						for(var j = 0; j < parameter.defaultValues.length; j++) {
							var defaultValue = parameter.defaultValues[j];
							defaultValue.isSelected = false;
						}
					} else {
						parameter.parameterValue = '';
					}
				} else {
					parameter.parameterValue = '';
				}
			},
			
			showParameterHtml: function(parameter) {
				if(parameter.valueSelection.toLowerCase() == 'lov' && parameter.multivalue) {
					parameter.parameterValue = parameter.parameterValue || [];
					var toReturn = parameter.parameterValue.join(",<br/>");
					return toReturn;
				} else {
					parameter.parameterValue = parameter.parameterValue || '';
					return parameter.parameterValue;
				}
			}
		};
		return obj;
	});
})();



(function() {
	angular.module('documentExecutionModule')
	.service('docExecute_pageviewService', function() {
				this.currentView ='DOCUMENT' ;				
				this.setCurrentView = function(currentView){
					this.currentView = currentView;
				};
				this.getCurrentView = function(){
					return this.currentView;
				};
	});
})();



(function() {
	angular.module('documentExecutionModule')
	.service('docExecute_urlViewPointService', function(execProperties,
			sbiModule_restServices, $mdDialog, sbiModule_translate,sbiModule_config
			,$mdSidenav,docExecute_paramRolePanelService,documentExecuteServices,documentExecuteFactories) {
		
		var serviceScope = this;	
		
		serviceScope.documentUrl = '';
		
		this.executionProcesRestV1 = function(role, paramsStr) {			
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
					if(data['documentError'] && data['documentError'].length > 0 ){
						//sbiModule_messaging.showErrorMessage(data['documentError'][0].message, 'Error');
						var alertDialog = $mdDialog.alert()
						.title(sbiModule_translate.load("sbi.generic.warning"))
						.content(data['documentError'][0].message).ok(sbiModule_translate.load("sbi.general.ok"));						
						$mdDialog.show( alertDialog );
					}else{
						if(data['errors'].length > 0 ){
							var strErros='';
							for(var i=0; i<=data['errors'].length-1;i++){
								strErros=strErros + data['errors'][i].description + '. \n';
							}
							//sbiModule_messaging.showErrorMessage(strErros, 'Error');
							var alertDialog = $mdDialog.alert()
							.title(sbiModule_translate.load("sbi.generic.warning"))
							.content(strErros).ok(sbiModule_translate.load("sbi.general.ok"));
							$mdDialog.show( alertDialog );
						}else{
							serviceScope.documentUrl=data.url;
							//angular.copy(data.url, serviceScope.documentUrl);
						}	
					}	
					
				}).error(function(data, status, headers, config) {
					console.log("TargetLayer non Ottenuto " + status);
				});
		};
		
		this.getViewpoints = function(){
			execProperties.currentView.status = 'PARAMETERS';
			execProperties.parameterView.status='FILTER_SAVED';
			execProperties.isParameterRolePanelDisabled.status=true;
			sbiModule_restServices.get(
					"1.0/documentviewpoint", 
					"getViewpoints",
					"label=" + execProperties.executionInstance.OBJECT_LABEL + "&role="+ execProperties.selectedRole.name)
			.success(function(data, status, headers, config) {	
				console.log('data viewpoints '  ,  data.viewpoints);
				serviceScope.gvpCtrlViewpoints = data.viewpoints;
			})
			.error(function(data, status, headers, config) {});																	
		};
		
		
		
		this.getParametersForExecution = function(role) {		
			var params = 
				"label=" + execProperties.executionInstance.OBJECT_LABEL
				+ "&role=" + role;
			sbiModule_restServices.get("1.0/documentexecution", "filters", params)
			.success(function(response, status, headers, config){
				console.log('getParametersForExecution response OK -> ', response);
				//check if document has parameters 
				if(response && response.filterStatus && response.filterStatus.length>0){
					//check default parameter control TODO										
					execProperties.showParametersPanel.status=true;
					if(!($mdSidenav('parametersPanelSideNav').isOpen())) {
						$mdSidenav('parametersPanelSideNav').open();
					}
					//execProperties.parametersData.documentParameters = response.filterStatus;
					angular.copy(response.filterStatus, execProperties.parametersData.documentParameters);
					execProperties.isParameterRolePanelDisabled.status = docExecute_paramRolePanelService.checkParameterRolePanelDisabled();
				}else{
					execProperties.showParametersPanel.status = false;
					if($mdSidenav('parametersPanelSideNav').isOpen()) {
						$mdSidenav('parametersPanelSideNav').close();
					}
				}
			})
			.error(function(response, status, headers, config) {
				console.log('getParametersForExecution response ERROR -> ', response);
				sbiModule_messaging.showErrorMessage(response.errors[0].message, 'Error');
			});
		};
		
		
		this.createNewViewpoint = function(){
			$mdDialog.show({
				//scope : serviceScope,
				preserveScope : true,				
				templateUrl : sbiModule_config.contextName + '/js/src/angular_1.4/tools/glossary/commons/templates/dialog-new-parameters-document-execution.html',
				controllerAs : 'vpCtrl',
				controller : function($mdDialog) {
					var vpctl = this;
					vpctl.headerTitle = sbiModule_translate.load("sbi.execution.executionpage.toolbar.saveas");
					vpctl.name = sbiModule_translate.load("sbi.execution.viewpoints.name");
					vpctl.description = sbiModule_translate.load("sbi.execution.viewpoints.description");
					vpctl.visibility = sbiModule_translate.load("sbi.execution.subobjects.visibility");
					vpctl.publicOpt = sbiModule_translate.load("sbi.execution.subobjects.visibility.public");
					vpctl.privateOpt = sbiModule_translate.load("sbi.execution.subobjects.visibility.private");
					vpctl.cancelOpt = sbiModule_translate.load("sbi.ds.wizard.cancel");
					vpctl.submitOpt = sbiModule_translate.load("sbi.generic.update");					
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
						serviceScope.newViewpoint = JSON.parse(JSON.stringify(documentExecuteFactories.EmptyViewpoint));
					};
				},

				templateUrl : sbiModule_config.contextName + '/js/src/angular_1.4/tools/documentexecution/templates/dialog-new-parameters-document-execution.html'
			});
		};
		
		
	});
})();


(function() {
	angular.module('documentExecutionModule')
	.service('docExecute_paramRolePanelService', function(execProperties,$mdSidenav) {
				
		this.checkParameterRolePanelDisabled = function(){
			return ((!execProperties.parametersData.documentParameters || execProperties.parametersData.documentParameters.length == 0)
					&& (execProperties.roles.length==1));
		};
		
		this.returnToDocument = function(){
			execProperties.currentView.status = 'DOCUMENT';
			execProperties.parameterView.status='';
			execProperties.isParameterRolePanelDisabled.status = this.checkParameterRolePanelDisabled();
		};
		
		this.isExecuteParameterDisabled = function() {
			if(execProperties.parametersData.documentParameters.length > 0) {
				for(var i = 0; i < execProperties.parametersData.documentParameters.length; i++ ) {
					if(execProperties.parametersData.documentParameters[i].mandatory 
							&& (!execProperties.parametersData.documentParameters[i].parameterValue
									|| execProperties.parametersData.documentParameters[i].parameterValue == '' )) {
						return true;
					}
				}
			}
			return false
		};
		
		
		this.toggleParametersPanel = function(){
			if(execProperties.showParametersPanel.status) {
				$mdSidenav('parametersPanelSideNav').close();
			} else {
				$mdSidenav('parametersPanelSideNav').open();
			}
			execProperties.showParametersPanel.status = $mdSidenav('parametersPanelSideNav').isOpen();
		};
		
	});
})();



















