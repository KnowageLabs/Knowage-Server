angular.module('cell_directive',[])
	.directive('cell', function () {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	            
	            scope.selectedCell = {};
	            var cell = {};
	            cell.id = attrs.id;
	            cell.measurename = attrs.measurename;
	            cell.ordinal = attrs.ordinal;
	            
	            
	            
	            var ondbClick = function(){
	            	scope.makeEditable(cell.id,cell.measurename);
	            }
	            
	            var onClick =function(){
	            		
	            	scope.selectedCell = cell;
			            
		        	 console.log(cell);
		        	 console.log(scope.selectedCell);
	            		
	            }
	           
	         element.bind('click', onClick);
	         
	         element.bind('dblclick', ondbClick);
	         
	         element.on('$destroy', function () {
	                
	        	 	
	        	 cell =null;
	        	 
		         element.unbind('dblclick',ondbClick);  
		         element.unbind('click',onClick); 
		         element.detach();
	        	 console.log("lllllllllllllllllllllllllllllllll");
	               
	            });
	        }
	    };
	});
