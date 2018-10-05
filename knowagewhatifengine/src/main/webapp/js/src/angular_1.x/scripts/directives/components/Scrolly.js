/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

angular.module('scrolly_directive',['ngSanitize'])
	.directive('scrolly',['$window','$interval','$sce', function ($window,$interval,$sce) {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	        	var ready = false;
	            var container = element[0];
	            scope.tableHeight = container.offsetHeight;
	            scope.tableWeight = container.offsetWidth;
	            
	            function Table(className){
	            	
	            	//table class name
	            	this.tableCssClass =className;
	            	
            		this.getHtmlTable = function(){
            	
            			return document.getElementsByClassName(this.tableCssClass)[0];
            	
            		};
            
            		this.getHtmlTableRows = function(){
            	
            			return this.getHtmlTable().getElementsByTagName("tr");
            		};
            			
            		this.getHtmlTableRow = function(rowNumber){
            	
            			return this.getHtmlTableRows()[rowNumber];
            		};
            		
            		this.getHtmlTableHeaderRow = function(rowNumber){
            	
            			return this.getHtmlTableHeader().getElementsByTagName("tr")[rowNumber];
            		};
            		
            		this.getHtmlTableHeaderRows = function (){
            			
            			return this.getHtmlTableHeader().getElementsByTagName("tr");
            		}
            
            		this.getHtmlTableBodyRow = function(rowNumber){
            	
            			return this.getHtmlTableBody().getElementsByTagName("tr")[rowNumber];
            		};
            		
            		this.getHtmlTableBodyRows = function(){
                    	
            			return this.getHtmlTableBody().getElementsByTagName("tr");
            		};
            
            		this.getHtmlTableColumns = function (htmlRow){
            	
            			return htmlRow.children;
            	
            		};
            		
            		this.getHtmlTableBodyDataRowElements = function (rowNumber){
            			
            			var bodyDataRowElements = this.getHtmlTableBodyRow(rowNumber).getElementsByTagName("td");
                    	
            			return bodyDataRowElements;
            	
            		};
            		
            		this.getHtmlTableBodyDataElements = function (){
            			
            			var bodyDataElements;
            			
            			if(this.getHtmlTableBody()){
            				var bodyDataElements = this.getHtmlTableBody().getElementsByTagName("td");
            			}

            			return bodyDataElements;
            		};
            		
            		this.getHtmlTableBodyDataColumnElements = function(columnNumber){
            			
            			var bodyDataColumnElements = [];
            			
            			var bodyRows = this.getHtmlTableBodyRows();
            			
            			for (var i = 0; i < bodyRows.length; i++) {
            				var bodyColumnElement = this.getHtmlTableBodyDataRowElements(i)[columnNumber];
            				bodyDataColumnElements.push(bodyColumnElement);
						}
            			return bodyDataColumnElements;
            		}
            		
            		this.getHtmlTableHeader = function(){
            			
            			return this.getHtmlTable().getElementsByTagName("thead")[0];
            		};
            		
            		
            		this.getHtmlTableBody =function(){
            			
            			var htmlTable;
            			
            			if(this.getHtmlTable()){
            				
            				htmlTable = this.getHtmlTable().getElementsByTagName("tbody")[0];
            			}
            			
            			return htmlTable;
            		};
            		
            		this.getHtmlTableBodyHeaders = function(rowNumber){
            			var bodyRowHeaders = [];
            			var bodyRow;
            			bodyRowColumns = this.getHtmlTableBodyRow(rowNumber).children;
            			
            			for (var i = 0; i < bodyRowColumns.length; i++) {
							
            				if(bodyRowColumns[i].nodeName==='TH'){
            					bodyRowHeaders.push(bodyRowColumns[i]);
            				}
						}
            			
            			return bodyRowHeaders;
            		}
            		
            		
	            	
	            }
	            
	            function Margin(top,left,bottom,right){
	            	this.top = top;
	            	this.left = left;
	            	this.bottom = bottom;
	            	this.right = right;
	            	
	            }
	            
	        
	            
	            function Bounds(htmlElement,marginObj){
	            	this.rectObject = htmlElement.getBoundingClientRect();
	            	this.margin = marginObj;
	       
	            	
	            	this.isOutOfBounds = function(htmlElement){
	            		
	            		var compareRectObject = htmlElement.getBoundingClientRect();
	            		if(this.rectObject.top+this.margin.top>compareRectObject.top||
	            				this.rectObject.left+this.margin.left>compareRectObject.left||
	            				this.rectObject.bottom-this.margin.bottom<compareRectObject.bottom||
	            				this.rectObject.right-this.margin.right<compareRectObject.right
	            				){
	            			return true;
	            		}
	            		return false;
	            	}
	            }
	        	
	        	var getInBoundsTableRowsColumsSet = function (table,bounds){
	        		
	        		var newRowsColumnsSet = {};
	        		var tableBodyDataRowElements = table.getHtmlTableBodyDataRowElements(0);
	        		var tableBodyDataColumnElements = table.getHtmlTableBodyDataColumnElements(0);
	        		var newTableHeight = 0;
	        		
	        		for (var i = 0; i < tableBodyDataRowElements.length; i++) {
	        			
	        			if(bounds.isOutOfBounds(tableBodyDataRowElements[i])){
	        				newRowsColumnsSet.columnsSet = i;
	        				break;
	        			}
	        				
					}
	        		
	        		for (var i = 0; i < tableBodyDataColumnElements.length; i++) {
	        			
	        			if(bounds.isOutOfBounds(tableBodyDataColumnElements[i])){
	        				newRowsColumnsSet.rowsSet = i;
	        				break;
	        			}
	        				
					}

	        		return newRowsColumnsSet;
	        		
	        		
	        	}
	        	
	        	
	        	
	        	var setNewModelConfigValues = function (modelConfig,newRowsColumnsSet){
	        		var  isReadyToResize = false;
	        		if(newRowsColumnsSet.rowsSet&&modelConfig.rowsSet!=newRowsColumnsSet.rowsSet){
	        			modelConfig.rowsSet=newRowsColumnsSet.rowsSet;
	        			isReadyToResize = true;
	        		}
	        		
	        		if(newRowsColumnsSet.columnsSet&&modelConfig.columnSet!=newRowsColumnsSet.columnsSet){
	        			modelConfig.columnSet=newRowsColumnsSet.columnsSet;
	        			isReadyToResize = true;
	        		}
	        		
	        		return isReadyToResize;
	            	
	        	}
	        	
	        	var sendNewTableSize = function(modelConfig,isReadyToResize){
	        		if(isReadyToResize){
	        			scope.sendModelConfig(modelConfig);
	        		}
	        	}
	        	
	        	
	        	
	        	scope.clearTableCellSelection = function(tableCells){
	        		if(tableCells&&scope.selectedCell){
	        			for (var i = 0; i < tableCells.length; i++) {
							if(tableCells[i].id!==scope.selectedCell.id){
								tableCells[i].className = 'pivot-table th';
							}else{
								tableCells[i].className = 'pivot-table-selected';
							}
						}
	        		}
	        	}
	        	  
	        	scope.resize = function(){
	        		
	        		var SCROLL_WIDTH = 15;
	        		var COLUMNS_WIDTH = 200;
	        		var ROW_HEIGHT = 50;
	        		var isReadyToResize = false;
	        		var table = new Table("pivot-table");
	        		var bounds = new Bounds(container,new Margin(0,0,SCROLL_WIDTH,SCROLL_WIDTH));
	        		var resizeRowsBounds = new Bounds(container,new Margin(0,0,SCROLL_WIDTH+ROW_HEIGHT,SCROLL_WIDTH));
	        		var resizeColumnBounds = new Bounds(container,new Margin(0,0,SCROLL_WIDTH,SCROLL_WIDTH+COLUMNS_WIDTH));
	        		var newRowsColumnsSet = {};
	        		
	        		
	        		if(table.getHtmlTable()){
	        			
	        			if(bounds.isOutOfBounds(table.getHtmlTable())){
		        			
		        			newRowsColumnsSet = getInBoundsTableRowsColumsSet(table,bounds);
		        			
		        			
		        		}
	        			if(!resizeRowsBounds.isOutOfBounds(table.getHtmlTable())&&scope.modelConfig.rowCount>scope.modelConfig.rowsSet){
		        			
		        			newRowsColumnsSet.rowsSet = 50;
		        			
		        		}
	        			if(!resizeColumnBounds.isOutOfBounds(table.getHtmlTable())&&scope.modelConfig.columnCount>scope.modelConfig.columnSet){
		        			
		        			newRowsColumnsSet.columnsSet = 50;
		        			
		        			
		        		}
	
	        		}
	        		
        			isReadyToResize = setNewModelConfigValues(scope.modelConfig,newRowsColumnsSet);
	        		sendNewTableSize(scope.modelConfig,isReadyToResize);
	        	}
	           
	        	scope.interval = $interval(
	        			function(){
	        				scope.tableHeight = container.offsetHeight;
	        	            scope.tableWeight = container.offsetWidth;
	        				scope.clearTableCellSelection(new Table("pivot-table").getHtmlTableBodyDataElements());
	        				scope.scroll();
	        				if(scope.ready){
	        					scope.resize();
	        				}
	        			},100);
	        	
	        	scope.scroll = function () {

	                var startRow = Math.round((container.scrollTop)/100);
	                var startColumn =  Math.round(container.scrollLeft/100);
	                
	                if(scope.modelConfig){
	                	
	                	if(scope.modelConfig.startRow!=startRow){
	                		
	            		   scope.modelConfig.startRow = startRow;
	            		   scope.showLoadingMask = false;
	           	    	
	            		   	if(scope.buffer!=null&&
	            		   		scope.buffer[scope.modelConfig.startRow]!=undefined&&
	            		   		scope.buffer[scope.modelConfig.startRow]!=null){
	            		   		var obj ={};
	            		   		obj.text = 2;
	            		   		angular.copy($sce.trustAsHtml(scope.buffer[scope.modelConfig.startRow]),scope.table);
	            		   	}else{
	            		   		scope.sendModelConfig(scope.modelConfig);
	            		   	}           	    
	                	}
	                	
	                	if(scope.modelConfig.startColumn!=startColumn){
	            	
	                		scope.modelConfig.startColumn = startColumn;
	                		scope.showLoadingMask = false;
	                		scope.sendModelConfig(scope.modelConfig);
	                	}
	   
	                	if(scope.modelConfig.rowCount>scope.max+1 && scope.max<scope.modelConfig.startRow+2*scope.modelConfig.pageSize &&
	                			scope.modelConfig.startRow+2*scope.modelConfig.pageSize<scope.modelConfig.rowCount){
            		   		scope.sendModelConfig(scope.modelConfig, true);
	                	}
	                }
	                
	                startRow = null;
	                startColumn =null;
	               
	            }	
	        }
	    };
	}]);
