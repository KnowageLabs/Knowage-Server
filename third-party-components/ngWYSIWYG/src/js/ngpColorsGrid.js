angular.module('ngWYSIWYG').directive('ngpColorsGrid', ['NGP_EVENTS', function(NGP_EVENTS) {
	var linker = function (scope, element) {

		//click away
		scope.$on(NGP_EVENTS.CLICK_AWAY, function() {
			scope.$apply(function() {
				scope.show = false;
			});
		});

		element.parent().bind('click', function(e) {
			e.stopPropagation();
		});

		scope.colors = [
			'#000000', '#993300', '#333300', '#003300', '#003366', '#000080', '#333399', '#333333',
			'#800000', '#FF6600', '#808000', '#008000', '#008080', '#0000FF', '#666699', '#808080',
			'#FF0000', '#FF9900', '#99CC00', '#339966', '#33CCCC', '#3366FF', '#800080', '#999999',
			'#FF00FF', '#FFCC00', '#FFFF00', '#00FF00', '#00FFFF', '#00CCFF', '#993366', '#C0C0C0',
			'#FF99CC', '#FFCC99', '#FFFF99', '#CCFFCC', '#CCFFFF', '#99CCFF', '#CC99FF', '#FFFFFF'
		];

		scope.pick = function( color ) {
			scope.onPick({color: color});
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
			for(var i = 0; i < document.getElementsByClassName('ngp-colors-grid').length; i += 1) {
				makeUnselectable(document.getElementsByClassName("ngp-colors-grid")[i]);
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
		template: '<ul ng-show="show" class="ngp-colors-grid"><li ng-style="{\'background-color\': color}" title: "{{color}}" ng-repeat="color in colors" unselectable="on" ng-click="pick(color)"></li></ul>'
	};
}]);