'use strict';
var app = angular.module('documentBrowserModule', ['ngMaterial', 'ui.tree', 'sbiModule', 'document_tree','angular_table','bread_crumb','document_view']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);

app.directive('mdTabFixedFirst',
        function ($compile) {
            return {
                link: function (scope, element, attrs, ctrl, transclude) {
	        		var mdTabsWrapper= angular.element(document.querySelector("md-tabs.documentNavigationToolbar"));
	                angular.element(mdTabsWrapper).append(element);
                }
            };
        })

app.controller( 'documentBrowserNavigationController', ['$scope',documentBrowserMasterFunction]);
function documentBrowserMasterFunction($scope){
	$scope.runningDocuments=[];
	
	$scope.removeDocumentFromList=function(docId){
		 for(var index in $scope.runningDocuments){
				if($scope.runningDocuments[index].id==docId){
					$scope.runningDocuments.splice(index,1);
					break;
				}
			}
	}
	
	 $scope.closeDocument=function(docId){
		 $scope.removeDocumentFromList(docId)
		 $scope.$apply();
	 }
	
}

