(function() {
	angular.module('driversExecutionModule',[])
		.service('driversExecutionService',['sbiModule_translate','sbiModule_config','$filter',function(sbiModule_translate,sbiModule_config,$filter){
			executionService = {}
			executionService.jsonDatum =  {};
			executionService.jsonDatumValue = null;
			executionService.jsonDatumDesc = null;
			executionService.buildStringParameters = function (documentParameters) {


				if(documentParameters.length > 0) {
					for(var i = 0; i < documentParameters.length; i++ ) {
						var parameter = documentParameters[i];
						var valueKey = parameter.urlName;
						var descriptionKey = parameter.urlName + "_field_visible_description";
						var isParameterSelectionValueLov = parameter.valueSelection.toLowerCase() == 'lov';
						var isParameterSelectionTypeTreeOrLookup = parameter.selectionType.toLowerCase() == 'tree' || parameter.selectionType.toLowerCase() == 'lookup';
						var isParameterSelectionValueMapIn = parameter.valueSelection.toLowerCase() == 'map_in';
						var isParameterTypeDate = parameter.type =='DATE';
						var isParameterTypeDateRange = parameter.type=='DATE_RANGE';

						if(isParameterSelectionValueLov) {

							if(isParameterSelectionTypeTreeOrLookup){
								parseParameterTreeOrLookupSelectionType(parameter);
							} else {
								parseParameterListOrComboxSelectionType(parameter);
							}

						} else if(isParameterSelectionValueMapIn){

							parseParameterMapInSelectionValue(parameter);

						} else {

							if(isParameterTypeDate){
								parseDateParameterType(parameter)
							}

							else if(isParameterTypeDateRange){
								parseDateRangeParameterType(parameter)

							}
							else{
								executionService.jsonDatumValue = (typeof parameter.parameterValue === 'undefined')? '' : parameter.parameterValue;
								executionService.jsonDatumDesc = executionService.jsonDatumValue;
							}
						}
						executionService.jsonDatum[valueKey] = executionService.jsonDatumValue;
						executionService.jsonDatum[descriptionKey] = executionService.jsonDatumDesc;
					}
				}
				return executionService.jsonDatum;
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

			var parseParameterTreeOrLookupSelectionType = function(parameter){
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
				executionService.jsonDatumValue = paramArrayTree;
				executionService.jsonDatumDesc = paramStrTree;

			};

			var parseParameterListOrComboxSelectionType = function(parameter){
				if(parameter.multivalue) {
					parameter.parameterValue = parameter.parameterValue || [];
					jsonDatumValue = parameter.parameterValue;
					// set descritpion
					if(parameter.parameterDescription){
						// if already in the form ; ;
						if (typeof parameter.parameterDescription === 'string') {
							executionService.jsonDatumDesc = parameter.parameterDescription;
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
							executionService.jsonDatumDesc = desc;
						}
					}else{
						executionService.jsonDatumDesc = jsonDatumValue.join(";");
					}

				} else {

					executionService.jsonDatumValue = parameter.parameterValue != undefined? parameter.parameterValue : '';
					if(parameter.parameterDescription){
						if (typeof parameter.parameterDescription === 'string') {
							executionService.jsonDatumDesc = parameter.parameterDescription;
						}
						else{
							executionService.jsonDatumDesc = parameter.parameterDescription[0];
						}
					}
					else{
						executionService.jsonDatumDesc = executionService.jsonDatumValue;
					}
				}
			};

			var parseParameterMapInSelectionvalue = function(parameter){
				if(parameter.parameterValue && parameter.multivalue) {
					parameter.parameterValue = parameter.parameterValue || [];
					executionService.jsonDatumValue = parameter.parameterValue.length > 0 ?
							("'" + parameter.parameterValue.join("','") + "'")
							: "";
							executionService.jsonDatumDesc = executionService.jsonDatumValue;
				} else {
					executionService.jsonDatumValue = (typeof parameter.parameterValue === 'undefined')? '' : parameter.parameterValue;
					executionService.jsonDatumDesc = executionService.jsonDatumValue;
				}
			};

			var parseDateParameterType = function(parameter){
				var dateToSubmitFilter = $filter('date')(parameter.parameterValue, sbiModule_config.serverDateFormat);
				if( Object.prototype.toString.call( dateToSubmitFilter ) === '[object Array]' ) {
					dateToSubmit = dateToSubmitFilter[0];
				}else{
					dateToSubmit = dateToSubmitFilter;
				}
				console.log('date to sub ' + dateToSubmit);
				executionService.jsonDatumValue=dateToSubmit;
				executionService.jsonDatumDesc=dateToSubmit;
			};

			var parseDateRangeParameterType = function(parameter){

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
					executionService.jsonDatumValue=dateToSubmit+"_"+rangeStr;
					executionService.jsonDatumDesc=dateToSubmit+"_"+rangeStr;
				}else{
					executionService.jsonDatumValue='';
					executionService.jsonDatumDesc='';
				}
			};

  return executionService;

		}])

	})();
