/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geo_module')
.directive('geoMap',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl:sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoMap/templates/geoMapTemplate.jspf',
		controller: geoMapControllerFunction,
		transclude: true,
		scope: {
			mapId:"@"
		},
		link: function(scope,elm,attrs){
			console.log("inizializzo geo-map con id= "+scope.mapId);
			
		}
	}
});

function geoMapControllerFunction($scope,geoReportUtils){
	geoReportUtils.GetTargetDataset();
	
}