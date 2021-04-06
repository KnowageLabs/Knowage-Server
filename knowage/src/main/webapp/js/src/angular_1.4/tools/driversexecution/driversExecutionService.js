
(function() {
	angular.module('driversExecutionModule')
		.service('driversExecutionService',['sbiModule_translate', 'sbiModule_config', '$filter',function(sbiModule_translate, sbiModule_config, $filter){
			var executionService = {}
			executionService.jsonDatum =  {};
			executionService.jsonDatumValue = null;
			executionService.jsonDatumDesc = null;

			executionService.gvpCtrlViewpoints = [];

			executionService.emptyViewpoint = {
						NAME : "",
						DESCRIPTION: "",
						SCOPE : "",
						OBJECT_LABEL : "",
						ROLE :"",
						VIEWPOINT : JSON.parse("{}")
			};

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
								executionService.jsonDatumValue = (parameter.parameterValue == undefined)? '' : parameter.parameterValue;
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
					if((childItem.checked && childItem.checked == true) && (!childItem.$parent || !childItem.$parent.checked)) {
						parameterValue.push(childItem.value);
						parameterDescription.push(childItem.description);
					}
					if(!childItem.leaf) {
						executionService.recursiveChildrenChecks(parameterValue,parameterDescription,childItem.children);
					}
				}
			};

			executionService.resetParameter = function(parameter, mainReset) {

				if(parameter.defaultValue != undefined && parameter.defaultValue != '' && parameter.defaultValue!= '[]'){
					if (parameter.type == "NUM") parameter.parameterValue = parseInt(parameter.defaultValue);
					else if (parameter.type == "DATE") parameter.parameterValue = new Date(parameter.defaultValue.split("#")[0]);
					else parameter.parameterValue = angular.copy(parameter.defaultValue);
					parameter.parameterDescription = angular.copy(parameter.defaultValueDescription);
					if(Array.isArray(parameter.parameterValue) && !Array.isArray(parameter.parameterDescription)){
						for(var j = 0; j < parameter.parameterValue.length; j++) {
							var val = parameter.parameterValue[j];
							if(!parameter.parameterDescription[val] && parameter.parameterDescription[j]!= undefined){
								parameter.parameterDescription[val]=parameter.parameterDescription[j];
							}else{
								parameter.parameterDescription[val]=val;
							}
						}
					}else if(Array.isArray(parameter.parameterValue) && Array.isArray(parameter.parameterDescription)){
						var tempParamDescription = {};
						for(var j = 0; j < parameter.parameterValue.length; j++) {
							tempParamDescription[parameter.parameterValue[j]] = parameter.parameterDescription[j];
						}
						parameter.parameterDescription = tempParamDescription;
					}
				} else if(parameter.defaultValues != undefined && parameter.defaultValues.length==1 && parameter.mandatory &&
						(parameter.selectionType=='LIST' ||	parameter.selectionType=='COMBOBOX') && mainReset) {
					parameter.parameterValue = parameter.multivalue ? [parameter.defaultValues[0].value] : parameter.defaultValues[0].value;
					parameter.parameterDescription = parameter.multivalue ?	[parameter.defaultValues[0].description] : parameter.defaultValues[0].description;
				} else {
					executionService.emptyParameter(parameter, true);
				}
			}

			executionService.resetParameterInnerLovData = function(childrenArray) {
				childrenArray = childrenArray || [];

				for(var i = 0; i < childrenArray.length; i++) {
					var childItem = childrenArray[i];
					childItem.checked = false;

					if(!childItem.leaf) {
						executionService.resetParameterInnerLovData(childItem.children);
					}
				}
			}

			executionService.emptyParameter = function(parameter, resetWithoutDefaultValue) {
				if(isParameterSelectionValueLov(parameter)) {
					if(isParameterSelectionTypeTree(parameter)) {
						if(parameter.multivalue) {
							parameter.parameterValue = [];
							parameter.parameterDescription = [];
							executionService.resetParameterInnerLovData(parameter.children);
						} else {
							delete parameter.parameterValue;
							if(resetWithoutDefaultValue) parameter.parameterDescription = {};
						}
					}else {
						if(parameter.multivalue) {
							parameter.parameterValue = [];
							if(resetWithoutDefaultValue) parameter.parameterDescription = '';
						} else {
							delete parameter.parameterValue;
							if(resetWithoutDefaultValue) parameter.parameterDescription = {};
						}
					}
				} else {
					delete parameter.parameterValue;
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

			executionService.showFilterIcon = false;

			executionService.hasMandatoryDrivers = function(drivers){
				var showSideBar = false;
				if(drivers && drivers.length > 0){
					for(var i = 0; i < drivers.length; i++){
						if(drivers[i].mandatory){
							if(drivers[i].defaultValues && drivers[i].defaultValues.length == 1 && drivers[i].defaultValues[0].isEnabled){
								showSideBar = false;
								executionService.showFilterIcon = false;
							} else {
								showSideBar = true;
								executionService.showFilterIcon = true;
							}
						} else {
							showSideBar = false;
							executionService.showFilterIcon = true;
						}
					}
				}
				return showSideBar;
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

			var setParameterValueForTreeSelectionType = function(parameter){

				if(parameter.multivalue) {
					var toReturn = '';

					parameter.parameterValue =  [];
					parameter.parameterDescription =  [];
					executionService.recursiveChildrenChecks(parameter.parameterValue,parameter.parameterDescription, parameter.children);
					return 	parameter.parameterDescription;
				} else {
					parameter.parameterDescription = [parameter.parameterDescription[parameter.parameterValue]];
					parameter.parameterValue = (parameter.parameterValue) ?	[parameter.parameterValue] : [];
					return (parameter.parameterValue && parameter.parameterValue.value) ? parameter.parameterValue.value : '';
				}
			}


			var parseParameterTreeOrLookupSelectionType = function(parameter){
				var paramArrayTree = [];
				var paramStrTree = "";

				for(var z = 0; parameter.parameterValue && z < parameter.parameterValue.length; z++) {
					if(z > 0) {
						paramStrTree += ";";
					}
					var value = parameter.parameterValue[z].value ? parameter.parameterValue[z].value : parameter.parameterValue[z];
					paramArrayTree[z] = value;
					//modify description tree
					if(typeof parameter.parameterDescription !== 'undefined'){
						var descr = parameter.parameterDescription[value];
						if (typeof descr == 'undefined') descr = parameter.parameterDescription[z];
						if (typeof descr == 'undefined') descr = value;
						paramStrTree += descr;
					}
				}
				executionService.jsonDatumValue = paramArrayTree;
				executionService.jsonDatumDesc = paramStrTree;

			};

			var parseParameterListOrComboxSelectionType = function(parameter){
				if(parameter.multivalue) {
					parameter.parameterValue = parameter.parameterValue || [];
					executionService.jsonDatumValue = parameter.parameterValue;
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
						executionService.jsonDatumDesc = executionService.jsonDatumValue.join(";");
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
				var dateToSubmitFilter = [];
				if (parameter.parameterValue || (parameter.parameterDescription && parameter.parameterDescription[0]))
					dateToSubmitFilter = $filter('date')(parameter.parameterValue || parameter.parameterDescription[0], sbiModule_config.serverDateFormat);
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
								var tempDriver = [];
								var urlName = drivers[i].urlName;
								if(drivers[i].parameterValue && Array.isArray(drivers[i].parameterValue)){
									for(var j = 0; j < drivers[i].parameterValue.length; j++) {
										if(drivers[i].parameterValue[j].value && drivers[i].parameterValue[j].description) {
											var val = drivers[i].parameterValue[j];
										} else {
											var val = {value: drivers[i].parameterValue[j]};
											if(drivers[i].parameterDescription && Array.isArray(drivers[i].parameterDescription)) {
												val.description = drivers[i].parameterDescription[j];
											} else {
												val.description = drivers[i].parameterDescription[drivers[i].parameterValue[j]];
											}
										}
										tempDriver.push(val);
									}
								}else{
									 var val = {value: drivers[i].parameterValue};
									 if(drivers[i].parameterDescription) {
										 val.description = drivers[i].parameterDescription;
									 } else {
										 val.description = drivers[i].parameterValue;
									 }
									 tempDriver.push(val);
								}
								transformedDrivers[urlName] = tempDriver;

						}
					}
				return transformedDrivers;
			}

			executionService.driversAreSet = function(drivers){
				var preparedDriver = executionService.prepareDriversForSending(drivers);
				for(var k in preparedDriver) {
					var currDriverDescValArr = preparedDriver[k];
					if(typeof currDriverDescValArr == 'undefined') {
						return false;
					} else {
						if (currDriverDescValArr.length == 0) {
							return false;
						} else {
							for (var i in currDriverDescValArr) {
								var curr = currDriverDescValArr[i];
								if (curr.value == undefined) {
									return false;
								}
							}
						}
					}
				}
				return true;
			}

  return executionService;

		}])

	})();