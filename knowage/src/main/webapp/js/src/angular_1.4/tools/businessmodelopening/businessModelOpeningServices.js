(function() {
	var businessModelOpeningModule = angular.module('businessModelOpeningModule');

	businessModelOpeningModule.service('bmOpen_sessionParameterService', function(sbiModule_config) {
		this.STORE_NAME = sbiModule_config.sessionParametersStoreName;
		this.PARAMETER_STATE_OBJECT_KEY = sbiModule_config.sessionParametersStateKey;

		this.store = new Persist.Store(this.STORE_NAME, {
			swf_path: sbiModule_config.contextName + '/js/lib/persist-0.1.0/persist.swf'
		});

		this.getParametersState = function(callback){
			this.store.get(this.PARAMETER_STATE_OBJECT_KEY, callback);
		}
	});

	businessModelOpeningModule.service('bmOpen_urlViewPointService', function(sbiModule_restServices, sbiModule_config, sbiModule_i18n,  bmOpen_sessionParameterService, driversDependencyService, driversExecutionService, sbiModule_dateServices) {

		var serviceScope = this;
		serviceScope.listOfDrivers = [];

	serviceScope.getParametersForExecution = function(role, buildCorrelation, businessModel) {

		var promise = null;
		bmOpen_sessionParameterService.getParametersState(
				function(ok, val, scope){
					if(ok === true){

						var params = {
								name:businessModel.name,
								role:role,
						};

						//add parameters session if they are managed
						if (sbiModule_config.isStatePersistenceEnabled == true){
							if (val == null || val == "null")
								val = "{}"; 	//clean from wrong values
							params.sessionParameters = val;
						}

						promise = sbiModule_restServices.promisePost("1.0/businessModelOpening", "filters", params);
						promise.then(function(response, status, headers, config) {
							console.log('getParametersForExecution response OK -> ', response);

							businessModel.parametersData = {};
							businessModel.parametersData.documentParameters = [];
							angular.copy(response.data.filterStatus, businessModel.parametersData.documentParameters);
							businessModel.drivers = businessModel.parametersData.documentParameters;
							serviceScope.businessModel = {};
							serviceScope.businessModel = businessModel;
							serviceScope.listOfDrivers = serviceScope.businessModel.parametersData.documentParameters;
							//check if document has parameters
							if(response && response.data.filterStatus && response.data.filterStatus.length>0) {

								sbiModule_i18n.loadI18nMap().then(function() {

									//correlation
									buildCorrelation(serviceScope.businessModel.parametersData.documentParameters, serviceScope.businessModel);


									//setting default value
									serviceScope.buildObjForFillParameterPanel(response.data.filterStatus);

//									angular.copy(serviceScope.businessModel.parametersData.documentParameters, serviceScope.listOfDrivers);

									// keep track of start value for reset!
									if(serviceScope.businessModel.parametersData.documentParameters != undefined){
										for(var i=0; i<serviceScope.businessModel.parametersData.documentParameters.length; i++){
											var param = serviceScope.businessModel.parametersData.documentParameters[i];

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
									}

								}); // end of load I 18n
							}
						},function(response, status, headers, config) {
							sbiModule_restServices.errorHandler(response.data,"error while attempt to load filters")
						});
					}
				}
		);
		return promise;
	};

	serviceScope.buildObjForFillParameterPanel = function(filterStatus){
		var fillObj = {};
		var hasDefVal = false;
		for(var i=0; i<filterStatus.length; i++){
			if(filterStatus[i].parameterValue && filterStatus[i].parameterValue.length>0) {
				var arrDefToFill = [];
				var arrDefToFillDescription = []; //TREE
				//MULTIVALUE
				hasDefVal= true;
				if(filterStatus[i].multivalue && filterStatus[i].valueSelection!='man_in' || filterStatus[i].selectionType=='TREE' || filterStatus[i].selectionType=='LOOKUP'){
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

	/*
	 * Fill Parameters Panel
	 */
	serviceScope.fillParametersPanel = function(params){

		if(serviceScope.businessModel.parametersData.documentParameters.length > 0){

			for(var i = 0; i < serviceScope.businessModel.parametersData.documentParameters.length; i++){
				var parameter = serviceScope.businessModel.parametersData.documentParameters[i];

				// in case the parameter value is missing or it is "[]", we reset the parameter.
				// TODO improve this: the "[]" is a string while it should be an actual empty array!!! fix this in combination with decodeRequestStringToJson
				// choosing a more convenient encoding/decoding
				if(!params[parameter.urlName] || params[parameter.urlName] == "[]") {
					driversExecutionService.resetParameter(parameter);
				} else {
					if(parameter.valueSelection=='lov') {
						if(parameter.selectionType.toLowerCase() == "tree" || parameter.selectionType.toLowerCase() == "lookup") {
							//TREE DESC FOR LABEL
							var ArrValue = JSON.parse(params[parameter.urlName]);
							if (typeof parameter.parameterDescription === 'undefined'){
								parameter.parameterDescription = {};
							}
							if(params[parameter.urlName+'_field_visible_description']!=undefined) {
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
				driversDependencyService.updateVisualDependency(parameter, serviceScope.businessModel);
			}
		}
	};

	/*
	 * Set max value for parameters.
	 */
	serviceScope.setMaxValueForParameters = function(params) {
		if(serviceScope.businessModel.parametersData.documentParameters.length > 0){

			for(var i = 0; i < serviceScope.businessModel.parametersData.documentParameters.length; i++){
				var parameter = serviceScope.businessModel.parametersData.documentParameters[i];

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



});


})();