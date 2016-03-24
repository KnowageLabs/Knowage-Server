(function() {
	var documentExecutionModule = angular.module('documentExecutionModule');
	
	documentExecutionModule.service('documentExecuteServices', function($mdToast) {
		var documentExecuteServicesObj = {
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

			buildStringParameters : function (documentParameters) {
				var jsonDatum =  {};
				if(documentParameters.length > 0) {
					for(var i = 0; i < documentParameters.length; i++ ) {
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
			
			recursiveChildrenChecks : function(parameterValue, childrenArray) {
				childrenArray = childrenArray || [];
				
				for(var i = 0; i < childrenArray.length; i++) {
					var childItem = childrenArray[i];
					if(childItem.checked && childItem.checked == true) {
						parameterValue.push(childItem);
					}
					
					if(!childItem.leaf) {
						documentExecuteServicesObj.recursiveChildrenChecks(parameterValue, childItem.children);
					}
				}
			},
			
			resetParameterInnerLovData: function(childrenArray) { 
				childrenArray = childrenArray || [];
				
				for(var i = 0; i < childrenArray.length; i++) {
					var childItem = childrenArray[i];
					childItem.checked = false;
					
					if(!childItem.leaf) {
						documentExecuteServicesObj.resetParameterInnerLovData(childItem.children);
					}
				}
			},
			
			resetParameter: function(parameter) {
				if(parameter.valueSelection.toLowerCase() == 'lov') {
					if(parameter.selectionType.toLowerCase() == 'tree') {
						if(parameter.multivalue) {
							parameter.parameterValue = [];
							
							documentExecuteServicesObj.resetParameterInnerLovData(parameter.children);
//							for(var j = 0; j < parameter.children.length; j++) {
//								var child = parameter.children[j];
//								child.checked = false;
//							}
						} else {
							parameter.parameterValue = '';
						}
					} else {
						if(parameter.multivalue) {
							parameter.parameterValue = [];
							
							for(var j = 0; j < parameter.defaultValues.length; j++) {
								var defaultValue = parameter.defaultValues[j];
								defaultValue.isSelected = false;
							}
						} else {
							parameter.parameterValue = '';
						}
					}
				} else {
					parameter.parameterValue = '';
				}
			},
			
			showParameterHtml: function(parameter) {
				if(parameter.selectionType.toLowerCase() == 'tree') {
					if(parameter.multivalue) {
						
						var toReturn = '';
						
						parameter.parameterValue = [];
						
						documentExecuteServicesObj.recursiveChildrenChecks(parameter.parameterValue, parameter.children);
						
						for(var i = 0; i < parameter.parameterValue.length; i++) {
							var parameterValueItem = parameter.parameterValue[i];
							
							if(i > 0) {
								toReturn += ",<br/>";
							}
							toReturn += parameterValueItem.value;
						}
						
						return toReturn;
						
					} else {
						return (parameter.parameterValue && parameter.parameterValue.value)?
								parameter.parameterValue.value : '';
					}
				} else {
					if(parameter.multivalue) {
						parameter.parameterValue = parameter.parameterValue || [];
						var toReturn = parameter.parameterValue.join(",<br/>");
						return toReturn;
					} else {
						parameter.parameterValue = parameter.parameterValue || '';
						return parameter.parameterValue;
					}
				}
			}
		};
		return documentExecuteServicesObj;
	});
	
	documentExecutionModule.service('docExecute_pageviewService', function() {
			this.currentView ='DOCUMENT' ;				
			this.setCurrentView = function(currentView) {
				this.currentView = currentView;
			};
			this.getCurrentView = function() {
				return this.currentView;
			};
	});
	
	documentExecutionModule.service('docExecute_urlViewPointService', function(execProperties,
			sbiModule_restServices, $mdDialog, sbiModule_translate,sbiModule_config
			,$mdSidenav,docExecute_paramRolePanelService,documentExecuteServices,documentExecuteFactories
			) {
		
		var serviceScope = this;	
		
		serviceScope.documentUrl = '';
		
		this.executionProcesRestV1 = function(role, paramsStr) {			
			if(typeof paramsStr === 'undefined') {
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
					if(data['documentError'] && data['documentError'].length > 0 ) {
						//sbiModule_messaging.showErrorMessage(data['documentError'][0].message, 'Error');
						var alertDialog = $mdDialog.alert()
						.title(sbiModule_translate.load("sbi.generic.warning"))
						.content(data['documentError'][0].message).ok(sbiModule_translate.load("sbi.general.ok"));						
						$mdDialog.show( alertDialog );
					}else{
						if(data['errors'].length > 0 ) {
							var strErros='';
							for(var i = 0; i<=data['errors'].length-1;i++) {
								strErros = strErros + data['errors'][i].description + '. \n';
							}
							//sbiModule_messaging.showErrorMessage(strErros, 'Error');
							var alertDialog = $mdDialog.alert()
							.title(sbiModule_translate.load("sbi.generic.warning"))
							.content(strErros).ok(sbiModule_translate.load("sbi.general.ok"));
							$mdDialog.show( alertDialog );
						}else{
							serviceScope.documentUrl = data.url;
							//angular.copy(data.url, serviceScope.documentUrl);
						}	
					}	
					
				}).error(function(data, status, headers, config) {
					console.log("TargetLayer non Ottenuto " + status);
				});
		};
		
		this.getViewpoints = function() {
			execProperties.currentView.status = 'PARAMETERS';
			execProperties.parameterView.status='FILTER_SAVED';
			execProperties.isParameterRolePanelDisabled.status = true;
			
			sbiModule_restServices.get(
					"1.0/documentviewpoint", 
					"getViewpoints",
					"label=" + execProperties.executionInstance.OBJECT_LABEL + "&role="+ execProperties.selectedRole.name)
			.success(function(data, status, headers, config) {	
				console.log('data viewpoints '  ,  data.viewpoints);
				serviceScope.gvpCtrlViewpoints = data.viewpoints;
				execProperties.showParametersPanel.status = false;
				if($mdSidenav('parametersPanelSideNav').isOpen()) {
					$mdSidenav('parametersPanelSideNav').close();
				}
			})
			.error(function(data, status, headers, config) {});																	
		};
		
		
		
		this.getParametersForExecution = function(role, buildCorrelation) {		
			var params = 
				"label=" + execProperties.executionInstance.OBJECT_LABEL
				+ "&role=" + role;
			sbiModule_restServices.get("1.0/documentexecution", "filters", params)
			.success(function(response, status, headers, config) {
				console.log('getParametersForExecution response OK -> ', response);
				//check if document has parameters 
				if(response && response.filterStatus && response.filterStatus.length>0) {
										
					execProperties.showParametersPanel.status = true;
					if(!($mdSidenav('parametersPanelSideNav').isOpen())) {
						$mdSidenav('parametersPanelSideNav').open();
					}
					//build documentParameters
					angular.copy(response.filterStatus, execProperties.parametersData.documentParameters);
					buildCorrelation(execProperties.parametersData.documentParameters);
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
		
		this.createNewViewpoint = function() {
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
							if(data.errors && data.errors.length > 0 ) {
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

				templateUrl : sbiModule_config.contextName 
					+ '/js/src/angular_1.4/tools/documentexecution/templates/dialog-new-parameters-document-execution.html'
			});
		};
	});
	
	documentExecutionModule.service('docExecute_paramRolePanelService', function(execProperties,$mdSidenav) {
				
		this.checkParameterRolePanelDisabled = function() {
			return ((!execProperties.parametersData.documentParameters || execProperties.parametersData.documentParameters.length == 0)
					&& (execProperties.roles.length==1));
		};
		
		this.returnToDocument = function() {
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
		
		this.toggleParametersPanel = function() {
			if(execProperties.showParametersPanel.status) {
				$mdSidenav('parametersPanelSideNav').close();
			} else {
				$mdSidenav('parametersPanelSideNav').open();
			}
			execProperties.showParametersPanel.status = $mdSidenav('parametersPanelSideNav').isOpen();
		};
	});
	
	//DEPENDENCIES
	
	angular.module('documentExecutionModule')
	.service('docExecute_dependencyService', function(execProperties, documentExecuteServices,sbiModule_restServices) {
	
		var serviceScope = this;
	
	
		/*
		 * BUILD DATA DEPENDENCIES 
		 */
			this.buildDataDependenciesMap = function(parameters){
			for(var i=0; i<parameters.length ; i++){
				if(parameters[i].dataDependencies && parameters[i].dataDependencies.length>0){						
					for(var k=0; k<parameters[i].dataDependencies.length; k++){ 
						var dependency = parameters[i].dataDependencies[k];						
						dependency.parameterToChangeUrlName = parameters[i].urlName;
						dependency.parameterToChangeId = this.getRowIdfromUrlName(parameters[i].urlName); 
						dependency.lovParameterMode = parameters[i].selectionType;
						//build visualCorrelationMap : Key is fatherUrlName 
						var keyMap = dependency.objParFatherUrlName;
						if (keyMap in serviceScope.dataDependenciesMap) {
							var dependenciesArr =  serviceScope.dataDependenciesMap[keyMap];
							dependenciesArr.push(dependency);
							serviceScope.dataDependenciesMap[keyMap] = dependenciesArr;
							} else {
								var dependenciesArr = new Array
								dependenciesArr.push(dependency);
								serviceScope.dataDependenciesMap[keyMap] = dependenciesArr;
							}						
					}
				}
			}
			for (var key in serviceScope.dataDependenciesMap) {
				//Fill Array DATA DEPENDENCIES
				var documentParamDependence = execProperties.parametersData.documentParameters[this.getRowIdfromUrlName(key)];
				serviceScope.observableDataDependenciesArray.push(documentParamDependence);	
			}
		}
		
		
		
			 this.dataDependenciesCorrelationWatch = function(value){
			 console.log('modify dependency : ' , value);
				//prendere elemeti dalla mappa e ciclare
				console.log('element key '+ value.urlName , serviceScope.dataDependenciesMap[value.urlName]);
				for(var k=0; k<serviceScope.dataDependenciesMap[value.urlName].length; k++){
					var dataDependenciesElementMap = serviceScope.dataDependenciesMap[value.urlName][k];
					var objPost = {};
					objPost.OBJECT_LABEL = execProperties.executionInstance.OBJECT_LABEL;
					objPost.ROLE=execProperties.selectedRole.name;
					objPost.PARAMETER_ID=dataDependenciesElementMap.parameterToChangeUrlName;
					console.log('mode parameter type ' + dataDependenciesElementMap.lovParameterMode);
					objPost.MODE= (dataDependenciesElementMap.lovParameterMode!='TREE' ) ? 'simple' : 'complete';
					objPost.PARAMETERS=documentExecuteServices.buildStringParameters(execProperties.parametersData.documentParameters);
					sbiModule_restServices.post(
							"1.0/documentExeParameters",
							"getParameters", objPost)
					   .success(function(data, status, headers, config) {
						   if(data.status=="OK"){
							   //from root only visibled element !!! 
							   //set to disabled all default value parameter 
							   for(var z=0; z<execProperties.parametersData.documentParameters.length;z++){
									  if(execProperties.parametersData.documentParameters[z].urlName==data.idParam){
										  for(var y=0;y<execProperties.parametersData.documentParameters[z].defaultValues.length;y++){
											  execProperties.parametersData.documentParameters[z].defaultValues[y].isEnabled=false; 
										  }
										  break;
									  }
									  
								  }
							   //set to enabled the correct default value 
							   if(data.result.root && data.result.root.length>0){
								   for(var p=0; p<data.result.root.length;p++){   
									   console.log("parameter ID : " + data.idParam + " set value " + data.result.root[p].value);
									   for(var z=0; z<execProperties.parametersData.documentParameters.length;z++){
										   if(execProperties.parametersData.documentParameters[z].urlName==data.idParam){
											   for(var y=0;y<execProperties.parametersData.documentParameters[z].defaultValues.length;y++){
												if( execProperties.parametersData.documentParameters[z].defaultValues[y].value==data.result.root[p].value){
													execProperties.parametersData.documentParameters[z].defaultValues[y].isEnabled=true;
												}	  
											   }
											   break;
										   }
									   }
									   
								   }
							   }
							  									   
						   }
					})
					.error(function(data, status, headers, config) {});
					
				}			
		  }
		
		
		
		
		/*
		 * BUILD VISUAL DEPENDENCIES
		 */
		this.buildVisualCorrelationMap = function(parameters){
			for(var i=0; i<parameters.length ; i++){
				if(parameters[i].visualDependencies && parameters[i].visualDependencies.length>0){						
					for(var k=0; k<parameters[i].visualDependencies.length; k++){
						var dependency = parameters[i].visualDependencies[k];
						dependency.parameterToChangeUrlName = parameters[i].urlName;
						dependency.parameterToChangeId = this.getRowIdfromUrlName(parameters[i].urlName); 
						//build visualCorrelationMap : Key is fatherUrlName 
						var keyMap = dependency.objParFatherUrlName;
						if (keyMap in serviceScope.visualCorrelationMap) {
							var dependenciesArr =  serviceScope.visualCorrelationMap[keyMap];
							dependenciesArr.push(dependency);
							serviceScope.visualCorrelationMap[keyMap] = dependenciesArr;
							} else {
								var dependenciesArr = new Array
								dependenciesArr.push(dependency);
								serviceScope.visualCorrelationMap[keyMap] = dependenciesArr;
							}						
					}
				}
			}
			for (var key in serviceScope.visualCorrelationMap) {
				//Fill Array VISUAL DEPENDENCIES
				var documentParamVisualDependency = execProperties.parametersData.documentParameters[this.getRowIdfromUrlName(key)];
				serviceScope.observableVisualParameterArray.push(documentParamVisualDependency);	
			}
		}
	
		this.visualCorrelationWatch = function(value){
			  for(var k=0; k<serviceScope.visualCorrelationMap[value.urlName].length; k++){
					var visualDependency=serviceScope.visualCorrelationMap[value.urlName][k];
				    //id document Parameter to control 
					var idDocumentParameter = visualDependency.parameterToChangeId;
					//value to compare
					var compareValueArr = visualDependency.compareValue.split(",");
					for(var z=0; z<compareValueArr.length; z++){
						var newValueStr = value.parameterValue;
						//conditions								
						var condition = (visualDependency.operation=='contains') 
							? (compareValueArr[z]==newValueStr) : condition=(compareValueArr[z]!=newValueStr); 
						if(condition){
							execProperties.parametersData.documentParameters[idDocumentParameter].label=visualDependency.viewLabel;
							execProperties.parametersData.documentParameters[idDocumentParameter].visible=true;
							//Exit if one conditions is verify 
							break;
						}else{
							execProperties.parametersData.documentParameters[idDocumentParameter].visible=false;
						}								
					}
				}
		  }
	
		//GET ROW ID FROM URL NAME
		this.getRowIdfromUrlName = function(urlName){
			var row=0;
			for(var i=0; i<execProperties.parametersData.documentParameters.length; i++ ){
				if(execProperties.parametersData.documentParameters[i].urlName == urlName){
					row = i;
					break;
				}
			}
			return row;
		}
	
	});
	
	
})();















