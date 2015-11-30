/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geoModule')
.directive('geoCrossNavMultiselect', function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl: sbiModule_config.contextName + '/js/src/angular_1.x/geo/geoCrossNavMultiselect/templates/geoCrossNavMultiselectTemplate.jspf',
		controller: geoCrossNavMultiselectControllerFunction,
		scope: {
			id:"@"
		},
		link: function(scope,elm,attrs){

		}
	}
});

function geoCrossNavMultiselectControllerFunction(
		$scope, geoModule_dataset, $mdSidenav, $timeout, $mdDialog, 
		geoModule_template, sbiModule_translate, geo_interaction, crossNavigation){
	
	console.log("geoCrossNavMultiselectControllerFunction");
	
	$scope.sbiModule_translate = sbiModule_translate;
	$scope.geo_interaction = geo_interaction;
	$scope.geoModule_template = geoModule_template;
	
	$scope.showPanelFlag = function() {
		return ($scope.geo_interaction.selectedFeatures.length > 0 
			&& $scope.geo_interaction.type == 'cross'
			&& $scope.geoModule_template.crossnav.multiSelect);
	};
	
	
	// Workaround for forcing the angular bind, since the click event 
	// on the selected features depends by a non-angular component
	$scope.geo_interaction.addSelectedFeaturesCallbackFunction(function(){
		if ($scope.$root.$$phase != '$apply') {
			$scope.$apply();
		}
	});
	
	$scope.multiSelectNavigateTo = function() {
		crossNavigation.navigateTo(geo_interaction.selectedFeatures);
	};
};