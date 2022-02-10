(function() {
	var documentExecutionModule = angular.module('documentExecutionModule');

	documentExecutionModule.service('multipartForm',['$http',function($http){

		this.post = function(uploadUrl,data){

			var formData = new FormData();

			for(var key in data){

				formData.append(key,data[key]);
			}

			return $http.post(uploadUrl,formData,{
				transformRequest:angular.identity,
				headers:{accept: 'application/pdf'},
				responseType: 'arraybuffer',

			})
		}

	}]);

	documentExecutionModule.service('documentExecuteServices', function($mdToast,execProperties,sbiModule_restServices,sbiModule_config,$filter,sbiModule_dateServices,sbiModule_messaging,driversExecutionService) {
		var documentExecuteServicesObj = {


//				decodeRequestStringToJson: function (str) {
//					var parametersJson = {};
//
//					var arrParam = str.split('%26');
//					for(var i=0; i<arrParam.length; i++){
//						var arrJsonElement = arrParam[i].split('%3D');
//						parametersJson[arrJsonElement[0]]=arrJsonElement[1];
//					}
//					return parametersJson;
//				},

				showToast: function(text, time) {
					var timer = time == undefined ? 6000 : time;
					//$mdToast.show($mdToast.simple().content(text).position('top').highlightAction(false).hideDelay(timer));
					sbiModule_messaging.showInfoMessage(text,"");
				},


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



	documentExecutionModule.service('docExecute_sessionParameterService', function(sbiModule_config, execProperties) {
		this.STORE_NAME = sbiModule_config.sessionParametersStoreName;
		this.PARAMETER_STATE_OBJECT_KEY = sbiModule_config.sessionParametersStateKey;


		this.store = new Persist.Store(this.STORE_NAME, {
			swf_path: sbiModule_config.contextName + '/js/lib/persist-0.1.0/persist.swf'
		});


		this.getParametersState = function(callback){
			this.store.get(this.PARAMETER_STATE_OBJECT_KEY, callback);
		}


		/**
		 * internal utility method that returns the key that will be used in order to store the parameter state.
		 * The key is composed by the following information retrieved by the parameter that stands behind the input field:
		 * - label of the parameter
		 * - id of the parameter use mode (in order to avoid that parameters with the same labels but different modalities conflict)
		 */
		this.getParameterStorageKey =  function(parDetail) {
			var parameterStorageKey = parDetail.driverLabel + '_' + parDetail.driverUseLabel;
			// if it is of tyype manual input add also url name as key
			if(parDetail.valueSelection == 'man_in'){
				parameterStorageKey += '_' + parDetail.urlName;
			}

			return parameterStorageKey;
		}


		/**
		 * clears a stored parameter
		 * The input par is parName
		 */
		this.clear =  function(parDetail) {
			try {
				var thisContext = this;
				if (sbiModule_config.isStatePersistenceEnabled == true
						//&& execProperties.executionInstance.isFromCross == false
				) {
					this.store.get(this.PARAMETER_STATE_OBJECT_KEY, function(ok, value) {
						if (ok) {
							var storedParameters = angular.fromJson(value);
							if (storedParameters !== undefined && storedParameters !== null) {
								var key = thisContext.getParameterStorageKey(parDetail);
								delete storedParameters[key];
							}
							thisContext.store.set(thisContext.PARAMETER_STATE_OBJECT_KEY, angular.toJson(storedParameters));
						}
					});
				}
			}
			catch (err) {
				console.error('Error in clearing session parameter for parameter '+parDetail.driverLabel);
			}
		}

		this.saveParameters = function(parameters, parametersDetail) {
			if (sbiModule_config.isStatePersistenceEnabled == true){
				//&& execProperties.executionInstance.isFromCross == false) {

				// create a copy
				var copyParameters = {}
				for(var parName in parameters){
					var parValue = parameters[parName];
					copyParameters[parName] = parValue;
				}


				for(let parName in parameters) {
					if(!parName.endsWith("_field_visible_description")&& parametersDetail[parName]){
						// if(!field.isTransient)
						var parValue = parameters[parName];
						if(parametersDetail[parName].type == 'DATE'){
							var parDateFormat = sbiModule_config.serverDateFormat;
							if(parValue != undefined && parValue != ''){
								parValue+=('#'+parDateFormat);
							}
						}
						else if(parametersDetail[parName].type == 'DATE_RANGE'){
							var parDateFormat = sbiModule_config.serverDateFormat;
							if(parValue != undefined  && parValue != ''){
								parValue+=('#'+parDateFormat);
							}
						}
						var parValueDescription = copyParameters[parName+"_field_visible_description"];
						this.saveParameterState(parName, parValue, parValueDescription, parametersDetail[parName]);


						//}
					}
				}
			}
		};

		this.saveParameterState = function(parName, parValue, parValueDescription, parDetail) {
			try {
				var thisContext = this;
				if (sbiModule_config.isStatePersistenceEnabled == true)
					//&& execProperties.executionInstance.isFromCross == false)
				{
					this.store.get(thisContext.PARAMETER_STATE_OBJECT_KEY, function(ok, value) {
						if (ok) {
							var storedParameters = null;
							if (value === undefined || value === null) {
								storedParameters = {};
							} else {
								storedParameters = angular.fromJson(value);

							}

							if (parValue === undefined || parValue === null || parValue === '' || parValue.length === 0) {

								thisContext.clear(parDetail);

							} else {
								var parameterStateObject = {};
								parameterStateObject.value = parValue;
								parameterStateObject.description = parValueDescription;
								parameterStateObject.name = parName;

								var keyy = thisContext.getParameterStorageKey(parDetail);
								storedParameters[keyy] = parameterStateObject;
								var json = angular.toJson(storedParameters);
								thisContext.store.set(thisContext.PARAMETER_STATE_OBJECT_KEY, json);
							}
						}

					});
				}
			}  catch (err) {
				console.error('Error in saving parameter in session for parameter '+parName);
			}
		};

	});










	documentExecutionModule.service('docExecute_urlViewPointService', function(execProperties,
			sbiModule_restServices, $mdDialog, sbiModule_translate,sbiModule_config,docExecute_exportService
			,$mdSidenav,docExecute_paramRolePanelService,documentExecuteServices,documentExecuteFactories,$q,$filter,$timeout
			,sbiModule_messaging, $http,sbiModule_dateServices,$mdToast,docExecute_sessionParameterService,sbiModule_i18n,
			driversExecutionService,driversDependencyService, cockpitEditing ) {

		var serviceScope = this;
		serviceScope.showOlapMenu = false;
//		serviceScope.documentUrl = '';
		serviceScope.frameLoaded = true;
		serviceScope.exportation=[];

		serviceScope.i18n = sbiModule_i18n;

		serviceScope.executionProcesRestV1 = function(role, params) {
			console.log('params', params)
			params = typeof params === 'undefined' ? {} : params;

			var dataPost = {
					label: execProperties.executionInstance.OBJECT_LABEL,
					role:role,
					SBI_EXECUTION_ID:execProperties.executionInstance.SBI_EXECUTION_ID,
					parameters: params,
					EDIT_MODE:execProperties.executionInstance.EDIT_MODE
			};

			if (!(execProperties.executionInstance.IS_FOR_EXPORT)) {
				dataPost.IS_FOR_EXPORT = true;

				if(execProperties.executionInstance.COCKPIT_SELECTIONS
						&& execProperties.executionInstance.COCKPIT_SELECTIONS.trim() != '') {
					dataPost.COCKPIT_SELECTIONS = execProperties.executionInstance.COCKPIT_SELECTIONS;
				}
			}

			// map par urlName with analytical driver details
			var parametersDetail = {};
			for(var parIndex in execProperties.parametersData.documentParameters){
				var parContent = execProperties.parametersData.documentParameters[parIndex];
				parametersDetail[parContent.urlName] = parContent;
			}

			// memorize parameters in session
			docExecute_sessionParameterService.saveParameters(dataPost.parameters, parametersDetail);
			console.log('params', parametersDetail);
			sbiModule_restServices.alterContextPath( sbiModule_config.contextName);
			console.log('dataPost', dataPost);
			var postObject = {
				params: {}
			};
			sbiModule_restServices.promisePost("1.0/documentexecution", 'url', dataPost)
			.then(
					function(response, status, headers, config) {
						var data=response.data;
						var documentUrl = data.url+'&timereloadurl=' + new Date().getTime();

						postObject.url = documentUrl.split('?')[0];
						var paramsFromUrl = documentUrl.split('?')[1].split('&');
						for(var i in paramsFromUrl){
							if(typeof paramsFromUrl[i] != 'function') {
								postObject.params[paramsFromUrl[i].split('=')[0]] = paramsFromUrl[i].split('=')[1];
							}
						}
						if(cockpitEditing.documentMode) postObject.params.documentMode = cockpitEditing.documentMode;
						var postForm = document.getElementById("postForm_"+postObject.params.document);
						if(!postForm){
							postForm = document.createElement("form");
							postForm.id="postForm_"+postObject.params.document;
							postForm.action = postObject.url;
							postForm.method = "post";
						    postForm.target = "documentFrame";
						    document.body.appendChild(postForm);
						}
						for (var k in postObject.params) {
							inputElement = document.getElementById("postForm_"+k);
							if(inputElement) {
								inputElement.value = decodeURIComponent(postObject.params[k]);
								inputElement.value = inputElement.value.replace(/\+/g,' ');
							}else{
								var element = document.createElement("input");
						        element.type = "hidden";
						        element.id= 'postForm_' + k;
						        element.name = k;
						        element.value = decodeURIComponent(postObject.params[k]);
						        element.value = element.value.replace(/\+/g,' ');
						        postForm.appendChild(element);
							}
						}

						// removing unused form elements; we loop backwards, so deletion of items does not affect the loop iterations
						for (var i = postForm.elements.length - 1; i >= 0; i--) {
							var postFormElement = postForm.elements[i].id.replace("postForm_", "");
							if(!postObject.params.hasOwnProperty(postFormElement)) {
								postForm.removeChild(postForm.elements[i]);
							}
						}

//						}else{
//							for (var k in postObject.params) {
//								inputElement = document.getElementById("postForm_"+k);
//								if(inputElement) {
//									inputElement.value = decodeURIComponent(postObject.params[k]);
//									inputElement.value = inputElement.value.replace(/\+/g,' ');
//								}else {
//									var element = document.createElement("input");
//							        element.type = "hidden";
//							        element.id= 'postForm_' + k;
//							        element.name = k;
//							        element.value = decodeURIComponent(postObject.params[k]);
//							        element.value = element.value.replace(/\+/g,' ');
//							        postForm.appendChild(element);
//								}
//							}
//						}
						postForm.submit();

						console.log("1.0/documentexecution/url -> " + documentUrl);

//						serviceScope.documentUrl = data.url+'&timereloadurl=' + new Date().getTime();
						execProperties.documentUrl = documentUrl;
						//SETTING EXPORT BUTTON
//						serviceScope.exportation = docExecute_exportService.exportationHandlers[data['typeCode']];
						docExecute_exportService.getExporters(data['engineLabel'], data['typeCode'])
						.then(function(exportersJSON){
							serviceScope.exportation = exportersJSON;
						},
						function(e){

						});
						execProperties.executionInstance.ENGINE_LABEL=data['engineLabel'];
						serviceScope.showOlapMenu = serviceScope.getOlapType();
						//SETTING URL SBI EXECUTION ID
						if(data['sbiExecutionId'] && data['sbiExecutionId'].length>0){
							execProperties.executionInstance.SBI_EXECUTION_ID=data['sbiExecutionId'];
						}
						//execProperties.currentView.status = 'DOCUMENT';
					},
					function(response, status, headers, config) {
						console.log('1.0/documentexecution ERROR: ', response.data);
						var toast = $mdToast.simple()
						.content("Error while execute parameter. "+response.data.errors[0].message)
						.action('OK')
						.highlightAction(false)
						.hideDelay(3000)
						.position('top')
						//cehck code error
						if(response.data.errors[0].type!="missingRole" && response.data.errors[0].errorCode!="9001"){
							$mdToast.show(toast).then(function(response) {
								if ( response == 'ok' ) {
								}
							});
						}

//						sbiModule_restServices.errorHandler(response.data,"Error while attempt to load filters")
//						.then(function(){
//						if(response.data.errors[0].type=="missingRole" || response.data.errors[0].category=="VALIDATION_ERROR"){
//						docExecute_paramRolePanelService.toggleParametersPanel(true);
//						}
//						});
						if(response.data.errors[0].type=="missingRole" || response.data.errors[0].errorCode=="9001"){
							docExecute_paramRolePanelService.toggleParametersPanel(true);
						}else{
							sbiModule_restServices.errorHandler(response.data,response.data.errors[0].message);
						}

						serviceScope.frameLoaded = true;
					});
		};



		serviceScope.getViewpoints = function() {
			execProperties.currentView.status = 'PARAMETERS';
			execProperties.parameterView.status='FILTER_SAVED';
			execProperties.isParameterRolePanelDisabled.status = true;

			sbiModule_restServices.get("1.0/documentviewpoint", "getViewpoints",
					"label=" + execProperties.executionInstance.OBJECT_LABEL + "&role="+ execProperties.selectedRole.name)
					.success(function(data, status, headers, config) {
						console.log('data viewpoints '  ,  data.viewpoints);
//						serviceScope.gvpCtrlViewpoints = data.viewpoints;
						driversExecutionService.gvpCtrlViewpoints = data.viewpoints;
						// angular.copy(data.viewpoints,driversExecutionService.gvpCtrlViewpoints);

//						execProperties.showParametersPanel.status = false;
						if($mdSidenav('parametersPanelSideNav').isOpen()) {
							docExecute_paramRolePanelService.toggleParametersPanel(false);
						}
					})
					.error(function(data, status, headers, config) {});
		};
		serviceScope.addToWorkspace = function() {

			sbiModule_restServices.promisePost('2.0/organizer/documents',execProperties.executionInstance.OBJECT_ID)
			.then(function(response) {
				console.log("[POST]: SUCCESS!");
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.browser.document.addedToWorkspace"), sbiModule_translate.load('sbi.generic.success'));
			}, function(response) {
				if(response.data.errors[0].message=="not-enabled-to-call-service"){
					response.data.errors[0].message=sbiModule_translate.load('sbi.workspace.user.role.constraint');
				}

				// @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), sbiModule_translate.load('sbi.generic.error'));
			});
		};


		serviceScope.getSchedulers = function() {
			execProperties.currentView.status = 'PARAMETERS';
			execProperties.parameterView.status='SCHEDULER';
			sbiModule_restServices.get( "1.0/documentsnapshot", "getSnapshots",
					"id=" + execProperties.executionInstance.OBJECT_ID)
					.success(function(data, status, headers, config) {
						console.log('data scheduler '  ,  data.schedulers);
						serviceScope.gvpCtrlSchedulers = data.schedulers;
						console.log('url path ' + data.urlPath);
						serviceScope.snapshotUrlPath=data.urlPath;

						if($mdSidenav('parametersPanelSideNav').isOpen()) {
							docExecute_paramRolePanelService.toggleParametersPanel(false);
						}
					})
					.error(function(data, status, headers, config) {});
		};

		serviceScope.getOlapDocs = function() {
			execProperties.currentView.status = 'OLAP';
			execProperties.parameterView.status = 'OLAP';

			serviceScope.olapList = [];

			sbiModule_restServices.get("1.0/olapsubobjects", 'getSubObjects',
					"idObj=" + execProperties.executionInstance.OBJECT_ID)
					.then(function(response){
						angular.copy(response.data.results,serviceScope.olapList);
					},function(response){
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.alert.load.error"));
					});

		};

		serviceScope.getOlapType = function(){

//			if (execProperties.executionInstance.ENGINE_LABEL == "knowagewhatifengine" || execProperties.executionInstance.ENGINE_LABEL == "knowageolapengine")
			if (execProperties.executionInstance.ENGINE_LABEL == sbiModule_config.whatIfEngineContextName
					|| execProperties.executionInstance.ENGINE_LABEL == sbiModule_config.olapEngineContextName)
				return true;
			else
				return false;
		}


		/*
		 * Fill Parameters Panel
		 */
		serviceScope.fillParametersPanel = function(params){

			//console.log('Load filter params : ' , params);
			if(execProperties.parametersData.documentParameters.length > 0){

				//var readyParams //-> su questi parametri è stato settato il valore (o non ho nessun valore da settarvi)
				//var dependingOnParameters //-> lista dei parametri dai quali dipendono altri parametri
				//var savedParamtersToSet //-> lista che scorro finchè non vuota, ogni volta che riesco a settarne uno, lo levo dalla lista
				//riesco a settare una valore quando tutti i paramteri (se ce ne sono) da cui dipende sono presenti in readyParams

				for(var i = 0; i < execProperties.parametersData.documentParameters.length; i++){
					var parameter = execProperties.parametersData.documentParameters[i];

					// in case the parameter value is missing or it is "[]", we reset the parameter.
					// TODO improve this: the "[]" is a string while it should be an actual empty array!!! fix this in combination with decodeRequestStringToJson
					// choosing a more convenient encoding/decoding
					if(!params[parameter.urlName] || params[parameter.urlName] == "[]") {
						driversExecutionService.resetParameter(parameter);
					} else {
						//console.log('parametro ' , parameter);
//						if(parameter.valueSelection=='lov'
////						&& parameter.multivalue==true
//						&& parameter.selectionType.toLowerCase() == "tree"
//						) {
						if(parameter.valueSelection=='lov') {
							if(parameter.selectionType.toLowerCase() == "tree" || parameter.selectionType.toLowerCase() == "lookup") {
								//TREE DESC FOR LABEL
								var ArrValue = JSON.parse(params[parameter.urlName]);
								if (typeof parameter.parameterDescription === 'undefined'){
									parameter.parameterDescription = {};
								}

								if(params[parameter.urlName+'_field_visible_description']!=undefined)
								{
									parameter.parameterDescription = {};
									var ArrDesc = params[parameter.urlName+'_field_visible_description'].split(';');
									for(var w=0; w<ArrValue.length; w++){
										parameter.parameterDescription[ArrValue[w]] =ArrDesc[w];
									}
									parameter.parameterValue = ArrValue;
								}
							} else {
								//FROM VIEWPOINT : the lov value saved (multivalue or single value) matched  with the parameter
								parameter.parameterValue = parameter.multivalue ? JSON.parse(params[parameter.urlName])	: params[parameter.urlName];
							}

						} else if(parameter.valueSelection.toLowerCase() == 'map_in') {
							var valueToBeCleanedByQuotes = params[parameter.urlName].replace(/^'(.*)'$/g, '$1');
							var valueToBeSplitted = valueToBeCleanedByQuotes.split("','");

							parameter.parameterValue = (parameter.multivalue)? valueToBeSplitted : valueToBeCleanedByQuotes;
						} else {
							if(parameter.type=='NUM'){
								if (parameter.multivalue){
									var values = params[parameter.urlName].split(",");
									parameter.parameterValue = "";
									for (var v=0; v<values.length; v++){
										parameter.parameterValue += parseFloat(values[v],10);
										if (v < (values.length-1)) parameter.parameterValue += ",";
									}
								}else
									parameter.parameterValue = parseFloat(params[parameter.urlName],10);
							}else if(parameter.type=='DATE'){
								//set parameter date server
								if (parameter.multivalue){
									var values = params[parameter.urlName].split(",");
									parameter.parameterValue = "";
									for (var v=0; v<values.length; v++){
										var res = sbiModule_dateServices.getDateFromFormat(values[v], sbiModule_config.serverDateFormat);
										parameter.parameterValue += sbiModule_dateServices.formatDate(res, sbiModule_config.serverDateFormat); //convert to string
										if (v < (values.length-1)) parameter.parameterValue += ",";
									}
								}else
									parameter.parameterValue = sbiModule_dateServices.getDateFromFormat(params[parameter.urlName], sbiModule_config.serverDateFormat);
							}else if(parameter.type=='DATE_RANGE'){
								var dateRange = params[parameter.urlName];
								var dateRangeArr = dateRange.split('_');
								var range = dateRangeArr[1];
								dateRange = dateRangeArr[0];
								if (dateRange === parseInt(dateRange)){
									//FROM DEFAULT
									parameter.parameterValue= new Date(parseInt(dateRange));
								}else{
									//FROM VIEWPOINT
									parameter.parameterValue= sbiModule_dateServices.getDateFromFormat(dateRange, sbiModule_config.serverDateFormat);
								}
								if(typeof parameter.datarange ==='undefined'){
									parameter.datarange = {};
								}
								parameter.datarange.opt=serviceScope.convertDateRange(range);
							}
							else if(parameter.type=='STRING'){
								parameter.parameterValue = params[parameter.urlName];
								if(parameter.defaultValues && parameter.defaultValues.length > 0) {
									var parameterValues = parameter.parameterValue;
									var parArr = parameterValues.split(';');
									for(var j = 0; j < parameter.defaultValues.length; j++) {
										var defaultValue = parameter.defaultValues[j];
										for(var k = 0; k < parArr.length; k++) {
											if(defaultValue.value == parArr[k]) {
												defaultValue.isSelected = true;
												break;
											} else {
												defaultValue.isSelected = false;
											}
										}
									}
								}
							}
						}
					}
					driversDependencyService.updateVisualDependency(parameter,execProperties);
				}


			}
		};



		/*
		 * Convert the range date value format type_quantity FROM 5D To dayes_5;
		 */
		serviceScope.convertDateRange = function(range) {
			var value = "";
			if (range != null && range.length > 1) {
				var type = range.substring(range.length - 1, range.length);
				var quantity = range.substring(0, range.length - 1);
				if (type=="D") {
					type = "days";
				}
				if (type=="Y") {
					type = "years";
				}
				if (type=="W") {
					type = "weeks";
				}
				if (type=="M") {
					type = "months";
				}

				value = type + "_" + quantity;

			}
			return value;
		}


		serviceScope.prepareDrivers = function(data, buildCorrelation) {
			//correlation
			buildCorrelation(execProperties.parametersData.documentParameters, execProperties);

			//setting default value
			serviceScope.buildObjForFillParameterPanel(data.filterStatus);
			// Enable visualcorrelation
			execProperties.initResetFunctionVisualDependency.status=true;
			execProperties.initResetFunctionDataDependency.status=true;
			execProperties.initResetFunctionLovDependency.status=true;

			execProperties.isParameterRolePanelDisabled.status = docExecute_paramRolePanelService.checkParameterRolePanelDisabled();

			if (data.isReadyForExecution === true) {
				serviceScope.executionProcesRestV1(execProperties.selectedRole.name,
						driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters));
			} else {
				serviceScope.frameLoaded = true; // this hides loading mask
				docExecute_paramRolePanelService.toggleParametersPanel(true);
			}

			// keep track of start value for reset!
			if(execProperties.parametersData.documentParameters != undefined){
				for(var i=0; i<execProperties.parametersData.documentParameters.length; i++){
					var param = execProperties.parametersData.documentParameters[i];

					// take driverDefaultValue for reset, no more present value
					if(param.driverDefaultValue){
						if(param.multivalue == false){
							if(param.driverDefaultValue.length >= 1){ // single value
								var valDef = param.driverDefaultValue[0];
								var valueD = valDef.value;
								var descriptionD = valDef.description;
								if(param.selectionType == 'LOOKUP' || param.selectionType == 'TREE'){
									param.defaultValue = [];
									param.defaultValue.push(valueD);
									param.defaultValueDescription = [];
									param.defaultValueDescription.push(descriptionD);
								}
								else{
									param.defaultValue = valueD;
									param.defaultValueDescription = descriptionD;
								}


							}
						}
						else{
							param.defaultValue = [];
							param.defaultValueDescription = [];
							for(var j = 0; j<param.driverDefaultValue.length;j++){
								var valDef = param.driverDefaultValue[j];
								var valueD = valDef.value;
								var descriptionD = valDef.description;
								param.defaultValue.push(valueD);
								param.defaultValueDescription.push(descriptionD);
							}
						}

					}
				}

//				if(param.parameterValue){
//				{
//				param.defaultValue = angular.copy(param.parameterValue);

//				if(param.parameterDescription == undefined){
//				param.defaultValueDescription = angular.copy(param.parameterValue);
//				}
//				else{
//				param.defaultValueDescription = angular.copy(param.parameterDescription);

//				}
//				}
//				}

			}
		}

		serviceScope.formatAdmissibleValue = function(execProperties) {
			execProperties.hasOneAdmissibleValue = true;
			var drivers = execProperties.parametersData.documentParameters;
			for(var i = 0; i < drivers.length; i++) {
				if(drivers[i].parameterValue && drivers[i].parameterValue.length > 0) {
					for(var j = 0; j < drivers[i].parameterValue.length; j++) {
						if(drivers[i].parameterValue[j].value) {
							var tempValue = drivers[i].parameterValue[j].value;
							drivers[i].parameterValue = [];
							drivers[i].parameterValue.push(tempValue);
						}
					}
					if(drivers[i].selectionType == "TREE" || drivers[i].selectionType == "LOOKUP" || drivers[i].driverDefaultValue || drivers[i].isReadFromCache) {
						execProperties.hasOneAdmissibleValue = false;
					}

				} else {
					execProperties.hasOneAdmissibleValue = false;
				}
			}
		}



		serviceScope.getParametersForExecution = function(role, buildCorrelation,crossParameters) {


			docExecute_sessionParameterService.getParametersState(
					function(ok, val, scope){
						if(ok === true){


							var params = {
									label:execProperties.executionInstance.OBJECT_LABEL,
									role:role,
									parameters:crossParameters
//									sessionParameters:val
							};

							//add parameters session if they are managed
							if (sbiModule_config.isStatePersistenceEnabled == true){
								if (val == null || val == "null")
									val = "{}"; 	//clean from wrong values
								params.sessionParameters = val;
							}


							sbiModule_restServices.promisePost("1.0/documentexecution", "filters", params)
							.then(function(response, status, headers, config) {
								console.log('getParametersForExecution response OK -> ', response);
								//check if document has parameters
								if(response && response.data.filterStatus && response.data.filterStatus.length>0) {

									//build documentParameters
									angular.copy(response.data.filterStatus, execProperties.parametersData.documentParameters);

									sbiModule_i18n.loadI18nMap().then(function() {
										serviceScope.formatAdmissibleValue(execProperties);
										serviceScope.prepareDrivers(response.data, buildCorrelation);
									}); // end of load I 18n

								} else {
//									execProperties.showParametersPanel.status = false;
//									$mdSidenav('parametersPanelSideNav').close();
									docExecute_paramRolePanelService.toggleParametersPanel(false);
									serviceScope.executionProcesRestV1(execProperties.selectedRole.name,
											driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters));

								}

							},function(response, status, headers, config) {
								sbiModule_restServices.errorHandler(response.data,"error while attempt to load filters")
							});
						}
					}
			);
		};



		serviceScope.buildObjForFillParameterPanel = function(filterStatus){
			var fillObj = {};
			var hasDefVal = false;
			for(var i=0; i<filterStatus.length; i++){
				if(filterStatus[i].parameterValue && filterStatus[i].parameterValue.length>0) {
					var arrDefToFill = [];
					var arrDefToFillDescription = []; //TREE
					//var fillObj = {};
					//MULTIVALUE
					hasDefVal= true;
					if(filterStatus[i].multivalue && filterStatus[i].valueSelection!='man_in' || filterStatus[i].selectionType=='TREE' || filterStatus[i].selectionType=='LOOKUP'){
						//if(filterStatus[i].defaultValues && filterStatus[i].defaultValues.length>0){
						//arrDefToFill=filterStatus[i].defaultValues;
						//}
						for(var k=0;k<filterStatus[i].parameterValue.length;k++){
							arrDefToFill.push(filterStatus[i].parameterValue[k].value);
							arrDefToFillDescription.push(filterStatus[i].parameterValue[k].description);
						}
						fillObj[filterStatus[i].urlName] = JSON.stringify(arrDefToFill);
						//TREE - LOOKUP
						if(filterStatus[i].selectionType=='TREE' || filterStatus[i].selectionType=='LOOKUP'){
							var strDefToFillDescription ='';
							for(var z=0; z<arrDefToFillDescription.length; z++){
								strDefToFillDescription=strDefToFillDescription+arrDefToFillDescription[z];
								if(z<arrDefToFill.length-1){
									strDefToFillDescription=strDefToFillDescription+';';
								}
							}
							fillObj[filterStatus[i].urlName+'_field_visible_description'] = strDefToFillDescription;
						}else{
							fillObj[filterStatus[i].urlName+'_field_visible_description'] = JSON.stringify(arrDefToFill);
						}

					}else{
						//SINGLE VALUE
						fillObj[filterStatus[i].urlName] = filterStatus[i].parameterValue[0].value;
						fillObj[filterStatus[i].urlName+'_field_visible_description'] =filterStatus[i].parameterValue[0].value;
					}
					//serviceScope.fillParametersPanel(fillObj);
				}

				if (filterStatus[i].driverMaxValue) {
					fillObj[filterStatus[i].urlName+'_max_value'] = filterStatus[i].driverMaxValue
				}
			}

			if(hasDefVal){
				serviceScope.fillParametersPanel(fillObj);
			}

			serviceScope.setMaxValueForParameters(fillObj);

		};

		serviceScope.createNewViewpoint = function() {
			$mdDialog.show({
				//scope : serviceScope,
				preserveScope : true,
				templateUrl : sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/glossary/commons/templates/dialog-new-parameters-document-execution.html',
				controllerAs : 'vpCtrl',
				controller : function($mdDialog, kn_regex) {
					var vpctl = this;
					vpctl.regex = kn_regex;
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
						//vpctl.newViewpoint.VIEWPOINT = documentExecuteServices.buildStringParametersForSave(execProperties.parametersData.documentParameters);
						vpctl.newViewpoint.VIEWPOINT = driversExecutionService.buildStringParameters(execProperties.parametersData.documentParameters);
						sbiModule_restServices.post(
								"1.0/documentviewpoint",
								"addViewpoint", vpctl.newViewpoint)
								.success(function(data, status, headers, config) {
									if(data.errors && data.errors.length > 0 ) {
										//documentExecuteServices.showToast(data.errors[0].message);
										sbiModule_restServices.errorHandler(data.errors[0].message,"sbi.generic.toastr.title.error");
									}else{
										$mdDialog.hide();
										documentExecuteServices.showToast(sbiModule_translate.load("sbi.execution.viewpoints.msg.saved"), 3000);
									}
								})
								.error(function(data, status, headers, config) {
									//documentExecuteServices.showToast(sbiModule_translate.load("sbi.execution.viewpoints.msg.error.save"),3000);
									sbiModule_restServices.errorHandler("Errors : " + status,"sbi.execution.viewpoints.msg.error.save");
								});
					};

					vpctl.annulla = function($event) {
						$mdDialog.hide();
						serviceScope.newViewpoint = JSON.parse(JSON.stringify(documentExecuteFactories.EmptyViewpoint));
					};
				},

				templateUrl : sbiModule_config.dynamicResourcesBasePath
				+ '/angular_1.4/tools/documentexecution/templates/dialog-new-parameters-document-execution.html'
			});
		};

		/*
		 * Set max value for parameters.
		 */
		serviceScope.setMaxValueForParameters = function(params) {
			if(execProperties.parametersData.documentParameters.length > 0){

				for(var i = 0; i < execProperties.parametersData.documentParameters.length; i++){
					var parameter = execProperties.parametersData.documentParameters[i];

					if(parameter.valueSelection.toLowerCase() == 'man_in') {
						var maxValue = params[parameter.urlName+'_max_value'];
						if(maxValue && parameter.type=='DATE'){
							parameter.maxValue = sbiModule_dateServices.getDateFromFormat(
									maxValue,
									sbiModule_config.serverDateFormat);
						}
					}
				}
			}
		};

		this.buildParameterForFirstExecution=function(navParam,menuParam){
			return angular.extend({},navParam,menuParam);
		};
	});

	documentExecutionModule.service('docExecute_paramRolePanelService', function(execProperties,$mdSidenav,$timeout) {

		this.checkParameterRolePanelDisabled = function() {
			return ((!execProperties.parametersData.documentParameters || execProperties.parametersData.documentParameters.length == 0 || execProperties.hasOneAdmissibleValue)
					&& (execProperties.roles.length==1));
		};

		this.returnToDocument = function() {
			execProperties.currentView.status = 'DOCUMENT';
			execProperties.parameterView.status='';
			execProperties.isParameterRolePanelDisabled.status = this.checkParameterRolePanelDisabled();
			execProperties.returnFromVisualViewpoint.status = true;
		};

		this.isExecuteParameterDisabled = function() {
			var docParams = execProperties.parametersData.documentParameters;
			for(var i = 0; i < docParams.length; i++) {
				var currParam = docParams[i];
				var currParamValue = currParam.parameterValue;
				if(currParam.mandatory
						&& (
								typeof currParamValue === 'undefined'
								|| (typeof currParamValue === 'string' && currParamValue.length == 0)
								|| (typeof currParamValue === 'number' && isNaN(currParamValue) && currParamValue !== 0)
								|| (Array.isArray(currParamValue) && currParamValue.length == 0)
								|| currParamValue == null
							)
						) {
					return true;
				}
			}
			return false;
		};

		this.toggleParametersPanel = function(open) {

			function toggleNewPanel(opened){
				 if(document.getElementById("parametersPanelSideNav-e")){
					 if(opened==undefined) $mdSidenav('parametersPanelSideNav-e').toggle();
					 if(opened) $mdSidenav('parametersPanelSideNav-e').open();
					 if(opened == false) $mdSidenav('parametersPanelSideNav-e').close();
				 }
			 }

			$timeout(function(){
				if(open==undefined){
					execProperties.showParametersPanel.status=!execProperties.showParametersPanel.status;
					toggleNewPanel();
				}else if(open){
					execProperties.showParametersPanel.status=true;
					toggleNewPanel(true);
				}else if(!open){
					execProperties.showParametersPanel.status=false;
					toggleNewPanel(false);
				}
			}, document.getElementById("parametersPanelSideNav-e") ? 0 : 500);
		};
	});


})();