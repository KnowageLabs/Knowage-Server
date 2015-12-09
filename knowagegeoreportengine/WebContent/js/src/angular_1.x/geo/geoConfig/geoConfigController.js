/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */
angular.module('geoModule')
.directive('geoConfig',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl: sbiModule_config.contextName + '/js/src/angular_1.x/geo/geoConfig/templates/geoConfigTemplate.jsp',
		controller: geoConfigControllerFunction,
//		require: "^geoMap",
		scope: {
			id:"@"
		}
	}
});

function geoConfigControllerFunction($scope,geoModule_template,sbiModule_translate,geoModule_indicators) {
	$scope.translate=sbiModule_translate;
	$scope.template = geoModule_template;
	$scope.indicators = geoModule_indicators;
	$scope.newArray=function(val){
		return new Array(val);
	}
};

