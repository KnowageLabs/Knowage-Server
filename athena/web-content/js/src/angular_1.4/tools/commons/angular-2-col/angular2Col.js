/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * 
 */

angular.module('angular_2_col', [ 'ngMaterial' ])
.directive('angular2Col',
		function() {
	return {
		template:'<md-content id="angular2colBody" layout="row" layout-wrap layout-fill></md-content>',
		controller : templatesControllerFunction,
		transclude : true,
		link: function(scope, element, attrs, ctrl, transclude) {
			var id="a2c";
			if(attrs.id){
				id=attrs.id;
			}
			transclude(function(clone) {
				angular.element(element[0].querySelector('#angular2colBody')).append(clone);
			});
			//append the id in every child
			angular.element(element[0].querySelector('left-col')).attr("id","left-col-"+id);
			angular.element(element[0].querySelector('right-col')).attr("id","right-col-"+id);
			angular.element(element[0].querySelector('resizer')).attr("id","resizer-"+id);
		}
	}
})
.directive('leftCol',
		function($compile) {
	return {
		template:'<md-content id="leftColBody" style="height: 100%;"></md-content>',
		controller : leftColControllerFunction,
		transclude : true,
		link: function(scope, element, attrs, ctrl, transclude) {
			transclude(function(clone) {
				angular.element(element[0].querySelector("#leftColBody")).append(clone);
			});
			element.css("width", "30%");
			element.after("<resizer style='background-color: #BFBCBC; width: 4px; cursor: e-resize;'> </resizer>")
			$compile(element[0].parentElement.querySelector('resizer'))(scope);

		}
	}
})
.directive('rightCol',
		function() {
	return {
		template:'<md-content id="rightColBody" style="height: 100%;"></md-content>',
		controller : rightColControllerFunction,
		transclude : true,
		link: function(scope, element, attrs, ctrl, transclude) {
			transclude(function(clone) {
				angular.element(element[0].querySelector('#rightColBody')).append(clone);
			});
			element.addClass("flex");
		},
	}
})
.directive('resizer',
		function($document) {
	return {
		link : function(scope, element, attrs) {
			element.on('mousedown', function(event) {
				event.preventDefault();
				$document.on('mousemove', mousemove);
				$document.on('mouseup', mouseup);
			});
			function mousemove(event) {
				// Handle vertical resizer
				var x = event.pageX;
				var offs=element[0].parentElement.offsetParent.offsetLeft;
				// remove var width
				x-=4;
				x-=offs;

				if(element[0].parentElement.children[0].clientWidth!=0 && element[0].parentElement.children[1].clientWidth!=0 && element[0].parentElement.children[2].clientWidth!=0){
					var maxw=(element[0].parentElement.clientWidth-50);
					if(x>maxw){
						x=maxw;
					}
				}
				var minw=5;
				if(x<minw){
					x=minw;
				}
				angular.element(element[0].parentElement.querySelector('left-col')).css("width", x + 'px');
			}
			function mouseup() {
				$document.unbind('mousemove', mousemove);
				$document.unbind('mouseup', mouseup);
			}
		}
	}
});

function templatesControllerFunction($scope) {
}
function leftColControllerFunction($scope) {
}
function rightColControllerFunction($scope) {
}
