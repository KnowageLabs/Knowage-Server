(function() {
	angular.module('driversExecutionModule',[])
		.service('driversExecutionService',['sbiModule_translate',function(sbiModule_translate){
			executionService = {}

			executionService.buildStringParameters = function (documentParameters) {

				var jsonDatum =  {};
				if(documentParameters.length > 0) {
					for(var i = 0; i < documentParameters.length; i++ ) {
						var parameter = documentParameters[i];
						var valueKey = parameter.urlName;
						var descriptionKey = parameter.urlName + "_field_visible_description";
						var jsonDatumValue = null;
						var jsonDatumDesc = null;

						if(parameter.valueSelection.toLowerCase() == 'lov') {
							if(parameter.selectionType.toLowerCase() == 'tree' || parameter.selectionType.toLowerCase() == 'lookup'){
								var paramArrayTree = [];
								var paramStrTree = "";

								for(var z = 0; parameter.parameterValue && z < parameter.parameterValue.length; z++) {
									if(z > 0) {
										paramStrTree += ";";
									}

									paramArrayTree[z] = parameter.parameterValue[z];
									//modify description tree
									if(typeof parameter.parameterDescription !== 'undefined'){
										paramStrTree += parameter.parameterDescription[parameter.parameterValue[z]];
									}
								}
								jsonDatumValue = paramArrayTree;
								jsonDatumDesc = paramStrTree;

							} else {
								if(parameter.multivalue) {

									parameter.parameterValue = parameter.parameterValue || [];
									jsonDatumValue = parameter.parameterValue;
									// set descritpion
									if(parameter.parameterDescription){
										// if already in the form ; ;
										if (typeof parameter.parameterDescription === 'string') {
											jsonDatumDesc = parameter.parameterDescription;
										}
										else{
											// else in the form object
											var desc = '';
											for(var z = 0; parameter.parameterValue && z < parameter.parameterValue.length; z++) {
												if(z > 0) {
													desc += ";";
												}
												// description is at index or at value depending on parameters type
												if(parameter.parameterDescription[z] != undefined){
													desc+=parameter.parameterDescription[z];
												}
												else if(parameter.parameterDescription[parameter.parameterValue[z]]!= undefined){
													desc+=parameter.parameterDescription[parameter.parameterValue[z]];
												}
												else{
													desc+= parameter.parameterValue[z];
												}
											}
											jsonDatumDesc = desc;
										}
									}
									else{
										jsonDatumDesc = jsonDatumValue.join(";");
									}

								} else {

									jsonDatumValue = parameter.parameterValue != undefined? parameter.parameterValue : '';
									if(parameter.parameterDescription){
										if (typeof parameter.parameterDescription === 'string') {
											jsonDatumDesc = parameter.parameterDescription;
										}
										else{
											jsonDatumDesc = parameter.parameterDescription[0];
										}
									}
									else{
										jsonDatumDesc = jsonDatumValue;
									}
								}
							}
						} else if(parameter.valueSelection.toLowerCase() == 'map_in'){
							if(parameter.parameterValue && parameter.multivalue) {
								parameter.parameterValue = parameter.parameterValue || [];
								jsonDatumValue = parameter.parameterValue.length > 0 ?
										("'" + parameter.parameterValue.join("','") + "'")
										: "";
										jsonDatumDesc = jsonDatumValue;
							} else {
								jsonDatumValue = (typeof parameter.parameterValue === 'undefined')? '' : parameter.parameterValue;
								jsonDatumDesc = jsonDatumValue;
							}
						} else {
							//DATE
							if(parameter.type=='DATE'){
								var dateToSubmitFilter = $filter('date')(parameter.parameterValue, sbiModule_config.serverDateFormat);
								if( Object.prototype.toString.call( dateToSubmitFilter ) === '[object Array]' ) {
									dateToSubmit = dateToSubmitFilter[0];
								}else{
									dateToSubmit = dateToSubmitFilter;
								}
								console.log('date to sub ' + dateToSubmit);
								jsonDatumValue=dateToSubmit;
								jsonDatumDesc=dateToSubmit;
							}
							//DATE RANGE
							else if(parameter.type=='DATE_RANGE'){
								var dateToSubmitFilter = $filter('date')(parameter.parameterValue, sbiModule_config.serverDateFormat);
								if( Object.prototype.toString.call( dateToSubmitFilter ) === '[object Array]' ) {
									dateToSubmit = dateToSubmitFilter[0].value;
								}else{
									dateToSubmit = dateToSubmitFilter;
								}

								if(dateToSubmit!= '' && dateToSubmit!=null && parameter.datarange && parameter.datarange.opt){
									var defaultValueObj = {};
									for(var ii=0; ii<parameter.defaultValues.length; ii++){
										if(parameter.datarange && parameter.datarange.opt && parameter.defaultValues[ii].value==parameter.datarange.opt){
											defaultValueObj = parameter.defaultValues[ii];
											break;
										}
									}
									var rangeStr = defaultValueObj.quantity + this.getRangeCharacter(defaultValueObj.type);
									console.log('rangeStr ', rangeStr);
									jsonDatumValue=dateToSubmit+"_"+rangeStr;
									jsonDatumDesc=dateToSubmit+"_"+rangeStr;
								}else{
									jsonDatumValue='';
									jsonDatumDesc='';
								}
							}
							else{
								jsonDatumValue = (typeof parameter.parameterValue === 'undefined')? '' : parameter.parameterValue;
								jsonDatumDesc = jsonDatumValue;
							}
						}
						jsonDatum[valueKey] = jsonDatumValue;
						jsonDatum[descriptionKey] = jsonDatumDesc;
					}
				}
				return jsonDatum;
			}
			executionService.setParameterValueResult = function(parameter) {
				if(parameter.selectionType.toLowerCase() == 'tree'  ) {
					if(parameter.multivalue) {
						var toReturn = '';

						parameter.parameterValue =  [];
						parameter.parameterDescription =  {};
						documentExecuteServicesObj.recursiveChildrenChecks(parameter.parameterValue,parameter.parameterDescription, parameter.children);
						for(var i = 0; i < parameter.parameterValue.length; i++) {
							var parameterValueItem = parameter.parameterValue[i];

							if(i > 0) {
								toReturn += ",<br/>";
							}
							toReturn += parameterValueItem;
						}

						return toReturn;

					} else {
						parameter.parameterValue = (parameter.parameterValue)?
								[parameter.parameterValue] : []
								parameter.parameterDescription = (parameter.parameterDescription)?
										parameter.parameterDescription : {}

								return (parameter.parameterValue && parameter.parameterValue.value)?
										parameter.parameterValue.value : '';
					}
				}else {
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
  return executionService;

		}])

	})();
