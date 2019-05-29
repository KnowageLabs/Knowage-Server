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
(function(){
	angular.module('advancedFiltersApp').directive('operand', function(advancedFilterAppBasePath) {
		var template;
		  return {
			  	scope:{
			  		node:'='
			  	},
			  	link:function(scope, element, attrs){
			  		 scope.getContentUrl = function() {
			  			 if(scope.node){
			  				if(scope.node.type!=='NODE_CONST' && scope.node.value!=='PAR'){
				  				return advancedFilterAppBasePath+ '/directives/operand/operand.html';
				  			 }
				  			if(scope.node.value==='PAR'){
				  				return advancedFilterAppBasePath+ '/directives/operand/operand2.html';
				  			 }
				  			if(scope.node.type==='NODE_CONST'){
				  				return advancedFilterAppBasePath+ '/directives/operand/operand3.html';
				  			 }
			  			 }


			           }

			  	},
			  	restrict:'E',
			    template: "<div ng-include='getContentUrl()'></div>",
			    controller:function($scope){

			    }
			  };
			});
})()
