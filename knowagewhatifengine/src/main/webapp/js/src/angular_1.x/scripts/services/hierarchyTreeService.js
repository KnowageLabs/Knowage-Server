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
	angular.module('olap.services').service('hierarchyTreeService',function(treeIteratorService,FiltersService){


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

		function slicerMembersVisitor () {
			this.slicerMembers = [];
			this.visit = function(element){
				if(element.isSlicer === true){
					this.slicerMembers.push(element)
				}
			}
		}

		function propertyManagerVisitor(propertyName,value){
			this[propertyName] = value;

			this.visit = function(element){

				element[propertyName] = this.value;

			}
		}

		function slicerSetterVisitor(selectedHierarchyUniqueName){
			this.visit = function(element){
				element.isSlicer = FiltersService.isSlicer(selectedHierarchyUniqueName,element.uniqueName)
			}
		}

		var setIsSlicer = function(tree,selectedHierarchyUniqueName){

			var visitor = new slicerSetterVisitor(selectedHierarchyUniqueName)

			treeIteratorService
			.accept(visitor)
			.iterate(tree,'children')

		}

		var getSlicerMembers = function(tree){

			var visitor = new slicerMembersVisitor()

			var treeCopy = angular.copy(tree);
			treeIteratorService
			.accept(visitor)
			.iterate(treeCopy,'children')


			return visitor.slicerMembers;
		}

		var getSlicerMemberUniqueNames = function(tree){
			var  members = getSlicerMembers(tree)
			var slicerMemberUniqueNames = [];
			for(var i in members){
				slicerMemberUniqueNames.push(members[i].uniqueName)
			}

			return slicerMemberUniqueNames;
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
			.accept(new propertyManagerVisitor('visible',visible))
			.iterate(tree,'children')
		}

		var setCollapsedForAll  = function(tree,collapsed){

			treeIteratorService
			.accept(new propertyManagerVisitor('collapsed',collapsed))
			.iterate(tree,'children')
		}

		var isAnyVisible = function(tree){
			var visibleMembers = getVisibleMembers(tree)
			return Array.isArray(visibleMembers) &&  visibleMembers.length > 0;
		}


		return {

			getVisibleMembers:getVisibleMembers,
			getSlicerMemberUniqueNames:getSlicerMemberUniqueNames,
			getSlicerMembers : getSlicerMembers,
			setIsSlicer:setIsSlicer,
			setVisibilityForAll:setVisibilityForAll,
			setCollapsedForAll:setCollapsedForAll,
			isAnyVisible:isAnyVisible

		}

	})

})()
