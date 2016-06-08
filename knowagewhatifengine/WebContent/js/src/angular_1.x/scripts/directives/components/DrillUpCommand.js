angular.module('drill_up_command_directive',[])
	.directive('drillup', function () {
	    return {
	        restrict: 'E',
	        link: function (scope, element, attrs) {
	            
	          var onClick = function($event){
	        	  $event.stopPropagation();
	        	  scope.drillUp(attrs.axis,attrs.memberordinal,attrs.position,attrs.uniquename,attrs.positionuniquename);
	          }
	          
	          element.bind('click',onClick);
	          
	          element.bind('$destroy', function () {
	                
	        	 	
	        	 
			         element.unbind('click',onClick); 
			         element.detach();
		        	 console.log("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
		               
		            });
	        },
	        template : "<img src='../img/minus.gif'>"//<img src='../img/plus.gif'></img>
	    };
	});
