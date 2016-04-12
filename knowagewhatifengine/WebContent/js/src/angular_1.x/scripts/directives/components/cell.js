angular.module('cell_directive',[])
	.directive('cell', function () {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	            
	            scope.selectedCell = {};
	           
	         element.bind('click', function () {
	                
	        	 	
	        	 	scope.selectedCell.id = element[0].attributes['id'].value;
		            scope.selectedCell.measureName = element[0].attributes['measureName'].value;
		            scope.selectedCell.ordinal = element[0].attributes['ordinal'].value;
		            
	        	 console.log(scope.selectedCell);
	               
	            });
	        }
	    };
	});
