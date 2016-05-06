angular.module('scrolly_directive',[])
	.directive('scrolly',['$window','$interval', function ($window,$interval) {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	        	var ready = false;
	        	
	            var raw = element[0];
	            scope.$watch('tableHeight', function() {
	                
	            });
	           
	            scope.tableHeight = raw.offsetHeight;
	            scope.tableWeight = raw.offsetWidth;
	            
	            scope.scrollTo = function(posX,posY) {
	        		raw.scrollTop = posX*100;
	        		raw.scrollLeft = posY*100;
	        	 
	        	};
	        	
	        	
	        	scope.resize = function(){
	        		
	        		
	            	 var table = document.getElementsByClassName("x-pivot-table")[0];
	            	 var thead = table.getElementsByTagName("thead")[0];
	            	 var bodyRows = table.getElementsByTagName("tbody")[0].children;
	            	 var bodyColumns = bodyRows[0].children;
	            	 
	            	 var newRowSet = scope.modelConfig.rowsSet;
	            	 var newColumnSet = scope.modelConfig.columnSet; 
	            	
	            	
	            	//Setting new number of rows if table height is bigger than div 
	            	if(table.offsetHeight>raw.offsetHeight-35){
	            		
	            		console.log('table height is larger that parent div');
	            		newRowSet = 0;
	            		var newTableHeight = thead.offsetHeight;
	            		
	            		while(bodyRows[newRowSet]!=undefined&&(newTableHeight+bodyRows[newRowSet].offsetHeight)<(raw.offsetHeight-35)){
	            			
	            			newTableHeight = newTableHeight+bodyRows[newRowSet].offsetHeight;
	            			newRowSet++;
	            		}
	            		
	            	}	
	            	if(table.offsetHeight<raw.offsetHeight-70&&
	            			scope.modelConfig.rowCount>newRowSet){
	            		console.log('table height is smaller than parent div and rowCount is greater than rowSet');
	            		
	            			newRowSet = 50;
	            			
	            		
	            	}	
	            	
	            	if(table.offsetWidth<raw.offsetWidth-70&&
	            			scope.modelConfig.columnCount>newColumnSet-1){
	            		
	            		newColumnSet= 50;
	            			
	            		
	            	}	
	            	
	            		if(table.offsetWidth>raw.parentElement.offsetWidth-70){
	            			
	            			newColumnSet = 0;
		            		var ajSize = 0;
		            		var headerCount = 0;
		            		while(bodyColumns[newColumnSet]!=undefined&&(ajSize+bodyColumns[newColumnSet].offsetWidth)<(raw.parentElement.offsetWidth-70)){
		            			ajSize = ajSize+bodyColumns[newColumnSet].offsetWidth;
		            			newColumnSet++;
		            			if(bodyColumns[newColumnSet].nodeName==='TH')
		            				headerCount++;
		            		}
		            		newColumnSet = newColumnSet-headerCount;
	        		}
	            		
		            	
		            	
		            	 if(newRowSet<1){
		            		
		            		 newRowSet =1;
		            	 }
		            	 if(newColumnSet<1){
		            		
		            		 newColumnSet =1;
		            	 }
		            	 
		            	 if(scope.modelConfig.rowsSet!=newRowSet||scope.modelConfig.columnSet!=newColumnSet){
		            		 if(scope.ready){
		            			 scope.modelConfig.rowsSet=newRowSet;
			            		 scope.modelConfig.columnSet=newColumnSet;
			            		 scope.sendModelConfig(scope.modelConfig);
			            		 console.log("sending modelconfig from resize!!!!!!!!!!!!");
		            		 }
		            		 
		            	 }
		            	 
	            	 
	            	 
	        	}
	           
	        	scope.interval = $interval(
	        			function(){
	        				
	        				scope.resize();
	        				scope.scroll();
	        				
	        			
	        			},100);
	               
	        	scope.scroll = function () {
	                
	              
	                
	                var startRow = Math.round((raw.scrollTop)/100);
	                var startColumn =  Math.round(raw.scrollLeft/100);
	               if(scope.modelConfig.startRow!=startRow){
	            	   scope.modelConfig.startRow = startRow;
	            	  
	           	    	
	           	    
	           	    scope.sendModelConfig(scope.modelConfig);
	               }
	               if(scope.modelConfig.startColumn!=startColumn){
	            	
	            	   scope.modelConfig.startColumn = startColumn;
	           	    	
	           	   
	           	    scope.sendModelConfig(scope.modelConfig);
	               }
	   
	    
	    
	               
	            }
	                
	          
	        }
	    };
	}]);
