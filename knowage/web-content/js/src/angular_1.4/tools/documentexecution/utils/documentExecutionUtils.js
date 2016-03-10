(function() {
		angular.module('documentExecutionModule')
		.factory('documentExecuteUtils', function($mdToast) {
			var obj = {
					decodeRequestStringToJson: function (str) {
					    var hash;
					    var myJson = {};
					    var hashes = str.slice(str.indexOf('?') + 1).split('&');
					    for (var i = 0; i < hashes.length; i++) {
					        hash = hashes[i].split('=');
					        myJson[hash[0]] = hash[1];
					    }
					    return myJson;
					},
					
					
					 showToast: function(text, time) {
						var timer = time == undefined ? 6000 : time;
						console.log(text)
						$mdToast.show($mdToast.simple().content(text).position('top').action(
								'OK').highlightAction(false).hideDelay(timer));
					},
					
					buildStringParameters : function (documentParameters){
						console.log("$scope.documentParameters -> ", documentParameters);
						var jsonDatum =  {};
						if(documentParameters.length > 0){
							for(var i = 0; i < documentParameters.length; i++ ){
								var parameter = documentParameters[i];
								var valueKey = parameter.urlName;
								var descriptionKey = parameter.urlName + "_field_visible_description";					
								var jsonDatumValue = null;
								if(parameter.valueSelection.toLowerCase() == 'lov') {
									if(Array.isArray(parameter.parameterValue)) {
										var arrayAsString = '';					
										for(var j = 0; j < parameter.parameterValue.length; j++) {
											if(j > 0) {
												arrayAsString += ',';
											}
											arrayAsString += "'" + parameter.parameterValue[j] + "'";
										}

										jsonDatumValue = arrayAsString;
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
						return  jsonDatum;
					}
					
					
					
					
			
			};
			return obj;
		});
	})();