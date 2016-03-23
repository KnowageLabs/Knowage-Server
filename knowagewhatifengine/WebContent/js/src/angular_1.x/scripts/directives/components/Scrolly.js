angular.module('scrolly_directive',[])
	.directive('scrolly', function () {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	        	var ready = false;
	            var raw = element[0];
	            console.log('loading directive');
	                
	            element.bind('scroll', function () {
	                
	                console.log(raw.scrollTop+"gore");
	                console.log(raw.scrollLeft+"levo");
	    scope.modelConfig.startRow = Math.round(raw.scrollTop/100);
	    scope.modelConfig.startColumn = Math.round(raw.scrollLeft/100);
	    scope.$apply();
	    console.log(scope.modelConfig);
	    scope.sendModelConfig(scope.modelConfig);
	                
	               
	            });
	        }
	    };
	});
