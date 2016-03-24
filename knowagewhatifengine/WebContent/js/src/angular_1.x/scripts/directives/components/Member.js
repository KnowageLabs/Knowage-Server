angular.module('member_directive',[])
	.directive('member', function () {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	            
	           
	            for(var i =0;i< scope.members.length;i++){
	            	if(scope.members[i].uniqueName===element[0].attributes['uniqueName'].value){
	            		
	            		element[0].className = 'x-pivot-header-column-selected';
	            		break;
	            	}
	            	
	            }
	            
	           
	         element.bind('click', function ($event,toaster) {
	        	 
	        	
	        		 
	        		 	scope.selectedMember.uniqueName = element[0].attributes['uniqueName'].value;
			            scope.selectedMember.level = element[0].attributes['level'].value;
			            scope.selectedMember.parentMember = element[0].attributes['parentMember'].value;
			            
			            var contains = false;
			            for(var i =0;i< scope.members.length;i++){
			            	if(scope.members[i].uniqueName===scope.selectedMember.uniqueName){
			            		contains = true;
			            		scope.members.splice(i,1);
			            		element[0].className = 'x-pivot-header-column';
			            		break;
			            	}
			            	
			            }
			            
			            if(!contains){
			            	scope.members.push(angular.copy(scope.selectedMember));
			            	element[0].className = 'x-pivot-header-column-selected';
			            	
			            }
			            
			            element[0].focus();
		        	 console.log(scope.selectedMember);
		        	 console.log(scope.members);
		        	 console.log($event.target+" event")
	        		
	        
	                
	        	 	
	        	 	
	               
	            });
	        
	         
	        }
	    };
	});
