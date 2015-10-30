/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geo_module',['ngMaterial','ngAnimate','angular_table','sbiModule'])
.directive('geoMap',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl:sbiModule_config.contextName+'/js/src/angular_1.4/tools/commons/geo/geoMap/templates/geoMapTemplate.jspf',
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

function geoMapControllerFunction($scope,$map,layerServices,baseLayer,sbiModule_config,sbiModule_translate,sbiModule_user){

	console.log("sbiModule.config",sbiModule_config)
	console.log("sbiModule_user",sbiModule_user)
	layerServices.alterBaseLayer(baseLayer.Default.OpenStreetMap); 

	
}