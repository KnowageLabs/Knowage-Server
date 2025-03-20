/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

angular.module('folder_view', ['ngMaterial'])
.directive('folderView', function() {
	return {
		templateUrl: currentScriptPath + 'folder-view.html',
		controller: folderViewControllerFunction,
		priority: 10,
		scope: {
			ngModel:"=",
			showGridView:"=?",
			selectedRow:"=?",
			tableSpeedMenuOption:"=?",
			selectedFolder:"=?",
			// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			searchingOrganizer: "=?",
			selectFolderAction:"&",
			deleteFolderAction:"&"
		},
		link: function (scope, elem, attrs) { 
			elem.css("position","relative")
			 if(!attrs.tableSpeedMenuOption){
				 scope.tableSpeedMenuOption=[];
			 }
		}
	}
});

function folderViewControllerFunction($scope,sbiModule_translate,sbiModule_user){
	
	$scope.showOrganizeFolder = (sbiModule_user.functionalities.indexOf("SelfServiceFolderManagement")>-1)? true:false;
	
	$scope.clickFolder=function(item){
		
		 $scope.selectFolderAction({folder: item});
	}
	
	$scope.translate=sbiModule_translate;
}
})();