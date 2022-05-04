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
	angular.module('advancedFiltersApp').service('treeService',function($injector){

		var childProperty = 'childNodes';


		var contains = function(tree,nodeToFind){
			var contains = false;
			traverseDF(tree,function(node){

				if(node === nodeToFind){
					contains = true
				};
			})

			return contains;
		}

		var find = function(tree,toFind){
			var equalNode;
			traverseDF(tree,function(node){

				if(angular.equals(node,toFind)){
					equalNode = node;
				};
			})

			return equalNode;
		}

		var add = function(tree,node,parent){
			nodeExistingCheck(tree,parent);
			parent[childProperty].unshift(node);
		}

		var move = function(tree,source,destination){

			nodeExistingCheck(tree,source);
			nodeExistingCheck(tree,destination);

			angular.copy(source,destination)

		}

		var swapNodes = function(node1,node2){
			var temp = angular.copy(node1)
			angular.copy(node2,node1)
			angular.copy(temp,node2)

		}

		var swapNodePropertyValues = function(node1,node2,properties){
			for(var i =0;i<properties.length;i++){
				var temp = node1[properties[i]];
				node1[properties[i]] =  node2[properties[i]];
				node2[properties[i]] = temp;

			}
		}



		var remove = function(tree,nodeToRemove){

			getSiblings(tree,nodeToRemove).splice(getNodeToRemoveIndex(tree,nodeToRemove),1);
		}

		var traverseDF = function(tree,callback){
			(function recurse(currentNode){

				callback(currentNode);
				for(var i = 0; i <currentNode[childProperty].length;i++){
					recurse(currentNode[childProperty][i]);
				}

			})(tree)
		}

		var replace = function(tree,expression,node){
			nodeExistingCheck(tree,node);
			angular.copy(expression,node)
		}

		var getParent = function(tree,child){
			var parent;

			nodeExistingCheck(tree,child)

			traverseDF(tree,function(node){

				if(findElementIndex(node[childProperty],child)>-1)parent = node;

			})
			//if(!parent)throw new Error('Parent does not exist.');

			return parent;

		}

		var findElementIndex = function(array,element){
			for(var i =0;i<array.length;i++){
				if(element===array[i]){
					return i;
				}
			}
		}

		var getNodeToRemoveIndex = function(tree,nodeToRemove){
			return findElementIndex(getSiblings(tree,nodeToRemove),nodeToRemove);
		}

		var getSiblings = function(tree,node){
			return getParent(tree,node)[childProperty];
		}

		var nodeExistingCheck = function(tree,node){

			if(!contains(tree,node)){

				throw new Error('Node does not exist.');
			}
		}

		var findByName = function(tree, filterName){
			var equalNode;
			traverseDF(tree,function(node){
			
				if(node.type == "NODE_CONST" && node.value == filterName) {
					equalNode = node;
				};
			})

			return equalNode;
		}

		function removeInPlace(expression, filterName) {
			var currentNodeInExpression = findByName(expression, filterName);
			var parentOfCurrentNode = getParent(expression, currentNodeInExpression);
			var parentOfParent = null;
			
			// Get PAR node for filter group exactly two elements
			try {
				parentOfParent = getParent(expression, parentOfCurrentNode);
			} catch(err) {}
			
			try {
				var siblings = getSiblings(expression, currentNodeInExpression);

				var indexOfCurrentNode = siblings.indexOf(currentNodeInExpression);
				siblings.splice(indexOfCurrentNode, 1)
				
				if (siblings.length == 1 && parentOfParent && parentOfParent.value == "PAR") {
					try {
						parentOfCurrentNode = parentOfParent;
					} catch(err2) {}
	
				}
				
				// If root nodes doesn't have children
				if (expression.childNodes.length == 0) {
					angular.copy({}, expression);
				} else if (parentOfParent != null) {
					replace(expression, siblings[0], parentOfCurrentNode);
				}

			} catch(err) {
				// no siblings
				angular.copy({}, expression);
			}
		
		}

		return {
			childProperty : childProperty,
			contains : contains,
			find : find,
			add : add,
			move : move,
			swapNodes : swapNodes,
			swapNodePropertyValues : swapNodePropertyValues,
			replace : replace,
			remove : remove,
			traverseDF : traverseDF,
			getParent : getParent,
			findByName : findByName,
			removeInPlace : removeInPlace
		}
	})
})()
