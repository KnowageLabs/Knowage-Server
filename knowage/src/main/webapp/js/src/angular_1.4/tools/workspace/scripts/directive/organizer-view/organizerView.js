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
	
angular.module('organizer_view', ['ngMaterial'])
.directive('organizerView', function() {
	return {
		templateUrl: currentScriptPath + 'organizer-view.html',
		controller: organizerViewControllerFunction,
		priority: 10,
		scope: {
			ngModel:"=",
			showGridView:"=?",
			selectedRow:"=?",
			tableSpeedMenuOption:"=?",
			selectedDocument:"=?",
			// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			searchingOrganizer: "=?",
			selectDocumentAction:"&",
			deleteDocumentAction:"&",
			executeDocumentAction:"&",
			moveDocumentAction:"&",
			orderingDocumentCards:"=?"
		},
		link: function (scope, elem, attrs) { 
			elem.css("position","relative")
			 if(!attrs.tableSpeedMenuOption){
				 scope.tableSpeedMenuOption=[];
			 }
		}
	}
});

function organizerViewControllerFunction($scope,sbiModule_translate, sbiModule_config,sbiModule_user){

	$scope.sbiModule_config = sbiModule_config;
	$scope.clickDocument=function(item){
		 $scope.selectDocumentAction({doc: item});
	}
	
	$scope.translate=sbiModule_translate;
	
	$scope.sbiModule_user = sbiModule_user;
	
	$scope.showMoveDocument = sbiModule_user.functionalities.indexOf("WorkspaceMoveDocument")>-1;


}
})();