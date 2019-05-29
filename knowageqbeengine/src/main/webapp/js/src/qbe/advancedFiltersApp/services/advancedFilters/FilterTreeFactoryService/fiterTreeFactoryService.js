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
	angular.module('advancedFiltersApp').service('filterTreeFactoryService',function(){
		
		function group(expression){
			var group = {};
			group.type = "NODE_OP";
			group.value = "PAR";
			group.childNodes = [];
			group.childNodes.push(expression);
			
			return group;
		}
		
		function expression(leftOperant,operator,rightOperand){
			var expression = {}
			if(!leftOperant) throw new Error('leftOperant cannot be undefined.'); 
			if(!operator) throw new Error('operator cannot be undefined.'); 
			if(!rightOperand) throw new Error('rightOperand cannot be undefined.'); 
			expression.type = operator.type;
			expression.value = operator.value;
			expression.childNodes = [];
			expression.childNodes.push(leftOperant);
			expression.childNodes.push(rightOperand);
			
			return expression;
		}
		
		function operator(value){
			var operator = {};
			operator.type = "NODE_OP";
			operator.value = value;
			operator.childNodes = [];
			
			return operator;
		}
		
		function filter(name){
			var filter = {};
			filter.type = "NODE_CONST";
			filter.value = name;
			filter.childNodes = [];
			
			return filter;
		}

			
		return{
			
			filter : filter,
			operator : operator,
			expression : expression,
			group : group
			
		}
		
	})
})()
