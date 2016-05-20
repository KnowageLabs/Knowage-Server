angular.module('scrolly_directive',[])
	.directive('scrolly',['$window','$interval','$sce', function ($window,$interval,$sce) {
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
	        		
	        		scope.tableHeight = raw.offsetHeight;
		            scope.tableWeight = raw.offsetWidth;
	            	 var table = document.getElementsByClassName("x-pivot-table")[0];
	            	 if(table){
	            		 var thead = table.getElementsByTagName("thead")[0];
		            	 var bodyRows = table.getElementsByTagName("tbody")[0].children;
		            	 var bodyColumns = bodyRows[0].children;
	            	 
	            	 
	            	 
	            	 var newRowSet = scope.modelConfig.rowsSet;
	            	 var newColumnSet = scope.modelConfig.columnSet; 
	            	
	            	
	            	//Setting new number of rows if table height is bigger than div 
	            	if(table.offsetHeight>raw.offsetHeight-25){
	            		
	            		
	            		
	            		var newTableHeight = thead.offsetHeight;
	            		
	            		for ( newRowSet = 0; newRowSet < bodyRows.length; newRowSet++) {
							
	            			
	            			
	            			if((newTableHeight+bodyRows[newRowSet].offsetHeight)>(raw.parentElement.offsetHeight-25)){
	            					            				
	            				break;
	            			}else{
	            				newTableHeight = newTableHeight+bodyRows[newRowSet].offsetHeight;
	            			}
	            				
						}
	            		
	            	
	            	}	
	            	if(table.offsetHeight<raw.offsetHeight-70&&
	            			scope.modelConfig.rowCount>newRowSet){
	            		console.log('table height is smaller than parent div and rowCount is greater than rowSet');
	            		
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
	            	
	            		if(table.offsetWidth>raw.parentElement.offsetWidth-25){
	            			
	            			
		            		var ajSize = 0;
		            		var headerCount = 0;
		            		
		            		for ( newColumnSet = 0; newColumnSet < bodyColumns.length; newColumnSet++) {
								
	
		            			if(bodyColumns[newColumnSet].nodeName==='TH'){
		            				headerCount++;
		            			}
		            			if((ajSize+bodyColumns[newColumnSet].offsetWidth)>(raw.parentElement.offsetWidth-25)){
		            					            				
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
			            		 console.log("sending modelconfig from resize!!!!!!!!!!!!");
		            		 }
		            		 
		            	 }
		            	 
	            	 
	        	} 
	        	}
	           
	        	scope.interval = $interval(
	        			function(){
	        				if(scope.ready){
	        					scope.resize();
	        				}
	        				
	        				scope.scroll();
	        				
	        			
	        			},100);
	               
	        	scope.scroll = function () {
	                
	              
	        		
	                var startRow = Math.round((raw.scrollTop)/100);
	                var startColumn =  Math.round(raw.scrollLeft/100);
	                if(scope.modelConfig){
	                	
	                
	               
	               if(scope.modelConfig.startRow!=startRow){
	            	   if(scope.modelConfig.suppressEmpty){
	            		   if(startRow<scope.modelConfig.startRow){
	            			   scope.modelConfig.startRow = -startRow;
	            		   }else{
	            			   scope.modelConfig.startRow = startRow;
	            		   }
	            	   }else{
	            		   scope.modelConfig.startRow = startRow;
	            	   }
	            	   
	            	   
	            	   scope.showLoadingMask = false;
	           	    	
	           	  //  if(scope.tableSubsets[scope.modelConfig.startRow]!=undefined){
	           	  //  	scope.table = $sce.trustAsHtml(scope.tableSubsets[scope.modelConfig.startRow]);
	           	 //   }else{
	           	    	scope.sendModelConfig(scope.modelConfig);
	           	//    }
	           	    
	               }
	               if(scope.modelConfig.startColumn!=startColumn){
	            	
	            	   scope.modelConfig.startColumn = startColumn;
	           	    	
	            	   scope.showLoadingMask = false;
	           	    scope.sendModelConfig(scope.modelConfig);
	               }
	   
	    
	        	}
	               
	            }
	                
	          
	        }
	    };
	}]);
