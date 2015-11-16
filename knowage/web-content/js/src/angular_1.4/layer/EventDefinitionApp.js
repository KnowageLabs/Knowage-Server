var eventDefinitionApp = angular.module('EventDefinitionApp', ['ngMaterial', 'angular_rest', 'angular_list', 'angular_time_picker']);

eventDefinitionApp.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette('blue-grey');
});
	
eventDefinitionApp.service('translate', function() {
this.addMessageFile= function(file){
	   messageResource.load([file,"messages"], function(){});
	    
}

