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

	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);
	
angular
	.module('main_toolbar_workspace', ['ngMaterial'])

	.directive('mainToolbarWorkspace', function () {		
		 return {			 
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: currentScriptPath + '../../../templates/mainToolbarWorkspace.html',
		      controller: toolbarController
		  };	  
	});

function toolbarController($scope,$mdSidenav,sbiModule_translate,sbiModule_user){
	
	$scope.showOrganizeFolder = (sbiModule_user.functionalities.indexOf("SelfServiceFolderManagement")>-1)? true:false;

	$scope.showCockpitAnalysisButton = (sbiModule_user.functionalities.indexOf("CreateSelfSelviceCockpit")>-1)? true:false;
    $scope.showGeoreportAnalysisButton = (sbiModule_user.functionalities.indexOf("CreateSelfSelviceGeoreport")>-1)? true:false;
    $scope.showKpiAnalysisButton = (sbiModule_user.functionalities.indexOf("CreateSelfSelviceKpi")>-1)? true:false;

    $scope.showCreateDocumentButton = $scope.showCockpitAnalysisButton || $scope.showGeoreportAnalysisButton || $scope.showKpiAnalysisButton;

	//$scope.openedSidebar = $mdSidenav('leftWorkspaceSideNav').isOpen();
	$scope.translate = sbiModule_translate;
	
	$scope.toggleNav = function(){
		$scope.toggleLeftNav();
	}
	
}
})();