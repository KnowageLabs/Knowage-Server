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

(function() {
	
	var app = angular.module("olap.services");
	app.service("indexChangingService", function() {
		
		var _this = this;
		
		_this.hasPrevious = function(index) {
			return index > 0;
		};
		
		_this.hasNext = function(index, arrayLength, limit) {
			return index < arrayLength - limit;
		};
		
		var isLeftAndHasPrevious = function(direction, index) {
			return direction === 'left' && _this.hasPrevious(index)
		};
		
		var isRightAndHasNext = function(direction, index, array, limit) {
			return direction === 'right' && _this.hasNext(index, array.length, limit);
		};
		
		_this.changeIndexValue = function(direction, index, array, limit) {
			if(isLeftAndHasPrevious(direction, index)) {
				index--;
					
			} else if(isRightAndHasNext(direction, index, array, limit)) {
				 index++;
			}
			
			return index;
			
		};
		
	});
	
}());