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
(function() {
	var module = angular.module('tagsModule');

		module.directive('tagDatasets',['sbiModule_config',function(sbiModule_config) {
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/tags/tagTemplate.html',
			controller: tagController,
			scope: {
				tags : '=',
				allTags : '='
			}
		};

	}]);



	function tagController($scope,tagsHandlerService,$mdConstant){
		$scope.searchText= "";
		$scope.selectedItem ;
		$scope.separators = [$mdConstant.KEY_CODE.COMMA,$mdConstant.KEY_CODE.ENTER]
		$scope.removeTag = function(tag,dsTags){
				if(tagsHandlerService.isTagExisting(tag,$scope.allTags)){
					tagsHandlerService.setTagForDeleting(tag,$scope.allTags);
			}
		};

		$scope.addNewTagToList = function(tag){
			if(!tag.tagId){
				return prepareTagForCreation(tag);
			}else {
				return tag;
			}
		};


		$scope.addOldTagToList = function(tag){
			$scope.tags.push(tag);
			resetTemporaryTag();
		}

		var resetTemporaryTag = function(){
				$scope.temporaryTag = "";
		}

		var prepareTagForCreation = function(tag){
			return tagsHandlerService.createNewTagObject(tag);
		}


		tagsHandlerService.setTags($scope.tags);
	}

})();