angular.module('member_directive',[])
	.directive('member', function (sbiModule_restServices,sbiModule_messaging,$mdDialog) {
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
			            scope.selectedMember.axisOrdinal = element[0].attributes['axisOrdinal'].value;
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
			            	if(scope.members.length>0&&scope.members[scope.members.length-1].level===scope.selectedMember.level&&scope.members[scope.members.length-1].parentMember===scope.selectedMember.parentMember){
			            		scope.members.push(angular.copy(scope.selectedMember));
				            	element[0].className = 'x-pivot-header-column-selected';
			            	}else if(scope.members.length===0){
			            		scope.members.push(angular.copy(scope.selectedMember));
				            	element[0].className = 'x-pivot-header-column-selected';
			            	}
			            	
			            	
			            }
			            
			            element[0].focus();
		        	 console.log(scope.selectedMember);
		        	 console.log(scope.members);
		        	 console.log($event.target+" event")
	        		
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
	                
	        	 	
	        	 	
	               
	            });
	        
	         
	        }
	    };
	});
