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

 module.service('tagsHandlerService',['$filter',function($filter){

	 var tags = [];
	 var tagsForDeleting = [];
	 var tagsForSending = {};
	 var tagsForFiltering = [];

	 var ownedDS = [];
	 var sharedDS = [];
	 var enterpriseDS = [];
	 var allDS = [];

	 var getOriginalDatasets = function(myDs,shared,enterprise,all){
			ownedDS = angular.copy(myDs);
			sharedDS = angular.copy(shared);
			enterpriseDS = angular.copy(enterprise);
			allDS = angular.copy(all);
		}
	 var restoreTags = function(tags){
		 for(var i = 0; i< tags.length; i++){
			 if(tags[i].isSelected)
				 tags[i].isSelected = false;
		 }
	 }

	 var restoreData = function(type){
				switch(type){
				case "myDataSet":
					return ownedDS;
					break;
				case "sharedDataSet":
					return sharedDS;
					break;
				case "enterpriseDataSet":
					return enterpriseDS;
					break;
				case "ckanDataSet":
					break;
				case "allDataSet":
					return allDS;
					break;
				}
			}

		 return{
			 setTags : function(sentTags){
				 tags = sentTags;
			 	},

			 isTagExisting : function(tag,dsTags){
				 tagId = tag.tagId;
					return ($filter('filter')(dsTags,{tagId:tagId})).length > 0
				},

			 removeTagFromList : function(tag,dsTags){
				 dsTags.splice(dsTags.indexOf(tag),1);
				},

			 setTagForDeleting : function(tag){
					tagsForDeleting.push(tag);
				},

			 addNewTagToList : function(tag,dsTags,allTags){
				 	dsTags.push(tag);
			    },

			 createNewTagObject : function(tagValue){
				 	var newTag = {};
				 	newTag.name = tagValue;
				 	return newTag;
			    },

			 trimComaSing : function(tag){
				return (tag.charAt(tag.length-1) == ',') ? tag.slice(0,-1): tag;
			 	},

			 isTagEmpty : function(tag){
				 return (tag == "" || tag == ',');
			 	},

			 prepareTagsForSending : function(dsVersionNumber,tags){
					 return {
						 versNum: dsVersionNumber,
						 tagsToAdd: tags
					 }
			 	},

			 isTagDeleted : function(){
				 return tagsForDeleting.length > 0;
			 },

			 isTagAdded : function(){
				 return tagsForAdding.length > 0;
			 },

			 toggleTag : function(tag){
				 if(tagsForFiltering.indexOf(tag) != -1){
					tagsForFiltering.splice(tagsForFiltering.indexOf(tag), 1);
				 }else
					tagsForFiltering.push(tag);
			 },

			 getAddedTags : function(){
				 return tagsForAdding;
			 },

			 getDeletedTags : function(){
				 return tagsForDeleting;
			 },

			 getFilteredTagIds : function(tags){
				 var tagIds = [];
				 for(var i = 0;i<tags.length; i++){
					 if(tags[i].isSelected)
					 tagIds.push(tags[i].tagId)
				 }
				 return tagIds;
			 },

			setOwnedDS : function(owned){
				ownedDS = angular.copy(owned);
			},
			getOwnedDS : function(){
				return ownedDS;
			},

			setSharedDS : function(shared){
				sharedDS = angular.copy(shared);
			},
			getSharedDS : function(){
				return sharedDS;
			},

			setEnterpriseDS : function(enterprise){
				enterpriseDS = angular.copy(enterprise);
			},
			getEnterpriseDS : function(){
				return enterpriseDS;
			},

			setAllDS : function(all){
				allDS = angular.copy(all);
			},
			getAllDS : function(){
				return allDS;
			},

			 getFilteredTags : function(tags){
				 var tempTags = [];
				 for(var i = 0;i<tags.length; i++){
					 if(tags[i].isSelected)
						 tempTags.push(tags[i])
				 }
				 return tempTags;
			 },

			 restore : function(tags,type){
				 restoreTags(tags);
				 return restoreData(type);
			 }
		 }

	 }])
 })();