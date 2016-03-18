angular.module('cell_directive',[])
	.directive('cell', function () {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	            
	            scope.selectedCell= {};
	            selectedCell.id = element[0].attributes['id'].value;
	            selectedCell.measureName = element[0].attributes['measureName'].value;
	            selectedCell.ordinal = element[0].attributes['ordinal'].value;
	         element.bind('click', function () {
	                
	        	
	        	
	        	 console.log(cell);
	               
	            });
	        }
	    };
	});
