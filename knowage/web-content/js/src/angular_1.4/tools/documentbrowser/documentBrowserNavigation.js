'use strict';
var app = angular.module('documentBrowserModule', ['ngMaterial', 'ui.tree', 'sbiModule', 'document_tree','angular_table','bread_crumb','document_view']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);

app.controller( 'documentBrowserNavigationController', ['$scope','$rootScope',documentBrowserMasterFunction]);
function documentBrowserMasterFunction($scope,$rootScope){
	$scope.runningDocuments=[];
	$rootScope.test="ciaoi";
	$rootScope.closeDocument=function(docId){
		alert("chiudo il documento... todo",docId)
	}
}

