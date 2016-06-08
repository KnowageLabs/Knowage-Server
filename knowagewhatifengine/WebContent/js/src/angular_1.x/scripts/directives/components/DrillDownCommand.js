angular.module('drill_down_command_directive',[])
	.directive('drilldown' ,function () {
	    return {
	        restrict: 'E',
	        link: function (scope, element, attrs) {
	        	
	        	
	        	 
	          var onClick = function($event){
	        	  $event.preventDefault()
	        	  $event.stopPropagation();
	        	  scope.drillDown(attrs.axis,attrs.memberordinal,attrs.position,attrs.uniquename,attrs.positionuniquename);
	        	 
	        	  console.log('click!!!!!!!!!!!!!!');
	        	  element.unbind('click',onClick); 
	          }
	          
	          element.bind('click',onClick);
	          
	          
	          
	          element.bind('$destroy', function () {
	                
	        	  
		        	 console.log(element);
			         element.unbind('click',onClick); 
			          
		        	
		               
		            });
	        },
	        template : "<img src='../img/plus.gif'>"//<img src='../img/plus.gif'></img>
	    };
	});
