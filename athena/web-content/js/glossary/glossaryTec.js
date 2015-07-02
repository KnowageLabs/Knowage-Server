var app = angular.module('AIDA_GLOSSARY_TECNICAL_USER', [ 'ngMaterial', 'angular_rest','angularUtils.directives.dirPagination' ]);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
			'blue-grey');
});


app.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});

app.controller('Controller', [ "translate", "restServices", "$q", "$scope",
                       		"$mdDialog", "$filter", "$timeout", "$mdToast", funzione ]);


var listDocument = [{
					DOCUMENT_ID :0,
					DOCUMENT_NM : "DOC1",
				},{
					DOCUMENT_ID :1,
					DOCUMENT_NM : "DOC2",
				},{
					DOCUMENT_ID :2,
					DOCUMENT_NM : "DOC3",
				} 
				]

function funzione(translate, restServices, $q, $scope, $mdDialog, $filter,
		$timeout, $mdToast) {
	ctrl=this;
	ctrl.listDoc=listDocument;
}