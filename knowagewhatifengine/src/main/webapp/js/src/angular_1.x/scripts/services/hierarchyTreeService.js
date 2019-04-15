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
	angular.module('olap.services').service('hierarchyTreeService',function(treeIteratorService){


		function visibleMembersVisitor () {
				this.visibleMembers = [];
				this.visit = function(element){
					if(element.visible === true){
						var elementCopy = angular.copy(element)
						delete elementCopy.collapsed
						delete elementCopy.children
						this.visibleMembers.push(elementCopy)
					}

				}
		}

		function visibilityManagerVisitor(root,visible){
			this.visible = visible;
			var rootElement = root;
			var isRoot = function(element){
				return rootElement === element;
			}
			this.visit = function(element){
				if(isRoot(element)){
					element.visible = true;
					return;
				}
				element.visible = this.visible;

			}
		}

		var getVisibleMembers = function(tree){
			var visitor = new visibleMembersVisitor()
			var treeCopy = angular.copy(tree);
			treeIteratorService
			.accept(visitor)
			.iterate(treeCopy,'children')

			return visitor.visibleMembers;

		}

		var setVisibilityForAll = function(tree,visible){

			treeIteratorService
			.accept(new visibilityManagerVisitor(tree[0],visible))
			.iterate(tree,'children')
		}

		var isRootElement = function(tree,element){
			if(tree[0]){
				return tree[0];
			}
		}

		return {
			getVisibleMembers:getVisibleMembers,
			setVisibilityForAll:setVisibilityForAll,

		}

	})

})()
