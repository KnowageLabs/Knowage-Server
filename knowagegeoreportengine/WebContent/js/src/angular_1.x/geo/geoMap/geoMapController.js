/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geoModule')
.directive('geoMap',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl:sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoMap/templates/geoMapTemplate.jspf',
		controller: geoMapControllerFunction,
		transclude: true,
		scope: {
			mapId:"@"
		}
	}
});


//Dont'remove geoReport_saveTemplate from function because it initialize the factory to save the template
function geoMapControllerFunction($scope,geoModule_reportUtils,geoReport_saveTemplate,geoModule_layerServices){
	geoModule_reportUtils.getTargetDataset();
	$scope.openCrossNavMultiSelectFlag = false;
	$scope.closePopup=function(){
		geoModule_layerServices.removeSelectPopup();
	}
}