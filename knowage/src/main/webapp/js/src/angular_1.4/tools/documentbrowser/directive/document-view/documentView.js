(function() {
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

angular.module('document_view', ['ngMaterial','sbiModule'])
.directive('documentView', function() {
	return {
		templateUrl: currentScriptPath + '/document-view.jsp',
		controller: documentViewControllerFunction,
		replace:true,
		 priority: 10,
		scope: {
			ngModel:"=",
			showGridView:"=?",
			tableSpeedMenuOption:"=?",
			selectedDocument:"=?",
			selectDocumentAction:"&",
			editDocumentAction:"&",
			cloneDocumentAction:"&",
			deleteDocumentAction:"&",
			executeDocumentAction:"&",
			orderingDocumentCards:"=?",
			firstInitialSorting:"=?",
		},
		link: function (scope, elem, attrs) {

			elem.css("margin","0px");
			 if(!attrs.tableSpeedMenuOption){
				 scope.tableSpeedMenuOption=[];
			 }
		}
	}
});

function documentViewControllerFunction($scope,sbiModule_config, sbiModule_translate, sbiModule_i18n){
	$scope.translate = sbiModule_translate;

	$scope.i18n = sbiModule_i18n;
	if(!$scope.i18n.isLoaded()){
			$scope.i18n.loadI18nMap();
		}

	$scope.sbiModule_config=sbiModule_config;
	$scope.clickDocument=function(item){
		$scope.selectDocumentAction({doc: item});
	}
}
})();