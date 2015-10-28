var app = angular.module('geoManager', [ 'ngMaterial', 'angular_rest','geo_module' ]);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
			'blue-grey');
});

app.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});

app.controller('mapCtrl', [ "translate", "restServices",  "$scope","$mdBottomSheet","$mdSidenav", funzione ]);

function funzione(translate,restServices,$scope,$mdBottomSheet,$mdSidenav){

}
