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
	
	var app = angular.module("olapModule");
	app.service("treeFilteringService", function() {
		
		var arrayObjects = [];
		
		this.testFun = function(arrayTree, name, value, children) {
			
			for(var i=0; i < arrayTree.length; i++) {
				
				if(arrayTree[i][name] != undefined && arrayTree[i][name] === value) {
					arrayObjects.push(arrayTree[i]);
				}
				
				if(arrayTree[i][children]) {
					this.testFun(arrayTree[i][children], name, value, children);
				}
				
			}
			
			console.log(arrayObjects);
			return arrayObjects;
			
		};
			
	});
	
}());