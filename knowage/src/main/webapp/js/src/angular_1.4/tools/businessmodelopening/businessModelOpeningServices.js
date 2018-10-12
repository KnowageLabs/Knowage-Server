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

	businessModelOpeningModule.service('bmOpen_urlViewPointService', function(sbiModule_restServices, sbiModule_config, sbiModule_i18n,  bmOpen_sessionParameterService) {

		var serviceScope = this;
		serviceScope.listOfDrivers = [];

	serviceScope.getParametersForExecution = function(role, buildCorrelation, businessModel) {

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

						sbiModule_restServices.promisePost("1.0/businessModelOpening", "filters", params)
						.then(function(response, status, headers, config) {
							console.log('getParametersForExecution response OK -> ', response);
							//check if document has parameters
							if(response && response.data.filterStatus && response.data.filterStatus.length>0) {
								serviceScope.listOfDrivers = response.data.filterStatus;
								//build documentParameters
								angular.copy(response.data.filterStatus, businessModel.parametersData.documentParameters);

								sbiModule_i18n.loadI18nMap().then(function() {
									// keep track of start value for reset!
									if(businessModel.parametersData.documentParameters != undefined){
										for(var i=0; i<businessModel.parametersData.documentParameters.length; i++){
											var param = businessModel.parametersData.documentParameters[i];

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
	};

});


})();