angular.module('scrolly_directive',[])
	.directive('scrolly',['$window','$interval', function ($window,$interval) {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	        	var ready = false;
	        	
	            var raw = element[0];
	            scope.$watch('tableHeight', function() {
	                console.log("table height is "+scope.tableHeight);
	            });
	            console.log('visina'+raw.parentElement.offsetHeight);
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
	            	 var thead = document.getElementsByTagName("thead")[0];
	            	 var header = headers[headers.length - 1];
	            	
	            	 var newRowSet=scope.modelConfig.rowsSet;
	            	 var newColumnSet=scope.modelConfig.columnSet;
	            	 
	            	 var bodyRows = document.getElementsByTagName("tbody")[0].children;
	            	 var bodyColumns = document.getElementsByTagName("tbody")[0].children[0].children;
	            	 var rowSize = document.getElementsByTagName("tbody")[0].children[0].offsetHeight;//28;
	            	 
	            	 var colunmSize = 90;
	            	
	            	
	            	if(document.getElementsByClassName("x-pivot-table")[0].offsetHeight>raw.parentElement.offsetHeight-35){
	            		
	            		newRowSet = 0;
	            		var ajSize = thead.offsetHeight;
	            		while(bodyRows[newRowSet]!=undefined&&(ajSize+bodyRows[newRowSet].offsetHeight)<(raw.parentElement.offsetHeight-35)){
	            			ajSize = ajSize+bodyRows[newRowSet].offsetHeight;
	            			newRowSet++;
	            			
	            		}
	            		
	            	}	
	            	if(document.getElementsByClassName("x-pivot-table")[0].offsetHeight<raw.parentElement.offsetHeight-70&&
	            			scope.modelConfig.rowCount>newRowSet){
	            		
	            			newRowSet++;
	            			
	            		
	            	}	
	            	
	            	if(document.getElementsByClassName("x-pivot-table")[0].offsetWidth<raw.parentElement.offsetWidth-70&&
	            			scope.modelConfig.columnCount>newColumnSet-1){
	            		
	            		newColumnSet++;
	            			
	            		
	            	}	
	            	
	            		if(document.getElementsByClassName("x-pivot-table")[0].offsetWidth>raw.parentElement.offsetWidth-70){
	            			 var bodyColumns = document.getElementsByTagName("tbody")[0].children[0].children;
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
	        		}else{
	        			/*
	        				if(thead){
		            			newRowSet = Math.round((raw.offsetHeight-thead.offsetHeight-35)/rowSize)-1;
		            		
		            			if(corner){
		            				newColumnSet = Math.round((raw.offsetWidth-corner.offsetWidth)/colunmSize)-1;
		            			}
			            	}else{
			            		newRowSet =Math.round(raw.offsetHeight/rowSize)-2;
			            		newColumnSet =Math.round(raw.offsetWidth/colunmSize)-2;
			            	}
	        				
	        			*/
	        			
	        			
	        			
	        			
	        		}
	            	 
	            	
	            /*		 if(corner){
		            		 scope.modelConfig.rowsSet = Math.round((raw.offsetHeight-corner.offsetHeight)/rowSize)-1;
		            		 scope.modelConfig.columnSet = Math.round((raw.offsetWidth-corner.offsetWidth)/colunmSize)-1;
		            		 console.log(corner.offsetHeight/rowSize);
		            		 if(header){
			            		 scope.modelConfig.rowsSet =scope.modelConfig.rowsSet - Math.round(header.offsetHeight/rowSize);
			            		 console.log("header"+header.offsetHeight/rowSize);
			            	}*/
	            		
		            	
		            	
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
	           
	        	scope.interval = $interval(
	        			function(){
	        				
	        				scope.resize();
	        				
	        				
	        			
	        			},10);
	               
	                
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
