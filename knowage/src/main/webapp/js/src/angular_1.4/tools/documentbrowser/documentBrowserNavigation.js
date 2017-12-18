'use strict';
var app = angular.module('documentBrowserModule', ['ngMaterial', 'ui.tree', 'sbiModule', 'document_tree', 'componentTreeModule', 'angular_table','bread_crumb','document_view','ngCookies']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);

app.directive('mdTabFixedFirstDocumentBrowser',
        function ($compile) {
            return {
                link: function (scope, element, attrs, ctrl, transclude) {
	        		var mdTabsWrapper= angular.element(document.querySelector("md-tabs.documentNavigationToolbar"));
	                angular.element(mdTabsWrapper).append(element);
                }
            };
        })
        
app.directive('mdTabFixedLastClearTabs',
		function ($compile) {
	return {
		link: function (scope, element, attrs, ctrl, transclude) {
			var mdTabsWrapper= angular.element(document.querySelector("md-tabs.documentNavigationToolbar"));
			angular.element(mdTabsWrapper).append(element);
		}
	};
})

app.controller( 'documentBrowserNavigationController', ['$scope','sbiModule_translate','$mdDialog',documentBrowserMasterFunction]);
function documentBrowserMasterFunction($scope,sbiModule_translate,$mdDialog){
	$scope.translate=sbiModule_translate;
	$scope.runningDocuments=[];
	$scope.documentNavigationToolbarSelectedIndex=0;
	$scope.keys = {
			'iconFolder' 		: 'fa fa-square',
			'iconFolderOpen'	: 'fa fa-square-o',
			'multiFolders'		: 'fa fa-plus-square',
			'multiFoldersOpen'	: 'fa fa-minus-square'
		};
	
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
	 
	 $scope.closeTabs=function(type){
		 var startIndex;
		 var totalDocumentCount=0;
		 var tmpDoc={};
		 if(type=='all'){
			 startIndex=0;
			 totalDocumentCount=$scope.runningDocuments.length;
		 }else if(type=="right"){
			 startIndex=$scope.documentNavigationToolbarSelectedIndex;
			 totalDocumentCount=$scope.runningDocuments.length-$scope.documentNavigationToolbarSelectedIndex;
		 }else if(type=="current"){			 
			 totalDocumentCount=1;
			 
		 }else{
			 //other
			 totalDocumentCount=$scope.runningDocuments.length-1;
		 } 
		 if(totalDocumentCount<=0){
			 return;
		 }
		 
		 var confirm = $mdDialog.confirm()
         .title(sbiModule_translate.format(sbiModule_translate.load('sbi.browser.close.document.message'), totalDocumentCount))
         .content(sbiModule_translate.load('sbi.browser.close.document.confirm'))
         .ariaLabel('Close tab')
         .ok(sbiModule_translate.load("sbi.general.continue"))
         .cancel(sbiModule_translate.load("sbi.general.cancel"));
		   $mdDialog.show(confirm).then(function() {
			 
			   if(type=='other'){ 
				   $scope.runningDocuments.splice(0,$scope.documentNavigationToolbarSelectedIndex-1);
				   $scope.runningDocuments.splice(1,$scope.runningDocuments.length);
			   }else if(type=='current'){
				   $scope.runningDocuments.splice($scope.documentNavigationToolbarSelectedIndex-1,1);
			   } else{
				   $scope.runningDocuments.splice(startIndex,$scope.runningDocuments.length);
			   }
		   } );
		 
		
	 }
	
	
}

