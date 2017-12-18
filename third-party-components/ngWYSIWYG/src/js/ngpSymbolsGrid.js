angular.module('ngWYSIWYG').directive('ngpSymbolsGrid', ['NGP_EVENTS', function(NGP_EVENTS) {
	var linker = function (scope, element) {

		scope.$on(NGP_EVENTS.CLICK_AWAY, function() {
			scope.$apply(function() {
				scope.show = false;
			});
		});

		element.parent().bind('click', function(e) {
			e.stopPropagation();
		});

		scope.symbols = [
			'&iexcl;', '&iquest;', '&ndash;', '&mdash;', '&raquo;', '&laquo;', '&copy;',
			'&divide;', '&micro;', '&para;', '&plusmn;', '&cent;', '&euro;', '&pound;', '&reg;',
			'&sect;', '&trade;', '&yen;', '&deg;', '&forall;', '&part;', '&exist;', '&empty;',
			'&nabla;', '&isin;', '&notin;', '&ni;', '&prod;', '&sum;', '&uarr;', '&rarr;', '&darr;',
			'&spades;', '&clubs;', '&hearts;', '&diams;', '&aacute;', '&agrave;', '&acirc;', '&aring;',
			'&atilde;', '&auml;', '&aelig;', '&ccedil;', '&eacute;', '&egrave;', '&ecirc;', '&euml;',
			'&iacute;', '&igrave;', '&icirc;', '&iuml;', '&ntilde;', '&oacute;', '&ograve;',
			'&ocirc;', '&oslash;', '&otilde;', '&ouml;', '&szlig;', '&uacute;', '&ugrave;',
			'&ucirc;', '&uuml;', '&yuml;'
		];

		scope.pick = function( symbol ) {
			scope.onPick({symbol: symbol});
		};

		element.ready(function() {
			//real deal for IE
			function makeUnselectable(node) {
				if (node.nodeType == 1) {
					node.setAttribute("unselectable", "on");
					node.unselectable = 'on';
				}
				var child = node.firstChild;
				while (child) {
					makeUnselectable(child);
					child = child.nextSibling;
				}
			}
			//IE fix
			for(var i = 0; i < document.getElementsByClassName('ngp-symbols-grid').length; i += 1) {
				makeUnselectable(document.getElementsByClassName("ngp-symbols-grid")[i]);
			}
		});
	};
	return {
		link: linker,
		scope: {
			show: '=',
			onPick: '&'
		},
		restrict: 'AE',
		template: '<ul ng-show="show" class="ngp-symbols-grid"><li ng-repeat="symbol in symbols" unselectable="on" ng-click="pick(symbol)" ng-bind-html="symbol"></li></ul>'
	}
}]);