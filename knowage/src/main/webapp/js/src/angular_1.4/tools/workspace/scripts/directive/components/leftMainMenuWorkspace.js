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
	.module('left_main_menu_workspace', [])

	/**
	 * The HTML content for the main menu on the left panel of the Workspace web page.
	 * It offers five views for various files that user has added to its personal
	 * workspace (folders, documents, datasets).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	.directive('leftMainMenuWorkspace', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: currentScriptPath + '../../../templates/leftMainMenuWorkspace.html',
		      controller: leftMenuController
		  };
	});

function leftMenuController($scope, sbiModule_translate, sbiModule_user){



	$scope.showMyData= (sbiModule_user.functionalities.indexOf("SeeMyData")>-1)? true:false;
	$scope.showMyWorkspace = (sbiModule_user.functionalities.indexOf("SeeMyWorkspace")>-1)? true:false;
	$scope.showAnalysis= (sbiModule_user.functionalities.indexOf("CreateDocument")>-1)? true:false;
    $scope.showOrganizer= (sbiModule_user.functionalities.indexOf("SaveIntoFolderFunctionality")>-1)? true:false;
    $scope.showSnapshots= (sbiModule_user.functionalities.indexOf("SeeSnapshotsFunctionality")>-1 && sbiModule_user.functionalities.indexOf("ViewScheduledWorkspace")>-1)? true:false;
    $scope.runSnapshots= (sbiModule_user.functionalities.indexOf("RunSnapshotsFunctionality")>-1)? true:false;

    $scope.showScheduler= $scope.showSnapshots ? true : false;  // previously used variable


    $scope.isUserAdmin= isAdmin;
    $scope.isUserDeveloper = isDeveloper;

    /**
     * Smart Filters are temporarily invisible as option (user should not be able to see it under the Data option).
     * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    var smartFilterVisible = false;

	/**
	 * Left main menu options and their associated icons for the Angular list on the
	 * the Workspace web page.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.leftMainMenu =
	[
  	 	{
  	 		"name": "Recent",
  	 		"label":sbiModule_translate.load('sbi.workspace.menu.recent'),
  	 		"icon": "fa fa-clock-o",
  	 		"visible":true,
  	 		"active":false,
  	 		"selected":true
 		},

  	 	{
			"name": "Documents",
			"label":sbiModule_translate.load('sbi.workspace.menu.documents'),
			"icon": "fa fa-file-text",
			"visible":$scope.showOrganizer && !$scope.isUserAdmin ,
			"active":false,
			"selected":false
		},

  	 	{
			"name": "Data",
			"label":sbiModule_translate.load('sbi.workspace.menu.data'),
			"icon": "fa fa-angle-down",
			"activeIcon":"fa fa-angle-up",
			"visible":$scope.showMyData || $scope.isUserAdmin || $scope.isUserDeveloper,
			"active":false,

			"submenuOptions":
 			[
 			 	{
 			 		"name": "Datasets",
 			 		"label":sbiModule_translate.load('sbi.workspace.menu.datasets'),
 			 		"icon": "fa fa-database",
 			 		"visible":!$scope.isUserAdmin, "selected":false
		 		},

          	 	{
		 			"name": "Models",
		 			"label":sbiModule_translate.load('sbi.workspace.menu.models'),
		 			"icon": "fa fa-cubes",
		 			"visible":true,
		 			"selected":false
		 		},

          	 	{
		 			"name": "SmartFilters",
		 			"label":sbiModule_translate.load('sbi.workspace.menu.smartFilter'),
		 			"icon": "fa fa-filter",
		 			"visible":smartFilterVisible,
		 			"selected":false
	 			}
             ]
  	 	},

  	 	{
  	 		"name": "Analysis",
  	 		"label":sbiModule_translate.load('sbi.workspace.menu.analysis'),
  	 		"icon": "fa fa-calculator",
  	 		"visible":$scope.showAnalysis ,
  	 		"active":false,
  	 		"selected":false
 		},

 		{
  	 		"name": "Schedulation",
  	 		"label":sbiModule_translate.load('sbi.schedulation.title'),
  	 		"icon": "fa fa-calendar",
  	 		"visible": !$scope.isUserAdmin && !$scope.isUserDeveloper && $scope.showSnapshots,
  	 		"active":false,
  	 		"selected":false
 		}
 	];

	$scope.selectedMenuItem=$scope.leftMainMenu[0];

	$scope.setExpanded=function(option){

		option.active=!option.active;

		/**
		 * If the currently selected item from the left main menu is one of three suboptions of the 'Data' option (Datasets, Models, SmartFilters) and the Data option
		 * is collapsed, the Data option will be selected by taking the 'true' value of the model that it's style tracks - the 'suboptionActive' value.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.isSuboptionActive = $scope.currentOptionMainMenu=="models" || $scope.currentOptionMainMenu=="datasets" || $scope.currentOptionMainMenu=="smartfilters";

		if ($scope.isSuboptionActive && !option.active) {
			$scope.suboptionActive = true;
		}
		else {
			$scope.suboptionActive = false;
		}
	}

	$scope.selectMenuItem= function(item){
		$scope.selectedMenuItem.selected=false;// previous to false
		$scope.selectedMenuItem=item; // change selected
		$scope.selectedMenuItem.selected=true; // mark new selected true

	}

	$scope.optionVisibility = function(option){
		if(option.name=="Data"){
			return false;
		} else {
			return option.submenuOptions==undefined && option.visible;
		}
	}


	$scope.getMenuFromName= function(menuName){
		var found = false;
		var selectedMenu;

		if($scope.leftMainMenu){
			for (i=0; i<$scope.leftMainMenu.length && found==false; i++) {

				if($scope.leftMainMenu[i].submenuOptions){
					for (j=0; j<$scope.leftMainMenu[i].submenuOptions.length && found==false; j++) {

						if($scope.leftMainMenu[i].submenuOptions[j].name.toLowerCase() == menuName){
							selectedMenu = $scope.leftMainMenu[i].submenuOptions[j];
							found = true;
						}
					}
				}

				if($scope.leftMainMenu[i].name.toLowerCase() == menuName){
					selectedMenu = $scope.leftMainMenu[i];
					found = true;
				}
			}
		}
		return selectedMenu;
	}


//	var currentOptMenuInit = $scope.$watch('currentOptionMainMenu',function(newValue, oldValue){
//		var selectedMenu = null;
//		var found = false;
//
//		if($scope.leftMainMenu){
//			for (i=0; i<$scope.leftMainMenu.length && found==false; i++) {
//
//				if($scope.leftMainMenu[i].submenuOptions){
//					for (j=0; j<$scope.leftMainMenu[i].submenuOptions.length && found==false; j++) {
//
//						if($scope.leftMainMenu[i].submenuOptions[j].name.toLowerCase() == newValue){
//							selectedMenu = $scope.leftMainMenu[i].submenuOptions[j];
//							found = true;
//						}
//					}
//				}
//
//				if($scope.leftMainMenu[i].name.toLowerCase() == newValue){
//					selectedMenu = $scope.leftMainMenu[i];
//					found = true;
//				}
//			}
//		}
//
//
//		$scope.leftMenuItemPicked(selectedMenu,true);
//
//		currentOptMenuInit();
//	});


}
})();