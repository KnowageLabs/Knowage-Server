angular.module('scrolly_directive',[])
	.directive('scrolly',['$window','$interval','$sce', function ($window,$interval,$sce) {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	        	var ready = false;
	            var raw = element[0];
	            
	            var startRow = null;
	            var startColumn = null;
	            
	            var table = null;
        		var thead = null;
        		var bodyRows = [];
        		var bodyColumns = [];
        		var newRowSet = null;
        		var newColumnSet = null;
        		var newTableHeight =null;
        		var newTableWeight = null;
	            
	           
	            scope.tableHeight = raw.offsetHeight;
	            scope.tableWeight = raw.offsetWidth;
	            
	            scope.scrollTo = function(posX,posY) {
	        		raw.scrollTop = posX*100;
	        		raw.scrollLeft = posY*100;
	        	 
	        	};
	        	
	        	  
	        	
	        	
	        	scope.resize = function(){
	        		
	        		scope.tableHeight = raw.offsetHeight;
		            scope.tableWeight = raw.offsetWidth;
		            
	            	table = document.getElementsByClassName("pivot-table")[0];
	            	
	            	
	            	 if(table){
	            		 
	            		  thead = table.getElementsByTagName("thead")[0];
		            	  bodyRows = table.getElementsByTagName("tbody")[0].children;
		            	  bodyColumns = bodyRows[0].children;
	            	 
		            	  newRowSet = scope.modelConfig.rowsSet;
		            	  newColumnSet = scope.modelConfig.columnSet; 
	            	
	            	
		            	  //Setting new number of rows if table height is bigger than div 
		            	  if(table.offsetHeight>raw.offsetHeight-25){
	            		
		            		  newTableHeight = thead.offsetHeight;
		            		  newRowSet = 0;
		            		  
		            		  	for ( var i = 0; i < bodyRows.length; i++) {
							
		            		  		if((newTableHeight+bodyRows[newRowSet].offsetHeight)>(raw.parentElement.offsetHeight-25)){
	            					            				
		            		  			break;
		            		  		}else{
		            		  			
		            		  			newRowSet++;
		            		  			newTableHeight = newTableHeight+bodyRows[newRowSet].offsetHeight;
	            				
		            		  		}
	            				
		            		  	}
	            		
	            	
		            	  }
		            	  
		            	  if(table.offsetHeight<raw.offsetHeight-70&&
		            		scope.modelConfig.rowCount>newRowSet){
	            		
	            			newRowSet = 50;

		            	  }	
	            	
		            	  if(table.offsetWidth<raw.offsetWidth-50&&
	            			scope.modelConfig.columnCount>newColumnSet){
		            		  
		            		  if(bodyColumns[newColumnSet]){
	            			
		            			  if(table.offsetWidth+bodyColumns[newColumnSet].offsetWidth<raw.offsetWidth-50){
	            			
		            			newColumnSet = 50;
		            		
	            			
		            			  }
	            		
		            		  }
	            			
	            		
		            	  }	
	            	
	            	
	            		if(table.offsetWidth>raw.parentElement.offsetWidth-50){
	            			
		            		var ajSize = 0;
		            		var headerCount = 0;
		            		
		            		for ( newColumnSet = 0; newColumnSet < bodyColumns.length; newColumnSet++) {
								
	
		            			if(bodyColumns[newColumnSet].nodeName==='TH'){
		            				headerCount++;
		            			}
		            			
		            			if((ajSize+bodyColumns[newColumnSet].offsetWidth)>(raw.parentElement.offsetWidth-50)){
		            				           				
		            				break;
		            			}else{
		            				ajSize = ajSize+bodyColumns[newColumnSet].offsetWidth;
		            			}
		            				
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
			            		
		            	}
		            		 
		            }
		            	 
	            	 
	        	} 
	            	  //table = null;
	            	  thead = null;
	            	  bodyRows.length = 0;
	            	  bodyColumns.length = 0;
	            	  newRowSet = null;
	            	  newColumnSet = null;
	            	  newTableHeight =null;
	        	}
	           
	        	scope.interval = $interval(
	        			function(){
	        				
	        				scope.scroll();
	        				if(scope.ready){
	        					scope.resize();
	        				}
	        				
	        					
	        				
	        				
	        				//scope.isScrolling = false;
	        			
	        			},100);
	        	
	        	
	        	
	               
	        	scope.scroll = function () {
	        		
	        		
	                
	                
	                startRow = Math.round((raw.scrollTop)/100);
	                startColumn =  Math.round(raw.scrollLeft/100);
	                
	                
	                if(scope.modelConfig){
	                	
	                	if(scope.modelConfig.startRow!=startRow){
	                		
	                		if(table){
	        		   			table.remove();
	        		   		}
	           
	            		   scope.modelConfig.startRow = startRow;
	            		   scope.showLoadingMask = false;
	           	    	
	            		   	if(scope.tableSubsets!=null&&
	            		   		scope.tableSubsets[scope.modelConfig.startRow]!=undefined&&
	            		   		scope.tableSubsets[scope.modelConfig.startRow]!=null){
	            		   		
	            		   		
	            		   		scope.table = $sce.trustAsHtml(scope.tableSubsets[scope.modelConfig.startRow]);
	            		   		
	            		   	}else{
	            		   		
	            		   		scope.sendModelConfig(scope.modelConfig);
	            		   	}
	           	    
	                	}
	                	
	                	if(scope.modelConfig.startColumn!=startColumn){
	            	
	                		scope.modelConfig.startColumn = startColumn;
	                		scope.showLoadingMask = false;
	                		scope.sendModelConfig(scope.modelConfig);
	           	    
	                	}
	   
	    
	                }
	                
	                startRow = null;
	                startColumn =null;
	               
	            }
	                
	          
	        	
	        	
	        }
	    };
	}]);
