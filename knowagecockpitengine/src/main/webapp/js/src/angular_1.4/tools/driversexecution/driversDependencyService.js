(function() {
	var driversExecutionModule = angular.module('driversExecutionModule');
		driversExecutionModule.service('driversDependencyService', ['driversExecutionService', 'sbiModule_restServices', function(driversExecutionService, sbiModule_restServices){
			var dependencyService = {};
			dependencyService.parametersWithVisualDependency = [];
			dependencyService.parametersWithDataDependency = [];
			dependencyService.visualCorrelationMap = {};
			dependencyService.dataDependenciesMap = {};
			dependencyService.parametersWithLovDependeny = [];
			dependencyService.lovCorrelationMap = {};


			dependencyService.buildDataDependenciesMap = function(parameters){

				for(var i=0; i < parameters.length ; i++){
					if(parameters[i].dataDependencies && parameters[i].dataDependencies.length>0){
						for(var k=0; k<parameters[i].dataDependencies.length; k++){
							var dependency = parameters[i].dataDependencies[k];
							dependency.parameterToChangeUrlName = parameters[i].urlName;
							dependency.parameterToChangeId = getArrayIndexByDriverUrlName(parameters[i].urlName,parameters);
							dependency.lovParameterMode = parameters[i].selectionType;
							var keyMap = dependency.parFatherUrlName;
							if (keyMap in dependencyService.dataDependenciesMap) {
								var dependenciesArr =  dependencyService.dataDependenciesMap[keyMap];
								dependenciesArr.push(dependency);
								dependencyService.dataDependenciesMap[keyMap] = dependenciesArr;
							} else {
								var dependenciesArr = new Array
								dependenciesArr.push(dependency);
								dependencyService.dataDependenciesMap[keyMap] = dependenciesArr;
							}
						}
					}
				}

				for (var key in dependencyService.dataDependenciesMap) {
					var documentParamDependence = parameters[getArrayIndexByDriverUrlName(key,parameters)];
					dependencyService.parametersWithDataDependency.push(documentParamDependence);
				}
			};

			dependencyService.buildCorrelation = function(parameters, execProperties){
				dependencyService.buildVisualCorrelationMap(parameters);
				dependencyService.buildDataDependenciesMap(parameters);
				dependencyService.buildLovCorrelationMap(parameters);
				//INIT VISUAL CORRELATION PARAMS
				for(var i=0; i<parameters.length; i++){
					dependencyService.updateVisualDependency(parameters[i],execProperties);
				}
			};

			dependencyService.buildLovCorrelationMap = function(parameters){
				for(var i=0; i<parameters.length ; i++){
					if(parameters[i].lovDependencies && parameters[i].lovDependencies.length>0){
						for(var k=0; k<parameters[i].lovDependencies.length; k++){
							var dependency = {};
							dependency.parFatherUrlName = parameters[i].lovDependencies[k];
							dependency.parameterToChangeUrlName = parameters[i].urlName;
							var keyMap = dependency.parFatherUrlName; //
							if (keyMap in dependencyService.lovCorrelationMap) {
								var dependenciesArr =  dependencyService.lovCorrelationMap[keyMap];
								dependenciesArr.push(dependency);
								dependencyService.lovCorrelationMap[keyMap] = dependenciesArr;
							} else {
								var dependenciesArr = new Array
								dependenciesArr.push(dependency);
								dependencyService.lovCorrelationMap[keyMap] = dependenciesArr;
							}
						}
					}
				}
				for (var key in dependencyService.lovCorrelationMap) {
					var documentParamLovDependency = parameters[getArrayIndexByDriverUrlName(key,parameters)];
					dependencyService.parametersWithLovDependeny.push(documentParamLovDependency);
				}
			};

			dependencyService.buildVisualCorrelationMap = function(parameters){
				for(var i=0; i<parameters.length ; i++){
					if(parameters[i].visualDependencies && parameters[i].visualDependencies.length>0){
						for(var k=0; k<parameters[i].visualDependencies.length; k++){
							var dependency = parameters[i].visualDependencies[k];
							dependency.parameterToChangeUrlName = parameters[i].urlName;
							dependency.parameterToChangeId = getArrayIndexByDriverUrlName(parameters[i].urlName,parameters);
							var keyMap = dependency.parFatherUrlName;
							if (keyMap in dependencyService.visualCorrelationMap) {
								var dependenciesArr =  dependencyService.visualCorrelationMap[keyMap];
								dependenciesArr.push(dependency);
								dependencyService.visualCorrelationMap[keyMap] = dependenciesArr;
							} else {
								var dependenciesArr = new Array
								dependenciesArr.push(dependency);
								dependencyService.visualCorrelationMap[keyMap] = dependenciesArr;
							}
						}
					}
				}
				for (var key in dependencyService.visualCorrelationMap) {
					var documentParamVisualDependency = parameters[getArrayIndexByDriverUrlName(key,parameters)];
					dependencyService.parametersWithVisualDependency.push(documentParamVisualDependency);
				}
			};


			dependencyService.buildParameterLovDependencies = function(execProperties){
				var obj = {};
				var parameters = execProperties.parametersData.documentParameters
				if(parameters && parameters.length>0
							  && parameters[0].parameterValue
							  && parameters[0].parameterValue.length>0
							  && parameters[0].parameterValue[0].value){

					var objToSend = parameters;
					for(var l=0; l<parameters.length; l++){
						var paramValueArr = parameters[l].parameterValue;
						var paramValueArrNew = [];
						if(parameters[l].parameterValue){
							for(var t=0; t<parameters[l].parameterValue.length; t++){
								paramValueArrNew.push(parameters[l].parameterValue[t].value);
							}
							objToSend[l].parameterValue = paramValueArrNew;
						}
					}
					obj=driversExecutionService.buildStringParameters(objToSend);
				}else{
					obj=driversExecutionService.buildStringParameters(parameters);
				}
				return obj;
			};


			dependencyService.updateVisualDependency = function(value,execProperties){
				if(dependencyService.visualCorrelationMap[value.urlName]){
					var destinationOk = {};
					for(var k=0; k < dependencyService.visualCorrelationMap[value.urlName].length; k++){
						var visualDependency=dependencyService.visualCorrelationMap[value.urlName][k];
						var idDocumentParameter = visualDependency.parameterToChangeId;
						var compareValueArr = visualDependency.compareValue.split(",");
						for(var z=0; z<compareValueArr.length; z++){
							if(destinationOk[idDocumentParameter]){
								break;
							}
							var newValueStr = value.parameterValue;
							var compareValueStr=compareValueArr[z].trim();
							var condition = false;
							if(angular.isArray(newValueStr)) {
								if(visualDependency.operation=='contains') {
									for(var l=0; l<newValueStr.length; l++){
										if(compareValueStr==newValueStr[l]){
											condition=true;
											break;
										}
									}
								} else { //not contains
									condition=true;
									for(var l=0; l<newValueStr.length; l++){
										if(compareValueStr==newValueStr[l]){
											condition=false;
											break;
										}
									}
								}
							}else{
								if(value.type=="DATE" || value.type=="DATE_RANGE"){
									if(typeof newValueStr!= 'undefined' && newValueStr!=''){
										var dateToSubmit1 = sbiModule_dateServices.formatDate(newValueStr, driversExecutionService.parseDateTemp(sbiModule_config.localizedDateFormat));
										condition = visualDependency.operation=='contains' && compareValueStr==dateToSubmit1;
									}
								}else{
									condition = (visualDependency.operation=='contains') ?
											(compareValueStr==newValueStr) : condition=(compareValueStr!=newValueStr);
								}
							}
							if(condition){
								execProperties.parametersData.documentParameters[idDocumentParameter].label=visualDependency.viewLabel;
								execProperties.parametersData.documentParameters[idDocumentParameter].visible=true;
								destinationOk[idDocumentParameter]=true;
								break;
							}else{
								execProperties.parametersData.documentParameters[idDocumentParameter].visible=false;
							}
						}
					}
				}

				//if return to viewpoin enable visual correlation
//				if(execProperties.returnFromVisualViewpoint.status){
//					execProperties.initResetFunctionVisualDependency.status=true;
//					execProperties.returnFromVisualViewpoint.status = false;
//				}
			};

			dependencyService.updateDependencyValues = function(newDependencyValue,execProperties){
				var executionParameters;
				var parametersPath;
					if(execProperties.currentView && execProperties.currentView.status == 'DOCUMENT'){
						var executionParameters = "1.0/documentExeParameters";
						var parametersPath = "getParameters";
					}else /*if($scope.execProperties.meta.dataset.hasOwnProperty('dataset'))*/{
						//adaptExecutionProperties();
						/*  TODO : For Behairoval Model and datasets preview
						 */
						var executionParameters = "1.0/businessModelOpening";
						var parametersPath = "getParameters";

					}

				if(dependencyService.dataDependenciesMap[newDependencyValue.urlName]){
					for(var k=0; k<dependencyService.dataDependenciesMap[newDependencyValue.urlName].length; k++){
						var dataDependenciesElementMap = dependencyService.dataDependenciesMap[newDependencyValue.urlName][k];

						if(dataDependenciesElementMap.lovParameterMode != 'TREE'){
							var objPost = createDependencyUpdatingObject(execProperties,dataDependenciesElementMap);
							sbiModule_restServices.post(executionParameters,parametersPath, objPost)
							.success(function(data, status, headers, config) {
								if(data.status == "OK"){
									prepareParameterForNewValues(execProperties,data);
									if(data.result.root && data.result.root.length>0){
										setParameterForNewValues(execProperties,data);
									}
								}
							})
							.error(function(data, status, headers, config) {});

						}else{
							for(var z=0; z<execProperties.parametersData.documentParameters.length;z++){
								if(execProperties.parametersData.documentParameters[z].urlName==dataDependenciesElementMap.parameterToChangeUrlName){
									if(execProperties.initResetFunctionDataDependency.status){
										execProperties.parametersData.documentParameters[z].children = [];
										driversExecutionService.resetParameter(execProperties.parametersData.documentParameters[z]);
									}
									break;
								}
							}
							if(execProperties.returnFromDataDepenViewpoint.status){
								execProperties.initResetFunctionDataDependency.status=true;
								execProperties.returnFromDataDepenViewpoint.status = false;
							}
						}
					}
				}
			};

			var getArrayIndexByDriverUrlName = function(urlName,parameters){
				var index= -1;
				for(var i=0; i<parameters.length; i++ ){
					if(parameters[i].urlName == urlName){
						index = i;
						break;
					}
				}
				return index;
			};

			var createDependencyUpdatingObject = function(execProperties,dependenciesMap){
				var objPost = {};
				if(execProperties.qbeDatamarts) {
					objPost.OBJECT_LABEL = execProperties.qbeDatamarts;
				} else {
					objPost.OBJECT_LABEL = execProperties.executionInstance.OBJECT_LABEL;
                }
				objPost.ROLE=execProperties.selectedRole.name;
				objPost.PARAMETER_ID=dependenciesMap.parameterToChangeUrlName;
				objPost.MODE='simple';
				objPost.PARAMETERS=driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters);
				return objPost;
			};

			var setParameterForNewValues = function(execProperties,data){

				var dataRoot = data.result.root;
				var dataValues = dataRoot.map(e => e.value);
				var parameters = execProperties.parametersData.documentParameters;
				for(var p=0; p<dataRoot.length;p++){
					var currData = dataRoot[p];

					for(var z=0; z<parameters.length;z++){
						var parameter = parameters[z];
						if(parameter.urlName==data.idParam){

							var toAdd = [];

							if(parameter.defaultValues &&
									parameter.defaultValues.length>0){
								var found = false;

								for(var y=0;y<parameter.defaultValues.length && !found;y++){
									if( parameter.defaultValues[y].value == currData.value){
										parameter.defaultValues[y].isEnabled=true;
										found = true;
										//if mandatory and if combo list set parameter default !!!
										if(dataRoot.length == 1 && parameter.mandatory
												&& (parameter.selectionType == 'COMBOBOX'
													|| parameter.selectionType == 'LIST')){

											parameter.parameterValue = parameter.multivalue ?
													[dataRoot[0].value]	: dataRoot[0].value;
										}
									}
								}
								// if not found add
								if(!found) {
									var objBase = {};
									objBase.value = currData.value;
									objBase.label = currData.label;
									objBase.description = currData.description;
									objBase.isEnabled = true;
									parameter.defaultValues.push(objBase);
								}
							}

							break;
						}
					}
				}

				for(var z=0; z<parameters.length;z++) {
					var parameter = parameters[z];
					if(parameter.urlName==data.idParam) {
						// Remove values not present in data
						parameter.defaultValues = parameter.defaultValues.filter(function(x) { return dataValues.includes(x.value) });

						// Reset selected value if not present in data
						if (!dataValues.includes(parameter.parameterValue)) {
							parameter.parameterValue = parameter.multivalue ? [] : undefined;
						}
					}
				}

				parameter.defaultValues.sort( function(a,b) {
					 return (a.description > b.description) ? 1 : ((b.description > a.description) ? -1 : 0);
					});
			};
			var prepareParameterForNewValues = function(execProperties,data){
				var parameters = execProperties.parametersData.documentParameters;
				for(var i=0; i<parameters.length;i++){
					var parameter = parameters[i];
					if(parameter.urlName==data.idParam &&
							areCorrelatedParametersEmpty(parameters, parameter)){

						driversExecutionService.emptyParameter(parameter);
						if(parameter.defaultValues &&
								parameter.defaultValues.length>0){
							for(var j=0;j<parameter.defaultValues.length;j++){
								parameter.defaultValues[j].isEnabled=false;
							}
						}
						break;
					}
				}
			};

			areCorrelatedParametersEmpty = function(allParams, currParam) {
				for (var i=0; i<currParam.dataDependencies.length; i++) {
					fatherUrl = currParam.dataDependencies[i].parFatherUrlName;
					for (var j=0; j<allParams.length; j++) {
						if (fatherUrl == allParams[j].urlName) {
							if (allParams[j].parameterValue == '') {
								return true;
							}
						}
					}
				}
				return false;
			}

			dependencyService.updateLovValues = function(value,execProperties){

				if(dependencyService.lovCorrelationMap[value.urlName]){
					for(var k=0; k<dependencyService.lovCorrelationMap[value.urlName].length; k++){
						var dataDependenciesElementMap = dependencyService.lovCorrelationMap[value.urlName][k];
						var objPost = createDependencyUpdatingObject(execProperties,dataDependenciesElementMap);

						sbiModule_restServices.promisePost("1.0/documentExeParameters",	"getParameters", objPost)
						.then(
								function(response, status, headers, config) {
									for(var z=0; z<execProperties.parametersData.documentParameters.length;z++){
										if(execProperties.parametersData.documentParameters[z].urlName==response.data.idParam){
											execProperties.parametersData.documentParameters[z].defaultValues = [];
											//BUILD DEAFULT VALUE
											var defaultValueArrCache = getDefaultValues(execProperties.parametersData.documentParameters[z],response);
											var parameterValue = execProperties.parametersData.documentParameters[z].parameterValue;
											var parameterDescription = execProperties.parametersData.documentParameters[z].parameterDescription;
											//Remove parameter value if not present in default value (clean operation)
											//MULTIVALUE
											if(angular.isArray(parameterValue)) {
												var paramValueArrCache= [];
												angular.copy(parameterValue,paramValueArrCache);
												for(var u = 0; u < paramValueArrCache.values.length; u++){
													var index = parameterValue.indexOf(paramValueArrCache[u]);
													if(defaultValueArrCache.indexOf(paramValueArrCache[u]) === -1) {
														parameterValue.splice(index, 1);
													}
												}
											}else{
												//SINGLEVALUE
												parameterValue = '';
											}

											//if mandatory and is unique default value
											// this should be done only if parameterValue is not already set!
											if(parameterValue == undefined || parameterValue.length == 0	){

												if(response.data.result.root != undefined && response.data.result.root.length==1 &&
														execProperties.parametersData.documentParameters[z].mandatory &&
														(execProperties.parametersData.documentParameters[z].selectionType=='LIST' ||
																execProperties.parametersData.documentParameters[z].selectionType=='COMBOBOX')){
												//	console.log('setting default value ', response.data.result.root[0].value);
													execProperties.parametersData.documentParameters[z].parameterValue = execProperties.parametersData.documentParameters[z].multivalue ?
															[response.data.result.root[0].value]	: response.data.result.root[0].value;
													execProperties.parametersData.documentParameters[z].parameterDescription = execProperties.parametersData.documentParameters[z].multivalue ?
																	[response.data.result.root[0].description]	: response.data.result.root[0].description;
												}else{
													//don't back from viewpoint and default value
													if(execProperties.initResetFunctionLovDependency.status){
														execProperties.parametersData.documentParameters[z].parameterValue = execProperties.parametersData.documentParameters[z].multivalue ?
																[]	: '';
													}
												}

												execProperties.parametersData.documentParameters[z].lovNotDefine=false;
											}


										}

									}

									if(execProperties.returnFromLovDepenViewpoint.status){
										execProperties.initResetFunctionLovDependency.status=true;
										execProperties.returnFromLovDepenViewpoint.status = false;
									}


								},function(response, status, headers, config) {
									var lovParamName = dataDependenciesElementMap.parameterToChangeUrlName;
									var errorMes = '';
									if(typeof response.data!='undefined' &&  typeof response.data.errors !='undefined'){
										errorMes = response.data.errors[0].message;
									}
									if(typeof response.data.RemoteException !='undefined' ){
										errorMes = response.data.RemoteException.message;
									}
									//documentExecuteServices.showToast('Error LOV " '+ lovParamName +' " : ' + errorMes);
									var idRowParameter = getArrayIndexByDriverUrlName(lovParamName, execProperties.parametersData.documentParameters);
									execProperties.parametersData.documentParameters[idRowParameter].lovNotDefine=true;
									execProperties.parametersData.documentParameters[idRowParameter].defaultValues = [];
									execProperties.parametersData.documentParameters[idRowParameter].parameterValue = [];
								}
						);
					}
				}
			}

			var getDefaultValues = function(parameter,response){
				var defaultValueArrCache = {};
				defaultValueArrCache.descriptions = [];
				defaultValueArrCache.values = [];
				if(response.data.result.root != undefined) {
					for(var k=0; k<response.data.result.root.length; k++){
						response.data.result.root[k].isEnabled = true;
						parameter.defaultValues.push(response.data.result.root[k]);
						defaultValueArrCache.values.push(response.data.result.root[k].value);
						defaultValueArrCache.descriptions.push(response.data.result.root[k].description);
					}
				}
				return defaultValueArrCache;
			}
			return dependencyService;
		}])
	})();