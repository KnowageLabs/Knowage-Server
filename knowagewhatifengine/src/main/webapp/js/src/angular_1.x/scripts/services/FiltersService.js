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
	app.service("FiltersService", function() {
		var filters = [];

		this.getFilters = function(){
			return filters;
		}

		this.setFilters = function(filtersArray){
			angular.copy(filtersArray,filters)
		}

		this.getFilter = function(filterName){
			for(var i in filters){
				if(filters[i].name == filterName){
					return filters[i];
				}
			}
		}

		this.getHierarchiesByFilterName = function(filterName){
			return this.getFilter(filterName).hierarchies;
		}

		this.getHierarchies = function(){
			var hierarchies = [];
			for(var i in filters){
				for(var j in filters[i].hierarchies){
					hierarchies.push(filters[i].hierarchies[j])
				}
			}
			return hierarchies;
		}

		this.getHierarchyByUniqueName = function(uniqueName){
			for(var i in this.getHierarchies()){
				if(this.getHierarchies()[i].uniqueName === uniqueName ){
					return this.getHierarchies()[i];
				}
			}
		}

		this.getSlicers = function(hierachyUniqueName){
			return (this.getHierarchyByUniqueName(hierachyUniqueName) ? this.getHierarchyByUniqueName(hierachyUniqueName).slicers : []);
		}

		this.isSlicer = function(hierachyUniqueName,memberUniqueName){
			var slicers = this.getSlicers(hierachyUniqueName)
			for(var i in slicers){
				if(slicers[i].uniqueName == memberUniqueName){
					return true;
				}
			}

			return false;
		}

		this.addSlicers = function(hierachyUniqueName,slicers){
			for(var i in slicers){
				if(!this.isSlicer(hierachyUniqueName,slicers[i].uniqueName)){
					this.getSlicers(hierachyUniqueName).push(slicers[i]);
				}
			}

		}

		this.setSlicers = function(hierachyUniqueName,slicers){
			angular.copy(slicers,this.getSlicers(hierachyUniqueName))

		}


		this.getSlicerUniqueNames = function(hierachyUniqueName){
			var slicers = this.getSlicers(hierachyUniqueName);
			var slicerUniqueNames = [];
			for(var i in slicers){
				slicerUniqueNames.push(slicers[i].uniqueName)
			}

			return slicerUniqueNames;

		}

		this.getSlicerNames = function(hierachyUniqueName){
			var slicers = this.getSlicers(hierachyUniqueName);
			var slicerNames = [];
			for(var i in slicers){
				slicerNames.push(slicers[i].name)
			}

			return slicerNames;

		}
		this.getSlicerNamesString = function(hierachyUniqueName){
			return this.getSlicerNames(hierachyUniqueName).join(",")
		}


	});

}());