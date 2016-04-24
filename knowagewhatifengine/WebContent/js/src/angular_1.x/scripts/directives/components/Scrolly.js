angular.module('scrolly_directive',[])
	.directive('scrolly',['$window', function ($window) {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	        	var ready = false;
	            var raw = element[0];
	            scope.$watch('tableHeight', function() {
	                console.log("table height is "+scope.tableHeight);
	            });
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
	        		 scope.tableWeight = raw.offsetWidth;
	            	 var corner = document.getElementsByClassName("x-pivot-corner")[0];
	            	 var headers = document.getElementsByClassName("pv-row-hdr");
	            	 var header = headers[headers.length - 1];
	            	 console.log(header);
	            	 var rowSize = 27;
	            	 var colunmSize = 80;
	            	console.log(corner+' corner');
	            	
	            	
	            	 
	            	
	            		 if(corner){
		            		 scope.modelConfig.rowsSet = Math.round((raw.offsetHeight-corner.offsetHeight)/rowSize)-1;
		            		 scope.modelConfig.columnSet = Math.round((raw.offsetWidth-corner.offsetWidth)/colunmSize)-1;
		            		 console.log(corner.offsetHeight/rowSize);
		            		 if(header){
			            		 scope.modelConfig.rowsSet =scope.modelConfig.rowsSet - Math.round(header.offsetHeight/rowSize);
			            		 console.log("header"+header.offsetHeight/rowSize);
			            	}
		            	}else{
		            		scope.modelConfig.rowsSet =Math.round(raw.offsetHeight/rowSize)-2;
		            		scope.modelConfig.columnSet =Math.round(raw.offsetWidth/colunmSize)-2;
		            	}
		            	
		            	 console.log(scope.modelConfig.rowsSet);
		            	 if(scope.modelConfig.columnSet<1){
		            		
		            		 scope.modelConfig.columnSet =1;
		            	 }
		            	 if(scope.modelConfig.columnSet<1){
		            		
		            		 scope.modelConfig.columnSet =1;
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
