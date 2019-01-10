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

 module.service('tagsHandlerService',tagsHandlerService)

	 function tagsHandlerService(){

	 var tags = [];
	 var tagsForDeleting = [];
	 var tagsForSending = {};
	 var tagsForFiltering = [];

		 return{
			 setTags : function(sentTags){
				 tags = sentTags;
			 	},

			 isTagExisting : function(tag,dsTags){
					return dsTags.indexOf(tag) != -1;
				},

			 removeTagFromList : function(tag,dsTags){
				 dsTags.splice(dsTags.indexOf(tag),1);
				},

			 setTagForDeleting : function(tag){
					tagsForDeleting.push(tag);
				},

			 addNewTagToList : function(tag,dsTags,allTags){
				 	dsTags.push(tag);
				 	allTags.push(tag)
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
						 tagsToAdd: tags,
						 tagsToRemove: tagsForDeleting
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

			 getFilteredTagIds : function(){
				 var tagIds = [];
				 for(var i = 0;i<tagsForFiltering.length; i++){
					 tagIds.push(tagsForFiltering[i].tagId)
				 }
				 return tagIds;
			 }




		 }

	 }
 })();