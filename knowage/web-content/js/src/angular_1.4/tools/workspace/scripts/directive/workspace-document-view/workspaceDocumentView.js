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

angular.module('workspace_document_view', ['ngMaterial'])
.directive('workspaceDocumentView', function() {
	return {
		templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/scripts/directive/workspace-document-view/workspace-document-view.html',
		controller: workspaceDocumentViewControllerFunction,
		priority: 10,
		scope: {
			ngModel:"=",
			showGridView:"=?",
			showAddToOrganizer:"=?",
			selectedRow:"=?",
			tableSpeedMenuOption:"=?",
			selectedDocument:"=?",
			selectDocumentAction:"&",
			deleteDocumentAction:"&",
			executeDocumentAction:"&",
			cloneDocumentAction:"&",
			addToOrganizerAction:"&",
			addToFavoritesAction:"&",
			orderingDocumentCards:"=?",
			cloneEnabled:"=?",
			deleteEnabled:"=?"	
		},
		link: function (scope, elem, attrs) { 
			
			/**
			 * Changed from 'relative' to 'static' so the Grid/List toggling button could 
			 * work for the Analysis option as well.
			 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			elem.css("position","static")
			
			 if(!attrs.tableSpeedMenuOption){
				 scope.tableSpeedMenuOption=[];
			 }
		}
	}
});

function workspaceDocumentViewControllerFunction($scope,sbiModule_translate){
	
	$scope.translate = sbiModule_translate;
	
	$scope.clickDocument=function(item){		
		 $scope.selectDocumentAction({doc: item});
	}
}