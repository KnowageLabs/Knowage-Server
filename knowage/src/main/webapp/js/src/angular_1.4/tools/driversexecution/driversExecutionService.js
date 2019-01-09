
(function() {
	angular.module('driversExecutionModule')
		.service('driversExecutionService',['sbiModule_translate', 'sbiModule_config', 'driversDependencyService', '$filter',function(sbiModule_translate, sbiModule_config, driversDependencyService, $filter){
			var executionService = {}
			executionService.jsonDatum =  {};
			executionService.jsonDatumValue = null;
			executionService.jsonDatumDesc = null;
			executionService.additionalUrlDrivers =[];
			var isParameterSelectionValueLov = function(parameter) {return  parameter.valueSelection.toLowerCase() == 'lov'};
			var isParameterSelectionTypeTree = function(parameter) {return parameter.selectionType.toLowerCase() == 'tree'};
			var isParameterSelectionTypeLookup = function(parameter) {return parameter.selectionType.toLowerCase() == 'lookup'};
			var isParameterSelectionValueMapIn = function(parameter) {return parameter.valueSelection.toLowerCase() == 'map_in'};
			var isParameterTypeDate = function(parameter) {return parameter.type == 'DATE'};
			var isParameterTypeDateRange = function(parameter) {return parameter.type == 'DATE_RANGE'};


			executionService.buildStringParameters = function (documentParameters) {

				if(documentParameters && documentParameters.length > 0) {
					for(var i = 0; i < documentParameters.length; i++ ) {
						var parameter = documentParameters[i];
						var valueKey = parameter.urlName;
						var descriptionKey = parameter.urlName + "_field_visible_description";

						if(isParameterSelectionValueLov(parameter)) {

							if(isParameterSelectionTypeTree(parameter) || isParameterSelectionTypeLookup(parameter)){
								parseParameterTreeOrLookupSelectionType(parameter);
							} else {
								parseParameterListOrComboxSelectionType(parameter);
							}

						} else if(isParameterSelectionValueMapIn(parameter)){

							parseParameterMapInSelectionValue(parameter);

						} else {

							if(isParameterTypeDate(parameter)){
								parseDateParameterType(parameter)
							}

							else if(isParameterTypeDateRange(parameter)){
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
			};

			executionService.setParameterValueResult = function(parameter) {
				if(isParameterSelectionTypeTree(parameter)) {
					setParameterValueForTreeSelectionType(parameter)
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
			};

			executionService.recursiveChildrenChecks = function(parameterValue,parameterDescription,childrenArray) {
				childrenArray = childrenArray || [];
				for(var i = 0; i < childrenArray.length; i++) {
					var childItem = childrenArray[i];
					if(childItem.checked && childItem.checked == true) {
						parameterValue.push(childItem.value);
						parameterDescription[childItem.value]=childItem.description;
					}
					if(!childItem.leaf) {
						executionService.recursiveChildrenChecks(parameterValue,parameterDescription,childItem.children);
					}
				}
			};

			executionService.resetParameter = function(parameter) {

				if(parameter.defaultValue != undefined && parameter.defaultValue != '' && parameter.defaultValue!= '[]'){
					parameter.parameterValue = angular.copy(parameter.defaultValue);
					parameter.parameterDescription = angular.copy(parameter.defaultValueDescription);
					if(Array.isArray(parameter.parameterValue)){
						for(var j = 0; j < parameter.parameterValue.length; j++) {
							var val = parameter.parameterValue[j];
							if(!parameter.parameterDescription[val] && parameter.parameterDescription[j]!= undefined){
								parameter.parameterDescription[val]=parameter.parameterDescription[j];
							}else{
								parameter.parameterDescription[val]=val;
							}
						}
					}
				}else{
					resetWithoutDefaultValues(parameter)
				}
			}

			executionService.resetParameterInnerLovData = function(childrenArray) {
				childrenArray = childrenArray || [];

				for(var i = 0; i < childrenArray.length; i++) {
					var childItem = childrenArray[i];
					childItem.checked = false;

					if(!childItem.leaf) {
						driversExecutionService.resetParameterInnerLovData(childItem.children);
					}
				}
			}

			executionService.emptyParameter = function(parameter) {
				if(isParameterSelectionValueLov(parameter)) {
					if(isParameterSelectionTypeTree(parameter)) {
						if(parameter.multivalue) {
							parameter.parameterValue = [];
							executionService.resetParameterInnerLovData(parameter.children);
						} else {
							parameter.parameterValue = '';
						}
					}else {
						if(parameter.multivalue) {
							parameter.parameterValue = [];
						} else {
							parameter.parameterValue = '';
						}
					}
				} else {
					parameter.parameterValue = '';
					if(isParameterTypeDateRange(parameter) && parameter.datarange){
						parameter.datarange.opt='';
					}

				}
			}

			executionService.parseDateTemp = function(date){
				result = "";
				if(date == "d/m/Y"){
					result = "dd/MM/yyyy";
				}
				if(date =="m/d/Y"){
					result = "MM/dd/yyyy"
				}
				return result;
			};

			executionService.buildCorrelation = function(parameters, execProperties){
				driversDependencyService.buildVisualCorrelationMap(parameters,execProperties);
				driversDependencyService.buildDataDependenciesMap(parameters,execProperties);
				driversDependencyService.buildLovCorrelationMap(parameters,execProperties);
				//INIT VISUAL CORRELATION PARAMS
				for(var i=0; i<parameters.length; i++){
					driversDependencyService.updateVisualDependency(parameters[i],execProperties);
				}
			};

			executionService.hasMandatoryDrivers = function(drivers){
				var showSideBar = false;
				if(drivers){
					for(var i = 0; i < drivers.length;i++){
						if(drivers[i].mandatory){
							if(drivers[i].defaultValues && drivers[i].defaultValues.length == 1 && drivers[i].defaultValues[0].isEnabled){
								executionService.additionalUrlDrivers.push(parseParameterSingleDefaultValue([drivers[i]]));
							}else{
								showSideBar = true;
							}
						}
					}
				}
				return showSideBar
			};

			executionService.createObjectFromArray = function(drivers){
				var returnObject = {}
				for(var i = 0; i < drivers.length; i++){
					var driverName = Object.keys(drivers[i]);
					var driverValue = drivers[i][Object.keys(drivers[i])[0]];
					returnObject[driverName] = driverValue;
				}
				return returnObject
			}

			var parseParameterSingleDefaultValue = function(rawDrivers){
				var drivers = executionService.buildStringParameters(rawDrivers);
				var driverName = Object.keys(drivers)[0];
				var driverValue = drivers[Object.keys(drivers)[0]][0].value;
				var driverObject = {};
				driverObject[driverName] = driverValue;
				return driverObject;
			}

			var resetWithoutDefaultValues = function(parameter){

				if(isParameterSelectionValueLov(parameter)) {
					if(isParameterSelectionTypeTree(parameter)) {
						if(parameter.multivalue) {
							parameter.parameterValue = [];
							driversExecutionService.resetParameterInnerLovData(parameter.children);
						} else {
							parameter.parameterValue = '';
						}
					}else {
						if(parameter.multivalue) {
							parameter.parameterValue = [];

						} else {
							parameter.parameterValue = '';
						}
					}
				} else {
					parameter.parameterValue = '';
					if(isParameterTypeDateRange(parameter) && parameter.datarange){
						parameter.datarange.opt='';
					}

				}
			}

			var setParameterValueForTreeSelectionType = function(parameter){

				if(parameter.multivalue) {
					var toReturn = '';

					parameter.parameterValue =  [];
					parameter.parameterDescription =  {};
					executionService.recursiveChildrenChecks(parameter.parameterValue,parameter.parameterDescription, parameter.children);
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
									desc += parameter.parameterValue[z];
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

			var getRangeCharacter = function(type){
				result = "";
				if(type=="days"){
					result = "D";
				}
				if(type=="years"){
					result = "Y";
				}
				if(type=="months"){
					result = "M";
				}
				if(type=="weeks"){
					result = "W";
				}
				return result;
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
					var rangeStr = defaultValueObj.quantity + getRangeCharacter(defaultValueObj.type);
					console.log('rangeStr ', rangeStr);
					executionService.jsonDatumValue=dateToSubmit+"_"+rangeStr;
					executionService.jsonDatumDesc=dateToSubmit+"_"+rangeStr;
				}else{
					executionService.jsonDatumValue='';
					executionService.jsonDatumDesc='';
				}
			};

			executionService.prepareDriversForSending = function(drivers){

				var transformedDrivers = {};
					if(drivers){
						for(var i = 0; i < drivers.length; i++){

								var tempDriver = {}
								tempDriver.urlName = drivers[i].urlName;
								tempDriver.type = drivers[i].type;
								tempDriver.multivalue = drivers[i].multivalue;
								if(drivers[i].parameterValue && Array.isArray(drivers[i].parameterValue)){
									tempDriver.value =  drivers[i].parameterValue[0];
								}else{
									tempDriver.value = drivers[i].parameterValue;
								}
								transformedDrivers[tempDriver.urlName] = tempDriver;

						}
					}
				return transformedDrivers;
			}
  return executionService;

		}])

	})();