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
	angular.module('targetApp').service('multiConditionTemplateRetriver',function(parsingHelper,mappings){

		this.operator;
		this.fieldType;
		this.targetType;

		var parsingHelper = parsingHelper;
		var mappings = mappings;



		var withOperator = function(operator){
			this.operator = adapt(operator)
			return this;

		}

		var withFieldType = function(fieldType){
			this.fieldType = adapt(fieldType)
			return this;
		}

		var withTargetType = function(targetType){
			this.targetType = adapt(targetType)
			return this;
		}

		var adapt = function(value){
			if(value){
				return value.toLowerCase().replace(/\s/g, '');
			}
		}

		var getTemplate = function(){



			var combinations = getCombinations([this.operator,this.fieldType,this.targetType]);

			var currMaps = mappings.get();
			for(var i = combinations.length-1; i >= 0;i--){
				var template = parsingHelper.get(combinations[i], currMaps);
				if(isString(template)){
					return template;
				}
			}

			if(!isString(template)){
				return mappings.get()['default'];
			}

		}

		var createCombinations = function(array,out,start,end,index,r,result){

			if(index == r){
				result.push(angular.copy(out))
					return;
			}

			for(var i= start; i<=end && end-i+1 >= r-index; i++){
				out[index] = array[i]
				createCombinations(array,out,i+1,end,index+1,r,result)
			}


		}

		function isString (value) {
			return typeof value === 'string' || value instanceof String;
		}

		var getCombinations = function(categories){
			var combinations = []
			for(var i = 0;i<categories.length;i++){
				createCombinations(categories,[],0,categories.length-1,0,i+1,combinations)
			}

			return combinations;
		}



		return {

			withOperator:withOperator,
			withFieldType:withFieldType,
			withTargetType:withTargetType,
			getTemplate:getTemplate
		}
	})
})()
