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
	angular.module('selectedEntitiesRelationshipsModule').service('selectedEntitiesRelationshipsService',function(){

		var objectToArray = function(obj){
			var array = [];
			for (var key in obj) {
			    array.push(obj[key])
			}

			return array;
		}

		var contains = function(array,object,arrayObjProperty,objProperty){
			for(var i = 0;i<array.length;i++){
				if(array[i][arrayObjProperty] === object[objProperty]){
					return true;
				}
			}

			return false;
		}

		var entityContainsField = function(entity,field){
			var entityFields = entity.children

			return contains(entityFields,field,'id','id');
		}



		var isSelectedEntityRelationship = function(relationship,selectedEntites){
			return contains(selectedEntites,relationship,'id','targetEntity')
		}



		var getSelectedEntites = function(entityUniqueNames,entities){
			var selectedEntities = {};
			for(var i = 0;i<entityUniqueNames.length;i++){
				for(var j = 0;j<entities.length;j++){
					if(entityUniqueNames[i] === entities[j].id){
						selectedEntities[entities[j].id] = entities[j];
					}
				}
			}

			return objectToArray(selectedEntities);
		}

		var selectedEntityRelationships = function(selectedEntities){
			var selectedEntityRelationships = [];
			for(var i = 0;i<selectedEntities.length;i++){
				var relationships = selectedEntities[i].relation
				for(var j= 0;j<relationships.length;j++){
					if(isSelectedEntityRelationship(relationships[j],selectedEntities)){
						selectedEntityRelationships.push(relationships[j])
					}
				}
			}

			return selectedEntityRelationships;
		}

		return {

			getRelationships:function(entityUniqueNames,entites){
				var selectedEntities = getSelectedEntites(entityUniqueNames,entites)
				return selectedEntityRelationships(selectedEntities);
			}
		}
	})
})()
