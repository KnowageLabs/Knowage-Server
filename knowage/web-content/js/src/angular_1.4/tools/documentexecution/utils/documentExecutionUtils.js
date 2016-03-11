(function() {
	angular.module('documentExecutionModule')
	.factory('documentExecuteUtils', function($mdToast) {
		var obj = {
				
			EmptyViewpoint : {
				NAME : "",
				DESCRIPTION: "",
				SCOPE : "",
				OBJECT_LABEL : "",
				ROLE :"",
				VIEWPOINT : JSON.parse("{}")
			},
				
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

			buildStringParameters : function (documentParameters){
				var jsonDatum =  {};
				if(documentParameters.length > 0){
					for(var i = 0; i < documentParameters.length; i++ ){
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
			
			resetParameter: function(parameter) {
				if(parameter.valueSelection.toLowerCase() == 'lov') {
					if(parameter.multivalue) {
						parameter.parameterValue = [];
						
						for(var j = 0; j < parameter.defaultValues.length; j++) {
							var defaultValue = parameter.defaultValues[j];
							defaultValue.isSelected = false;
						}
					} else {
						parameter.parameterValue = '';
					}
				} else {
					parameter.parameterValue = '';
				}
			},
			
			showParameterHtml: function(parameter) {
				if(parameter.valueSelection.toLowerCase() == 'lov' && parameter.multivalue) {
					parameter.parameterValue = parameter.parameterValue || [];
					var toReturn = parameter.parameterValue.join(",<br/>");
					return toReturn;
				} else {
					parameter.parameterValue = parameter.parameterValue || '';
					return parameter.parameterValue;
				}
			}
		};
		return obj;
	});
})();