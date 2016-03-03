angular.module('scrolly_directive',[])
	.directive('scrolly', function () {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	            var raw = element[0];
	            console.log('loading directive');
	                
	            element.bind('scroll', function () {
	                
	                console.log(raw.scrollTop );
	    var pos = Math.round(raw.scrollTop/100);
	    scope.startFrom(pos);
	                
	               
	            });
	        }
	    };
	});
