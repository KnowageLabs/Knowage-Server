angular.module('member_directive',[])
	.directive('member', function (sbiModule_restServices,sbiModule_messaging,$mdDialog) {
	    return {
	        restrict: 'A',
	        
	        link: function (scope, $element, attrs) {
	           
	           
	            for(var i =0;i< scope.members.length;i++){
	            	if(scope.members[i].position===attrs.position&&scope.members[i].uniqueName===attrs.uniquename){
	            		
	            		$element.addClass('pivot-table-selected');
	            		break;
	            	}
	            	
	            }
	            
	            $element.bind('$destroy', function () {
	                
	        	 	
		        	 
		        	 $element.detach();
			        
			          
		        	 console.log("jjjjjjjjjjjjjjjjjjjjjjjjj");
		               
		            });
	            
	            var onClick = function($event,toaster){

	        	 	$event.preventDefault()
	        	 	$event.stopPropagation();
	        		 	scope.selectedMember.uniqueName = attrs.uniquename;
			            scope.selectedMember.level = attrs.level;
			            scope.selectedMember.parentMember = attrs.parentmember;
			            scope.selectedMember.axisOrdinal = attrs.axisordinal;
			            scope.selectedMember.hierarchyUniqueName = attrs.hierarchyuniquename;
			            scope.selectedMember.position = attrs.position;
			            var contains = false;
			            for(var i =0;i< scope.members.length;i++){
			            	if(scope.members[i].position===scope.selectedMember.position){
			            		contains = true;
			            		scope.members.splice(i,1);
			            		$element[0].className = 'pivot-table th';
			            		break;
			            	}
			            	
			            }
			            
			            if(!contains){
			            	if(scope.members.length>0&&scope.selectedMember.hierarchyUniqueName===scope.members[0].hierarchyUniqueName){
			            		scope.members.push(angular.copy(scope.selectedMember));
				            	$element[0].className = 'pivot-table-selected';
			            	}else if(scope.members.length===0){
			            		scope.members.push(angular.copy(scope.selectedMember));
				            	$element[0].className = 'pivot-table-selected';
			            	}
			            	
			            	
			            }
			            console.log(scope.members);
			            
			         
	        		
	        if(scope.modelConfig.showCompactProperties == true){
	        	
	        	sbiModule_restServices.promiseGet
	    		("1.0",'/member/properties/'+scope.selectedMember.uniqueName+'?SBI_EXECUTION_ID='+JSsbiExecutionID)
	    		.then(function(response) {
	    			console.log(response.data);
	    			scope.propertiesArray = response.data;
	    			$mdDialog
	    			.show({
	    				scope : scope,
	    				preserveScope : true,
	    				parent: angular.element(document.body),
	    				controllerAs : 'olapCtrl',
	    				templateUrl : '/knowagewhatifengine/html/template/main/toolbar/properties.html',
	    				//targetEvent : ev,
	    				clickOutsideToClose : false,
	    				hasBackdrop:false
	    			});
	    			
	    			
	    		}, function(response) {
	    			sbiModule_messaging.showErrorMessage("An error occured while getting properties for selected member", 'Error');
	    			
	    		});
	        	
	        }
	            }
	            
	           
	         $element.bind('click', onClick);
	        
	         
	        }
	    };
	});
