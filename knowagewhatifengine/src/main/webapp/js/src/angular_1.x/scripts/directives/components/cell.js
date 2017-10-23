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

angular.module('cell_directive',[])
	.directive('cell', function (sbiModule_restServices,sbiModule_messaging) {
	    return {
	        restrict: 'A',
	        link: function (scope, element, attrs) {
	            
	            scope.selectedCell = {};
	            var cell = {};
	            cell.id = attrs.id;
	            cell.measurename = attrs.measurename;
	            cell.ordinal = attrs.ordinal;
	            
	            
	            
	            var ondbClick = function(){
	            	if(scope.modelConfig.whatIfScenario){
	            		scope.makeEditable(cell.id,cell.measurename);
	            		
	            	}
	            }
	            
	           
	            
	            
	            
	            var onClick =function(){
	            	
	            	if(scope.selectedCell.id===cell.id){
	            		scope.selectedCell = {};
	            	}else{
	            		scope.selectedCell = cell;
	            		
	            	}
	
	            }
	            
	           
	           
	         element.bind('click', onClick);
	         
	         element.bind('dblclick', ondbClick);
	         
	         element.on('$destroy', function () {
	                
	        	 	
	        	 cell =null;
	        	 
		         element.unbind('dblclick',ondbClick);  
		         element.unbind('click',onClick); 
		         element.detach();
	        	 
	               
	            });
	        }
	    };
	});
