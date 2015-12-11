/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geoModule')

.directive('geoRigthMenu',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl:sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoRigthMenu/templates/geoRigthMenuTemplate.jspf',
		controller: geoRigthMenuControllerFunction,
		scope: {
			id:"@"
		}
	}
});

function geoRigthMenuControllerFunction($scope,$timeout,$map,sbiModule_translate,geoModule_template){	
	$scope.openRigthMenu=false;
	$scope.translate=sbiModule_translate;
	$scope.template=geoModule_template;
	$scope.toggleMenu=function(){
		$scope.openRigthMenu=!$scope.openRigthMenu;
		$timeout(function() {
			$map.updateSize();
		}, 500);
	};
	
	
	
};