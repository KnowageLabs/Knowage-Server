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
	angular.module('olap.services').service('treeIteratorService',function(){
		var visitors = [];


		var emptyVisitor = function(){
			visitors.length = 0;
		};

		var isLeaf = function(element,childrenPropertyName){
			return element[childrenPropertyName] === undefined
		}

		var forEachVisitor = function(element){
			for(var i = 0;i<visitors.length;i++){
				visitors[i].visit(element);

			}
		};
		var accept = function(visitor){
			visitors.push(visitor);
			return this;

		};

		var treeIterate = function(tree,childrenPropertyName){

			for(var i = 0;i<tree.length;i++){

				var element = tree[i];

				forEachVisitor(element)

				if(!isLeaf(element,childrenPropertyName)){
					treeIterate(element[childrenPropertyName],childrenPropertyName)
				}
			}
		}
		var iterate = function(tree,childrenPropertyName){

			treeIterate(tree,childrenPropertyName);
			emptyVisitor();
		};

		return {
			accept:accept,
			iterate:iterate
		}

	})
})()
