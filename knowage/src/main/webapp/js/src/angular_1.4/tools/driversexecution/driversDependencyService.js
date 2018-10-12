(function() {
	var driversExecutionModule = angular.module('driversExecutionModule');
		driversExecutionModule.service('driversDependencyService', [function(){
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
							var keyMap = dependency.objParFatherUrlName;
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

			dependencyService.buildLovCorrelationMap = function(parameters){
				for(var i=0; i<parameters.length ; i++){
					if(parameters[i].lovDependencies && parameters[i].lovDependencies.length>0){
						for(var k=0; k<parameters[i].lovDependencies.length; k++){
							var dependency = {};
							dependency.objParFatherUrlName = parameters[i].lovDependencies[k];
							dependency.parameterToChangeUrlName = parameters[i].urlName;
							var keyMap = dependency.objParFatherUrlName; //
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
							var keyMap = dependency.objParFatherUrlName;
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
								}
								else { //not contains
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
									condition = visualDependency.operation=='contains' && compareValueStr==newValueStr;
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
				if(execProperties.returnFromVisualViewpoint.status){
					execProperties.initResetFunctionVisualDependency.status=true;
					execProperties.returnFromVisualViewpoint.status = false;
				}
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
						$scope.executionParameters = "1.0/businessModelOpening";
						$scope.parametersPath = "getParameters";

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
				objPost.OBJECT_LABEL = execProperties.executionInstance.OBJECT_LABEL;
				objPost.ROLE=execProperties.selectedRole.name;
				objPost.PARAMETER_ID=dependenciesMap.parameterToChangeUrlName;
				objPost.MODE='simple';
				objPost.PARAMETERS=driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters);
				return objPost;
			};

			var setParameterForNewValues = function(execProperties,data){

				for(var p=0; p<data.result.root.length;p++){
					for(var z=0; z<execProperties.parametersData.documentParameters.length;z++){
						if(execProperties.parametersData.documentParameters[z].urlName==data.idParam){

							var toAdd = [];

							if(execProperties.parametersData.documentParameters[z].defaultValues &&
									execProperties.parametersData.documentParameters[z].defaultValues.length>0){
								var found = false;

								for(var y=0;y<execProperties.parametersData.documentParameters[z].defaultValues.length && !found;y++){
									if( execProperties.parametersData.documentParameters[z].defaultValues[y].value==data.result.root[p].value){
										execProperties.parametersData.documentParameters[z].defaultValues[y].isEnabled=true;
										found = true;
										//if mandatory and if combo list set parameter default !!!
										if(data.result.root.length == 1 && execProperties.parametersData.documentParameters[z].mandatory
												&& (execProperties.parametersData.documentParameters[z].selectionType == 'COMBOBOX'
													|| execProperties.parametersData.documentParameters[z].selectionType == 'LIST')){

											execProperties.parametersData.documentParameters[z].parameterValue = execProperties.parametersData.documentParameters[z].multivalue ?
													[data.result.root[0].value]	: data.result.root[0].value;
										}
									}
								}
								// if not found add
								if(!found) {
									var objBase = {};
									objBase.value = data.result.root[p].value;
									objBase.label = data.result.root[p].label;
									objBase.description = data.result.root[p].description;
									objBase.isEnabled = true;
									execProperties.parametersData.documentParameters[z].defaultValues.push(objBase);
								}
							}

							break;
						}
					}
				}
			};
			var prepareParameterForNewValues = function(execProperties,data){
				for(var i=0; z<execProperties.parametersData.documentParameters.length;z++){
					if(execProperties.parametersData.documentParameters[i].urlName==data.idParam){

						driversExecutionService.emptyParameter(execProperties.parametersData.documentParameters[i]);

						if(execProperties.parametersData.documentParameters[i].defaultValues &&
								execProperties.parametersData.documentParameters[i].defaultValues.length>0){
							for(var j=0;y<execProperties.parametersData.documentParameters[i].defaultValues.length;j++){
								execProperties.parametersData.documentParameters[i].defaultValues[j].isEnabled=false;
							}
						}
						break;
					}
				}
			};

			dependencyService.updateLovValues = function(value,execProperties){

				if(dependencyService.driversDependencyService[value.urlName]){
					for(var k=0; k<dependencyService.driversDependencyService[value.urlName].length; k++){
						var dataDependenciesElementMap = dependencyService.driversDependencyService[value.urlName][k];
						var objPost = createDependencyUpdatingObject(execProperties,dataDependenciesElementMap);

						sbiModule_restServices.promisePost("1.0/documentExeParameters",	"getParameters", objPost)
						.then(
								function(response, status, headers, config) {
									for(var z=0; z<execProperties.parametersData.documentParameters.length;z++){
										if(execProperties.parametersData.documentParameters[z].urlName==response.data.idParam){
											execProperties.parametersData.documentParameters[z].defaultValues = [];
											//BUILD DEAFULT VALUE
											var defaultValueArrCache = setDefaultValues(execProperties,response);
											var parameterValue = execProperties.parametersData.documentParameters[z].parameterValue;
											//Remove parameter value if not present in default value (clean operation)
											//MULTIVALUE
											if(angular.isArray(parameterValues)) {
												var paramValueArrCache= [];
												angular.copy(parameterValue,paramValueArrCache);
												for(var u = 0; u < paramValueArrCache.length; u++){
													var index = parameterValue.indexOf(paramValueArrCache[u]);
													if(defaultValueArrCache.indexOf(paramValueArrCache[u]) === -1) {
														parameterValue.splice(index, 1);
													}
												}
											}else{
												//SINGLEVALUE
												if(defaultValueArrCache.indexOf(parameterValue) === -1) {
													parameterValue = '';
												}
											}

											//if mandatory and is unique default value
											// this should be done only if parameterValue is not already set!
											if(parameterValue == undefined || parameterValue.length == 0	){

												if(response.data.result.root.length==1 &&
														execProperties.parametersData.documentParameters[z].mandatory &&
														(execProperties.parametersData.documentParameters[z].selectionType=='LIST' ||
																execProperties.parametersData.documentParameters[z].selectionType=='COMBOBOX')){
													console.log('setting default value ', response.data.result.root[0].value);
													execProperties.parametersData.documentParameters[z].parameterValue = execProperties.parametersData.documentParameters[z].multivalue ?
															[response.data.result.root[0].value]	: response.data.result.root[0].value;
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
									var idRowParameter = driversDependencyService.getRowIdfromUrlName(lovParamName);
									execProperties.parametersData.documentParameters[idRowParameter].lovNotDefine=true;
									execProperties.parametersData.documentParameters[idRowParameter].defaultValues = [];
									execProperties.parametersData.documentParameters[idRowParameter].parameterValue = [];
								}
						);
					}
				}
			}

			var setDefaultValues = function(execProperties,response){
				var defaultValueArrCache = [];
				for(var k=0; k<response.data.result.root.length; k++){
					response.data.result.root[k].isEnabled = true;
					execProperties.parametersData.documentParameters[z].defaultValues.push(response.data.result.root[k]);
					defaultValueArrCache.push(response.data.result.root[k].value);
				}
				return defaultValueArrCache;
			}
			return dependencyService;
		}])
	})();