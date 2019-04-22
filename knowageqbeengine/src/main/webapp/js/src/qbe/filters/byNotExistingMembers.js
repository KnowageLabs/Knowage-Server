/**
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

(function() {
	
	angular.module('qbe.controller')
	  .filter('byNotExistingMembers', function($filter) {
	    return function(array,filterProperty,filterValues,mapping,comparationArray,comparationProperty) {
	    
	  
	    var expression = function(value, index, array){
	        var seachObj = {}
	        seachObj[comparationProperty] = adapter(value[filterProperty],mapping)
	        var hasFilterValue = contains(comparationArray,seachObj)
	        var shouldFilter = contains(filterValues,value[filterProperty])  
	        return !shouldFilter || hasFilterValue 
	    }
	    
	    var contains = function(array,expression){
	     if(array){
	       return $filter('filter')(array,expression,true).length>0;
	     }
	      return false;
	    };
	    
	    var adapter = function(value,mapping){
	       
	        if(mapping){
	           return mapping[value];
	        }
	        return value;
	    }
	    
	  
	   return $filter('filter')(array, expression)
	     
	     
	    };
	  })
})();