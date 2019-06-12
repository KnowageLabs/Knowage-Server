/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
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
	angular.module('advancedFiltersApp').service('selectedOperandService',function($injector){
		var advancedFiltersService = $injector.get('advancedFiltersService');
		var filterTreeService = $injector.get('filterTreeService');
		var selected = []

		var getSelected = function(){
			return selected;
		}

		var add = function(operand){
			selected.push(operand)
		}

		var contains = function(operand){
			for(var i =0;i<selected.length;i++){
				if(angular.equals(selected[i],operand)){
					return true;
				}
			}

			return false;
		}

		var remove = function(operand){
			for(var i =0;i<selected.length;i++){
				if(angular.equals(selected[i],operand)){
					selected.splice(i,1)
				}
			}
		}

		var addOrRemove = function(operand){
			if(contains(operand)){
				remove(operand)
			}else{
				add(operand)
			}

			console.log(selected)
		}

		var unSelectAll = function(){
			selected.length = 0;
		}

		var isSingleGroupSelected = function(){
			return selected.length ===1 && selected[0].value==='PAR'
		}

		var isSelectable = function(operand){
			return isEmpty()||(!isEmpty()&&isSameGroupAsSelected(operand))&&
			!allOtherGroupMembersAreSelected(operand)&&!allOtherSameLevelMembersAreSelected(operand)
		}

		var isEmpty = function(){
			return selected.length===0
		}

		var isSameGroupAsSelected = function(operand){
			return advancedFiltersService.isSameGroup(filterTreeService.filterTree,[selected[0],operand])
		}

		var getGroupOperands = function(groupOperand){
			return advancedFiltersService.getGroupOperands(advancedFiltersService.getGroup(filterTreeService.filterTree,groupOperand))
		}

		var getGroupOperandsCount = function(groupOperand){
			if(getGroupOperands(groupOperand)&&angular.isArray(getGroupOperands(groupOperand))){
				return getGroupOperands(groupOperand).length;
			}
			return 0;
		}

		var allOtherGroupMembersAreSelected = function(operand){
			return getGroupOperandsCount(operand) - getSelectedCount() === 1 && !contains(operand)
		}

		var allOtherSameLevelMembersAreSelected = function(operand){
			return getFirstLevelOperandsCount(operand) - getSelectedCount() === 1 && !contains(operand) && isFirstLevelOperand(operand)
		}

		var getFirstLevelOperands = function(){
			return advancedFiltersService.getFirstLevelOperands(filterTreeService.filterTree);
		}

		var isFirstLevelOperand = function(operand){
			for(var i =0;i<getFirstLevelOperands().length;i++){
				if(angular.equals(getFirstLevelOperands()[i],operand)){
					return true;
				}
			}

			return false;
		}



		var getFirstLevelOperandsCount = function(){
			if(getFirstLevelOperands()&&angular.isArray(getFirstLevelOperands())){
				return getFirstLevelOperands().length;
			}
			return 0;
		}

		var getSelectedCount = function(){
			return selected.length;
		}

		var isMovable = function(operand){
			return (isFirstLevelOperand(operand) && getFirstLevelOperandsCount(operand)>2)||getGroupOperandsCount(operand)>2
		}

		return {
			addOrRemove:addOrRemove,
			getSelected:getSelected,
			unSelectAll:unSelectAll,
			isSingleGroupSelected:isSingleGroupSelected,
			contains:contains,
			isSelectable:isSelectable,
			isMovable:isMovable,
			getSelectedCount:getSelectedCount,
			getGroupOperandsCount:getGroupOperandsCount,
			getFirstLevelOperandsCount:getFirstLevelOperandsCount
		}
	})
})()
