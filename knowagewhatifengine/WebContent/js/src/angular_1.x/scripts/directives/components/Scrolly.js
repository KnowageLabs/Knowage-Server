angular.module('scrolly_directive',[])
	.directive('scrolly',['$window', function ($window) {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	        	var ready = false;
	            var raw = element[0];
	            console.log('loading directive');
	            scope.tableHeight = raw.offsetHeight;
	            console.log(scope.tableHeight);
	            scope.scrollTo = function(posX,posY) {
		    		console.log(scope.modelConfig);
	        		raw.scrollTop = posX*100;
	        		raw.scrollLeft = posY*100;
	        	 
	        	};
	        	
	        	scope.resize = function(){
	        		 scope.tableHeight = raw.offsetHeight;
	            	 var corner = document.getElementsByClassName("x-pivot-corner")[0];
	            	console.log(corner+' corner');
	            	if(corner){
	            		 scope.modelConfig.rowsSet = Math.round(raw.offsetHeight/43)-Math.round(corner.offsetHeight/43)-1;
	            		 console.log(corner.offsetHeight/43);
	            	}else{
	            		scope.modelConfig.rowsSet =Math.round(raw.offsetHeight/43)-2;
	            	}
	            	
	            	 console.log(scope.modelConfig.rowsSet);
	            	 if(scope.modelConfig.rowsSet<1){
	            		 scope.modelConfig.rowsSet = 1;
	            	 }
	            	 scope.sendModelConfig(scope.modelConfig);
	        	}
	           
	        	
	               
	            angular.element($window).bind('resize', function(){
	                
	            	scope.resize();
	              });
	                
	               
	            
	                
	            element.bind('scroll', function () {
	                
	                console.log(raw.scrollTop+"gore");
	                console.log(raw.scrollLeft+"levo");
	                
	                var startRow = Math.round((raw.scrollTop)/100);
	                var startColumn =  Math.round(raw.scrollLeft/100);
	               if(scope.modelConfig.startRow!=startRow){
	            	   scope.modelConfig.startRow = startRow;
	            	  
	           	    	scope.$apply();
	           	    
	           	    scope.sendModelConfig(scope.modelConfig);
	               }
	               if(scope.modelConfig.startColumn!=startColumn){
	            	
	            	   scope.modelConfig.startColumn = startColumn;
	           	    	scope.$apply();
	           	    
	           	    scope.sendModelConfig(scope.modelConfig);
	               }
	   
	    
	    
	               
	            });
	        }
	    };
	}]);
